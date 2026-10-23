# BE-1 · API đăng ký, đăng nhập

> **Phần 2** · Repo `backend-cap-money` · Nhánh `feature/wallets-auth` · Task trước: không có · Task sau: [BE-2](BE-2_Api_Thong_Tin_Ca_Nhan.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 3.1, 4.1 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 1, 2.1, 2.2

## Mục tiêu

Người dùng tạo được tài khoản và đăng nhập, nhận token JWT dùng được cho **mọi** API (kể cả API của Phần 1). Đăng ký xong tự có 1 ví "Tiền mặt" mặc định.

## File

| File | Việc |
|---|---|
| `src/controllers/auth.controller.js` | **Tạo mới**: `register`, `login` |
| `src/routes/auth.routes.js` | **Tạo mới** |
| `src/app.js` | **Thêm** mount `/api/auth` |

## Cách làm

1. Kéo code mới nhất và tạo nhánh nếu chưa có (Hướng dẫn, mục 3 và 4.1).
2. `auth.controller.js`:
   - Hàm phụ `signToken(userId)` → `jwt.sign({ id: userId }, process.env.JWT_SECRET, { expiresIn: '7d' })`.
   - Hàm phụ `toPublicUser(row)` → `{ id, full_name, email }`.
   - `register` đúng mục 2.1 của file 01: kiểm tra theo thứ tự, mã hoá `bcrypt.hash(password, 10)`, INSERT user + ví "Tiền mặt" trong **1 DB transaction**, bắt `ER_DUP_ENTRY` → 409.
   - `login` đúng mục 2.2: email so sánh sau `trim().toLowerCase()`, sai email hay sai mật khẩu đều `401 "Email hoặc mật khẩu không đúng"`.
   - **Không bao giờ** trả `password_hash` ra ngoài, không `console.log` mật khẩu.
3. `auth.routes.js`:

   ```js
   router.post('/register', register);
   router.post('/login', login);
   ```

4. `app.js` (chỉ thêm, không xoá gì của người khác):
   - Dưới các dòng `require` route đang có: `const authRoutes = require('./routes/auth.routes');`
   - Dưới các dòng `app.use` route đang có: `app.use('/api/auth', authRoutes);`
   - Xoá dòng comment giữ chỗ `// app.use('/api/auth', ...)`. Giữ nguyên các comment khác.

## Test (bắt buộc đạt trước khi commit)

Backend đang chạy `npm run dev`. Mở Git Bash ở thư mục backend:

```bash
API=http://localhost:5000/api
```

| Ca | Lệnh | Kỳ vọng |
|---|---|---|
| A1 | `curl -s -X POST $API/auth/register -H "Content-Type: application/json" -d '{"full_name":"Test A","email":"testa@example.test","password":"123456"}'` | `201`, có `token`, `user.id` |
| A2 | Gọi lại đúng lệnh A1 | `409 "Email đã được sử dụng"` |
| A3 | Đăng ký với `"email":"abc"` | `400 "Email không hợp lệ"` |
| A4 | Đăng ký với `"password":"123"` | `400 "Mật khẩu phải từ 6 đến 72 ký tự"` |
| A5 | Đăng ký với `"full_name":"   "` | `400 "Vui lòng nhập họ tên..."` |
| A6 | Sau A1, xem bảng `wallets` của user mới (MySQL Workbench) | Có đúng 1 ví `Tiền mặt`, `balance 0`, `type cash`, `is_default 1` |
| A7 | `curl -s -X POST $API/auth/login -H "Content-Type: application/json" -d '{"email":"TESTA@example.test","password":"123456"}'` | `200`, có token (email viết hoa vẫn đăng nhập được) |
| A8 | Login sai mật khẩu | `401 "Email hoặc mật khẩu không đúng"` |
| A9 | Login email không tồn tại | `401`, cùng message A8 |
| A10 | Login `dev@example.test` mật khẩu bất kỳ | `401` (không phải 500) |
| A13 | `TOKEN=<token ở A7>`; `curl -s $API/categories -H "Authorization: Bearer $TOKEN"` | `200` → token Phần 2 ký dùng được với API Phần 1 |

Dọn sau khi test (MySQL): `DELETE FROM users WHERE email LIKE '%@example.test' AND id <> 1;` (ví của user đó tự xoá theo).

## Commit

```text
feat: api dang ky dang nhap

- Them POST /api/auth/register: kiem tra du lieu, ma hoa mat khau bcrypt, tao vi Tien mat mac dinh trong 1 transaction
- Them POST /api/auth/login: tra token JWT han 7 ngay va thong tin user
- Mount /api/auth trong app.js
```

Không thêm dòng `Co-Authored-By` hay tên công cụ AI.

## Xong khi

- [ ] Đạt A1 → A10, A13.
- [ ] `git status` chỉ có 3 file ở bảng "File".
- [ ] Đã commit đúng message trên.
