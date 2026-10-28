package com.example.comgroupcapmoney.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

@Dao
public abstract class TransactionDao {

    @Query("SELECT * FROM cached_transactions WHERE user_id = :userId AND year = :year AND month = :month ORDER BY transaction_date DESC")
    public abstract List<TransactionEntity> getMonth(int userId, int month, int year);

    @Query("DELETE FROM cached_transactions WHERE user_id = :userId AND year = :year AND month = :month")
    public abstract void deleteMonth(int userId, int month, int year);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<TransactionEntity> items);

    /** null neu thang nay chua co du lieu luu. */
    @Query("SELECT MAX(cached_at) FROM cached_transactions WHERE user_id = :userId AND year = :year AND month = :month")
    public abstract Long getCachedAt(int userId, int month, int year);

    @Transaction
    public void replaceMonth(int userId, int month, int year, List<TransactionEntity> items) {
        deleteMonth(userId, month, year);   // giao dich da xoa tren server cung bien mat khoi cache
        insertAll(items);
    }
}
