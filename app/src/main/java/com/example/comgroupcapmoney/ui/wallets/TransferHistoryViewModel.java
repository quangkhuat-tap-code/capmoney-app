package com.example.comgroupcapmoney.ui.wallets;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.data.repository.WalletRepository;
import com.example.comgroupcapmoney.model.Transfer;
import com.example.comgroupcapmoney.utils.SingleEvent;
import java.util.Calendar;
import java.util.List;

// Lich su chuyen tien theo thang (4B-1); xoay man hinh giu thang dang chon
public class TransferHistoryViewModel extends AndroidViewModel {

    private final WalletRepository repository;

    private final MutableLiveData<int[]> monthYear = new MutableLiveData<>();
    private final MutableLiveData<List<Transfer>> transfers = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SingleEvent<Boolean>> unauthorized = new MutableLiveData<>();

    private int month;
    private int year;
    private int seq = 0;

    public TransferHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new WalletRepository(application);
        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        monthYear.setValue(new int[]{month, year});
        load();
    }

    public LiveData<int[]> getMonthYear() { return monthYear; }
    public LiveData<List<Transfer>> getTransfers() { return transfers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<SingleEvent<Boolean>> getUnauthorized() { return unauthorized; }

    public boolean isCurrentMonth() {
        Calendar now = Calendar.getInstance();
        return month == now.get(Calendar.MONTH) + 1 && year == now.get(Calendar.YEAR);
    }

    public void prevMonth() {
        month--;
        if (month < 1) {
            month = 12;
            year--;
        }
        monthYear.setValue(new int[]{month, year});
        load();
    }

    public void nextMonth() {
        if (isCurrentMonth()) return;
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        monthYear.setValue(new int[]{month, year});
        load();
    }

    public void load() {
        final int mySeq = ++seq;
        loading.setValue(true);
        error.setValue(null);
        repository.getTransfers(month, year, new RepoCallback<List<Transfer>>() {
            @Override
            public void onSuccess(List<Transfer> data, String message) {
                if (mySeq != seq) return;
                loading.setValue(false);
                transfers.setValue(data);
            }

            @Override
            public void onError(String message, boolean isUnauthorized) {
                if (mySeq != seq) return;
                loading.setValue(false);
                if (isUnauthorized) {
                    unauthorized.setValue(new SingleEvent<>(true));
                    return;
                }
                transfers.setValue(null);
                error.setValue(message);
            }
        });
    }
}
