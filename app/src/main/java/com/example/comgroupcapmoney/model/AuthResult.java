package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

public class AuthResult {
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
