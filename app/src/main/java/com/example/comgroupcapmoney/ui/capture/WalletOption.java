package com.example.comgroupcapmoney.ui.capture;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class WalletOption implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    @SerializedName("type")
    private String type;

    @SerializedName("is_default")
    private JsonElement isDefaultRaw;

    public WalletOption() {}

    public WalletOption(int id, String name, double balance, String type, JsonElement isDefaultRaw) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.isDefaultRaw = isDefaultRaw;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }

    public boolean isDefault() {
        if (isDefaultRaw == null || isDefaultRaw.isJsonNull()) {
            return false;
        }
        if (isDefaultRaw.isJsonPrimitive()) {
            if (isDefaultRaw.getAsJsonPrimitive().isBoolean()) {
                return isDefaultRaw.getAsBoolean();
            }
            if (isDefaultRaw.getAsJsonPrimitive().isNumber()) {
                return isDefaultRaw.getAsInt() != 0;
            }
        }
        return false;
    }
}
