package com.example.comgroupcapmoney.ui.wallets;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.local.WalletCache;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.data.repository.WalletRepository;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.utils.SingleEvent;
import java.util.List;

public class WalletsViewModel extends AndroidViewModel {

    private final WalletRepository repository;
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<SingleEvent<Boolean>> unauthorized = new MutableLiveData<>();
    // 4C-3: null = so lieu online; khac null = dang xem ban luu luc nay (millis)
    private final MutableLiveData<Long> offlineCachedAt = new MutableLiveData<>(null);
    private final WalletCache cache;

    public WalletsViewModel(@NonNull Application application) {
        super(application);
        repository = new WalletRepository(application);
        cache = new WalletCache(application);
    }

    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<SingleEvent<Boolean>> getUnauthorized() { return unauthorized; }
    public LiveData<Long> getOfflineCachedAt() { return offlineCachedAt; }

    public boolean isOffline() {
        return offlineCachedAt.getValue() != null;
    }

    public void loadWallets() {
        if (Boolean.TRUE.equals(loading.getValue())) return;
        loading.setValue(true);
        error.setValue(null);
        repository.getWallets(new RepoCallback<List<Wallet>>() {
            @Override
            public void onSuccess(List<Wallet> data, String message) {
                offlineCachedAt.setValue(null);
                wallets.setValue(data);
                loading.setValue(false);
                cache.saveWallets(data);
            }

            @Override
            public void onError(String message, boolean isUnauthorized) {
                if (isUnauthorized) {
                    loading.setValue(false);
                    unauthorized.setValue(new SingleEvent<>(true));
                } else if (getApplication().getString(R.string.common_error_network).equals(message)) {
                    // Mat mang: hien ban da luu (neu co); loi khac (400, 500) thi bao loi nhu cu
                    loadFromCache(message);
                } else {
                    loading.setValue(false);
                    error.setValue(message);
                }
            }
        });
    }

    private void loadFromCache(String networkMessage) {
        final long[] at = {0};
        cache.getWallets(list -> {
            loading.setValue(false);
            if (list == null || list.isEmpty()) {
                offlineCachedAt.setValue(null);
                error.setValue(networkMessage);
                return;
            }
            offlineCachedAt.setValue(at[0]);
            wallets.setValue(list);
        }, cachedAt -> at[0] = cachedAt);
    }
}
