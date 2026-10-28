package com.example.comgroupcapmoney.ui.budget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.ApiService;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Category;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialog đặt hạn mức ngân sách mới.
 *
 * Dùng Dialog thay vì BottomSheetDialog để đơn giản hơn.
 * Caller truyền vào:
 *   - danh sách danh mục (đã lấy từ /api/categories)
 *   - tháng / năm hiện tại
 *   - callback OnSaved khi lưu thành công
 */
public class AddBudgetDialog extends Dialog {

    public interface OnBudgetSavedListener {
        void onSaved(Budget savedBudget);
    }

    private final List<Category> categories;
    private final int month;
    private final int year;
    private final OnBudgetSavedListener listener;
    private ApiService apiService;

    public AddBudgetDialog(@NonNull Context context,
                           List<Category> categories,
                           int month,
                           int year,
                           OnBudgetSavedListener listener) {
        super(context, android.R.style.Theme_Material_Dialog_Alert);
        this.categories = categories;
        this.month      = month;
        this.year       = year;
        this.listener   = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_budget, null);
        setContentView(view);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.bg_card);
        }

        apiService = ApiClient.getClient(getContext()).create(ApiService.class);

        Spinner spinnerCategory   = view.findViewById(R.id.spinner_category);
        TextInputEditText etLimit = view.findViewById(R.id.et_limit_amount);
        Button btnCancel          = view.findViewById(R.id.btn_cancel);
        Button btnSave            = view.findViewById(R.id.btn_save);

        // Điền danh sách danh mục vào Spinner
        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            categoryNames[i] = (cat.getIcon() != null ? cat.getIcon() + " " : "")
                    + cat.getName();
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Hủy dialog
        btnCancel.setOnClickListener(v -> dismiss());

        // Lưu ngân sách
        btnSave.setOnClickListener(v -> {
            String amountStr = etLimit.getText() != null
                    ? etLimit.getText().toString().trim() : "";

            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền hạn mức", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount <= 0) {
                Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = spinnerCategory.getSelectedItemPosition();
            if (selectedIndex < 0 || selectedIndex >= categories.size()) {
                Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            Category selectedCategory = categories.get(selectedIndex);

            // Tạo Budget object để POST
            Budget budget = new Budget();
            budget.setCategoryId(selectedCategory.getId());
            budget.setLimitAmount(amount);
            budget.setMonth(month);
            budget.setYear(year);

            btnSave.setEnabled(false);
            saveBudget(budget);
        });
    }

    private void saveBudget(Budget budget) {
        apiService.createBudget(budget).enqueue(new Callback<ApiResponse<Budget>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Budget>> call,
                                   @NonNull Response<ApiResponse<Budget>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    if (listener != null) {
                        listener.onSaved(response.body().getData());
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Lưu thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    View saveBtn = findViewById(R.id.btn_save);
                    if (saveBtn != null) saveBtn.setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Budget>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
                View saveBtn = findViewById(R.id.btn_save);
                if (saveBtn != null) saveBtn.setEnabled(true);
            }
        });
    }
}
