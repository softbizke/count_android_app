package com.fahmy.countapp.Data;

public class ProductEntry {

    private String productTitle, productDescription, openingCount, closingCount, totalCount, totalBales, photo_path;

    public ProductEntry(String productTitle, String productDescription, String openingCount, String closingCount, String totalCount, String totalBales, String photo_path) {
        this.productTitle = productTitle;
        this.productDescription = productDescription;
        this.openingCount = openingCount;
        this.closingCount = closingCount;
        this.totalCount = totalCount;
        this.totalBales = totalBales;
        this.photo_path = photo_path;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getOpeningCount() {
        return openingCount;
    }

    public void setOpeningCount(String openingCount) {
        this.openingCount = openingCount;
    }

    public String getClosingCount() {
        return closingCount;
    }

    public void setClosingCount(String closingCount) {
        this.closingCount = closingCount;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getTotalBales() {
        return totalBales;
    }

    public void setTotalBales(String totalBales) {
        this.totalBales = totalBales;
    }


    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhoto_path(String photo_path) {
        this.photo_path = photo_path;
    }
}
