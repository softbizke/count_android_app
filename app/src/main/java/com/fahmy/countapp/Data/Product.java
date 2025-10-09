package com.fahmy.countapp.Data;

public class Product {
    private final String id, name, barcode, description;

    public Product(String id, String name, String barcode, String description) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " - " + Util.extractWeight(description) + " Kgs"; // important for AutoCompleteTextView to show the name
    }
}

