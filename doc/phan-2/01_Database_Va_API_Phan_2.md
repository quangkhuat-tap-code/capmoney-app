# PHẦN 2 · Database và hợp đồng API

> Thuộc bộ tài liệu Phần 2. Đọc [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) trước.
> File này là **hợp đồng chung**: Backend trả đúng như đây, Android đọc đúng như đây. Muốn đổi field/tên/kiểu thì sửa file này trước, rồi sửa code cả 2 phía.

---

## 1. Database (đã có sẵn, KHÔNG sửa `database_init.sql`)

```sql
CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,                                   -- FK users(id) ON DELETE CASCADE
  name       VARCHAR(100) NOT NULL,
  balance    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  type       ENUM('cash', 'bank', 'saving') NOT NULL DEFAULT 'cash',
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallet_transfers (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  user_id        INT NOT NULL,
  from_wallet_id INT NOT NULL,                               -- FK wallets(id)
  to_wallet_id   INT NOT NULL,                               -- FK wallets(id)
  amount         DECIMAL(15,2) NOT NULL,
  note           TEXT NULL,
  transfer_date  DATETIME NOT NULL,
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Bảng `transactions` (Phần 1 sở hữu) — Phần 2 chỉ **đọc** để tính tổng ở `GET /api/auth/profile`:
`transactions(id, user_id, wallet_id, category_id, amount DECIMAL(15,2) > 0, type ENUM('expense','income'), transaction_date DATETIME, ...)`.

Quy ước:
- Mỗi user có **đúng 1** ví `is_default = 1` (ví "Tiền mặt" tạo lúc đăng ký). Màn chụp ảnh của Phần 1 chọn sẵn ví này.
- `type`: `cash` = Tiền mặt, `bank` = Ngân hàng, `saving` = Sổ tiết kiệm.
- Số dư không được âm.

---

## 2. API

Mọi response đúng khuôn `{ success, message, data }`. Tiền là **số**. Ngày giờ là chuỗi `yyyy-MM-dd HH:mm:ss`.
Lỗi không lường trước → `500 "Lỗi máy chủ"` (đã có sẵn trong `app.js`).

### 2.1. `POST /api/auth/register` (không cần token)

Body JSON:

```json
{ "full_name": "Nguyễn Văn A", "email": "a@gmail.com", "password": "123456" }
```

Kiểm tra theo **đúng thứ tự**, gặp lỗi đầu tiên thì trả luôn:

| # | Điều kiện | HTTP | message |
|---|---|---|---|
| 1 | `full_name` sau `trim()` rỗng hoặc dài hơn 100 ký tự | 400 | `Vui lòng nhập họ tên (tối đa 100 ký tự)` |
| 2 | `email` sau `trim().toLowerCase()` không khớp regex `^[^\s@]+@[^\s@]+\.[^\s@]+$` hoặc dài hơn 150 | 400 | `Email không hợp lệ` |
| 3 | `password` không phải chuỗi, ngắn hơn 6 hoặc dài hơn 72 ký tự | 400 | `Mật khẩu phải từ 6 đến 72 ký tự` |
| 4 | Email đã có trong `users` | 409 | `Email đã được sử dụng` |

Thành công: trong **1 DB transaction**:
1. `INSERT INTO users (full_name, email, password_hash)` với `password_hash = await bcrypt.hash(password, 10)`.
2. `INSERT INTO wallets (user_id, name, balance, type, is_default) VALUES (?, 'Tiền mặt', 0, 'cash', 1)`.
3. Commit, rồi ký token `jwt.sign({ id: userId }, process.env.JWT_SECRET, { expiresIn: '7d' })`.

INSERT users báo lỗi `ER_DUP_ENTRY` (2 người đăng ký cùng lúc cùng email) → rollback, trả `409 "Email đã được sử dụng"`.

`201`:

```json
{ "success": true, "message": "Đăng ký thành công", "data": {
  "token": "eyJhbGciOi...",
  "user": { "id": 5, "full_name": "Nguyễn Văn A", "email": "a@gmail.com" }
} }
```

### 2.2. `POST /api/auth/login` (không cần token)

Body JSON: `{ "email": "a@gmail.com", "password": "123456" }`

| Điều kiện | HTTP | message |
|---|---|---|
| Thiếu `email` hoặc `password` (sau trim rỗng) | 400 | `Vui lòng nhập email và mật khẩu` |
| Không có email (so sánh sau `trim().toLowerCase()`) hoặc `bcrypt.compare` sai | 401 | `Email hoặc mật khẩu không đúng` |

- Không phân biệt "sai email" với "sai mật khẩu" (để không lộ email nào đã đăng ký).
- `bcrypt.compare` với hash không hợp lệ (như user seed `not-a-real-hash`) phải ra 401, không ra 500. Bọc `try/catch` cho chắc.

`200`: giống hệt `data` của đăng ký, `message: "Đăng nhập thành công"`.

### 2.3. `GET /api/auth/profile` (cần token)

```sql
SELECT u.id, u.full_name, u.email, u.created_at,
       COUNT(t.id)                                                        AS total_transactions,
       COALESCE(SUM(CASE WHEN t.type = 'income'  THEN t.amount END), 0)   AS total_income,
       COALESCE(SUM(CASE WHEN t.type = 'expense' THEN t.amount END), 0)   AS total_expense
FROM users u
LEFT JOIN transactions t ON t.user_id = u.id
WHERE u.id = ?
GROUP BY u.id
```

Không có dòng nào → `404 "Người dùng không tồn tại"`.

`200`:

```json
{ "success": true, "message": "Lấy thông tin cá nhân thành công", "data": {
  "id": 1, "full_name": "Dev Tester", "email": "dev@example.test", "created_at": "2026-10-22 10:09:00",
  "total_transactions": 12, "total_income": 8000000, "total_expense": 1350000
} }
```

`total_*` là **số** (ép `Number(...)`), tính trên **toàn bộ** giao dịch, không lọc tháng.

### 2.4. `GET /api/wallets` (cần token) — **Phần 1 đang chờ API này**

```sql
SELECT id, name, balance, type, is_default, created_at
FROM wallets WHERE user_id = ?
ORDER BY is_default DESC, id ASC
```

`200`:

```json
{ "success": true, "message": "Lấy danh sách ví thành công", "data": [
  { "id": 1, "name": "Tiền mặt",  "balance": 2000000, "type": "cash", "is_default": true,  "created_at": "2026-10-22 10:09:00" },
  { "id": 2, "name": "Ngân hàng", "balance": 5000000, "type": "bank", "is_default": false, "created_at": "2026-10-22 10:09:00" }
] }
```

- **Giữ đúng tên field** `id`, `name`, `balance`, `type`, `is_default`. Màn chụp ảnh Phần 1 đọc đúng các field này.
- `balance` là số. `is_default` đổi sang **boolean** (`!!row.is_default`).
- User chưa có ví → `data: []`.

Gọi object này là **"object ví"**. Mọi API ví trả object ví đúng khuôn này.

### 2.5. `POST /api/wallets` (cần token)

Body JSON: `{ "name": "Ví MoMo", "type": "bank", "balance": 300000 }` (`balance` không bắt buộc, mặc định 0).

| Điều kiện | HTTP | message |
|---|---|---|
| `name` sau trim rỗng hoặc dài hơn 100 | 400 | `Vui lòng nhập tên ví (tối đa 100 ký tự)` |
| `type` không thuộc `cash`, `bank`, `saving` | 400 | `Loại ví không hợp lệ` |
| `balance` có gửi nhưng không phải số, < 0 hoặc > 999999999999 | 400 | `Số dư không hợp lệ` |
| User đã có ví trùng tên (không phân biệt hoa thường: `LOWER(name) = LOWER(?)`) | 409 | `Tên ví đã tồn tại` |

Ví mới luôn `is_default = 0`. Làm tròn `balance` 2 chữ số: `Math.round(x * 100) / 100`.

`201`: `message: "Đã tạo ví"`, `data` là **1 object ví**.

### 2.6. `PUT /api/wallets/:id` (cần token)

Body JSON, gửi **ít nhất 1** trong 3 field: `{ "name": "...", "type": "...", "balance": 123 }`.

| Điều kiện | HTTP | message |
|---|---|---|
| `:id` không phải số nguyên dương | 400 | `Mã ví không hợp lệ` |
| Không gửi field nào trong 3 field | 400 | `Không có thông tin cần cập nhật` |
| Field có gửi nhưng sai (cùng luật mục 2.5) | 400 | (cùng message mục 2.5) |
| Ví không có hoặc không thuộc user | 404 | `Ví không tồn tại` |
| Đổi tên trùng ví **khác** của user | 409 | `Tên ví đã tồn tại` |

Chỉ `UPDATE` các field có gửi. **Không** cho đổi `is_default`, `user_id`.
`200`: `message: "Đã cập nhật ví"`, `data` là object ví sau khi sửa.

Sửa `balance` trực tiếp là "điều chỉnh số dư" (ví dụ đếm lại tiền mặt). Không tạo giao dịch. Android phải hỏi xác nhận.

### 2.7. `POST /api/wallets/transfer` (cần token)

Body JSON: `{ "from_wallet_id": 1, "to_wallet_id": 2, "amount": 500000, "note": "Rút ATM" }` (`note` không bắt buộc).

| # | Điều kiện | HTTP | message |
|---|---|---|---|
| 1 | `from_wallet_id` hoặc `to_wallet_id` không phải số nguyên dương | 400 | `Vui lòng chọn ví nguồn và ví đích` |
| 2 | `from_wallet_id === to_wallet_id` | 400 | `Ví nguồn và ví đích phải khác nhau` |
| 3 | `amount` không phải số, ≤ 0 hoặc > 999999999999 | 400 | `Số tiền phải lớn hơn 0` |
| 4 | `note` dài hơn 500 ký tự | 400 | `Ghi chú tối đa 500 ký tự` |
| 5 | 1 trong 2 ví không có hoặc không thuộc user | 404 | `Ví không tồn tại` |
| 6 | Số dư ví nguồn < `amount` | 400 | `Số dư ví nguồn không đủ` |

`note` rỗng sau trim → lưu `NULL`. Cách khoá dòng xem task BE-4.

`201`:

```json
{ "success": true, "message": "Chuyển tiền thành công", "data": {
  "transfer": { "id": 3, "from_wallet_id": 1, "to_wallet_id": 2, "amount": 500000, "note": "Rút ATM", "transfer_date": "2026-10-23 09:15:00" },
  "from_wallet": { "id": 1, "name": "Tiền mặt",  "balance": 1500000, "type": "cash", "is_default": true,  "created_at": "..." },
  "to_wallet":   { "id": 2, "name": "Ngân hàng", "balance": 5500000, "type": "bank", "is_default": false, "created_at": "..." }
} }
```

`from_wallet`, `to_wallet` đọc lại **sau khi commit**.
