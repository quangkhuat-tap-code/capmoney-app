package com.example.comgroupcapmoney.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.ApiService;
import com.example.comgroupcapmoney.model.AnalyticsSummary;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.CategoryStat;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tab 2 — Thống kê Donut Chart
 *
 * Chức năng:
 *   - Chọn tháng / năm qua nút < và >
 *   - Toggle Chi tiêu / Thu nhập
 *   - Vẽ PieChart rỗng ruột (Donut) bằng MPAndroidChart
 *   - Hiển thị RecyclerView danh sách % từng danh mục phía dưới
 *
 * Phụ trách: Thành viên 3
 */
public class StatsFragment extends Fragment {

    // ─── Views ─────────────────────────────────────────────────────────────
    private PieChart pieChart;
    private TextView tvMonthYear;
    private TextView tvTotalAmount;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private MaterialButtonToggleGroup toggleType;
    private RecyclerView rvCategories;

    // ─── Data ──────────────────────────────────────────────────────────────
    private CategoryStatAdapter adapter;
    private ApiService apiService;

    private int currentMonth;
    private int currentYear;
    private String currentType = "expense";   // "expense" | "income"

    private static final NumberFormat VND_FORMAT;
    static {
        VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        VND_FORMAT.setMaximumFractionDigits(0);
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý insets status bar (Android 15+ edge-to-edge)
        View root = view.findViewById(R.id.fragment_stats_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        bindViews(view);
        setupDefaultDate();
        setupChart();
        setupRecyclerView();
        setupListeners();

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        loadStats();
    }

    // ─── Setup ─────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        pieChart       = view.findViewById(R.id.pie_chart);
        tvMonthYear    = view.findViewById(R.id.tv_month_year);
        tvTotalAmount  = view.findViewById(R.id.tv_total_amount);
        tvEmpty        = view.findViewById(R.id.tv_empty);
        progressBar    = view.findViewById(R.id.progress_bar);
        btnPrevMonth   = view.findViewById(R.id.btn_prev_month);
        btnNextMonth   = view.findViewById(R.id.btn_next_month);
        toggleType     = view.findViewById(R.id.toggle_type);
        rvCategories   = view.findViewById(R.id.rv_categories);
    }

    private void setupDefaultDate() {
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;  // Calendar.MONTH là 0-indexed
        currentYear  = cal.get(Calendar.YEAR);
        updateMonthYearLabel();
    }

    private void setupChart() {
        // Cấu hình PieChart dạng Donut rỗng ruột
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(63f);
        pieChart.setHoleColor(Color.parseColor("#0E0F12"));    // khớp @color/bg_main
        pieChart.setTransparentCircleColor(Color.parseColor("#0E0F12"));

        // Tắt mô tả, legend mặc định
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        // Tắt nhãn % trong lát bánh
        pieChart.setDrawEntryLabels(false);

        // Animation khi load data
        pieChart.animateY(600);

        // Không cho xoay biểu đồ khi chạm
        pieChart.setRotationEnabled(false);
    }

    private void setupRecyclerView() {
        adapter = new CategoryStatAdapter(requireContext());
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(adapter);
        rvCategories.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        // Nút tháng trước
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }
            updateMonthYearLabel();
            loadStats();
        });

        // Nút tháng sau
        btnNextMonth.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
            updateMonthYearLabel();
            loadStats();
        });

        // Toggle Chi / Thu
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_expense) {
                currentType = "expense";
            } else if (checkedId == R.id.btn_income) {
                currentType = "income";
            }
            loadStats();
        });

        // Chọn "Chi" mặc định
        toggleType.check(R.id.btn_expense);
    }

    // ─── API Call ──────────────────────────────────────────────────────────

    private void loadStats() {
        showLoading(true);

        apiService.getAnalyticsSummary(currentMonth, currentYear, currentType)
                .enqueue(new Callback<ApiResponse<AnalyticsSummary>>() {

                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AnalyticsSummary>> call,
                                           @NonNull Response<ApiResponse<AnalyticsSummary>> response) {
                        if (!isAdded()) return;
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            AnalyticsSummary summary = response.body().getData();
                            updateUI(summary);
                        } else {
                            showEmptyState(true);
                            Toast.makeText(requireContext(),
                                    "Không tải được dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AnalyticsSummary>> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showLoading(false);
                        showEmptyState(true);
                        Toast.makeText(requireContext(),
                                "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── UI Update ─────────────────────────────────────────────────────────

    private void updateUI(AnalyticsSummary summary) {
        List<CategoryStat> categories = summary.getCategories();

        if (categories == null || categories.isEmpty()) {
            showEmptyState(true);
            updateDonutChart(new ArrayList<>());
            tvTotalAmount.setText("0 đ");
            adapter.setItems(new ArrayList<>());
            return;
        }

        showEmptyState(false);

        // Tổng tiền hiển thị
        double grand = "income".equals(currentType)
                ? summary.getTotalIncome()
                : summary.getTotalExpense();
        tvTotalAmount.setText(VND_FORMAT.format(grand) + " đ");

        // Màu tổng tiền theo loại
        int colorRes = "income".equals(currentType)
                ? R.color.color_income
                : R.color.color_expense;
        tvTotalAmount.setTextColor(requireContext().getColor(colorRes));

        // Cập nhật biểu đồ và danh sách
        updateDonutChart(categories);
        adapter.setItems(categories);
    }

    private void updateDonutChart(List<CategoryStat> categories) {
        List<PieEntry> entries    = new ArrayList<>();
        List<Integer>  colors     = new ArrayList<>();

        for (CategoryStat stat : categories) {
            if (stat.getTotal() > 0) {
                entries.add(new PieEntry((float) stat.getTotal(), ""));
                try {
                    colors.add(Color.parseColor(stat.getColorHex()));
                } catch (IllegalArgumentException e) {
                    colors.add(Color.GRAY);
                }
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);           // khoảng cách giữa các lát
        dataSet.setSelectionShift(5f);       // lát nổi lên khi chọn
        dataSet.setDrawValues(false);        // tắt nhãn giá trị

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.animateY(500);
        pieChart.invalidate();
    }

    private void updateMonthYearLabel() {
        tvMonthYear.setText(
                String.format(Locale.getDefault(), "Tháng %d / %d", currentMonth, currentYear)
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        pieChart.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        pieChart.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
