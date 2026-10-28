package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO Danh mục thu/chi — parse từ GET /api/categories.
 * Dùng trong AddBudgetDialog (Spinner chọn danh mục).
 */
public class Category {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private Integer userId;          // null nếu là danh mục hệ thống

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("color_hex")
    private String colorHex;

    @SerializedName("type")
    private String type;             // "expense" | "income"

    // ─── Getters ───────────────────────────────────────────

    public int getId() { return id; }

    public Integer getUserId() { return userId; }

    public String getName() { return name; }

    public String getIcon() { return icon; }

    public String getColorHex() { return colorHex; }

    public String getType() { return type; }
}
