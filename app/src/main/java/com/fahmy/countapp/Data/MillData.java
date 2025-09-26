package com.fahmy.countapp.Data;

public class MillData {
    private final String millCapacity, millExtraction;

    public MillData(String millCapacity, String millExtraction) {
        this.millCapacity = millCapacity;
        this.millExtraction = millExtraction;
    }

    public String getMillCapacity() {
        return millCapacity;
    }

    public String getMillExtraction() {
        return millExtraction;
    }
}
