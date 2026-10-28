package com.example.comgroupcapmoney.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.comgroupcapmoney.data.local.dao.TransactionDao;
import com.example.comgroupcapmoney.data.local.dao.WalletDao;
import com.example.comgroupcapmoney.data.local.entity.TransactionEntity;
import com.example.comgroupcapmoney.data.local.entity.WalletEntity;

/**
 * AppDatabase — singleton Room Database cho toàn ứng dụng.
 *
 * Phụ trách: Thành viên 3 (Member 3)
 * version = 1 — thêm entity mới phải tăng version và cung cấp Migration.
 */
@Database(
    entities = { TransactionEntity.class, WalletEntity.class },
    version  = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "capmoney.db";
    private static volatile AppDatabase instance;

    /** Trả về DAO để thao tác với bảng cached_transactions. */
    public abstract TransactionDao transactionDao();

    /** Trả về DAO để thao tác với bảng cached_wallets. */
    public abstract WalletDao walletDao();

    /**
     * Lấy singleton instance của database.
     * Thread-safe nhờ double-checked locking.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                        )
                        // fallbackToDestructiveMigration: nếu version tăng mà không có Migration
                        // thì xóa và tạo lại DB (dữ liệu cache có thể mất — chấp nhận được).
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return instance;
    }
}
