package com.example.comgroupcapmoney.data.local;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lop duy nhat cac man khac dung de doc/ghi cache giao dich.
 * Room cam chay tren main thread: moi thao tac chay nen, ket qua tra ve main thread.
 */
public class TransactionCache {

    /** Luon goi tren main thread. */
    public interface Callback<T> {
        void onResult(T data);
    }

    private static final int NO_USER = -1;

    // Dung chung cho moi instance de cac lenh ghi/doc chay lan luot, dung thu tu
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Context appContext;
    private final TransactionDao dao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TransactionCache(Context context) {
        this.appContext = context.getApplicationContext();
        this.dao = AppDatabase.getInstance(appContext).transactionDao();
    }

    private int currentUserId() {
        return SharedPrefManager.getInstance(appContext).getUserId();
    }

    /** Thay toan bo giao dich cua thang nay (cua user hien tai) bang danh sach moi. */
    public void saveMonth(int month, int year, List<Transaction> items) {
        final int userId = currentUserId();
        if (userId == NO_USER) return;

        final long now = System.currentTimeMillis();
        final List<TransactionEntity> entities = new ArrayList<>();
        if (items != null) {
            for (Transaction t : items) {
                if (t != null) {
                    entities.add(TransactionEntity.from(t, userId, month, year, now));
                }
            }
        }
        EXECUTOR.execute(() -> dao.replaceMonth(userId, month, year, entities));
    }

    /** Rong neu chua co du lieu luu (hoac chua dang nhap). */
    public void getMonth(int month, int year, Callback<List<TransactionEntity>> cb) {
        final int userId = currentUserId();
        EXECUTOR.execute(() -> {
            List<TransactionEntity> result = null;
            if (userId != NO_USER) {
                result = dao.getMonth(userId, month, year);
            }
            final List<TransactionEntity> data = result != null ? result : new ArrayList<>();
            mainHandler.post(() -> cb.onResult(data));
        });
    }

    /** 0 neu chua co du lieu luu. */
    public void getCachedAt(int month, int year, Callback<Long> cb) {
        final int userId = currentUserId();
        EXECUTOR.execute(() -> {
            Long at = null;
            if (userId != NO_USER) {
                at = dao.getCachedAt(userId, month, year);
            }
            final long data = at != null ? at : 0L;
            mainHandler.post(() -> cb.onResult(data));
        });
    }
}
