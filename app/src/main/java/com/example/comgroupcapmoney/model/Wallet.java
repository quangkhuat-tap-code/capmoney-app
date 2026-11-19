package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Wallet implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    @SerializedName("type")
    private String type;

    // Gson doc true/false thanh Boolean, 1/0 thanh Double: ca hai deu Serializable, truyen qua Bundle duoc
    @SerializedName("is_default")
    private Object isDefaultRaw;

    @SerializedName("created_at")
    private String createdAt;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isDefault() {
        if (isDefaultRaw instanceof Boolean) {
            return (Boolean) isDefaultRaw;
        }
        if (isDefaultRaw instanceof Number) {
            return ((Number) isDefaultRaw).intValue() != 0;
        }
        return false;
    }

    public boolean isSaving() {
        return "saving".equals(type);
    }

    /**
     * Ví thuộc nhóm "Bank" ở bộ lọc Home/Thống kê hay không.
     * Ưu tiên loại ví lưu trên server (type); chỉ khi chưa biết type (chưa mở Tab Tài khoản lần nào)
     * mới đoán theo tên ví. Các ví còn lại (cash, saving) thuộc nhóm "Wallet".
     */
    public static boolean isBankWallet(String type, String walletName) {
        if (type != null && !type.trim().isEmpty()) {
            return "bank".equalsIgnoreCase(type);
        }
        String n = walletName != null ? walletName.toLowerCase(java.util.Locale.ROOT).trim() : "";
        return n.contains("bank") || n.contains("ngân hàng") || n.contains("ngan hang")
                || n.contains("chuyển khoản") || n.contains("chuyen khoan")
                || n.equals("ck") || n.startsWith("ck ") || n.endsWith(" ck") || n.contains(" ck ");
    }

    public String getTypeEmoji() {
        if ("cash".equals(type)) {
            return "💵";
        }
        if ("bank".equals(type)) {
            return "🏦";
        }
        if ("saving".equals(type)) {
            return "🐷";
        }
        return "👛";
    }
}
