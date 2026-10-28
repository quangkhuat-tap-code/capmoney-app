package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Transfer;
import com.example.comgroupcapmoney.model.TransferResult;
import com.example.comgroupcapmoney.model.Wallet;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WalletApi {
    @GET("wallets")
    Call<ApiResponse<List<Wallet>>> getWallets();

    @POST("wallets")
    Call<ApiResponse<Wallet>> createWallet(@Body Map<String, Object> body);

    @PUT("wallets/{id}")
    Call<ApiResponse<Wallet>> updateWallet(@Path("id") int id, @Body Map<String, Object> body);

    @POST("wallets/transfer")
    Call<ApiResponse<TransferResult>> transfer(@Body Map<String, Object> body);

    @GET("wallets/transfers")
    Call<ApiResponse<List<Transfer>>> getTransfers(@Query("month") int month, @Query("year") int year);
}
