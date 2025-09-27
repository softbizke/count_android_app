package com.fahmy.countapp.Data;

public class MillData {
    private final String millCapacity, millExtraction, photo_path;

    public MillData(String millCapacity, String millExtraction, String photo_path) {
        this.millCapacity = millCapacity;
        this.millExtraction = millExtraction;
        this.photo_path = photo_path;
    }

    public String getMillCapacity() {
        return millCapacity;
    }

    public String getMillExtraction() {
        return millExtraction;
    }


    public String getPhoto_path() {
        return photo_path;
    }
}
