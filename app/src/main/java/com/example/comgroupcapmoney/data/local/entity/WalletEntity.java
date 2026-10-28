package com.example.comgroupcapmoney.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity — ánh xạ với bảng "cached_wallets" trong SQLite local.
 * Lưu cache danh sách ví để xem Tab Tài khoản khi offline.
 */
@Entity(tableName = "cached_wallets")
public class WalletEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "balance")
    public double balance;

    @ColumnInfo(name = "type")
    public String type;         // "cash" | "bank" | "saving"

    @ColumnInfo(name = "is_default")
    public int isDefault;       // 0 = false, 1 = true
}
