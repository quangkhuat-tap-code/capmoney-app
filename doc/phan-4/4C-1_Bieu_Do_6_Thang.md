# 4C-1 · Biểu đồ Chi/Thu 6 tháng

> **Phần 4C** · Trình bày: **Quang** · Code + test: Lâm Anh · Task trước: [4B-3](4B-3_Anh_Dai_Dien.md) · Task sau: [4C-2](4C-2_Thong_Bao_Vuot_Ngan_Sach.md)
> **Đọc trước:** [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) mục 2.6

## Mục tiêu

Tab Thống kê (Phần 3) chỉ xem **1 tháng**. Task này thêm biểu đồ cột so sánh Chi và Thu của **6 tháng gần nhất** (tính tới tháng đang chọn), để thấy xu hướng tiêu tiền tăng hay giảm.

## Backend

| File | Việc |
|---|---|
| `src/controllers/analytics.controller.js` | Thêm `getTrend`, export |
| `src/routes/analytics.routes.js` | `router.get('/trend', auth, getTrend);` |

1. `parseMonthYear(req.query.month, req.query.year)` → tháng cuối. `months`: không truyền = 6; phải là số nguyên 2–12. Sai → `400 "Tham số lọc không hợp lệ"`.
2. Dựng sẵn mảng `months` phần tử từ cũ đến mới (lùi tháng, qua năm đúng: tháng 2/2026 lùi 6 → 9/2025 … 2/2026), mỗi phần tử `{ month, year, total_expense: 0, total_income: 0 }`.
3. `start` = ngày 1 của tháng **đầu mảng**, `end` = `range.end` của tháng cuối. Chạy câu SQL mục 2.6 file 01, cộng vào đúng phần tử.
4. Số tiền ép `Number`, làm tròn 2 chữ số.

### Test backend

| Ca | Làm | Kỳ vọng |
|---|---|---|
| TR1 | `GET /analytics/trend?month=10&year=2026` | 6 phần tử 5→10/2026; tháng 10 khớp `GET /analytics/summary` tháng 10 |
| TR2 | `month=2&year=2026` | 9/2025 → 2/2026 (qua năm đúng) |
| TR3 | `months=12` | 12 phần tử |
| TR4 | `months=1`; `months=13`; `months=abc` | `400` |
| TR5 | Tháng chưa có giao dịch | Phần tử có số 0, không bị thiếu |
| TR6 | Ghi khoản chi ngày `2026-10-31 23:59:59` | Rơi vào tháng 10, không sang 11 |

**Commit backend:** `feat: api xu huong chi thu theo thang`.

## Android

| File | Việc |
|---|---|
| `model/TrendPoint.java` | `month`, `year`, `total_expense`, `total_income` |
| `data/api/AnalyticsTrendApi.java` | `@GET("analytics/trend") Call<ApiResponse<List<TrendPoint>>> getTrend(@Query("month") int m, @Query("year") int y, @Query("months") int months);` |
| `data/repository/StatsRepository.java` | `getTrend(month, year, Callback<List<TrendPoint>>)` (Callback lồng sẵn của Phần 3) |
| `ui/stats/StatsViewModel.java` | LiveData `trend`; tải **cùng lúc** với tổng tháng mỗi khi đổi tháng |
| `ui/stats/TrendChartHelper.java` | **Tạo mới**: cấu hình `BarChart` và đổ dữ liệu, để `StatsFragment` gọn |
| `res/layout/fragment_stats.xml` | Thẻ mới `trend_card` **dưới** `stats_rv_categories` (trong `stats_scroll`): tiêu đề "6 tháng gần nhất" + `BarChart` `trend_bar_chart` cao 220dp |

### Biểu đồ

- `BarChart` nhóm 2 cột mỗi tháng: Chi `color_expense`, Thu `color_income`. `groupBars(...)` với `barWidth 0.35`, `barSpace 0.05`, `groupSpace 0.2`.
- Trục X: nhãn `T5`, `T6`, … (tháng 1 của năm mới ghi `T1/27` để biết qua năm). Trục Y trái: `CurrencyUtils.formatShort` (`1,2tr`); tắt trục phải, tắt mô tả, chữ màu `text_secondary`, lưới mờ.
- Bấm vào cột: dòng chữ `trend_tv_selected` dưới biểu đồ ghi "Tháng 10/2026 · Chi … · Thu …" (`CurrencyUtils.format`). Làm thay `MarkerView` cho đơn giản, không cần layout riêng.
- Nhãn trục X canh giữa nhóm cột (vị trí `i + 0.5`) nên tự viết `ValueFormatter` lấy phần nguyên; `IndexAxisValueFormatter` làm tròn và lệch 1 tháng.
- **Không** phụ thuộc nút Chi/Thu của donut (biểu đồ cột luôn hiện cả hai).
- Cả 6 tháng đều 0 → ẩn biểu đồ, ghi "Chưa có dữ liệu 6 tháng gần nhất".
- **Mất mạng**: donut vẫn đọc Room như Phần 3, còn biểu đồ cột **ẩn**, ghi "Cần kết nối mạng để xem xu hướng" (Room chỉ lưu tháng đã mở, không đủ 6 tháng).
- Đổi tháng ở thanh chọn tháng → biểu đồ dịch theo (6 tháng kết thúc ở tháng đang chọn).

### Test Android

| Ca | Làm | Kỳ vọng |
|---|---|---|
| TRA1 | Mở Tab Thống kê tháng 10/2026, cuộn xuống | Biểu đồ 6 tháng, cột tháng 10 khớp số tổng phía trên |
| TRA2 | Bấm ‹ sang tháng 9 | Biểu đồ thành 4→9/2026 |
| TRA3 | Bấm 1 cột | Hiện số tiền đầy đủ |
| TRA4 | Tắt backend, mở lại tab | Donut hiện từ Room + banner; biểu đồ cột ẩn kèm chữ |
| TRA5 | Tài khoản mới chưa có giao dịch | "Chưa có dữ liệu 6 tháng gần nhất" |
| TRA6 | Xoay màn hình | Không gọi lại API, biểu đồ còn nguyên |

**Commit app:** `feat: bieu do chi thu 6 thang o tab thong ke`.

## Xong khi

- [ ] Đạt TR1 → TR6, TRA1 → TRA6.
