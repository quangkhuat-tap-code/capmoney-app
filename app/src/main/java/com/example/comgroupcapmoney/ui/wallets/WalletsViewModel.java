package com.example.comgroupcapmoney.ui.wallets;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.local.TransactionCache;
import com.example.comgroupcapmoney.data.local.WalletCache;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.data.repository.StatsRepository;
import com.example.comgroupcapmoney.data.repository.WalletRepository;
import com.example.comgroupcapmoney.model.StatsSummary;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.ui.stats.StatsCalculator;
import com.example.comgroupcapmoney.utils.SingleEvent;
import java.util.Calendar;
import java.util.List;

public class WalletsViewModel extends AndroidViewModel {

    private final WalletRepository repository;
    private final StatsRepository statsRepository;
    private final TransactionCache transactionCache;
    private final WalletCache cache;

    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SingleEvent<Boolean>> unauthorized = new MutableLiveData<>();
    // 4C-3: null = so lieu online; khac null = dang xem ban luu luc nay (millis)
    private final MutableLiveData<Long> offlineCachedAt = new MutableLiveData<>(null);

    // Thong ke theo thang
    private int month;
    private int year;
    private final MutableLiveData<int[]> period = new MutableLiveData<>();
    /** [totalExpense, totalIncome, monthBalance] */
    private final MutableLiveData<double[]> monthSummary = new MutableLiveData<>(new double[]{0.0, 0.0, 0.0});
    private final MutableLiveData<Boolean> monthLoading = new MutableLiveData<>(false);

    public WalletsViewModel(@NonNull Application application) {
        super(application);
        repository = new WalletRepository(application);
        statsRepository = new StatsRepository(application);
        transactionCache = new TransactionCache(application);
        cache = new WalletCache(application);

        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        period.setValue(new int[]{month, year});
    }

    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<SingleEvent<Boolean>> getUnauthorized() { return unauthorized; }
    public LiveData<Long> getOfflineCachedAt() { return offlineCachedAt; }
    public LiveData<int[]> getPeriod() { return period; }
    public LiveData<double[]> getMonthSummary() { return monthSummary; }
    public LiveData<Boolean> getMonthLoading() { return monthLoading; }

    public boolean isOffline() {
        return offlineCachedAt.getValue() != null;
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
        period.setValue(new int[]{month, year});
        loadMonthSummary();
    }

    public void nextMonth() {
        if (isCurrentMonth()) return;
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        period.setValue(new int[]{month, year});
        loadMonthSummary();
    }

    public void setCurrentMonth() {
        if (isCurrentMonth()) return;
        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        period.setValue(new int[]{month, year});
        loadMonthSummary();
    }

    public void loadMonthSummary() {
        final int m = month;
        final int y = year;
        monthLoading.setValue(true);

        statsRepository.getSummary(m, y, "expense", new StatsRepository.Callback<StatsSummary>() {
            @Override
            public void onSuccess(StatsSummary data) {
                if (m != month || y != year) return;
                monthLoading.setValue(false);
                if (data != null) {
                    double exp = data.getTotalExpense();
                    double inc = data.getTotalIncome();
                    double bal = inc - exp;
                    monthSummary.setValue(new double[]{exp, inc, bal});
                } else {
                    monthSummary.setValue(new double[]{0.0, 0.0, 0.0});
                }
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (m != month || y != year) return;
                if (isNetworkError) {
                    transactionCache.getMonth(m, y, entities -> {
                        if (m != month || y != year) return;
                        monthLoading.setValue(false);
                        if (entities != null && !entities.isEmpty()) {
                            StatsSummary cached = StatsCalculator.fromCache(entities, m, y, StatsSummary.TYPE_EXPENSE);
                            double exp = cached.getTotalExpense();
                            double inc = cached.getTotalIncome();
                            double bal = inc - exp;
                            monthSummary.setValue(new double[]{exp, inc, bal});
                        } else {
                            monthSummary.setValue(new double[]{0.0, 0.0, 0.0});
                        }
                    });
                } else {
                    monthLoading.setValue(false);
                    monthSummary.setValue(new double[]{0.0, 0.0, 0.0});
                }
            }
        });
    }

    public void loadWallets() {
        loadMonthSummary();
        if (Boolean.TRUE.equals(loading.getValue())) return;
        loading.setValue(true);
        error.setValue(null);
        repository.getWallets(new RepoCallback<List<Wallet>>() {
            @Override
            public void onSuccess(List<Wallet> data, String message) {
                offlineCachedAt.setValue(null);
                wallets.setValue(data);
                loading.setValue(false);
                cache.saveWallets(data);
            }

            @Override
            public void onError(String message, boolean isUnauthorized) {
                if (isUnauthorized) {
                    loading.setValue(false);
                    unauthorized.setValue(new SingleEvent<>(true));
                } else if (getApplication().getString(R.string.common_error_network).equals(message)) {
                    // Mat mang: hien ban da luu (neu co); loi khac (400, 500) thi bao loi nhu cu
                    loadFromCache(message);
                } else {
                    loading.setValue(false);
                    error.setValue(message);
                }
            }
        });
    }

    private void loadFromCache(String networkMessage) {
        final long[] at = {0};
        cache.getWallets(list -> {
            loading.setValue(false);
            if (list == null || list.isEmpty()) {
                offlineCachedAt.setValue(null);
                error.setValue(networkMessage);
                return;
            }
            offlineCachedAt.setValue(at[0]);
            wallets.setValue(list);
        }, cachedAt -> at[0] = cachedAt);
    }
}
