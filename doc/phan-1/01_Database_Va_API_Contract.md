# 01 · Database & API Contract (Phần 1)

> File này là **hợp đồng giao tiếp** giữa Android và Backend của Phần 1. Hai bên code theo đúng file này. Muốn đổi field, tên hay kiểu dữ liệu thì sửa file này trước, rồi sửa code ở cả hai phía.

---

## 1. Bảng dữ liệu Phần 1 dùng

Phần 1 **đọc/ghi** 2 bảng `transactions`, `categories` và **cập nhật cột `balance`** của bảng `wallets`.
Bảng `users`, `wallets` do Thành viên 2 sở hữu, Phần 1 chỉ dựa vào các cột ghi trong mục 1.3.

Toàn bộ DB phải dùng `utf8mb4` / `utf8mb4_unicode_ci`, vì `categories.icon` lưu **emoji**.

### 1.1. `categories`

```sql
CREATE TABLE IF NOT EXISTS categories (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NULL,                         -- NULL = danh mục hệ thống, dùng chung cho mọi user
  name       VARCHAR(50)  NOT NULL,
  icon       VARCHAR(16)  NOT NULL,            -- 1 emoji, ví dụ '🍜'
  color_hex  CHAR(7)      NOT NULL,            -- '#RRGGBB'
  type       ENUM('expense','income') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_categories_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Dữ liệu mẫu bắt buộc** (danh mục hệ thống, `user_id = NULL`):

```sql
INSERT INTO categories (user_id, name, icon, color_hex, type) VALUES
(NULL, 'Ăn uống',       '🍜', '#FF9F0A', 'expense'),
(NULL, 'Di chuyển',     '🚗', '#0A84FF', 'expense'),
(NULL, 'Mua sắm',       '🛍️', '#FF375F', 'expense'),
(NULL, 'Giải trí',      '🎮', '#BF5AF2', 'expense'),
(NULL, 'Hóa đơn',       '🧾', '#64D2FF', 'expense'),
(NULL, 'Sức khỏe',      '💊', '#32D74B', 'expense'),
(NULL, 'Giáo dục',      '📚', '#FFD60A', 'expense'),
(NULL, 'Khác',          '📦', '#8E8E93', 'expense'),
(NULL, 'Lương',         '💼', '#32D74B', 'income'),
(NULL, 'Thưởng',        '🎁', '#FFD60A', 'income'),
(NULL, 'Đầu tư',        '📈', '#0A84FF', 'income'),
(NULL, 'Thu nhập khác', '💰', '#8E8E93', 'income');
```

### 1.2. `transactions`

```sql
CREATE TABLE IF NOT EXISTS transactions (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  user_id          INT NOT NULL,
  wallet_id        INT NOT NULL,
  category_id      INT NOT NULL,
  amount           DECIMAL(15,2) NOT NULL,       -- luôn > 0; chiều tiền do cột type quyết định
  note             TEXT NULL,
  image_url        VARCHAR(255) NULL,            -- đường dẫn TƯƠNG ĐỐI: '/uploads/img_1729570000000_123.jpg'
  type             ENUM('expense','income') NOT NULL,
  transaction_date DATETIME NOT NULL,            -- giờ địa phương Asia/Ho_Chi_Minh, không lưu timezone
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tx_user     FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
  CONSTRAINT fk_tx_wallet   FOREIGN KEY (wallet_id)   REFERENCES wallets(id)    ON DELETE RESTRICT,
  CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
  INDEX idx_tx_user_date (user_id, transaction_date),
  INDEX idx_tx_wallet (wallet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 1.3. Cột của bảng khác mà Phần 1 phụ thuộc

| Bảng | Cột | Phần 1 dùng để |
|---|---|---|
| `users` | `id` | Khóa ngoại; lấy từ `req.user.id` |
| `wallets` | `id`, `user_id`, `name`, `balance DECIMAL(15,2)`, `type`, `is_default` | Kiểm tra ví có thuộc user không, cộng/trừ `balance`, hiển thị tên ví |

> Nếu `database_init.sql` chưa có bảng `users`, `wallets` thì xem [02_Backend_Spec.md](02_Backend_Spec.md) mục 2 (khung backend). Phần 1 chỉ tạo khung tối thiểu để chạy được, **không** viết logic của Thành viên 2.

---

## 2. Quy ước chung cho mọi API

| Mục | Quy ước |
|---|---|
| Base URL | `http://<host>:5000/api` (emulator: `10.0.2.2`) |
| Xác thực | Mọi endpoint của Phần 1 đều cần header `Authorization: Bearer <JWT>`; payload JWT là `{ "id": <userId> }` |
| Response | Luôn đúng khuôn `{ "success": bool, "message": string, "data": any \| null }` |
| Tiền | Kiểu **number** trong JSON (`50000`, không phải `"50000.00"`). VND không có phần lẻ; Android làm tròn khi hiển thị |
| Ngày giờ | Chuỗi `"yyyy-MM-dd HH:mm:ss"` theo giờ địa phương, ví dụ `"2026-10-22 12:30:00"`. **Không** dùng ISO có `Z` |
| Ảnh | Server trả **đường dẫn tương đối** `image_url` (`"/uploads/..."`) hoặc `null`. Android tự ghép `ApiClient.IMAGE_BASE_URL + image_url` |
| `type` | Chỉ nhận `"expense"` hoặc `"income"` (chữ thường) |
| Mã lỗi | `400` dữ liệu sai · `401` thiếu/sai token · `404` không tìm thấy **hoặc không thuộc user** (không trả 403 để tránh lộ dữ liệu) · `500` lỗi server |

> **Lưu ý:** tài liệu gốc có chỗ ghi "trả về link ảnh đầy đủ", có chỗ ghi "Android ghép host". Spec này **chốt cách thứ hai** (server trả đường dẫn tương đối), để khi đổi giữa emulator và điện thoại thật chỉ phải sửa `ApiClient.IMAGE_BASE_URL`.

### 2.1. Object `Transaction`, dùng chung cho mọi response

```json
{
  "id": 12,
  "amount": 50000,
  "type": "expense",
  "note": "Bún chả trưa",
  "image_url": "/uploads/img_1729570000000_123.jpg",
  "transaction_date": "2026-10-22 12:30:00",
  "wallet_id": 1,
  "wallet_name": "Tiền mặt",
  "category_id": 1,
  "category_name": "Ăn uống",
  "category_icon": "🍜",
  "category_color": "#FF9F0A"
}
```

- `note` có thể là `null` hoặc `""`.
- `image_url` có thể là `null` (giao dịch không có ảnh).

### 2.2. Object `Category`

```json
{ "id": 1, "name": "Ăn uống", "icon": "🍜", "color_hex": "#FF9F0A", "type": "expense", "is_system": true }
```

`is_system` = `user_id IS NULL`.

---

## 3. Danh sách endpoint

### 3.1. `GET /api/categories` (Phần 1 làm; tài liệu gốc chưa giao cho ai)

Query: `type` (tùy chọn) = `expense` | `income`. Không truyền thì trả cả hai loại.

Trả về danh mục hệ thống (`user_id IS NULL`) và danh mục riêng của user, sắp xếp theo `type`, rồi `user_id IS NULL DESC`, rồi `id ASC`.

```json
{ "success": true, "message": "Lấy danh mục thành công", "data": [ { ...Category }, ... ] }
```

Lỗi: `type` khác 2 giá trị hợp lệ → `400 "Loại danh mục không hợp lệ"`.

### 3.2. `POST /api/transactions` (multipart/form-data)

| Field | Kiểu | Bắt buộc | Ràng buộc |
|---|---|---|---|
| `image` | file | Không | `image/jpeg`, `image/png`, `image/webp`; ≤ 5MB |
| `amount` | text | Có | Số > 0 và ≤ 999 999 999 999; nhận `"50000"` hoặc `"50000.5"` |
| `wallet_id` | text | Có | Số nguyên; ví phải thuộc user |
| `category_id` | text | Có | Số nguyên; danh mục là danh mục hệ thống hoặc của user; `category.type` phải **trùng** `type` |
| `type` | text | Có | `expense` \| `income` |
| `note` | text | Không | Trim; tối đa 500 ký tự |
| `transaction_date` | text | Không | `yyyy-MM-dd HH:mm:ss`; không gửi thì lấy thời điểm hiện tại theo giờ server |

Quy tắc số dư:
- `expense`: `wallets.balance -= amount`. **Nếu `amount > balance` thì chặn**, trả `400 "Số dư ví không đủ để thực hiện giao dịch"`.
- `income`: `wallets.balance += amount`.

Thành công trả `201`:
```json
{ "success": true, "message": "Đã lưu giao dịch", "data": { ...Transaction } }
```

Lỗi (`400`), tất cả đều kèm `data: null`:

| Điều kiện | `message` |
|---|---|
| Thiếu/sai `amount` | `"Số tiền phải lớn hơn 0"` |
| Thiếu/sai `type` | `"Loại giao dịch không hợp lệ"` |
| Thiếu `wallet_id` / ví không thuộc user | `"Ví không tồn tại"` (dùng `404`) |
| Thiếu `category_id` / không hợp lệ | `"Danh mục không tồn tại"` (dùng `404`) |
| `category.type ≠ type` | `"Danh mục không khớp loại giao dịch"` |
| `transaction_date` sai định dạng | `"Ngày giao dịch không hợp lệ"` |
| `note` dài hơn 500 ký tự | `"Ghi chú tối đa 500 ký tự"` |
| File quá 5MB | `"Ảnh vượt quá 5MB"` |
| Sai định dạng file | `"Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP"` |
| Không đủ số dư | `"Số dư ví không đủ để thực hiện giao dịch"` |

Nếu bị lỗi sau khi multer đã ghi file thì **phải xóa file vừa upload**.

### 3.3. `GET /api/transactions`

| Query | Bắt buộc | Mặc định | Ghi chú |
|---|---|---|---|
| `month` | Không | tháng hiện tại | 1–12 |
| `year` | Không | năm hiện tại | 2000–2100 |
| `wallet_id` | Không | tất cả ví | Lọc theo 1 ví |
| `type` | Không | cả hai | `expense` \| `income` |

Sắp xếp: `transaction_date DESC, id DESC`. Không phân trang, vì một tháng chỉ có vài trăm bản ghi.

```json
{ "success": true, "message": "Lấy danh sách giao dịch thành công", "data": [ { ...Transaction }, ... ] }
```

Lỗi: `month`/`year`/`wallet_id`/`type` sai → `400 "Tham số lọc không hợp lệ"`. Tháng không có giao dịch thì trả `data: []`, không phải lỗi.

### 3.4. `DELETE /api/transactions/:id`

Hoàn lại số dư: giao dịch `expense` → `balance += amount`; `income` → `balance -= amount` (cho phép số dư âm sau khi hoàn, không chặn).
Xóa file ảnh trong `public/uploads/` nếu có; file không tồn tại thì bỏ qua.

```json
{ "success": true, "message": "Đã xóa giao dịch", "data": { "id": 12, "wallet_id": 1, "wallet_balance": 1250000 } }
```

Lỗi: `id` không phải số nguyên dương → `400 "Mã giao dịch không hợp lệ"`; không có hoặc không thuộc user → `404 "Giao dịch không tồn tại"`.

### 3.5. Endpoint của Thành viên 2 mà Phần 1 cần gọi: `GET /api/wallets`

Màn hình chụp ảnh cần danh sách ví để chọn. Phần 1 **không** viết endpoint này, chỉ đọc theo khuôn đã thống nhất sau. Phải **báo Thành viên 2** khuôn này:

```json
{ "success": true, "message": "...", "data": [
  { "id": 1, "name": "Tiền mặt", "balance": 1300000, "type": "cash", "is_default": true }
] }
```

`is_default` có thể là `true/false` hoặc `1/0` (MySQL BOOL). Android phải đọc được cả hai, xem [03_Android_Nen_Tang_Chung.md](03_Android_Nen_Tang_Chung.md) mục 5.
