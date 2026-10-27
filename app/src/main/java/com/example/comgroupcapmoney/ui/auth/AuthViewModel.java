package com.example.comgroupcapmoney.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.data.repository.AuthRepository;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.model.AuthResult;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import com.example.comgroupcapmoney.utils.SingleEvent;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<AuthResult> success = new MutableLiveData<>();
    private final MutableLiveData<SingleEvent<String>> error = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<AuthResult> getSuccess() { return success; }
    public LiveData<SingleEvent<String>> getError() { return error; }

    public void login(String email, String password) {
        if (isLoading()) return;
        loading.setValue(true);
        repository.login(email, password, callback());
    }

    public void register(String fullName, String email, String password) {
        if (isLoading()) return;
        loading.setValue(true);
        repository.register(fullName, email, password, callback());
    }

    private boolean isLoading() {
        return Boolean.TRUE.equals(loading.getValue());
    }

    private RepoCallback<AuthResult> callback() {
        return new RepoCallback<AuthResult>() {
            @Override
            public void onSuccess(AuthResult data, String message) {
                if (data == null || data.getToken() == null || data.getUser() == null) {
                    onError(null, false);
                    return;
                }
                // Luu token ngay o day de man hinh bi tao lai van khong mat phien
                SharedPrefManager.getInstance(getApplication())
                        .saveAuthData(data.getToken(), data.getUser().getId(), data.getUser().getFullName());
                loading.setValue(false);
                success.setValue(data);
            }

            @Override
            public void onError(String message, boolean unauthorized) {
                loading.setValue(false);
                error.setValue(new SingleEvent<>(message != null ? message
                        : getApplication().getString(com.example.comgroupcapmoney.R.string.common_error_unknown)));
            }
        };
    }
}
