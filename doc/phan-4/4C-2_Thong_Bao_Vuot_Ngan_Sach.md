# 4C-2 · Thông báo khi sắp vượt / vượt ngân sách

> **Phần 4C** · Trình bày: **Quang** · Chỉ có Android · Task trước: [4C-1](4C-1_Bieu_Do_6_Thang.md) · Task sau: [4C-3](4C-3_Tab_Tai_Khoan_Offline.md)
> **Đọc trước:** [00_Tong_Quan_Phan_4.md](00_Tong_Quan_Phan_4.md) mục 4.3 · Phần 3, `01_Database_Va_API_Phan_3.md` mục 2.2 (`GET /api/budgets`)

## Mục tiêu

Tab Ngân sách (Phần 3) chỉ đổi màu thanh khi người dùng **tự mở tab**. Task này hiện **thông báo của Android** ngay khi vừa lưu một khoản chi làm danh mục đó **chạm 80%** hoặc **vượt 100%** hạn mức tháng.

Không cần backend mới: dùng `GET /api/budgets?month=&year=` có sẵn.

## File

| File | Việc |
|---|---|
| `AndroidManifest.xml` | Thêm `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`. **Commit riêng** (có thể gộp với commit manifest của 4A-2 nếu làm cùng lúc, ghi rõ trong message) |
| `ui/budget/BudgetAlertNotifier.java` | **Tạo mới**: hàm quyết định (thuần Java) + tạo kênh, hiện thông báo |
| `ui/capture/CaptureExpenseActivity.java` | Sau khi lưu **khoản chi mới** thành công: gọi `BudgetAlertNotifier.checkAfterExpense(appContext, categoryId, amount, transactionDate)`. Chỉ thêm **1 lời gọi** |
| `ui/budget/BudgetFormDialog.java` | Lưu ngân sách thành công lần đầu → xin quyền thông báo (Android 13+) |
| `res/drawable/ic_budget_alert.xml` | Biểu tượng nhỏ trên thanh trạng thái (vector, 1 màu trắng) |
| `test/.../ui/budget/BudgetAlertNotifierTest.java` | **Tạo mới** |

## Quy tắc quyết định

Hàm thuần: `static int decide(double limit, double spentAfter, double amountAdded)` trả `NONE`, `WARNING`, `OVER`.

```text
spentBefore = spentAfter - amountAdded
pBefore = spentBefore / limit,  pAfter = spentAfter / limit
pBefore < 1.0  và  pAfter >= 1.0   → OVER       ("Vượt ngân sách Ăn uống: 2.150.000 / 2.000.000 đ")
pBefore < 0.8  và  0.8 <= pAfter < 1.0 → WARNING ("Ăn uống đã dùng 85% ngân sách tháng 10")
còn lại → NONE
```

Chỉ báo khi **vừa vượt ngưỡng** do khoản chi này, nên đã vượt rồi mà ghi thêm thì **không báo lại** (tránh làm phiền). Ngưỡng 80% / 100% trùng màu vàng / đỏ của Tab Ngân sách.

## Luồng

1. Lưu khoản chi mới thành công → lấy tháng/năm từ `transaction_date` của giao dịch vừa lưu.
2. `BudgetRepository.getBudgets(month, year, ...)` bằng **application context** (màn chụp đã `finish()` nên không dùng Activity).
3. Tìm ngân sách có `category_id` trùng. Không có → dừng.
4. `decide(limitAmount, spentAmount, amount)` → khác `NONE` thì hiện thông báo.
5. Mọi lỗi (mạng, 401, không có quyền) → **im lặng bỏ qua**. Đây là tính năng phụ, không được làm hỏng việc lưu giao dịch.

Thông báo:
- Kênh `budget_alert`, tên "Cảnh báo ngân sách", mức `IMPORTANCE_DEFAULT`. Tạo kênh mỗi lần gọi (gọi lại không sao).
- Tiêu đề "⚠️ Sắp hết ngân sách" (WARNING) hoặc "🔴 Đã vượt ngân sách" (OVER), nội dung như mục trên.
- Bấm vào → mở app (`getLaunchIntentForPackage`), `setAutoCancel(true)`. **Không** sửa `MainActivity` để nhảy thẳng Tab Ngân sách.
- Id thông báo = `category_id` → cùng danh mục thì thay thông báo cũ, khác danh mục thì hiện riêng.
- Android 13+ chưa có quyền `POST_NOTIFICATIONS` → không hiện (không crash).

Xin quyền: trong `BudgetFormDialog`, lưu thành công + Android 13+ + chưa có quyền + chưa hỏi lần nào (cờ trong `SharedPreferences` **riêng** tên `budget_alert_prefs`, không dùng `SharedPrefManager`) → gọi `requestPermissions`. Từ chối thì thôi, không hỏi lại.

## Test

### Unit test `BudgetAlertNotifierTest`

| Ca | `limit`, `spentAfter`, `amountAdded` | Kỳ vọng |
|---|---|---|
| N1 | 1.000.000, 850.000, 100.000 (75% → 85%) | `WARNING` |
| N2 | 1.000.000, 1.050.000, 300.000 (75% → 105%) | `OVER` (nhảy qua 80% thẳng lên 100% thì báo OVER) |
| N3 | 1.000.000, 1.200.000, 100.000 (110% → 120%) | `NONE` |
| N4 | 1.000.000, 900.000, 50.000 (85% → 90%) | `NONE` |
| N5 | 1.000.000, 1.000.000, 100.000 (90% → 100%) | `OVER` (đúng 100% là vượt, giống Phần 3) |
| N6 | 1.000.000, 500.000, 100.000 | `NONE` |

### Test trên máy

| Ca | Làm | Kỳ vọng |
|---|---|---|
| NA1 | Lần đầu đặt ngân sách Ăn uống 500.000 (Android 13+) | Hỏi quyền thông báo; chọn Cho phép |
| NA2 | Ghi chi Ăn uống 300.000, rồi 150.000 (60% → 90%) | Lần 2 có thông báo "Sắp hết ngân sách" |
| NA3 | Ghi thêm 100.000 (→ 110%) | Thông báo "Đã vượt ngân sách", thay thông báo cũ |
| NA4 | Ghi thêm 50.000 | Không thông báo mới |
| NA5 | Ghi chi danh mục không có ngân sách | Không thông báo |
| NA6 | Ghi **khoản thu** | Không thông báo |
| NA7 | Từ chối quyền rồi làm lại NA2 | Không thông báo, app không lỗi, giao dịch vẫn lưu |
| NA8 | Bấm vào thông báo | Mở app |

**Commit:** `chore: xin quyen thong bao trong manifest` (riêng) và `feat: thong bao khi sap vuot hoac vuot ngan sach`.

## Xong khi

- [ ] Đạt N1 → N6, NA1 → NA8.
