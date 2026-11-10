package com.example.comgroupcapmoney.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.budget.BudgetFragment;
import com.example.comgroupcapmoney.ui.capture.CaptureExpenseActivity;
import com.example.comgroupcapmoney.ui.home.HomeFragment;
import com.example.comgroupcapmoney.ui.profile.ProfileFragment;
import com.example.comgroupcapmoney.ui.stats.StatsFragment;
import com.example.comgroupcapmoney.ui.wallets.WalletsFragment;

public class MainActivity extends AppCompatActivity {

    private View indicatorHome, indicatorStats, indicatorWallets, indicatorBudget, indicatorProfile;
    private ImageView iconHome, iconStats, iconWallets, iconBudget, iconProfile;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabCapture = findViewById(R.id.fab_capture_expense);

        // Find Tab Views
        View tabHome = findViewById(R.id.tab_home);
        View tabStats = findViewById(R.id.tab_stats);
        View tabWallets = findViewById(R.id.tab_wallets);
        View tabBudget = findViewById(R.id.tab_budget);
        View tabProfile = findViewById(R.id.tab_profile);

        indicatorHome = findViewById(R.id.indicator_home);
        indicatorStats = findViewById(R.id.indicator_stats);
        indicatorWallets = findViewById(R.id.indicator_wallets);
        indicatorBudget = findViewById(R.id.indicator_budget);
        indicatorProfile = findViewById(R.id.indicator_profile);

        iconHome = findViewById(R.id.icon_home);
        iconStats = findViewById(R.id.icon_stats);
        iconWallets = findViewById(R.id.icon_wallets);
        iconBudget = findViewById(R.id.icon_budget);
        iconProfile = findViewById(R.id.icon_profile);

        // Mặc định mở Tab 1 (HomeFragment) khi vừa vào app
        if (savedInstanceState == null) {
            selectTab(0, new HomeFragment());
        }

        // Chuyển Tab qua Floating Pill Navigation Bar
        tabHome.setOnClickListener(v -> selectTab(0, new HomeFragment()));
        tabStats.setOnClickListener(v -> selectTab(1, new StatsFragment()));
        tabWallets.setOnClickListener(v -> selectTab(2, new WalletsFragment()));
        tabBudget.setOnClickListener(v -> selectTab(3, new BudgetFragment()));
        tabProfile.setOnClickListener(v -> selectTab(4, new ProfileFragment()));

        // Bấm nút (+) mở màn hình Chụp ảnh chi tiêu
        fabCapture.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CaptureExpenseActivity.class);
            startActivity(intent);
        });
    }

    private void selectTab(int index, Fragment fragment) {
        currentTab = index;

        // Reset all indicators & colors
        indicatorHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        indicatorStats.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        indicatorWallets.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        indicatorBudget.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        indicatorProfile.setVisibility(index == 4 ? View.VISIBLE : View.GONE);

        int colorAccent = ContextCompat.getColor(this, R.color.color_accent);
        int colorInactive = ContextCompat.getColor(this, R.color.white);

        iconHome.setColorFilter(index == 0 ? colorAccent : colorInactive);
        iconStats.setColorFilter(index == 1 ? colorAccent : colorInactive);
        iconWallets.setColorFilter(index == 2 ? colorAccent : colorInactive);
        iconBudget.setColorFilter(index == 3 ? colorAccent : colorInactive);
        iconProfile.setColorFilter(index == 4 ? colorAccent : colorInactive);

        loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
