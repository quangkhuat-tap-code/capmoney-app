# 4C-3 · Tab Tài khoản xem được khi mất mạng

> **Phần 4C** · Trình bày: **Quang** · Chỉ có Android · Task trước: [4C-2](4C-2_Thong_Bao_Vuot_Ngan_Sach.md) · Task sau: [99](99_Nghiem_Thu_Va_PR.md)
> **Đọc trước:** Phần 3, [AN-3_Cache_Offline_Room.md](../phan-3/AN-3_Cache_Offline_Room.md) (cách `TransactionCache` đang làm)

## Mục tiêu

Tài liệu kỹ thuật gốc (mục 1.1) ghi Room lưu **giao dịch và ví**, nhưng Phần 3 mới lưu giao dịch (Tab Home, Tab Thống kê đã xem được offline). Task này làm nốt phần **ví**: mất mạng vẫn xem được số dư các ví ở Tab Tài khoản.

## File

| File | Việc |
|---|---|
| `data/local/WalletEntity.java` | **Tạo mới**. Bảng `wallet_cache`: `id` (khoá chính, id ví), `userId`, `name`, `type`, `balance`, `isDefault`, `sortOrder`, `cachedAt` |
| `data/local/WalletDao.java` | **Tạo mới**. `getAll(userId)` (theo `sortOrder`), `deleteAll(userId)`, `insertAll(list)`, `@Transaction replaceAll(userId, list)` |
| `data/local/WalletCache.java` | **Tạo mới**. Giống `TransactionCache`: chạy nền bằng executor, trả kết quả về main thread. `saveWallets(List<Wallet>)`, `getWallets(Callback<List<Wallet>>)`, `getCachedAt(Callback<Long>)` |
| `data/local/AppDatabase.java` | Thêm `WalletEntity.class` vào `entities`, `version = 2`, thêm `abstract WalletDao walletDao()` |
| `ui/wallets/WalletsViewModel.java` | Tải thành công → lưu cache; lỗi mạng → đọc cache |
| `ui/wallets/WalletsFragment.java` + `fragment_wallets.xml` | Banner `wallet_cache_banner` dưới thẻ Tổng tài sản; khoá nút Thêm ví / Chuyển tiền / Lịch sử khi đang offline |

> `fallbackToDestructiveMigration()` đã bật: lên `version = 2` thì cache cũ bị xoá, **không** mất dữ liệu thật (dữ liệu thật ở MySQL). Lần mở app đầu sau khi cập nhật, Tab Home/Thống kê offline sẽ trống cho tới khi có mạng lại một lần.

## Luồng

- Cache theo **người dùng** (`SharedPrefManager.getUserId()`), để đăng xuất rồi đăng nhập tài khoản khác không thấy ví của người trước.
- `loadWallets()` thành công → `WalletCache.saveWallets(list)` (giữ thứ tự server trả về qua `sortOrder`), ẩn banner.
- Lỗi đúng chữ `R.string.capture_error_network`:
  - Cache có dữ liệu → hiện danh sách ví và Tổng tài sản từ cache; banner "Đang xem số dư đã lưu lúc HH:mm dd/MM (không có mạng)" (dùng màu và drawable giống `tv_offline_banner` của Tab Home); **khoá** nút Thêm ví, Chuyển tiền, Lịch sử, và bấm vào ví không mở form sửa.
  - Cache trống → báo lỗi + Thử lại như hiện tại.
- Lỗi khác (`400`, `500`) → giữ cách báo lỗi hiện tại, **không** đọc cache.
- `401` → về màn Đăng nhập như cũ. Đăng xuất (`SessionManager.logout`) **không** cần xoá cache (đã lọc theo `userId`).

## Test

| Ca | Làm | Kỳ vọng |
|---|---|---|
| W1 | Có mạng, mở Tab Tài khoản | Như cũ, không banner |
| W2 | Tắt backend, bấm Thử lại / chuyển tab rồi quay lại | Thấy 4 ví và Tổng tài sản đã lưu, có banner giờ lưu |
| W3 | Ở trạng thái W2, bấm Thêm ví, Chuyển tiền, bấm vào 1 ví | Không mở gì (nút mờ) |
| W4 | Bật lại backend, Thử lại | Banner mất, nút bấm lại được |
| W5 | Tài khoản mới chưa từng mở Tab Tài khoản, tắt backend | Báo lỗi + Thử lại (không có cache) |
| W6 | Đăng nhập tài khoản B trên cùng máy, tắt backend | **Không** thấy ví của `demo@example.test` |
| W7 | Cài bản mới đè bản cũ (Room `version` 1 → 2) | App mở bình thường, không crash |
| W8 | Tab Home và Tab Thống kê offline (Phần 3, A-10) | Vẫn chạy như trước sau khi có mạng 1 lần |

**Commit:** `feat: tab tai khoan xem duoc so du da luu khi mat mang`.

## Xong khi

- [ ] Đạt W1 → W8.
- [ ] Đã ghi trong PR 4C: Room lên `version = 2`, cache cũ bị xoá một lần.
