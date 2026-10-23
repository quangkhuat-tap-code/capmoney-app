# PHẦN 3 · Database và hợp đồng API

> Thuộc bộ tài liệu Phần 3. Đọc [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) trước.
> File này là **hợp đồng chung**: Backend trả đúng như đây, Android đọc đúng như đây. Muốn đổi field/tên/kiểu thì sửa file này trước, rồi sửa code cả 2 phía.

---

## 1. Database (đã có sẵn, KHÔNG sửa `database_init.sql`)

```sql
CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NULL,                          -- NULL = danh mục hệ thống
  name VARCHAR(50) NOT NULL,
  icon VARCHAR(16) NOT NULL,                 -- 1 emoji
  color_hex CHAR(7) NOT NULL,                -- '#RRGGBB'
  type ENUM('expense','income') NOT NULL
);

CREATE TABLE transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  wallet_id INT NOT NULL,
  category_id INT NOT NULL,
  amount DECIMAL(15,2) NOT NULL,             -- luôn > 0, chiều tiền do type quyết định
  type ENUM('expense','income') NOT NULL,
  transaction_date DATETIME NOT NULL,        -- giờ Việt Nam, chuỗi 'yyyy-MM-dd HH:mm:ss'
  note TEXT NULL, image_url VARCHAR(255) NULL, created_at TIMESTAMP
);

CREATE TABLE budgets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  category_id INT NOT NULL,                  -- FK categories(id)
  limit_amount DECIMAL(15,2) NOT NULL,
  month TINYINT NOT NULL,                    -- 1..12
  year SMALLINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_budgets_user_period (user_id, month, year)
);
```

Danh mục hệ thống có sẵn (id theo thứ tự): chi tiêu `1 Ăn uống 🍜`, `2 Di chuyển 🚗`, `3 Mua sắm 🛍️`, `4 Giải trí 🎮`, `5 Hóa đơn 🧾`, `6 Sức khỏe 💊`, `7 Giáo dục 📚`, `8 Khác 📦`; thu nhập `9 Lương 💼`, `10 Thưởng 🎁`, `11 Đầu tư 📈`, `12 Thu nhập khác 💰`.

Quy ước:
- Bảng `budgets` **không** có khoá duy nhất. Backend tự đảm bảo mỗi `(user_id, category_id, month, year)` chỉ có **1** dòng (mục 2.3).
- Danh mục user nhìn thấy: `user_id IS NULL OR user_id = <user>`.
- "Đã tiêu" của ngân sách = tổng `amount` giao dịch `type = 'expense'` cùng `category_id`, cùng user, trong tháng đó.

---

## 2. API

Mọi response đúng khuôn `{ success, message, data }`. Tiền là **số**. Mọi API **cần token**.
Lỗi không lường trước → `500 "Lỗi máy chủ"` (có sẵn trong `app.js`).

Tham số tháng/năm dùng chung:
- `month` (1–12), `year` (2000–2100). Không gửi → tháng/năm **hiện tại** của server.
- Sai (không phải số nguyên, ngoài khoảng) → `400 "Tham số lọc không hợp lệ"`.

### 2.1. `GET /api/analytics/summary?month=10&year=2026&type=expense`

`type`: `expense` (mặc định) hoặc `income`; khác → `400 "Tham số lọc không hợp lệ"`.

SQL (dùng `start`, `end` của tháng):

```sql
-- Tổng thu, tổng chi trong tháng
SELECT COALESCE(SUM(CASE WHEN type = 'income'  THEN amount END), 0) AS total_income,
       COALESCE(SUM(CASE WHEN type = 'expense' THEN amount END), 0) AS total_expense
FROM transactions
WHERE user_id = ? AND transaction_date >= ? AND transaction_date < ?;

-- Chi tiết theo danh mục của loại đang xem
SELECT c.id AS category_id, c.name AS category_name, c.icon AS category_icon, c.color_hex AS category_color,
       SUM(t.amount) AS amount, COUNT(*) AS count
FROM transactions t
JOIN categories c ON c.id = t.category_id
WHERE t.user_id = ? AND t.type = ? AND t.transaction_date >= ? AND t.transaction_date < ?
GROUP BY c.id, c.name, c.icon, c.color_hex
ORDER BY amount DESC, c.id ASC;
```

- `total` = `total_expense` nếu `type=expense`, `total_income` nếu `type=income`.
- `percent` mỗi danh mục = `amount / total * 100`, làm tròn **1 chữ số** (`Math.round(x * 10) / 10`). `total = 0` thì `categories` rỗng.
- Ép mọi tiền về `Number(...)`, `count` về số nguyên.

`200`:

```json
{ "success": true, "message": "Lấy thống kê thành công", "data": {
  "month": 10, "year": 2026, "type": "expense",
  "total_income": 8000000, "total_expense": 1350000, "total": 1350000,
  "categories": [
    { "category_id": 1, "category_name": "Ăn uống",   "category_icon": "🍜", "category_color": "#FF9F0A", "amount": 850000, "count": 3, "percent": 63 },
    { "category_id": 2, "category_name": "Di chuyển", "category_icon": "🚗", "category_color": "#0A84FF", "amount": 500000, "count": 1, "percent": 37 }
  ]
} }
```

Tháng không có giao dịch → `200`, các tổng = 0, `categories: []`.

### 2.2. `GET /api/budgets?month=10&year=2026`

```sql
SELECT b.id, b.category_id, c.name AS category_name, c.icon AS category_icon, c.color_hex AS category_color,
       b.limit_amount, b.month, b.year,
       (SELECT COALESCE(SUM(t.amount), 0)
          FROM transactions t
         WHERE t.user_id = b.user_id AND t.category_id = b.category_id AND t.type = 'expense'
           AND t.transaction_date >= ? AND t.transaction_date < ?) AS spent_amount
FROM budgets b
JOIN categories c ON c.id = b.category_id
WHERE b.user_id = ? AND b.month = ? AND b.year = ?
ORDER BY spent_amount / b.limit_amount DESC, b.id ASC;
```

Mỗi phần tử trả thêm 3 trường tính ở Node:
- `percent` = `Math.round(spent_amount / limit_amount * 100)` (số nguyên, **có thể > 100**).
- `remaining` = `limit_amount - spent_amount` (âm nếu vượt).
- `status`: `"ok"` nếu `percent < 80`, `"warning"` nếu `80 ≤ percent < 100`, `"over"` nếu `percent ≥ 100`.

`200`:

```json
{ "success": true, "message": "Lấy danh sách ngân sách thành công", "data": [
  { "id": 4, "category_id": 1, "category_name": "Ăn uống", "category_icon": "🍜", "category_color": "#FF9F0A",
    "limit_amount": 2000000, "spent_amount": 1800000, "remaining": 200000, "percent": 90, "status": "warning",
    "month": 10, "year": 2026 }
] }
```

Gọi object này là **"object ngân sách"** — `POST` cũng trả đúng khuôn này.

### 2.3. `POST /api/budgets` (tạo hoặc cập nhật)

Body JSON: `{ "category_id": 1, "limit_amount": 2000000, "month": 10, "year": 2026 }`

| # | Điều kiện | HTTP | message |
|---|---|---|---|
| 1 | `month`/`year` sai hoặc thiếu (ở đây **bắt buộc** gửi) | 400 | `Tháng/năm không hợp lệ` |
| 2 | `limit_amount` không phải số, ≤ 0 hoặc > 999999999999 | 400 | `Hạn mức phải lớn hơn 0` |
| 3 | `category_id` không phải số nguyên dương, hoặc không có / user không thấy được | 404 | `Danh mục không tồn tại` |
| 4 | Danh mục là `income` | 400 | `Chỉ đặt ngân sách cho danh mục chi tiêu` |

Đã có ngân sách cùng `(user, category, month, year)` → **cập nhật** `limit_amount`, `200 "Đã cập nhật ngân sách"`. Chưa có → **thêm mới**, `201 "Đã tạo ngân sách"`. Cả hai trả **object ngân sách** (có `spent_amount`, `percent`, `status`). Cách làm xem task BE-2.

### 2.4. `DELETE /api/budgets/:id`

| Điều kiện | HTTP | message |
|---|---|---|
| `:id` không phải số nguyên dương | 400 | `Mã ngân sách không hợp lệ` |
| Không có hoặc không thuộc user | 404 | `Ngân sách không tồn tại` |

`200`: `{ "success": true, "message": "Đã xoá ngân sách", "data": { "id": 4 } }`
