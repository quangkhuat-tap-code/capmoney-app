# PHẦN 4 · Database và API (hợp đồng chung Android ↔ Backend)

> Mọi API dùng khuôn `{ success, message, data }`, cần header `Authorization: Bearer <token>`. Thiếu hoặc sai token → `401` (middleware có sẵn).
> Tiền là **number**, ngày là chuỗi `yyyy-MM-dd HH:mm:ss` giờ local (giống Phần 1).

---

## 1. Thay đổi database

Chỉ **một** thay đổi: thêm cột ảnh đại diện cho bảng `users` (tính năng 4B-3).

### 1.1. File migration mới `migrations/phan4_users_avatar.sql`

Chạy được nhiều lần không lỗi (MySQL 8 không có `ADD COLUMN IF NOT EXISTS`, nên kiểm tra qua `information_schema`):

```sql
USE capmoney;

SET @has_col := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'capmoney' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'avatar_url'
);
SET @sql := IF(@has_col = 0,
  'ALTER TABLE users ADD COLUMN avatar_url VARCHAR(255) NULL AFTER password_hash',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

Chạy: `node scripts/run-sql.js migrations/phan4_users_avatar.sql`.

### 1.2. `database_init.sql`

Thêm dòng `avatar_url VARCHAR(255) NULL,` sau `password_hash` trong `CREATE TABLE users`. Máy mới cài chỉ cần `npm run db:init`; máy cũ chạy thêm file migration ở mục 1.1.

**Báo cả nhóm** khi merge: ai đã có DB cũ thì chạy migration, nếu không `GET /api/auth/profile` sẽ lỗi 500 (`Unknown column 'avatar_url'`).

---

## 2. API mới

### 2.1. `PUT /api/transactions/:id` · Sửa giao dịch (4A-1)

Body JSON (không multipart, **không** đổi ảnh, **không** đổi loại):

```json
{ "amount": 120000, "wallet_id": 2, "category_id": 1, "note": "Bún bò", "transaction_date": "2026-10-20 12:30:00" }
```

| Thứ tự kiểm tra | Điều kiện | Mã | `message` |
|---|---|---|---|
| 1 | `:id` không phải số nguyên dương | 400 | `Mã giao dịch không hợp lệ` |
| 2 | `amount` không phải số, ≤ 0 hoặc > 999.999.999.999 | 400 | `Số tiền phải lớn hơn 0` |
| 3 | `note` dài hơn 500 ký tự | 400 | `Ghi chú tối đa 500 ký tự` |
| 4 | `transaction_date` thiếu hoặc sai định dạng | 400 | `Ngày giao dịch không hợp lệ` |
| 5 | Giao dịch không có hoặc của người khác | 404 | `Giao dịch không tồn tại` |
| 6 | Ví không có hoặc của người khác | 404 | `Ví không tồn tại` |
| 7 | Danh mục không có / không dùng được | 404 | `Danh mục không tồn tại` |
| 8 | Loại danh mục khác loại giao dịch đang có | 400 | `Danh mục không khớp loại giao dịch` |
| 9 | Sau khi sửa, ví nào có số dư < 0 | 400 | `Số dư ví không đủ để thực hiện giao dịch` |

Thành công `200`, `message: "Đã cập nhật giao dịch"`, `data` = 1 giao dịch đúng khuôn `mapTransaction` của Phần 1.

**Cách tính số dư** (trong 1 DB transaction):

1. `SELECT ... FROM transactions WHERE id = ? AND user_id = ? FOR UPDATE` → giao dịch cũ.
2. Khoá các ví liên quan (ví cũ, ví mới, bỏ trùng) **theo thứ tự id tăng dần** bằng `SELECT id, balance FROM wallets WHERE id IN (?) AND user_id = ? ORDER BY id FOR UPDATE` (giống `transferMoney`, tránh deadlock).
3. Tính trên bộ nhớ: hoàn tác giao dịch cũ ở ví cũ (chi thì **cộng lại**, thu thì **trừ đi**), rồi áp giao dịch mới ở ví mới (chi thì trừ, thu thì cộng).
4. Ví nào ra số âm → rollback, lỗi dòng 9. Ví dụ: sửa khoản **thu** 5.000.000 xuống 1.000.000 nhưng ví đã tiêu hết → số dư âm → chặn.
5. `UPDATE wallets SET balance = ?` cho từng ví, `UPDATE transactions SET amount, wallet_id, category_id, note, transaction_date`.

### 2.2. `GET /api/transactions/search?q=&page=` · Tìm kiếm (4A-2)

| Tham số | Quy tắc |
|---|---|
| `q` | Bắt buộc, sau khi `trim` dài 1–50 ký tự. Tìm **không phân biệt hoa thường** trong `note` **hoặc** tên danh mục |
| `page` | Không truyền = 1. Số nguyên 1–1000 |

Sai tham số → `400 "Từ khoá tìm kiếm không hợp lệ"`.

Mỗi trang **20** giao dịch, mọi thời gian, mới nhất trước (`transaction_date DESC, id DESC`). Lấy 21 dòng để biết còn trang sau.

```json
{
  "success": true,
  "message": "Tìm thấy giao dịch",
  "data": { "items": [ /* giao dịch khuôn mapTransaction */ ], "page": 1, "has_more": true }
}
```

Ký tự `%`, `_`, `\` trong `q` phải được thoát trước khi ghép vào `LIKE` (`q.replace(/[\\%_]/g, '\\$&')`). Không có kết quả vẫn `200`, `items: []`.

Route phải khai báo **trước** mọi route `/:id` trong `transaction.routes.js`.

### 2.3. `GET /api/wallets/transfers?month=&year=` · Lịch sử chuyển tiền (4B-1)

Không truyền tháng/năm = tháng hiện tại. Sai → `400 "Tham số lọc không hợp lệ"` (dùng `parseMonthYear`).

```json
{
  "success": true,
  "message": "Lấy lịch sử chuyển tiền thành công",
  "data": [
    {
      "id": 7, "amount": 500000, "note": "Gửi tiết kiệm",
      "transfer_date": "2026-10-21 09:15:00",
      "from_wallet_id": 1, "from_wallet_name": "Tiền mặt",
      "to_wallet_id": 4, "to_wallet_name": "Sổ tiết kiệm"
    }
  ]
}
```

SQL: `wallet_transfers` `JOIN wallets fw ON fw.id = from_wallet_id JOIN wallets tw ON tw.id = to_wallet_id`, lọc `user_id` và khoảng tháng `[start, end)`, sắp `transfer_date DESC, id DESC`.

### 2.4. `PUT /api/auth/password` · Đổi mật khẩu (4B-2)

Body: `{ "current_password": "...", "new_password": "..." }`

| Điều kiện | Mã | `message` |
|---|---|---|
| Thiếu `current_password` | 400 | `Vui lòng nhập mật khẩu hiện tại` |
| `new_password` không dài 6–72 ký tự | 400 | `Mật khẩu phải từ 6 đến 72 ký tự` |
| `new_password` trùng `current_password` | 400 | `Mật khẩu mới phải khác mật khẩu hiện tại` |
| `current_password` sai | 400 | `Mật khẩu hiện tại không đúng` |

> Sai mật khẩu hiện tại trả **400** chứ không phải 401, vì 401 làm app tự đăng xuất.

Thành công `200 "Đã đổi mật khẩu"`, `data: null`. Token đang dùng **vẫn còn hạn** (JWT không lưu phiên, xem mục 2.2 Không làm của file 00).

### 2.5. `POST /api/auth/avatar` · Tải ảnh đại diện (4B-3)

`multipart/form-data`, field `image`, dùng lại `uploadSingleImage` (≤ 5MB, JPG/PNG/WEBP, lỗi giống Phần 1). Thiếu file → `400 "Vui lòng chọn ảnh"`.

Lưu `avatar_url = '/uploads/<tên file>'`, xoá file avatar cũ (nếu có) **sau** khi cập nhật DB thành công. Trả `200 "Đã cập nhật ảnh đại diện"`, `data` = user công khai **có thêm** `avatar_url`.

`toPublicUser(row)` thêm `avatar_url: row.avatar_url || null` → `login`, `register`, `GET /api/auth/profile` đều trả trường này. Các câu `SELECT` user phải lấy thêm cột `avatar_url`.

### 2.6. `GET /api/analytics/trend?month=&year=&months=6` · Xu hướng (4C-1)

Trả `months` tháng **kết thúc ở** tháng/năm truyền vào (mặc định tháng hiện tại), cũ nhất trước. `months` mặc định 6, cho phép 2–12. Sai → `400 "Tham số lọc không hợp lệ"`.

```json
{
  "success": true,
  "message": "Lấy xu hướng thành công",
  "data": [
    { "month": 5, "year": 2026, "total_expense": 0, "total_income": 0 },
    { "month": 10, "year": 2026, "total_expense": 3400000, "total_income": 15000000 }
  ]
}
```

(Ví dụ rút gọn; thực tế đủ 6 phần tử.) Tháng không có giao dịch vẫn có phần tử với số 0. SQL một câu:

```sql
SELECT YEAR(transaction_date) AS y, MONTH(transaction_date) AS m, type, SUM(amount) AS total
FROM transactions
WHERE user_id = ? AND transaction_date >= ? AND transaction_date < ?
GROUP BY y, m, type
```

rồi ghép vào mảng tháng dựng sẵn bằng JS.

---

## 3. API có sẵn được dùng thêm

| API | Dùng ở | Ghi chú |
|---|---|---|
| `GET /api/budgets?month=&year=` | 4C-2 | Sau khi lưu khoản chi, lấy ngân sách của tháng giao dịch để so ngưỡng |
| `GET /api/wallets` | 4C-3 | Kết quả thành công được lưu vào Room |
