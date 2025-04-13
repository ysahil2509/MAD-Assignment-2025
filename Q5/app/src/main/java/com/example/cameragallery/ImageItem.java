package com.example.cameragallery;

public class ImageItem {
    private final String uri;
    private final String name;
    private final String path;
    private final long size;
    private final long date;

    public ImageItem(String uri, String name, String path, long size, long date) {
        this.uri = uri;
        this.name = name;
        this.path = path;
        this.size = size;
        this.date = date;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getDate() {
        return date;
    }
}