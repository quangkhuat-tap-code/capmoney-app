# AN-4 · Tab Tài khoản: tổng tài sản, danh sách ví

> **Phần 2** · Repo `capmoney-app` · Nhánh `feature/wallets-auth` · Task trước: [AN-3](AN-3_Doi_Man_Mo_Dau.md) · Task sau: [AN-5](AN-5_Them_Sua_Vi_Chuyen_Tien.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 5 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 2.4

## Mục tiêu

Tab 3 hiển thị Tổng tài sản và danh sách ví chia 2 nhóm. Có sẵn 2 nút "Thêm ví" và "Chuyển tiền" (task này chỉ cần nút hiện ra; dialog làm ở AN-5).

## File

| File | Việc |
|---|---|
| `ui/wallets/WalletsFragment.java` | Viết lại (đang rỗng) |
| `ui/wallets/WalletsViewModel.java` | Tạo mới: `loadWallets()`, LiveData danh sách, loading, lỗi |
| `ui/wallets/WalletAdapter.java` | Tạo mới |
| `res/layout/fragment_wallets.xml`, `item_wallet.xml` | Tạo mới |
| `res/drawable/bg_wallet_total_card.xml`, `bg_wallet_badge.xml` | Tạo mới |

## Bố cục `fragment_wallets.xml` (`NestedScrollView` bọc toàn bộ)

```text
Tài khoản                                      (tiêu đề 22sp đậm)

┌──────────────────────────────────────────┐   bg_wallet_total_card: gradient color_accent → #3A4FB0, bo 20dp
│ Tổng tài sản                              │
│ 7.000.000 đ                               │   32sp đậm, trắng
│ 2 ví                                      │
└──────────────────────────────────────────┘

[ + Thêm ví mới ]   [ Chuyển tiền giữa các ví ]    2 nút cạnh nhau; nút phụ viền color_accent

Ví thanh toán
┌──────────────────────────────────────────┐
│ 💵  Tiền mặt     [Mặc định]   2.000.000 đ │   item_wallet.xml
│ 🏦  Ngân hàng                  5.000.000 đ │
└──────────────────────────────────────────┘

Sổ tiết kiệm
  Chưa có sổ tiết kiệm                          (khi rỗng)

[ProgressBar giữa màn khi đang tải lần đầu]
[Khối lỗi: chữ lỗi + nút "Thử lại" khi tải thất bại]
```

Chừa `paddingBottom="88dp"` cho nội dung cuộn để dòng cuối không bị nút (+) của `MainActivity` che.

## Hành vi

- 2 `RecyclerView` (`nestedScrollingEnabled="false"`), cùng dùng `WalletAdapter`. "Ví thanh toán" gồm `cash` + `bank`; "Sổ tiết kiệm" gồm `saving`. Giữ thứ tự server trả.
- `item_wallet.xml`: emoji loại ví (`getTypeEmoji()`, 24sp) trong ô tròn `bg_card_secondary` 44dp; tên ví 16sp; nhãn `wallet_badge_default` (`bg_wallet_badge`: nền `color_accent` mờ 20%, chữ `color_accent` 11sp) chỉ khi `isDefault()`; số dư bên phải `CurrencyUtils.format(balance)`.
- Tổng tài sản = tổng `balance` **mọi** ví. Dòng dưới = `wallet_count` (số ví).
- Tải ở `onResume` (để quay lại từ màn chụp ảnh thấy số dư mới). Đã có dữ liệu cũ thì vẫn hiện trong lúc tải lại, không nháy trắng.
- Bấm 1 ví / bấm 2 nút: task này để trống hoặc `Toast` tạm; AN-5 nối dialog.
- API trả 401 → `SessionManager.logout(requireActivity(), true)`.
- Xoay màn hình: không gọi lại API (ViewModel giữ dữ liệu).

## Test

Backend có BE-3. Đăng nhập bằng tài khoản vừa đăng ký, hoặc dùng user 1: tạm lưu token dev như Hướng dẫn mục 10 (không commit).

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| W1 | Mở Tab Tài khoản (user 1 sau khi chạy seed) | Tổng 7.000.000 đ, "2 ví"; Tiền mặt có nhãn Mặc định đứng đầu; mục Sổ tiết kiệm hiện "Chưa có sổ tiết kiệm" |
| W1b | Thêm 1 ví `saving` bằng curl (ca W2 ở BE-3), quay lại tab | Ví mới nằm ở mục Sổ tiết kiệm, tổng tăng |
| W7 | Ghi 1 khoản chi bằng màn chụp (+), quay lại Tab Tài khoản | Số dư ví đã trừ |
| W8 | Tắt backend, mở tab | Khối lỗi + Thử lại; bật backend bấm Thử lại → có dữ liệu |
| X1 | Xoay màn hình | Không crash, không gọi lại API |

## Commit

```text
feat: tab tai khoan danh sach vi

- WalletsFragment: the tong tai san, danh sach Vi thanh toan va So tiet kiem, nhan Mac dinh
- WalletsViewModel tai danh sach o onResume, giu du lieu khi xoay man hinh, ve dang nhap khi 401
- Them WalletAdapter, fragment_wallets.xml, item_wallet.xml
```

## Xong khi

- [ ] Đạt W1, W1b, W7, W8, X1.
- [ ] Không còn dòng `DEV ONLY`.
- [ ] Đã commit, không có dòng co-author.
