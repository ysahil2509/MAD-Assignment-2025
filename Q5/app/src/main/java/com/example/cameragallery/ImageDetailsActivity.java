package com.example.cameragallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;

import java.util.Date;

public class ImageDetailsActivity extends AppCompatActivity {
    private static final String TAG = "ImageDetailsActivity";

    private ImageView ivImage;
    private TextView tvName, tvPath, tvSize, tvDate;
    private Button btnDelete;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);
        Log.d(TAG, "ImageDetailsActivity created");

        initializeViews();
        handleIntent();
    }

    private void initializeViews() {
        ivImage = findViewById(R.id.ivImage);
        tvName = findViewById(R.id.tvName);
        tvPath = findViewById(R.id.tvPath);
        tvSize = findViewById(R.id.tvSize);
        tvDate = findViewById(R.id.tvDate);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String uriString = intent.getStringExtra("imageUri");
            String name = intent.getStringExtra("imageName");
            String path = intent.getStringExtra("imagePath");
            long size = intent.getLongExtra("imageSize", 0);
            long date = intent.getLongExtra("imageDate", 0);

            if (uriString != null && !uriString.isEmpty()) {
                imageUri = Uri.parse(uriString);
                displayImageDetails(name, path, size, date);
                setupDeleteButton();
            } else {
                showErrorAndFinish("Invalid image data");
            }
        } else {
            showErrorAndFinish("No image data received");
        }
    }

    private void displayImageDetails(String name, String path, long size, long date) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .fitCenter()
                    .into(ivImage);

            tvName.setText(String.format("Name: %s", name != null ? name : "Unknown"));
            tvPath.setText(String.format("Path: %s", path != null ? path : "Unknown"));
            tvSize.setText(String.format("Size: %s", Formatter.formatFileSize(this, size)));
            tvDate.setText(String.format("Date: %s", DateFormat.format("dd/MM/yyyy hh:mm:ss a", new Date(date))));
        } catch (Exception e) {
            Log.e(TAG, "Error displaying image details: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading image details", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (d, w) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage() {
        try {
            DocumentFile file = DocumentFile.fromSingleUri(this, imageUri);
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + e.getMessage(), e);
            Toast.makeText(this, "Error deleting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}