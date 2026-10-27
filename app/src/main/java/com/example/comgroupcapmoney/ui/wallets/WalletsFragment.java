package com.example.comgroupcapmoney.ui.wallets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.List;

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

        view.findViewById(R.id.wallet_btn_add).setOnClickListener(v -> openAddWallet());
        view.findViewById(R.id.wallet_btn_transfer).setOnClickListener(v -> openTransfer());
        view.findViewById(R.id.wallet_btn_history).setOnClickListener(v ->
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
        viewModel.getUnauthorized().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getContentIfNotHandled() != null) {
                SessionManager.logout(requireActivity(), true);
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
        double total = 0;
        for (Wallet w : wallets) {
            total += w.getBalance();
            if (w.isSaving()) {
                saving.add(w);
            } else {
                payment.add(w);
            }
        }
        tvTotal.setText(CurrencyUtils.format(total));
        tvCount.setText(getString(R.string.wallet_count, wallets.size()));
        paymentAdapter.submit(payment);
        savingAdapter.submit(saving);
        tvEmptySaving.setVisibility(saving.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onWalletClick(Wallet wallet) {
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
