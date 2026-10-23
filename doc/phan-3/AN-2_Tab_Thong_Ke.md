# AN-2 · Tab Thống kê: biểu đồ donut (chỉ online)

> **Phần 3** · Repo `capmoney-app` · Nhánh `feature/stats-budget` · Task trước: [AN-1](AN-1_Nen_Tang_Model_Api.md) · Task sau: [AN-3](AN-3_Cache_Offline_Room.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 5, 5.1 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 2.1

## Mục tiêu

Tab 2 có thanh chọn tháng, nút Chi tiêu / Thu nhập, biểu đồ donut, danh sách danh mục. Task này **chỉ online**; chạy khi mất mạng làm ở AN-3.

## File

| File | Việc |
|---|---|
| `ui/stats/StatsFragment.java` | Viết lại (đang rỗng) |
| `ui/stats/StatsViewModel.java` | Tạo mới |
| `ui/stats/CategoryStatAdapter.java` | Tạo mới |
| `res/layout/fragment_stats.xml`, `item_stats_category.xml` | Tạo mới |
| `res/drawable/bg_stats_*.xml` | Tạo mới khi cần |

## 1. Bố cục `fragment_stats.xml`

`NestedScrollView` bọc toàn bộ, `paddingBottom="88dp"`, `clipToPadding="false"`:

```text
Thống kê                                     (22sp đậm)
‹      Tháng 10/2026      ›                  (thanh chọn tháng, 00 mục 5.1)

[ Chi tiêu | Thu nhập ]                      MaterialButtonToggleGroup, singleSelection, selectionRequired
Thu 8.000.000 đ · Chi 1.350.000 đ            text_secondary

(chỗ dành cho banner offline — AN-3 dùng, task này để ẩn)

        ╭───────────╮
      ╱   Tổng chi    ╲                      PieChart cao 260dp
     │  1.350.000 đ    │
      ╲               ╱
        ╰───────────╯

🍜 Ăn uống                    850.000 đ      item_stats_category.xml
   3 giao dịch   ███████████░░░░░  63%
🚗 Di chuyển                  500.000 đ
   1 giao dịch   ██████░░░░░░░░░░  37%

(Rỗng: "Chưa có khoản chi nào trong tháng này", ẩn biểu đồ)
(Lỗi: chữ lỗi + nút "Thử lại")
```

Danh sách danh mục: `RecyclerView` với `nestedScrollingEnabled="false"`.

## 2. Cấu hình `PieChart`

XML: `<com.github.mikephil.charting.charts.PieChart android:id="@+id/stats_pie_chart" ... />`

```java
chart.setDrawHoleEnabled(true);
chart.setHoleColor(ContextCompat.getColor(ctx, R.color.bg_main));   // lỗ trùng màu nền tối
chart.setHoleRadius(72f);
chart.setTransparentCircleRadius(0f);
chart.setDrawEntryLabels(false);
chart.getDescription().setEnabled(false);
chart.getLegend().setEnabled(false);
chart.setRotationEnabled(false);
chart.setCenterTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
chart.setCenterTextSize(16f);
chart.setNoDataText("");

PieDataSet ds = new PieDataSet(entries, "");      // 1 PieEntry / danh mục, value = amount
ds.setColors(colors);                             // Color.parseColor(category_color); lỗi parse → color_accent
ds.setSliceSpace(2f);
ds.setDrawValues(false);
chart.setData(new PieData(ds));
chart.setCenterText(getString(R.string.stats_total_expense) + "\n" + CurrencyUtils.format(total));
chart.animateY(600);
chart.invalidate();
```

- Đang ở tab Thu nhập: chữ giữa dùng `stats_total_income`.
- Chạm 1 lát → chữ giữa đổi thành `"<icon> <tên>\n<số tiền>\n<percent>%"`; chạm chỗ trống → về "Tổng chi"/"Tổng thu". Dùng `chart.setOnChartValueSelectedListener(...)` (`onValueSelected`, `onNothingSelected`).

## 3. `item_stats_category.xml`

- Emoji 24sp trong ô tròn 40dp, nền là màu danh mục độ mờ 20%.
- Tên danh mục 15sp; số tiền bên phải `CurrencyUtils.format(amount)`.
- Dòng dưới: `stats_count` (số giao dịch), `LinearProgressIndicator` (max 100, `progress = Math.round(percent)`, `indicatorColor` = màu danh mục, `trackColor` = `bg_card_secondary`, cao 6dp, bo tròn), chữ % bên phải.
- Chữ %: 1 chữ số thập phân kiểu Việt Nam, bỏ `,0` nếu tròn: `63.0` → `"63%"`, `36.5` → `"36,5%"`.

## 4. Chuyển Chi tiêu ↔ Thu nhập

- Mặc định Chi tiêu.
- Nút đang chọn: Chi tiêu nền `color_expense`, Thu nhập nền `color_income`, chữ trắng.
- Đổi → gọi lại API với `type` mới.

## 5. `StatsViewModel`

- Giữ `month`, `year`, `type` đang chọn; LiveData: `summary`, `loading`, `error`.
- `load()`: gọi `StatsRepository.getSummary(month, year, type)`.
- Đổi nhanh tháng/tab: chỉ hiện kết quả của lần gọi **mới nhất** (tăng `requestId` mỗi lần gọi, kết quả về mà `requestId` cũ thì bỏ).
- Fragment gọi `load()` ở `onResume` (ghi giao dịch xong quay lại thấy số mới) và khi đổi tháng / đổi tab. Xoay màn hình **không** gọi lại (ViewModel đã có dữ liệu).
- Dòng "Thu … · Chi …" = `stats_summary_line` với `total_income`, `total_expense`.

## Test

Chuẩn bị: backend có BE-1. Tạo dữ liệu theo mục "Chuẩn bị dữ liệu test" của [BE-1](BE-1_Api_Thong_Ke.md). Chưa có màn đăng nhập thì tạm lưu token dev ở `onCreate` của `StatsFragment` (Hướng dẫn, mục 10), **không commit**.

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| ST1 | Mở Tab Thống kê, chọn Tháng 10/2026 | Donut 2 lát, giữa "Tổng chi 1.350.000 đ"; Ăn uống 850.000 đ 63%, Di chuyển 500.000 đ 37%; dòng "Thu 8.000.000 đ · Chi 1.350.000 đ" |
| ST2 | Bấm Thu nhập | Donut 1 lát Lương 100%, giữa "Tổng thu 8.000.000 đ", nút nền xanh |
| ST3 | Lùi về Tháng 9/2026 | Chi 100.000 đ |
| ST4 | Ở tháng hiện tại bấm › | Không bấm được |
| ST5 | Tháng không có giao dịch | Ẩn donut, hiện "Chưa có khoản chi nào trong tháng này" |
| ST6 | Chạm lát Ăn uống | Giữa hiện "🍜 Ăn uống / 850.000 đ / 63%"; chạm chỗ trống thì về lại |
| ST7 | Ghi thêm 1 khoản chi bằng màn chụp (+), quay lại tab | Số liệu cập nhật |
| ST8 | Xoay màn hình | Giữ tháng + tab Chi/Thu, không crash |
| ST9 | Bấm nhanh ‹ ‹ ‹ › › | Hiện đúng tháng cuối cùng được chọn |
| ST10 | Tắt backend, mở tab | Lỗi + nút Thử lại (chưa có offline ở task này), không crash |

## Commit

Trước khi commit: `git diff` không còn `DEV ONLY`.

```text
feat: tab thong ke bieu do donut

- StatsFragment: chon thang, chuyen Chi tieu/Thu nhap, bieu do donut PieChart lo trung mau nen, cham lat hien chi tiet
- Danh sach danh muc kem thanh phan tram va so tien (CategoryStatAdapter)
- StatsViewModel giu thang va tab khi xoay man hinh, bo ket qua cu khi doi thang nhanh
```

## Xong khi

- [ ] Đạt ST1 → ST10.
- [ ] Đã commit, không có dòng co-author.
