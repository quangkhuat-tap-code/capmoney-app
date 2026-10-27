package com.example.comgroupcapmoney.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.example.comgroupcapmoney.model.Transaction;

/** 1 giao dich da tai ve, luu theo user + thang dang xem. */
@Entity(tableName = "cached_transactions",
        indices = {@Index(value = {"user_id", "year", "month"})})
public class TransactionEntity {

    /** id giao dich tren server */
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "month")
    public int month;

    @ColumnInfo(name = "year")
    public int year;

    @ColumnInfo(name = "amount")
    public double amount;

    /** expense / income */
    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "image_url")
    public String imageUrl;

    @ColumnInfo(name = "transaction_date")
    public String transactionDate;

    @ColumnInfo(name = "wallet_id")
    public int walletId;

    @ColumnInfo(name = "wallet_name")
    public String walletName;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "category_name")
    public String categoryName;

    @ColumnInfo(name = "category_icon")
    public String categoryIcon;

    @ColumnInfo(name = "category_color")
    public String categoryColor;

    /** System.currentTimeMillis() luc luu */
    @ColumnInfo(name = "cached_at")
    public long cachedAt;

    public TransactionEntity() {}

    public static TransactionEntity from(Transaction t, int userId, int month, int year, long now) {
        TransactionEntity e = new TransactionEntity();
        e.id = t.getId();
        e.userId = userId;
        e.month = month;
        e.year = year;
        e.amount = t.getAmount();
        e.type = t.getType();
        e.note = t.getNote();
        e.imageUrl = t.getImageUrl();
        e.transactionDate = t.getTransactionDate();
        e.walletId = t.getWalletId();
        e.walletName = t.getWalletName();
        e.categoryId = t.getCategoryId();
        e.categoryName = t.getCategoryName();
        e.categoryIcon = t.getCategoryIcon();
        e.categoryColor = t.getCategoryColor();
        e.cachedAt = now;
        return e;
    }
}
