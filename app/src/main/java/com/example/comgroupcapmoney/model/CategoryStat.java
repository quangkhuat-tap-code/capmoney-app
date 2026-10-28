package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO phần trăm từng danh mục — parse từ mảng "categories"
 * trong response GET /api/analytics/summary.
 * Dùng để vẽ từng lát Donut Chart và hiển thị danh sách bên dưới.
 */
public class CategoryStat {

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("color_hex")
    private String colorHex;

    @SerializedName("total")
    private double total;

    @SerializedName("percentage")
    private double percentage;

    // ─── Getters ───────────────────────────────────────────

    public int getCategoryId() { return categoryId; }

    public String getName() { return name; }

    public String getIcon() { return icon; }

    public String getColorHex() { return colorHex; }

    public double getTotal() { return total; }

    public double getPercentage() { return percentage; }
}
