# 05 · Android: Tab 1 Home (Lưới ảnh, Lịch, Chi tiết, Xóa)

> Làm sau [03](03_Android_Nen_Tang_Chung.md). Gọi API theo [01](01_Database_Va_API_Contract.md): `GET /transactions`, `DELETE /transactions/:id`.

`MainActivity` đã nối `nav_home` vào `new HomeFragment()` và đặt nó vào `R.id.fragment_container`. Mỗi lần bấm tab, `MainActivity` tạo **instance mới**, nên state cần giữ (chế độ xem, tháng đang xem) phải nằm ở ViewModel gắn với **Activity**, không gắn với Fragment.

---

## 1. File cần tạo hoặc sửa

| File | Loại |
|---|---|
| `ui/home/HomeFragment.java` | Sửa (đang là class rỗng) |
| `ui/home/HomeViewModel.java` | Mới (`AndroidViewModel`) |
| `ui/home/PhotoGridAdapter.java` | Mới (`ListAdapter<Transaction, VH>` + `DiffUtil`) |
| `ui/home/CalendarAdapter.java` | Mới |
| `ui/home/CalendarDay.java` | Mới (model cho ô lịch) |
| `ui/home/SquareFrameLayout.java` | Mới (`FrameLayout` có `onMeasure` ép chiều cao bằng chiều rộng) |
| `ui/home/TransactionDetailDialog.java` | Mới (`BottomSheetDialogFragment`) |
| `ui/home/DayTransactionsBottomSheet.java` | Mới (`BottomSheetDialogFragment`) |
| `res/layout/fragment_home.xml` | Mới |
| `res/layout/item_photo_grid.xml` | Mới |
| `res/layout/item_calendar_day.xml` | Mới |
| `res/layout/dialog_transaction_detail.xml` | Mới |
| `res/layout/bottom_sheet_day_transactions.xml` | Mới |

---

## 2. Bố cục `fragment_home.xml`

Nền `@color/bg_main`; padding ngang `@dimen/spacing_medium`.

```
┌─────────────────────────────────────┐
│  ‹    Tháng 10/2026    ›            │  ← chọn tháng
│                                     │
│ ┌───────────────┐ ┌───────────────┐ │
│ │ Chi tiêu      │ │ Thu nhập      │ │  ← 2 thẻ tổng tháng
│ │ -1.250.000 đ  │ │ +8.000.000 đ  │ │
│ └───────────────┘ └───────────────┘ │
│                                     │
│ [ 🖼️ Lưới ảnh |  📅 Lịch  ]         │  ← toggle chế độ xem
│                                     │
│ ┌────┐┌────┐┌────┐                  │
│ │ảnh ││ảnh ││ảnh │                  │  ← lưới 3 cột hoặc lịch
│ │-50k││+8tr││-30k│                  │
│ └────┘└────┘└────┘                  │
└─────────────────────────────────────┘
```

| id | View | Ghi chú |
|---|---|---|
| `btn_prev_month` / `btn_next_month` | `ImageButton` 48dp | `ic_chevron_left_24` / `ic_chevron_right_24`. `btn_next_month` **bị vô hiệu** (alpha 0.3) khi đang xem tháng hiện tại |
| `tv_month` | `TextView` | `DateUtils.monthTitle(m, y)`, `text_size_title`, bold, `text_primary`. Bấm vào thì quay về tháng hiện tại |
| `card_total_expense` / `card_total_income` | `MaterialCardView` | Nền `bg_card`, bo `corner_radius_small`, mỗi thẻ `weight=1`, cách nhau `spacing_small`. Nhãn `text_size_small text_secondary`; số tiền `text_size_title` bold, màu `color_expense` / `color_income` |
| `toggle_view_mode` | `MaterialButtonToggleGroup` | `singleSelection`, `selectionRequired`; `btn_mode_grid` "🖼️ Lưới ảnh", `btn_mode_calendar` "📅 Lịch". Nút chọn: nền `color_accent`, chữ trắng; nút còn lại: nền `bg_card`, chữ `text_secondary` |
| `rv_photo_grid` | `RecyclerView` | `GridLayoutManager(3)`, khoảng cách giữa ô 4dp (`ItemDecoration`), `clipToPadding=false`, `paddingBottom=88dp` để FAB không che ô cuối |
| `layout_calendar` | `LinearLayout` dọc | Hàng thứ T2…CN (7 `TextView`, `text_size_small text_secondary`, căn giữa), rồi `rv_calendar` `GridLayoutManager(7)`, `nestedScrollingEnabled=false` |
| `progress_home` | `CircularProgressIndicator` | Giữa màn hình, chỉ hiện khi tải **lần đầu** (danh sách đang rỗng) |
| `layout_empty` | `LinearLayout` | Emoji 📸 48sp và chữ `home_empty` = "Chưa có giao dịch nào trong tháng này.\nBấm + để chụp khoản đầu tiên"; `text_secondary`, căn giữa. Chỉ hiện ở chế độ **lưới**; lịch vẫn hiện các ô trống |
| `layout_error` | `LinearLayout` | Chữ lỗi + nút `btn_retry` "Thử lại" |

`rv_photo_grid` và `layout_calendar` nằm chung một chỗ; tại mỗi thời điểm chỉ một cái `VISIBLE`.
Khi ở chế độ lịch, bọc `layout_calendar` trong `NestedScrollView` để màn hình thấp vẫn cuộn được.

---

## 3. `HomeViewModel`

Lấy bằng `new ViewModelProvider(requireActivity()).get(HomeViewModel.class)`, để giữ state khi đổi tab.

| LiveData | Kiểu | Mặc định |
|---|---|---|
| `month`, `year` | `int` (gộp thành `YearMonth`, là 1 class nhỏ tự viết vì `java.time` cần API 26) | tháng hiện tại |
| `viewMode` | `int` (`MODE_GRID = 0`, `MODE_CALENDAR = 1`) | `MODE_GRID` |
| `transactions` | `List<Transaction>` | rỗng |
| `totals` | `double[2]` = {tổng chi, tổng thu} | {0, 0} |
| `loading` | `Boolean` | `false` |
| `error` | `String` | `null` |
| `event` | `SingleEvent<String>` | Toast một lần, ví dụ "Đã xóa giao dịch" |

Phương thức:
- `load()`: gọi `repository.getTransactions(month, year)`. Thành công thì cập nhật `transactions`, tính `totals` ở client (cộng `amount` theo `type`), và `error = null`. Lỗi thì `error = message`; **giữ nguyên** danh sách cũ nếu đã có.
- `prevMonth()` / `nextMonth()`: đổi tháng (qua năm khi cần), xóa danh sách, rồi `load()`. `nextMonth()` không cho vượt tháng hiện tại.
- `goToCurrentMonth()`
- `setViewMode(int)`
- `delete(int id)`: gọi `repository.deleteTransaction`. Thành công thì bỏ phần tử khỏi danh sách hiện tại (tạo **list mới**, để `DiffUtil` nhận ra thay đổi), tính lại `totals`, và bắn event "Đã xóa giao dịch". Lỗi thì bắn event với message lỗi.
- **Chống race:** mỗi lần `load()` tăng `requestSeq`. Callback chỉ áp dụng khi `seq == requestSeq`, để người dùng bấm đổi tháng nhanh không bị dữ liệu tháng cũ đè lên.

`HomeFragment.onResume()` gọi `viewModel.load()`, để tải lại sau khi lưu xong ở `CaptureExpenseActivity`.

---

## 4. Chế độ Lưới ảnh, `PhotoGridAdapter`

`item_photo_grid.xml`: gốc là `SquareFrameLayout`; bên trong là `MaterialCardView` `match_parent`, bo `@dimen/corner_radius_small`, nền `bg_card_secondary`, `strokeWidth=0`.

| id | View | Ghi chú |
|---|---|---|
| `iv_thumb` | `ImageView` | `match_parent`, `centerCrop` |
| `tv_placeholder_icon` | `TextView` | Emoji `categoryIcon` 36sp, căn giữa; chỉ hiện khi **không có ảnh** |
| `tv_amount_label` | `TextView` | Góc **dưới trái**, margin 6dp, nền `@drawable/bg_photo_amount_label`, padding 6/2dp, `text_size_small`, bold. Text `CurrencyUtils.formatSigned(amount, isExpense)`; màu `color_expense` / `color_income` |
| `tv_category_badge` | `TextView` | Góc **trên phải**, margin 6dp, emoji `categoryIcon` 14sp, nền tròn `capture_overlay`; chỉ hiện khi **có ảnh** |

Load ảnh:

```java
Glide.with(holder.itemView)
     .load(tx.getFullImageUrl())
     .transform(new CenterCrop(), new RoundedCorners(cornerPx))   // cornerPx = getDimensionPixelSize(corner_radius_small)
     .placeholder(R.color.bg_card_secondary)
     .error(R.color.bg_card_secondary)
     .into(holder.ivThumb);
```

Không có ảnh thì `Glide.with(...).clear(ivThumb)` và hiện placeholder emoji. Phải gọi `clear`, nếu không ô tái sử dụng sẽ hiện ảnh cũ.

`DiffUtil.ItemCallback`: `areItemsTheSame` so `id`; `areContentsTheSame` so `amount`, `imageUrl`, `note`, `categoryId`, `transactionDate`.

Bấm một ô thì mở `TransactionDetailDialog.newInstance(tx)`.

---

## 5. Chế độ Lịch, `CalendarAdapter`

### 5.1. Dựng dữ liệu `List<CalendarDay>`

- Tuần bắt đầu **Thứ Hai**, theo thói quen Việt Nam.
- Số ô trống đầu tháng: `(dayOfWeek(ngày 1) + 5) % 7`, với `Calendar.SUNDAY = 1`.
- Tổng số ô làm tròn lên bội số của 7 (35 hoặc 42). Ô ngoài tháng có `CalendarDay.inMonth = false`, không hiện gì.
- Gom `transactions` theo `getDayKey()` thành `Map<String, List<Transaction>>`.

`CalendarDay` gồm: `int day`, `boolean inMonth`, `boolean isToday`, `List<Transaction> items`, `String thumbUrl` (ảnh của giao dịch **mới nhất có ảnh** trong ngày, không có thì `null`), `double totalExpense`, `double totalIncome`.

### 5.2. `item_calendar_day.xml`

Gốc `SquareFrameLayout`, padding 2dp.

| id | Ghi chú |
|---|---|
| `iv_day_thumb` | Ảnh `thumbUrl`, `centerCrop`, bo 8dp, alpha 0.85; ẩn khi không có |
| `tv_day_number` | Góc trên trái, `text_size_small`, bold; có ảnh thì chữ `white` có `shadowRadius=4`, không có thì `text_primary` |
| `tv_day_total` | Đáy, căn giữa, 10sp; text `"-" + CurrencyUtils.formatShort(totalExpense)` màu `color_expense`. Nếu chỉ có thu thì `"+…"` màu `color_income`. Không có giao dịch thì ẩn |
| `view_today_border` | `@drawable/bg_calendar_today`; chỉ hiện khi `isToday` |

Ô có giao dịch mà không có ảnh: nền `bg_card_secondary`. Ô không có giao dịch: nền trong suốt.

### 5.3. Bấm ô

- Không có giao dịch: không làm gì.
- Đúng 1 giao dịch: mở `TransactionDetailDialog`.
- Từ 2 giao dịch trở lên: mở `DayTransactionsBottomSheet.newInstance(dayKey)`. Sheet có tiêu đề `"Ngày 22/10/2026"` và tổng chi/thu của ngày; bên dưới là `RecyclerView` lưới 3 cột **dùng lại `PhotoGridAdapter`**. Sheet lấy dữ liệu từ `HomeViewModel` (lọc theo `dayKey`), nên khi xóa một giao dịch thì sheet tự cập nhật. Sheet tự đóng khi danh sách của ngày rỗng.

---

## 6. `TransactionDetailDialog` (`BottomSheetDialogFragment`)

Nhận `Transaction` qua `Bundle` (`putSerializable("tx", tx)`). Nền `bg_card`, bo 2 góc trên 20dp; mở ra ở trạng thái `STATE_EXPANDED`.

```
┌─────────────────────────────────────┐
│               ───                   │
│ ┌─────────────────────────────────┐ │
│ │         ảnh lớn 1:1             │ │  ← bo 20dp; không có ảnh thì ẩn khối này
│ └─────────────────────────────────┘ │
│          -50.000 đ                  │  ← 32sp, màu theo loại
│     🍜 Ăn uống · Chi tiêu           │
│                                     │
│  Ví          💵 Tiền mặt            │
│  Thời gian   22/10/2026 12:30       │
│  Ghi chú     Bún chả trưa           │  ← ẩn dòng nếu note rỗng
│                                     │
│ [ 🗑  Xóa giao dịch ]               │  ← nút outlined, viền và chữ color_expense
└─────────────────────────────────────┘
```

- Ảnh dùng Glide `fitCenter`, bo `corner_radius_card`, nền `bg_main`. Bấm ảnh thì **không** cần phóng to (ngoài phạm vi).
- Nhãn bên trái dùng `text_secondary`, giá trị bên phải dùng `text_primary`, căn phải.
- Bấm **Xóa giao dịch** → `MaterialAlertDialogBuilder`:
  - Tiêu đề: "Xóa giao dịch?"
  - Nội dung: Chi tiêu thì "Số tiền 50.000 đ sẽ được hoàn lại vào ví Tiền mặt."; Thu nhập thì "Số tiền 8.000.000 đ sẽ bị trừ khỏi ví Tiền mặt."
  - Nút: "Hủy" / "Xóa" (chữ `color_expense`)
- Xác nhận xóa thì vô hiệu nút, hiện progress trên nút và gọi `HomeViewModel.delete(id)` (`ViewModelProvider(requireActivity())`). Quan sát kết quả qua event:
  - Thành công: đóng dialog; `HomeFragment` hiện Toast "Đã xóa giao dịch".
  - Lỗi: bật lại nút và hiện Toast lỗi.

---

## 7. Hiển thị theo trạng thái

| Trạng thái | Lưới | Lịch |
|---|---|---|
| Đang tải lần đầu | `progress_home` | `progress_home` |
| Tải lại khi đã có dữ liệu (`onResume`, sau khi xóa) | Giữ nội dung, **không** hiện progress toàn màn | Như bên trái |
| Rỗng | `layout_empty` | Lịch trống, không có `layout_empty` |
| Lỗi, chưa có dữ liệu | `layout_error` + nút Thử lại | `layout_error` |
| Lỗi, đã có dữ liệu | Giữ nội dung, Toast lỗi | Như bên trái |
| `401` | `layout_error` với chữ "Phiên đăng nhập đã hết hạn…" | Như bên trái |

> Khi màn đăng nhập của Thành viên 2 xong: lỗi 401 sẽ chuyển về `LoginActivity`. **Đợt này chưa làm**; chỉ hiện thông báo.

---

## 8. Chữ hiển thị (thêm vào `strings_capture_home.xml`)

```
home_total_expense       Chi tiêu
home_total_income        Thu nhập
home_mode_grid           🖼️ Lưới ảnh
home_mode_calendar       📅 Lịch
home_empty               Chưa có giao dịch nào trong tháng này.\nBấm + để chụp khoản đầu tiên
home_retry               Thử lại
home_weekdays            T2,T3,T4,T5,T6,T7,CN     (string-array home_weekdays)
home_day_title           Ngày %1$s
home_detail_wallet       Ví
home_detail_time         Thời gian
home_detail_note         Ghi chú
home_delete              Xóa giao dịch
home_delete_title        Xóa giao dịch?
home_delete_msg_expense  Số tiền %1$s sẽ được hoàn lại vào ví %2$s.
home_delete_msg_income   Số tiền %1$s sẽ bị trừ khỏi ví %2$s.
home_delete_cancel       Hủy
home_delete_confirm      Xóa
home_deleted             Đã xóa giao dịch
```

---

## 9. Nghiệm thu Tab Home

| # | Ca kiểm thử | Kỳ vọng |
|---|---|---|
| H1 | Mở app | Tab Home ở tháng hiện tại; lưới 3 cột, ảnh vuông bo góc, nhãn tiền ở góc dưới trái |
| H2 | Có 0 giao dịch | Hiện "Chưa có giao dịch nào…" |
| H3 | Giao dịch không ảnh | Ô hiện emoji danh mục trên nền `bg_card_secondary` |
| H4 | Thẻ tổng | Tổng chi/thu đúng bằng tổng các ô |
| H5 | Bấm ‹ | Sang tháng trước, dữ liệu đúng tháng đó; › bật lại |
| H6 | Ở tháng hiện tại | Nút › bị mờ, bấm không có tác dụng |
| H7 | Chuyển sang Lịch | Tuần bắt đầu T2; hôm nay có viền xanh; ngày có ảnh hiện thumbnail và tổng "-50k" |
| H8 | Đổi tab Thống kê rồi quay lại | Vẫn ở chế độ và tháng trước đó |
| H9 | Bấm ô ngày có 1 giao dịch | Mở chi tiết |
| H10 | Bấm ô ngày có ≥ 2 giao dịch | Mở sheet lưới của ngày |
| H11 | Xóa từ chi tiết → xác nhận | Ô biến mất, tổng cập nhật, Toast "Đã xóa giao dịch"; gọi `GET /api/wallets` thấy số dư được hoàn |
| H12 | Lưu giao dịch mới ở màn chụp rồi quay về | Ô mới hiện đầu lưới (không cần kéo để làm mới) |
| H13 | Tắt backend, mở Home | Hiện lỗi + nút Thử lại; bật backend, bấm Thử lại thì tải được |
| H14 | Cuộn lưới 100+ ô nhanh | Không giật, không hiện nhầm ảnh của ô khác |
| H15 | FAB (+) | Không che mất ô cuối lưới |
