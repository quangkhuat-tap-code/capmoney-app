# TÀI LIỆU ĐẶC TẢ KỸ THUẬT, KIẾN TRÚC & QUY CHUẨN AI VIBE CODING

**DỰ ÁN: CAPMONEY ANDROID & NODE.JS BACKEND**
Ứng dụng Quản lý Chi tiêu bằng Hình ảnh (Locket-style Expense Tracker)

- **Package chuẩn:** `com.example.comgroupcapmoney`
- **Nhánh làm việc:** `develop`

> 💡 **HƯỚNG DẪN SỬ DỤNG TÀI LIỆU (CHO THÀNH VIÊN NHÓM & AI VIBE CODING)**
>
> 1. **Dành cho thành viên:** Đọc kỹ mục [Phân công nhiệm vụ (Mục 5)](#5-phân-công-nhiệm-vụ-chi-tiết-cho-3-thành-viên), [quy trình Git (Mục 6)](#6-quy-chuẩn-giao-tiếp-api--quy-trình-làm-việc-git) và chỉ sửa các file thuộc package của mình.
> 2. **Dành cho AI (ChatGPT / Claude / Gemini / Cursor):** Khi nhờ AI viết code, hãy copy nguyên khối **Master Prompt** ở [Mục 7](#7-bộ-prompt-mẫu-để-vibe-code-với-ai-copy-paste-trực-tiếp) kèm theo file này để AI sinh code đúng 100% package `com.example.comgroupcapmoney`, đúng tên màu sắc, kích thước và cấu trúc API đã khóa.

---

## Mục lục

1. [Tổng quan công nghệ & thư viện](#1-tổng-quan-công-nghệ--thư-viện-tech-stack)
2. [Thiết kế cơ sở dữ liệu](#2-thiết-kế-cơ-sở-dữ-liệu-chuẩn-hóa-6-bảng-mysql)
3. [Cấu trúc thư mục dự án](#3-cấu-trúc-thư-mục-dự-án-đã-khớp-base-code-trên-github)
4. [Design System & các file base](#4-hệ-thống-design-system--các-file-base-đã-có-sẵn)
5. [Phân công nhiệm vụ](#5-phân-công-nhiệm-vụ-chi-tiết-cho-3-thành-viên)
6. [Quy chuẩn API & Git](#6-quy-chuẩn-giao-tiếp-api--quy-trình-làm-việc-git)
7. [Prompt mẫu cho AI](#7-bộ-prompt-mẫu-để-vibe-code-với-ai-copy-paste-trực-tiếp)

---

## 1. TỔNG QUAN CÔNG NGHỆ & THƯ VIỆN (TECH STACK)

### 1.1. Mobile App (Android Native - Java)

- **Môi trường & Ngôn ngữ:** Android Studio, Java (JDK 17), MinSDK 24+, TargetSDK 34.
- **Root Package thực tế trên GitHub:** `com.example.comgroupcapmoney` (⚠️ Tuyệt đối **không** dùng `com.group.capmoney`).
- **Kiến trúc phần mềm:** MVVM (Model - View - ViewModel) kết hợp Repository Pattern.
- **Thư viện UI & Hiển thị:**
  - **Material Components:** `BottomNavigationView` (5 Tab), `FloatingActionButton`, `CardView`, `BottomSheetDialog`.
  - **Glide (v4.16.0):** Load ảnh bất đồng bộ, bo góc ảnh (`RoundedCorners`), quản lý cache ảnh cục bộ/mạng tránh lỗi tràn bộ nhớ (OutOfMemory).
  - **CameraX / ActivityResultContracts:** Chụp ảnh món ăn/hóa đơn trực tiếp hoặc chọn từ Gallery, nén ảnh lưu tạm vào cache.
  - **MPAndroidChart (v3.1.0):** Vẽ biểu đồ `PieChart` dạng Donut rỗng ruột (hiển thị tổng tiền ở tâm biểu đồ).
- **Thư viện Dữ liệu & Mạng:**
  - **Retrofit 2 + Gson Converter + OkHttp3 Logging Interceptor:** Gọi REST API tới Node.js, tự động đính kèm Header `Authorization: Bearer <token>` thông qua `ApiClient`.
  - **Room Database (SQLite):** Lưu cache danh sách giao dịch và ví tiền để xem offline khi mất kết nối mạng.
  - **SharedPreferences (`SharedPrefManager`):** Lưu trữ JWT Token, User ID, thông tin đăng nhập.

### 1.2. Backend Server (Node.js & MySQL)

- **Framework:** Node.js + Express.js.
- **Cơ sở dữ liệu:** MySQL (chạy qua XAMPP / MySQL Workbench) sử dụng thư viện `mysql2/promise` hỗ trợ Connection Pool & Database Transaction.
- **Xử lý Upload ảnh:** Thư viện `multer` nhận dữ liệu `multipart/form-data`, lưu file vật lý vào `public/uploads/` và trả về đường dẫn tĩnh.
- **Bảo mật & Xác thực:** `jsonwebtoken` (cấp và kiểm tra JWT Token), `bcryptjs` (mã hóa mật khẩu), `cors`, `dotenv`.

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA (6 BẢNG MYSQL)

Hệ thống sử dụng 6 bảng chính phục vụ đầy đủ 5 Tab trên ứng dụng: Tài khoản (`users`), Sổ ví (`wallets`), Danh mục thu/chi (`categories`), Giao dịch kèm ảnh (`transactions`), Lịch sử chuyển tiền giữa các ví (`wallet_transfers`) và Hạn mức ngân sách (`budgets`).

### 2.1. Sơ đồ quan hệ thực thể (ERD)

```text
+------------------+       +------------------+       +------------------+
|      users       | 1   * |     wallets      | 1   * |    categories    |
+------------------+-------+------------------+-------+------------------+
| id (PK, INT)     |       | id (PK, INT)     |       | id (PK, INT)     |
| full_name        |       | user_id (FK)     |       | user_id (FK/null)|
| email (UNIQUE)   |       | name             |       | name             |
| password_hash    |       | balance (DECIMAL)|       | icon (VARCHAR)   |
| avatar_url       |       | type (cash/bank/ |       | color_hex        |
| created_at       |       |       saving)    |       | type (expense/   |
+------------------+       | is_default (BOOL)|       |       income)    |
        | 1                +------------------+       +------------------+
        |                          | 1                        | 1
        | *                        | *                        | *
+------------------+       +------------------+---------------+
|     budgets      |       |   transactions   |
+------------------+       +------------------+
| id (PK, INT)     |       | id (PK, INT)     |
| user_id (FK)     |       | user_id (FK)     |
| category_id (FK) |       | wallet_id (FK)   |
| limit_amount     |       | category_id (FK) |
| month (INT)      |       | amount (DECIMAL) |
| year (INT)       |       | note (TEXT)      |
| created_at       |       | image_url        |
+------------------+       | type (expense/   |
                           |       income)    |
                           | transaction_date |
                           +------------------+
                                   |
                                   | (Ví nguồn / Ví đích)
                                   v
                           +------------------+
                           | wallet_transfers |
                           +------------------+
                           | id (PK, INT)     |
                           | user_id (FK)     |
                           | from_wallet_id   |
                           | to_wallet_id     |
                           | amount (DECIMAL) |
                           | note (VARCHAR)   |
                           | transfer_date    |
                           +------------------+
```

### 2.2. Mô tả các bảng

| Tên Bảng | Các cột chính (Columns) | Mô tả & Ràng buộc Logic |
|---|---|---|
| 1. `users` | `id`, `full_name`, `email`, `password_hash`, `avatar_url`, `created_at` | Lưu thông tin người dùng. Khi đăng ký mới, Backend tự động tạo sẵn 1 ví **'Tiền mặt'** mặc định. |
| 2. `wallets` | `id`, `user_id`, `name`, `balance`, `type` (`'cash'`, `'bank'`, `'saving'`), `is_default` | Quản lý danh sách ví tiền mặt, tài khoản ngân hàng và sổ tiết kiệm (Tab 3). |
| 3. `categories` | `id`, `user_id` (`NULL` = mặc định), `name`, `icon`, `color_hex`, `type` (`'expense'`, `'income'`) | Danh mục thu chi (Ăn uống, Di chuyển, Mua sắm, Lương...). Có sẵn danh mục hệ thống. |
| 4. `transactions` | `id`, `user_id`, `wallet_id`, `category_id`, `amount`, `note`, `image_url`, `type`, `transaction_date` | Bảng lõi lưu khoản thu/chi kèm đường dẫn ảnh chụp. Khi thêm giao dịch phải **tự động cộng/trừ `balance`** của bảng `wallets`. |
| 5. `wallet_transfers` | `id`, `user_id`, `from_wallet_id`, `to_wallet_id`, `amount`, `note`, `transfer_date` | Lưu lịch sử chuyển tiền qua lại giữa các ví (Tab 3). Thực thi trong **SQL Transaction** (trừ ví nguồn, cộng ví đích). |
| 6. `budgets` | `id`, `user_id`, `category_id`, `limit_amount`, `month`, `year`, `created_at` | Lưu hạn mức ngân sách theo danh mục từng tháng (Tab 4). Dùng để so sánh với tổng chi thực tế và cảnh báo đỏ. |

---

## 3. CẤU TRÚC THƯ MỤC DỰ ÁN (ĐÃ KHỚP BASE CODE TRÊN GITHUB)

### 3.1. Cấu trúc Android Java (Repo: `capmoney-app`, nhánh `develop`)

```text
capmoney-app/
├── app/
│   ├── build.gradle                         # Đã cài sẵn Retrofit, Gson, Glide, Room, CameraX, MPAndroidChart
│   └── src/main/
│       ├── AndroidManifest.xml              # Khai báo quyền INTERNET, CAMERA, usesCleartextTraffic="true"
│       ├── java/com/example/comgroupcapmoney/
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   ├── ApiClient.java       # [ĐÃ CÓ SẴN] Singleton Retrofit + JWT Auth Interceptor
│       │   │   │   └── ApiService.java      # Interface khai báo các endpoint @GET, @POST, @Multipart
│       │   │   ├── local/                   # [Người 3 phụ trách]
│       │   │   │   ├── AppDatabase.java     # Khởi tạo Room Database
│       │   │   │   ├── dao/                 # TransactionDao.java, WalletDao.java
│       │   │   │   └── entity/              # TransactionEntity.java, WalletEntity.java
│       │   │   └── repository/              # Các lớp trung gian gọi API & Room
│       │   ├── model/
│       │   │   ├── ApiResponse.java         # [ĐÃ CÓ SẴN] Generic wrapper ApiResponse<T>
│       │   │   ├── User.java                # POJO người dùng
│       │   │   ├── Wallet.java              # POJO ví tiền
│       │   │   ├── Category.java            # POJO danh mục
│       │   │   ├── Transaction.java         # POJO giao dịch chi tiêu + ảnh
│       │   │   └── Budget.java              # POJO ngân sách
│       │   ├── ui/
│       │   │   ├── main/
│       │   │   │   └── MainActivity.java    # [ĐÃ CÓ SẴN] Quản lý BottomNavigationView 5 Tab + Nút FAB (+)
│       │   │   ├── auth/                    # [Người 2] LoginActivity.java, RegisterActivity.java
│       │   │   ├── capture/                 # [Người 1] Luồng chụp ảnh & lưu chi tiêu
│       │   │   │   ├── CaptureExpenseActivity.java   # [ĐÃ CÓ KHUNG] Màn hình chụp/nhập tiền
│       │   │   │   └── SelectCategoryBottomSheet.java
│       │   │   ├── home/                    # [Người 1] TAB 1: Lưới ảnh (Photo Grid) & Lịch (Calendar)
│       │   │   │   ├── HomeFragment.java             # [ĐÃ CÓ KHUNG]
│       │   │   │   ├── PhotoGridAdapter.java
│       │   │   │   └── TransactionDetailDialog.java
│       │   │   ├── stats/                   # [Người 3] TAB 2: Thống kê Donut Chart
│       │   │   │   ├── StatsFragment.java            # [ĐÃ CÓ KHUNG]
│       │   │   │   └── CategoryStatAdapter.java
│       │   │   ├── wallets/                 # [Người 2] TAB 3: Quản lý Ví & Chuyển tiền
│       │   │   │   ├── WalletsFragment.java          # [ĐÃ CÓ KHUNG]
│       │   │   │   ├── WalletAdapter.java
│       │   │   │   └── TransferMoneyDialog.java
│       │   │   ├── budget/                  # [Người 3] TAB 4: Quản lý Ngân sách
│       │   │   │   ├── BudgetFragment.java           # [ĐÃ CÓ KHUNG]
│       │   │   │   ├── BudgetAdapter.java
│       │   │   │   └── AddBudgetDialog.java
│       │   │   └── profile/                 # [Người 2] TAB 5: Hồ sơ cá nhân & Tổng quan thu/chi
│       │   │       └── ProfileFragment.java          # [ĐÃ CÓ KHUNG]
│       │   └── utils/
│       │       ├── SharedPrefManager.java   # [ĐÃ CÓ SẴN] Lưu/đọc JWT Token, User info
│       │       ├── CurrencyUtils.java       # Format tiền tệ VND (vd: 50.000 đ)
│       │       └── ImageUtils.java          # Nén Bitmap/File trước khi upload Multipart
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml        # [ĐÃ CÓ SẴN] Khung FragmentContainer + BottomNav + FAB
│           ├── menu/
│           │   └── bottom_nav_menu.xml      # [ĐÃ CÓ SẴN] 5 ID: nav_home, nav_stats, nav_wallets, nav_budget, nav_profile
│           └── values/
│               ├── colors.xml               # [ĐÃ CÓ SẴN] Hệ màu Dark Mode chuẩn
│               └── dimens.xml               # [ĐÃ CÓ SẴN] Kích thước chuẩn
```

### 3.2. Cấu trúc Backend Node.js (Thư mục: `capmoney-backend`)

```text
capmoney-backend/
├── src/
│   ├── config/
│   │   └── db.js                        # Cấu hình mysql2/promise createPool()
│   ├── middlewares/
│   │   ├── auth.middleware.js           # Kiểm tra JWT Bearer Token, gắn req.user = { id }
│   │   └── upload.middleware.js         # Cấu hình Multer lưu ảnh vào public/uploads/
│   ├── controllers/
│   │   ├── auth.controller.js           # [Người 2] Đăng ký (tạo ví mặc định), Đăng nhập, Lấy Profile
│   │   ├── wallet.controller.js         # [Người 2] CRUD Ví, Chuyển tiền giữa 2 ví (SQL Transaction)
│   │   ├── transaction.controller.js    # [Người 1] Upload ảnh + Tạo khoản thu/chi, Lấy lưới ảnh, Xóa/Sửa
│   │   ├── analytics.controller.js      # [Người 3] Thống kê tổng thu/chi, gom nhóm theo danh mục (Donut Chart)
│   │   └── budget.controller.js         # [Người 3] CRUD Ngân sách tháng + tính số tiền đã tiêu
│   ├── routes/
│   │   ├── auth.routes.js               # /api/auth/*
│   │   ├── wallet.routes.js             # /api/wallets/*
│   │   ├── transaction.routes.js        # /api/transactions/*
│   │   ├── analytics.routes.js          # /api/analytics/*
│   │   └── budget.routes.js             # /api/budgets/*
│   └── app.js                           # Khởi tạo Express, CORS, static folder '/uploads', mount routes
├── public/
│   └── uploads/                         # Thư mục chứa ảnh hóa đơn/món ăn upload lên
├── database_init.sql                    # File SQL tạo 6 bảng và dữ liệu danh mục mẫu
├── .env                                 # PORT=5000, DB_HOST, DB_USER, DB_PASS, DB_NAME, JWT_SECRET
└── package.json
```

---

## 4. HỆ THỐNG DESIGN SYSTEM & CÁC FILE BASE ĐÃ CÓ SẴN

Để giao diện của 3 thành viên đồng nhất 100% theo phong cách Dark Mode hiện đại (giống Locket / CapMoney), dự án Base đã định nghĩa sẵn các tài nguyên dưới đây. Khi code XML hoặc khi nhờ AI sinh layout, **BẮT BUỘC** sử dụng đúng tên biến này.

### 4.1. Bảng màu chuẩn (`res/values/colors.xml`)

```xml
<!-- Nền tối chủ đạo (Dark Theme) -->
<color name="bg_primary_dark">#121212</color>         <!-- Nền chính của mọi màn hình -->
<color name="bg_surface_card">#1E1E1E</color>         <!-- Nền của các khối CardView, BottomNav -->
<color name="bg_elevated_input">#2C2C2E</color>       <!-- Nền ô nhập liệu EditText, Nút phụ, Chip -->

<!-- Màu chữ (Typography) -->
<color name="text_primary_white">#FFFFFF</color>      <!-- Chữ tiêu đề, số tiền chính -->
<color name="text_secondary_gray">#A0A0A5</color>     <!-- Chữ mô tả, ngày tháng, tiêu đề phụ -->
<color name="text_hint_gray">#636366</color>          <!-- Chữ gợi ý trong ô nhập -->

<!-- Màu nhấn & Trạng thái tài chính (Semantic Colors) -->
<color name="accent_blue">#0A84FF</color>             <!-- Nút bấm chính, Tab đang chọn -->
<color name="expense_red">#FF453A</color>             <!-- Màu khoản CHI TIÊU, cảnh báo vượt ngân sách -->
<color name="income_green">#32D74B</color>            <!-- Màu khoản THU NHẬP, hoàn thành tốt -->
<color name="warning_yellow">#FFD60A</color>          <!-- Màu cảnh báo sắp chạm trần ngân sách -->
<color name="overlay_dark_60">#99000000</color>       <!-- Lớp phủ mờ đè lên ảnh để hiện số tiền -->
```

### 4.2. Kích thước chuẩn (`res/values/dimens.xml`)

```xml
<!-- Khoảng cách lề Spacing / Padding -->
<dimen name="spacing_tiny">4dp</dimen>
<dimen name="spacing_small">8dp</dimen>
<dimen name="spacing_medium">16dp</dimen>
<dimen name="spacing_large">24dp</dimen>

<!-- Độ bo góc Card & Nút -->
<dimen name="corner_radius_small">12dp</dimen>
<dimen name="corner_radius_card">20dp</dimen>
<dimen name="corner_radius_pill">50dp</dimen>

<!-- Cỡ chữ Font size -->
<dimen name="text_size_small">12sp</dimen>
<dimen name="text_size_body">14sp</dimen>
<dimen name="text_size_title">18sp</dimen>
<dimen name="text_size_money_large">32sp</dimen>
```

### 4.3. Các Class tiện ích dùng chung đã có sẵn trong Base

1. **`ApiClient.getClient(context)`** (`com.example.comgroupcapmoney.data.api.ApiClient`): Đã tích hợp sẵn OkHttp Interceptor tự động lấy JWT Token từ `SharedPrefManager` gắn vào Header `Authorization: Bearer <token>`. Mọi thành viên chỉ cần gọi `ApiClient.getClient(context).create(ApiService.class)` để sử dụng.
2. **`ApiResponse<T>`** (`com.example.comgroupcapmoney.model.ApiResponse`): Class bọc kết quả trả về từ Server gồm 3 trường: `boolean isSuccess()`, `String getMessage()`, và `T getData()`.
3. **`SharedPrefManager`** (`com.example.comgroupcapmoney.utils.SharedPrefManager`): Gọi `SharedPrefManager.getInstance(context)` với các hàm có sẵn: `saveToken(String)`, `getToken()`, `saveUser(id, name, email)`, `isLoggedIn()`, `clear()` (dùng khi đăng xuất).
4. **`MainActivity`** (`com.example.comgroupcapmoney.ui.main.MainActivity`): Đã nối sẵn 5 Tab (`nav_home`, `nav_stats`, `nav_wallets`, `nav_budget`, `nav_profile`) vào 5 Fragment tương ứng và nút tròn ở giữa (`fab_capture_expense`) mở màn hình `CaptureExpenseActivity`.

---

## 5. PHÂN CÔNG NHIỆM VỤ CHI TIẾT CHO 3 THÀNH VIÊN

**Nguyên tắc phân chia:** Mỗi thành viên phụ trách trọn vẹn từ Giao diện Android (Java + XML) đến API Backend (Node.js + SQL) của tính năng đó. Điều này giúp 3 người có khối lượng điểm Frontend + Backend ngang nhau khi bảo vệ đồ án và **KHÔNG BỊ XUNG ĐỘT FILE** khi merge Git.

### 👤 Thành viên 1: Luồng chụp ảnh ghi chi tiêu + Tab 1 (Home Grid & Lịch) + Transactions API

- **Nhánh Git làm việc:** `feature/capture-home`
- **Package Android được phép sửa:** `ui/capture/*`, `ui/home/*`, `model/Transaction.java`, `model/Category.java`

**Công việc Android (Java):**

- **Màn hình `CaptureExpenseActivity`:** Mở CameraX hoặc chọn ảnh từ thư viện → Hiển thị ảnh preview trong khung vuông bo góc lớn (20dp) → Nút chuyển đổi trạng thái Chi tiêu (Đỏ) / Thu nhập (Xanh) → Ô nhập số tiền cỡ lớn (32sp) đè trên nền mờ → Nút mở BottomSheet chọn Ví & Danh mục → Ô nhập ghi chú → Bấm Lưu để nén ảnh và gửi Multipart lên Server.
- **Màn hình Tab 1 `HomeFragment`:** Có nút gạt chuyển đổi giữa 2 chế độ xem:
  1. Lưới ảnh 3 cột (`PhotoGridAdapter` dùng Glide bo góc, hiển thị ảnh món đồ kèm nhãn số tiền ở góc dưới giống Locket);
  2. Lịch tháng (hiển thị các ngày trong tháng có thumbnail ảnh chi tiêu).

  Bấm vào 1 ảnh bất kỳ sẽ mở Dialog xem chi tiết hóa đơn & nút Xóa.

**Công việc Backend (Node.js):**

- **Middleware `upload.middleware.js`:** Cấu hình `multer` lưu file ảnh vào `public/uploads/`, đổi tên file theo timestamp, giới hạn dung lượng 5MB.
- **API `POST /api/transactions`:** Nhận `multipart/form-data` gồm `image` (file), `amount`, `wallet_id`, `category_id`, `type` (`'expense'`/`'income'`), `note`, `transaction_date`. Lưu bản ghi vào bảng `transactions` đồng thời cập nhật cộng/trừ `balance` trong bảng `wallets`.
- **API `GET /api/transactions`:** Hỗ trợ filter theo `?month=&year=&wallet_id=`, trả về danh sách giao dịch kèm tên danh mục, icon, tên ví và link ảnh đầy đủ.
- **API `DELETE /api/transactions/:id`:** Xóa giao dịch và hoàn lại số dư vào ví tương ứng.

### 👤 Thành viên 2: Xác thực (Auth) + Tab 3 (Quản lý ví & chuyển tiền) + Tab 5 (Profile)

- **Nhánh Git làm việc:** `feature/wallets-auth`
- **Package Android được phép sửa:** `ui/auth/*`, `ui/wallets/*`, `ui/profile/*`, `model/User.java`, `model/Wallet.java`

**Công việc Android (Java):**

- **Màn hình `LoginActivity` & `RegisterActivity`:** Form đăng nhập/đăng ký Dark Mode. Khi đăng nhập thành công, lưu JWT Token và thông tin User vào `SharedPrefManager`, sau đó chuyển vào `MainActivity`. Kiểm tra tự động đăng nhập nếu đã có Token.
- **Màn hình Tab 3 `WalletsFragment`:** Hiển thị thẻ Tổng tài sản nổi bật ở trên cùng. Bên dưới chia danh sách các Ví thanh toán (Tiền mặt, Ngân hàng) và Sổ tiết kiệm. Có nút **'+ Thêm ví mới'** và nút **'Chuyển tiền giữa các ví'** (`TransferMoneyDialog` cho phép chọn Ví nguồn, Ví đích, nhập số tiền cần chuyển).
- **Màn hình Tab 5 `ProfileFragment`:** Hiển thị Avatar, Họ tên, Email người dùng; 3 thẻ thống kê nhanh: Tổng số giao dịch đã chụp, Tổng thu, Tổng chi; Nút Đăng xuất (xóa `SharedPrefManager` và quay về `LoginActivity`).

**Công việc Backend (Node.js):**

- **API `POST /api/auth/register` & `POST /api/auth/login`:** Mã hóa mật khẩu bằng `bcryptjs`, cấp JWT Token. **Đặc biệt:** Ngay khi đăng ký tài khoản thành công, tự động `INSERT` 1 ví mặc định (`'Tiền mặt'`, `balance = 0`) vào bảng `wallets` cho user đó.
- **API `GET /api/auth/profile`:** Trả về thông tin user kèm tổng số lượng giao dịch, tổng thu, tổng chi.
- **API Quản lý Ví** (`GET /api/wallets`, `POST /api/wallets`, `PUT /api/wallets/:id`): Lấy danh sách ví của user, tạo ví mới, sửa tên/số dư ví.
- **API Chuyển tiền** (`POST /api/wallets/transfer`): Nhận `from_wallet_id`, `to_wallet_id`, `amount`, `note`. Sử dụng Database Transaction (`connection.beginTransaction()`) để trừ tiền ví nguồn, cộng tiền ví đích và lưu lịch sử vào bảng `wallet_transfers`.

### 👤 Thành viên 3: Tab 2 (Thống kê Donut Chart) + Tab 4 (Ngân sách) + Room Offline Cache

- **Nhánh Git làm việc:** `feature/stats-budget`
- **Package Android được phép sửa:** `ui/stats/*`, `ui/budget/*`, `data/local/*`, `model/Budget.java`

**Công việc Android (Java):**

- **Màn hình Tab 2 `StatsFragment`:** Tích hợp thư viện `MPAndroidChart` vẽ biểu đồ tròn rỗng ruột (`PieChart` với `setDrawHoleEnabled(true)`, màu nền lỗ trùng màu nền tối `#121212`, ở tâm biểu đồ hiển thị Tổng chi tiêu). Phía trên có bộ lọc chọn Tháng/Năm và tab chuyển đổi Chi tiêu / Thu nhập. Phía dưới là `RecyclerView` hiển thị danh sách từng danh mục kèm thanh phần trăm (%) và số tiền.
- **Màn hình Tab 4 `BudgetFragment`:** Hiển thị danh sách hạn mức ngân sách của từng danh mục trong tháng (ví dụ: *Ăn uống: Đã tiêu 1.800.000đ / 2.000.000đ*). Hiển thị thanh `ProgressBar` đổi màu tự động:
  - 🟢 Xanh lá: `< 80%`
  - 🟡 Vàng cảnh báo: `80% – 99%`
  - 🔴 Đỏ báo động: `>= 100%`

  Có nút **'+'** mở `AddBudgetDialog` để đặt hạn mức mới.
- **Cài đặt Room Database (`data/local/*`):** Tạo `AppDatabase`, `TransactionEntity`, `TransactionDao` để lưu cache các giao dịch tải về từ Server. Khi điện thoại mất mạng, ứng dụng vẫn đọc từ Room để hiển thị dữ liệu.

**Công việc Backend (Node.js):**

- **API Thống kê** (`GET /api/analytics/summary?month=&year=&type=`): Dùng câu lệnh SQL `SUM(amount) ... GROUP BY category_id` để trả về tổng thu, tổng chi và danh sách tỷ lệ phần trăm từng danh mục phục vụ vẽ Donut Chart.
- **API Ngân sách** (`GET /api/budgets?month=&year=`, `POST /api/budgets`, `DELETE /api/budgets/:id`): Lấy danh sách ngân sách kèm câu truy vấn con tính tổng số tiền đã tiêu (`spent_amount`) của danh mục đó trong tháng để Android vẽ thanh tiến độ.

### 📋 Bảng tổng hợp phân công

| Thành viên | Nhánh Git | Phạm vi Android (Java) | Phạm vi Node.js Backend |
|---|---|---|---|
| **Người 1**<br>(Core & Visual) | `feature/capture-home` | • `CaptureExpenseActivity` (CameraX)<br>• Tab 1: `HomeFragment` (Grid + Lịch)<br>• Chi tiết giao dịch | • Multer upload middleware<br>• `POST /api/transactions`<br>• `GET /api/transactions`<br>• `DELETE /api/transactions/:id` |
| **Người 2**<br>(Auth & Wallets) | `feature/wallets-auth` | • Login / Register Activity<br>• Tab 3: `WalletsFragment` & Chuyển tiền<br>• Tab 5: `ProfileFragment` | • `POST /api/auth/register` & `login`<br>• `GET /api/auth/profile`<br>• CRUD `/api/wallets`<br>• `POST /api/wallets/transfer` |
| **Người 3**<br>(Stats & Budget) | `feature/stats-budget` | • Tab 2: `StatsFragment` (Donut Chart)<br>• Tab 4: `BudgetFragment` (Progress)<br>• Room Database (Offline Cache) | • `GET /api/analytics/summary`<br>• `GET /api/budgets`<br>• `POST /api/budgets`<br>• `DELETE /api/budgets/:id` |

---

## 6. QUY CHUẨN GIAO TIẾP API & QUY TRÌNH LÀM VIỆC GIT

### 6.1. Chuẩn JSON Response duy nhất từ Node.js

Mọi API trong Node.js (dù thành công hay thất bại) **BẮT BUỘC** trả về đúng cấu trúc 3 trường `success`, `message`, `data` dưới đây để khớp với class `ApiResponse<T>` bên Android:

```jsonc
// Khi thành công (HTTP 200 / 201):
{
  "success": true,
  "message": "Lấy dữ liệu thành công",
  "data": { ... } // hoặc mảng [ ... ]
}

// Khi có lỗi (HTTP 400 / 401 / 500):
{
  "success": false,
  "message": "Số dư ví không đủ để thực hiện giao dịch",
  "data": null
}
```

### 6.2. Quy ước URL kết nối & Load ảnh

- **Địa chỉ Server khi chạy Máy ảo Android Studio (Emulator):** `http://10.0.2.2:5000/api/` (đã cấu hình sẵn trong `ApiClient.java`).
- **Địa chỉ khi chạy Điện thoại thật cắm cáp:** Đổi `10.0.2.2` thành địa chỉ IPv4 LAN của máy tính chạy Node.js (ví dụ `http://192.168.1.15:5000/api/`). Điện thoại và laptop **bắt buộc** bắt chung 1 mạng WiFi.
- **Cách hiển thị ảnh từ Server bằng Glide:** Trong DB cột `image_url` lưu dạng `/uploads/img_171000.jpg`. Khi hiển thị trên Android, ghép thêm host `http://10.0.2.2:5000` + `image_url` rồi truyền vào `Glide.with(context).load(fullUrl)`.

### 6.3. Quy trình lệnh Git bắt buộc cho các thành viên

```bash
# BƯỚC 1: Lần đầu tiên lấy dự án Base về máy tính:
git clone https://github.com/quangkhuat-tap-code/capmoney-app.git
cd capmoney-app
git checkout develop

# BƯỚC 2: Tự tạo nhánh riêng theo phân công (chỉ làm 1 lần):
git checkout -b feature/capture-home     # (Nếu là Người 1)
git checkout -b feature/wallets-auth     # (Nếu là Người 2)
git checkout -b feature/stats-budget     # (Nếu là Người 3)

# BƯỚC 3: Sau khi code xong tính năng, kiểm tra app chạy không lỗi đỏ rồi đẩy lên:
git add .
git commit -m "feat: hoan thanh man hinh danh sach vi va chuyen tien"
git push -u origin feature/ten-nhanh-cua-ban

# BƯỚC 4: Lên GitHub tạo Pull Request (PR) từ nhánh feature/... vào nhánh `develop`.
# TUYỆT ĐỐI KHÔNG push trực tiếp lên `main` hoặc `develop` khi chưa kiểm tra!
```

---

## 7. BỘ PROMPT MẪU ĐỂ "VIBE CODE" VỚI AI (COPY-PASTE TRỰC TIẾP)

Khi bất kỳ thành viên nào trong nhóm dùng ChatGPT, Claude, Gemini hoặc Cursor để nhờ viết code cho phần việc của mình, hãy **COPY NGUYÊN KHỐI PROMPT DƯỚI ĐÂY** dán vào đầu cuộc trò chuyện. AI sẽ lập tức hiểu toàn bộ cấu trúc dự án và viết code lắp vào chạy ngay mà không cần sửa lại import hay màu sắc.

**📄 Master Prompt cho AI Vibe Coding:**

```text
[SYSTEM CONTEXT - DỰ ÁN CAPMONEY ANDROID & NODE.JS]
Bạn là Senior Android (Java) & Node.js Developer đang hỗ trợ tôi code một module trong dự án nhóm "CapMoney" (ứng dụng quản lý chi tiêu bằng hình ảnh phong cách Locket).
Hãy tuân thủ TUYỆT ĐỐI các quy chuẩn kỹ thuật đã khóa sau đây của dự án:

1. THÔNG TIN ANDROID (JAVA):
- Ngôn ngữ: 100% Java (TUYỆT ĐỐI KHÔNG viết Kotlin).
- Root Package bắt buộc: `com.example.comgroupcapmoney`
- Các class dùng chung ĐÃ CÓ SẴN (chỉ việc import và gọi, không viết lại):
  + `com.example.comgroupcapmoney.data.api.ApiClient` -> gọi `ApiClient.getClient(context)` (đã tự động đính kèm Header `Authorization: Bearer <token>`).
  + `com.example.comgroupcapmoney.model.ApiResponse<T>` -> có sẵn `isSuccess()`, `getMessage()`, `getData()`.
  + `com.example.comgroupcapmoney.utils.SharedPrefManager` -> gọi `SharedPrefManager.getInstance(context)` với các hàm `saveToken()`, `getToken()`, `saveUser()`, `getUserId()`, `isLoggedIn()`, `clear()`.
- Hệ màu Dark Mode ĐÃ CÓ SẴN trong `res/values/colors.xml` (bắt buộc dùng `@color/...`, không hardcode mã hex):
  + `@color/bg_primary_dark` (#121212 - nền màn hình)
  + `@color/bg_surface_card` (#1E1E1E - nền CardView/Dialog)
  + `@color/bg_elevated_input` (#2C2C2E - nền EditText/Button phụ)
  + `@color/text_primary_white` (#FFFFFF), `@color/text_secondary_gray` (#A0A0A5), `@color/text_hint_gray` (#636366)
  + `@color/accent_blue` (#0A84FF), `@color/expense_red` (#FF453A), `@color/income_green` (#32D74B), `@color/warning_yellow` (#FFD60A), `@color/overlay_dark_60` (#99000000).
- Kích thước ĐÃ CÓ SẴN trong `res/values/dimens.xml`:
  + `@dimen/spacing_tiny` (4dp), `@dimen/spacing_small` (8dp), `@dimen/spacing_medium` (16dp), `@dimen/spacing_large` (24dp)
  + `@dimen/corner_radius_small` (12dp), `@dimen/corner_radius_card` (20dp), `@dimen/corner_radius_pill` (50dp)
  + `@dimen/text_size_small` (12sp), `@dimen/text_size_body` (14sp), `@dimen/text_size_title` (18sp), `@dimen/text_size_money_large` (32sp).

2. THÔNG TIN BACKEND (NODE.JS + MYSQL):
- Sử dụng Express.js và `mysql2/promise` (async/await).
- Mọi response trả về BẮT BUỘC có định dạng: `res.status(code).json({ success: true/false, message: "...", data: ... })`.
- Middleware xác thực JWT đã gán user ID vào `req.user.id`.
- Database gồm 6 bảng: `users`, `wallets`, `categories`, `transactions`, `wallet_transfers`, `budgets`.

3. PHẦN VIỆC CỦA TÔI HIỆN TẠI:
Tôi là [Điền: Thành viên 1 / Thành viên 2 / Thành viên 3].
Hãy viết đầy đủ, hoàn chỉnh, không viết tắt (không dùng comment `// TODO`) cho yêu cầu sau đây:
[Mô tả màn hình hoặc API bạn muốn AI code tại đây...]
```
