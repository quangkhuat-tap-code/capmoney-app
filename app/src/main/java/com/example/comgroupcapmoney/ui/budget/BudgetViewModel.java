package com.example.comgroupcapmoney.ui.budget;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.data.repository.BudgetRepository;
import com.example.comgroupcapmoney.model.Budget;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository repository;

    private int month;
    private int year;

    /** Moi lan goi load() tang len; ket qua ve ma requestId cu thi bo. */
    private int requestId = 0;
    private boolean started = false;

    /** {month, year} */
    private final MutableLiveData<int[]> period = new MutableLiveData<>();
    /** null = chua co du lieu cua thang dang chon. */
    private final MutableLiveData<List<Budget>> budgets = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new BudgetRepository(application);

        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        period.setValue(new int[]{month, year});
    }

    public LiveData<int[]> getPeriod() { return period; }
    public LiveData<List<Budget>> getBudgets() { return budgets; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public int getMonthValue() { return month; }
    public int getYearValue() { return year; }

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
        onMonthChanged();
    }

    public void nextMonth() {
        if (isCurrentMonth()) return;
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        onMonthChanged();
    }

    private void onMonthChanged() {
        period.setValue(new int[]{month, year});
        // Bo danh sach cua thang cu de khong hien nham trong luc tai
        budgets.setValue(null);
        load();
    }

    public void load() {
        started = true;
        final int id = ++requestId;

        loading.setValue(true);
        error.setValue(null);

        repository.getBudgets(month, year, new BudgetRepository.Callback<List<Budget>>() {
            @Override
            public void onSuccess(List<Budget> data) {
                if (id != requestId) return;
                budgets.setValue(data != null ? data : new ArrayList<>());
                loading.setValue(false);
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (id != requestId) return;
                budgets.setValue(null);
                error.setValue(message);
                loading.setValue(false);
            }
        });
    }
}
