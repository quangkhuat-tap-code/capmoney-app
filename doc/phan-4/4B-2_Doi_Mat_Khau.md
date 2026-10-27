# 4B-2 · Đổi mật khẩu

> **Phần 4B** · Trình bày: **Trung** · Code + test: Lâm Anh · Task trước: [4B-1](4B-1_Lich_Su_Chuyen_Tien.md) · Task sau: [4B-3](4B-3_Anh_Dai_Dien.md)
> **Đọc trước:** [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) mục 2.4

## Mục tiêu

Người dùng đổi được mật khẩu ngay trong app, phải nhập đúng mật khẩu cũ. Mật khẩu mới được mã hoá `bcrypt` giống lúc đăng ký (Phần 2, BE-1).

## Backend

| File | Việc |
|---|---|
| `src/controllers/auth.controller.js` | Thêm `changePassword`, export |
| `src/routes/auth.routes.js` | `router.put('/password', auth, changePassword);` |

1. Kiểm tra theo đúng thứ tự bảng mục 2.4 file 01. Độ dài mật khẩu dùng **cùng quy tắc** với `register` (6–72 ký tự, vì bcrypt chỉ đọc 72 byte đầu).
2. `SELECT password_hash FROM users WHERE id = ?` → không có user → `401` (token của user đã bị xoá).
3. `bcrypt.compare(current, hash)` bọc `try/catch` giống `login` → sai thì `400 "Mật khẩu hiện tại không đúng"`.
4. `bcrypt.hash(newPassword, 10)` (cùng số vòng với `register`, kiểm tra lại trong code) → `UPDATE users SET password_hash = ? WHERE id = ?`.
5. Trả `200 "Đã đổi mật khẩu"`, `data: null`. **Không** ghi mật khẩu ra log.

### Test backend

| Ca | Làm | Kỳ vọng |
|---|---|---|
| P1 | Đúng mật khẩu cũ, mới hợp lệ | `200`; `login` bằng mật khẩu mới được, mật khẩu cũ báo sai |
| P2 | Sai mật khẩu cũ | `400 "Mật khẩu hiện tại không đúng"` (**không** phải 401) |
| P3 | Mới 5 ký tự; mới 73 ký tự | `400 "Mật khẩu phải từ 6 đến 72 ký tự"` |
| P4 | Mới trùng cũ | `400 "Mật khẩu mới phải khác mật khẩu hiện tại"` |
| P5 | Thiếu `current_password` | `400 "Vui lòng nhập mật khẩu hiện tại"` |
| P6 | Không có token | `401` |
| P7 | Sau P1, gọi `GET /auth/profile` bằng **token cũ** | Vẫn `200` (đúng với thiết kế, ghi rõ khi bảo vệ) |

Test xong đổi mật khẩu của `demo@example.test` về như cũ.

**Commit backend:** `feat: api doi mat khau`.

## Android

| File | Việc |
|---|---|
| `data/api/AuthApi.java` | `@PUT("auth/password") Call<ApiResponse<Object>> changePassword(@Body Map<String, String> body);` |
| `data/repository/AuthRepository.java` | `changePassword(current, next, RepoCallback<Object>)` (dùng `wrap(..., needsToken = true)` có sẵn) |
| `ui/profile/ChangePasswordDialog.java` | **Tạo mới**, `BottomSheetDialogFragment` (giống form thêm ví của Phần 2) |
| `res/layout/dialog_change_password.xml` | 3 ô `TextInputLayout` kiểu filled như màn Đăng nhập, có nút con mắt |
| `ui/profile/ProfileFragment.java` + `fragment_profile.xml` | Nút viền `profile_btn_change_password` "Đổi mật khẩu" **trên** nút Đăng xuất |

### Hộp thoại

- 3 ô: Mật khẩu hiện tại, Mật khẩu mới, Nhập lại mật khẩu mới.
- Kiểm tra tại máy trước khi gọi API (lỗi hiện dưới ô, gõ lại thì xoá lỗi):
  - Ô trống → "Vui lòng nhập …".
  - Mới không dài 6–72 → "Mật khẩu phải từ 6 đến 72 ký tự".
  - Nhập lại khác mới → "Mật khẩu nhập lại không khớp".
- Đang gửi: nút Lưu thành vòng xoay, khoá các ô, không đóng được bằng bấm ra ngoài.
- Lỗi `400` từ server: "Mật khẩu hiện tại không đúng" hiện **dưới ô mật khẩu hiện tại**; lỗi khác hiện dưới ô cuối.
- Thành công: đóng hộp thoại, Toast "Đã đổi mật khẩu". Người dùng **vẫn đăng nhập**.
- Xoay màn hình: không mất chữ đã gõ (`TextInputEditText` có `id` thì tự giữ).

### Test Android

| Ca | Làm | Kỳ vọng |
|---|---|---|
| PA1 | Tab Cá nhân → Đổi mật khẩu → nhập đúng | Toast, vẫn ở Tab Cá nhân; đăng xuất rồi đăng nhập bằng mật khẩu mới được |
| PA2 | Sai mật khẩu hiện tại | Lỗi dưới ô hiện tại, hộp thoại không đóng |
| PA3 | Nhập lại không khớp | Lỗi tại máy, không gọi API |
| PA4 | Tắt backend | Báo lỗi mạng dưới ô cuối |
| PA5 | Xoay màn hình khi đang gõ | Giữ chữ |

**Commit app:** `feat: doi mat khau o tab ca nhan`.

## Xong khi

- [ ] Đạt P1 → P7, PA1 → PA5.
- [ ] Mật khẩu `demo@example.test` đã trả về như cũ.
