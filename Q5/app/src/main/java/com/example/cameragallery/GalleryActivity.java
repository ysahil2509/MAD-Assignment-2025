package com.example.cameragallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity implements ImageAdapter.OnImageClickListener {
    private static final String TAG = "GalleryActivity";
    private static final int REQUEST_IMAGE_DETAILS = 201;

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<ImageItem> imageList;
    private TextView tvEmpty;
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Log.d(TAG, "GalleryActivity created");

        initializeViews();
        handleIntent();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Setup RecyclerView with 3-column grid
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageList = new ArrayList<>();
        adapter = new ImageAdapter(this, imageList, this);
        recyclerView.setAdapter(adapter);
    }

    private void handleIntent() {
        try {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra("folder_uri")) {
                showErrorAndFinish("No folder specified");
                return;
            }

            String folderUriString = intent.getStringExtra("folder_uri");
            if (folderUriString == null || folderUriString.isEmpty()) {
                showErrorAndFinish("Invalid folder URI");
                return;
            }

            folderUri = Uri.parse(folderUriString);
            loadImagesFromFolder();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error: " + e.getMessage(), e);
            showErrorAndFinish("Failed to initialize gallery");
        }
    }

    private void loadImagesFromFolder() {
        new Thread(() -> {
            try {
                DocumentFile directory = DocumentFile.fromTreeUri(this, folderUri);
                if (directory == null || !directory.exists()) {
                    runOnUiThread(() -> {
                        tvEmpty.setText("Folder not accessible");
                        tvEmpty.setVisibility(View.VISIBLE);
                    });
                    return;
                }

                List<ImageItem> tempList = new ArrayList<>();
                DocumentFile[] files = directory.listFiles();
                Log.d(TAG, "Found " + files.length + " files in directory");

                for (DocumentFile file : files) {
                    if (file.isFile() && isImageFile(file.getName())) {
                        Log.d(TAG, "Adding image: " + file.getName());
                        tempList.add(new ImageItem(
                                file.getUri().toString(),
                                file.getName(),
                                file.getUri().getPath(),
                                file.length(),
                                file.lastModified()
                        ));
                    }
                }

                runOnUiThread(() -> {
                    imageList.clear();
                    imageList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(imageList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (imageList.isEmpty()) {
                        tvEmpty.setText("No images found in this folder");
                    }
                    Log.d(TAG, "Gallery updated with " + imageList.size() + " images");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading images: " + e.getMessage(), e);
                runOnUiThread(() -> showErrorAndFinish("Error loading images: " + e.getMessage()));
            }
        }).start();
    }

    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".jpg") ||
                lowerCase.endsWith(".jpeg") ||
                lowerCase.endsWith(".png") ||
                lowerCase.endsWith(".gif") ||
                lowerCase.endsWith(".bmp");
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onImageClick(ImageItem image) {
        try {
            Intent intent = new Intent(this, ImageDetailsActivity.class);
            intent.putExtra("imageUri", image.getUri());
            intent.putExtra("imageName", image.getName());
            intent.putExtra("imagePath", image.getPath());
            intent.putExtra("imageSize", image.getSize());
            intent.putExtra("imageDate", image.getDate());
            startActivityForResult(intent, REQUEST_IMAGE_DETAILS);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image details: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to open image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_DETAILS && resultCode == RESULT_OK) {
            // Refresh the image list after deletion
            loadImagesFromFolder();
            Toast.makeText(this, "Gallery refreshed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}