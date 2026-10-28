package com.example.comgroupcapmoney.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity — ánh xạ với bảng "cached_transactions" trong SQLite local.
 * Lưu cache danh sách giao dịch tải về từ server để xem khi offline.
 */
@Entity(tableName = "cached_transactions")
public class TransactionEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "wallet_id")
    public int walletId;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "image_url")
    public String imageUrl;

    @ColumnInfo(name = "type")
    public String type;         // "expense" | "income"

    @ColumnInfo(name = "transaction_date")
    public String transactionDate;

    @ColumnInfo(name = "category_name")
    public String categoryName;

    @ColumnInfo(name = "category_icon")
    public String categoryIcon;

    @ColumnInfo(name = "wallet_name")
    public String walletName;

    // Tháng/năm để lọc offline nhanh (không cần parse date string)
    @ColumnInfo(name = "month")
    public int month;

    @ColumnInfo(name = "year")
    public int year;
}
