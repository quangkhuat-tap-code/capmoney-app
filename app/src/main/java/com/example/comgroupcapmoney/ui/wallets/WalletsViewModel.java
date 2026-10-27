package com.example.comgroupcapmoney.ui.wallets;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    public WalletsViewModel(@NonNull Application application) {
        super(application);
        repository = new WalletRepository(application);
    }

    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<SingleEvent<Boolean>> getUnauthorized() { return unauthorized; }

    public void loadWallets() {
        if (Boolean.TRUE.equals(loading.getValue())) return;
        loading.setValue(true);
        error.setValue(null);
        repository.getWallets(new RepoCallback<List<Wallet>>() {
            @Override
            public void onSuccess(List<Wallet> data, String message) {
                wallets.setValue(data);
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
