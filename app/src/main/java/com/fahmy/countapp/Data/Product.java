package com.fahmy.countapp.Data;

public class Product {
    private final String id, name;

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name; // important for AutoCompleteTextView to show the name
    }
}

