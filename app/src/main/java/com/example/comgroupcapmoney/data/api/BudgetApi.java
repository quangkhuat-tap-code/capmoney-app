package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Category;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BudgetApi {
    @GET("budgets")
    Call<ApiResponse<List<Budget>>> getBudgets(@Query("month") int month, @Query("year") int year);

    @POST("budgets")
    Call<ApiResponse<Budget>> saveBudget(@Body Map<String, Object> body);

    @DELETE("budgets/{id}")
    Call<ApiResponse<Object>> deleteBudget(@Path("id") int id);

    // API Phan 1 (chi doc) - danh muc chi tieu de chon khi dat ngan sach
    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories(@Query("type") String type);
}
