package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Category;
import com.example.comgroupcapmoney.model.DeleteTransactionResult;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.ui.capture.WalletOption;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransactionApi {
    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories(@Query("type") String type);

    @GET("wallets")
    Call<ApiResponse<List<WalletOption>>> getWallets();

    @Multipart
    @POST("transactions")
    Call<ApiResponse<Transaction>> createTransaction(
            @Part MultipartBody.Part image,
            @Part("amount") RequestBody amount,
            @Part("wallet_id") RequestBody walletId,
            @Part("category_id") RequestBody categoryId,
            @Part("type") RequestBody type,
            @Part("note") RequestBody note,
            @Part("transaction_date") RequestBody transactionDate);

    @GET("transactions")
    Call<ApiResponse<List<Transaction>>> getTransactions(
            @Query("month") Integer month,
            @Query("year") Integer year,
            @Query("wallet_id") Integer walletId);

    @DELETE("transactions/{id}")
    Call<ApiResponse<DeleteTransactionResult>> deleteTransaction(@Path("id") int id);
}
