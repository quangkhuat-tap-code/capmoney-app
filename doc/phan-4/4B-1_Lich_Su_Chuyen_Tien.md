# 4B-1 · Lịch sử chuyển tiền

> **Phần 4B** · Trình bày: **Trung** · Code + test: Lâm Anh · Task trước: [4A-3](4A-3_Doc_So_Tien_Hoa_Don.md) · Task sau: [4B-2](4B-2_Doi_Mat_Khau.md)
> **Đọc trước:** [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) mục 2.3

## Mục tiêu

Mỗi lần chuyển tiền giữa các ví (Phần 2, BE-4) đã được ghi vào bảng `wallet_transfers`, nhưng người dùng **chưa xem lại được**. Task này thêm màn xem lịch sử theo tháng.

## Backend

| File | Việc |
|---|---|
| `src/controllers/wallet.controller.js` | Thêm `getTransfers`, export |
| `src/routes/wallet.routes.js` | `router.get('/transfers', auth, getTransfers);` đặt **trước** `router.put('/:id', ...)` |

```js
const range = parseMonthYear(req.query.month, req.query.year); // null → 400
const [rows] = await pool.query(
  `SELECT wt.id, wt.amount, wt.note, wt.transfer_date,
          wt.from_wallet_id, fw.name AS from_wallet_name,
          wt.to_wallet_id,   tw.name AS to_wallet_name
   FROM wallet_transfers wt
   JOIN wallets fw ON fw.id = wt.from_wallet_id
   JOIN wallets tw ON tw.id = wt.to_wallet_id
   WHERE wt.user_id = ? AND wt.transfer_date >= ? AND wt.transfer_date < ?
   ORDER BY wt.transfer_date DESC, wt.id DESC`,
  [req.user.id, range.start, range.end]
);
// amount ép Number; note null giữ null
```

### Test backend

| Ca | Làm | Kỳ vọng |
|---|---|---|
| T1 | Chuyển 2 lần trong tháng 10 (qua app hoặc `POST /wallets/transfer`), rồi `GET /wallets/transfers?month=10&year=2026` | 2 dòng, mới nhất trước, đủ tên ví nguồn/đích |
| T2 | Không truyền tháng | Ra tháng hiện tại |
| T3 | `month=11&year=2026` | `200`, `data: []` |
| T4 | `month=13`; `year=abc` | `400 "Tham số lọc không hợp lệ"` |
| T5 | Đổi tên 1 ví (`PUT /wallets/:id`) rồi gọi lại T1 | Lịch sử hiện **tên mới** (đúng, vì JOIN theo id) |
| T6 | Token user khác | `data: []` |

**Commit backend:** `feat: api lich su chuyen tien`.

## Android

| File | Việc |
|---|---|
| `model/Transfer.java` | Các trường như JSON mục 2.3 file 01 (`@SerializedName`) |
| `data/api/WalletApi.java` | `@GET("wallets/transfers") Call<ApiResponse<List<Transfer>>> getTransfers(@Query("month") int month, @Query("year") int year);` |
| `data/repository/WalletRepository.java` | `getTransfers(month, year, RepoCallback<List<Transfer>>)` |
| `ui/wallets/TransferHistoryBottomSheet.java`, `TransferHistoryAdapter.java`, `TransferHistoryViewModel.java` | **Tạo mới** (BottomSheetDialogFragment, không cần khai báo manifest; ViewModel giữ tháng khi xoay màn hình) |
| `res/layout/bottom_sheet_transfer_history.xml`, `item_transfer_history.xml` | **Tạo mới** |
| `ui/wallets/WalletsFragment.java` + `fragment_wallets.xml` | Thêm nút chữ `wallet_btn_history` "🕘 Lịch sử chuyển tiền" **dưới** hàng nút Thêm ví / Chuyển tiền (3 nút cùng hàng thì chữ "Chuyển tiền giữa các ví" bị xuống dòng) |

### Giao diện

```text
            Lịch sử chuyển tiền
       ‹        Tháng 10/2026        ›
 ┌─────────────────────────────────────────┐
 │ Tiền mặt  →  Sổ tiết kiệm     500.000 đ │
 │ Gửi tiết kiệm            21/10 09:15    │
 ├─────────────────────────────────────────┤
 │ Techcombank → Tiền mặt      1.000.000 đ │
 │ (không ghi chú)          18/10 17:40    │
 └─────────────────────────────────────────┘
```

- Thanh chọn tháng giống Phần 3 (nút › mờ ở tháng hiện tại). Mặc định tháng hiện tại.
- Số tiền màu `color_accent` (không phải thu hay chi). Ghi chú trống → dòng phụ chỉ có ngày giờ.
- Trống → "Tháng này chưa chuyển tiền lần nào". Lỗi → chữ lỗi + Thử lại. 401 → `SessionManager.notifyExpired()`.
- Chuyển tiền xong (dialog Phần 2) mà bottom sheet đang mở thì không cần tự cập nhật; mở lại là thấy.

### Test Android

| Ca | Làm | Kỳ vọng |
|---|---|---|
| TA1 | Tab Tài khoản → Lịch sử | Bottom sheet tháng hiện tại, đúng 2 lần chuyển ở T1 |
| TA2 | Bấm ‹ | Tháng 9, trống, đúng chữ |
| TA3 | Chuyển tiền mới → mở lại Lịch sử | Có dòng mới ở đầu |
| TA4 | Tắt backend → mở Lịch sử | Báo lỗi mạng + Thử lại |
| TA5 | Xoay màn hình khi đang mở | Giữ tháng đang chọn |

**Commit app:** `feat: xem lich su chuyen tien o tab tai khoan`.

## Xong khi

- [ ] Đạt T1 → T6, TA1 → TA5.
