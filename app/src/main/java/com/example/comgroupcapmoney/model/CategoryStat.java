package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

public class CategoryStat {
    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("category_icon")
    private String categoryIcon;

    @SerializedName("category_color")
    private String categoryColor;

    @SerializedName("amount")
    private double amount;

    @SerializedName("count")
    private int count;

    @SerializedName("percent")
    private double percent;

    public CategoryStat() {}

    public CategoryStat(int categoryId, String categoryName, String categoryIcon, String categoryColor,
                        double amount, int count, double percent) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.amount = amount;
        this.count = count;
        this.percent = percent;
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

    public double getAmount() {
        return amount;
    }

    public int getCount() {
        return count;
    }

    public double getPercent() {
        return percent;
    }
}
