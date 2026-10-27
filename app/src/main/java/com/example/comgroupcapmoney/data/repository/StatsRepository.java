package com.example.comgroupcapmoney.data.repository;

import android.content.Context;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.StatsApi;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.StatsSummary;
import com.example.comgroupcapmoney.model.Transaction;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class StatsRepository {

    public interface Callback<T> {
        void onSuccess(T data);

        /** isNetworkError = true khi khong goi duoc may chu (mat mang, server tat). */
        void onError(String message, boolean isNetworkError);
    }

    private final Context appContext;
    private final StatsApi api;
    private final Gson gson = new Gson();

    public StatsRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.api = ApiClient.getClient(appContext).create(StatsApi.class);
    }

    public void getSummary(int month, int year, String type, Callback<StatsSummary> cb) {
        enqueue(api.getSummary(month, year, type), cb);
    }

    public void getTransactions(int month, int year, Callback<List<Transaction>> cb) {
        enqueue(api.getTransactions(month, year), cb);
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
