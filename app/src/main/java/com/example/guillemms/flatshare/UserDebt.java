package com.example.guillemms.flatshare;

public class UserDebt {
    private String userName;
    private String userDebt;

    public UserDebt(String userName, String userDebt) {
        this.userName = userName;
        this.userDebt = userDebt;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserDebt() {
        return userDebt;
    }
}
