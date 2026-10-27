package com.example.comgroupcapmoney.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.example.comgroupcapmoney.model.Wallet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

// Ban sao danh sach vi de Tab Tai khoan xem duoc khi mat mang (4C-3)
@Entity(tableName = "wallet_cache", indices = {@Index("user_id")})
public class WalletEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "balance")
    public double balance;

    @ColumnInfo(name = "is_default")
    public boolean isDefault;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    // Giu dung thu tu server tra ve (mac dinh truoc, roi theo id)
    @ColumnInfo(name = "sort_order")
    public int sortOrder;

    @ColumnInfo(name = "cached_at")
    public long cachedAt;

    // Wallet chi doc tu JSON (is_default kieu Object) nen dung lai Gson, giong TransactionEntity
    public Wallet toWallet() {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("name", name);
        o.addProperty("balance", balance);
        o.addProperty("type", type);
        o.addProperty("is_default", isDefault);
        o.addProperty("created_at", createdAt);
        return new Gson().fromJson(o, Wallet.class);
    }

    public static WalletEntity from(Wallet w, int userId, int sortOrder, long now) {
        WalletEntity e = new WalletEntity();
        e.id = w.getId();
        e.userId = userId;
        e.name = w.getName();
        e.type = w.getType();
        e.balance = w.getBalance();
        e.isDefault = w.isDefault();
        e.createdAt = w.getCreatedAt();
        e.sortOrder = sortOrder;
        e.cachedAt = now;
        return e;
    }
}
