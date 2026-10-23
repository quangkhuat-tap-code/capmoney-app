# 03 · Android: nền tảng dùng chung cho Phần 1

> Làm file này **trước** [04](04_Android_Man_Chup_Chi_Tieu.md) và [05](05_Android_Tab1_Home.md), vì cả hai màn hình đều dùng các class ở đây.
> Ngôn ngữ: **100% Java**, không Kotlin. Package gốc `com.example.comgroupcapmoney`.

---

## 1. Base code thực tế khác tài liệu gốc (BẮT BUỘC đọc)

Tài liệu gốc ghi nhiều thứ là "ĐÃ CÓ SẴN", nhưng code thực tế trên `develop` (commit `bb08ff3`) **khác**. Agent phải code theo **code thực tế**, không theo tài liệu:

| Tài liệu gốc nói | Thực tế trong repo | Phần 1 xử lý |
|---|---|---|
| Màu `bg_primary_dark`, `bg_surface_card`, `expense_red`, `overlay_dark_60`… | `colors.xml` chỉ có: `bg_main #0E0F12`, `bg_card #1B1C20`, `bg_card_secondary #26282E`, `text_primary #FFFFFF`, `text_secondary #8E8E93`, `color_expense #FF6464`, `color_income #4CD964`, `color_accent #5E7CE2`, `color_warning #F5A623`, `black`, `white`, `transparent` | Dùng **tên thực tế**. Màu còn thiếu thì thêm vào file riêng (mục 3) |
| Có `ApiService.java` | **Không có** | Tạo `TransactionApi.java` riêng (mục 4), **không** tạo `ApiService.java` |
| `SharedPrefManager.saveToken()`, `saveUser()` | Chỉ có `saveAuthData(token, userId, userName)`, `getToken()`, `getUserId()`, `getUserName()`, `isLoggedIn()`, `clear()` | Phần 1 chỉ cần `getToken()` (do `ApiClient` tự gọi) |
| `CaptureExpenseActivity`, `HomeFragment` "ĐÃ CÓ KHUNG" | Chỉ là class rỗng, không có layout | Viết mới toàn bộ |
| Đã cài CameraX | `app/build.gradle` **không có** CameraX | Dùng camera hệ thống qua `ActivityResultContracts` (mục 2.3), **không** thêm thư viện |
| `AndroidManifest` có `INTERNET`, `CAMERA`, `usesCleartextTraffic` | **Không có** cả ba; `CaptureExpenseActivity` cũng chưa khai báo | Phần 1 thêm (mục 2.1) |
| `ApiClient.IMAGE_BASE_URL` | Có: `"http://10.0.2.2:5000"` | Dùng để ghép link ảnh |
| `MainActivity` mở `CaptureExpenseActivity` | Đúng, bằng `startActivity` (không chờ kết quả) | `HomeFragment` tự tải lại trong `onResume()`; **không sửa** `MainActivity` |

---

## 2. File dùng chung Phần 1 buộc phải sửa

Sửa **tối thiểu**, commit riêng, để người khác merge ít xung đột.

### 2.1. `app/src/main/AndroidManifest.xml`

Thêm các phần sau, giữ nguyên mọi thứ khác:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    ...
    android:usesCleartextTraffic="true">

    <!-- giữ nguyên MainActivity -->

    <activity
        android:name=".ui.capture.CaptureExpenseActivity"
        android:exported="false"
        android:screenOrientation="portrait"
        android:windowSoftInputMode="adjustResize" />

    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>
```

- **Không** khai báo `android.permission.CAMERA`. Mở camera hệ thống bằng `ACTION_IMAGE_CAPTURE` thì không cần quyền này. Nếu app **khai báo** CAMERA mà chưa xin quyền lúc chạy, Android sẽ ném `SecurityException`.
- Nếu sau này thành viên khác thêm CAMERA vào manifest, `CaptureExpenseActivity` phải xin quyền runtime trước khi mở camera. Ghi chú điều này trong comment ở code mở camera.

### 2.2. File mới `app/src/main/res/xml/file_paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="captures" path="captures/" />
    <cache-path name="uploads" path="uploads/" />
</paths>
```

### 2.3. Không sửa `app/build.gradle`

Mọi thư viện Phần 1 cần đều đã có hoặc có sẵn theo dependency đi kèm:

| Cần | Lấy từ |
|---|---|
| Retrofit, Gson, OkHttp | Đã có |
| Glide 4.16.0 | Đã có |
| `RecyclerView`, `MaterialCardView`, `MaterialButtonToggleGroup`, `Chip`, `BottomSheetDialogFragment` | `com.google.android.material` (kéo theo `recyclerview`) |
| `ViewModel`, `LiveData` | Đi kèm `appcompat` / `activity` |
| `ActivityResultContracts.TakePicture`, `PickVisualMedia` | `androidx.activity` 1.13 (đã có) |
| `FileProvider` | `androidx.core` (đi kèm appcompat) |
| `ExifInterface` | Dùng `android.media.ExifInterface` (API 24+ có constructor nhận `InputStream`) |

> Nếu build báo thiếu một trong các class trên thì mới thêm dependency, và ghi rõ lý do trong commit.

---

## 3. Resource riêng của Phần 1

Để không đụng `colors.xml`, `strings.xml` (file dùng chung), Phần 1 tạo file mới:

### 3.1. `res/values/colors_capture_home.xml`

```xml
<resources>
    <color name="capture_overlay">#99000000</color>      <!-- lớp phủ mờ đè lên ảnh -->
    <color name="capture_text_hint">#636366</color>      <!-- chữ gợi ý -->
    <color name="capture_divider">#2C2C2E</color>
</resources>
```

Tên có tiền tố `capture_`, để không trùng nếu sau này `colors.xml` thêm màu theo tài liệu gốc.

### 3.2. `res/values/strings_capture_home.xml`

Đặt **toàn bộ** chữ hiển thị của Phần 1 vào file này, **không** hardcode trong layout/Java. Tên key có tiền tố `capture_` hoặc `home_`. Danh sách chữ cần có nằm trong file 04 và 05.

### 3.3. Drawable mới (`res/drawable/`)

| File | Mô tả |
|---|---|
| `bg_capture_amount_pill.xml` | `shape` bo góc `@dimen/corner_radius_pill`, nền `@color/capture_overlay` |
| `bg_photo_amount_label.xml` | `shape` bo góc 8dp, nền `@color/capture_overlay` |
| `bg_input_rounded.xml` | `shape` bo góc `@dimen/corner_radius_small`, nền `@color/bg_card_secondary` |
| `bg_calendar_today.xml` | `shape` viền 2dp `@color/color_accent`, bo góc 8dp, nền trong suốt |
| `ic_close_24.xml`, `ic_camera_24.xml`, `ic_gallery_24.xml`, `ic_chevron_left_24.xml`, `ic_chevron_right_24.xml`, `ic_delete_24.xml` | Vector Material Icons, `android:tint="@color/text_primary"` |

---

## 4. Lớp mạng: `data/api/TransactionApi.java` (file mới)

```java
public interface TransactionApi {
    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories(@Query("type") String type);   // type có thể null

    @GET("wallets")                                                                  // API của Thành viên 2
    Call<ApiResponse<List<WalletOption>>> getWallets();

    @Multipart
    @POST("transactions")
    Call<ApiResponse<Transaction>> createTransaction(
            @Part MultipartBody.Part image,                  // null nếu không có ảnh
            @Part("amount") RequestBody amount,
            @Part("wallet_id") RequestBody walletId,
            @Part("category_id") RequestBody categoryId,
            @Part("type") RequestBody type,
            @Part("note") RequestBody note,
            @Part("transaction_date") RequestBody transactionDate);

    @GET("transactions")
    Call<ApiResponse<List<Transaction>>> getTransactions(
            @Query("month") Integer month,
            @Query("year") Integer year,
            @Query("wallet_id") Integer walletId);           // null = không lọc

    @DELETE("transactions/{id}")
    Call<ApiResponse<DeleteTransactionResult>> deleteTransaction(@Path("id") int id);
}
```

- Lấy instance: `ApiClient.getClient(context).create(TransactionApi.class)`. Truyền `getApplicationContext()`, vì `ApiClient` giữ singleton.
- `RequestBody` dạng text dùng `MediaType.parse("text/plain")`.
- `MultipartBody.Part` của ảnh có tên field `"image"`, filename `"upload.jpg"`, `MediaType.parse("image/jpeg")`.
- Retrofit bỏ qua `@Part` có giá trị `null`. Không có ảnh thì truyền `image = null`; `note` rỗng thì vẫn gửi chuỗi rỗng.
- Khi Thành viên 2 merge `ApiService.java`, **không** gộp vội. Giữ `TransactionApi` để tránh xung đột; trưởng nhóm sẽ gộp sau nếu muốn.

---

## 5. Model (package `model/`, Phần 1 sở hữu)

Mọi field dùng `@SerializedName` khớp **chính xác** tên ở file 01. Có getter, không cần setter.

### 5.1. `model/Transaction.java`

| Field Java | JSON | Kiểu |
|---|---|---|
| `id` | `id` | `int` |
| `amount` | `amount` | `double` |
| `type` | `type` | `String` |
| `note` | `note` | `String` (nullable) |
| `imageUrl` | `image_url` | `String` (nullable) |
| `transactionDate` | `transaction_date` | `String` |
| `walletId` | `wallet_id` | `int` |
| `walletName` | `wallet_name` | `String` |
| `categoryId` | `category_id` | `int` |
| `categoryName` | `category_name` | `String` |
| `categoryIcon` | `category_icon` | `String` |
| `categoryColor` | `category_color` | `String` |

Hằng số và hàm tiện ích:
- `public static final String TYPE_EXPENSE = "expense"`, `TYPE_INCOME = "income"`
- `boolean isExpense()`
- `String getFullImageUrl()`: `null` nếu `imageUrl` rỗng; ngược lại trả `ApiClient.IMAGE_BASE_URL + imageUrl`
- `Date getParsedDate()`: parse `"yyyy-MM-dd HH:mm:ss"` bằng `SimpleDateFormat` với `Locale.US`; lỗi thì trả `null`
- `String getDayKey()`: `"yyyy-MM-dd"` (10 ký tự đầu của `transactionDate`), dùng để gom theo ngày cho lịch
- Cài `Serializable`, để truyền qua `Bundle` sang dialog chi tiết

### 5.2. `model/Category.java`

Field: `id`, `name`, `icon`, `colorHex` (`color_hex`), `type`, `system` (`is_system`, `boolean`).
Hàm `int getColorInt()` gọi `Color.parseColor(colorHex)`; lỗi parse thì trả `Color.GRAY`.

### 5.3. `model/DeleteTransactionResult.java`

Field: `id` (`int`), `walletId` (`wallet_id`, `int`), `walletBalance` (`wallet_balance`, `double`).

### 5.4. `ui/capture/WalletOption.java` (DTO riêng, **không** phải `model/Wallet.java`)

`model/Wallet.java` thuộc Thành viên 2. Phần 1 dùng DTO riêng để không phụ thuộc và không xung đột:

| Field | JSON | Kiểu |
|---|---|---|
| `id` | `id` | `int` |
| `name` | `name` | `String` |
| `balance` | `balance` | `double` |
| `type` | `type` | `String` |
| `isDefaultRaw` | `is_default` | `com.google.gson.JsonElement` |

`boolean isDefault()`: nếu là JSON boolean thì trả giá trị đó; nếu là số thì trả `!= 0`; `null` thì trả `false`. Lý do: MySQL `BOOL` có thể trả về `1/0`, còn Gson sẽ lỗi nếu ép số sang `boolean`.

---

## 6. Repository: `data/repository/TransactionRepository.java` (file mới)

Gói các lời gọi Retrofit và trả kết quả qua callback đơn giản, để ViewModel không đụng Retrofit trực tiếp.

Constructor: `TransactionRepository(Context context)`. Bên trong giữ `context.getApplicationContext()` để tạo `TransactionApi` và đọc chuỗi lỗi từ `strings_capture_home.xml`.

```java
public interface ResultCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}
```

Khai báo `ResultCallback` là interface lồng trong `TransactionRepository`.

Phương thức:
- `void getCategories(String type, ResultCallback<List<Category>> cb)`
- `void getWallets(ResultCallback<List<WalletOption>> cb)`
- `void getTransactions(int month, int year, ResultCallback<List<Transaction>> cb)`
- `void createTransaction(File imageFileOrNull, long amount, int walletId, int categoryId, String type, String note, String transactionDate, ResultCallback<Transaction> cb)`
- `void deleteTransaction(int id, ResultCallback<DeleteTransactionResult> cb)`

Xử lý lỗi thống nhất (một hàm private `handle(Response, cb)`):

| Tình huống | Chuỗi trả cho `onError` |
|---|---|
| HTTP 2xx, `success=true` | Gọi `onSuccess(body.getData())` |
| HTTP 2xx, `success=false` | `body.getMessage()` |
| HTTP lỗi có body JSON | Parse `errorBody()` bằng Gson thành `ApiResponse<Object>`, lấy `message` |
| HTTP 401 | `R.string.capture_error_unauthorized`: "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại" |
| `onFailure` (`IOException`) | `R.string.capture_error_network`: "Không kết nối được máy chủ. Kiểm tra mạng và thử lại" |
| Lỗi khác hoặc parse lỗi | `R.string.capture_error_unknown`: "Đã có lỗi xảy ra, vui lòng thử lại" |

Retrofit `enqueue` đã gọi callback trên main thread, nên không cần chuyển thread.

---

## 7. Tiện ích (package `utils/`, file mới)

### 7.1. `utils/CurrencyUtils.java`

> Thành viên 3 cũng sẽ cần class này. Phần 1 tạo trước. **Báo nhóm** để không ai tạo trùng.

- `String format(double amount)`: `"50.000 đ"`. Dùng `DecimalFormat("#,###")` với `DecimalFormatSymbols` có `groupingSeparator = '.'`; làm tròn `Math.round`.
- `String formatSigned(double amount, boolean isExpense)`: `"-50.000 đ"` hoặc `"+50.000 đ"`.
- `String formatShort(double amount)`: dùng cho ô lịch. `< 1.000` thì giữ nguyên; `< 1.000.000` thì `"50k"`; từ `1.000.000` trở lên thì `"1,2tr"` (1 chữ số thập phân, bỏ `,0`).
- `long parseDigits(String text)`: bỏ mọi ký tự không phải số, rồi parse. Chuỗi rỗng → `0`. Vượt `999_999_999_999` → trả đúng `999_999_999_999`.
- `String formatInput(long value)`: `"50.000"` (không có " đ"), dùng khi định dạng lại ô nhập.

### 7.2. `utils/DateUtils.java`

- `String toServer(Calendar c)`: `"yyyy-MM-dd HH:mm:ss"`
- `String toDisplay(String serverDate)`: `"22/10/2026 12:30"`
- `String toDisplayDay(String serverDate)`: `"22/10/2026"`
- `String monthTitle(int month, int year)`: `"Tháng 10/2026"`

Mọi `SimpleDateFormat` dùng `Locale.US` khi parse, và `TimeZone.getDefault()`.

### 7.3. `utils/ImageUtils.java`

`File compressForUpload(Context ctx, Uri source) throws IOException`, **chạy trên background thread**:

1. Đọc kích thước bằng `BitmapFactory.Options.inJustDecodeBounds = true`.
2. Tính `inSampleSize` (lũy thừa của 2) để cạnh dài nhất còn **≤ 2560px**, rồi decode.
3. Scale tiếp bằng `Bitmap.createScaledBitmap` để cạnh dài nhất **≤ 1280px**.
4. Đọc `ExifInterface(inputStream)`, xoay theo `ORIENTATION_ROTATE_90/180/270` bằng `Matrix`.
5. Nén `JPEG` chất lượng **80**. Nếu file > 1MB thì nén lại ở 70, rồi 60.
6. Ghi vào `ctx.getCacheDir()/uploads/upload_<timestamp>.jpg` (tạo thư mục nếu chưa có).
7. `recycle()` các bitmap trung gian. Trả về `File`.

`void clearOldUploads(Context ctx)`: xóa file trong `cache/uploads` và `cache/captures` cũ hơn 24 giờ. Gọi khi mở `CaptureExpenseActivity`.

`Uri createCaptureUri(Context ctx)`: tạo file rỗng `cache/captures/capture_<timestamp>.jpg`, trả `FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", file)`.

---

## 8. Test khi chưa có màn đăng nhập (chỉ trên máy dev, KHÔNG commit)

`LoginActivity` thuộc Thành viên 2 và có thể chưa xong. Để test Phần 1:

1. Lấy token ở backend: `node scripts/dev-token.js 1`.
2. **Tạm thời** thêm vào `CaptureExpenseActivity.onCreate` và `HomeFragment.onCreate`:
   ```java
   // DEV ONLY - KHONG COMMIT
   SharedPrefManager.getInstance(requireContext()).saveAuthData("<token>", 1, "Dev");
   ```
3. Trước khi commit, chạy `git diff` và xác nhận **không còn** dòng `DEV ONLY`.

Chạy trên điện thoại thật: tạm đổi `BASE_URL` / `IMAGE_BASE_URL` trong `ApiClient.java` sang IP LAN, và **không commit** thay đổi này.
