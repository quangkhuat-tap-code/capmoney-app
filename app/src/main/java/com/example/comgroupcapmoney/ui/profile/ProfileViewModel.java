package com.example.comgroupcapmoney.ui.profile;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.AuthRepository;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.model.User;
import com.example.comgroupcapmoney.model.UserProfile;
import com.example.comgroupcapmoney.utils.ImageUtils;
import com.example.comgroupcapmoney.utils.SingleEvent;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<UserProfile> profile = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SingleEvent<Boolean>> unauthorized = new MutableLiveData<>();

    // Anh dai dien (4B-3)
    private final MutableLiveData<Boolean> uploadingAvatar = new MutableLiveData<>(false);
    private final MutableLiveData<SingleEvent<String>> avatarUpdated = new MutableLiveData<>();
    private final MutableLiveData<SingleEvent<String>> avatarError = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<Boolean> getUploadingAvatar() { return uploadingAvatar; }
    public LiveData<SingleEvent<String>> getAvatarUpdated() { return avatarUpdated; }
    public LiveData<SingleEvent<String>> getAvatarError() { return avatarError; }

    // Nen anh tren may (<= 1280px, xoay dung chieu) roi tai len
    public void uploadAvatar(Uri uri) {
        if (uri == null || Boolean.TRUE.equals(uploadingAvatar.getValue())) return;
        uploadingAvatar.setValue(true);
        executor.execute(() -> {
            File file;
            try {
                file = ImageUtils.compressForUpload(getApplication(), uri);
            } catch (Exception e) {
                uploadingAvatar.postValue(false);
                avatarError.postValue(new SingleEvent<>(getApplication().getString(R.string.capture_err_image)));
                return;
            }
            final File upload = file;
            new Handler(Looper.getMainLooper()).post(() -> repository.uploadAvatar(upload, new RepoCallback<User>() {
                @Override
                public void onSuccess(User data, String message) {
                    upload.delete();
                    uploadingAvatar.setValue(false);
                    UserProfile current = profile.getValue();
                    if (current != null && data != null) {
                        current.setAvatarUrl(data.getAvatarUrl());
                        profile.setValue(current);
                    }
                    avatarUpdated.setValue(new SingleEvent<>(message));
                }

                @Override
                public void onError(String message, boolean isUnauthorized) {
                    upload.delete();
                    uploadingAvatar.setValue(false);
                    if (isUnauthorized) {
                        unauthorized.setValue(new SingleEvent<>(true));
                    } else {
                        avatarError.setValue(new SingleEvent<>(message));
                    }
                }
            }));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
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
