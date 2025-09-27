package com.fahmy.countapp.Data;

public class User {
    private String userId, phoneNo, role;

    public User(String userId, String phoneNo, String role) {
        this.userId = userId;
        this.phoneNo = phoneNo;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
