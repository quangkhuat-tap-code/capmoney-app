package com.example.comgroupcapmoney.ui.capture;

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
import com.example.comgroupcapmoney.model.Category;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class SelectCategoryBottomSheet extends BottomSheetDialogFragment {

    public static SelectCategoryBottomSheet newInstance() {
        return new SelectCategoryBottomSheet();
    }

    private CaptureViewModel viewModel;
    private ChipGroup chipGroupWallets;
    private RecyclerView rvCategories;
    private TextView tvEmptyCategories;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_select_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CaptureViewModel.class);

        chipGroupWallets = view.findViewById(R.id.chip_group_wallets);
        rvCategories = view.findViewById(R.id.rv_categories);
        tvEmptyCategories = view.findViewById(R.id.tv_empty_categories);

        rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        categoryAdapter = new CategoryAdapter(category -> {
            viewModel.selectCategory(category);
            dismiss();
        });
        rvCategories.setAdapter(categoryAdapter);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> {
            updateWalletChips(wallets, viewModel.getSelectedWallet().getValue());
        });

        viewModel.getSelectedWallet().observe(getViewLifecycleOwner(), selected -> {
            updateWalletChips(viewModel.getWallets().getValue(), selected);
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            updateCategoryList(categories, viewModel.getType().getValue(), viewModel.getSelectedCategory().getValue());
        });

        viewModel.getType().observe(getViewLifecycleOwner(), type -> {
            updateCategoryList(viewModel.getCategories().getValue(), type, viewModel.getSelectedCategory().getValue());
        });

        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), selected -> {
            categoryAdapter.setSelectedCategory(selected);
        });
    }

    private void updateWalletChips(List<WalletOption> wallets, WalletOption selected) {
        chipGroupWallets.removeAllViews();
        if (wallets == null || wallets.isEmpty()) return;

        for (WalletOption w : wallets) {
            Chip chip = new Chip(requireContext());
            String icon = getWalletIcon(w.getType());
            String label = icon + " " + w.getName() + " · " + CurrencyUtils.format(w.getBalance());
            chip.setText(label);
            chip.setCheckable(true);
            chip.setClickable(true);

            boolean isChecked = selected != null && selected.getId() == w.getId();
            chip.setChecked(isChecked);

            chip.setOnClickListener(v -> viewModel.selectWallet(w));
            chipGroupWallets.addView(chip);
        }
    }

    private void updateCategoryList(List<Category> allCategories, String type, Category selected) {
        if (allCategories == null || allCategories.isEmpty()) {
            tvEmptyCategories.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
            return;
        }

        List<Category> filtered = new ArrayList<>();
        for (Category c : allCategories) {
            if (type == null || type.equalsIgnoreCase(c.getType())) {
                filtered.add(c);
            }
        }

        if (filtered.isEmpty()) {
            tvEmptyCategories.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            tvEmptyCategories.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
            categoryAdapter.setItems(filtered, selected);
        }
    }

    private String getWalletIcon(String type) {
        if (type == null) return "👛";
        switch (type.toLowerCase()) {
            case "cash": return "💵";
            case "bank": return "🏦";
            case "saving": return "🐷";
            default: return "👛";
        }
    }
}
