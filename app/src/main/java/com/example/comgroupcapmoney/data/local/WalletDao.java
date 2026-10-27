package com.example.comgroupcapmoney.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

@Dao
public abstract class WalletDao {

    @Query("SELECT * FROM wallet_cache WHERE user_id = :userId ORDER BY sort_order ASC")
    public abstract List<WalletEntity> getAll(int userId);

    @Query("DELETE FROM wallet_cache WHERE user_id = :userId")
    public abstract void deleteAll(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<WalletEntity> items);

    /** null neu chua luu lan nao. */
    @Query("SELECT MAX(cached_at) FROM wallet_cache WHERE user_id = :userId")
    public abstract Long getCachedAt(int userId);

    @Transaction
    public void replaceAll(int userId, List<WalletEntity> items) {
        deleteAll(userId);   // vi da xoa tren server cung bien mat khoi cache
        insertAll(items);
    }
}
