package com.example.comgroupcapmoney.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.comgroupcapmoney.data.local.entity.WalletEntity;

import java.util.List;

/**
 * DAO cho bảng cached_wallets.
 */
@Dao
public interface WalletDao {

    /**
     * Lưu / cập nhật danh sách ví vào cache.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WalletEntity> wallets);

    /**
     * Lấy tất cả ví đã cache, sắp theo id.
     */
    @Query("SELECT * FROM cached_wallets ORDER BY id ASC")
    List<WalletEntity> getAll();

    /**
     * Xóa toàn bộ cache ví (dùng khi đăng xuất).
     */
    @Query("DELETE FROM cached_wallets")
    void deleteAll();
}
