package com.example.cameragallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraGallery";
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_STORAGE = 102;
    private static final int REQUEST_MANAGE_STORAGE = 103;
    private static final int REQUEST_CAPTURE_IMAGE = 104;
    private static final int REQUEST_PICK_FOLDER = 105;

    private String currentPhotoPath;
    private Uri saveFolderUri;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "CameraPrefs";
    private static final String KEY_FOLDER_URI = "folder_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "App started");

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUri = prefs.getString(KEY_FOLDER_URI, null);
        if (savedUri != null) {
            saveFolderUri = Uri.parse(savedUri);
        }

        setupUI();
    }

    private void setupUI() {
        TextView tvLocation = findViewById(R.id.tvSaveLocation);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnViewGallery = findViewById(R.id.btnViewGallery);
        Button btnChangeFolder = findViewById(R.id.btnChangeSaveFolder);

        // Update location text
        updateLocationText(tvLocation);

        btnTakePhoto.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            } else if (saveFolderUri == null) {
                showFolderDialog();
            } else {
                openCamera();
            }
        });

        btnViewGallery.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else if (saveFolderUri == null) {
                showFolderDialog();
            } else {
                openGallery();
            }
        });

        btnChangeFolder.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else {
                pickFolder();
            }
        });
    }

    private void updateLocationText(TextView textView) {
        runOnUiThread(() -> {
            if (saveFolderUri != null) {
                textView.setText("Location: " + saveFolderUri.getLastPathSegment());
            } else {
                textView.setText("Location: Not set");
            }
        });
    }

    // ========== Permission Handling ==========
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_STORAGE:
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // ========== Camera Implementation ==========
    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = createImageFile();

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Camera error: " + e.getMessage());
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "IMG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // ========== Folder Selection ==========
    private void pickFolder() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PICK_FOLDER);
        } catch (Exception e) {
            Log.e(TAG, "Folder picker error: " + e.getMessage());
            Toast.makeText(this, "Failed to open folder picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFolderDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Folder Not Selected")
                .setMessage("Please select a folder to save your photos")
                .setPositiveButton("Select Folder", (d, w) -> pickFolder())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ========== Gallery Implementation ==========
    private void openGallery() {
        try {
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtra("folder_uri", saveFolderUri.toString());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Gallery error: " + e.getMessage());
            Toast.makeText(this, "Failed to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== Activity Results ==========
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_MANAGE_STORAGE:
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Storage access granted", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_PICK_FOLDER:
                if (data != null && data.getData() != null) {
                    saveFolderUri = data.getData();
                    getContentResolver().takePersistableUriPermission(
                            saveFolderUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    // Save to preferences
                    prefs.edit().putString(KEY_FOLDER_URI, saveFolderUri.toString()).apply();
                    updateLocationText(findViewById(R.id.tvSaveLocation));
                }
                break;

            case REQUEST_CAPTURE_IMAGE:
                if (currentPhotoPath != null) {
                    saveImageToSelectedFolder();
                }
                break;
        }
    }

    // New method to save captured image to selected folder
    private void saveImageToSelectedFolder() {
        if (saveFolderUri == null) {
            Toast.makeText(this, "No folder selected to save images", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File sourceFile = new File(currentPhotoPath);
            String fileName = sourceFile.getName();

            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, saveFolderUri);
            if (pickedDir == null) {
                Toast.makeText(this, "Cannot access selected folder", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new file in the selected directory
            DocumentFile newFile = pickedDir.createFile("image/jpeg", fileName);
            if (newFile == null) {
                Toast.makeText(this, "Failed to create file in the selected folder", Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy file content
            try (InputStream in = new FileInputStream(sourceFile);
                 OutputStream out = getContentResolver().openOutputStream(newFile.getUri())) {

                if (out == null) {
                    Toast.makeText(this, "Failed to open output stream", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                Toast.makeText(this, "Photo saved to selected folder", Toast.LENGTH_SHORT).show();

                // Delete the temporary file
                if (!sourceFile.delete()) {
                    Log.w(TAG, "Failed to delete temporary file");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving image to selected folder: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to save photo to selected folder", Toast.LENGTH_SHORT).show();
        }
    }
}