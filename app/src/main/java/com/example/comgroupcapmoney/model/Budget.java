package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Budget implements Serializable {
    public static final String STATUS_OK = "ok";
    public static final String STATUS_WARNING = "warning";
    public static final String STATUS_OVER = "over";

    @SerializedName("id")
    private int id;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("category_icon")
    private String categoryIcon;

    @SerializedName("category_color")
    private String categoryColor;

    @SerializedName("limit_amount")
    private double limitAmount;

    @SerializedName("spent_amount")
    private double spentAmount;

    @SerializedName("remaining")
    private double remaining;

    @SerializedName("percent")
    private int percent;

    @SerializedName("status")
    private String status;

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    public Budget() {}

    public int getId() {
        return id;
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

    public double getLimitAmount() {
        return limitAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public double getRemaining() {
        return remaining;
    }

    public int getPercent() {
        return percent;
    }

    public String getStatus() {
        return status;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
