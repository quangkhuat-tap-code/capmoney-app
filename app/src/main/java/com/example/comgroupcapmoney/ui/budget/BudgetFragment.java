package com.example.comgroupcapmoney.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.ApiService;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Category;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tab 4 — Quản lý Ngân sách
 *
 * Chức năng:
 *   - Chọn tháng / năm qua nút < và >
 *   - Hiển thị thẻ tóm tắt Tổng hạn mức / Đã tiêu
 *   - Hiển thị RecyclerView danh sách ngân sách + ProgressBar 3 màu
 *   - Nút "+ Thêm" mở AddBudgetDialog (Spinner danh mục + nhập hạn mức)
 *   - Xóa ngân sách bằng nút xóa trên từng item
 *
 * Phụ trách: Thành viên 3
 */
public class BudgetFragment extends Fragment {

    // ─── Views ─────────────────────────────────────────────────────────────
    private TextView tvMonthYear;
    private TextView tvTotalBudget;
    private TextView tvTotalSpent;
    private RecyclerView rvBudgets;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private MaterialButton btnAddBudget;

    // ─── Data ──────────────────────────────────────────────────────────────
    private BudgetAdapter adapter;
    private ApiService apiService;
    private List<Category> expenseCategories = new ArrayList<>();

    private int currentMonth;
    private int currentYear;

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
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý insets status bar (Android 15+ edge-to-edge)
        View root = view.findViewById(R.id.fragment_budget_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, top, 0, 0);
            return insets;
        });

        bindViews(view);
        setupDefaultDate();
        setupRecyclerView();
        setupListeners();

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        // Tải danh mục và ngân sách song song
        loadCategories();
        loadBudgets();
    }

    // ─── Setup ─────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvMonthYear   = view.findViewById(R.id.tv_month_year);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvTotalSpent  = view.findViewById(R.id.tv_total_spent);
        rvBudgets     = view.findViewById(R.id.rv_budgets);
        layoutEmpty   = view.findViewById(R.id.layout_empty);
        progressBar   = view.findViewById(R.id.progress_bar);
        btnPrevMonth  = view.findViewById(R.id.btn_prev_month);
        btnNextMonth  = view.findViewById(R.id.btn_next_month);
        btnAddBudget  = view.findViewById(R.id.btn_add_budget);
    }

    private void setupDefaultDate() {
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentYear  = cal.get(Calendar.YEAR);
        updateMonthYearLabel();
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter(requireContext());
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setAdapter(adapter);

        // Xóa ngân sách khi bấm nút xóa
        adapter.setOnDeleteClickListener((budget, position) ->
                showDeleteConfirmDialog(budget, position));
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 1) { currentMonth = 12; currentYear--; }
            updateMonthYearLabel();
            loadBudgets();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 12) { currentMonth = 1; currentYear++; }
            updateMonthYearLabel();
            loadBudgets();
        });

        btnAddBudget.setOnClickListener(v -> {
            if (expenseCategories.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Đang tải danh mục, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                loadCategories();
                return;
            }
            openAddBudgetDialog();
        });
    }

    // ─── API Calls ─────────────────────────────────────────────────────────

    /** Tải danh mục chi tiêu để điền vào Spinner trong AddBudgetDialog */
    private void loadCategories() {
        apiService.getCategories("expense")
                .enqueue(new Callback<ApiResponse<List<Category>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call,
                                           @NonNull Response<ApiResponse<List<Category>>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            List<Category> data = response.body().getData();
                            if (data != null) {
                                expenseCategories = data;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call,
                                          @NonNull Throwable t) {
                        // Không báo lỗi — danh mục có thể đã có từ lần load trước
                    }
                });
    }

    /** Tải danh sách ngân sách tháng hiện tại */
    private void loadBudgets() {
        showLoading(true);

        apiService.getBudgets(currentMonth, currentYear)
                .enqueue(new Callback<ApiResponse<List<Budget>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Budget>>> call,
                                           @NonNull Response<ApiResponse<List<Budget>>> response) {
                        if (!isAdded()) return;
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            List<Budget> budgets = response.body().getData();
                            updateUI(budgets);
                        } else {
                            showEmptyState(true);
                            Toast.makeText(requireContext(),
                                    "Không tải được dữ liệu ngân sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Budget>>> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showLoading(false);
                        showEmptyState(true);
                        Toast.makeText(requireContext(),
                                "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteBudget(int budgetId, int position) {
        apiService.deleteBudget(budgetId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    adapter.removeAt(position);
                    Toast.makeText(requireContext(), "Đã xóa ngân sách", Toast.LENGTH_SHORT).show();
                    // Reload để cập nhật thẻ tóm tắt
                    loadBudgets();
                } else {
                    Toast.makeText(requireContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── UI Update ─────────────────────────────────────────────────────────

    private void updateUI(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            showEmptyState(true);
            adapter.setItems(new ArrayList<>());
            tvTotalBudget.setText("0 đ");
            tvTotalSpent.setText("0 đ");
            return;
        }

        showEmptyState(false);
        adapter.setItems(budgets);

        // Tính tổng hạn mức và tổng đã tiêu
        double totalLimit = 0;
        double totalSpent = 0;
        for (Budget b : budgets) {
            totalLimit += b.getLimitAmount();
            totalSpent += b.getSpentAmount();
        }
        tvTotalBudget.setText(VND_FORMAT.format(totalLimit) + " đ");
        tvTotalSpent.setText(VND_FORMAT.format(totalSpent) + " đ");
    }

    private void updateMonthYearLabel() {
        tvMonthYear.setText(
                String.format(Locale.getDefault(), "Tháng %d / %d", currentMonth, currentYear)
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvBudgets.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ─── Dialogs ───────────────────────────────────────────────────────────

    private void openAddBudgetDialog() {
        AddBudgetDialog dialog = new AddBudgetDialog(
                requireContext(),
                expenseCategories,
                currentMonth,
                currentYear,
                savedBudget -> {
                    // Reload sau khi lưu thành công
                    loadBudgets();
                    Toast.makeText(requireContext(), "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }

    private void showDeleteConfirmDialog(Budget budget, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa ngân sách")
                .setMessage("Bạn có chắc muốn xóa ngân sách \"" + budget.getCategoryName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBudget(budget.getId(), position))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
