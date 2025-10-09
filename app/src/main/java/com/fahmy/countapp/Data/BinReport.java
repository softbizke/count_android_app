package com.fahmy.countapp.Data;

public class BinReport {
    private int ringCount;
    private String binId, binType, endingTime, totalBales, comments, ended_by;

    public BinReport(String binId, int ringCount, String binType, String totalBales, String endingTime, String ended_by, String comments) {
        this.binId = binId;
        this.ringCount = ringCount;
        this.binType = binType;
        this.totalBales = totalBales;
        this.endingTime = endingTime;
        this.ended_by = ended_by;
        this.comments = comments;
    }

    public String getBinId() {
        return binId;
    }

    public void setBinId(String binId) {
        this.binId = binId;
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

    public String getEnded_by() {
        return ended_by;
    }

    public void setEnded_by(String ended_by) {
        this.ended_by = ended_by;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
