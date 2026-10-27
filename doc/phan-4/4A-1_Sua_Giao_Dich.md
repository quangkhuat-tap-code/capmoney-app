# 4A-1 · Sửa giao dịch

> **Phần 4A** · Trình bày: **Lâm Anh** · Code + test: Lâm Anh · Nhánh `lamanh` (2 repo) · Task sau: [4A-2](4A-2_Tim_Kiem_Giao_Dich.md)
> **Đọc trước:** [00_Tong_Quan_Phan_4.md](00_Tong_Quan_Phan_4.md) mục 3 · [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) mục 2.1

## Mục tiêu

Ghi nhầm số tiền, ví, danh mục, ngày hay ghi chú thì sửa được, **số dư ví tự tính lại đúng**. Hiện tại chỉ có cách xoá rồi chụp lại.

## Backend

| File | Việc |
|---|---|
| `src/controllers/transaction.controller.js` | Thêm `updateTransaction`, export |
| `src/routes/transaction.routes.js` | Thêm `router.put('/:id', auth, updateTransaction);` |

1. Kiểm tra dữ liệu theo đúng thứ tự bảng mục 2.1 file 01. Dùng lại `isValidDateTimeString`. Riêng `transaction_date` ở đây **bắt buộc** (app luôn gửi).
2. Trong 1 DB transaction:
   - Khoá giao dịch: `SELECT id, wallet_id, amount, type FROM transactions WHERE id = ? AND user_id = ? FOR UPDATE`.
   - Kiểm tra danh mục: `SELECT id, type FROM categories WHERE id = ? AND (user_id IS NULL OR user_id = ?)`; khác `type` của giao dịch cũ → 400.
   - Khoá ví `[oldWalletId, newWalletId]` (bỏ trùng, sắp tăng) bằng `... WHERE id IN (?) AND user_id = ? ORDER BY id FOR UPDATE`. Ví mới không có trong kết quả → 404 `Ví không tồn tại`.
   - Tính số dư trên một object `balances[id]`: hoàn tác cũ, áp mới (mục 2.1 file 01). Có ví âm → 400.
   - `UPDATE wallets SET balance = ? WHERE id = ?` cho từng ví; `UPDATE transactions SET amount = ?, wallet_id = ?, category_id = ?, note = ?, transaction_date = ? WHERE id = ?`.
   - Commit, đọc lại bằng `BASE_TRANSACTION_QUERY + ' WHERE t.id = ?'`, trả `mapTransaction`.
3. Làm tròn `amount` 2 chữ số như `createTransaction`. `note` rỗng → `null`.

### Test backend (token của `demo@example.test`)

| Ca | Làm | Kỳ vọng |
|---|---|---|
| E1 | Sửa khoản chi 100.000 → 150.000, cùng ví | `200`; ví giảm thêm 50.000 |
| E2 | Sửa khoản chi 150.000 → 80.000 | Ví tăng lại 70.000 |
| E3 | Chuyển khoản chi 80.000 từ ví A sang ví B | Ví A +80.000, ví B −80.000 |
| E4 | Sửa khoản chi lên lớn hơn số dư ví + số cũ | `400 "Số dư ví không đủ..."`, số dư **không đổi** |
| E5 | Sửa khoản **thu** xuống thấp khi ví đã tiêu gần hết | `400 "Số dư ví không đủ..."` |
| E6 | Đổi danh mục chi sang danh mục **Lương** | `400 "Danh mục không khớp loại giao dịch"` |
| E7 | `PUT /transactions/abc`; id không tồn tại; id của user khác | `400`; `404`; `404` |
| E8 | `amount: 0`; `note` 501 ký tự; `transaction_date: "2026-13-01 00:00:00"` | 3 lỗi `400` đúng chữ |
| E9 | Sửa ngày sang tháng 9 rồi `GET /transactions?month=9` | Giao dịch nằm ở tháng 9, không còn ở tháng 10 |

Sau mỗi ca kiểm tra tổng số dư bằng SQL: số dư ví = số dư đầu + tổng thu − tổng chi ± chuyển tiền (không lệch).

**Commit backend:** `feat: api sua giao dich` (thân: liệt kê PUT, cách hoàn tác số dư, khoá ví theo thứ tự id).

## Android

| File | Việc |
|---|---|
| `data/api/TransactionApi.java` | Thêm `@PUT("transactions/{id}") Call<ApiResponse<Transaction>> updateTransaction(@Path("id") int id, @Body Map<String, Object> body);` |
| `data/repository/TransactionRepository.java` | Thêm `updateTransaction(...)`, 401 → `SessionManager.notifyExpired()` như các hàm khác |
| `ui/home/TransactionDetailDialog.java` + `dialog_transaction_detail.xml` | Thêm nút `btn_edit_tx` (viền, chữ "Sửa") **bên trái** nút Xoá, 2 nút chia đôi hàng |
| `ui/capture/CaptureExpenseActivity.java` + `CaptureViewModel.java` | Thêm chế độ sửa |

### Chế độ sửa

- Hằng `CaptureExpenseActivity.EXTRA_EDIT_TX` (`Transaction`, `Serializable` có sẵn). Hàm tiện `static Intent editIntent(Context, Transaction)`.
- Bấm **Sửa** trong hộp chi tiết: `dismiss()` rồi `startActivity(editIntent(...))`. Không cần trả kết quả: `HomeFragment.onResume()` tự tải lại.
- Có `EXTRA_EDIT_TX` thì:
  - Tiêu đề "Sửa giao dịch"; nút Lưu ghi "Cập nhật".
  - Ảnh: có `image_url` thì hiện bằng Glide (`ApiClient.IMAGE_BASE_URL + image_url`), **ẩn** nút chụp/chọn ảnh. Không có ảnh thì hiện emoji danh mục như ô lưới Home.
  - Nút Chi/Thu: chọn sẵn theo giao dịch và **khoá** (`setEnabled(false)`).
  - Điền sẵn số tiền, ghi chú, ngày giờ. Ví và danh mục: sau khi `loadOptions()` xong thì chọn đúng `wallet_id`, `category_id` (không có trong danh sách thì để trống, bắt người dùng chọn lại).
  - Kiểm tra số dư phía app: với khoản chi, cho phép tối đa `số dư ví + số tiền cũ` nếu **cùng ví**, `số dư ví` nếu **đổi ví**.
  - Lưu: `CaptureViewModel.update(txId)` gọi PUT. Thành công → Toast "Đã cập nhật giao dịch", `finish()`.
- Xoay màn hình không mất dữ liệu đang sửa (ViewModel giữ cờ `editing` và `txId`, chỉ điền sẵn **lần đầu**).

### Test Android (emulator)

| Ca | Làm | Kỳ vọng |
|---|---|---|
| EA1 | Home → bấm ảnh → **Sửa** | Mở màn sửa, đủ dữ liệu, Chi/Thu bị khoá, không có nút chụp |
| EA2 | Đổi 100.000 → 250.000, Cập nhật | Về Home, ô lưới và thẻ tổng Chi cập nhật; Tab Tài khoản số dư giảm 150.000 |
| EA3 | Đổi ví | Tab Tài khoản: ví cũ tăng, ví mới giảm |
| EA4 | Nhập số vượt số dư | Báo lỗi ngay tại màn, không gọi API |
| EA5 | Đổi ngày sang tháng trước | Giao dịch biến khỏi tháng này, xuất hiện ở tháng trước |
| EA6 | Đang sửa thì xoay màn hình | Giữ nguyên số đã gõ |
| EA7 | Tắt backend rồi Cập nhật | Báo lỗi mạng, ở lại màn sửa |
| EA8 | Giao dịch không có ảnh | Màn sửa hiện emoji danh mục |

**Commit app:** `feat: sua giao dich tu hop chi tiet`.

## Xong khi

- [ ] Đạt E1 → E9, EA1 → EA8.
- [ ] 2 commit (backend, app), không có dòng co-author.
