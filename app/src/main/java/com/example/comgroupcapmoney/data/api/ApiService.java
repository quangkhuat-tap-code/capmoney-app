package com.example.comgroupcapmoney.data.api;

import com.example.comgroupcapmoney.model.AnalyticsSummary;
import com.example.comgroupcapmoney.model.ApiResponse;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit Interface — khai báo tất cả endpoint REST API của CapMoney.
 *
 * Thành viên 3 chịu trách nhiệm các endpoint:
 *   - GET /api/analytics/summary  → Thống kê Donut Chart (Tab 2)
 *   - GET /api/budgets            → Danh sách ngân sách (Tab 4)
 *   - POST /api/budgets           → Tạo / cập nhật ngân sách
 *   - DELETE /api/budgets/:id     → Xóa ngân sách
 *
 * (Thành viên 1, 2 sẽ bổ sung endpoint của họ vào interface này)
 */
public interface ApiService {

    // ════════════════════════════════════════════════
    //  CATEGORIES — danh mục thu/chi
    // ════════════════════════════════════════════════

    /**
     * GET /api/categories?type=expense|income
     *
     * Trả về danh sách danh mục hệ thống + danh mục của user.
     * @param type (tùy chọn) lọc theo "expense" hoặc "income"
     */
    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories(
        @Query("type") String type
    );

    // ════════════════════════════════════════════════
    //  ANALYTICS — Tab 2 Thống kê Donut Chart
    // ════════════════════════════════════════════════

    /**
     * GET /api/analytics/summary?month=&year=&type=expense|income
     *
     * Trả về tổng thu, tổng chi và danh sách % từng danh mục
     * để vẽ Donut Chart và hiển thị danh sách bên dưới.
     *
     * @param month  tháng (1–12)
     * @param year   năm (vd: 2026)
     * @param type   "expense" hoặc "income"
     */
    @GET("analytics/summary")
    Call<ApiResponse<AnalyticsSummary>> getAnalyticsSummary(
        @Query("month") int month,
        @Query("year")  int year,
        @Query("type")  String type
    );

    // ════════════════════════════════════════════════
    //  BUDGETS — Tab 4 Ngân sách
    // ════════════════════════════════════════════════

    /**
     * GET /api/budgets?month=&year=
     *
     * Trả về danh sách ngân sách kèm spent_amount (số đã tiêu thực tế).
     */
    @GET("budgets")
    Call<ApiResponse<List<Budget>>> getBudgets(
        @Query("month") int month,
        @Query("year")  int year
    );

    /**
     * POST /api/budgets
     *
     * Tạo mới hoặc cập nhật ngân sách (upsert theo category + tháng + năm).
     * Body là Budget object — Gson tự serialize thành JSON.
     */
    @POST("budgets")
    Call<ApiResponse<Budget>> createBudget(@Body Budget budget);

    /**
     * DELETE /api/budgets/{id}
     *
     * Xóa ngân sách theo id.
     */
    @DELETE("budgets/{id}")
    Call<ApiResponse<Void>> deleteBudget(@Path("id") int budgetId);
}
