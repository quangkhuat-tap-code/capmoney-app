package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class StatsSummary {
    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("type")
    private String type;

    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    @SerializedName("total")
    private double total;

    @SerializedName("categories")
    private List<CategoryStat> categories;

    public StatsSummary() {}

    public StatsSummary(int month, int year, String type, double totalIncome, double totalExpense,
                        double total, List<CategoryStat> categories) {
        this.month = month;
        this.year = year;
        this.type = type;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.total = total;
        this.categories = categories;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getType() {
        return type;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getTotal() {
        return total;
    }

    /** Khong bao gio tra null. */
    public List<CategoryStat> getCategories() {
        return categories != null ? categories : new ArrayList<>();
    }

    public boolean isIncome() {
        return TYPE_INCOME.equals(type);
    }
}
