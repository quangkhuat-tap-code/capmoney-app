# AN-1 · Nền tảng: model, Retrofit, repository, SessionManager, chữ hiển thị

> **Phần 2** · Repo `capmoney-app` · Nhánh `feature/wallets-auth` · Task trước: [BE-4](BE-4_Api_Chuyen_Tien.md) · Task sau: [AN-2](AN-2_Man_Dang_Nhap_Dang_Ky.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 3.2, 4, 5 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 2 (khuôn JSON)

## Mục tiêu

Tạo lớp dữ liệu dùng chung cho mọi màn Phần 2. Task này **chưa có màn hình**, chỉ cần build được.

**Điều kiện:** `develop` của app đã merge Phần 1 (xem 00, mục 3.2). Build `develop` lỗi thì dừng, báo Thành viên 1.

## File

```text
model/User.java, AuthResult.java, UserProfile.java, Wallet.java, TransferResult.java
data/api/AuthApi.java, WalletApi.java
data/repository/RepoCallback.java, AuthRepository.java, WalletRepository.java
ui/auth/SessionManager.java
res/values/strings_auth_wallet_profile.xml
```

(Đường dẫn Java tính từ `app/src/main/java/com/example/comgroupcapmoney/`.)

## 1. Model (`model/`, có `@SerializedName` đúng tên JSON)

| Class | Field (tên JSON) | Ghi chú |
|---|---|---|
| `User` | `id` int, `full_name` String, `email` String | Getter `getId()`, `getFullName()`, `getEmail()` |
| `AuthResult` | `token` String, `user` User | `data` của login/register |
| `UserProfile` | `id`, `full_name`, `email`, `created_at` String, `total_transactions` int, `total_income` double, `total_expense` double | |
| `Wallet` | `id` int, `name` String, `balance` double, `type` String, `is_default` (**`JsonElement`**), `created_at` String | `implements Serializable`. `isDefault()` đọc được cả `true/false` lẫn `1/0` (chép cách làm của `WalletOption.isDefault()` ở Phần 1). `isSaving()` = `"saving".equals(type)`. `getTypeEmoji()`: `cash` → `💵`, `bank` → `🏦`, `saving` → `🐷`, khác → `👛` |
| `TransferResult` | `transfer` (lớp con tĩnh, chỉ cần `id`, `amount`), `from_wallet` Wallet, `to_wallet` Wallet | |

Lưu ý: `JsonElement` **không** `Serializable`. Trong `Wallet`, đánh dấu field này `transient` và thêm field `boolean defaultFlag` được gán trong `isDefault()` lần đầu; hoặc đơn giản hơn: tự viết `isDefault()` và lưu kết quả vào field `boolean` thường khi parse xong. Cách nào cũng được, miễn truyền `Wallet` qua `Bundle` không crash.

## 2. Retrofit

```java
public interface AuthApi {
    @POST("auth/register") Call<ApiResponse<AuthResult>> register(@Body Map<String, Object> body);
    @POST("auth/login")    Call<ApiResponse<AuthResult>> login(@Body Map<String, Object> body);
    @GET("auth/profile")   Call<ApiResponse<UserProfile>> getProfile();
}

public interface WalletApi {
    @GET("wallets")              Call<ApiResponse<List<Wallet>>> getWallets();
    @POST("wallets")             Call<ApiResponse<Wallet>> createWallet(@Body Map<String, Object> body);
    @PUT("wallets/{id}")         Call<ApiResponse<Wallet>> updateWallet(@Path("id") int id, @Body Map<String, Object> body);
    @POST("wallets/transfer")    Call<ApiResponse<TransferResult>> transfer(@Body Map<String, Object> body);
}
```

Lấy instance: `ApiClient.getClient(context).create(AuthApi.class)`. **Không** sửa `ApiClient`.

## 3. Repository

```java
public interface RepoCallback<T> {
    void onSuccess(T data, String message);
    void onError(String message, boolean unauthorized);   // unauthorized = true khi HTTP 401 ở API cần token
}
```

`AuthRepository` (`register`, `login`, `getProfile`) và `WalletRepository` (`getWallets`, `createWallet`, `updateWallet`, `transfer`):
- Dùng `enqueue`. `response.isSuccessful()` và `body().isSuccess()` → `onSuccess(body.getData(), body.getMessage())`.
- Response lỗi (4xx/5xx): đọc `response.errorBody().string()`, parse bằng Gson thành `ApiResponse<Object>`, lấy `message` của server. Parse lỗi → chuỗi `common_error_unknown`.
- HTTP 401 ở `getProfile` và mọi API ví → `onError(message, true)`. 401 ở `login` là **sai mật khẩu**, không phải hết phiên → `onError(message, false)`.
- `onFailure` (mất mạng, server tắt) → `onError(getString(R.string.common_error_network), false)`.
- Giữ `Context` bằng `context.getApplicationContext()`.

## 4. `ui/auth/SessionManager.java`

```java
public final class SessionManager {
    private SessionManager() {}

    // Gọi khi bấm Đăng xuất, hoặc khi API báo 401 (token hết hạn)
    public static void logout(Activity activity, boolean expired) {
        SharedPrefManager.getInstance(activity).clear();
        if (expired) Toast.makeText(activity, R.string.auth_session_expired, Toast.LENGTH_LONG).show();
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
```

`LoginActivity` chưa có ở task này → tạo trước 1 class rỗng `ui/auth/LoginActivity.java` (`extends AppCompatActivity`) để build được; AN-2 sẽ viết tiếp.

## 5. Chữ hiển thị (`res/values/strings_auth_wallet_profile.xml`)

Mỗi dòng: `tên  nội dung`. Viết thành `<string name="tên">nội dung</string>`. Có `%1$s` thì giữ nguyên.

```text
common_error_network        Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại
common_error_unknown        Có lỗi xảy ra, vui lòng thử lại
common_cancel               Huỷ
common_save                 Lưu
common_retry                Thử lại

auth_login_title            Đăng nhập
auth_login_subtitle         Ghi lại chi tiêu bằng một tấm ảnh
auth_register_title         Tạo tài khoản
auth_hint_full_name         Họ và tên
auth_hint_email             Email
auth_hint_password          Mật khẩu
auth_hint_confirm_password  Nhập lại mật khẩu
auth_btn_login              Đăng nhập
auth_btn_register           Đăng ký
auth_go_register            Chưa có tài khoản? Đăng ký
auth_go_login               Đã có tài khoản? Đăng nhập
auth_error_full_name        Vui lòng nhập họ tên
auth_error_email            Email không hợp lệ
auth_error_password         Mật khẩu phải từ 6 đến 72 ký tự
auth_error_confirm          Mật khẩu nhập lại không khớp
auth_session_expired        Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại

wallet_title                Tài khoản
wallet_total_assets         Tổng tài sản
wallet_count                %1$d ví
wallet_section_payment      Ví thanh toán
wallet_section_saving       Sổ tiết kiệm
wallet_empty_saving         Chưa có sổ tiết kiệm
wallet_badge_default        Mặc định
wallet_btn_add              + Thêm ví mới
wallet_btn_transfer         Chuyển tiền giữa các ví
wallet_form_add_title       Thêm ví mới
wallet_form_edit_title      Sửa ví
wallet_hint_name            Tên ví
wallet_hint_balance         Số dư
wallet_type_cash            Tiền mặt
wallet_type_bank            Ngân hàng
wallet_type_saving          Tiết kiệm
wallet_error_name           Vui lòng nhập tên ví
wallet_confirm_balance_title   Điều chỉnh số dư?
wallet_confirm_balance_msg     Số dư sẽ đổi từ %1$s thành %2$s. Thay đổi này không tạo giao dịch.
wallet_created              Đã thêm ví
wallet_updated              Đã cập nhật ví

transfer_title              Chuyển tiền
transfer_from               Từ ví
transfer_to                 Đến ví
transfer_hint_amount        Số tiền
transfer_hint_note          Ghi chú (không bắt buộc)
transfer_available          Số dư khả dụng: %1$s
transfer_btn                Chuyển
transfer_need_two_wallets   Cần ít nhất 2 ví để chuyển tiền
transfer_error_same         Ví nguồn và ví đích phải khác nhau
transfer_error_amount       Số tiền phải lớn hơn 0
transfer_error_balance      Số dư ví nguồn không đủ
transfer_success            Đã chuyển %1$s

profile_title               Cá nhân
profile_stat_transactions   Giao dịch
profile_stat_income         Tổng thu
profile_stat_expense        Tổng chi
profile_member_since        Thành viên từ %1$s
profile_btn_logout          Đăng xuất
profile_logout_confirm_title   Đăng xuất?
profile_logout_confirm_msg     Bạn sẽ cần đăng nhập lại để dùng ứng dụng.
```

## Test

```bash
./gradlew :app:assembleDebug
```

Phải thành công. App chạy lên vẫn như cũ (chưa có màn mới).

## Commit

```text
feat: model, api va repository dang nhap va vi

- Them model User, AuthResult, UserProfile, Wallet, TransferResult
- Them AuthApi, WalletApi va AuthRepository, WalletRepository doc message loi tu server, bao rieng loi 401
- Them SessionManager xoa token va quay ve man dang nhap
- Them strings_auth_wallet_profile.xml
```

## Xong khi

- [ ] Build thành công.
- [ ] Không sửa file nào ngoài danh sách (00, mục 4).
- [ ] Đã commit, không có dòng co-author.
