package com.example.comgroupcapmoney.model;

import com.example.comgroupcapmoney.data.api.ApiClient;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction implements Serializable {
    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";

    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    @SerializedName("type")
    private String type;

    @SerializedName("note")
    private String note;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("transaction_date")
    private String transactionDate;

    @SerializedName("wallet_id")
    private int walletId;

    @SerializedName("wallet_name")
    private String walletName;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("category_icon")
    private String categoryIcon;

    @SerializedName("category_color")
    private String categoryColor;

    public Transaction() {}

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public int getWalletId() {
        return walletId;
    }

    public String getWalletName() {
        return walletName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public boolean isExpense() {
        return TYPE_EXPENSE.equalsIgnoreCase(type);
    }

    public String getFullImageUrl() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        return ApiClient.IMAGE_BASE_URL + imageUrl;
    }

    public Date getParsedDate() {
        if (transactionDate == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return sdf.parse(transactionDate);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getDayKey() {
        if (transactionDate != null && transactionDate.length() >= 10) {
            return transactionDate.substring(0, 10);
        }
        return "";
    }
}
