package com.example.comgroupcapmoney.ui.wallets;

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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WalletsFragment extends Fragment {

    private WalletsViewModel viewModel;
    private WalletAdapter paymentAdapter;
    private WalletAdapter savingAdapter;

    private View layoutContent;
    private View layoutError;
    private ProgressBar progress;
    private TextView tvTotal;
    private TextView tvCount;
    private TextView tvEmptySaving;
    private TextView tvError;
    private TextView tvCacheBanner;
    private View btnAdd;
    private View btnTransfer;
    private View btnHistory;

    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private TextView tvMonthTitle;
    private TextView btnCurrentMonth;
    private TextView tvSummaryIncome;
    private TextView tvSummaryExpense;

    // Xoay man hinh: ViewModel da co du lieu thi lan onResume dau tien khong goi lai API
    private boolean skipNextResumeLoad;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallets, container, false);
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

        layoutContent = view.findViewById(R.id.wallet_layout_content);
        layoutError = view.findViewById(R.id.wallet_layout_error);
        progress = view.findViewById(R.id.wallet_progress);
        tvTotal = view.findViewById(R.id.wallet_tv_total);
        tvCount = view.findViewById(R.id.wallet_tv_count);
        tvEmptySaving = view.findViewById(R.id.wallet_tv_empty_saving);
        tvError = view.findViewById(R.id.wallet_tv_error);

        paymentAdapter = new WalletAdapter(this::onWalletClick);
        savingAdapter = new WalletAdapter(this::onWalletClick);
        setupList(view.findViewById(R.id.wallet_rv_payment), paymentAdapter);
        setupList(view.findViewById(R.id.wallet_rv_saving), savingAdapter);

        tvCacheBanner = view.findViewById(R.id.wallet_cache_banner);
        btnAdd = view.findViewById(R.id.wallet_btn_add);
        btnTransfer = view.findViewById(R.id.wallet_btn_transfer);
        btnHistory = view.findViewById(R.id.wallet_btn_history);

        btnPrevMonth = view.findViewById(R.id.wallet_btn_prev_month);
        btnNextMonth = view.findViewById(R.id.wallet_btn_next_month);
        tvMonthTitle = view.findViewById(R.id.wallet_tv_month_title);
        btnCurrentMonth = view.findViewById(R.id.wallet_btn_current_month);
        tvSummaryIncome = view.findViewById(R.id.wallet_tv_summary_income);
        tvSummaryExpense = view.findViewById(R.id.wallet_tv_summary_expense);

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        }
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
        }
        if (btnCurrentMonth != null) {
            btnCurrentMonth.setOnClickListener(v -> viewModel.setCurrentMonth());
        }

        btnAdd.setOnClickListener(v -> openAddWallet());
        btnTransfer.setOnClickListener(v -> openTransfer());
        btnHistory.setOnClickListener(v ->
                TransferHistoryBottomSheet.newInstance().show(getChildFragmentManager(), "transfer_history"));
        view.findViewById(R.id.wallet_btn_retry).setOnClickListener(v -> viewModel.loadWallets());

        viewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);
        skipNextResumeLoad = savedInstanceState != null && viewModel.getWallets().getValue() != null;

        // Dialog them/sua vi, chuyen tien bao thanh cong thi tai lai danh sach
        getChildFragmentManager().setFragmentResultListener(WalletFormDialog.RESULT_KEY, getViewLifecycleOwner(),
                (key, bundle) -> viewModel.loadWallets());

        viewModel.getWallets().observe(getViewLifecycleOwner(), list -> render());
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> render());
        viewModel.getError().observe(getViewLifecycleOwner(), error -> render());
        viewModel.getOfflineCachedAt().observe(getViewLifecycleOwner(), at -> render());
        viewModel.getUnauthorized().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getContentIfNotHandled() != null) {
                SessionManager.logout(requireActivity(), true);
            }
        });

        viewModel.getPeriod().observe(getViewLifecycleOwner(), p -> {
            if (p == null || p.length < 2) return;
            int m = p[0];
            int y = p[1];
            if (tvMonthTitle != null) {
                tvMonthTitle.setText("tháng " + m + " " + y);
            }
            boolean isCurrent = viewModel.isCurrentMonth();
            if (btnNextMonth != null) {
                btnNextMonth.setEnabled(!isCurrent);
                btnNextMonth.setAlpha(isCurrent ? 0.3f : 1.0f);
            }
            if (btnCurrentMonth != null) {
                btnCurrentMonth.setBackgroundResource(isCurrent ? R.drawable.bg_filter_pill_active : R.drawable.bg_filter_pill_inactive);
                btnCurrentMonth.setTextColor(ContextCompat.getColor(requireContext(), isCurrent ? R.color.white : R.color.text_secondary));
                int padH = (int) (14 * getResources().getDisplayMetrics().density + 0.5f);
                btnCurrentMonth.setPadding(padH, 0, padH, 0);
            }
        });

        viewModel.getMonthSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary == null || summary.length < 3) return;
            double exp = summary[0];
            double inc = summary[1];
            double bal = summary[2];

            if (tvSummaryIncome != null) {
                tvSummaryIncome.setText(CurrencyUtils.format(inc));
            }
            if (tvSummaryExpense != null) {
                tvSummaryExpense.setText(CurrencyUtils.format(exp));
            }
            if (tvTotal != null) {
                tvTotal.setText(CurrencyUtils.format(bal));
                if (bal < 0) {
                    tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_expense));
                } else if (bal > 0) {
                    tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_income));
                } else {
                    tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tai lai moi lan quay ve tab de thay so du moi (vd sau khi ghi chi tieu)
        if (skipNextResumeLoad) {
            skipNextResumeLoad = false;
            return;
        }
        viewModel.loadWallets();
    }

    private void setupList(RecyclerView rv, WalletAdapter adapter) {
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);
    }

    private void render() {
        List<Wallet> wallets = viewModel.getWallets().getValue();
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        String error = viewModel.getError().getValue();

        if (error != null && !loading) {
            layoutError.setVisibility(View.VISIBLE);
            tvError.setText(error);
            layoutContent.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            return;
        }
        layoutError.setVisibility(View.GONE);

        if (wallets == null) {
            // Lan tai dau tien: chua co gi de hien
            layoutContent.setVisibility(View.GONE);
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            return;
        }

        // Da co du lieu cu thi van hien trong luc tai lai, khong nhay trang
        progress.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);

        List<Wallet> payment = new ArrayList<>();
        List<Wallet> saving = new ArrayList<>();
        for (Wallet w : wallets) {
            if (w.isSaving()) {
                saving.add(w);
            } else {
                payment.add(w);
            }
        }
        if (tvCount != null) {
            tvCount.setText(getString(R.string.wallet_count, wallets.size()));
        }
        paymentAdapter.submit(payment);
        savingAdapter.submit(saving);
        tvEmptySaving.setVisibility(saving.isEmpty() ? View.VISIBLE : View.GONE);

        // 4C-3: dang xem ban luu khi mat mang thi hien banner va khoa cac nut ghi du lieu
        Long cachedAt = viewModel.getOfflineCachedAt().getValue();
        boolean offline = cachedAt != null;
        if (offline) {
            String time = new SimpleDateFormat("HH:mm dd/MM", Locale.US).format(new Date(cachedAt));
            tvCacheBanner.setText(getString(R.string.wallet_cache_banner, time));
        }
        tvCacheBanner.setVisibility(offline ? View.VISIBLE : View.GONE);
        for (View b : new View[]{btnAdd, btnTransfer, btnHistory}) {
            b.setEnabled(!offline);
            b.setAlpha(offline ? 0.4f : 1f);
        }
    }

    private void onWalletClick(Wallet wallet) {
        if (viewModel.isOffline()) return;
        WalletFormDialog.newInstance(wallet).show(getChildFragmentManager(), "wallet_form");
    }

    private void openAddWallet() {
        WalletFormDialog.newInstance(null).show(getChildFragmentManager(), "wallet_form");
    }

    private void openTransfer() {
        List<Wallet> wallets = viewModel.getWallets().getValue();
        if (wallets == null || wallets.size() < 2) {
            Toast.makeText(requireContext(), R.string.transfer_need_two_wallets, Toast.LENGTH_SHORT).show();
            return;
        }
        TransferMoneyDialog.newInstance(new ArrayList<>(wallets)).show(getChildFragmentManager(), "transfer");
    }
}
