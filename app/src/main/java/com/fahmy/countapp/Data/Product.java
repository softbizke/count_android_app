package com.fahmy.countapp.Data;

public class Product {
    private final String id, name, barcode;

    public Product(String id, String name, String barcode) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public String getBarcode() {
        return barcode;
    }

    @Override
    public String toString() {
        return name; // important for AutoCompleteTextView to show the name
    }
}

