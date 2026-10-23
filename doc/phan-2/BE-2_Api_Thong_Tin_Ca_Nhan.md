# BE-2 · API thông tin cá nhân

> **Phần 2** · Repo `backend-cap-money` · Nhánh `feature/wallets-auth` · Task trước: [BE-1](BE-1_Api_Dang_Ky_Dang_Nhap.md) · Task sau: [BE-3](BE-3_Api_Danh_Sach_Them_Sua_Vi.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 3.1 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 2.3

## Mục tiêu

Trả thông tin user đang đăng nhập kèm tổng số giao dịch, tổng thu, tổng chi **toàn thời gian**, để Tab Cá nhân (AN-6) hiển thị.

## File

| File | Việc |
|---|---|
| `src/controllers/auth.controller.js` | **Thêm** hàm `getProfile` |
| `src/routes/auth.routes.js` | **Thêm** route |

## Cách làm

1. Kéo code mới nhất (Hướng dẫn, mục 4.1).
2. `getProfile` chạy đúng câu SQL ở mục 2.3 file 01 với `req.user.id`:
   - Không có dòng → `404 "Người dùng không tồn tại"`.
   - Ép `total_transactions`, `total_income`, `total_expense` về **số** bằng `Number(...)`.
3. Route (có middleware `auth`):

   ```js
   const auth = require('../middlewares/auth.middleware');
   router.get('/profile', auth, getProfile);
   ```

## Test

```bash
API=http://localhost:5000/api
```

Đăng ký 1 user test như ca A1 của BE-1, lấy `token` trong kết quả:

| Ca | Lệnh | Kỳ vọng |
|---|---|---|
| A11 | `TOKEN=<token>`; `curl -s $API/auth/profile -H "Authorization: Bearer $TOKEN"` | `200`, đúng tên + email, `total_transactions: 0`, `total_income: 0`, `total_expense: 0` (là **số**, không phải `"0"`) |
| A11b | `TOKEN=$(npm run -s dev:token)` rồi gọi lại | `200`, thông tin user 1; nếu user 1 có giao dịch thì các tổng khớp với dữ liệu |
| A12 | `curl -s $API/auth/profile` (không token) | `401` |

## Commit

```text
feat: api thong tin ca nhan

- Them GET /api/auth/profile: tra ho ten, email, ngay tao va tong so giao dich, tong thu, tong chi toan thoi gian
```

## Xong khi

- [ ] Đạt A11, A11b, A12.
- [ ] Đã commit đúng message trên, không có dòng co-author.
