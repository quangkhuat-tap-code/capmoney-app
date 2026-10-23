# BE-2 · API ngân sách

> **Phần 3** · Repo `backend-cap-money` · Nhánh `feature/stats-budget` · Task trước: [BE-1](BE-1_Api_Thong_Ke.md) · Task sau: [AN-1](AN-1_Nen_Tang_Model_Api.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 3.1 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 1, 2.2, 2.3, 2.4

## Mục tiêu

Có `GET /api/budgets`, `POST /api/budgets` (tạo hoặc cập nhật), `DELETE /api/budgets/:id`. **Xong task này thì push và tạo PR backend.**

## File

| File | Việc |
|---|---|
| `src/controllers/budget.controller.js` | **Tạo mới**: `getBudgets`, `upsertBudget`, `deleteBudget` |
| `src/routes/budget.routes.js` | **Tạo mới** |
| `src/app.js` | **Thêm** mount `/api/budgets` |

## Cách làm

1. Kéo code mới nhất (Hướng dẫn, mục 4.1).
2. `budget.controller.js`:
   - Hàm phụ `mapBudget(row)`: ép `limit_amount`, `spent_amount` về số; tính `percent`, `remaining`, `status` theo mục 2.2 file 01. Dùng chung cho GET và POST.
   - Hằng `BUDGET_SELECT` = câu SQL mục 2.2 (bỏ phần `WHERE ... ORDER BY` để ghép tuỳ chỗ dùng).
   - `getBudgets`: `parseMonthYear` (từ `utils/monthRange.js` của BE-1), sai → `400 "Tham số lọc không hợp lệ"`; chạy câu SQL mục 2.2.
   - `upsertBudget`: kiểm tra theo thứ tự bảng mục 2.3 (`month`/`year` **bắt buộc**, dùng `parseMonthYear` nhưng thiếu thì báo lỗi). Danh mục kiểm tra bằng `SELECT id, type FROM categories WHERE id = ? AND (user_id IS NULL OR user_id = ?)`. Rồi trong **1 DB transaction**:

     ```js
     const [rows] = await conn.query(
       'SELECT id FROM budgets WHERE user_id = ? AND category_id = ? AND month = ? AND year = ? FOR UPDATE',
       [req.user.id, categoryId, month, year]
     );
     // Có  → UPDATE budgets SET limit_amount = ? WHERE id = ?                          → status 200, "Đã cập nhật ngân sách"
     // Chưa → INSERT INTO budgets (user_id, category_id, limit_amount, month, year) ... → status 201, "Đã tạo ngân sách"
     // commit
     ```

     Sau commit đọc lại bằng `BUDGET_SELECT` thêm `WHERE b.id = ?`, trả object ngân sách qua `mapBudget`. Làm tròn `limit_amount` 2 chữ số.
   - `deleteBudget` theo mục 2.4: `DELETE FROM budgets WHERE id = ? AND user_id = ?`, `affectedRows = 0` → 404.
3. Routes (mọi route có `auth`):

   ```js
   router.get('/', auth, getBudgets);
   router.post('/', auth, upsertBudget);
   router.delete('/:id', auth, deleteBudget);
   ```

4. `app.js`: thêm `const budgetRoutes = require('./routes/budget.routes');` và `app.use('/api/budgets', budgetRoutes);` cạnh dòng của `analyticsRoutes`. Xoá dòng comment giữ chỗ `// app.use('/api/budgets', ...)`.

## Test

Dùng dữ liệu đã tạo ở BE-1 (Ăn uống 850.000, Di chuyển 500.000 trong tháng 10/2026).

```bash
API=http://localhost:5000/api
```

```bash
TOKEN=$(npm run -s dev:token)
```

| Ca | Lệnh | Kỳ vọng |
|---|---|---|
| B1 | `curl -s -X POST $API/budgets -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"category_id":1,"limit_amount":1000000,"month":10,"year":2026}'` | `201`, `spent_amount: 850000`, `percent: 85`, `status: "warning"` |
| B2 | Gọi lại B1 với `"limit_amount":2000000` | `200 "Đã cập nhật ngân sách"`, cùng `id`, `percent: 43`, `status: "ok"`; bảng `budgets` vẫn **1** dòng cho Ăn uống tháng 10 |
| B3 | POST `category_id: 2` (Di chuyển), `limit_amount: 400000`, tháng 10/2026 | `201`, `percent: 125`, `status: "over"`, `remaining: -100000` |
| B4 | POST `category_id: 9` (Lương) | `400 "Chỉ đặt ngân sách cho danh mục chi tiêu"` |
| B5 | POST `category_id: 99999` | `404 "Danh mục không tồn tại"` |
| B6 | POST `limit_amount` = `0`, `-1`, `"abc"` (3 lần) | `400 "Hạn mức phải lớn hơn 0"` |
| B7 | POST thiếu `month`, rồi POST `month: 13` | `400 "Tháng/năm không hợp lệ"` |
| B8 | `curl -s "$API/budgets?month=10&year=2026" -H "Authorization: Bearer $TOKEN"` | 2 ngân sách, Di chuyển (125%) đứng **trước** Ăn uống (43%) |
| B9 | `GET $API/budgets?month=11&year=2026` | `200`, `data: []` |
| B10 | Ghi thêm 1 khoản chi Ăn uống 300.000 ngày `2026-10-07 12:00:00` (lệnh như BE-1) rồi gọi lại B8 | Ăn uống `spent_amount` tăng 300.000 |
| B11 | `curl -s -X DELETE $API/budgets/<id Di chuyển> -H "Authorization: Bearer $TOKEN"` | `200`, `data.id` đúng |
| B12 | Xoá lại cùng id | `404 "Ngân sách không tồn tại"` |
| B13 | `DELETE $API/budgets/abc` | `400 "Mã ngân sách không hợp lệ"` |

## Dọn dữ liệu sau khi test

Chạy trong MySQL:

```sql
DELETE FROM budgets WHERE user_id = 1;
DELETE FROM transactions WHERE user_id = 1;
```

Rồi:

```bash
node scripts/run-sql.js database_dev_seed.sql
```

(Ảnh không có vì giao dịch test không kèm ảnh.)

## Commit

```text
feat: api ngan sach

- Them GET /api/budgets: danh sach ngan sach trong thang kem spent_amount, percent, status, sap xep muc nguy hiem truoc
- Them POST /api/budgets: tao moi hoac cap nhat han muc neu da co cung danh muc va thang, chi cho danh muc chi tieu
- Them DELETE /api/budgets/:id
- Mount /api/budgets trong app.js
```

## Sau khi commit: push và tạo PR backend

1. Kiểm tra co-author (phải ra rỗng):

   ```bash
   git log origin/develop..HEAD --format=%B | grep -i -E "co-authored|signed-off|generated with"
   ```

2. `git push`.
3. Tạo PR `feature/stats-budget` → `develop` trên repo `backend-cap-money`, tiêu đề `Phan 3: api thong ke va ngan sach`. Mô tả: danh sách API, bảng kết quả S1–S7, B1–B13, dòng "File dùng chung đã sửa: `src/app.js`".

## Xong khi

- [ ] Đạt B1 → B13.
- [ ] Đã dọn dữ liệu, commit, push, tạo PR backend.
