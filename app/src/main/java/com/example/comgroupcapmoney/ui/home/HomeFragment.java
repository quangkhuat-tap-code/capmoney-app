package com.example.comgroupcapmoney.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private TextView tvMonth;
    private TextView tvTotalExpense;
    private TextView tvTotalIncome;
    private MaterialButtonToggleGroup toggleViewMode;
    private MaterialButton btnModeGrid;
    private MaterialButton btnModeCalendar;
    private RecyclerView rvPhotoGrid;
    private View layoutCalendar;
    private LinearLayout layoutWeekdays;
    private RecyclerView rvCalendar;
    private View layoutEmpty;
    private View layoutError;
    private TextView tvErrorMessage;
    private MaterialButton btnRetry;
    private CircularProgressIndicator progressHome;

    private PhotoGridAdapter photoGridAdapter;
    private CalendarAdapter calendarAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // targetSdk 35+ bat buoc edge-to-edge: day thanh chon thang xuong duoi status bar
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, v.getPaddingBottom());
            return insets;
        });

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        // API bao 401 (token het han) thi ve man dang nhap
        SessionManager.observeExpired(getViewLifecycleOwner(), requireActivity());

        initViews(view);
        setupWeekdays();
        setupAdapters();
        setupListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.load();
    }

    private void initViews(View view) {
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        tvMonth = view.findViewById(R.id.tv_month);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        toggleViewMode = view.findViewById(R.id.toggle_view_mode);
        btnModeGrid = view.findViewById(R.id.btn_mode_grid);
        btnModeCalendar = view.findViewById(R.id.btn_mode_calendar);
        rvPhotoGrid = view.findViewById(R.id.rv_photo_grid);
        layoutCalendar = view.findViewById(R.id.layout_calendar);
        layoutWeekdays = view.findViewById(R.id.layout_weekdays);
        rvCalendar = view.findViewById(R.id.rv_calendar);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutError = view.findViewById(R.id.layout_error);
        tvErrorMessage = view.findViewById(R.id.tv_error_message);
        btnRetry = view.findViewById(R.id.btn_retry);
        progressHome = view.findViewById(R.id.progress_home);
    }

    private void setupWeekdays() {
        layoutWeekdays.removeAllViews();
        String[] weekdays = getResources().getStringArray(R.array.home_weekdays);
        for (String day : weekdays) {
            TextView tv = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER);
            tv.setText(day);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tv.setTextSize(12);
            layoutWeekdays.addView(tv);
        }
    }

    private void setupAdapters() {
        rvPhotoGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        photoGridAdapter = new PhotoGridAdapter(tx -> {
            TransactionDetailDialog.newInstance(tx).show(getChildFragmentManager(), "detail_dialog");
        });
        rvPhotoGrid.setAdapter(photoGridAdapter);

        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarAdapter = new CalendarAdapter(day -> {
            if (day.getItems().isEmpty()) {
                return;
            }
            if (day.getItems().size() == 1) {
                TransactionDetailDialog.newInstance(day.getItems().get(0)).show(getChildFragmentManager(), "detail_dialog");
            } else {
                DayTransactionsBottomSheet.newInstance(day.getDayKey()).show(getChildFragmentManager(), "day_sheet");
            }
        });
        rvCalendar.setAdapter(calendarAdapter);
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
        tvMonth.setOnClickListener(v -> viewModel.goToCurrentMonth());

        toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_mode_grid) {
                viewModel.setViewMode(HomeViewModel.MODE_GRID);
            } else if (checkedId == R.id.btn_mode_calendar) {
                viewModel.setViewMode(HomeViewModel.MODE_CALENDAR);
            }
        });

        btnRetry.setOnClickListener(v -> viewModel.load());
    }

    private void observeViewModel() {
        // Mat mang va dang hien du lieu da luu: hien banner kem thoi diem luu
        TextView tvOfflineBanner = requireView().findViewById(R.id.tv_offline_banner);
        viewModel.getOfflineCachedAt().observe(getViewLifecycleOwner(), cachedAt -> {
            if (cachedAt == null) {
                tvOfflineBanner.setVisibility(View.GONE);
                return;
            }
            String time = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(new Date(cachedAt));
            tvOfflineBanner.setText(getString(R.string.home_offline_banner, time));
            tvOfflineBanner.setVisibility(View.VISIBLE);
        });

        viewModel.getMonth().observe(getViewLifecycleOwner(), m -> updateMonthTitle());
        viewModel.getYear().observe(getViewLifecycleOwner(), y -> updateMonthTitle());

        viewModel.getTotals().observe(getViewLifecycleOwner(), totals -> {
            if (totals != null && totals.length == 2) {
                // Bang 0 thi khong hien dau, tranh "-0 đ" / "+0 đ"
                tvTotalExpense.setText((totals[0] > 0 ? "-" : "") + CurrencyUtils.format(totals[0]));
                tvTotalIncome.setText((totals[1] > 0 ? "+" : "") + CurrencyUtils.format(totals[1]));
            }
        });

        viewModel.getViewMode().observe(getViewLifecycleOwner(), mode -> {
            boolean isGrid = (mode == HomeViewModel.MODE_GRID);
            rvPhotoGrid.setVisibility(isGrid ? View.VISIBLE : View.GONE);
            layoutCalendar.setVisibility(isGrid ? View.GONE : View.VISIBLE);

            int colorAccent = ContextCompat.getColor(requireContext(), R.color.color_accent);
            int colorBgCard = ContextCompat.getColor(requireContext(), R.color.bg_card);
            int colorBgCardSec = ContextCompat.getColor(requireContext(), R.color.bg_card_secondary);
            int colorWhite = ContextCompat.getColor(requireContext(), R.color.white);
            int colorTextSec = ContextCompat.getColor(requireContext(), R.color.text_secondary);

            if (isGrid) {
                btnModeGrid.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
                btnModeGrid.setTextColor(colorWhite);
                btnModeGrid.setStrokeColor(ColorStateList.valueOf(colorAccent));

                btnModeCalendar.setBackgroundTintList(ColorStateList.valueOf(colorBgCard));
                btnModeCalendar.setTextColor(colorTextSec);
                btnModeCalendar.setStrokeColor(ColorStateList.valueOf(colorBgCardSec));
            } else {
                btnModeCalendar.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
                btnModeCalendar.setTextColor(colorWhite);
                btnModeCalendar.setStrokeColor(ColorStateList.valueOf(colorAccent));

                btnModeGrid.setBackgroundTintList(ColorStateList.valueOf(colorBgCard));
                btnModeGrid.setTextColor(colorTextSec);
                btnModeGrid.setStrokeColor(ColorStateList.valueOf(colorBgCardSec));
            }

            updateEmptyState();
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            photoGridAdapter.submitList(transactions);
            updateEmptyState();
        });

        viewModel.getCalendarDays().observe(getViewLifecycleOwner(), days -> {
            calendarAdapter.setItems(days);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            progressHome.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                layoutError.setVisibility(View.VISIBLE);
                tvErrorMessage.setText(error);
                layoutEmpty.setVisibility(View.GONE);
                rvPhotoGrid.setVisibility(View.GONE);
                layoutCalendar.setVisibility(View.GONE);
            } else {
                layoutError.setVisibility(View.GONE);
                boolean isGrid = (viewModel.getViewMode().getValue() == null
                        || viewModel.getViewMode().getValue() == HomeViewModel.MODE_GRID);
                rvPhotoGrid.setVisibility(isGrid ? View.VISIBLE : View.GONE);
                layoutCalendar.setVisibility(isGrid ? View.GONE : View.VISIBLE);
                updateEmptyState();
            }
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), singleEvent -> {
            if (singleEvent == null) return;
            String msg = singleEvent.getContentIfNotHandled();
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthTitle() {
        Integer m = viewModel.getMonth().getValue();
        Integer y = viewModel.getYear().getValue();
        if (m != null && y != null) {
            tvMonth.setText(DateUtils.monthTitle(m, y));
        }

        boolean isCurrent = viewModel.isCurrentMonth();
        btnNextMonth.setEnabled(!isCurrent);
        btnNextMonth.setAlpha(isCurrent ? 0.3f : 1.0f);
    }

    private void updateEmptyState() {
        if (viewModel.getError().getValue() != null) {
            layoutEmpty.setVisibility(View.GONE);
            return;
        }

        boolean isGrid = (viewModel.getViewMode().getValue() == null
                || viewModel.getViewMode().getValue() == HomeViewModel.MODE_GRID);

        boolean isEmpty = (viewModel.getTransactions().getValue() == null
                || viewModel.getTransactions().getValue().isEmpty());

        layoutEmpty.setVisibility(isGrid && isEmpty ? View.VISIBLE : View.GONE);
    }
}
