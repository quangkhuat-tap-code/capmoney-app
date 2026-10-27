package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.TrendPoint;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Tach khoi StatsApi cho ro phan 4C-1
public interface AnalyticsTrendApi {
    @GET("analytics/trend")
    Call<ApiResponse<List<TrendPoint>>> getTrend(@Query("month") int month,
                                                 @Query("year") int year,
                                                 @Query("months") int months);
}
