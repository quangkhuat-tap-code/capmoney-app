package com.example.comgroupcapmoney.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO Ngân sách — dùng để parse dữ liệu từ GET /api/budgets
 * và hiển thị ở Tab 4 BudgetFragment.
 */
public class Budget {

    @SerializedName("id")
    private int id;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("limit_amount")
    private double limitAmount;

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("created_at")
    private String createdAt;

    // Thông tin danh mục (JOIN trong SQL)
    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("category_icon")
    private String categoryIcon;

    @SerializedName("category_color")
    private String categoryColor;

    // Số tiền đã tiêu thực tế trong tháng (truy vấn con trong SQL)
    @SerializedName("spent_amount")
    private double spentAmount;

    // ─── Getters ───────────────────────────────────────────

    public int getId() { return id; }

    public int getCategoryId() { return categoryId; }

    public double getLimitAmount() { return limitAmount; }

    public int getMonth() { return month; }

    public int getYear() { return year; }

    public String getCreatedAt() { return createdAt; }

    public String getCategoryName() { return categoryName; }

    public String getCategoryIcon() { return categoryIcon; }

    public String getCategoryColor() { return categoryColor; }

    public double getSpentAmount() { return spentAmount; }

    // ─── Setters (dùng trong AddBudgetDialog) ──────────────

    public void setId(int id) { this.id = id; }

    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }

    public void setMonth(int month) { this.month = month; }

    public void setYear(int year) { this.year = year; }

    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    // ─── Helper ────────────────────────────────────────────

    /**
     * Tính % đã tiêu so với hạn mức.
     * Trả về giá trị 0.0 – 1.0 (float, để dùng với ProgressBar max=100).
     */
    public float getProgressRatio() {
        if (limitAmount <= 0) return 0f;
        return (float) (spentAmount / limitAmount);
    }

    /**
     * Tính % đã tiêu, trả về 0–100 (int cho ProgressBar.setProgress).
     */
    public int getProgressPercent() {
        return Math.min((int) (getProgressRatio() * 100), 100);
    }
}
