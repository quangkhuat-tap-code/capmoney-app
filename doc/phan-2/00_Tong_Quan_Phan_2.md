# PHẦN 2 · Tổng quan: Đăng nhập/Đăng ký + Tab Tài khoản (Ví & Chuyển tiền) + Tab Cá nhân

> **Người phụ trách:** Thành viên 2 (Auth & Wallets) · **Nhánh:** `feature/wallets-auth` (ở **cả 2 repo**) · **Ngày viết:** 23/10/2026
> **Đọc kèm:** [../Huong_Dan_Lam_Viec_Nhom.md](../Huong_Dan_Lam_Viec_Nhom.md) (quy trình Git, quy tắc commit, cách giao việc cho AI).

Bộ tài liệu này viết để **đưa thẳng cho AI** code theo. AI làm **đúng** theo tài liệu, không tự thêm tính năng. Chỗ nào chưa nói rõ thì chọn cách **đơn giản nhất** và ghi lại để báo trong PR.

---

## 1. Danh sách file và thứ tự làm

| Thứ tự | File | Repo | Nội dung |
|---|---|---|---|
| — | **00_Tong_Quan_Phan_2.md** (file này) | | Phạm vi, code đang có, ranh giới file, quy ước giao diện |
| — | [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) | | Bảng DB, JSON từng API, mã lỗi. **Hợp đồng chung** giữa Android và Backend |
| 1 | [BE-1_Api_Dang_Ky_Dang_Nhap.md](BE-1_Api_Dang_Ky_Dang_Nhap.md) | backend | `POST /api/auth/register`, `POST /api/auth/login` |
| 2 | [BE-2_Api_Thong_Tin_Ca_Nhan.md](BE-2_Api_Thong_Tin_Ca_Nhan.md) | backend | `GET /api/auth/profile` |
| 3 | [BE-3_Api_Danh_Sach_Them_Sua_Vi.md](BE-3_Api_Danh_Sach_Them_Sua_Vi.md) | backend | `GET/POST /api/wallets`, `PUT /api/wallets/:id` — **Phần 1 đang chờ** |
| 4 | [BE-4_Api_Chuyen_Tien.md](BE-4_Api_Chuyen_Tien.md) | backend | `POST /api/wallets/transfer` → **push + tạo PR backend** |
| 5 | [AN-1_Nen_Tang_Model_Api.md](AN-1_Nen_Tang_Model_Api.md) | app | Model, Retrofit, Repository, `SessionManager`, chữ hiển thị |
| 6 | [AN-2_Man_Dang_Nhap_Dang_Ky.md](AN-2_Man_Dang_Nhap_Dang_Ky.md) | app | `LoginActivity`, `RegisterActivity` |
| 7 | [AN-3_Doi_Man_Mo_Dau.md](AN-3_Doi_Man_Mo_Dau.md) | app | Đặt màn Đăng nhập làm màn mở app (sửa manifest) |
| 8 | [AN-4_Tab_Tai_Khoan.md](AN-4_Tab_Tai_Khoan.md) | app | Tab 3: tổng tài sản, danh sách ví |
| 9 | [AN-5_Them_Sua_Vi_Chuyen_Tien.md](AN-5_Them_Sua_Vi_Chuyen_Tien.md) | app | Dialog Thêm/Sửa ví, dialog Chuyển tiền |
| 10 | [AN-6_Tab_Ca_Nhan.md](AN-6_Tab_Ca_Nhan.md) | app | Tab 5: thông tin, 3 thẻ thống kê, Đăng xuất |
| 11 | [99_Nghiem_Thu_Va_PR.md](99_Nghiem_Thu_Va_PR.md) | | Test toàn bộ, tạo PR app, báo nhóm |

**Mỗi file task = 1 commit.** Giao AI **1 file task** mỗi lần, kèm file 00 và 01 (xem prompt mẫu trong Hướng dẫn, mục 6.2).

**Ưu tiên cao nhất:** BE-1 → BE-4 (đặc biệt `GET /api/wallets` ở BE-3). Màn chụp ảnh của Phần 1 đang chờ API này. Xong BE-4 thì **push và tạo PR backend ngay**, không đợi xong Android.

---

## 2. Phạm vi Phần 2

### 2.1. Làm

| Mảng | Hạng mục |
|---|---|
| Backend | `POST /api/auth/register`: tạo user, mã hoá mật khẩu bằng `bcryptjs`, **tự tạo ví "Tiền mặt" mặc định** |
| Backend | `POST /api/auth/login`: trả token JWT |
| Backend | `GET /api/auth/profile`: thông tin user + tổng số giao dịch, tổng thu, tổng chi |
| Backend | `GET /api/wallets`, `POST /api/wallets`, `PUT /api/wallets/:id`: xem, thêm, sửa ví |
| Backend | `POST /api/wallets/transfer`: chuyển tiền giữa 2 ví (DB transaction), lưu lịch sử vào `wallet_transfers` |
| Android | `LoginActivity`, `RegisterActivity`: đăng nhập, đăng ký, tự đăng nhập nếu đã có token |
| Android | Tab 3 `WalletsFragment`: thẻ Tổng tài sản, danh sách Ví thanh toán + Sổ tiết kiệm, Thêm ví, Sửa ví, Chuyển tiền |
| Android | Tab 5 `ProfileFragment`: avatar chữ cái, họ tên, email, 3 thẻ thống kê, Đăng xuất |

### 2.2. Không làm

- Xoá ví, quên mật khẩu, đổi mật khẩu, đổi avatar bằng ảnh, màn xem lịch sử chuyển tiền.
- Mọi thứ của Phần 1 (giao dịch, danh mục, Tab Home, màn chụp) và Phần 3 (thống kê, ngân sách, Room).

---

## 3. Code đang có (đọc kỹ, đừng tạo lại)

Đây là code **thật** trong repo, đã kiểm tra ngày 23/10/2026. Nếu thấy khác thì code theo repo thật và ghi lại.

### 3.1. Backend (`backend-cap-money`)

| File | Đã có gì | Phần 2 làm gì |
|---|---|---|
| `src/app.js` | Express, `cors`, `express.json()`, phục vụ `/uploads`, mount `/api/categories` và `/api/transactions`, xử lý 404 và lỗi chung | Thêm dòng mount `/api/auth`, `/api/wallets` ở chỗ comment giữ chỗ |
| `src/config/db.js` | Pool `mysql2/promise`, `decimalNumbers: true` (DECIMAL ra **số**), `dateStrings: true` (DATETIME/TIMESTAMP ra chuỗi `yyyy-MM-dd HH:mm:ss`) | Dùng, không sửa |
| `src/middlewares/auth.middleware.js` | Đọc `Authorization: Bearer <token>`, `jwt.verify` bằng `process.env.JWT_SECRET`, gán `req.user = { id: payload.id }`. Lỗi → `401` | Dùng, không sửa |
| `src/utils/AppError.js` | `new AppError(status, message)` | Có thể dùng |
| `package.json` | Đã có `bcryptjs`, `jsonwebtoken`, `express`, `mysql2`, `cors`, `dotenv` | **Không cần cài thêm gì** |
| `.env` | `JWT_SECRET`, `DB_*`, `PORT=5000` | Dùng `process.env.JWT_SECRET` |
| `database_init.sql` | Đủ 6 bảng | **Không sửa** |
| `database_dev_seed.sql` | User id 1 (`dev@example.test`, mật khẩu **không đăng nhập được**: `password_hash = 'not-a-real-hash'`), ví 1 "Tiền mặt" 2.000.000 (`is_default=1`), ví 2 "Ngân hàng" 5.000.000 | Chỉ để test |
| `scripts/dev-token.js` | `npm run dev:token` in ra token của user 1 | Dùng để test API ví khi chưa có đăng nhập |

Kiểu code backend đang dùng (làm **giống** để đồng bộ):
- Controller là `async function (req, res, next)`, bọc `try/catch`, lỗi lạ thì `next(err)`.
- Lỗi kiểm tra dữ liệu trả thẳng: `return res.status(400).json({ success: false, message: '...', data: null });`
- Thao tác nhiều bảng: `const conn = await pool.getConnection(); await conn.beginTransaction(); ... await conn.commit();` trong `try`, `catch` thì `await conn.rollback()`, `finally` thì `conn.release()`.
- Khoá dòng khi đọc để cập nhật: `SELECT ... FOR UPDATE`.
- Mọi câu SQL lọc `user_id = req.user.id`.

### 3.2. Android (`capmoney-app`)

| File | Đã có gì |
|---|---|
| `data/api/ApiClient.java` | `ApiClient.getClient(context)` trả `Retrofit` (singleton). Interceptor **tự gắn** `Authorization: Bearer <token>` lấy từ `SharedPrefManager.getToken()` ở **mỗi request**. `BASE_URL = "http://10.0.2.2:5000/api/"` |
| `model/ApiResponse.java` | `ApiResponse<T>`: `isSuccess()`, `getMessage()`, `getData()` |
| `utils/SharedPrefManager.java` | `getInstance(context)`, `saveAuthData(String token, int userId, String userName)`, `getToken()`, `getUserId()`, `getUserName()`, `isLoggedIn()`, `clear()`. **Không có** `saveToken()`, `saveUser()` như tài liệu gốc ghi |
| `ui/main/MainActivity.java` | 5 tab + nút (+) mở màn chụp. Đang là màn **LAUNCHER** trong manifest |
| `ui/wallets/WalletsFragment.java`, `ui/profile/ProfileFragment.java` | Class **rỗng** (`public class X extends Fragment {}`), chưa có layout |
| `utils/CurrencyUtils.java` (Phần 1) | `format(double)` → `"50.000 đ"`; `formatSigned(double, boolean isExpense)` → `"-50.000 đ"`/`"+50.000 đ"`; `parseDigits(String)` → `long` (bỏ ký tự không phải số, tối đa `MAX_ALLOWED_AMOUNT = 999_999_999_999L`); `formatInput(long)` → `"1.500.000"` (không có " đ") |
| `utils/DateUtils.java` (Phần 1) | `toDisplay("2026-10-22 12:30:00")` → `"22/10/2026 12:30"` |
| `ui/capture/WalletOption.java` (Phần 1) | DTO đọc `GET /api/wallets` của Phần 1. **Không sửa, không dùng** — Phần 2 tạo `model/Wallet.java` riêng |
| `ui/auth/` | **Chưa có**. Phần 2 tạo mới |
| `model/User.java`, `model/Wallet.java` | **Chưa có**. Phần 2 tạo mới |

Thư viện đã có trong `app/build.gradle` (**không sửa** file này): Retrofit 2.9.0 + Gson, OkHttp logging, Glide, Material 1.11.0, ConstraintLayout, AppCompat, Room, MPAndroidChart.
**Không có** `SwipeRefreshLayout`, `CircleImageView`, CameraX → không dùng.

Cấu hình: `minSdk 24`, Java 11. **Không** dùng `record`, switch expression, text block, `java.time`. Dùng `Calendar`, `SimpleDateFormat`.

> **Điều kiện trước khi code Android:** `develop` của app phải đã merge Phần 1 (có `CurrencyUtils`, `DateUtils` và sửa jitpack trong `settings.gradle`). Nếu build `develop` báo `Could not find com.github.PhilJay:MPAndroidChart` hoặc không thấy `CurrencyUtils` → Phần 1 chưa merge, báo Thành viên 1.

---

## 4. Ranh giới file

### 4.1. Backend: Phần 2 tạo mới

```text
src/controllers/auth.controller.js
src/controllers/wallet.controller.js
src/routes/auth.routes.js
src/routes/wallet.routes.js
```

Sửa **duy nhất** `src/app.js`: thêm `require` + `app.use` cho 2 route. Không sửa file nào khác.

### 4.2. Android: Phần 2 tạo mới

```text
app/src/main/java/com/example/comgroupcapmoney/
├── data/api/AuthApi.java
├── data/api/WalletApi.java
├── data/repository/AuthRepository.java
├── data/repository/WalletRepository.java
├── data/repository/RepoCallback.java
├── model/User.java
├── model/AuthResult.java
├── model/UserProfile.java
├── model/Wallet.java
├── model/TransferResult.java
├── ui/auth/LoginActivity.java
├── ui/auth/RegisterActivity.java
├── ui/auth/AuthViewModel.java
├── ui/auth/SessionManager.java
├── ui/wallets/WalletsFragment.java             (đang rỗng, viết lại)
├── ui/wallets/WalletsViewModel.java
├── ui/wallets/WalletAdapter.java
├── ui/wallets/WalletFormDialog.java
├── ui/wallets/TransferMoneyDialog.java
├── ui/profile/ProfileFragment.java             (đang rỗng, viết lại)
└── ui/profile/ProfileViewModel.java

app/src/main/res/
├── layout/activity_login.xml, activity_register.xml,
│         fragment_wallets.xml, item_wallet.xml, dialog_wallet_form.xml, dialog_transfer_money.xml,
│         fragment_profile.xml
├── drawable/bg_auth_*.xml, bg_wallet_*.xml, bg_profile_*.xml
├── values/strings_auth_wallet_profile.xml
└── values/colors_auth_wallet_profile.xml      (chỉ khi cần màu mới)
```

### 4.3. Android: file dùng chung được sửa (commit riêng)

| File | Sửa gì | Ở task |
|---|---|---|
| `AndroidManifest.xml` | Khai báo `LoginActivity`, `RegisterActivity` | AN-2 |
| `AndroidManifest.xml` | Chuyển LAUNCHER từ `MainActivity` sang `LoginActivity` | AN-3 |

### 4.4. KHÔNG được sửa

`MainActivity.java`, `activity_main.xml`, `bottom_nav_menu.xml`, `ApiClient.java`, `ApiResponse.java`, `SharedPrefManager.java`, `colors.xml`, `dimens.xml`, `strings.xml`, `themes.xml`, `app/build.gradle`, `settings.gradle`, mọi file trong `ui/home`, `ui/capture`, `ui/stats`, `ui/budget`, `data/local`, các file Phần 1 tạo (`TransactionApi`, `TransactionRepository`, `Transaction`, `Category`, `CurrencyUtils`, `DateUtils`, `ImageUtils`, `SingleEvent`...).

---

## 5. Quy ước giao diện và kiến trúc Android (áp dụng cho mọi task AN)

- Màu có sẵn (`colors.xml`, **không sửa**): `bg_main #0E0F12`, `bg_card #1B1C20`, `bg_card_secondary #26282E`, `text_primary #FFFFFF`, `text_secondary #8E8E93`, `color_expense #FF6464`, `color_income #4CD964`, `color_accent #5E7CE2`, `color_warning #F5A623`, `white`, `black`, `transparent`.
- Kích thước có sẵn (`dimens.xml`): `spacing_tiny 4dp`, `spacing_small 8dp`, `spacing_medium 16dp`, `spacing_large 24dp`, `corner_radius_small 12dp`, `corner_radius_card 20dp`, `corner_radius_pill 50dp`, `text_size_small 12sp`, `text_size_body 14sp`, `text_size_title 18sp`, `text_size_money_large 32sp`.
- Nền màn `@color/bg_main`. Thẻ `@color/bg_card` bo `@dimen/corner_radius_card`. Chữ `text_primary` / `text_secondary`.
- Nút chính: nền `color_accent`, bo `corner_radius_pill`, cao 52dp.
- Ô nhập: nền `bg_card_secondary`, bo `corner_radius_small`, cao 52dp, padding ngang 16dp.
- Lề màn hình `@dimen/spacing_medium`.
- Kiến trúc giống Phần 1: `Activity/Fragment` → `ViewModel` (LiveData) → `Repository` → Retrofit. ViewModel giữ trạng thái `loading`, dữ liệu, sự kiện lỗi; xoay màn hình **không** gọi lại API, **không** mất chữ đã nhập.
- Tên layout/drawable/string/id bắt đầu bằng `auth_`, `wallet_`, `transfer_`, `profile_` hoặc `common_` (tiền tố `common_` dành riêng cho Phần 2).
