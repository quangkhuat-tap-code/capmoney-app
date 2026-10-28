package com.example.comgroupcapmoney.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import com.example.comgroupcapmoney.utils.SingleEvent;

public final class SessionManager {
    private SessionManager() {}

    // Su kien "phien het han" dung chung cho cac man khong dung RepoCallback (Phan 1, Phan 3)
    private static final MutableLiveData<SingleEvent<Boolean>> EXPIRED = new MutableLiveData<>();

    // Goi khi bam Dang xuat, hoac khi API bao 401 (token het han)
    public static void logout(Activity activity, boolean expired) {
        EXPIRED.setValue(null);
        SharedPrefManager.getInstance(activity).clear();
        if (expired) {
            Toast.makeText(activity, R.string.auth_session_expired, Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }

    public static LiveData<SingleEvent<Boolean>> expiredEvents() {
        return EXPIRED;
    }

    // Repository goi khi API can token tra 401
    public static void notifyExpired() {
        set(new SingleEvent<>(true));
    }

    // Dang nhap thanh cong: bo su kien 401 con sot tu phien cu (request tra ve sau khi da dang xuat)
    public static void resetExpired() {
        set(null);
    }

    // Man hinh dang mo nhan su kien thi ve man dang nhap, giong cac man Phan 2
    public static void observeExpired(LifecycleOwner owner, Activity activity) {
        EXPIRED.observe(owner, event -> {
            if (event != null && event.getContentIfNotHandled() != null) {
                logout(activity, true);
            }
        });
    }

    private static void set(SingleEvent<Boolean> value) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            EXPIRED.setValue(value);
        } else {
            EXPIRED.postValue(value);
        }
    }
}
