package com.example.comgroupcapmoney.ui.stats;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.data.repository.StatsRepository;
import com.example.comgroupcapmoney.model.StatsSummary;
import java.util.ArrayList;
import java.util.Calendar;

public class StatsViewModel extends AndroidViewModel {

    private final StatsRepository repository;

    private int month;
    private int year;
    private String type = StatsSummary.TYPE_EXPENSE;

    /** Moi lan goi load() tang len; ket qua ve ma requestId cu thi bo. */
    private int requestId = 0;
    private boolean started = false;

    /** {month, year} */
    private final MutableLiveData<int[]> period = new MutableLiveData<>();
    private final MutableLiveData<String> typeLiveData = new MutableLiveData<>(StatsSummary.TYPE_EXPENSE);
    private final MutableLiveData<StatsSummary> summary = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public StatsViewModel(@NonNull Application application) {
        super(application);
        repository = new StatsRepository(application);

        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH) + 1;
        year = now.get(Calendar.YEAR);
        period.setValue(new int[]{month, year});
    }

    public LiveData<int[]> getPeriod() { return period; }
    public LiveData<String> getType() { return typeLiveData; }
    public LiveData<StatsSummary> getSummary() { return summary; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

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
        onSelectionChanged();
    }

    public void nextMonth() {
        if (isCurrentMonth()) return;
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        onSelectionChanged();
    }

    public void setType(String newType) {
        if (newType == null || newType.equals(type)) return;
        type = newType;
        typeLiveData.setValue(newType);
        onSelectionChanged();
    }

    private void onSelectionChanged() {
        period.setValue(new int[]{month, year});
        // Bo so lieu cua thang/tab cu de khong hien nham trong luc tai
        summary.setValue(null);
        load();
    }

    public void load() {
        started = true;
        final int id = ++requestId;
        final int m = month;
        final int y = year;
        final String t = type;

        loading.setValue(true);
        error.setValue(null);

        repository.getSummary(m, y, t, new StatsRepository.Callback<StatsSummary>() {
            @Override
            public void onSuccess(StatsSummary data) {
                if (id != requestId) return;
                summary.setValue(data != null ? data
                        : new StatsSummary(m, y, t, 0, 0, 0, new ArrayList<>()));
                loading.setValue(false);
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (id != requestId) return;
                summary.setValue(null);
                error.setValue(message);
                loading.setValue(false);
            }
        });
    }
}
