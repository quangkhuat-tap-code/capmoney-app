package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

// Tong chi/thu cua 1 thang, tu GET /api/analytics/trend (4C-1)
public class TrendPoint {

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("total_expense")
    private double totalExpense;

    @SerializedName("total_income")
    private double totalIncome;

    public int getMonth() { return month; }
    public int getYear() { return year; }
    public double getTotalExpense() { return totalExpense; }
    public double getTotalIncome() { return totalIncome; }
}
