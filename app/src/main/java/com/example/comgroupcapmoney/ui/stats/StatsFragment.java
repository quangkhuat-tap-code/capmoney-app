package com.example.comgroupcapmoney.ui.stats;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.model.CategoryStat;
import com.example.comgroupcapmoney.model.StatsSummary;
import com.example.comgroupcapmoney.model.TrendPoint;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private StatsViewModel viewModel;

    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private TextView tvMonth;
    private MaterialButtonToggleGroup toggleType;
    private MaterialButton btnExpense;
    private MaterialButton btnIncome;
    private TextView tvSummaryLine;
    private View layoutOfflineBanner;
    private TextView tvOfflineBanner;
    private LinearProgressIndicator progress;
    private PieChart pieChart;
    private RecyclerView rvCategories;
    private View layoutEmpty;
    private TextView tvEmpty;
    private View layoutError;
    private TextView tvError;
    private MaterialButton btnRetry;

    private CategoryStatAdapter adapter;

    // Bieu do 6 thang (4C-1)
    private BarChart trendChart;
    private TextView tvTrendState;
    private TextView tvTrendSelected;
    private List<TrendPoint> boundTrend;

    /** Summary dang ve tren bieu do/danh sach, de khong ve lai (va chay lai animation) khi chi doi loading. */
    private StatsSummary boundSummary;
    /** Xoay man hinh: ViewModel da co du lieu nen bo qua lan onResume dau tien. */
    private boolean skipNextResumeLoad;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // targetSdk 35+ bat buoc edge-to-edge: day noi dung xuong duoi status bar
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, v.getPaddingBottom());
            return insets;
        });

        viewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
        // API bao 401 (token het han) thi ve man dang nhap
        SessionManager.observeExpired(getViewLifecycleOwner(), requireActivity());
        skipNextResumeLoad = savedInstanceState != null && viewModel.hasStarted();

        initViews(view);
        setupChart();
        setupListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (skipNextResumeLoad) {
            skipNextResumeLoad = false;
            return;
        }
        viewModel.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        boundSummary = null;
        boundTrend = null;
    }

    private void initViews(View view) {
        btnPrevMonth = view.findViewById(R.id.stats_btn_prev_month);
        btnNextMonth = view.findViewById(R.id.stats_btn_next_month);
        tvMonth = view.findViewById(R.id.stats_tv_month);
        toggleType = view.findViewById(R.id.stats_toggle_type);
        btnExpense = view.findViewById(R.id.stats_btn_expense);
        btnIncome = view.findViewById(R.id.stats_btn_income);
        tvSummaryLine = view.findViewById(R.id.stats_tv_summary_line);
        layoutOfflineBanner = view.findViewById(R.id.stats_layout_offline_banner);
        tvOfflineBanner = view.findViewById(R.id.stats_tv_offline_banner);
        progress = view.findViewById(R.id.stats_progress);
        pieChart = view.findViewById(R.id.stats_pie_chart);
        rvCategories = view.findViewById(R.id.stats_rv_categories);
        layoutEmpty = view.findViewById(R.id.stats_layout_empty);
        tvEmpty = view.findViewById(R.id.stats_tv_empty);
        layoutError = view.findViewById(R.id.stats_layout_error);
        tvError = view.findViewById(R.id.stats_tv_error);
        btnRetry = view.findViewById(R.id.stats_btn_retry);
        trendChart = view.findViewById(R.id.trend_bar_chart);
        tvTrendState = view.findViewById(R.id.trend_tv_state);
        tvTrendSelected = view.findViewById(R.id.trend_tv_selected);

        adapter = new CategoryStatAdapter();
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(adapter);
    }

    private void setupChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.bg_main));
        pieChart.setHoleRadius(72f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        pieChart.setCenterTextSize(16f);
        pieChart.setNoDataText("");

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e != null && e.getData() instanceof CategoryStat) {
                    CategoryStat c = (CategoryStat) e.getData();
                    String icon = c.getCategoryIcon() != null ? c.getCategoryIcon() + " " : "";
                    pieChart.setCenterText(icon + c.getCategoryName() + "\n"
                            + CurrencyUtils.format(c.getAmount()) + "\n"
                            + CategoryStatAdapter.formatPercent(c.getPercent()));
                }
            }

            @Override
            public void onNothingSelected() {
                pieChart.setCenterText(defaultCenterText(boundSummary));
            }
        });

        TrendChartHelper.setup(trendChart, requireContext());
        // Bam vao cot: hien so day du cua thang do duoi bieu do
        trendChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e != null && e.getData() instanceof TrendPoint) {
                    TrendPoint p = (TrendPoint) e.getData();
                    tvTrendSelected.setText(getString(R.string.trend_selected, p.getMonth(), p.getYear(),
                            CurrencyUtils.format(p.getTotalExpense()), CurrencyUtils.format(p.getTotalIncome())));
                    tvTrendSelected.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected() {
                tvTrendSelected.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
        btnRetry.setOnClickListener(v -> viewModel.load());

        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.stats_btn_income) {
                viewModel.setType(StatsSummary.TYPE_INCOME);
            } else if (checkedId == R.id.stats_btn_expense) {
                viewModel.setType(StatsSummary.TYPE_EXPENSE);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getPeriod().observe(getViewLifecycleOwner(), p -> {
            if (p == null || p.length < 2) return;
            tvMonth.setText(DateUtils.monthTitle(p[0], p[1]));
            boolean isCurrent = viewModel.isCurrentMonth();
            btnNextMonth.setEnabled(!isCurrent);
            btnNextMonth.setAlpha(isCurrent ? 0.3f : 1.0f);
        });

        viewModel.getType().observe(getViewLifecycleOwner(), this::updateTypeToggle);

        viewModel.getSummary().observe(getViewLifecycleOwner(), s -> render());
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> render());
        viewModel.getError().observe(getViewLifecycleOwner(), e -> render());
        viewModel.getOfflineCachedAt().observe(getViewLifecycleOwner(), at -> render());
        viewModel.getTrend().observe(getViewLifecycleOwner(), t -> renderTrend());
        viewModel.getTrendState().observe(getViewLifecycleOwner(), s -> renderTrend());
    }

    private void renderTrend() {
        Integer state = viewModel.getTrendState().getValue();
        List<TrendPoint> points = viewModel.getTrend().getValue();
        int s = state != null ? state : StatsViewModel.TREND_LOADING;

        if (s == StatsViewModel.TREND_OK && points != null) {
            tvTrendState.setVisibility(View.GONE);
            trendChart.setVisibility(View.VISIBLE);
            // Xoay man hinh / LiveData phat lai: khong ve lai, khong chay lai animation
            if (points != boundTrend) {
                boundTrend = points;
                tvTrendSelected.setVisibility(View.GONE);
                TrendChartHelper.bind(trendChart, points, requireContext());
            }
            return;
        }

        if (s == StatsViewModel.TREND_LOADING) {
            // Dang tai lai: giu bieu do cu neu co de khong nhay
            return;
        }

        boundTrend = null;
        trendChart.setVisibility(View.GONE);
        tvTrendSelected.setVisibility(View.GONE);
        tvTrendState.setVisibility(View.VISIBLE);
        if (s == StatsViewModel.TREND_EMPTY) {
            tvTrendState.setText(R.string.trend_empty);
        } else if (s == StatsViewModel.TREND_OFFLINE) {
            tvTrendState.setText(R.string.trend_offline);
        } else {
            tvTrendState.setText(R.string.trend_error);
        }
    }

    private void updateTypeToggle(String type) {
        boolean isIncome = StatsSummary.TYPE_INCOME.equals(type);
        int checkedId = isIncome ? R.id.stats_btn_income : R.id.stats_btn_expense;
        if (toggleType.getCheckedButtonId() != checkedId) {
            toggleType.check(checkedId);
        }

        int colorExpense = ContextCompat.getColor(requireContext(), R.color.color_expense);
        int colorIncome = ContextCompat.getColor(requireContext(), R.color.color_income);
        int colorBgCard = ContextCompat.getColor(requireContext(), R.color.bg_card);
        int colorBgCardSec = ContextCompat.getColor(requireContext(), R.color.bg_card_secondary);
        int colorWhite = ContextCompat.getColor(requireContext(), R.color.white);
        int colorTextSec = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        styleToggleButton(btnExpense, !isIncome, colorExpense, colorBgCard, colorBgCardSec, colorWhite, colorTextSec);
        styleToggleButton(btnIncome, isIncome, colorIncome, colorBgCard, colorBgCardSec, colorWhite, colorTextSec);
    }

    private void styleToggleButton(MaterialButton button, boolean selected, int selectedColor,
                                   int bgCard, int bgCardSec, int white, int textSec) {
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? selectedColor : bgCard));
        button.setStrokeColor(ColorStateList.valueOf(selected ? selectedColor : bgCardSec));
        button.setTextColor(selected ? white : textSec);
    }

    /** Ve lai man hinh tu trang thai hien tai cua ViewModel. */
    private void render() {
        StatsSummary s = viewModel.getSummary().getValue();
        String error = viewModel.getError().getValue();
        boolean isLoading = Boolean.TRUE.equals(viewModel.getLoading().getValue());

        progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (error != null) {
            layoutError.setVisibility(View.VISIBLE);
            tvError.setText(error);
            hideContent();
            return;
        }
        layoutError.setVisibility(View.GONE);

        if (s == null) {
            hideContent();
            return;
        }

        tvSummaryLine.setVisibility(View.VISIBLE);
        tvSummaryLine.setText(getString(R.string.stats_summary_line,
                CurrencyUtils.format(s.getTotalIncome()), CurrencyUtils.format(s.getTotalExpense())));

        Long cachedAt = viewModel.getOfflineCachedAt().getValue();
        if (cachedAt != null) {
            String time = new SimpleDateFormat("HH:mm dd/MM", Locale.US).format(new Date(cachedAt));
            tvOfflineBanner.setText(getString(R.string.stats_offline_banner, time));
            layoutOfflineBanner.setVisibility(View.VISIBLE);
        } else {
            layoutOfflineBanner.setVisibility(View.GONE);
        }

        if (s != boundSummary) {
            bindSummary(s);
        }

        boolean hasData = !s.getCategories().isEmpty();
        pieChart.setVisibility(hasData ? View.VISIBLE : View.GONE);
        rvCategories.setVisibility(hasData ? View.VISIBLE : View.GONE);
        layoutEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        tvEmpty.setText(s.isIncome() ? R.string.stats_empty_income : R.string.stats_empty_expense);
    }

    private void hideContent() {
        tvSummaryLine.setVisibility(View.GONE);
        layoutOfflineBanner.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        rvCategories.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void bindSummary(StatsSummary s) {
        boundSummary = s;
        List<CategoryStat> categories = s.getCategories();
        adapter.setItems(categories);

        pieChart.highlightValues(null);
        if (categories.isEmpty()) {
            pieChart.clear();
            return;
        }

        int fallback = ContextCompat.getColor(requireContext(), R.color.color_accent);
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (CategoryStat c : categories) {
            entries.add(new PieEntry((float) c.getAmount(), c));
            colors.add(CategoryStatAdapter.parseColor(c.getCategoryColor(), fallback));
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setSliceSpace(2f);
        ds.setDrawValues(false);
        pieChart.setData(new PieData(ds));
        pieChart.setCenterText(defaultCenterText(s));
        pieChart.animateY(600);
        pieChart.invalidate();
    }

    private String defaultCenterText(StatsSummary s) {
        if (s == null) return "";
        int label = s.isIncome() ? R.string.stats_total_income : R.string.stats_total_expense;
        return getString(label) + "\n" + CurrencyUtils.format(s.getTotal());
    }
}
