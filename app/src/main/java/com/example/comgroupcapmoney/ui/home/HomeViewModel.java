package com.example.comgroupcapmoney.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.local.TransactionCache;
import com.example.comgroupcapmoney.data.local.WalletCache;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.data.local.TransactionEntity;
import com.example.comgroupcapmoney.data.repository.TransactionRepository;
import com.example.comgroupcapmoney.model.DeleteTransactionResult;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.SingleEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    public static final int MODE_GRID = 0;
    public static final int MODE_CALENDAR = 1;

    public static final int FILTER_ALL = 0;
    public static final int FILTER_WALLET = 1;
    public static final int FILTER_BANK = 2;

    private final TransactionRepository repository;
    private int requestSeq = 0;

    private int month;
    private int year;

    private final MutableLiveData<Integer> monthLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> yearLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> viewMode = new MutableLiveData<>(MODE_GRID);
    private final MutableLiveData<Integer> walletFilter = new MutableLiveData<>(FILTER_ALL);
    private int currentFilter = FILTER_ALL;
    private List<Transaction> allTransactions = new ArrayList<>();
    private final Map<Integer, String> walletTypes = new HashMap<>();

    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<double[]> totals = new MutableLiveData<>(new double[]{0.0, 0.0});
    private final MutableLiveData<List<CalendarDay>> calendarDays = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SingleEvent<String>> event = new MutableLiveData<>();
    // Khac null = dang hien du lieu luu trong Room vi mat mang (gia tri la thoi diem luu)
    private final MutableLiveData<Long> offlineCachedAt = new MutableLiveData<>(null);
    private final TransactionCache cache;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TransactionRepository(application);
        this.cache = new TransactionCache(application);

        Calendar now = Calendar.getInstance();
        this.month = now.get(Calendar.MONTH) + 1;
        this.year = now.get(Calendar.YEAR);

        monthLiveData.setValue(month);
        yearLiveData.setValue(year);

        loadWalletTypes();
        load();
    }

    public LiveData<Integer> getMonth() { return monthLiveData; }
    public LiveData<Integer> getYear() { return yearLiveData; }
    public LiveData<Integer> getViewMode() { return viewMode; }
    public LiveData<Integer> getWalletFilter() { return walletFilter; }
    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public LiveData<double[]> getTotals() { return totals; }
    public LiveData<List<CalendarDay>> getCalendarDays() { return calendarDays; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<SingleEvent<String>> getEvent() { return event; }
    public LiveData<Long> getOfflineCachedAt() { return offlineCachedAt; }

    public void setViewMode(int mode) {
        viewMode.setValue(mode);
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
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction tx : allTransactions) {
            if (matchesFilter(tx, currentFilter)) {
                filtered.add(tx);
            }
        }
        transactions.setValue(filtered);
        calculateTotalsAndCalendar(filtered);
    }

    private boolean matchesFilter(Transaction tx, int filter) {
        if (filter == FILTER_ALL) {
            return true;
        }
        String type = walletTypes.get(tx.getWalletId());
        String name = tx.getWalletName();
        String lowerName = name != null ? name.toLowerCase(Locale.getDefault()) : "";

        if (filter == FILTER_WALLET) {
            // Ví / Tiền mặt
            if ("cash".equalsIgnoreCase(type)) {
                return true;
            }
            if (lowerName.contains("wallet") || lowerName.contains("ví") || lowerName.contains("vi")
                    || lowerName.contains("tiền mặt") || lowerName.contains("tien mat") || lowerName.contains("cash")) {
                return true;
            }
            // Mặc định nếu không phải ngân hàng/chuyển khoản thì thuộc ví tiền mặt
            if (!"bank".equalsIgnoreCase(type) && !lowerName.contains("bank")
                    && !lowerName.contains("ngân hàng") && !lowerName.contains("chuyển khoản") && !lowerName.contains("ck")) {
                return true;
            }
            return false;
        } else if (filter == FILTER_BANK) {
            // Ngân hàng / Chuyển khoản
            if ("bank".equalsIgnoreCase(type)) {
                return true;
            }
            if (lowerName.contains("bank") || lowerName.contains("ngân hàng") || lowerName.contains("ngan hang")
                    || lowerName.contains("chuyển khoản") || lowerName.contains("chuyen khoan") || lowerName.contains("ck")) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isCurrentMonth() {
        Calendar now = Calendar.getInstance();
        return year == now.get(Calendar.YEAR) && month == (now.get(Calendar.MONTH) + 1);
    }

    public void prevMonth() {
        month--;
        if (month < 1) {
            month = 12;
            year--;
        }
        monthLiveData.setValue(month);
        yearLiveData.setValue(year);
        load();
    }

    public void nextMonth() {
        if (isCurrentMonth()) return;

        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        monthLiveData.setValue(month);
        yearLiveData.setValue(year);
        load();
    }

    public void goToCurrentMonth() {
        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        monthLiveData.setValue(month);
        yearLiveData.setValue(year);
        load();
    }

    public void load() {
        final int currentSeq = ++requestSeq;
        final int m = month;
        final int y = year;

        List<Transaction> currentList = transactions.getValue();
        if (currentList == null || currentList.isEmpty()) {
            loading.setValue(true);
        }

        repository.getTransactions(month, year, new TransactionRepository.ResultCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                if (currentSeq != requestSeq) return;

                loading.setValue(false);
                error.setValue(null);
                offlineCachedAt.setValue(null);

                List<Transaction> list = data != null ? data : new ArrayList<>();
                allTransactions = list;
                cache.saveMonth(m, y, list);

                applyFilter();
            }

            @Override
            public void onError(String message) {
                if (currentSeq != requestSeq) return;

                if (getApplication().getString(R.string.capture_error_network).equals(message)) {
                    // Mat mang: thu doc giao dich da luu cua thang nay
                    loadFromCache(m, y, currentSeq, message);
                    return;
                }
                showLoadError(message);
            }
        });
    }

    private void loadFromCache(int m, int y, int seq, String networkMessage) {
        cache.getCachedAt(m, y, cachedAt -> {
            if (seq != requestSeq) return;
            if (cachedAt == null || cachedAt == 0L) {
                // Thang nay chua tung tai luc co mang: bo du lieu thang cu, hien loi + Thu lai
                List<Transaction> empty = new ArrayList<>();
                allTransactions = empty;
                loading.setValue(false);
                offlineCachedAt.setValue(null);
                applyFilter();
                error.setValue(networkMessage);
                return;
            }
            cache.getMonth(m, y, items -> {
                if (seq != requestSeq) return;
                List<Transaction> list = new ArrayList<>();
                for (TransactionEntity e : items) {
                    list.add(e.toTransaction());
                }
                allTransactions = list;
                loading.setValue(false);
                error.setValue(null);
                offlineCachedAt.setValue(cachedAt);
                applyFilter();
            });
        });
    }

    private void showLoadError(String message) {
        loading.setValue(false);
        List<Transaction> list = transactions.getValue();
        if (list == null || list.isEmpty()) {
            error.setValue(message);
        } else {
            event.setValue(new SingleEvent<>(message));
        }
    }

    public void delete(int id) {
        repository.deleteTransaction(id, new TransactionRepository.ResultCallback<DeleteTransactionResult>() {
            @Override
            public void onSuccess(DeleteTransactionResult result) {
                if (allTransactions != null) {
                    List<Transaction> updated = new ArrayList<>();
                    for (Transaction tx : allTransactions) {
                        if (tx.getId() != id) {
                            updated.add(tx);
                        }
                    }
                    if (updated.size() == allTransactions.size()) {
                        event.setValue(new SingleEvent<>(getApplication().getString(R.string.home_deleted)));
                        return;
                    }
                    allTransactions = updated;
                    applyFilter();
                    cache.saveMonth(month, year, updated);
                }
                event.setValue(new SingleEvent<>(getApplication().getString(R.string.home_deleted)));
            }

            @Override
            public void onError(String message) {
                event.setValue(new SingleEvent<>(message));
            }
        });
    }

    private void calculateTotalsAndCalendar(List<Transaction> list) {
        double expense = 0;
        double income = 0;
        Map<String, List<Transaction>> grouped = new HashMap<>();

        for (Transaction tx : list) {
            if (tx.isExpense()) {
                expense += tx.getAmount();
            } else {
                income += tx.getAmount();
            }
            String dayKey = tx.getDayKey();
            if (!grouped.containsKey(dayKey)) {
                grouped.put(dayKey, new ArrayList<>());
            }
            grouped.get(dayKey).add(tx);
        }
        totals.setValue(new double[]{expense, income});

        // Compute Calendar days
        Calendar firstDayCal = Calendar.getInstance();
        firstDayCal.set(year, month - 1, 1, 0, 0, 0);

        // Day of week: Sunday = 1, Monday = 2...
        // Mon = 0, Tue = 1, ..., Sun = 6
        int dayOfWeek = (firstDayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int maxDays = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar now = Calendar.getInstance();
        int nowY = now.get(Calendar.YEAR);
        int nowM = now.get(Calendar.MONTH) + 1;
        int nowD = now.get(Calendar.DAY_OF_MONTH);

        int totalCells = ((dayOfWeek + maxDays + 6) / 7) * 7;
        List<CalendarDay> days = new ArrayList<>(totalCells);

        for (int i = 0; i < totalCells; i++) {
            if (i < dayOfWeek || i >= dayOfWeek + maxDays) {
                days.add(new CalendarDay(0, month, year, false, false, ""));
            } else {
                int dayNum = i - dayOfWeek + 1;
                boolean isToday = (year == nowY && month == nowM && dayNum == nowD);
                String dayKey = String.format("%04d-%02d-%02d", year, month, dayNum);

                CalendarDay day = new CalendarDay(dayNum, month, year, true, isToday, dayKey);
                day.addTransactions(grouped.get(dayKey));
                days.add(day);
            }
        }
        calendarDays.setValue(days);
    }
}
