package com.example.comgroupcapmoney.ui.budget;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.BudgetRepository;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import java.util.List;

// Thong bao khi khoan chi vua luu lam danh muc cham 80% hoac vuot 100% ngan sach thang (4C-2)
public final class BudgetAlertNotifier {

    public static final int NONE = 0;
    public static final int WARNING = 1;
    public static final int OVER = 2;

    private static final String CHANNEL_ID = "budget_alert";
    private static final String PREFS = "budget_alert_prefs";
    private static final String KEY_ASKED = "asked_permission";
    private static final int NOTIFY_ID_BASE = 4000;

    private BudgetAlertNotifier() {}

    // Chi bao khi VUA vuot nguong do khoan chi nay; da vuot tu truoc thi khong bao lai
    public static int decide(double limit, double spentAfter, double amountAdded) {
        if (limit <= 0 || amountAdded <= 0) return NONE;
        double before = (spentAfter - amountAdded) / limit;
        double after = spentAfter / limit;
        if (before < 1.0 && after >= 1.0) return OVER;
        if (before < 0.8 && after >= 0.8) return WARNING;
        return NONE;
    }

    // Goi sau khi luu khoan chi MOI thanh cong. Moi loi deu bo qua: day la tinh nang phu.
    public static void checkAfterExpense(Context context, Transaction tx) {
        if (tx == null || !tx.isExpense() || tx.getTransactionDate() == null || tx.getTransactionDate().length() < 7) {
            return;
        }
        final Context appContext = context.getApplicationContext();
        int year;
        int month;
        try {
            year = Integer.parseInt(tx.getTransactionDate().substring(0, 4));
            month = Integer.parseInt(tx.getTransactionDate().substring(5, 7));
        } catch (NumberFormatException e) {
            return;
        }
        new BudgetRepository(appContext).getBudgets(month, year, new BudgetRepository.Callback<List<Budget>>() {
            @Override
            public void onSuccess(List<Budget> data) {
                if (data == null) return;
                for (Budget b : data) {
                    if (b.getCategoryId() == tx.getCategoryId()) {
                        int level = decide(b.getLimitAmount(), b.getSpentAmount(), tx.getAmount());
                        if (level != NONE) {
                            show(appContext, b, level);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                // bo qua
            }
        });
    }

    private static void show(Context ctx, Budget b, int level) {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (!nm.areNotificationsEnabled()) return;

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    ctx.getString(R.string.budget_alert_channel), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        String title;
        String text;
        if (level == OVER) {
            title = ctx.getString(R.string.budget_alert_over_title);
            text = ctx.getString(R.string.budget_alert_over_text, b.getCategoryName(),
                    CurrencyUtils.format(b.getSpentAmount()), CurrencyUtils.format(b.getLimitAmount()));
        } else {
            int percent = (int) Math.floor(b.getSpentAmount() * 100 / b.getLimitAmount());
            title = ctx.getString(R.string.budget_alert_warning_title);
            text = ctx.getString(R.string.budget_alert_warning_text, b.getCategoryName(), percent, b.getMonth());
        }

        // Bam vao thong bao: mo app (khong sua MainActivity de nhay thang Tab Ngan sach)
        Intent open = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        PendingIntent pi = open != null
                ? PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
                : null;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_budget_alert)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        if (pi != null) builder.setContentIntent(pi);

        try {
            // Cung danh muc thi thay thong bao cu
            nm.notify(NOTIFY_ID_BASE + b.getCategoryId(), builder.build());
        } catch (SecurityException ignored) {
            // Nguoi dung vua thu hoi quyen
        }
    }

    // Lan dau dat ngan sach (Android 13+): xin quyen thong bao, tu choi thi khong hoi lai
    public static void maybeAskPermission(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < 33) return;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_ASKED, false)) return;
        prefs.edit().putBoolean(KEY_ASKED, true).apply();
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 4001);
    }
}
