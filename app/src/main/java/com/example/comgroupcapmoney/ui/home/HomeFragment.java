package com.example.comgroupcapmoney.ui.home;

import android.content.Intent;
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
import com.example.comgroupcapmoney.ui.search.SearchActivity;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private TextView tvGreeting;
    private TextView tvUserName;
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
    private boolean isAmountsHidden = false;

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

        // Edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, v.getPaddingBottom());
            return insets;
        });

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        SessionManager.observeExpired(getViewLifecycleOwner(), requireActivity());

        initViews(view);
        setupGreeting();
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
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUserName = view.findViewById(R.id.tv_user_name);
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

    private void setupGreeting() {
        if (getContext() != null) {
            String name = SharedPrefManager.getInstance(requireContext()).getUserName();
            if (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("User")) {
                tvUserName.setText(name);
            } else {
                tvUserName.setText("Quang");
            }

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour >= 5 && hour < 12) {
                tvGreeting.setText("Chào buổi sáng 🌅");
            } else if (hour >= 12 && hour < 18) {
                tvGreeting.setText("Chào buổi chiều 🌤️");
            } else {
                tvGreeting.setText("Chào buổi tối 🌙");
            }
        }
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
        View btnSearch = requireView().findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SearchActivity.class)));
        }

        View btnEye = requireView().findViewById(R.id.btn_toggle_eye);
        if (btnEye != null) {
            btnEye.setOnClickListener(v -> {
                isAmountsHidden = !isAmountsHidden;
                updateTotalsDisplay(viewModel.getTotals().getValue());
            });
        }
    }

    private void observeViewModel() {
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

        viewModel.getTotals().observe(getViewLifecycleOwner(), this::updateTotalsDisplay);

        viewModel.getViewMode().observe(getViewLifecycleOwner(), mode -> {
            boolean isGrid = (mode == HomeViewModel.MODE_GRID);
            rvPhotoGrid.setVisibility(isGrid ? View.VISIBLE : View.GONE);
            layoutCalendar.setVisibility(isGrid ? View.GONE : View.VISIBLE);

            if (isGrid) {
                btnModeGrid.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                btnModeGrid.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_card_elevated));
                btnModeCalendar.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                btnModeCalendar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent));
            } else {
                btnModeCalendar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                btnModeCalendar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_card_elevated));
                btnModeGrid.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                btnModeGrid.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent));
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
                boolean isGrid = (viewModel.getViewMode().getValue() != null
                        && viewModel.getViewMode().getValue() == HomeViewModel.MODE_GRID);
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

    private void updateTotalsDisplay(double[] totals) {
        if (totals != null && totals.length == 2) {
            if (isAmountsHidden) {
                tvTotalExpense.setText("******");
                tvTotalIncome.setText("******");
            } else {
                tvTotalExpense.setText(CurrencyUtils.format(totals[0]));
                tvTotalIncome.setText(CurrencyUtils.format(totals[1]));
            }
        }
    }

    private void updateMonthTitle() {
        Integer m = viewModel.getMonth().getValue();
        Integer y = viewModel.getYear().getValue();
        if (m != null && y != null) {
            tvMonth.setText(DateUtils.monthTitle(m, y) + " ⌄");
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

        boolean isGrid = (viewModel.getViewMode().getValue() != null
                && viewModel.getViewMode().getValue() == HomeViewModel.MODE_GRID);

        boolean isEmpty = (viewModel.getTransactions().getValue() == null
                || viewModel.getTransactions().getValue().isEmpty());

        layoutEmpty.setVisibility(isGrid && isEmpty ? View.VISIBLE : View.GONE);
    }
}
