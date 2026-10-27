package com.example.comgroupcapmoney.ui.wallets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Transfer;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.List;

// Lich su chuyen tien giua cac vi theo thang (4B-1)
public class TransferHistoryBottomSheet extends BottomSheetDialogFragment {

    private TransferHistoryViewModel viewModel;
    private TransferHistoryAdapter adapter;

    private TextView tvMonth;
    private View btnNext;
    private View progress;
    private RecyclerView rvList;
    private TextView tvState;
    private View btnRetry;

    public static TransferHistoryBottomSheet newInstance() {
        return new TransferHistoryBottomSheet();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                BottomSheetBehavior.from(sheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_transfer_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMonth = view.findViewById(R.id.transfer_history_tv_month);
        btnNext = view.findViewById(R.id.transfer_history_btn_next);
        progress = view.findViewById(R.id.transfer_history_progress);
        rvList = view.findViewById(R.id.transfer_history_rv);
        tvState = view.findViewById(R.id.transfer_history_tv_state);
        btnRetry = view.findViewById(R.id.transfer_history_btn_retry);

        adapter = new TransferHistoryAdapter();
        rvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransferHistoryViewModel.class);

        view.findViewById(R.id.transfer_history_btn_prev).setOnClickListener(v -> viewModel.prevMonth());
        btnNext.setOnClickListener(v -> viewModel.nextMonth());
        btnRetry.setOnClickListener(v -> viewModel.load());

        viewModel.getMonthYear().observe(getViewLifecycleOwner(), my -> {
            tvMonth.setText(DateUtils.monthTitle(my[0], my[1]));
            boolean current = viewModel.isCurrentMonth();
            btnNext.setEnabled(!current);
            btnNext.setAlpha(current ? 0.3f : 1f);
        });
        viewModel.getTransfers().observe(getViewLifecycleOwner(), list -> render());
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> render());
        viewModel.getError().observe(getViewLifecycleOwner(), error -> render());
        viewModel.getUnauthorized().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getContentIfNotHandled() != null) {
                SessionManager.logout(requireActivity(), true);
            }
        });
    }

    private void render() {
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        String error = viewModel.getError().getValue();
        List<Transfer> list = viewModel.getTransfers().getValue();

        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRetry.setVisibility(error != null && !loading ? View.VISIBLE : View.GONE);

        if (loading) {
            rvList.setVisibility(View.GONE);
            tvState.setVisibility(View.GONE);
        } else if (error != null) {
            rvList.setVisibility(View.GONE);
            tvState.setVisibility(View.VISIBLE);
            tvState.setText(error);
        } else if (list == null || list.isEmpty()) {
            rvList.setVisibility(View.GONE);
            tvState.setVisibility(View.VISIBLE);
            tvState.setText(R.string.transfer_history_empty);
        } else {
            tvState.setVisibility(View.GONE);
            rvList.setVisibility(View.VISIBLE);
            adapter.submit(list);
        }
    }
}
