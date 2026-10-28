package com.example.comgroupcapmoney.data.repository;

import android.content.Context;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.data.api.WalletApi;
import com.example.comgroupcapmoney.model.Transfer;
import com.example.comgroupcapmoney.model.TransferResult;
import com.example.comgroupcapmoney.model.Wallet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletRepository {

    private final Context appContext;
    private final WalletApi api;

    public WalletRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.api = ApiClient.getClient(appContext).create(WalletApi.class);
    }

    public void getWallets(RepoCallback<List<Wallet>> cb) {
        api.getWallets().enqueue(AuthRepository.wrap(appContext, cb, true));
    }

    public void createWallet(String name, String type, long balance, RepoCallback<Wallet> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("type", type);
        body.put("balance", balance);
        api.createWallet(body).enqueue(AuthRepository.wrap(appContext, cb, true));
    }

    // Chi gui field khac null
    public void updateWallet(int id, String name, String type, Long balance, RepoCallback<Wallet> cb) {
        Map<String, Object> body = new HashMap<>();
        if (name != null) {
            body.put("name", name);
        }
        if (type != null) {
            body.put("type", type);
        }
        if (balance != null) {
            body.put("balance", balance);
        }
        api.updateWallet(id, body).enqueue(AuthRepository.wrap(appContext, cb, true));
    }

    public void transfer(int fromWalletId, int toWalletId, long amount, String note, RepoCallback<TransferResult> cb) {
        Map<String, Object> body = new HashMap<>();
        body.put("from_wallet_id", fromWalletId);
        body.put("to_wallet_id", toWalletId);
        body.put("amount", amount);
        if (note != null && !note.trim().isEmpty()) {
            body.put("note", note.trim());
        }
        api.transfer(body).enqueue(AuthRepository.wrap(appContext, cb, true));
    }

    public void getTransfers(int month, int year, RepoCallback<List<Transfer>> cb) {
        api.getTransfers(month, year).enqueue(AuthRepository.wrap(appContext, cb, true));
    }
}
