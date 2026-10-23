# BE-1 · API thống kê theo danh mục

> **Phần 3** · Repo `backend-cap-money` · Nhánh `feature/stats-budget` · Task trước: không có · Task sau: [BE-2](BE-2_Api_Ngan_Sach.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 3.1, 4.1 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 1, 2 (đầu mục), 2.1

## Mục tiêu

Có `GET /api/analytics/summary` trả tổng thu, tổng chi và chi tiết theo danh mục kèm %, để Tab Thống kê vẽ donut.

## File

| File | Việc |
|---|---|
| `src/utils/monthRange.js` | **Tạo mới** (BE-2 dùng lại) |
| `src/controllers/analytics.controller.js` | **Tạo mới**: `getSummary` |
| `src/routes/analytics.routes.js` | **Tạo mới** |
| `src/app.js` | **Thêm** mount `/api/analytics` |

## Cách làm

1. Kéo code mới nhất và tạo nhánh nếu chưa có (Hướng dẫn, mục 3 và 4.1).
2. `src/utils/monthRange.js`:

   ```js
   // Trả { month, year, start, end } hoặc null nếu tham số sai.
   // Khoảng [start, end) giống transaction.controller.js của Phần 1.
   function parseMonthYear(monthRaw, yearRaw) { ... }
   module.exports = { parseMonthYear };
   ```

   - `monthRaw` / `yearRaw` là `undefined` → dùng tháng/năm hiện tại (`new Date()`).
   - Sai (không phải số nguyên, tháng ngoài 1–12, năm ngoài 2000–2100) → `null`.
   - `start = 'YYYY-MM-01 00:00:00'`; `end` = ngày 1 tháng sau (tháng 12 → tháng 1 năm sau). Tháng có 2 chữ số (`'09'`).
3. `getSummary` theo mục 2.1 file 01: `parseMonthYear(req.query.month, req.query.year)`; `type` mặc định `expense`; chạy 2 câu SQL; tính `total`, `percent`; ép số.
4. Route: `router.get('/summary', auth, getSummary);`
5. `app.js` (chỉ thêm): `const analyticsRoutes = require('./routes/analytics.routes');` cạnh các `require` route; `app.use('/api/analytics', analyticsRoutes);` cạnh các `app.use`. Xoá dòng comment giữ chỗ `// app.use('/api/analytics', ...)`. Giữ nguyên dòng của người khác.

## Chuẩn bị dữ liệu test

Backend đang chạy `npm run dev`. Git Bash ở thư mục backend:

```bash
node scripts/run-sql.js database_dev_seed.sql
```

```bash
API=http://localhost:5000/api
```

```bash
TOKEN=$(npm run -s dev:token)
```

Tạo giao dịch bằng API của Phần 1 (danh mục 1 = Ăn uống, 2 = Di chuyển, 9 = Lương):

```bash
for a in 300000 250000 300000; do curl -s -X POST $API/transactions -H "Authorization: Bearer $TOKEN" -F amount=$a -F wallet_id=1 -F category_id=1 -F type=expense -F "transaction_date=2026-10-05 12:00:00" > /dev/null; done
```

```bash
curl -s -X POST $API/transactions -H "Authorization: Bearer $TOKEN" -F amount=500000 -F wallet_id=1 -F category_id=2 -F type=expense -F "transaction_date=2026-10-06 08:00:00"
```

```bash
curl -s -X POST $API/transactions -H "Authorization: Bearer $TOKEN" -F amount=8000000 -F wallet_id=1 -F category_id=9 -F type=income -F "transaction_date=2026-10-01 09:00:00"
```

```bash
curl -s -X POST $API/transactions -H "Authorization: Bearer $TOKEN" -F amount=100000 -F wallet_id=1 -F category_id=1 -F type=expense -F "transaction_date=2026-09-30 20:00:00"
```

**Giữ lại dữ liệu này** để làm BE-2 và test Android.

## Test

| Ca | Lệnh | Kỳ vọng |
|---|---|---|
| S1 | `curl -s "$API/analytics/summary?month=10&year=2026" -H "Authorization: Bearer $TOKEN"` | `type: expense`, `total_expense: 1350000`, `total_income: 8000000`, `total: 1350000`; Ăn uống `850000` `63`%, `count 3`; Di chuyển `500000` `37`%; Ăn uống đứng trước |
| S2 | Như S1 thêm `&type=income` | `total: 8000000`, 1 danh mục Lương `100`% |
| S3 | `month=9&year=2026` | `total_expense: 100000` (giao dịch 30/09 **không** lọt sang tháng 10) |
| S4 | `month=1&year=2020` | `200`, tổng 0, `categories: []` |
| S5 | `month=13`, `year=abc`, `type=all` (3 lần gọi) | `400 "Tham số lọc không hợp lệ"` |
| S6 | Không gửi token | `401` |
| S7 | Gọi `GET $API/transactions?month=10&year=2026`, cộng tay các khoản chi | Bằng `total_expense` ở S1 |

## Commit

```text
feat: api thong ke theo danh muc

- Them GET /api/analytics/summary: tong thu, tong chi va chi tiet theo danh muc kem phan tram trong thang
- Them utils/monthRange.js tinh khoang thoi gian 1 thang giong API giao dich
- Mount /api/analytics trong app.js
```

Không thêm dòng `Co-Authored-By` hay tên công cụ AI.

## Xong khi

- [ ] Đạt S1 → S7.
- [ ] `git status` chỉ có 4 file ở bảng "File".
- [ ] Đã commit.
