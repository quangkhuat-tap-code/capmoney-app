# PHẦN 4 · Tổng quan: Mở rộng dự án (chia 3 phần để bảo vệ)

> **Người code + test toàn bộ:** Lâm Anh · **Nhánh:** `lamanh` (ở **cả 2 repo**) · **Ngày viết:** 27/10/2026
> **Đọc kèm:** [../Huong_Dan_Lam_Viec_Nhom.md](../Huong_Dan_Lam_Viec_Nhom.md) (quy trình Git, quy tắc commit) · [../Bao_Ve_Do_An.md](../Bao_Ve_Do_An.md) (ai trình bày phần nào)

Phần 1–3 đã xong và đã merge vào `develop` (27/10/2026). Phần 4 thêm **9 tính năng mở rộng**, chia thành **3 phần con 4A, 4B, 4C**, mỗi phần con nối tiếp phần gốc của một người. Như vậy khi bảo vệ, **mỗi người trình bày đúng 1 phần**: phần gốc của mình + phần mở rộng của mình.

| Phần con | Người **trình bày** | Nối tiếp | Nội dung |
|---|---|---|---|
| **4A** · Giao dịch nâng cao | **Lâm Anh** | Phần 1 | Sửa giao dịch · Tìm kiếm giao dịch · Đọc số tiền từ ảnh hoá đơn |
| **4B** · Tài khoản & ví mở rộng | **Trung** | Phần 2 | Lịch sử chuyển tiền · Đổi mật khẩu · Ảnh đại diện |
| **4C** · Thống kê & ngân sách mở rộng | **Quang** | Phần 3 | Biểu đồ 6 tháng · Thông báo vượt ngân sách · Tab Tài khoản xem được khi mất mạng |

**Người code và test:** Lâm Anh làm **cả 9 tính năng** trên nhánh `lamanh`. Trung, Quang **không code**; việc của hai bạn là **đọc hiểu** phần của mình theo [../Bao_Ve_Do_An.md](../Bao_Ve_Do_An.md) để trình bày và trả lời câu hỏi.

Bộ tài liệu này viết để **đưa thẳng cho AI** code theo, giống Phần 1–3: làm **đúng** theo tài liệu, không tự thêm tính năng. Chỗ nào chưa nói rõ thì chọn cách **đơn giản nhất** và ghi lại trong PR.

---

## 1. Danh sách file và thứ tự làm

Mỗi file tính năng gồm **cả backend và Android** của tính năng đó, để người trình bày chỉ cần đọc 1 file là nắm trọn.

| Thứ tự | File | Phần con | Repo |
|---|---|---|---|
| — | **00_Tong_Quan_Phan_4.md** (file này) | | Phạm vi, ranh giới file, quy ước |
| — | [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) | | Thay đổi DB, JSON từng API, mã lỗi. **Hợp đồng chung** |
| 1 | [4A-1_Sua_Giao_Dich.md](4A-1_Sua_Giao_Dich.md) | 4A | backend + app |
| 2 | [4A-2_Tim_Kiem_Giao_Dich.md](4A-2_Tim_Kiem_Giao_Dich.md) | 4A | backend + app |
| 3 | [4A-3_Doc_So_Tien_Hoa_Don.md](4A-3_Doc_So_Tien_Hoa_Don.md) | 4A | app |
| 4 | [4B-1_Lich_Su_Chuyen_Tien.md](4B-1_Lich_Su_Chuyen_Tien.md) | 4B | backend + app |
| 5 | [4B-2_Doi_Mat_Khau.md](4B-2_Doi_Mat_Khau.md) | 4B | backend + app |
| 6 | [4B-3_Anh_Dai_Dien.md](4B-3_Anh_Dai_Dien.md) | 4B | backend (có migration) + app |
| 7 | [4C-1_Bieu_Do_6_Thang.md](4C-1_Bieu_Do_6_Thang.md) | 4C | backend + app |
| 8 | [4C-2_Thong_Bao_Vuot_Ngan_Sach.md](4C-2_Thong_Bao_Vuot_Ngan_Sach.md) | 4C | app |
| 9 | [4C-3_Tab_Tai_Khoan_Offline.md](4C-3_Tab_Tai_Khoan_Offline.md) | 4C | app |
| 10 | [99_Nghiem_Thu_Va_PR.md](99_Nghiem_Thu_Va_PR.md) | | Test toàn bộ, 3 cặp PR (4A, 4B, 4C) |

**Commit:** mỗi file tính năng = 1 commit backend (nếu có) + 1 commit app. Sửa file dùng chung (mục 4.3) thì **commit riêng**, ghi lý do.

**Pull Request:** làm xong 3 file của một phần con thì mở PR `lamanh` → `develop` cho phần con đó (mục 99). Mỗi người có PR riêng của phần mình để chỉ khi bảo vệ.

Thứ tự 4A → 4B → 4C chỉ là gợi ý. Các tính năng **không phụ thuộc nhau**, trừ 4C-2 dùng API ngân sách có sẵn và 4C-3 tăng `version` Room (làm sau cùng cho đỡ xoá cache nhiều lần).

---

## 2. Phạm vi Phần 4

### 2.1. Làm

| Mã | Tính năng | Backend | Android |
|---|---|---|---|
| 4A-1 | Sửa giao dịch | `PUT /api/transactions/:id` | Nút **Sửa** trong hộp chi tiết giao dịch → màn chụp ở **chế độ sửa** |
| 4A-2 | Tìm kiếm giao dịch | `GET /api/transactions/search?q=&page=` | Màn `SearchActivity` mở từ Tab Home, tải thêm khi cuộn |
| 4A-3 | Đọc số tiền từ ảnh hoá đơn | — | ML Kit đọc chữ trên máy, tự điền ô số tiền, có nút hoàn tác |
| 4B-1 | Lịch sử chuyển tiền | `GET /api/wallets/transfers?month=&year=` | Nút **Lịch sử** ở Tab Tài khoản → bottom sheet theo tháng |
| 4B-2 | Đổi mật khẩu | `PUT /api/auth/password` | Nút **Đổi mật khẩu** ở Tab Cá nhân → hộp thoại |
| 4B-3 | Ảnh đại diện | Thêm cột `users.avatar_url`, `POST /api/auth/avatar` | Bấm avatar ở Tab Cá nhân → chọn ảnh → tải lên |
| 4C-1 | Biểu đồ 6 tháng | `GET /api/analytics/trend?month=&year=&months=6` | Biểu đồ cột Chi/Thu 6 tháng dưới donut ở Tab Thống kê |
| 4C-2 | Thông báo vượt ngân sách | — (dùng `GET /api/budgets` có sẵn) | Lưu khoản chi làm ngân sách vượt 80% hoặc 100% → hiện thông báo |
| 4C-3 | Tab Tài khoản offline | — | Room lưu danh sách ví; mất mạng vẫn xem được, có banner |

### 2.2. Không làm

- Đổi **loại** giao dịch (Chi ↔ Thu) khi sửa; đổi **ảnh** của giao dịch đã lưu. Muốn đổi thì xoá và tạo lại.
- Quên mật khẩu (cần gửi email), đăng xuất mọi thiết bị khi đổi mật khẩu.
- Ghi giao dịch khi mất mạng (hàng chờ gửi lại). Phần 4 chỉ **đọc** offline.
- Thông báo đẩy từ server (FCM), nhắc lịch định kỳ.
- Đọc chữ ngoài số tiền (tên cửa hàng, ngày trên hoá đơn).
- Xoá ví, danh mục tự tạo, xuất file.

---

## 3. Code đang có (đọc kỹ, đừng tạo lại)

Kiểm tra ngày 27/10/2026 trên `develop` (`913d76d` app, `3bce232` backend).

### 3.1. Backend (`backend-cap-money`)

| File | Đã có gì | Phần 4 dùng thế nào |
|---|---|---|
| `src/controllers/transaction.controller.js` | `createTransaction` (khoá ví `FOR UPDATE`, chặn chi vượt số dư), `getTransactions`, `deleteTransaction` (hoàn số dư, xoá file ảnh), hàm `isValidDateTimeString`, `removeFileSafe` | 4A-1, 4A-2 thêm `updateTransaction`, `searchTransactions` **trong file này** |
| `src/utils/transaction.query.js` | `BASE_TRANSACTION_QUERY` (JOIN danh mục + ví), `mapTransaction(row)` | Dùng để trả giao dịch đúng khuôn Phần 1 |
| `src/middlewares/upload.middleware.js` | `uploadSingleImage` (multer, field `image`, ≤ 5MB, JPG/PNG/WEBP), `UPLOAD_DIR` | 4B-3 dùng lại cho avatar |
| `src/controllers/auth.controller.js` | `register`, `login`, `getProfile`, `toPublicUser(row)` | 4B-2, 4B-3 thêm `changePassword`, `uploadAvatar` |
| `src/controllers/wallet.controller.js` | `getWallets`, `createWallet`, `updateWallet`, `transferMoney` (ghi bảng `wallet_transfers`) | 4B-1 thêm `getTransfers` |
| `src/controllers/analytics.controller.js` | `getSummary` | 4C-1 thêm `getTrend` |
| `src/utils/monthRange.js` | `parseMonthYear(month, year)` → `{ month, year, start, end }` hoặc `null` | Dùng cho 4B-1, 4C-1 |
| `database_init.sql` | 6 bảng. Bảng `users` **không có** cột `avatar_url` (tài liệu kỹ thuật gốc ghi có, nhưng code thật không có) | 4B-3 thêm cột (mục 4.3) |

Kiểu code giữ nguyên như Phần 1–3: controller `async (req, res, next)`, lỗi kiểm tra trả thẳng `res.status(400).json({ success: false, message, data: null })`, lỗi lạ `next(err)`, mọi SQL lọc `user_id = req.user.id`.

### 3.2. Android (`capmoney-app`)

| File | Đã có gì | Phần 4 dùng thế nào |
|---|---|---|
| `ui/capture/CaptureExpenseActivity` + `CaptureViewModel` | Màn chụp: ảnh, Chi/Thu, số tiền, ví, danh mục, ngày, ghi chú, lưu multipart. Lưu xong `setResult(RESULT_OK); finish()` | 4A-1 thêm chế độ sửa; 4A-3 thêm đọc hoá đơn; 4C-2 gọi kiểm tra ngân sách sau khi lưu |
| `ui/home/TransactionDetailDialog` | `newInstance(Transaction)`, nút `btn_delete_tx` | 4A-1 thêm nút Sửa |
| `ui/home/HomeFragment` | `onResume()` gọi `viewModel.load()` → quay lại từ màn khác là tự tải lại | 4A-1 không cần thêm gì để Home cập nhật; 4A-2 thêm nút tìm kiếm |
| `utils/ImageUtils` | `compressForUpload(ctx, uri)` (≤ 1280px, JPEG, xoay đúng chiều), `createCaptureUri(ctx)` | 4A-3 đọc chữ trên ảnh đã nén; 4B-3 nén avatar |
| `ui/wallets/WalletsFragment` + `WalletsViewModel` | Nút `wallet_btn_add`, `wallet_btn_transfer`; `loadWallets()` | 4B-1 thêm nút Lịch sử; 4C-3 thêm cache |
| `ui/profile/ProfileFragment` | Avatar là **chữ cái đầu** (`profile_tv_avatar`), nút `profile_btn_logout` | 4B-2, 4B-3 |
| `ui/stats/StatsFragment` | Donut `stats_pie_chart`, danh sách `stats_rv_categories` trong `stats_scroll` | 4C-1 thêm biểu đồ cột |
| `data/local/AppDatabase` | Room `version = 1`, `fallbackToDestructiveMigration()`, chỉ có `TransactionEntity` | 4C-3 thêm `WalletEntity`, tăng `version = 2` |
| `ui/auth/SessionManager` | `notifyExpired()`, `observeExpired(...)` | Mọi màn mới gặp 401 dùng cơ chế này |
| `data/repository/RepoCallback` | `onSuccess(data, msg)`, `onError(msg, unauthorized)` | API mới của 4A/4B dùng lại |
| `utils/CurrencyUtils`, `DateUtils` | `format`, `formatShort`, `formatInput`, `parseDigits`; `toServer`, `toDisplay`, `monthTitle` | Dùng, không sửa |

Thư viện **đã có**: Retrofit 2.9 + Gson, Glide 4.16, Room 2.6.1, MPAndroidChart 3.1.0 (có `BarChart`), Material. **Chưa có**: ML Kit (4A-3 thêm).

Cấu hình: `minSdk 24`, `targetSdk 37`, Java 11. **Không** dùng `record`, switch expression, `java.time`. Màn mới là Activity thì xử lý insets giống `LoginActivity.applyWindowInsets()`.

---

## 4. Ranh giới file

### 4.1. Backend: sửa / tạo

| Tính năng | File |
|---|---|
| 4A-1, 4A-2 | `transaction.controller.js` (thêm hàm), `transaction.routes.js` (thêm route) |
| 4B-1 | `wallet.controller.js`, `wallet.routes.js` |
| 4B-2, 4B-3 | `auth.controller.js`, `auth.routes.js` |
| 4B-3 | **Tạo mới** `migrations/phan4_users_avatar.sql`; sửa `database_init.sql` (dùng chung, commit riêng) |
| 4C-1 | `analytics.controller.js`, `analytics.routes.js` |

### 4.2. Android: tạo mới

```text
app/src/main/java/com/example/comgroupcapmoney/
├── model/SearchPage.java, Transfer.java, TrendPoint.java
├── data/api/AnalyticsTrendApi.java          (4C-1, tách khỏi StatsApi cho rõ phần)
├── data/local/WalletEntity.java, WalletDao.java, WalletCache.java   (4C-3)
├── ui/search/SearchActivity.java, SearchViewModel.java, SearchAdapter.java   (4A-2)
├── ui/capture/ReceiptAmountParser.java, ReceiptScanner.java   (4A-3)
├── ui/wallets/TransferHistoryBottomSheet.java, TransferHistoryAdapter.java   (4B-1)
├── ui/profile/ChangePasswordDialog.java   (4B-2)
├── ui/stats/TrendChartHelper.java   (4C-1)
└── ui/budget/BudgetAlertNotifier.java   (4C-2)

app/src/test/java/com/example/comgroupcapmoney/
├── ui/capture/ReceiptAmountParserTest.java
└── ui/budget/BudgetAlertNotifierTest.java
```

Layout, drawable, string mới đặt tên **bắt đầu bằng** tiền tố của tính năng: `edit_`, `search_`, `receipt_`, `transfer_history_`, `password_`, `avatar_`, `trend_`, `budget_alert_`, `wallet_cache_`. String để trong file mới `values/strings_phan4.xml`.

### 4.3. File dùng chung phải sửa (mỗi file 1 commit riêng)

| File | Tính năng | Sửa gì |
|---|---|---|
| `database_init.sql` (backend) | 4B-3 | Thêm `avatar_url VARCHAR(255) NULL` vào `CREATE TABLE users`, để máy mới cài có sẵn cột |
| `AndroidManifest.xml` | 4A-2, 4C-2 | Khai báo `SearchActivity` (`exported="false"`); thêm `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` |
| `app/build.gradle` | 4A-3 | Thêm `implementation 'com.google.android.gms:play-services-mlkit-text-recognition:19.0.1'` (bản qua Google Play services, xem 4A-3) |

**Vẫn không sửa:** `MainActivity`, `activity_main.xml`, `ApiClient`, `ApiResponse`, `SharedPrefManager`, `colors.xml`, `dimens.xml`, `strings.xml`, `themes.xml`, `settings.gradle`, `auth.middleware.js`, `db.js`.

---

## 5. Quy ước giao diện (giống Phần 1–3)

- Màu, kích thước dùng đúng tên trong `colors.xml`, `dimens.xml` (xem Phần 3, file 00, mục 5). Nền `bg_main`, thẻ `bg_card` bo `corner_radius_card`.
- Kiến trúc: `Activity/Fragment` → `ViewModel` (LiveData) → `Repository` → Retrofit. Xoay màn hình không gọi lại API.
- Gặp `401`: repository gọi `SessionManager.notifyExpired()`, màn hình gọi `SessionManager.observeExpired(...)` → về màn Đăng nhập.
- Mất mạng: hiện đúng chữ `R.string.capture_error_network` như các màn khác.
- Nút (+) của `MainActivity` nằm góc dưới phải, nội dung cuộn chừa `paddingBottom="88dp"`.

---

## 6. Dữ liệu test

Dùng tài khoản `demo@example.test` trên máy Lâm Anh (đã có 4 ví, giao dịch tháng 10/2026, ngân sách). Tài khoản này chỉ có trong DB máy Lâm Anh, **không** commit. Test xong không cần dọn, trừ ảnh avatar test trong `public/uploads` (đã nằm trong `.gitignore`).
