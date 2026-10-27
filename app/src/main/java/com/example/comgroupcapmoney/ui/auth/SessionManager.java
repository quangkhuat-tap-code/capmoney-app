package com.example.comgroupcapmoney.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.utils.SharedPrefManager;

public final class SessionManager {
    private SessionManager() {}

    // Goi khi bam Dang xuat, hoac khi API bao 401 (token het han)
    public static void logout(Activity activity, boolean expired) {
        SharedPrefManager.getInstance(activity).clear();
        if (expired) {
            Toast.makeText(activity, R.string.auth_session_expired, Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
