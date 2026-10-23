# AN-2 · Màn Đăng nhập, Đăng ký

> **Phần 2** · Repo `capmoney-app` · Nhánh `feature/wallets-auth` · Task trước: [AN-1](AN-1_Nen_Tang_Model_Api.md) · Task sau: [AN-3](AN-3_Doi_Man_Mo_Dau.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 5 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 2.1, 2.2

## Mục tiêu

Có 2 màn `LoginActivity`, `RegisterActivity` hoạt động đầy đủ. Task này **chưa** đổi màn mở app (để ở AN-3).

## File

| File | Việc |
|---|---|
| `ui/auth/LoginActivity.java` | Viết tiếp (AN-1 đã tạo class rỗng) |
| `ui/auth/RegisterActivity.java` | Tạo mới |
| `ui/auth/AuthViewModel.java` | Tạo mới: gọi `AuthRepository`, giữ `loading`, kết quả, lỗi |
| `res/layout/activity_login.xml`, `activity_register.xml` | Tạo mới |
| `res/drawable/bg_auth_input.xml`, `bg_auth_button.xml` | Tạo mới |
| `AndroidManifest.xml` | **Thêm** khai báo 2 activity, **không** có `intent-filter` |

## 1. `LoginActivity`

Bố cục `activity_login.xml` (bọc `ScrollView` để bàn phím không che nút):
1. Tiêu đề lớn "CapMoney" (28sp, đậm) + `auth_login_subtitle` (`text_secondary`).
2. Ô Email (`inputType="textEmailAddress"`), ô Mật khẩu (`inputType="textPassword"`). Dùng `TextInputLayout` của Material; mật khẩu có nút hiện/ẩn (`app:endIconMode="password_toggle"`).
3. Nút `auth_btn_login`, kèm `ProgressBar` nhỏ hiện khi đang gọi API.
4. Dòng chữ bấm được `auth_go_register` → mở `RegisterActivity`.

Hành vi:
- `onCreate`, **trước** `setContentView`: nếu `SharedPrefManager.getInstance(this).isLoggedIn()` → mở `MainActivity`, `finish()`, `return` (tự đăng nhập).
- Bấm Đăng nhập: kiểm tra tại máy — email đúng `android.util.Patterns.EMAIL_ADDRESS`, mật khẩu 6–72 ký tự. Sai thì hiện lỗi ngay dưới ô (`TextInputLayout.setError`), **không** gọi API.
- Đang gọi API: khoá nút, khoá 2 ô, hiện ProgressBar. Bấm nhiều lần chỉ gửi 1 request.
- Thành công: `SharedPrefManager.getInstance(this).saveAuthData(token, user.getId(), user.getFullName())` → mở `MainActivity` với `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.
- Lỗi: hiện `message` của server bằng `Snackbar`, mở khoá form, giữ nguyên chữ đã nhập.

## 2. `RegisterActivity`

Bố cục giống Login, thêm ô **Họ tên** (đầu tiên) và ô **Nhập lại mật khẩu** (cuối). Có dòng `auth_go_login` → `finish()` (quay về Login).

- Kiểm tra tại máy: họ tên không rỗng (≤ 100), email hợp lệ, mật khẩu 6–72, nhập lại khớp (`auth_error_confirm`).
- Thành công → giống Login: lưu token, vào `MainActivity`, xoá back stack.
- Server trả `"Email đã được sử dụng"` → hiện lỗi dưới ô Email.

## 3. ViewModel

- `AuthViewModel` có `LiveData<Boolean> loading`, `LiveData<AuthResult> success`, sự kiện lỗi dùng 1 lần (dùng `utils/SingleEvent` của Phần 1 nếu tiện, chỉ dùng không sửa).
- Xoay màn hình: chữ đã nhập còn nguyên, đang gọi API thì không gọi lại.

## 4. Manifest (chỉ thêm 2 khối này, **chưa** đổi LAUNCHER)

```xml
<activity
    android:name=".ui.auth.LoginActivity"
    android:exported="false"
    android:screenOrientation="portrait"
    android:windowSoftInputMode="adjustResize" />

<activity
    android:name=".ui.auth.RegisterActivity"
    android:exported="false"
    android:screenOrientation="portrait"
    android:windowSoftInputMode="adjustResize" />
```

## Test

Để mở được màn Login khi test: **tạm** chuyển khối `<intent-filter>` LAUNCHER từ `MainActivity` sang `LoginActivity` (và `exported="true"`) trên máy. Test xong **trả lại như cũ**, không commit phần tạm này.

Backend đang chạy, đã có BE-1.

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| L2 | Bấm Đăng nhập khi để trống | Lỗi dưới ô, không gọi API (xem Logcat không có request) |
| L3 | Nhập sai mật khẩu | "Email hoặc mật khẩu không đúng", chữ đã nhập còn nguyên |
| L4 | Tắt backend rồi bấm Đăng nhập | Lỗi mạng, không crash |
| L5a | Đăng nhập đúng | Vào `MainActivity` |
| L7 | Đang nhập dở, xoay màn hình | Chữ còn nguyên |
| L8 | Bấm Đăng nhập liên tục 5 lần | Chỉ 1 request (log OkHttp trong Logcat) |
| R1 | Đăng ký, nhập lại mật khẩu không khớp | Lỗi dưới ô nhập lại |
| R2 | Đăng ký email đã có | "Email đã được sử dụng" dưới ô Email |
| R3a | Đăng ký thành công | Vào `MainActivity` |

## Commit

Trước khi commit: `git diff AndroidManifest.xml` chỉ có 2 khối khai báo ở mục 4, **không** có thay đổi LAUNCHER.

```text
feat: man hinh dang nhap dang ky

- Them LoginActivity: kiem tra email mat khau tai may, goi /api/auth/login, luu token, tu dang nhap neu da co token
- Them RegisterActivity: ho ten, email, mat khau, nhap lai mat khau, goi /api/auth/register
- Them AuthViewModel giu trang thai khi xoay man hinh
- Khai bao LoginActivity, RegisterActivity trong AndroidManifest
```

## Xong khi

- [ ] Đạt L2, L3, L4, L5a, L7, L8, R1, R2, R3a.
- [ ] Manifest đã trả LAUNCHER về `MainActivity` trước khi commit.
- [ ] Đã commit, không có dòng co-author.
