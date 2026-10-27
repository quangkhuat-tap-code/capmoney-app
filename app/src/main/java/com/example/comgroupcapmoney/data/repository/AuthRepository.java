package com.example.comgroupcapmoney.data.repository;

import android.content.Context;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.AuthApi;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.AuthResult;
import com.example.comgroupcapmoney.model.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final Context appContext;
    private final AuthApi api;

    public AuthRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.api = ApiClient.getClient(appContext).create(AuthApi.class);
    }

    public void register(String fullName, String email, String password, RepoCallback<AuthResult> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("password", password);
        api.register(body).enqueue(wrap(appContext, cb, false));
    }

    public void login(String email, String password, RepoCallback<AuthResult> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        // 401 o login la sai mat khau, khong phai het phien
        api.login(body).enqueue(wrap(appContext, cb, false));
    }

    public void getProfile(RepoCallback<UserProfile> cb) {
        api.getProfile().enqueue(wrap(appContext, cb, true));
    }

    // Sai mat khau hien tai server tra 400 nen khong bi coi la het phien
    public void changePassword(String currentPassword, String newPassword, RepoCallback<Object> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("current_password", currentPassword);
        body.put("new_password", newPassword);
        api.changePassword(body).enqueue(wrap(appContext, cb, true));
    }

    // Dung chung cho AuthRepository va WalletRepository
    static <T> Callback<ApiResponse<T>> wrap(Context appContext, RepoCallback<T> cb, boolean needsToken) {
        return new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<T> body = response.body();
                    if (body != null && body.isSuccess()) {
                        cb.onSuccess(body.getData(), body.getMessage());
                    } else {
                        String msg = body != null ? body.getMessage() : null;
                        cb.onError(msg != null ? msg : appContext.getString(R.string.common_error_unknown), false);
                    }
                    return;
                }
                boolean unauthorized = needsToken && response.code() == 401;
                cb.onError(readErrorMessage(appContext, response), unauthorized);
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                cb.onError(appContext.getString(R.string.common_error_network), false);
            }
        };
    }

    private static String readErrorMessage(Context appContext, Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorJson = response.errorBody().string();
                Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
                ApiResponse<Object> errorResp = new Gson().fromJson(errorJson, type);
                if (errorResp != null && errorResp.getMessage() != null && !errorResp.getMessage().isEmpty()) {
                    return errorResp.getMessage();
                }
            } catch (Exception ignored) {
            }
        }
        return appContext.getString(R.string.common_error_unknown);
    }
}
