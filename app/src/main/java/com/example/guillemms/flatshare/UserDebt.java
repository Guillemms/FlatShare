package com.example.guillemms.flatshare;

public class UserDebt {
    private String userName;
    private float userDebt;

    public UserDebt(String userName, float userDebt) {
        this.userName = userName;
        this.userDebt = userDebt;
    }

    public String getUserName() {
        return userName;
    }

    public float getUserDebt() {
        return userDebt;
    }
}
