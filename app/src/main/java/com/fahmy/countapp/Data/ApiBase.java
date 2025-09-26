package com.fahmy.countapp.Data;

public enum ApiBase {
    DEV("http://192.168.2.202:5000/api");

    private final String url;

    ApiBase(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    // Choose the default environment
    public static final ApiBase CURRENT = DEV;
}
