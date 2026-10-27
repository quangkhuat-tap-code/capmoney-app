package com.example.comgroupcapmoney.data.repository;

import android.content.Context;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.BudgetApi;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Category;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Response;

public class BudgetRepository {

    public interface Callback<T> {
        void onSuccess(T data);

        /** isNetworkError = true khi khong goi duoc may chu (mat mang, server tat). */
        void onError(String message, boolean isNetworkError);
    }

    private final Context appContext;
    private final BudgetApi api;
    private final Gson gson = new Gson();

    public BudgetRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.api = ApiClient.getClient(appContext).create(BudgetApi.class);
    }

    public void getBudgets(int month, int year, Callback<List<Budget>> cb) {
        enqueue(api.getBudgets(month, year), cb);
    }

    public void saveBudget(int categoryId, long limit, int month, int year, Callback<Budget> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("category_id", categoryId);
        body.put("limit_amount", limit);
        body.put("month", month);
        body.put("year", year);
        enqueue(api.saveBudget(body), cb);
    }

    public void deleteBudget(int id, Callback<Object> cb) {
        enqueue(api.deleteBudget(id), cb);
    }

    public void getExpenseCategories(Callback<List<Category>> cb) {
        enqueue(api.getCategories("expense"), cb);
    }

    private <T> void enqueue(Call<ApiResponse<T>> call, Callback<T> cb) {
        call.enqueue(new retrofit2.Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> c, Response<ApiResponse<T>> response) {
                handleResponse(response, cb);
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> c, Throwable t) {
                if (t instanceof IOException) {
                    cb.onError(appContext.getString(R.string.stats_error_network), true);
                } else {
                    cb.onError(appContext.getString(R.string.stats_error_unknown), false);
                }
            }
        });
    }

    private <T> void handleResponse(Response<ApiResponse<T>> response, Callback<T> cb) {
        if (response.isSuccessful()) {
            ApiResponse<T> body = response.body();
            if (body != null && body.isSuccess()) {
                cb.onSuccess(body.getData());
            } else if (body != null && body.getMessage() != null && !body.getMessage().isEmpty()) {
                cb.onError(body.getMessage(), false);
            } else {
                cb.onError(appContext.getString(R.string.stats_error_unknown), false);
            }
            return;
        }

        if (response.code() == 401) {
            cb.onError(appContext.getString(R.string.stats_error_unauthorized), false);
            return;
        }

        String message = null;
        if (response.errorBody() != null) {
            try {
                ApiResponse<?> error = gson.fromJson(response.errorBody().string(), ApiResponse.class);
                if (error != null) {
                    message = error.getMessage();
                }
            } catch (Exception ignored) {
                // Body loi khong phai JSON: dung thong bao chung ben duoi
            }
        }
        if (message == null || message.trim().isEmpty()) {
            message = appContext.getString(R.string.stats_error_unknown);
        }
        cb.onError(message, false);
    }
}
