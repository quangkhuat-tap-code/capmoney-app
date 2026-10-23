# 02 · Backend Spec (Node.js + Express + MySQL), Phần 1

> Đọc [01_Database_Va_API_Contract.md](01_Database_Va_API_Contract.md) trước. File này nói **cách làm**; file 01 nói **làm ra cái gì**.

---

## 1. Vị trí code backend

- Repo backend: `https://github.com/quangkhuat-tap-code/backend-cap-money`, nhánh chính `develop`, làm trên nhánh `lamanh`.
- Thư mục local: `E:\Hoctap\capmoney\capmoney-backend\` (ngang hàng với `capmoney-app`).
- Khung backend (mục 2) đã có trong repo, không cần tạo lại.

---

## 2. Khung backend (chỉ làm khi CHƯA có)

Những file dưới đây là **file dùng chung** của cả nhóm. Phần 1 chỉ tạo bản tối thiểu để chạy được API của mình, commit riêng với message `chore: khung backend dung chung`, và **báo nhóm** để Thành viên 2, 3 dùng chung, không tạo lại.

### 2.1. `package.json`

```json
{
  "name": "capmoney-backend",
  "version": "1.0.0",
  "main": "src/app.js",
  "scripts": {
    "start": "node src/app.js",
    "dev": "nodemon src/app.js",
    "db:init": "node scripts/run-sql.js database_init.sql",
    "dev:token": "node scripts/dev-token.js"
  },
  "dependencies": {
    "bcryptjs": "^2.4.3",
    "cors": "^2.8.5",
    "dotenv": "^16.4.5",
    "express": "^4.21.2",
    "jsonwebtoken": "^9.0.2",
    "multer": "^1.4.5-lts.1",
    "mysql2": "^3.11.5"
  },
  "devDependencies": { "nodemon": "^3.1.9" }
}
```

> Giữ **Express 4** (không lên 5) để giống code mẫu phổ biến và cách bắt lỗi của multer bên dưới.

### 2.2. `.env.example` (commit file này; **không** commit `.env`)

```dotenv
PORT=5000
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASS=
DB_NAME=capmoney
JWT_SECRET={REDACTED}
```

`.gitignore` của backend: `node_modules/`, `.env`, `public/uploads/*` và `!public/uploads/.gitkeep`.

### 2.3. `src/config/db.js`

```js
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
  charset: 'utf8mb4',
  decimalNumbers: true, // DECIMAL -> number thay vì string
  dateStrings: true     // DATETIME -> 'yyyy-MM-dd HH:mm:ss', không đổi múi giờ
});

module.exports = pool;
```

`decimalNumbers` và `dateStrings` là **bắt buộc**. Thiếu hai option này thì JSON trả `amount: "50000.00"` và `transaction_date` dạng ISO UTC, làm sai hợp đồng ở file 01.

### 2.4. `src/middlewares/auth.middleware.js`

- Đọc header `Authorization`. Không có hoặc không bắt đầu bằng `Bearer ` → `401 { success:false, message:"Bạn chưa đăng nhập", data:null }`.
- `jwt.verify(token, process.env.JWT_SECRET)`. Lỗi → `401 "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"`.
- Gắn `req.user = { id: payload.id }`.
- **Thống nhất với Thành viên 2:** khi đăng nhập phải ký `jwt.sign({ id: user.id }, JWT_SECRET, { expiresIn: '7d' })`.

### 2.5. `src/app.js`

Thứ tự:
1. `require('dotenv').config()`
2. `app.use(cors())`, `app.use(express.json())`, `app.use(express.urlencoded({ extended: true }))`
3. `app.use('/uploads', express.static(path.join(__dirname, '..', 'public', 'uploads')))`
4. Mount route: `app.use('/api/categories', categoryRoutes)`, `app.use('/api/transactions', transactionRoutes)`. Route của Thành viên 2, 3 để dòng comment giữ chỗ: `// app.use('/api/auth', ...)  // Thành viên 2`
5. Handler 404 cho `/api/*`: `404 { success:false, message:"Không tìm thấy API", data:null }`
6. Error handler cuối cùng `(err, req, res, next)`: log `console.error(err)` và trả `500 { success:false, message:"Lỗi máy chủ", data:null }`. **Không** trả `err.message` hoặc stack ra client.
7. Tự tạo thư mục `public/uploads` nếu chưa có (`fs.mkdirSync(..., { recursive: true })`).
8. `app.listen(PORT, '0.0.0.0')` để điện thoại thật trong cùng WiFi gọi được.

### 2.6. `database_init.sql`

- `CREATE DATABASE IF NOT EXISTS capmoney CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; USE capmoney;`
- Tạo **đủ 6 bảng** theo tài liệu gốc, thứ tự `users` → `wallets` → `categories` → `transactions` → `wallet_transfers` → `budgets`. Riêng `categories` và `transactions` dùng đúng DDL ở file 01. Bốn bảng còn lại viết theo cột trong tài liệu gốc, mục 2.2. Thành viên 2 và 3 có thể chỉnh sau.
- Kèm dữ liệu mẫu danh mục ở file 01, mục 1.1.

### 2.7. `database_dev_seed.sql` (dữ liệu test, chỉ dùng trên máy dev)

```sql
USE capmoney;
INSERT INTO users (id, full_name, email, password_hash) VALUES
  (1, 'Dev Tester', 'dev@example.test', 'not-a-real-hash');
INSERT INTO wallets (id, user_id, name, balance, type, is_default) VALUES
  (1, 1, 'Tiền mặt',  2000000, 'cash', 1),
  (2, 1, 'Ngân hàng', 5000000, 'bank', 0);
```

`password_hash` cố ý không hợp lệ: user này **không đăng nhập được**, chỉ dùng với token sinh bởi `dev-token.js`.

### 2.8. `scripts/dev-token.js`

In ra JWT cho user id truyền vào (mặc định 1), ký bằng `JWT_SECRET` trong `.env`, hạn 30 ngày. Dùng để test API Phần 1 khi Thành viên 2 chưa xong API đăng nhập:

```bash
node scripts/dev-token.js 1
```

### 2.9. `scripts/run-sql.js`

Đọc file SQL rồi chạy bằng `mysql2` với `multipleStatements: true`. Kết nối **không** chỉ định `database`, vì file init tự `CREATE DATABASE`. Viết script này để nhóm không cần cài MySQL CLI.

---

## 3. File của Phần 1

```text
src/
├── middlewares/upload.middleware.js
├── controllers/category.controller.js
├── controllers/transaction.controller.js
├── routes/category.routes.js
├── routes/transaction.routes.js
└── utils/transaction.query.js       # câu SELECT dùng chung + hàm map row -> Transaction
```

### 3.1. `upload.middleware.js`

```js
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, UPLOAD_DIR),            // public/uploads
  filename: (req, file, cb) => {
    const ext = { 'image/jpeg': '.jpg', 'image/png': '.png', 'image/webp': '.webp' }[file.mimetype];
    cb(null, `img_${Date.now()}_${Math.round(Math.random() * 1e6)}${ext}`);
  }
});
```

- `limits: { fileSize: 5 * 1024 * 1024, files: 1 }`
- `fileFilter`: chỉ nhận 3 mimetype trên. Sai mimetype → `cb(new AppError(400, 'Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP'))`.
- **Không** đặt tên file theo tên gốc người dùng gửi lên, để tránh path traversal.
- Export hàm `uploadSingleImage(req, res, next)` bọc `upload.single('image')` và tự bắt lỗi:
  - `err.code === 'LIMIT_FILE_SIZE'` → `400 "Ảnh vượt quá 5MB"`
  - `AppError` → trả đúng status và message
  - Lỗi khác → `next(err)`

> `AppError` là class nhỏ `{ status, message }` đặt trong `src/utils/AppError.js`.

### 3.2. `transaction.query.js`

```sql
SELECT t.id, t.amount, t.type, t.note, t.image_url, t.transaction_date,
       t.wallet_id, w.name AS wallet_name,
       t.category_id, c.name AS category_name, c.icon AS category_icon, c.color_hex AS category_color
FROM transactions t
JOIN wallets w    ON w.id = t.wallet_id
JOIN categories c ON c.id = t.category_id
```

Hàm `mapTransaction(row)` trả object đúng thứ tự field ở file 01, mục 2.1, và ép `amount: Number(row.amount)` để chắc chắn.

### 3.3. `category.controller.js` → `getCategories`

```sql
SELECT id, name, icon, color_hex, type, (user_id IS NULL) AS is_system
FROM categories
WHERE (user_id IS NULL OR user_id = ?) [AND type = ?]
ORDER BY type, (user_id IS NULL) DESC, id
```

Map `is_system` thành boolean (`!!row.is_system`).

### 3.4. `transaction.controller.js`

#### `createTransaction`, chạy sau `uploadSingleImage`

1. **Validate** toàn bộ input theo bảng ở file 01, mục 3.2, **trước** khi mở kết nối DB. Hàm validate trả lỗi đầu tiên gặp phải.
   - `amount`: `Number(req.body.amount)`; phải `Number.isFinite` và trong khoảng `(0, 999999999999]`. Làm tròn 2 chữ số.
   - `wallet_id`, `category_id`: `Number.isInteger(n) && n > 0`.
   - `transaction_date`: regex `^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$` và phải là ngày có thật (dùng `new Date(y, m-1, d, ...)` rồi so lại từng thành phần). Không gửi thì format `new Date()` theo giờ local thành chuỗi.
   - `note`: `String(note ?? '').trim()`; rỗng thì lưu `NULL`.
2. `const conn = await pool.getConnection(); await conn.beginTransaction();`
3. `SELECT id, balance FROM wallets WHERE id = ? AND user_id = ? FOR UPDATE`. Không có → `404 "Ví không tồn tại"`.
4. `SELECT id, type FROM categories WHERE id = ? AND (user_id IS NULL OR user_id = ?)`. Không có → `404 "Danh mục không tồn tại"`. Khác `type` → `400 "Danh mục không khớp loại giao dịch"`.
5. `expense` và `amount > balance` → `400 "Số dư ví không đủ để thực hiện giao dịch"`.
6. `INSERT INTO transactions (...)` với `image_url = req.file ? '/uploads/' + req.file.filename : null`.
7. `UPDATE wallets SET balance = balance + ? WHERE id = ?`, với delta là `+amount` (income) hoặc `-amount` (expense).
8. `await conn.commit()`.
9. Dùng `transaction.query.js` + `WHERE t.id = ?` lấy lại bản ghi, trả `201`.
10. `catch`: `await conn.rollback()`, **xóa `req.file.path` nếu có** (`fs.promises.unlink`, bỏ qua lỗi), rồi trả lỗi nghiệp vụ hoặc `next(err)`.
11. `finally`: `conn.release()`.

> Nếu bước validate (bước 1) lỗi mà đã có `req.file` thì cũng phải xóa file.

#### `getTransactions`

1. Parse `month`, `year`; thiếu thì lấy tháng/năm hiện tại. Validate theo file 01, mục 3.3.
2. Tính khoảng `[start, end)` để query dùng được index: `start = 'YYYY-MM-01 00:00:00'`, `end` = ngày 1 tháng kế tiếp (tháng 12 thì sang năm sau).
3. `WHERE t.user_id = ? AND t.transaction_date >= ? AND t.transaction_date < ?`, cộng thêm `AND t.wallet_id = ?` và `AND t.type = ?` nếu có.
4. `ORDER BY t.transaction_date DESC, t.id DESC`.
5. Trả `200` với `data` là mảng đã map.

**Không** dùng `MONTH(t.transaction_date) = ?`, vì cách này làm mất index.

#### `deleteTransaction`

1. Validate `req.params.id`.
2. `beginTransaction`. Chạy `SELECT id, wallet_id, amount, type, image_url FROM transactions WHERE id = ? AND user_id = ? FOR UPDATE`. Không có → `404`.
3. `UPDATE wallets SET balance = balance + ? WHERE id = ?`, với delta: `expense` → `+amount`, `income` → `-amount`.
4. `DELETE FROM transactions WHERE id = ?`.
5. Đọc lại `SELECT balance FROM wallets WHERE id = ?`.
6. `commit`.
7. **Sau khi commit** mới xóa file ảnh. Lấy tên file bằng `path.basename(image_url)` rồi ghép với `UPLOAD_DIR`, để chống path traversal. Lỗi `ENOENT` thì bỏ qua.
8. Trả `200` với `{ id, wallet_id, wallet_balance }`.

### 3.5. Routes

```js
// category.routes.js
router.get('/', auth, getCategories);

// transaction.routes.js
router.get('/', auth, getTransactions);
router.post('/', auth, uploadSingleImage, createTransaction);
router.delete('/:id', auth, deleteTransaction);
```

`auth` phải chạy **trước** `uploadSingleImage`, để không ghi file khi chưa đăng nhập.

---

## 4. Ngoài phạm vi

- `PUT /api/transactions/:id` (sửa giao dịch): tài liệu gốc chỉ nhắc "Xóa/Sửa" trong comment cây thư mục, không có trong danh sách API của Thành viên 1. **Không làm** đợt này.
- Phân trang, tìm kiếm, upload nhiều ảnh.
- API ví, đăng nhập, thống kê, ngân sách: thuộc Thành viên 2 và 3.

---

## 5. Kiểm thử backend bằng curl

Chuẩn bị: `npm i`, tạo `.env`, `npm run db:init`, chạy `database_dev_seed.sql` (qua `node scripts/run-sql.js database_dev_seed.sql`), rồi `npm run dev`. Lấy token bằng `npm run dev:token`.

```bash
TOKEN=$(node scripts/dev-token.js 1)
```

```bash
curl -s http://localhost:5000/api/categories?type=expense -H "Authorization: Bearer $TOKEN"
```

```bash
curl -s -X POST http://localhost:5000/api/transactions -H "Authorization: Bearer $TOKEN" -F "image=@test.jpg;type=image/jpeg" -F amount=50000 -F wallet_id=1 -F category_id=1 -F type=expense -F "note=Bún chả" -F "transaction_date=2026-10-22 12:30:00"
```

```bash
curl -s "http://localhost:5000/api/transactions?month=10&year=2026" -H "Authorization: Bearer $TOKEN"
```

```bash
curl -s -X DELETE http://localhost:5000/api/transactions/1 -H "Authorization: Bearer $TOKEN"
```

| # | Ca kiểm thử | Kỳ vọng |
|---|---|---|
| B1 | Gọi bất kỳ API nào không có token | `401`, đúng khuôn JSON |
| B2 | POST expense 50 000 vào ví 1 (2 000 000) | `201`; ví 1 còn `1 950 000`; file ảnh có trong `public/uploads` |
| B3 | POST không kèm ảnh | `201`; `image_url: null` |
| B4 | POST expense 99 000 000 | `400` "Số dư ví không đủ…"; ví không đổi; **không** còn file rác trong `uploads` |
| B5 | POST `category_id` của danh mục income với `type=expense` | `400` "Danh mục không khớp…" |
| B6 | POST `wallet_id` của user khác hoặc không tồn tại | `404` "Ví không tồn tại" |
| B7 | POST file 6MB | `400` "Ảnh vượt quá 5MB" |
| B8 | POST file `.txt` đổi tên thành `.jpg` (mimetype `text/plain`) | `400` "Chỉ chấp nhận ảnh…" |
| B9 | POST `amount=0`, `-5`, `abc` | `400` "Số tiền phải lớn hơn 0" |
| B10 | GET `?month=10&year=2026` | Mảng sắp xếp mới nhất trước; `amount` là number; ngày dạng `yyyy-MM-dd HH:mm:ss` |
| B11 | GET `?month=13` | `400` |
| B12 | DELETE giao dịch expense 50 000 | `200`; ví được cộng lại 50 000; file ảnh bị xóa |
| B13 | DELETE lại cùng id | `404` |
| B14 | `GET http://localhost:5000/uploads/<file>` | Trả ảnh |
| B15 | Hai request POST expense gửi đồng thời, tổng vượt số dư | Chỉ 1 request thành công (nhờ `FOR UPDATE`) |
