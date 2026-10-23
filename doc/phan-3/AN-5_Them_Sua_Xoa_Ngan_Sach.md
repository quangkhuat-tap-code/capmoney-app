# AN-5 · Thêm, sửa, xoá ngân sách

> **Phần 3** · Repo `capmoney-app` · Nhánh `feature/stats-budget` · Task trước: [AN-4](AN-4_Tab_Ngan_Sach.md) · Task sau: [99_Nghiem_Thu_Va_PR](99_Nghiem_Thu_Va_PR.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 5 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 2.3, 2.4

## Mục tiêu

Đặt hạn mức mới, sửa hạn mức, xoá ngân sách từ Tab Ngân sách.

## File

| File | Việc |
|---|---|
| `ui/budget/BudgetFormDialog.java` | Tạo mới |
| `res/layout/dialog_budget_form.xml` | Tạo mới |
| `ui/budget/BudgetFragment.java`, `BudgetViewModel.java`, `BudgetAdapter.java` | Nối nút Thêm, bấm item (sửa), nhấn giữ item (xoá) |

## 1. `BudgetFormDialog` (`BottomSheetDialogFragment`)

`newInstance(int month, int year, ArrayList<Integer> budgetedCategoryIds, Budget editingOrNull)`.

Bố cục `dialog_budget_form.xml`:

```text
Đặt ngân sách                         (hoặc "Sửa hạn mức")
Áp dụng cho Tháng 10/2026

Danh mục chi tiêu
[🍜 Ăn uống] [🚗 Di chuyển] [🛍️ Mua sắm] [🎮 Giải trí]     ChipGroup singleSelection, chip tạo bằng code
[🧾 Hóa đơn] [💊 Sức khỏe] [📚 Giáo dục] [📦 Khác]
ⓘ Danh mục này đã có ngân sách tháng này, lưu sẽ cập nhật hạn mức     (chỉ khi chọn danh mục đã có)

Hạn mức trong tháng
[ 2.000.000            đ ]           32sp, tự chèn dấu chấm khi gõ

[ Huỷ ]                 [ Lưu ]
```

Hành vi:
- Danh mục lấy từ `BudgetRepository.getExpenseCategories`. Đang tải → ProgressBar nhỏ thay chỗ ChipGroup. Lỗi → chữ lỗi + Thử lại.
- Chọn chip có id nằm trong `budgetedCategoryIds` → hiện dòng `budget_form_existing_hint`.
- Chế độ **Sửa**: chọn sẵn và **khoá** ChipGroup ở danh mục đang sửa; điền sẵn hạn mức; tiêu đề `budget_form_edit_title`.
- Ô hạn mức: `TextWatcher` → `CurrencyUtils.parseDigits(s.toString())`, gỡ watcher, `setText(CurrencyUtils.formatInput(v))`, con trỏ về cuối, gắn lại watcher.
- Kiểm tra tại máy: chưa chọn danh mục → `budget_error_category`; hạn mức ≤ 0 → `budget_error_limit`. Không gọi API.
- Lưu → `saveBudget(categoryId, limit, month, year)`; đang gửi khoá nút. Thành công → `Toast budget_saved`, báo Fragment tải lại (mục 2), đóng.
- Lỗi server → hiện message, giữ dialog mở.

## 2. Cách dialog báo Tab tải lại

- `BudgetFragment` mở dialog: `dialog.show(getChildFragmentManager(), "budget_form")`.
- Trong `onViewCreated` của `BudgetFragment`:

  ```java
  getChildFragmentManager().setFragmentResultListener("budget_changed", getViewLifecycleOwner(),
          (key, bundle) -> viewModel.load());
  ```

- Dialog thành công: `getParentFragmentManager().setFragmentResult("budget_changed", new Bundle());`

## 3. Nối vào `BudgetFragment`

- Nút "+ Thêm" và nút `budget_empty_action` → form chế độ Thêm, truyền tháng đang chọn và danh sách `category_id` đã có ngân sách.
- Bấm 1 dòng → form chế độ Sửa với `Budget` đó.
- **Nhấn giữ** 1 dòng → `AlertDialog` `budget_delete_title` / `budget_delete_message` (ví dụ "Xoá hạn mức 2.000.000 đ của Ăn uống?") → đồng ý: `deleteBudget(id)`, `Toast budget_deleted`, tải lại.

## Test

Dữ liệu như AN-4 (BE-1 + B1, B2, B3).

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| BU2 | Thêm ngân sách Mua sắm 100.000, rồi ghi 1 khoản chi Mua sắm 85.000 bằng màn chụp (+) | Mua sắm 85% thanh **vàng** |
| BU3 | Mở form, chọn Ăn uống (đã có) | Hiện dòng "đã có ngân sách…"; lưu → cập nhật hạn mức, **không** thêm dòng mới |
| BU4 | Lưu khi chưa chọn danh mục / hạn mức 0 | Báo lỗi tại máy, không gọi API |
| BU5 | Gõ 2000000 vào ô hạn mức | Hiện "2.000.000" khi đang gõ |
| BU6 | Bấm 1 dòng | Form Sửa, danh mục bị khoá, hạn mức điền sẵn |
| BU7 | Nhấn giữ 1 dòng → Xoá | Hỏi xác nhận; đồng ý thì dòng biến mất, thẻ tổng cập nhật; Huỷ thì không đổi |
| BU12 | Mở form rồi xoay màn hình | Không crash |

## Commit

```text
feat: them sua xoa ngan sach

- Them BudgetFormDialog: chon danh muc chi tieu bang chip, o han muc tu chen dau cham, bao khi danh muc da co ngan sach
- Bam dong de sua han muc (khoa danh muc), nhan giu de xoa co xac nhan
- Tab Ngan sach tai lai sau khi luu hoac xoa
```

## Xong khi

- [ ] Đạt BU2 → BU7, BU12.
- [ ] Đã commit, không có dòng co-author.
- [ ] Sang [99_Nghiem_Thu_Va_PR.md](99_Nghiem_Thu_Va_PR.md).
