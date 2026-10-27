# 4B-3 · Ảnh đại diện

> **Phần 4B** · Trình bày: **Trung** · Code + test: Lâm Anh · Task trước: [4B-2](4B-2_Doi_Mat_Khau.md) · Task sau: [4C-1](4C-1_Bieu_Do_6_Thang.md)
> **Đọc trước:** [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) mục 1, 2.5

## Mục tiêu

Tab Cá nhân đang hiện **chữ cái đầu** của tên trong vòng tròn. Task này cho người dùng chọn ảnh làm avatar. Đây là tính năng **duy nhất** của Phần 4 phải sửa cấu trúc DB.

## Backend

| File | Việc |
|---|---|
| `migrations/phan4_users_avatar.sql` | **Tạo mới**, nội dung mục 1.1 file 01 |
| `database_init.sql` | Thêm `avatar_url VARCHAR(255) NULL,` sau `password_hash`. **Commit riêng** cùng file migration |
| `src/controllers/auth.controller.js` | `toPublicUser` thêm `avatar_url`; câu `SELECT` ở `login` và `getProfile` lấy thêm `avatar_url`; thêm `uploadAvatar` |
| `src/routes/auth.routes.js` | `router.post('/avatar', auth, uploadSingleImage, uploadAvatar);` |

`uploadAvatar`:

```js
if (!req.file) return res.status(400).json({ success: false, message: 'Vui lòng chọn ảnh', data: null });
const newUrl = `/uploads/${req.file.filename}`;
try {
  const [rows] = await pool.query('SELECT id, full_name, email, avatar_url FROM users WHERE id = ?', [req.user.id]);
  if (rows.length === 0) { await removeFileSafe(req.file.path); return res.status(401)...; }
  const oldUrl = rows[0].avatar_url;
  await pool.query('UPDATE users SET avatar_url = ? WHERE id = ?', [newUrl, req.user.id]);
  if (oldUrl) await removeFileSafe(path.join(UPLOAD_DIR, path.basename(oldUrl)));   // xoá SAU khi DB đã cập nhật
  res.json({ success: true, message: 'Đã cập nhật ảnh đại diện', data: toPublicUser({ ...rows[0], avatar_url: newUrl }) });
} catch (err) { await removeFileSafe(req.file.path); next(err); }
```

`removeFileSafe` đang nằm trong `transaction.controller.js`: **chép** hàm nhỏ này sang `auth.controller.js` (không tách file chung để khỏi sửa code Phần 1). `path.basename` chặn đường dẫn kiểu `../`.

### Test backend

| Ca | Làm | Kỳ vọng |
|---|---|---|
| V1 | Chạy migration **2 lần** | Lần 2 không lỗi; `DESCRIBE users` có `avatar_url` |
| V2 | `GET /auth/profile` trước khi có ảnh | Có `avatar_url: null` |
| V3 | `POST /auth/avatar` kèm ảnh JPG | `200`, `avatar_url: "/uploads/img_…"`; mở `http://localhost:5000/uploads/…` thấy ảnh |
| V4 | Tải ảnh thứ 2 | File ảnh lần 1 **bị xoá** khỏi `public/uploads` |
| V5 | Không kèm file | `400 "Vui lòng chọn ảnh"` |
| V6 | File PDF; ảnh 6MB | `400 "Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP"`; `400 "Ảnh vượt quá 5MB"` |
| V7 | `POST /auth/login` | `data.user` có `avatar_url` |
| V8 | `npm run db:init` trên DB mới tinh | Bảng `users` có cột ngay |

**Commit backend:** `chore: them cot avatar_url cho bang users` (migration + `database_init.sql`) và `feat: api tai anh dai dien`.

## Android

| File | Việc |
|---|---|
| `model/User.java`, `model/UserProfile.java` | Thêm `@SerializedName("avatar_url") String avatarUrl` + getter |
| `data/api/AuthApi.java` | `@Multipart @POST("auth/avatar") Call<ApiResponse<User>> uploadAvatar(@Part MultipartBody.Part image);` |
| `data/repository/AuthRepository.java` | `uploadAvatar(File, RepoCallback<User>)` (giống cách Phần 1 tạo `MultipartBody.Part` cho ảnh giao dịch) |
| `ui/profile/ProfileFragment.java`, `ProfileViewModel.java` | Chọn ảnh, nén, tải lên, hiện ảnh |
| `res/layout/fragment_profile.xml` | Thêm `ImageView` `profile_iv_avatar` **chồng đúng lên** `profile_tv_avatar` (cùng kích thước, bo tròn), mặc định ẩn; thêm biểu tượng máy ảnh nhỏ góc dưới phải vòng tròn |

### Luồng

- Bấm vòng tròn avatar → `PickVisualMedia` (chỉ ảnh, **không** cần xin quyền).
- Có ảnh → chạy nền `ImageUtils.compressForUpload(ctx, uri)` (đã có, ≤ 1280px, đúng chiều) → tải lên. Trong lúc tải: vòng xoay đè lên avatar, không bấm được lần nữa.
- Thành công: Glide tải `ApiClient.IMAGE_BASE_URL + avatarUrl` với `circleCrop()`, hiện `profile_iv_avatar`, ẩn chữ cái. Toast "Đã cập nhật ảnh đại diện".
- `avatarUrl` null → giữ chữ cái đầu như cũ. Glide lỗi tải ảnh → cũng hiện chữ cái.
- Lỗi tải lên → Snackbar chữ lỗi của server (ví dụ "Ảnh vượt quá 5MB"). 401 → về màn Đăng nhập.
- Mở lại Tab Cá nhân / mở lại app: `GET /auth/profile` trả `avatar_url` → hiện ảnh.

### Test Android

| Ca | Làm | Kỳ vọng |
|---|---|---|
| VA1 | Tài khoản chưa có ảnh | Hiện chữ cái đầu như cũ |
| VA2 | Bấm avatar → chọn ảnh | Có vòng xoay, sau đó hiện ảnh tròn |
| VA3 | Thoát app, mở lại | Vẫn thấy ảnh |
| VA4 | Đổi ảnh khác | Ảnh mới; `public/uploads` chỉ còn file mới (file cũ đã xoá) |
| VA5 | Tắt backend → đổi ảnh | Snackbar lỗi mạng, giữ ảnh cũ |
| VA6 | Ảnh chụp dọc từ điện thoại thật | Hiện đúng chiều |

**Commit app:** `feat: doi anh dai dien o tab ca nhan`.

## Xong khi

- [ ] Đạt V1 → V8, VA1 → VA6.
- [ ] Đã báo nhóm chạy migration (ghi trong PR 4B và `Tien_Do_Nhom.md` mục "Việc còn lại").
