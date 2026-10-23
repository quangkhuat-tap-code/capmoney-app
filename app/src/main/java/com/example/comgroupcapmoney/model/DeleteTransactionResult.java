package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class DeleteTransactionResult implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("wallet_id")
    private int walletId;

    @SerializedName("wallet_balance")
    private double walletBalance;

    public DeleteTransactionResult() {}

    public int getId() {
        return id;
    }

    public int getWalletId() {
        return walletId;
    }

    public double getWalletBalance() {
        return walletBalance;
    }
}
