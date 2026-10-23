# PHẦN 1 · Chụp ảnh ghi chi tiêu + Tab 1 Home + Transactions API

> **Người phụ trách:** Thành viên 1 (Core & Visual) · **Nhánh:** `lamanh` · **Ngày viết spec:** 22/10/2026
> **Nguồn:** tách từ [../Tai_Lieu_Ky_Thuat_Va_Phan_Cong_CapMoney.md](../Tai_Lieu_Ky_Thuat_Va_Phan_Cong_CapMoney.md), mục 5, phần "Thành viên 1". Đã đối chiếu với code thực tế trên `develop` (commit `bb08ff3`).

Bộ tài liệu này viết cho **agent code**. Agent làm đúng theo spec, không tự suy diễn thêm. Chỗ nào spec chưa nói thì chọn cách **đơn giản nhất** và ghi lại trong mô tả PR.

---

## 1. Thứ tự đọc

| File | Nội dung | Ai cần đọc |
|---|---|---|
| **00_Tong_Quan_Phan_1.md** (file này) | Phạm vi, ranh giới file, điểm lệch so với tài liệu gốc, các quyết định đã chốt | Tất cả |
| [01_Database_Va_API_Contract.md](01_Database_Va_API_Contract.md) | Bảng DB, khuôn JSON, danh sách endpoint, mã lỗi | Android + Backend |
| [02_Backend_Spec.md](02_Backend_Spec.md) | Cách code backend Node.js + ca test curl | Backend |
| [03_Android_Nen_Tang_Chung.md](03_Android_Nen_Tang_Chung.md) | Manifest, resource, model, Retrofit, repository, utils | Android |
| [04_Android_Man_Chup_Chi_Tieu.md](04_Android_Man_Chup_Chi_Tieu.md) | Màn `CaptureExpenseActivity` + bottom sheet chọn ví/danh mục | Android |
| [05_Android_Tab1_Home.md](05_Android_Tab1_Home.md) | `HomeFragment`: lưới ảnh, lịch, chi tiết, xóa | Android |
| [06_Ke_Hoach_Commit_Va_Nghiem_Thu.md](06_Ke_Hoach_Commit_Va_Nghiem_Thu.md) | Thứ tự làm, commit, unit test, nghiệm thu, PR | Agent code |

---

## 2. Phạm vi Phần 1

### 2.1. Làm

| Mảng | Hạng mục |
|---|---|
| Android | Màn chụp ảnh hoặc chọn ảnh, nhập tiền, chọn Chi/Thu, chọn ví + danh mục, ngày giờ, ghi chú; Lưu bằng upload multipart |
| Android | Tab 1 Home: chọn tháng, thẻ tổng chi/thu, chế độ **Lưới ảnh 3 cột** kiểu Locket, chế độ **Lịch tháng** có thumbnail |
| Android | Dialog chi tiết giao dịch + **Xóa** |
| Backend | Middleware upload `multer` (≤ 5MB, JPG/PNG/WEBP) |
| Backend | `POST /api/transactions` (cập nhật số dư ví trong SQL transaction) |
| Backend | `GET /api/transactions?month=&year=&wallet_id=` |
| Backend | `DELETE /api/transactions/:id` (hoàn số dư, xóa file ảnh) |
| Backend | **`GET /api/categories`**: tài liệu gốc **không giao cho ai**, nhưng màn chụp ảnh bắt buộc cần, mà `model/Category.java` lại thuộc Phần 1, nên Phần 1 nhận làm |

### 2.2. Không làm

- Sửa giao dịch (`PUT`), phân trang, tìm kiếm.
- Màn đăng nhập/đăng ký, API ví, API auth: của **Thành viên 2**.
- Thống kê, ngân sách, cache offline Room: của **Thành viên 3**. Phần 1 **không** lưu Room.
- Thư viện CameraX (xem quyết định Q3).

---

## 3. Ranh giới file (để không xung đột khi merge)

### 3.1. Android: Phần 1 được tạo hoặc sửa

```text
app/src/main/java/com/example/comgroupcapmoney/
├── data/api/TransactionApi.java                (mới)
├── data/repository/TransactionRepository.java  (mới)
├── model/Transaction.java                      (mới)
├── model/Category.java                         (mới)
├── model/DeleteTransactionResult.java          (mới)
├── ui/capture/*                                (toàn bộ)
├── ui/home/*                                   (toàn bộ)
└── utils/CurrencyUtils.java, DateUtils.java, ImageUtils.java, SingleEvent.java   (mới)

app/src/main/res/
├── layout/activity_capture_expense.xml, bottom_sheet_select_category.xml, item_category.xml,
│         fragment_home.xml, item_photo_grid.xml, item_calendar_day.xml,
│         dialog_transaction_detail.xml, bottom_sheet_day_transactions.xml
├── drawable/bg_capture_*, bg_photo_*, bg_input_rounded, bg_calendar_today, ic_*_24
├── values/colors_capture_home.xml, strings_capture_home.xml
└── xml/file_paths.xml

app/src/test/java/com/example/comgroupcapmoney/utils/CurrencyUtilsTest.java, DateUtilsTest.java
```

### 3.2. Android: file dùng chung, sửa tối thiểu và commit riêng

| File | Sửa gì |
|---|---|
| `AndroidManifest.xml` | Quyền `INTERNET`, `usesCleartextTraffic`, khai báo `CaptureExpenseActivity`, `FileProvider` |
| `settings.gradle` | Chỉ khi build đang lỗi (mục 4) |

### 3.3. Android: KHÔNG được sửa

`MainActivity.java`, `activity_main.xml`, `bottom_nav_menu.xml`, `ApiClient.java` (chỉ tạm đổi IP khi test, không commit), `ApiResponse.java`, `SharedPrefManager.java`, `colors.xml`, `dimens.xml`, `strings.xml`, `themes.xml`, `app/build.gradle`, mọi file trong `ui/auth`, `ui/wallets`, `ui/profile`, `ui/stats`, `ui/budget`, `data/local`, và `model/User.java`, `Wallet.java`, `Budget.java`.

### 3.4. Backend

Phần 1 sở hữu: `upload.middleware.js`, `category.*`, `transaction.*`, `utils/transaction.query.js`, `utils/AppError.js`.
File dùng chung (`app.js`, `db.js`, `auth.middleware.js`, `database_init.sql`): chỉ tạo khung nếu **chưa có** (file 02, mục 2); nếu có rồi thì chỉ thêm dòng mount route của Phần 1.

---

## 4. Code thực tế khác tài liệu gốc

Tài liệu gốc ghi nhiều thứ "đã có sẵn", nhưng repo thực tế không có. **Code theo thực tế.**

| # | Tài liệu gốc | Thực tế | Ảnh hưởng |
|---|---|---|---|
| D1 | Màu `bg_primary_dark`, `expense_red`, `overlay_dark_60`… | Tên khác: `bg_main`, `bg_card`, `bg_card_secondary`, `text_primary`, `text_secondary`, `color_expense`, `color_income`, `color_accent`, `color_warning` | Dùng tên thực tế; màu thiếu thì thêm vào `colors_capture_home.xml` |
| D2 | Có `ApiService.java` | Không có | Phần 1 tạo `TransactionApi.java` riêng |
| D3 | `SharedPrefManager.saveToken()`, `saveUser()` | Chỉ có `saveAuthData(token, userId, userName)` | Phần 1 không cần; Thành viên 2 nên biết |
| D4 | Đã cài CameraX | `build.gradle` không có | Dùng camera hệ thống (Q3) |
| D5 | Manifest có `INTERNET`, `CAMERA`, `usesCleartextTraffic` | Không có | Thiếu `INTERNET` thì **mọi** API đều lỗi. Phần 1 thêm |
| D6 | `CaptureExpenseActivity`, `HomeFragment` "đã có khung" | Class rỗng, chưa có layout, activity chưa khai báo trong manifest | Viết mới |
| D7 | Backend `capmoney-backend` | Chưa có repo/thư mục | Q1 |
| D8 | Không có API danh mục | Màn chụp ảnh cần | Phần 1 làm `GET /api/categories` |
| D9 | `targetSdk 34`, JDK 17 | `compileSdk/targetSdk 37`, `sourceCompatibility` Java 11 | Viết theo Java 11: không dùng `record`, switch expression, text block. **Không** dùng `java.time`, vì minSdk 24 mà chưa bật desugaring; dùng `Calendar` / `SimpleDateFormat` |
| D10 | `settings.gradle` | Dòng jitpack bị lỗi định dạng: `maven { url '[https://jitpack.io](https://jitpack.io)' }`, lại nằm trong `pluginManagement`. `dependencyResolutionManagement` **không có** jitpack, nên `MPAndroidChart` khả năng cao không tải được | Xem mục 4.1 |
| D11 | Tài liệu gốc mục "Hướng dẫn" ghi prompt ở "Mục 8" | Prompt ở Mục 7 | Không ảnh hưởng code |

### 4.1. Build hiện tại của `develop`

**Chưa kiểm chứng được.** Lúc viết spec đã chạy thử `./gradlew :app:assembleDebug`, nhưng sau hơn 20 phút vẫn chưa xong: Gradle 9.6.0 / AGP 9.4.1 chưa có trong cache, và có thể Android Studio đang giữ lock. Việc đầu tiên agent phải làm:

1. Build `develop` **trước khi sửa gì**: `./gradlew :app:assembleDebug`.
2. Build lỗi do `Could not find com.github.PhilJay:MPAndroidChart:v3.1.0` (rất có khả năng, theo D10): sửa `settings.gradle` như dưới đây, commit riêng `build: sua cau hinh jitpack de build duoc` (bước A-0 ở file 06), rồi **báo nhóm**. Nên đẩy sửa này lên `develop` qua 1 PR nhỏ riêng, vì cả 3 người đều cần.

```groovy
pluginManagement {
    repositories {
        google { /* giữ nguyên content { ... } */ }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

3. Build lỗi do lý do khác (SDK 37 chưa cài, thiếu `local.properties`…): đó là cấu hình máy, **không** sửa code. Ghi lại lỗi và báo người dùng.

---

## 5. Quyết định đã chốt trong spec

Các điểm này tài liệu gốc chưa rõ hoặc mâu thuẫn. Spec đã chọn sẵn một phương án; muốn đổi thì sửa spec trước khi code.

| # | Vấn đề | Phương án chốt | Lý do |
|---|---|---|---|
| Q1 | Backend để ở đâu | Thư mục riêng `E:\Hoctap\capmoney-backend` có git riêng (đúng tài liệu gốc). Có repo nhóm rồi thì dùng repo đó | Tài liệu gốc tách 2 thư mục |
| Q2 | Ai tạo khung backend dùng chung | Phần 1 tạo bản tối thiểu nếu chưa có, commit riêng, báo nhóm | Phần 1 cần chạy được API |
| Q3 | CameraX hay camera hệ thống | `ActivityResultContracts.TakePicture` + `PickVisualMedia` | Không phải thêm thư viện, không cần xin quyền CAMERA, ít lỗi trên emulator. Tài liệu gốc cho phép "CameraX / ActivityResultContracts" |
| Q4 | Ảnh có bắt buộc không | **Không**. Thiếu ảnh thì ô lưới hiện emoji danh mục | Khoản thu như Lương thường không có ảnh |
| Q5 | Chi tiêu vượt số dư | **Chặn** (client + server, 400) | Tài liệu gốc dùng chính lỗi này làm ví dụ JSON |
| Q6 | Server trả link ảnh đầy đủ hay tương đối | **Tương đối** (`/uploads/...`); Android ghép `ApiClient.IMAGE_BASE_URL` | Đổi emulator/điện thoại thật chỉ sửa 1 chỗ |
| Q7 | Định dạng tiền và ngày trong JSON | Tiền là number; ngày `yyyy-MM-dd HH:mm:ss` giờ local | Gson parse thẳng, không lệch múi giờ |
| Q8 | `icon` danh mục là gì | 1 emoji (DB `utf8mb4`) | Không cần bộ icon drawable |
| Q9 | Lấy danh sách ví khi Thành viên 2 chưa xong | Gọi `GET /api/wallets` theo khuôn ở file 01, mục 3.5, dùng DTO riêng `WalletOption`; test bằng dữ liệu seed | Không phụ thuộc `model/Wallet.java` |
| Q10 | Tuần trên lịch bắt đầu thứ mấy | Thứ Hai | Thói quen Việt Nam |
| Q11 | Ngày tương lai | Không cho chọn | Ghi chép chi tiêu đã xảy ra |
| Q12 | Kiến trúc | MVVM nhẹ: `Activity/Fragment` → `ViewModel` (LiveData) → `TransactionRepository` → Retrofit | Đúng tài liệu gốc (MVVM + Repository), không thêm thư viện |

---

## 6. Phụ thuộc với thành viên khác (cần báo nhóm)

| Với | Nội dung | Trạng thái |
|---|---|---|
| Thành viên 2 | `GET /api/wallets` trả đúng khuôn ở file 01, mục 3.5 | Cần thống nhất |
| Thành viên 2 | JWT ký với payload `{ id: user.id }`; `auth.middleware.js` gán `req.user.id` | Cần thống nhất |
| Thành viên 2 | Đăng ký xong phải tạo ví mặc định `is_default = 1`; màn chụp sẽ chọn sẵn ví này | Đã có trong tài liệu gốc |
| Thành viên 3 | `utils/CurrencyUtils.java` do Phần 1 tạo, dùng chung, không tạo lại | Báo khi merge |
| Thành viên 3 | Room cache giao dịch có thể dùng chung `model/Transaction.java` / API `GET /api/transactions` | Báo khi merge |
| Cả nhóm | Ai sửa `settings.gradle` (mục 4.1) | Cần chốt |
