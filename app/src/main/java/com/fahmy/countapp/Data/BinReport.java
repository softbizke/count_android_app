package com.fahmy.countapp.Data;

public class BinReport {
    private int ringCount;
    private String binType, endingTime, totalBales, comments;

    public BinReport(int ringCount, String binType, String totalBales, String endingTime, String comments) {
        this.ringCount = ringCount;
        this.binType = binType;
        this.totalBales = totalBales;
        this.endingTime = endingTime;
        this.comments = comments;
    }

    public int getRingCount() {
        return ringCount;
    }

    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
    }

    public String getBinType() {
        return binType;
    }

    public void setBinType(String binType) {
        this.binType = binType;
    }

    public String getTotalBales() {
        return totalBales;
    }

    public void setTotalBales(String totalBales) {
        this.totalBales = totalBales;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(String endingTime) {
        this.endingTime = endingTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
