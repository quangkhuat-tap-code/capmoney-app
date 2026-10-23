# PHẦN 3 · Tổng quan: Tab Thống kê (Donut Chart) + Tab Ngân sách + Cache offline Room

> **Người phụ trách:** Thành viên 3 (Stats & Budget) · **Nhánh:** `feature/stats-budget` (ở **cả 2 repo**) · **Ngày viết:** 23/10/2026
> **Đọc kèm:** [../Huong_Dan_Lam_Viec_Nhom.md](../Huong_Dan_Lam_Viec_Nhom.md) (quy trình Git, quy tắc commit, cách giao việc cho AI).

Bộ tài liệu này viết để **đưa thẳng cho AI** code theo. AI làm **đúng** theo tài liệu, không tự thêm tính năng. Chỗ nào chưa nói rõ thì chọn cách **đơn giản nhất** và ghi lại để báo trong PR.

---

## 1. Danh sách file và thứ tự làm

| Thứ tự | File | Repo | Nội dung |
|---|---|---|---|
| — | **00_Tong_Quan_Phan_3.md** (file này) | | Phạm vi, code đang có, ranh giới file, quy ước giao diện |
| — | [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) | | Bảng DB, JSON từng API, mã lỗi. **Hợp đồng chung** giữa Android và Backend |
| 1 | [BE-1_Api_Thong_Ke.md](BE-1_Api_Thong_Ke.md) | backend | `GET /api/analytics/summary` |
| 2 | [BE-2_Api_Ngan_Sach.md](BE-2_Api_Ngan_Sach.md) | backend | `GET/POST /api/budgets`, `DELETE /api/budgets/:id` → **push + tạo PR backend** |
| 3 | [AN-1_Nen_Tang_Model_Api.md](AN-1_Nen_Tang_Model_Api.md) | app | Model, Retrofit, Repository, chữ hiển thị |
| 4 | [AN-2_Tab_Thong_Ke.md](AN-2_Tab_Thong_Ke.md) | app | Tab 2: chọn tháng, Chi/Thu, biểu đồ donut, danh sách danh mục |
| 5 | [AN-3_Cache_Offline_Room.md](AN-3_Cache_Offline_Room.md) | app | Room lưu giao dịch, Tab Thống kê chạy khi mất mạng |
| 6 | [AN-4_Tab_Ngan_Sach.md](AN-4_Tab_Ngan_Sach.md) | app | Tab 4: danh sách hạn mức, thanh màu xanh/vàng/đỏ |
| 7 | [AN-5_Them_Sua_Xoa_Ngan_Sach.md](AN-5_Them_Sua_Xoa_Ngan_Sach.md) | app | Form đặt/sửa hạn mức, xoá bằng nhấn giữ |
| 8 | [99_Nghiem_Thu_Va_PR.md](99_Nghiem_Thu_Va_PR.md) | | Test toàn bộ, tạo PR app, báo nhóm |

**Mỗi file task = 1 commit.** Giao AI **1 file task** mỗi lần, kèm file 00 và 01 (xem prompt mẫu trong Hướng dẫn, mục 6.2).

Phần 3 **không phải chờ ai** để làm backend. Android chỉ cần `develop` đã có Phần 1 (mục 3.2). Chưa có màn đăng nhập thì test bằng token dev (Hướng dẫn, mục 10).

---

## 2. Phạm vi Phần 3

### 2.1. Làm

| Mảng | Hạng mục |
|---|---|
| Backend | `GET /api/analytics/summary?month=&year=&type=`: tổng thu, tổng chi, chi tiết theo danh mục kèm % (`SUM ... GROUP BY category_id`) |
| Backend | `GET /api/budgets?month=&year=`: danh sách ngân sách kèm số đã tiêu (`spent_amount`) bằng truy vấn con |
| Backend | `POST /api/budgets`: đặt hạn mức (đã có cùng danh mục + tháng thì **cập nhật**) |
| Backend | `DELETE /api/budgets/:id` |
| Android | Tab 2 `StatsFragment`: chọn tháng, chuyển Chi tiêu/Thu nhập, biểu đồ donut `PieChart`, danh sách danh mục kèm thanh % và số tiền |
| Android | Tab 4 `BudgetFragment`: danh sách hạn mức, thanh tiến độ đổi màu xanh/vàng/đỏ, thêm/sửa/xoá hạn mức |
| Android | Room (`data/local/*`): lưu giao dịch tải về; **mất mạng** thì Tab Thống kê vẫn hiện số liệu từ Room |

### 2.2. Không làm

- Biểu đồ theo ngày/tuần, so sánh nhiều tháng, xuất file.
- Ngân sách cho danh mục **thu nhập**, ngân sách tổng không theo danh mục, thông báo khi vượt hạn mức.
- Sửa màn của Phần 1 (Tab Home) để đọc từ Room. Phần 3 chỉ cung cấp lớp cache; Phần 1 muốn dùng thì tự nối sau.
- Mọi thứ của Phần 1 (giao dịch, danh mục, màn chụp) và Phần 2 (đăng nhập, ví, cá nhân).

---

## 3. Code đang có (đọc kỹ, đừng tạo lại)

Đây là code **thật** trong repo, đã kiểm tra ngày 23/10/2026. Nếu thấy khác thì code theo repo thật và ghi lại.

### 3.1. Backend (`backend-cap-money`)

| File | Đã có gì | Phần 3 làm gì |
|---|---|---|
| `src/app.js` | Express, mount `/api/categories`, `/api/transactions`, xử lý 404 và lỗi chung. Có comment giữ chỗ `// app.use('/api/analytics', ...)` và `// app.use('/api/budgets', ...)` | Thêm 2 dòng mount ở đó |
| `src/config/db.js` | Pool `mysql2/promise`, `decimalNumbers: true` (DECIMAL/SUM ra **số**), `dateStrings: true` | Dùng, không sửa |
| `src/middlewares/auth.middleware.js` | Kiểm tra token, gán `req.user = { id }`. Lỗi → `401` | Dùng, không sửa |
| `src/controllers/transaction.controller.js` (Phần 1) | Khoảng 1 tháng: `start = 'YYYY-MM-01 00:00:00'`, `end` = ngày 1 tháng sau, lọc `transaction_date >= start AND transaction_date < end` | **Làm giống hệt** để số liệu khớp Tab Home |
| `database_init.sql` | Bảng `budgets`, `categories`, `transactions` | **Không sửa** |
| `scripts/dev-token.js` | `npm run dev:token` in token của user 1 | Dùng để test |

Kiểu code backend đang dùng (làm **giống**):
- Controller `async function (req, res, next)`, `try/catch`, lỗi lạ `next(err)`.
- Lỗi kiểm tra dữ liệu trả thẳng: `return res.status(400).json({ success: false, message: '...', data: null });`
- Mọi câu SQL lọc `user_id = req.user.id`.

### 3.2. Android (`capmoney-app`)

| File | Đã có gì |
|---|---|
| `data/api/ApiClient.java` | `ApiClient.getClient(context)` trả `Retrofit` (singleton), **tự gắn** `Authorization: Bearer <token>` từ `SharedPrefManager`. `BASE_URL = "http://10.0.2.2:5000/api/"` |
| `model/ApiResponse.java` | `ApiResponse<T>`: `isSuccess()`, `getMessage()`, `getData()` |
| `utils/SharedPrefManager.java` | `getInstance(context)`, `saveAuthData(token, userId, userName)`, `getToken()`, `getUserId()` (chưa đăng nhập = `-1`), `isLoggedIn()`, `clear()` |
| `ui/stats/StatsFragment.java`, `ui/budget/BudgetFragment.java` | Class **rỗng** (`public class X extends Fragment {}`), chưa có layout |
| `model/Transaction.java` (Phần 1) | `getId()`, `getAmount()`, `getType()`, `getNote()`, `getImageUrl()`, `getTransactionDate()`, `getWalletId()`, `getWalletName()`, `getCategoryId()`, `getCategoryName()`, `getCategoryIcon()`, `getCategoryColor()`, `isExpense()`. **Chỉ dùng, không sửa** |
| `model/Category.java` (Phần 1) | Đọc JSON danh mục `id`, `name`, `icon`, `color_hex`, `type`, `is_system`. **Chỉ dùng, không sửa** |
| `utils/CurrencyUtils.java` (Phần 1) | `format(double)` → `"1.350.000 đ"`; `formatShort(double)` → `"50k"`, `"1,2tr"`; `parseDigits(String)` → `long`; `formatInput(long)` → `"1.500.000"`. **Chỉ dùng** |
| `utils/DateUtils.java` (Phần 1) | `monthTitle(int month, int year)` → `"Tháng 10/2026"`. **Chỉ dùng** |
| `data/local/` | **Chưa có**. Phần 3 tạo mới |

Thư viện **đã có** trong `app/build.gradle` (**không sửa** file này):
- `androidx.room:room-runtime:2.6.1` + `annotationProcessor 'androidx.room:room-compiler:2.6.1'` (Java, **không** dùng `kapt`/`ksp`).
- `com.github.PhilJay:MPAndroidChart:v3.1.0`.
- Retrofit 2.9.0 + Gson, Material 1.11.0 (có `LinearProgressIndicator`, `MaterialButtonToggleGroup`, `Chip`), ConstraintLayout. LiveData/ViewModel đi kèm `appcompat`.

**Không có** `SwipeRefreshLayout` → không dùng.

Cấu hình: `minSdk 24`, Java 11. **Không** dùng `record`, switch expression, text block, `java.time`. Dùng `Calendar`, `SimpleDateFormat`.

> **Điều kiện trước khi code Android:** `develop` của app phải đã merge Phần 1 (có `Transaction`, `Category`, `CurrencyUtils`, `DateUtils` và sửa jitpack trong `settings.gradle` để tải được MPAndroidChart). Build `develop` báo `Could not find com.github.PhilJay:MPAndroidChart` hoặc thiếu các class trên → Phần 1 chưa merge, báo Thành viên 1.

---

## 4. Ranh giới file

### 4.1. Backend: Phần 3 tạo mới

```text
src/controllers/analytics.controller.js
src/controllers/budget.controller.js
src/routes/analytics.routes.js
src/routes/budget.routes.js
src/utils/monthRange.js
```

Sửa **duy nhất** `src/app.js`: thêm `require` + `app.use` cho 2 route.

### 4.2. Android: Phần 3 tạo mới

```text
app/src/main/java/com/example/comgroupcapmoney/
├── data/api/StatsApi.java
├── data/api/BudgetApi.java
├── data/repository/StatsRepository.java
├── data/repository/BudgetRepository.java
├── data/local/AppDatabase.java
├── data/local/TransactionEntity.java
├── data/local/TransactionDao.java
├── data/local/TransactionCache.java
├── model/StatsSummary.java
├── model/CategoryStat.java
├── model/Budget.java
├── ui/stats/StatsFragment.java            (đang rỗng, viết lại)
├── ui/stats/StatsViewModel.java
├── ui/stats/CategoryStatAdapter.java
├── ui/stats/StatsCalculator.java
├── ui/budget/BudgetFragment.java          (đang rỗng, viết lại)
├── ui/budget/BudgetViewModel.java
├── ui/budget/BudgetAdapter.java
└── ui/budget/BudgetFormDialog.java

app/src/main/res/
├── layout/fragment_stats.xml, item_stats_category.xml,
│         fragment_budget.xml, item_budget.xml, dialog_budget_form.xml
├── drawable/bg_stats_*.xml, bg_budget_*.xml
├── values/strings_stats_budget.xml
└── values/colors_stats_budget.xml        (chỉ khi cần màu mới)

app/src/test/java/com/example/comgroupcapmoney/ui/stats/StatsCalculatorTest.java
```

### 4.3. Quy tắc đặt tên để không trùng Phần 2

- Mọi tên string/color/layout/drawable/id của Phần 3 **bắt đầu bằng** `stats_`, `budget_` hoặc `cache_`.
- **Không** đặt tên `common_...` (Phần 2 đã dùng) → trùng tên sẽ lỗi build khi merge.
- **Không** tạo file `RepoCallback.java` (Phần 2 đã dùng). Callback của Phần 3 khai báo **lồng trong** repository.

### 4.4. KHÔNG được sửa

`AndroidManifest.xml` (Phần 3 không cần khai báo gì), `MainActivity.java`, `activity_main.xml`, `bottom_nav_menu.xml`, `ApiClient.java`, `ApiResponse.java`, `SharedPrefManager.java`, `colors.xml`, `dimens.xml`, `strings.xml`, `themes.xml`, `app/build.gradle`, `settings.gradle`, mọi file trong `ui/home`, `ui/capture`, `ui/auth`, `ui/wallets`, `ui/profile`, các file Phần 1 tạo (`TransactionApi`, `TransactionRepository`, `Transaction`, `Category`, `CurrencyUtils`, `DateUtils`...).

---

## 5. Quy ước giao diện và kiến trúc Android (áp dụng cho mọi task AN)

- Màu có sẵn (`colors.xml`, **không sửa**): `bg_main #0E0F12`, `bg_card #1B1C20`, `bg_card_secondary #26282E`, `text_primary #FFFFFF`, `text_secondary #8E8E93`, `color_expense #FF6464` (đỏ), `color_income #4CD964` (xanh lá), `color_accent #5E7CE2`, `color_warning #F5A623` (vàng cam), `white`, `transparent`.
- Kích thước có sẵn (`dimens.xml`): `spacing_small 8dp`, `spacing_medium 16dp`, `spacing_large 24dp`, `corner_radius_small 12dp`, `corner_radius_card 20dp`, `corner_radius_pill 50dp`, `text_size_small 12sp`, `text_size_body 14sp`, `text_size_title 18sp`, `text_size_money_large 32sp`.
- Nền `@color/bg_main`, thẻ `@color/bg_card` bo `@dimen/corner_radius_card`, chữ `text_primary` / `text_secondary`. Lề màn hình `@dimen/spacing_medium`.
- Kiến trúc giống Phần 1: `Fragment` → `ViewModel` (LiveData) → `Repository` → Retrofit. Xoay màn hình **không** gọi lại API, **không** mất tháng/tab đang chọn.
- **Nút (+) của `MainActivity`** nổi ở **góc dưới bên phải** đè lên mọi tab. Phần 3 **không** đặt nút nổi ở góc dưới phải. Nội dung cuộn chừa `paddingBottom="88dp"` + `clipToPadding="false"` để dòng cuối không bị che.

### 5.1. Thanh chọn tháng (dùng ở cả 2 tab)

```text
   ‹      Tháng 10/2026      ›
```

- Chữ giữa: `DateUtils.monthTitle(month, year)`.
- Nút ‹ lùi 1 tháng (tháng 1 → tháng 12 năm trước). Nút › tiến 1 tháng, **mờ và không bấm được** khi đang ở tháng hiện tại.
- Mặc định: tháng hiện tại. Tháng đang chọn giữ trong ViewModel.
- Mỗi tab tự giữ tháng riêng.
