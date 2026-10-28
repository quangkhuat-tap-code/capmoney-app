package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * POJO bao ngoài response GET /api/analytics/summary.
 * Trường "data" của ApiResponse<AnalyticsSummary> sẽ được Gson parse vào đây.
 */
public class AnalyticsSummary {

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("type")
    private String type;        // "expense" hoặc "income"

    @SerializedName("total_expense")
    private double totalExpense;

    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("categories")
    private List<CategoryStat> categories;

    // ─── Getters ───────────────────────────────────────────

    public int getMonth() { return month; }

    public int getYear() { return year; }

    public String getType() { return type; }

    public double getTotalExpense() { return totalExpense; }

    public double getTotalIncome() { return totalIncome; }

    public List<CategoryStat> getCategories() { return categories; }
}
