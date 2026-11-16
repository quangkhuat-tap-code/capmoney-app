package com.example.comgroupcapmoney.ui.stats;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.local.TransactionCache;
import com.example.comgroupcapmoney.data.local.TransactionEntity;
import com.example.comgroupcapmoney.data.local.WalletCache;
import com.example.comgroupcapmoney.data.repository.StatsRepository;
import com.example.comgroupcapmoney.model.StatsSummary;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.model.TrendPoint;
import com.example.comgroupcapmoney.model.Wallet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsViewModel extends AndroidViewModel {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_WALLET = 1;
    public static final int FILTER_BANK = 2;

    private final StatsRepository repository;
    private final TransactionCache cache;

    private int month;
    private int year;
    private String type = StatsSummary.TYPE_EXPENSE;

    private final MutableLiveData<Integer> walletFilter = new MutableLiveData<>(FILTER_ALL);
    private int currentFilter = FILTER_ALL;
    private List<Transaction> monthTransactions = new ArrayList<>();
    private final Map<Integer, String> walletTypes = new HashMap<>();

    /** Moi lan goi load() tang len; ket qua ve (ca tu API lan tu cache) ma requestId cu thi bo. */
    private int requestId = 0;
    private boolean started = false;

    /** {month, year} */
    private final MutableLiveData<int[]> period = new MutableLiveData<>();
    private final MutableLiveData<String> typeLiveData = new MutableLiveData<>(StatsSummary.TYPE_EXPENSE);
    private final MutableLiveData<StatsSummary> summary = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    /** null = so lieu online; khac null = dang xem cache, gia tri la thoi diem luu (millis). */
    private final MutableLiveData<Long> offlineCachedAt = new MutableLiveData<>(null);

    // Bieu do 6 thang (4C-1)
    public static final int TREND_MONTHS = 6;
    public static final int TREND_LOADING = 0;
    public static final int TREND_OK = 1;
    public static final int TREND_EMPTY = 2;
    public static final int TREND_OFFLINE = 3;
    public static final int TREND_ERROR = 4;
    private final MutableLiveData<List<TrendPoint>> trend = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> trendState = new MutableLiveData<>(TREND_LOADING);
    private int trendRequestId = 0;

    public StatsViewModel(@NonNull Application application) {
        super(application);
        repository = new StatsRepository(application);
        cache = new TransactionCache(application);

        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        period.setValue(new int[]{month, year});

        loadWalletTypes();
    }

    public LiveData<int[]> getPeriod() { return period; }
    public LiveData<String> getType() { return typeLiveData; }
    public LiveData<Integer> getWalletFilter() { return walletFilter; }
    public LiveData<StatsSummary> getSummary() { return summary; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Long> getOfflineCachedAt() { return offlineCachedAt; }
    public LiveData<List<TrendPoint>> getTrend() { return trend; }
    public LiveData<Integer> getTrendState() { return trendState; }

    /** Da tung goi load() chua (de Fragment biet co can tai lai sau khi xoay man hinh). */
    public boolean hasStarted() {
        return started;
    }

    public boolean isCurrentMonth() {
        Calendar now = Calendar.getInstance();
        return year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH) + 1;
    }

    public void prevMonth() {
        month--;
        if (month < 1) {
            month = 12;
            year--;
        }
        onSelectionChanged(true);
    }

    public void nextMonth() {
        if (isCurrentMonth()) return;
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        onSelectionChanged(true);
    }

    public void setWalletFilter(int filter) {
        if (currentFilter == filter) return;
        currentFilter = filter;
        walletFilter.setValue(filter);
        applyFilter();
    }

    public void loadWalletTypes() {
        WalletCache walletCache = new WalletCache(getApplication());
        walletCache.getWallets(wallets -> {
            if (wallets != null) {
                for (Wallet w : wallets) {
                    if (w != null) {
                        walletTypes.put(w.getId(), w.getType());
                    }
                }
            }
            applyFilter();
        }, cachedAt -> {});
    }

    private void applyFilter() {
        if (monthTransactions == null) return;
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction tx : monthTransactions) {
            if (matchesFilter(tx, currentFilter)) {
                filtered.add(tx);
            }
        }
        StatsSummary calculated = StatsCalculator.fromTransactions(filtered, month, year, type);
        showSummary(calculated, offlineCachedAt.getValue());
    }

    private boolean matchesFilter(Transaction tx, int filter) {
        if (filter == FILTER_ALL) return true;
        String wType = walletTypes.get(tx.getWalletId());
        String name = tx.getWalletName();
        String lower = name != null ? name.toLowerCase(Locale.getDefault()) : "";

        if (filter == FILTER_WALLET) {
            if ("cash".equalsIgnoreCase(wType)) return true;
            if (lower.contains("wallet") || lower.contains("ví") || lower.contains("vi")
                    || lower.contains("tiền mặt") || lower.contains("tien mat") || lower.contains("cash")) {
                return true;
            }
            if (!"bank".equalsIgnoreCase(wType) && !lower.contains("bank")
                    && !lower.contains("ngân hàng") && !lower.contains("chuyển khoản") && !lower.contains("ck")) {
                return true;
            }
            return false;
        } else if (filter == FILTER_BANK) {
            if ("bank".equalsIgnoreCase(wType)) return true;
            if (lower.contains("bank") || lower.contains("ngân hàng") || lower.contains("ngan hang")
                    || lower.contains("chuyển khoản") || lower.contains("chuyen khoan") || lower.contains("ck")) {
                return true;
            }
            return false;
        }
        return true;
    }

    public void setType(String newType) {
        if (newType == null || newType.equals(type)) return;
        type = newType;
        typeLiveData.setValue(newType);
        // Khi đổi loại Thu/Chi, tính lại summary từ transactions đã có
        applyFilter();
    }

    private void onSelectionChanged(boolean monthChanged) {
        period.setValue(new int[]{month, year});
        summary.setValue(null);
        offlineCachedAt.setValue(null);
        load(monthChanged);
    }

    public void load() {
        load(true);
    }

    private void load(boolean withTrend) {
        if (withTrend) {
            loadTrend(month, year);
        }
        started = true;
        final int id = ++requestId;
        final int m = month;
        final int y = year;
        final String t = type;

        loading.setValue(true);
        error.setValue(null);

        repository.getTransactions(m, y, new StatsRepository.Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                if (id != requestId) return;
                monthTransactions = data != null ? data : new ArrayList<>();
                cache.saveMonth(m, y, monthTransactions);
                applyFilter();
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (id != requestId) return;
                if (isNetworkError) {
                    loadFromCache(id, m, y, t);
                } else {
                    showError(message);
                }
            }
        });
    }

    // 6 thang ket thuc o thang dang chon; mat mang thi khong co (Room chi luu thang da mo)
    private void loadTrend(int m, int y) {
        final int id = ++trendRequestId;
        trendState.setValue(TREND_LOADING);
        repository.getTrend(m, y, TREND_MONTHS, new StatsRepository.Callback<List<TrendPoint>>() {
            @Override
            public void onSuccess(List<TrendPoint> data) {
                if (id != trendRequestId) return;
                boolean allZero = true;
                if (data != null) {
                    for (TrendPoint p : data) {
                        if (p.getTotalExpense() > 0 || p.getTotalIncome() > 0) {
                            allZero = false;
                            break;
                        }
                    }
                }
                trend.setValue(data);
                trendState.setValue(allZero ? TREND_EMPTY : TREND_OK);
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (id != trendRequestId) return;
                trend.setValue(null);
                trendState.setValue(isNetworkError ? TREND_OFFLINE : TREND_ERROR);
            }
        });
    }

    /** Luu giao dich thang dang xem vao Room; loi buoc nay bo qua, khong bao. */
    private void refreshCache(int m, int y) {
        repository.getTransactions(m, y, new StatsRepository.Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                cache.saveMonth(m, y, data);
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                // bo qua
            }
        });
    }

    private void loadFromCache(int id, int m, int y, String t) {
        cache.getMonth(m, y, items -> {
            if (id != requestId) return;
            if (items == null || items.isEmpty()) {
                showError(getApplication().getString(R.string.stats_offline_no_cache));
                return;
            }
            cache.getCachedAt(m, y, cachedAt -> {
                if (id != requestId) return;
                List<Transaction> list = new ArrayList<>();
                for (TransactionEntity e : items) {
                    list.add(e.toTransaction());
                }
                monthTransactions = list;
                offlineCachedAt.setValue(cachedAt);
                applyFilter();
            });
        });
    }

    private void showSummary(StatsSummary data, Long cachedAt) {
        error.setValue(null);
        offlineCachedAt.setValue(cachedAt);
        summary.setValue(data);
        loading.setValue(false);
    }

    private void showError(String message) {
        summary.setValue(null);
        offlineCachedAt.setValue(null);
        error.setValue(message);
        loading.setValue(false);
    }
}
