package com.fahmy.countapp.Data;

import java.io.Serializable;

public class ProductEntry implements Serializable {

    private String id, prodId, productTitle, productDescription, status, openingCount, closingCount, bags, totalCount, totalBales, totalKgs, openingCountImg, closingCountImg, comments;
    private  boolean isBranPollardOperator;

    public ProductEntry(String id, String productTitle, String productDescription, String openingCount, String closingCount, String bags, String totalCount, String totalBales, String totalKgs,  String openingCountImg, String closingCountImg, String status, String comments, boolean isBranPollardOperator) {
        this.id = id;
        this.productTitle = productTitle;
        this.productDescription = productDescription;
        this.openingCount = openingCount;
        this.closingCount = closingCount;
        this.bags = bags;
        this.totalCount = totalCount;
        this.totalBales = totalBales;
        this.totalKgs = totalKgs;
        this.openingCountImg = openingCountImg;
        this.closingCountImg = closingCountImg;
        this.status = status;
        this.comments = comments;
        this.isBranPollardOperator = isBranPollardOperator;
    }

    public ProductEntry(String id, String prodId, String productTitle, String productDescription, String openingCount, String closingCount, String bags, String totalCount, String totalBales, String totalKgs,  String openingCountImg, String closingCountImg, String status, String comments, boolean isBranPollardOperator) {
        this.id = id;
        this.prodId = prodId;
        this.productTitle = productTitle;
        this.productDescription = productDescription;
        this.openingCount = openingCount;
        this.closingCount = closingCount;
        this.bags = bags;
        this.totalCount = totalCount;
        this.totalBales = totalBales;
        this.totalKgs = totalKgs;
        this.openingCountImg = openingCountImg;
        this.closingCountImg = closingCountImg;
        this.comments = comments;
        this.status = status;
        this.isBranPollardOperator = isBranPollardOperator;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getBags() {
        return bags;
    }

    public void setBags(String bags) {
        this.bags = bags;
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

    public String getTotalKgs() {
        return totalKgs;
    }

    public void setTotalKgs(String totalKgs) {
        this.totalKgs = totalKgs;
    }

    public String getOpeningCountImg() {
        return openingCountImg;
    }

    public void setOpeningCountImg(String openingCountImg) {
        this.openingCountImg = openingCountImg;
    }

    public String getClosingCountImg() {
        return closingCountImg;
    }

    public void setClosingCountImg(String closingCountImg) {
        this.closingCountImg = closingCountImg;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isBranPollardOperator() {
        return isBranPollardOperator;
    }

    public void setBranPollardOperator(boolean branPollardOperator) {
        isBranPollardOperator = branPollardOperator;
    }
}
