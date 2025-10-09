package com.fahmy.countapp.Data;

public enum UserRoles {
    ADMIN("ADMIN"),
    OPERATOR("OPERATOR"),
    MILLER("MILLER"),
    CONTROLLER("CONTROLLER"),
    BRAN_POLLARD_OPERATOR("BRAN_POLLARD_OPERATOR");
    private final String value;

    UserRoles(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Choose the default environment
    public static final UserRoles CURRENT = OPERATOR;
}
