package com.example.comgroupcapmoney.data.repository;

import android.content.Context;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.TransactionApi;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Category;
import com.example.comgroupcapmoney.model.DeleteTransactionResult;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.ui.capture.WalletOption;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionRepository {

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    private final Context appContext;
    private final TransactionApi api;
    private final Gson gson = new Gson();

    public TransactionRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.api = ApiClient.getClient(appContext).create(TransactionApi.class);
    }

    public void getCategories(String type, ResultCallback<List<Category>> cb) {
        api.getCategories(type).enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                handleResponse(response, cb);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                handleFailure(t, cb);
            }
        });
    }

    public void getWallets(ResultCallback<List<WalletOption>> cb) {
        api.getWallets().enqueue(new Callback<ApiResponse<List<WalletOption>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<WalletOption>>> call, Response<ApiResponse<List<WalletOption>>> response) {
                handleResponse(response, cb);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<WalletOption>>> call, Throwable t) {
                handleFailure(t, cb);
            }
        });
    }

    public void getTransactions(int month, int year, ResultCallback<List<Transaction>> cb) {
        api.getTransactions(month, year, null).enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                handleResponse(response, cb);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                handleFailure(t, cb);
            }
        });
    }

    public void createTransaction(File imageFileOrNull, long amount, int walletId, int categoryId,
                                  String type, String note, String transactionDate,
                                  ResultCallback<Transaction> cb) {
        MediaType textMedia = MediaType.parse("text/plain");

        MultipartBody.Part imagePart = null;
        if (imageFileOrNull != null && imageFileOrNull.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFileOrNull);
            imagePart = MultipartBody.Part.createFormData("image", "upload.jpg", fileBody);
        }

        RequestBody amountBody = RequestBody.create(textMedia, String.valueOf(amount));
        RequestBody walletIdBody = RequestBody.create(textMedia, String.valueOf(walletId));
        RequestBody categoryIdBody = RequestBody.create(textMedia, String.valueOf(categoryId));
        RequestBody typeBody = RequestBody.create(textMedia, type);
        RequestBody noteBody = RequestBody.create(textMedia, note != null ? note : "");
        RequestBody dateBody = RequestBody.create(textMedia, transactionDate != null ? transactionDate : "");

        api.createTransaction(imagePart, amountBody, walletIdBody, categoryIdBody, typeBody, noteBody, dateBody)
                .enqueue(new Callback<ApiResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Transaction>> call, Response<ApiResponse<Transaction>> response) {
                        handleResponse(response, cb);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Transaction>> call, Throwable t) {
                        handleFailure(t, cb);
                    }
                });
    }

    public void deleteTransaction(int id, ResultCallback<DeleteTransactionResult> cb) {
        api.deleteTransaction(id).enqueue(new Callback<ApiResponse<DeleteTransactionResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<DeleteTransactionResult>> call, Response<ApiResponse<DeleteTransactionResult>> response) {
                handleResponse(response, cb);
            }

            @Override
            public void onFailure(Call<ApiResponse<DeleteTransactionResult>> call, Throwable t) {
                handleFailure(t, cb);
            }
        });
    }

    private <T> void handleResponse(Response<ApiResponse<T>> response, ResultCallback<T> cb) {
        if (response.isSuccessful()) {
            ApiResponse<T> body = response.body();
            if (body != null) {
                if (body.isSuccess()) {
                    cb.onSuccess(body.getData());
                } else {
                    String msg = body.getMessage();
                    cb.onError(msg != null ? msg : appContext.getString(R.string.capture_error_unknown));
                }
            } else {
                cb.onError(appContext.getString(R.string.capture_error_unknown));
            }
        } else {
            if (response.code() == 401) {
                cb.onError(appContext.getString(R.string.capture_error_unauthorized));
                return;
            }

            if (response.errorBody() != null) {
                try {
                    String errorJson = response.errorBody().string();
                    Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                    ApiResponse<Object> errorResp = gson.fromJson(errorJson, type);
                    if (errorResp != null && errorResp.getMessage() != null && !errorResp.getMessage().isEmpty()) {
                        cb.onError(errorResp.getMessage());
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
            cb.onError(appContext.getString(R.string.capture_error_unknown));
        }
    }

    private <T> void handleFailure(Throwable t, ResultCallback<T> cb) {
        if (t instanceof IOException) {
            cb.onError(appContext.getString(R.string.capture_error_network));
        } else {
            cb.onError(appContext.getString(R.string.capture_error_unknown));
        }
    }
}
