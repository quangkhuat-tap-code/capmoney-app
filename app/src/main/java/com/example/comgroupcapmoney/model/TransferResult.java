package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

public class TransferResult {
    @SerializedName("transfer")
    private Transfer transfer;

    @SerializedName("from_wallet")
    private Wallet fromWallet;

    @SerializedName("to_wallet")
    private Wallet toWallet;

    public Transfer getTransfer() {
        return transfer;
    }

    public Wallet getFromWallet() {
        return fromWallet;
    }

    public Wallet getToWallet() {
        return toWallet;
    }

    public static class Transfer {
        @SerializedName("id")
        private int id;

        @SerializedName("amount")
        private double amount;

        public int getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }
    }
}
