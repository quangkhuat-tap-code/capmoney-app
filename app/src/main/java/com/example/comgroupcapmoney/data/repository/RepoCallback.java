package com.example.comgroupcapmoney.data.repository;

public interface RepoCallback<T> {
    void onSuccess(T data, String message);

    // unauthorized = true khi HTTP 401 o API can token (token het han)
    void onError(String message, boolean unauthorized);
}
