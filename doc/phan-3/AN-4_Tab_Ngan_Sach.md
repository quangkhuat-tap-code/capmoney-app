# AN-4 · Tab Ngân sách: danh sách hạn mức

> **Phần 3** · Repo `capmoney-app` · Nhánh `feature/stats-budget` · Task trước: [AN-3](AN-3_Cache_Offline_Room.md) · Task sau: [AN-5](AN-5_Them_Sua_Xoa_Ngan_Sach.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 5, 5.1 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 2.2

## Mục tiêu

Tab 4 hiển thị hạn mức từng danh mục trong tháng, thanh tiến độ đổi màu xanh/vàng/đỏ, thẻ tổng. Nút "Thêm" có sẵn nhưng form làm ở AN-5.

## File

| File | Việc |
|---|---|
| `ui/budget/BudgetFragment.java` | Viết lại (đang rỗng) |
| `ui/budget/BudgetViewModel.java` | Tạo mới |
| `ui/budget/BudgetAdapter.java` | Tạo mới |
| `res/layout/fragment_budget.xml`, `item_budget.xml` | Tạo mới |
| `res/drawable/bg_budget_*.xml` | Tạo mới khi cần |

## Bố cục `fragment_budget.xml`

`RecyclerView` chính, `paddingBottom="88dp"`, `clipToPadding="false"`. Phần đầu (tiêu đề, thanh tháng, thẻ tổng) có thể là header của adapter hoặc nằm trên RecyclerView trong `NestedScrollView`.

```text
Ngân sách                              [+ Thêm]     nút nhỏ viền color_accent ở góc TRÊN phải
‹      Tháng 10/2026      ›

┌──────────────────────────────────────────┐
│ Tổng hạn mức   Đã chi        Còn lại      │   thẻ tổng
│ 2.400.000 đ    1.350.000 đ   1.050.000 đ  │
│ ███████████████████░░░░░░░░░░  56%        │   thanh tổng, cùng luật màu
└──────────────────────────────────────────┘

🚗 Di chuyển                          125%           item_budget.xml
Đã tiêu 500.000 đ / 400.000 đ
████████████████████████████████  (đỏ)
Vượt 100.000 đ                                      (chữ color_expense)

🍜 Ăn uống                             43%
Đã tiêu 850.000 đ / 2.000.000 đ
██████████████░░░░░░░░░░░░░░░░░░  (xanh)
Còn 1.150.000 đ
```

**Không** đặt nút nổi ở góc dưới phải (nút (+) của `MainActivity` ở đó).

## Hành vi

- Màu thanh và chữ % theo `status` server trả:

  | `status` | Khi | Màu |
  |---|---|---|
  | `ok` | < 80% | `color_income` (xanh lá) |
  | `warning` | 80% – 99% | `color_warning` (vàng) |
  | `over` | ≥ 100% | `color_expense` (đỏ) |

- Thanh: `LinearProgressIndicator` max 100, `progress = Math.min(percent, 100)`, cao 8dp bo tròn, `trackColor` = `bg_card_secondary`.
- Dòng dưới thanh: `remaining >= 0` → `budget_item_remaining`; âm → `budget_item_over` với số dương, chữ `color_expense`.
- Thẻ tổng: cộng `limit_amount`, `spent_amount` của mọi dòng; còn lại = hạn mức − đã chi; % tổng tính ở máy, cùng luật màu. **Ẩn** thẻ tổng khi danh sách rỗng.
- Rỗng → chữ `budget_empty` + nút `budget_empty_action` (task này: nút để trống, AN-5 nối form).
- Thanh chọn tháng theo 00 mục 5.1. Tải ở `onResume` và khi đổi tháng.
- Không cần cache offline cho tab này: mất mạng → lỗi + nút Thử lại.
- Xoay màn hình: giữ tháng, không gọi lại API.

## Test

Chuẩn bị: backend có BE-2. Tạo lại dữ liệu theo "Chuẩn bị dữ liệu test" của [BE-1](BE-1_Api_Thong_Ke.md), rồi chạy ca **B1, B2, B3** của [BE-2](BE-2_Api_Ngan_Sach.md) (Ăn uống 2.000.000, Di chuyển 400.000). **Không** chạy B10–B13. Chưa có màn đăng nhập thì tạm lưu token dev ở `onCreate` của `BudgetFragment` (Hướng dẫn, mục 10), **không commit**.

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| BU1 | Mở Tab Ngân sách tháng 10/2026 | Di chuyển 125% thanh **đỏ** đứng trước, "Vượt 100.000 đ"; Ăn uống 43% thanh **xanh**, "Còn 1.150.000 đ"; thẻ tổng 2.400.000 / 1.350.000 / 1.050.000 |
| BU1b | Thêm bằng curl ngân sách Mua sắm (`category_id: 3`) 100.000, ghi 1 khoản chi Mua sắm 85.000 (curl như BE-1), quay lại tab | Mua sắm 85% thanh **vàng** |
| BU8 | Chuyển sang tháng chưa đặt ngân sách | "Chưa đặt ngân sách cho tháng này" + nút "Đặt ngân sách", ẩn thẻ tổng |
| BU9 | Cuộn tới dòng cuối | Dòng cuối không bị nút (+) của `MainActivity` che |
| BU10 | Tắt backend, mở tab | Lỗi mạng + Thử lại, không crash |
| BU11 | Xoay màn hình | Giữ tháng, không crash |

## Commit

Trước khi commit: `git diff` không còn `DEV ONLY`.

```text
feat: tab ngan sach

- BudgetFragment: chon thang, the tong han muc/da chi/con lai, danh sach ngan sach theo danh muc
- Thanh tien do doi mau theo status: xanh duoi 80%, vang 80-99%, do tu 100%; hien so con lai hoac so vuot
- Them BudgetViewModel, BudgetAdapter, fragment_budget.xml, item_budget.xml
```

## Xong khi

- [ ] Đạt BU1, BU1b, BU8 → BU11.
- [ ] Đã commit, không có dòng co-author.
