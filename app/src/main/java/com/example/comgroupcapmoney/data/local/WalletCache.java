package com.example.comgroupcapmoney.data.local;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Doc/ghi cache danh sach vi cua user dang dang nhap (4C-3).
 * Giong TransactionCache: chay nen, ket qua tra ve main thread.
 */
public class WalletCache {

    /** Luon goi tren main thread. */
    public interface Callback<T> {
        void onResult(T data);
    }

    private static final int NO_USER = -1;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Context appContext;
    private final WalletDao dao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public WalletCache(Context context) {
        this.appContext = context.getApplicationContext();
        this.dao = AppDatabase.getInstance(appContext).walletDao();
    }

    private int currentUserId() {
        return SharedPrefManager.getInstance(appContext).getUserId();
    }

    /** Thay toan bo vi cua user hien tai bang danh sach moi tu server. */
    public void saveWallets(List<Wallet> wallets) {
        final int userId = currentUserId();
        if (userId == NO_USER) return;
        final long now = System.currentTimeMillis();
        final List<WalletEntity> entities = new ArrayList<>();
        if (wallets != null) {
            for (int i = 0; i < wallets.size(); i++) {
                Wallet w = wallets.get(i);
                if (w != null) {
                    entities.add(WalletEntity.from(w, userId, i, now));
                }
            }
        }
        EXECUTOR.execute(() -> dao.replaceAll(userId, entities));
    }

    /** Rong neu chua co du lieu luu (hoac chua dang nhap). cachedAt = 0 neu chua luu. */
    public void getWallets(Callback<List<Wallet>> cb, Callback<Long> cachedAtCb) {
        final int userId = currentUserId();
        EXECUTOR.execute(() -> {
            List<Wallet> result = new ArrayList<>();
            Long at = null;
            if (userId != NO_USER) {
                for (WalletEntity e : dao.getAll(userId)) {
                    result.add(e.toWallet());
                }
                at = dao.getCachedAt(userId);
            }
            final long cachedAt = at != null ? at : 0L;
            mainHandler.post(() -> {
                cachedAtCb.onResult(cachedAt);
                cb.onResult(result);
            });
        });
    }
}
