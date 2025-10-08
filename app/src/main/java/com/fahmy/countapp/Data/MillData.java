package com.fahmy.countapp.Data;

public class MillData {
    private final String millCapacity, millExtraction, photo_path, machine, comments;

    public MillData(String machine, String millCapacity, String millExtraction, String photo_path, String comments) {
        this.machine = machine;
        this.millCapacity = millCapacity;
        this.millExtraction = millExtraction;
        this.photo_path = photo_path;
        this.comments = comments;
    }

    public String getMachine() {
        return machine;
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

    public String getComments() {
        return comments;
    }
}
