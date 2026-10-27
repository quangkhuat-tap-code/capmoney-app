package com.example.comgroupcapmoney.ui.budget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.BudgetRepository;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Category;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Form dat ngan sach moi (editing = null) hoac sua han muc (editing != null). */
public class BudgetFormDialog extends BottomSheetDialogFragment {

    public static final String TAG = "budget_form";
    /** Fragment cha nghe key nay de tai lai danh sach. */
    public static final String RESULT_BUDGET_CHANGED = "budget_changed";

    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private static final String ARG_BUDGETED_IDS = "budgeted_category_ids";
    private static final String ARG_EDITING = "editing";
    private static final String STATE_SELECTED_CATEGORY = "selected_category_id";

    private static final int NO_CATEGORY = -1;

    public static BudgetFormDialog newInstance(int month, int year, ArrayList<Integer> budgetedCategoryIds, Budget editingOrNull) {
        BudgetFormDialog dialog = new BudgetFormDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_YEAR, year);
        args.putIntegerArrayList(ARG_BUDGETED_IDS, budgetedCategoryIds != null ? budgetedCategoryIds : new ArrayList<>());
        if (editingOrNull != null) {
            args.putSerializable(ARG_EDITING, editingOrNull);
        }
        dialog.setArguments(args);
        return dialog;
    }

    private int month;
    private int year;
    private final Set<Integer> budgetedIds = new HashSet<>();
    private Budget editing;
    private int selectedCategoryId = NO_CATEGORY;
    private boolean saving = false;

    private BudgetRepository repository;

    private ChipGroup chipGroup;
    private View progressCategories;
    private View layoutCategoryError;
    private TextView tvCategoryError;
    private TextView tvExistingHint;
    private EditText etLimit;
    private TextView tvError;
    private MaterialButton btnSave;

    private TextWatcher limitWatcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            month = args.getInt(ARG_MONTH);
            year = args.getInt(ARG_YEAR);
            ArrayList<Integer> ids = args.getIntegerArrayList(ARG_BUDGETED_IDS);
            if (ids != null) {
                budgetedIds.addAll(ids);
            }
            editing = (Budget) args.getSerializable(ARG_EDITING);
        }

        if (savedInstanceState != null) {
            selectedCategoryId = savedInstanceState.getInt(STATE_SELECTED_CATEGORY, NO_CATEGORY);
        } else if (editing != null) {
            selectedCategoryId = editing.getCategoryId();
        }

        repository = new BudgetRepository(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.setOnShowListener(d -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                // Nen bo goc nam trong layout (bg_budget_sheet)
                sheet.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_budget_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.budget_form_tv_title);
        TextView tvMonth = view.findViewById(R.id.budget_form_tv_month);
        chipGroup = view.findViewById(R.id.budget_form_chip_group);
        progressCategories = view.findViewById(R.id.budget_form_progress_categories);
        layoutCategoryError = view.findViewById(R.id.budget_form_layout_category_error);
        tvCategoryError = view.findViewById(R.id.budget_form_tv_category_error);
        MaterialButton btnRetry = view.findViewById(R.id.budget_form_btn_retry);
        tvExistingHint = view.findViewById(R.id.budget_form_tv_existing_hint);
        etLimit = view.findViewById(R.id.budget_form_et_limit);
        tvError = view.findViewById(R.id.budget_form_tv_error);
        MaterialButton btnCancel = view.findViewById(R.id.budget_form_btn_cancel);
        btnSave = view.findViewById(R.id.budget_form_btn_save);

        tvTitle.setText(editing != null ? R.string.budget_form_edit_title : R.string.budget_form_add_title);
        tvMonth.setText(getString(R.string.budget_form_month, DateUtils.monthTitle(month, year)));
        tvExistingHint.setText("ⓘ " + getString(R.string.budget_form_existing_hint));

        // Sua: dien san han muc (xoay man hinh thi EditText tu khoi phuc)
        if (editing != null && savedInstanceState == null) {
            etLimit.setText(CurrencyUtils.formatInput(Math.round(editing.getLimitAmount())));
            etLimit.setSelection(etLimit.getText().length());
        }
        setupLimitWatcher();

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategoryId = NO_CATEGORY;
            } else {
                View chip = group.findViewById(checkedIds.get(0));
                selectedCategoryId = (chip != null && chip.getTag() instanceof Integer) ? (Integer) chip.getTag() : NO_CATEGORY;
            }
            tvError.setVisibility(View.GONE);
            updateExistingHint();
        });

        btnRetry.setOnClickListener(v -> loadCategories());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> save());

        loadCategories();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_CATEGORY, selectedCategoryId);
    }

    /** Tu chen dau cham khi go: 2000000 -> 2.000.000. */
    private void setupLimitWatcher() {
        limitWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                long value = CurrencyUtils.parseDigits(s.toString());
                String formatted = value > 0 ? CurrencyUtils.formatInput(value) : "";
                if (!formatted.equals(s.toString())) {
                    etLimit.removeTextChangedListener(this);
                    etLimit.setText(formatted);
                    etLimit.setSelection(formatted.length());
                    etLimit.addTextChangedListener(this);
                }
                tvError.setVisibility(View.GONE);
            }
        };
        etLimit.addTextChangedListener(limitWatcher);
    }

    private boolean isViewAlive() {
        return isAdded() && getView() != null;
    }

    private void loadCategories() {
        progressCategories.setVisibility(View.VISIBLE);
        chipGroup.setVisibility(View.INVISIBLE);
        layoutCategoryError.setVisibility(View.GONE);

        repository.getExpenseCategories(new BudgetRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                if (!isViewAlive()) return;
                progressCategories.setVisibility(View.GONE);
                chipGroup.setVisibility(View.VISIBLE);
                buildChips(data);
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (!isViewAlive()) return;
                progressCategories.setVisibility(View.GONE);
                chipGroup.setVisibility(View.GONE);
                layoutCategoryError.setVisibility(View.VISIBLE);
                tvCategoryError.setText(message);
            }
        });
    }

    private void buildChips(List<Category> data) {
        List<Category> categories = new ArrayList<>();
        boolean hasEditing = false;
        if (data != null) {
            for (Category c : data) {
                if (c == null) continue;
                if (c.getType() != null && !"expense".equalsIgnoreCase(c.getType())) continue;
                categories.add(c);
                if (editing != null && c.getId() == editing.getCategoryId()) {
                    hasEditing = true;
                }
            }
        }
        // Danh muc dang sua khong con trong danh sach (hiem): van hien de khoa chon
        if (editing != null && !hasEditing) {
            categories.add(0, new Category(editing.getCategoryId(), editing.getCategoryName(),
                    editing.getCategoryIcon(), editing.getCategoryColor(), "expense", false));
        }

        Context ctx = requireContext();
        int accent = ContextCompat.getColor(ctx, R.color.color_accent);
        int bgCardSec = ContextCompat.getColor(ctx, R.color.bg_card_secondary);
        int textPrimary = ContextCompat.getColor(ctx, R.color.text_primary);
        int white = ContextCompat.getColor(ctx, R.color.white);
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}};
        ColorStateList chipBg = new ColorStateList(states, new int[]{accent, bgCardSec});
        ColorStateList chipText = new ColorStateList(states, new int[]{white, textPrimary});

        int keepSelected = selectedCategoryId;
        chipGroup.removeAllViews();
        for (Category c : categories) {
            Chip chip = new Chip(ctx);
            chip.setId(View.generateViewId());
            chip.setTag(c.getId());
            String icon = c.getIcon() != null ? c.getIcon() + " " : "";
            chip.setText(icon + c.getName());
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColor(chipBg);
            chip.setTextColor(chipText);
            chip.setChipStrokeWidth(0f);
            chipGroup.addView(chip);

            if (c.getId() == keepSelected) {
                chip.setChecked(true);
            }
            // Che do Sua: khoa danh muc
            if (editing != null && c.getId() != editing.getCategoryId()) {
                chip.setEnabled(false);
                chip.setAlpha(0.4f);
            }
        }
        selectedCategoryId = keepSelected;
        updateExistingHint();
    }

    private void updateExistingHint() {
        boolean show = editing == null
                && selectedCategoryId != NO_CATEGORY
                && budgetedIds.contains(selectedCategoryId);
        tvExistingHint.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showFormError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void save() {
        if (saving) return;

        // Kiem tra tai may, khong goi API
        if (selectedCategoryId == NO_CATEGORY) {
            showFormError(getString(R.string.budget_error_category));
            return;
        }
        long limit = CurrencyUtils.parseDigits(etLimit.getText().toString());
        if (limit <= 0) {
            showFormError(getString(R.string.budget_error_limit));
            return;
        }

        saving = true;
        btnSave.setEnabled(false);
        tvError.setVisibility(View.GONE);

        final Context appContext = requireContext().getApplicationContext();
        repository.saveBudget(selectedCategoryId, limit, month, year, new BudgetRepository.Callback<Budget>() {
            @Override
            public void onSuccess(Budget data) {
                Toast.makeText(appContext, R.string.budget_saved, Toast.LENGTH_SHORT).show();
                if (!isAdded()) return;
                getParentFragmentManager().setFragmentResult(RESULT_BUDGET_CHANGED, new Bundle());
                dismissAllowingStateLoss();
            }

            @Override
            public void onError(String message, boolean isNetworkError) {
                if (!isViewAlive()) return;
                // Loi server: giu dialog mo
                saving = false;
                btnSave.setEnabled(true);
                showFormError(message);
            }
        });
    }
}
