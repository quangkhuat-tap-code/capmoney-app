package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

// 1 lan chuyen tien giua 2 vi, tu GET /api/wallets/transfers (4B-1)
public class Transfer {

    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    @SerializedName("note")
    private String note;

    @SerializedName("transfer_date")
    private String transferDate;

    @SerializedName("from_wallet_id")
    private int fromWalletId;

    @SerializedName("from_wallet_name")
    private String fromWalletName;

    @SerializedName("to_wallet_id")
    private int toWalletId;

    @SerializedName("to_wallet_name")
    private String toWalletName;

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public String getTransferDate() { return transferDate; }
    public int getFromWalletId() { return fromWalletId; }
    public String getFromWalletName() { return fromWalletName; }
    public int getToWalletId() { return toWalletId; }
    public String getToWalletName() { return toWalletName; }
}
