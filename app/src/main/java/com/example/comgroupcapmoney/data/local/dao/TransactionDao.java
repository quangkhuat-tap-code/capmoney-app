package com.example.comgroupcapmoney.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.comgroupcapmoney.data.local.entity.TransactionEntity;

import java.util.List;

/**
 * DAO cho bảng cached_transactions.
 * Member 3 dùng để lưu/đọc cache giao dịch khi offline.
 */
@Dao
public interface TransactionDao {

    /**
     * Lưu / cập nhật danh sách giao dịch vào cache.
     * REPLACE để ghi đè nếu đã có bản ghi cùng PK (id).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TransactionEntity> transactions);

    /**
     * Lấy tất cả giao dịch đã cache (mọi tháng, sắp theo ngày mới nhất).
     */
    @Query("SELECT * FROM cached_transactions ORDER BY transaction_date DESC")
    List<TransactionEntity> getAll();

    /**
     * Lấy giao dịch theo tháng / năm — dùng để hiển thị Tab Home offline.
     */
    @Query("SELECT * FROM cached_transactions WHERE month = :month AND year = :year ORDER BY transaction_date DESC")
    List<TransactionEntity> getByMonthYear(int month, int year);

    /**
     * Xóa toàn bộ cache (dùng khi người dùng đăng xuất hoặc làm sạch dữ liệu).
     */
    @Query("DELETE FROM cached_transactions")
    void deleteAll();

    /**
     * Xóa cache theo tháng / năm (dùng khi muốn làm mới dữ liệu của 1 tháng).
     */
    @Query("DELETE FROM cached_transactions WHERE month = :month AND year = :year")
    void deleteByMonthYear(int month, int year);
}
