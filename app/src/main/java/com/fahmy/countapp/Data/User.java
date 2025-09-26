package com.fahmy.countapp.Data;

public class User {
    private String userId, phoneNo;

    public User(String userId, String phoneNo) {
        this.userId = userId;
        this.phoneNo = phoneNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
