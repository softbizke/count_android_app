package com.fahmy.countapp.Data;

public class AttaData {

    private final String bags, addedByName, comments, totalBales;

    public AttaData(String bags, String addedByName, String totalBales, String comments) {
        this.bags = bags;
        this.addedByName = addedByName;
        this.comments = comments;
        this.totalBales = totalBales;
    }

    public String getBags() {
        return bags;
    }

    public String getAddedByName() {
        return addedByName;
    }

    public String getComments() {
        return comments;
    }

    public String getTotalBales() {
        return totalBales;
    }
}
