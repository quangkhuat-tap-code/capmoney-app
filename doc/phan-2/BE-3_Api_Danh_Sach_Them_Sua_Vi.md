# BE-3 · API danh sách, thêm, sửa ví

> **Phần 2** · Repo `backend-cap-money` · Nhánh `feature/wallets-auth` · Task trước: [BE-2](BE-2_Api_Thong_Tin_Ca_Nhan.md) · Task sau: [BE-4](BE-4_Api_Chuyen_Tien.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 3.1 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 1, 2.4, 2.5, 2.6

## Mục tiêu

Có `GET /api/wallets` (màn chụp ảnh của Phần 1 **đang chờ**), `POST /api/wallets`, `PUT /api/wallets/:id`.

## File

| File | Việc |
|---|---|
| `src/controllers/wallet.controller.js` | **Tạo mới**: `getWallets`, `createWallet`, `updateWallet` |
| `src/routes/wallet.routes.js` | **Tạo mới** |
| `src/app.js` | **Thêm** mount `/api/wallets` |

## Cách làm

1. Kéo code mới nhất (Hướng dẫn, mục 4.1).
2. `wallet.controller.js`:
   - Hàm phụ `mapWallet(row)` → `{ id, name, balance: Number(row.balance), type, is_default: !!row.is_default, created_at }`. Dùng ở **mọi** API ví (kể cả BE-4).
   - Hàm phụ `parseAmount(value)` → số đã làm tròn 2 chữ số (`Math.round(x * 100) / 100`), hoặc `null` nếu không phải số, < 0, > 999999999999.
   - `getWallets` theo mục 2.4: sắp `is_default DESC, id ASC`.
   - `createWallet` theo mục 2.5: kiểm tra tên, loại, số dư; trùng tên (không phân biệt hoa thường) → 409; ví mới `is_default = 0`; trả object ví vừa tạo (`SELECT` lại theo `insertId`).
   - `updateWallet` theo mục 2.6: chỉ `UPDATE` field có gửi; trùng tên ví **khác** (`AND id <> ?`) → 409; không cho đổi `is_default`.
   - Mọi câu SQL lọc `user_id = req.user.id`.
3. `wallet.routes.js` (mọi route có `auth`):

   ```js
   router.get('/', auth, getWallets);
   router.post('/', auth, createWallet);
   router.put('/:id', auth, updateWallet);
   ```

4. `app.js`: thêm `const walletRoutes = require('./routes/wallet.routes');` và `app.use('/api/wallets', walletRoutes);` cạnh dòng của `authRoutes`. Xoá dòng comment giữ chỗ `// app.use('/api/wallets', ...)`.

## Test

```bash
node scripts/run-sql.js database_dev_seed.sql
```

```bash
API=http://localhost:5000/api
```

```bash
TOKEN=$(npm run -s dev:token)
```

| Ca | Lệnh | Kỳ vọng |
|---|---|---|
| W1 | `curl -s $API/wallets -H "Authorization: Bearer $TOKEN"` | `200`, 2 ví, ví `is_default: true` đứng đầu, `balance` là số |
| W2 | `curl -s -X POST $API/wallets -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Tiết kiệm","type":"saving","balance":1000000}'` | `201`, `is_default: false` |
| W3 | Gọi lại W2 với `"name":"tiết kiệm"` | `409 "Tên ví đã tồn tại"` |
| W4 | POST với `"type":"crypto"` | `400 "Loại ví không hợp lệ"` |
| W5 | POST với `"balance":-5` | `400 "Số dư không hợp lệ"` |
| W6 | `curl -s -X PUT $API/wallets/2 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Vietcombank"}'` | `200`, tên đổi, số dư giữ nguyên |
| W7 | PUT `/wallets/2` với body `{}` | `400 "Không có thông tin cần cập nhật"` |
| W8 | PUT `/wallets/999999` với `{"name":"x"}` | `404 "Ví không tồn tại"` |
| W9 | Đăng ký 1 user khác (ca A1 của BE-1), lấy id ví của user đó trong DB, gọi PUT ví đó bằng token user 1 | `404` |
| W10 | `curl -s $API/wallets` (không token) | `401` |

Dọn sau test: `DELETE FROM wallets WHERE user_id = 1 AND id > 2;` rồi chạy lại `database_dev_seed.sql`; xoá user test như ở BE-1.

## Commit

```text
feat: api danh sach them sua vi

- Them GET /api/wallets: danh sach vi cua user, vi mac dinh dung dau, is_default tra boolean
- Them POST /api/wallets: tao vi moi, kiem tra ten trung, loai vi, so du
- Them PUT /api/wallets/:id: sua ten, loai, so du cua vi
- Mount /api/wallets trong app.js
```

## Xong khi

- [ ] Đạt W1 → W10.
- [ ] Đã commit, không có dòng co-author.
- [ ] Nhắn Thành viên 1: "Đã có `GET /api/wallets` trên nhánh `feature/wallets-auth`, đúng khuôn file 01 mục 2.4."
