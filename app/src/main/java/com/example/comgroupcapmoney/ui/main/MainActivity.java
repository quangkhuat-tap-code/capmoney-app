package com.example.comgroupcapmoney.ui.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.budget.BudgetFragment;
import com.example.comgroupcapmoney.ui.capture.CaptureExpenseActivity;
import com.example.comgroupcapmoney.ui.home.HomeFragment;
import com.example.comgroupcapmoney.ui.profile.ProfileFragment;
import com.example.comgroupcapmoney.ui.stats.StatsFragment;
import com.example.comgroupcapmoney.ui.wallets.WalletsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton fabCapture = findViewById(R.id.fab_capture_expense);

        // Mặc định mở Tab 1 (HomeFragment) khi vừa vào app
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Chuyển Tab
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();      // Member 1
            } else if (itemId == R.id.nav_stats) {
                selectedFragment = new StatsFragment();     // Member 3
            } else if (itemId == R.id.nav_wallets) {
                selectedFragment = new WalletsFragment();   // Member 2
            } else if (itemId == R.id.nav_budget) {
                selectedFragment = new BudgetFragment();    // Member 3
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();   // Member 2
            }

            return loadFragment(selectedFragment);
        });

        // Bấm nút (+) mở màn hình Chụp ảnh chi tiêu (Member 1)
        fabCapture.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CaptureExpenseActivity.class);
            startActivity(intent);
        });
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