package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.StatsSummary;
import com.example.comgroupcapmoney.model.Transaction;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StatsApi {
    @GET("analytics/summary")
    Call<ApiResponse<StatsSummary>> getSummary(@Query("month") int month,
                                               @Query("year") int year,
                                               @Query("type") String type);

    // API Phan 1 (chi doc) - AN-3 dung de luu giao dich vao Room
    @GET("transactions")
    Call<ApiResponse<List<Transaction>>> getTransactions(@Query("month") int month,
                                                         @Query("year") int year);
}
