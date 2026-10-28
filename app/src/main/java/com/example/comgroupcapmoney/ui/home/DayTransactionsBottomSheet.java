package com.example.comgroupcapmoney.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class DayTransactionsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_DAY_KEY = "day_key";

    public static DayTransactionsBottomSheet newInstance(String dayKey) {
        DayTransactionsBottomSheet sheet = new DayTransactionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_DAY_KEY, dayKey);
        sheet.setArguments(args);
        return sheet;
    }

    private String dayKey;
    private HomeViewModel viewModel;
    private TextView tvDaySheetTitle;
    private TextView tvDaySheetTotal;
    private RecyclerView rvDayTransactions;
    private PhotoGridAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayKey = getArguments().getString(ARG_DAY_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_day_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (dayKey == null) {
            dismiss();
            return;
        }

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        tvDaySheetTitle = view.findViewById(R.id.tv_day_sheet_title);
        tvDaySheetTotal = view.findViewById(R.id.tv_day_sheet_total);
        rvDayTransactions = view.findViewById(R.id.rv_day_transactions);

        tvDaySheetTitle.setText(getString(R.string.home_day_title, DateUtils.toDisplayDay(dayKey + " 00:00:00")));

        rvDayTransactions.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new PhotoGridAdapter(tx -> {
            TransactionDetailDialog.newInstance(tx).show(getParentFragmentManager(), "detail_dialog");
        });
        rvDayTransactions.setAdapter(adapter);

        viewModel.getTransactions().observe(getViewLifecycleOwner(), allList -> {
            List<Transaction> dayList = new ArrayList<>();
            double expense = 0;
            double income = 0;

            if (allList != null) {
                for (Transaction tx : allList) {
                    if (dayKey.equals(tx.getDayKey())) {
                        dayList.add(tx);
                        if (tx.isExpense()) {
                            expense += tx.getAmount();
                        } else {
                            income += tx.getAmount();
                        }
                    }
                }
            }

            if (dayList.isEmpty()) {
                dismiss();
                return;
            }

            adapter.submitList(dayList);

            String summary = "";
            if (expense > 0 && income > 0) {
                summary = "Chi: -" + CurrencyUtils.format(expense) + " · Thu: +" + CurrencyUtils.format(income);
            } else if (expense > 0) {
                summary = "Tổng chi: -" + CurrencyUtils.format(expense);
            } else if (income > 0) {
                summary = "Tổng thu: +" + CurrencyUtils.format(income);
            }
            tvDaySheetTotal.setText(summary);
        });
    }
}
