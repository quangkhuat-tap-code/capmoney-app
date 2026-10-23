# AN-5 · Thêm/Sửa ví và Chuyển tiền

> **Phần 2** · Repo `capmoney-app` · Nhánh `feature/wallets-auth` · Task trước: [AN-4](AN-4_Tab_Tai_Khoan.md) · Task sau: [AN-6](AN-6_Tab_Ca_Nhan.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 5 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 2.5, 2.6, 2.7

## Mục tiêu

Từ Tab Tài khoản: thêm ví mới, sửa ví (tên, loại, số dư), chuyển tiền giữa 2 ví.

## File

| File | Việc |
|---|---|
| `ui/wallets/WalletFormDialog.java` | Tạo mới (dùng chung cho Thêm và Sửa) |
| `ui/wallets/TransferMoneyDialog.java` | Tạo mới |
| `res/layout/dialog_wallet_form.xml`, `dialog_transfer_money.xml` | Tạo mới |
| `ui/wallets/WalletsFragment.java` | Nối nút và bấm item vào 2 dialog, lắng nghe kết quả để tải lại |

## 1. Cách dialog báo Tab tải lại

- `WalletsFragment` mở dialog bằng `dialog.show(getChildFragmentManager(), "...")`.
- Trong `onViewCreated` của `WalletsFragment`:

  ```java
  getChildFragmentManager().setFragmentResultListener("wallet_changed", getViewLifecycleOwner(),
          (key, bundle) -> viewModel.loadWallets());
  ```

- Dialog thành công: `getParentFragmentManager().setFragmentResult("wallet_changed", new Bundle());` rồi `dismiss()`.

## 2. `WalletFormDialog` (`BottomSheetDialogFragment`)

`newInstance(Wallet walletOrNull)` (truyền qua `Bundle` bằng `putSerializable`). `null` = Thêm.

Bố cục `dialog_wallet_form.xml`:
- Tiêu đề `wallet_form_add_title` / `wallet_form_edit_title`.
- Ô Tên ví.
- Chọn loại: `MaterialButtonToggleGroup` 3 nút Tiền mặt / Ngân hàng / Tiết kiệm (`app:singleSelection="true"`, `app:selectionRequired="true"`).
- Ô Số dư (`inputType="number"`).
- Nút Lưu.

Hành vi:
- Ô số dư tự chèn dấu chấm khi gõ: trong `TextWatcher`, `long v = CurrencyUtils.parseDigits(s.toString())`, gỡ watcher, `setText(CurrencyUtils.formatInput(v))`, đặt con trỏ về cuối, gắn lại watcher.
- **Thêm**: mặc định loại Tiền mặt; số dư trống = 0. Gửi `{ name, type, balance }`.
- **Sửa**: điền sẵn tên, loại, số dư. Chỉ gửi field **đã đổi**. Nếu số dư đổi → `AlertDialog` `wallet_confirm_balance_title` / `wallet_confirm_balance_msg` (số cũ → số mới bằng `CurrencyUtils.format`), đồng ý mới gửi.
- Tên rỗng → lỗi `wallet_error_name` dưới ô, không gọi API.
- Đang gửi: khoá nút Lưu. Thành công → `Toast` `wallet_created` / `wallet_updated`, gửi kết quả (mục 1), đóng.
- Lỗi server (ví dụ `"Tên ví đã tồn tại"`) → hiện dưới ô tên, giữ dialog mở.

## 3. `TransferMoneyDialog` (`BottomSheetDialogFragment`)

`newInstance(ArrayList<Wallet> wallets)`.

Bố cục `dialog_transfer_money.xml`:
- Tiêu đề `transfer_title`.
- "Từ ví": `TextInputLayout` kiểu `ExposedDropdownMenu` + `MaterialAutoCompleteTextView`.
- Nút đảo chiều ⇅ ở giữa.
- "Đến ví": dropdown như trên.
- Ô Số tiền (32sp, tự chèn dấu chấm như mục 2) + dòng `transfer_available` (số dư ví nguồn).
- Ô Ghi chú.
- Nút `transfer_btn`.

Hành vi:
- Mỗi dòng dropdown: `"<emoji> <tên> · <số dư>"`.
- Mặc định: Từ = ví mặc định; Đến = ví đầu tiên khác ví nguồn.
- Bấm ⇅ → đổi chỗ 2 ví, dòng "Số dư khả dụng" đổi theo.
- Kiểm tra tại máy trước khi gửi: 2 ví khác nhau (`transfer_error_same`), số tiền > 0 (`transfer_error_amount`), số tiền ≤ số dư ví nguồn (`transfer_error_balance`).
- Thành công → `Toast` `transfer_success` với `CurrencyUtils.format(amount)`, gửi kết quả (mục 1), đóng.

## 4. Nối vào `WalletsFragment`

- Bấm 1 ví → `WalletFormDialog.newInstance(wallet)`.
- Bấm "+ Thêm ví mới" → `WalletFormDialog.newInstance(null)`.
- Bấm "Chuyển tiền": số ví < 2 → `Toast transfer_need_two_wallets`; ngược lại `TransferMoneyDialog.newInstance(danh sách ví hiện có)`.

## Test

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| W2 | Thêm ví "Tiết kiệm", loại Tiết kiệm, số dư 1.000.000 | Ví hiện ở **Sổ tiết kiệm**; tổng tài sản tăng 1.000.000 |
| W3 | Thêm ví trùng tên (khác hoa thường) | "Tên ví đã tồn tại" dưới ô tên, dialog vẫn mở |
| W4 | Gõ số dư 1500000 | Ô hiện "1.500.000" khi đang gõ |
| W5 | Sửa tên ví | Danh sách cập nhật, số dư giữ nguyên |
| W6 | Sửa số dư | Hộp xác nhận số cũ → số mới; Huỷ thì không đổi, đồng ý mới lưu |
| T1 | Chuyển 500.000 từ Tiền mặt sang Ngân hàng | Toast "Đã chuyển 500.000 đ"; 2 số dư cập nhật; tổng tài sản **không đổi** |
| T2 | Chuyển nhiều hơn số dư | Báo lỗi tại máy, không gọi API |
| T3 | Chọn 2 ví giống nhau | Báo lỗi |
| T4 | Bấm ⇅ | 2 ví đổi chỗ, "Số dư khả dụng" đổi theo |
| T5 | Tài khoản chỉ có 1 ví bấm Chuyển tiền | Toast "Cần ít nhất 2 ví để chuyển tiền" |
| X2 | Mở dialog rồi xoay màn hình | Không crash |

## Commit

```text
feat: them sua vi va chuyen tien

- Them WalletFormDialog dung chung cho them va sua vi, chon loai vi, o so du tu chen dau cham, xac nhan khi doi so du
- Them TransferMoneyDialog: chon vi nguon/dich, dao chieu, kiem tra so du truoc khi gui
- WalletsFragment mo 2 dialog va tai lai danh sach khi co thay doi
```

## Xong khi

- [ ] Đạt W2 → W6, T1 → T5, X2.
- [ ] Đã commit, không có dòng co-author.
