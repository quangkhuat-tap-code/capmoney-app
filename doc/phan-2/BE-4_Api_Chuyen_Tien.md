# BE-4 · API chuyển tiền giữa các ví

> **Phần 2** · Repo `backend-cap-money` · Nhánh `feature/wallets-auth` · Task trước: [BE-3](BE-3_Api_Danh_Sach_Them_Sua_Vi.md) · Task sau: [AN-1](AN-1_Nen_Tang_Model_Api.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 3.1 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 1, 2.7

## Mục tiêu

Chuyển tiền giữa 2 ví của cùng user, an toàn khi nhiều request chạy cùng lúc, lưu lịch sử vào `wallet_transfers`. **Xong task này thì push và tạo PR backend.**

## File

| File | Việc |
|---|---|
| `src/controllers/wallet.controller.js` | **Thêm** hàm `transferMoney` |
| `src/routes/wallet.routes.js` | **Thêm** route `/transfer` |

## Cách làm

1. Kéo code mới nhất (Hướng dẫn, mục 4.1).
2. `transferMoney`: kiểm tra 4 điều kiện đầu ở bảng mục 2.7 file 01, rồi trong **1 DB transaction**:

   ```js
   // Khoá 2 ví theo thứ tự id tăng dần để 2 lệnh chuyển ngược chiều chạy cùng lúc không bị deadlock
   const [rows] = await conn.query(
     'SELECT id, name, balance, type, is_default, created_at FROM wallets WHERE id IN (?, ?) AND user_id = ? ORDER BY id FOR UPDATE',
     [fromId, toId, req.user.id]
   );
   // rows.length !== 2               → rollback, 404 "Ví không tồn tại"
   // số dư ví nguồn < amount         → rollback, 400 "Số dư ví nguồn không đủ"
   // UPDATE wallets SET balance = balance - ? WHERE id = ?     (ví nguồn)
   // UPDATE wallets SET balance = balance + ? WHERE id = ?     (ví đích)
   // INSERT INTO wallet_transfers (user_id, from_wallet_id, to_wallet_id, amount, note, transfer_date) VALUES (?, ?, ?, ?, ?, NOW())
   // commit
   ```

   Sau commit `SELECT` lại 2 ví và dòng `wallet_transfers` vừa tạo, trả `201` đúng khuôn mục 2.7 (dùng `mapWallet` của BE-3).
3. Route — đặt **trước** `router.put('/:id', ...)`:

   ```js
   router.post('/transfer', auth, transferMoney);
   ```

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
| T1 | `curl -s -X POST $API/wallets/transfer -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"from_wallet_id":1,"to_wallet_id":2,"amount":500000,"note":"Rút ATM"}'` | `201`; ví 1 còn 1.500.000, ví 2 thành 5.500.000; bảng `wallet_transfers` có 1 dòng |
| T2 | Chuyển `from = to = 1` | `400 "Ví nguồn và ví đích phải khác nhau"` |
| T3 | Chuyển `amount: 99000000` | `400 "Số dư ví nguồn không đủ"`; 2 ví **không đổi** |
| T4 | Chuyển `amount: 0` và `amount: "abc"` | `400 "Số tiền phải lớn hơn 0"` |
| T5 | Chuyển sang id ví của user khác | `404 "Ví không tồn tại"`; không ví nào đổi |
| T6 | Sau T1 (ví 1 còn 1.500.000), gửi **cùng lúc** 2 lệnh chuyển 1.500.000 từ ví 1 (lệnh dưới) | Chỉ 1 lệnh `201`, lệnh kia `400`; ví 1 về 0, không âm |

Lệnh T6:

```bash
for i in 1 2; do curl -s -X POST $API/wallets/transfer -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"from_wallet_id":1,"to_wallet_id":2,"amount":1500000}' & done; wait
```

Dọn sau test: chạy lại `database_dev_seed.sql`; `DELETE FROM wallet_transfers WHERE user_id = 1;`.

## Commit

```text
feat: api chuyen tien giua cac vi

- Them POST /api/wallets/transfer: tru vi nguon, cong vi dich, luu wallet_transfers trong 1 transaction
- Khoa 2 vi theo thu tu id (FOR UPDATE) de tranh am so du va deadlock khi gui dong thoi
```

## Sau khi commit: push và tạo PR backend

1. Kiểm tra co-author (phải ra rỗng):

   ```bash
   git log origin/develop..HEAD --format=%B | grep -i -E "co-authored|signed-off|generated with"
   ```

2. `git push`.
3. Tạo PR `feature/wallets-auth` → `develop` trên repo `backend-cap-money`, tiêu đề `Phan 2: api dang nhap, ca nhan, vi va chuyen tien`. Mô tả: danh sách API, bảng kết quả A1–A13, W1–W10, T1–T6, và dòng "File dùng chung đã sửa: `src/app.js`".
4. Báo nhóm: đã có `/api/auth` và `/api/wallets`.

## Xong khi

- [ ] Đạt T1 → T6.
- [ ] Đã commit, push, tạo PR backend.
