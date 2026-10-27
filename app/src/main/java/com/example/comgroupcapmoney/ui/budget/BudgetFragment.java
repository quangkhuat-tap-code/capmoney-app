package com.example.comgroupcapmoney.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetViewModel viewModel;

    private MaterialButton btnAdd;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private TextView tvMonth;
    private LinearProgressIndicator progress;
    private View cardSummary;
    private TextView tvTotalLimit;
    private TextView tvTotalSpent;
    private TextView tvTotalRemaining;
    private LinearProgressIndicator progressTotal;
    private TextView tvTotalPercent;
    private RecyclerView rvList;
    private View layoutEmpty;
    private MaterialButton btnEmptyAction;
    private View layoutError;
    private TextView tvError;
    private MaterialButton btnRetry;

    private BudgetAdapter adapter;

    /** Xoay man hinh: ViewModel da co du lieu nen bo qua lan onResume dau tien. */
    private boolean skipNextResumeLoad;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
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

        viewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        // API bao 401 (token het han) thi ve man dang nhap
        SessionManager.observeExpired(getViewLifecycleOwner(), requireActivity());
        skipNextResumeLoad = savedInstanceState != null && viewModel.hasStarted();

        initViews(view);
        setupListeners();
        observeViewModel();

        // Form luu thanh cong -> tai lai danh sach
        getChildFragmentManager().setFragmentResultListener(BudgetFormDialog.RESULT_BUDGET_CHANGED,
                getViewLifecycleOwner(), (key, bundle) -> viewModel.load());
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

    private void initViews(View view) {
        btnAdd = view.findViewById(R.id.budget_btn_add);
        btnPrevMonth = view.findViewById(R.id.budget_btn_prev_month);
        btnNextMonth = view.findViewById(R.id.budget_btn_next_month);
        tvMonth = view.findViewById(R.id.budget_tv_month);
        progress = view.findViewById(R.id.budget_progress);
        cardSummary = view.findViewById(R.id.budget_card_summary);
        tvTotalLimit = view.findViewById(R.id.budget_tv_total_limit);
        tvTotalSpent = view.findViewById(R.id.budget_tv_total_spent);
        tvTotalRemaining = view.findViewById(R.id.budget_tv_total_remaining);
        progressTotal = view.findViewById(R.id.budget_progress_total);
        tvTotalPercent = view.findViewById(R.id.budget_tv_total_percent);
        rvList = view.findViewById(R.id.budget_rv_list);
        layoutEmpty = view.findViewById(R.id.budget_layout_empty);
        btnEmptyAction = view.findViewById(R.id.budget_btn_empty_action);
        layoutError = view.findViewById(R.id.budget_layout_error);
        tvError = view.findViewById(R.id.budget_tv_error);
        btnRetry = view.findViewById(R.id.budget_btn_retry);

        btnAdd.setText("+ " + getString(R.string.budget_add));

        adapter = new BudgetAdapter();
        adapter.setListener(new BudgetAdapter.Listener() {
            @Override
            public void onBudgetClick(Budget budget) {
                openForm(budget);
            }

            @Override
            public void onBudgetLongClick(Budget budget) {
                confirmDelete(budget);
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvList.setAdapter(adapter);
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
        btnRetry.setOnClickListener(v -> viewModel.load());
        btnAdd.setOnClickListener(v -> openForm(null));
        btnEmptyAction.setOnClickListener(v -> openForm(null));
    }

    /** editing = null: che do Them; khac null: che do Sua. */
    private void openForm(@Nullable Budget editing) {
        if (getChildFragmentManager().findFragmentByTag(BudgetFormDialog.TAG) != null) return;
        BudgetFormDialog.newInstance(viewModel.getMonthValue(), viewModel.getYearValue(),
                viewModel.getBudgetedCategoryIds(), editing)
                .show(getChildFragmentManager(), BudgetFormDialog.TAG);
    }

    private void confirmDelete(Budget budget) {
        String message = getString(R.string.budget_delete_message,
                CurrencyUtils.format(budget.getLimitAmount()), budget.getCategoryName());
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.budget_delete_title)
                .setMessage(message)
                .setNegativeButton(R.string.budget_cancel, null)
                .setPositiveButton(R.string.budget_delete, (dialog, which) -> viewModel.deleteBudget(budget.getId()))
                .show();
    }

    private void observeViewModel() {
        viewModel.getPeriod().observe(getViewLifecycleOwner(), p -> {
            if (p == null || p.length < 2) return;
            tvMonth.setText(DateUtils.monthTitle(p[0], p[1]));
            boolean isCurrent = viewModel.isCurrentMonth();
            btnNextMonth.setEnabled(!isCurrent);
            btnNextMonth.setAlpha(isCurrent ? 0.3f : 1.0f);
        });

        viewModel.getBudgets().observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
            render();
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), l -> render());
        viewModel.getError().observe(getViewLifecycleOwner(), e -> render());

        viewModel.getEvent().observe(getViewLifecycleOwner(), singleEvent -> {
            if (singleEvent == null) return;
            String msg = singleEvent.getContentIfNotHandled();
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Ve lai man hinh tu trang thai hien tai cua ViewModel. */
    private void render() {
        List<Budget> list = viewModel.getBudgets().getValue();
        String error = viewModel.getError().getValue();
        boolean isLoading = Boolean.TRUE.equals(viewModel.getLoading().getValue());

        progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (error != null) {
            layoutError.setVisibility(View.VISIBLE);
            tvError.setText(error);
            cardSummary.setVisibility(View.GONE);
            rvList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
            return;
        }
        layoutError.setVisibility(View.GONE);

        if (list == null) {
            cardSummary.setVisibility(View.GONE);
            rvList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
            return;
        }

        if (list.isEmpty()) {
            cardSummary.setVisibility(View.GONE);
            rvList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        rvList.setVisibility(View.VISIBLE);
        cardSummary.setVisibility(View.VISIBLE);
        bindTotals(list);
    }

    /** The tong: cong han muc va da chi cua moi dong, % tinh tai may, cung luat mau. */
    private void bindTotals(List<Budget> list) {
        double totalLimit = 0;
        double totalSpent = 0;
        for (Budget b : list) {
            totalLimit += b.getLimitAmount();
            totalSpent += b.getSpentAmount();
        }
        double remaining = totalLimit - totalSpent;
        int percent = totalLimit > 0 ? (int) Math.round(totalSpent / totalLimit * 100) : 0;
        int color = ContextCompat.getColor(requireContext(),
                BudgetAdapter.statusColorRes(BudgetAdapter.resolveStatus(null, percent)));

        tvTotalLimit.setText(CurrencyUtils.format(totalLimit));
        tvTotalSpent.setText(CurrencyUtils.format(totalSpent));
        tvTotalRemaining.setText(CurrencyUtils.format(remaining));
        tvTotalRemaining.setTextColor(ContextCompat.getColor(requireContext(),
                remaining < 0 ? R.color.color_expense : R.color.text_primary));

        progressTotal.setIndicatorColor(color);
        progressTotal.setProgressCompat(Math.max(0, Math.min(percent, 100)), false);
        tvTotalPercent.setText(percent + "%");
        tvTotalPercent.setTextColor(color);
    }
}
