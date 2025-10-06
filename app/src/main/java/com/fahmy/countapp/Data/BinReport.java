package com.fahmy.countapp.Data;

public class BinReport {
    private int ringCount;
    private String binType, endingTime, totalBales;

    public BinReport(int ringCount, String binType, String totalBales, String endingTime) {
        this.ringCount = ringCount;
        this.binType = binType;
        this.totalBales = totalBales;
        this.endingTime = endingTime;
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
}
