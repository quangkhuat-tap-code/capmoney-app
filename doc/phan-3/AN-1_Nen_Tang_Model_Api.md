# AN-1 · Nền tảng: model, Retrofit, repository, chữ hiển thị

> **Phần 3** · Repo `capmoney-app` · Nhánh `feature/stats-budget` · Task trước: [BE-2](BE-2_Api_Ngan_Sach.md) · Task sau: [AN-2](AN-2_Tab_Thong_Ke.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 3.2, 4, 5 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 2 (khuôn JSON)

## Mục tiêu

Tạo lớp dữ liệu cho 2 tab Thống kê và Ngân sách. Task này **chưa có màn hình**, chỉ cần build được.

**Điều kiện:** `develop` của app đã merge Phần 1 (xem 00, mục 3.2). Build `develop` lỗi thì dừng, báo Thành viên 1.

## File

```text
model/StatsSummary.java, CategoryStat.java, Budget.java
data/api/StatsApi.java, BudgetApi.java
data/repository/StatsRepository.java, BudgetRepository.java
res/values/strings_stats_budget.xml
```

(Đường dẫn Java tính từ `app/src/main/java/com/example/comgroupcapmoney/`.)

## 1. Model (`model/`, có `@SerializedName` đúng tên JSON)

| Class | Field (tên JSON) |
|---|---|
| `CategoryStat` | `category_id` int, `category_name` String, `category_icon` String, `category_color` String, `amount` double, `count` int, `percent` double |
| `StatsSummary` | `month` int, `year` int, `type` String, `total_income` double, `total_expense` double, `total` double, `categories` `List<CategoryStat>` |
| `Budget` | `id` int, `category_id` int, `category_name`, `category_icon`, `category_color` String, `limit_amount` double, `spent_amount` double, `remaining` double, `percent` int, `status` String, `month` int, `year` int. `implements Serializable` |

Thêm constructor đầy đủ cho `StatsSummary` và `CategoryStat` (AN-3 dùng để tạo khi offline), kèm constructor rỗng cho Gson.

## 2. Retrofit

```java
public interface StatsApi {
    @GET("analytics/summary")
    Call<ApiResponse<StatsSummary>> getSummary(@Query("month") int month, @Query("year") int year, @Query("type") String type);

    // API Phần 1 (chỉ đọc) — AN-3 dùng để lưu giao dịch vào Room
    @GET("transactions")
    Call<ApiResponse<List<Transaction>>> getTransactions(@Query("month") int month, @Query("year") int year);
}

public interface BudgetApi {
    @GET("budgets")
    Call<ApiResponse<List<Budget>>> getBudgets(@Query("month") int month, @Query("year") int year);

    @POST("budgets")
    Call<ApiResponse<Budget>> saveBudget(@Body Map<String, Object> body);

    @DELETE("budgets/{id}")
    Call<ApiResponse<Object>> deleteBudget(@Path("id") int id);

    // API Phần 1 (chỉ đọc) — danh mục chi tiêu để chọn khi đặt ngân sách
    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories(@Query("type") String type);   // truyền "expense"
}
```

Lấy instance: `ApiClient.getClient(context).create(StatsApi.class)`. **Không** sửa `ApiClient`, `TransactionApi`.

## 3. Repository

Callback khai báo **lồng** trong từng repository (không tạo file `RepoCallback.java`):

```java
public class StatsRepository {
    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message, boolean isNetworkError);   // true khi onFailure (mất mạng, server tắt)
    }
    ...
}
```

- `StatsRepository`: `getSummary(month, year, type, cb)`, `getTransactions(month, year, cb)`.
- `BudgetRepository` (cũng có `Callback<T>` lồng giống trên): `getBudgets(month, year, cb)`, `saveBudget(categoryId, limit, month, year, cb)`, `deleteBudget(id, cb)`, `getExpenseCategories(cb)`.
- `response.isSuccessful()` và `body().isSuccess()` → `onSuccess(body.getData())`.
- Response lỗi (4xx/5xx): đọc `response.errorBody().string()`, parse bằng Gson ra `message` của server. HTTP 401 → `stats_error_unauthorized`. Parse lỗi → `stats_error_unknown`. Gọi `onError(message, false)`.
- `onFailure` → `onError(getString(R.string.stats_error_network), true)`.
- Giữ `Context` bằng `context.getApplicationContext()`.

## 4. Chữ hiển thị (`res/values/strings_stats_budget.xml`)

Mỗi dòng: `tên  nội dung`. Viết thành `<string name="tên">nội dung</string>`. Có `%1$s`, `%1$d` thì giữ nguyên.

```text
stats_title                     Thống kê
stats_tab_expense               Chi tiêu
stats_tab_income                Thu nhập
stats_total_expense             Tổng chi
stats_total_income              Tổng thu
stats_summary_line              Thu %1$s · Chi %2$s
stats_count                     %1$d giao dịch
stats_empty_expense             Chưa có khoản chi nào trong tháng này
stats_empty_income              Chưa có khoản thu nào trong tháng này
stats_offline_banner            Đang xem dữ liệu đã lưu lúc %1$s (không có mạng)
stats_offline_no_cache          Không có mạng và chưa có dữ liệu lưu sẵn cho tháng này
stats_error_network             Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại
stats_error_unknown             Có lỗi xảy ra, vui lòng thử lại
stats_error_unauthorized        Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại
stats_retry                     Thử lại

budget_title                    Ngân sách
budget_add                      Thêm
budget_summary_limit            Tổng hạn mức
budget_summary_spent            Đã chi
budget_summary_remaining        Còn lại
budget_item_spent               Đã tiêu %1$s / %2$s
budget_item_remaining           Còn %1$s
budget_item_over                Vượt %1$s
budget_empty                    Chưa đặt ngân sách cho tháng này
budget_empty_action             Đặt ngân sách
budget_form_add_title           Đặt ngân sách
budget_form_edit_title          Sửa hạn mức
budget_form_category            Danh mục chi tiêu
budget_form_limit_hint          Hạn mức trong tháng
budget_form_month               Áp dụng cho %1$s
budget_form_existing_hint       Danh mục này đã có ngân sách tháng này, lưu sẽ cập nhật hạn mức
budget_error_category           Vui lòng chọn danh mục
budget_error_limit              Hạn mức phải lớn hơn 0
budget_save                     Lưu
budget_cancel                   Huỷ
budget_saved                    Đã lưu ngân sách
budget_delete_title             Xoá ngân sách?
budget_delete_message           Xoá hạn mức %1$s của %2$s?
budget_delete                   Xoá
budget_deleted                  Đã xoá ngân sách
```

## Test

```bash
./gradlew :app:assembleDebug
```

Phải thành công. App chạy lên vẫn như cũ.

## Commit

```text
feat: model, api va repository thong ke ngan sach

- Them model StatsSummary, CategoryStat, Budget
- Them StatsApi, BudgetApi va StatsRepository, BudgetRepository doc message loi tu server, bao rieng loi mat mang
- Them strings_stats_budget.xml
```

Không thêm dòng `Co-Authored-By` hay tên công cụ AI.

## Xong khi

- [ ] Build thành công.
- [ ] Không sửa file nào ngoài danh sách (00, mục 4).
- [ ] Đã commit.
