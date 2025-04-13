package com.example.cameragallery;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private static final String TAG = "ImageAdapter";
    private final Context context;
    private final List<ImageItem> imageList;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(ImageItem image);
    }

    public ImageAdapter(Context context, List<ImageItem> imageList, OnImageClickListener listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem item = imageList.get(position);
        try {
            Uri imageUri = Uri.parse(item.getUri());
            Log.d(TAG, "Loading image: " + imageUri);

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background); // Replace with your placeholder

            Glide.with(context)
                    .load(imageUri)
                    .apply(requestOptions)
                    .into(holder.imageView);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(item);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading image at position " + position + ": " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}