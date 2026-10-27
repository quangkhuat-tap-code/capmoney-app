package com.example.comgroupcapmoney.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.data.repository.AuthRepository;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.model.UserProfile;
import com.example.comgroupcapmoney.utils.SingleEvent;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<UserProfile> profile = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SingleEvent<Boolean>> unauthorized = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<UserProfile> getProfile() { return profile; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<SingleEvent<Boolean>> getUnauthorized() { return unauthorized; }

    public void loadProfile() {
        if (Boolean.TRUE.equals(loading.getValue())) return;
        loading.setValue(true);
        error.setValue(null);
        repository.getProfile(new RepoCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data, String message) {
                profile.setValue(data);
                loading.setValue(false);
            }

            @Override
            public void onError(String message, boolean isUnauthorized) {
                loading.setValue(false);
                if (isUnauthorized) {
                    unauthorized.setValue(new SingleEvent<>(true));
                } else {
                    error.setValue(message);
                }
            }
        });
    }
}
