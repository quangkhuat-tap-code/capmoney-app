# Tiến độ nhóm CapMoney

> Cập nhật: **27/10/2026**. Ai xong task nào thì sửa cột "Trạng thái" của task đó và ghi ngày cập nhật ở dòng này.
> Chi tiết từng task: Phần 1 ở `doc/phan-1/`, Phần 2 ở `doc/phan-2/`, Phần 3 ở `doc/phan-3/`. Quy trình chung: [Huong_Dan_Lam_Viec_Nhom.md](Huong_Dan_Lam_Viec_Nhom.md).

Ký hiệu trạng thái:

| Ký hiệu | Nghĩa |
|---|---|
| ✅ | Xong (đã commit, đã test đạt) |
| 🔄 | Đang làm |
| ⬜ | Chưa làm, **làm được ngay** |
| ⏳ | Chưa làm, **đang chờ** task khác (xem cột "Chờ") |

---

## 1. Tổng quan

| Phần | Người | Nội dung | Nhánh (cả 2 repo) | BE | FE | Ghi chú |
|---|---|---|---|---|---|---|
| Phần 1 | **Lâm Anh** | Chụp ảnh ghi chi tiêu, Tab Home (lưới + lịch), API giao dịch, API danh mục | `lamanh` | ✅ 5/5 | ✅ 7/7, nghiệm thu xong với API ví thật | Xong cả C15 trên điện thoại thật |
| Phần 2 | **Trung** (Lâm Anh làm thay) | Đăng nhập/đăng ký, Tab Tài khoản (ví, chuyển tiền), Tab Cá nhân | **`lamanh`** | ✅ 4/4 | ✅ 6/6, nghiệm thu đạt | Đã merge [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2), [backend #2](https://github.com/quangkhuat-tap-code/backend-cap-money/pull/2) vào `develop` (27/10) |
| Phần 3 | **Quang** (Lâm Anh làm thay) | Tab Thống kê (donut), Tab Ngân sách, cache offline Room | **`lamanh`** | ✅ 2/2 | ✅ 5/5, nghiệm thu đạt | Đã merge cùng [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2), [backend #2](https://github.com/quangkhuat-tap-code/backend-cap-money/pull/2) |

27/10/2026: toàn bộ Phần 2 và Phần 3 đã làm xong trên nhánh **`lamanh`** (không dùng `feature/wallets-auth`, `feature/stats-budget`). Trung, Quang **không** làm lại; kéo `lamanh` về để review.

### Chuỗi phụ thuộc quan trọng

```text
✅ Lâm Anh: PR #1 app + backend lamanh → develop (đã merge 24/10)
✅ Phần 2 + Phần 3 + sửa insets Phần 1 trên lamanh (27/10, đã push)
✅ PR #2 lamanh → develop ở cả 2 repo (merge 27/10) ──► cả nhóm kéo develop: app mở vào màn Đăng nhập, bỏ dòng DEV ONLY
```

---

## 2. Phần 1 · Lâm Anh

### 2.1. Backend (repo `backend-cap-money`, nhánh `lamanh`)

| Mã | Task | Trạng thái | Chờ | Ghi chú |
|---|---|---|---|---|
| B-1 | Khung backend dùng chung (`app.js`, `db.js`, `auth.middleware.js`, `database_init.sql`, scripts) | ✅ | — | Đã có trên `develop` |
| B-2 | `GET /api/categories` | ✅ | — | Đã có trên `develop` |
| B-3 | Middleware upload ảnh `multer` | ✅ | — | Đã nâng lên `multer@2.5.0` (commit `4c5733a`, chỉ trên `lamanh`) |
| B-4 | `POST /api/transactions`, `GET /api/transactions` | ✅ | — | Đã có trên `develop` |
| B-5 | `DELETE /api/transactions/:id` (hoàn số dư, xoá ảnh) | ✅ | — | Đã có trên `develop` |
| B-T | Test 15 ca B1–B15 | ✅ | — | 15/15 đạt ngày 23/10/2026 |
| B-6 | Seed danh mục chạy lại không trùng, README, sửa comment route | ✅ | — | Commit `4c5733a`, chỉ trên `lamanh` |
| B-PR | PR `lamanh` → `develop` | ✅ | — | PR #1 đã merge 24/10/2026 (`96c4a88`). `develop` backend đã có README + `multer@2` |

### 2.2. Android (repo `capmoney-app`, nhánh `lamanh`)

| Mã | Task | Trạng thái | Chờ | Ghi chú |
|---|---|---|---|---|
| A-0 | Sửa cấu hình jitpack để build được | ✅ | — | Commit `4b98229` |
| A-1 | Khai báo manifest (INTERNET, FileProvider…) | ✅ | — | Commit `9ed9d55` |
| A-2 | Model, API, repository giao dịch, `CurrencyUtils`, `DateUtils`, `ImageUtils` | ✅ | — | Commit `724987a`. Có unit test, 8/8 đạt |
| A-3 | Màn chụp ảnh ghi chi tiêu | ✅ | — | Commit `f2924b5` |
| A-4 | Tab Home dạng lưới ảnh | ✅ | — | Commit `f47ba66` |
| A-5 | Tab Home dạng lịch | ✅ | — | Commit `cbb726c` |
| A-6 | Chi tiết và xoá giao dịch | ✅ | — | Commit `b7e0017` |
| A-7 | `.gitignore` bỏ qua `.env` và `/doc/` | ✅ | — | Commit `5b5b616` |
| A-T1 | Test Tab Home trên emulator (H1–H15) | ✅ | — | Xong 24/10/2026, dùng token dev. Không phát sinh lỗi, không cần commit sửa |
| A-PR | Push `lamanh` + PR `lamanh` → `develop` | ✅ | — | PR #1 đã merge 24/10/2026 (`b5f382a`). `develop` app đã có Phần 1, build được |
| A-T2 | Test màn chụp ảnh (C1–C15) bằng **route giả** `/api/wallets` | ✅ | — | 14/15 đạt (C15 đạt ở A-T2c), 0 lỗi. C15 (ảnh camera 12MP) chưa test được trên emulator. Route giả đã xoá, không commit |
| A-T2b | Test lại màn chụp ảnh với `GET /api/wallets` **thật** | ✅ | — | 27/10/2026, emulator Pixel 7 Pro API 36: C1, C6, C9, C10 đạt 4/4. Ví mặc định, chip số dư đọc đúng từ API thật; C9 chặn ở client, không gọi API |
| A-T2c | Thử C15 (ảnh camera 12MP) trên **điện thoại thật** | ✅ | — | Đạt trên Vivo V2035 (Android 13). Ảnh gốc 4000×3000, 2,5MB → file tải lên 960×1280, 98KB, đứng đúng chiều. Dùng `adb reverse tcp:5000` + `127.0.0.1` tạm trong `ApiClient`, đã trả lại, không commit |
| A-T3 | Nghiệm thu toàn luồng (7 bước, file 06 mục 4) | ✅ | — | 27/10/2026, đạt 7/7. Bước 4: `GET /api/wallets` thật trả ví 1 = 10.000.000 đ, file ảnh đã xoá. DB có sẵn giao dịch cũ nên thẻ Chi tiêu so theo chênh lệch (+50.000) |
| A-8 | Sửa Tab Home + màn chụp bị đè lên status bar (Android 15+) | ✅ | — | Commit `c026a0f`, đã push `lamanh`. `targetSdk 37` bắt buộc edge-to-edge; thêm padding theo insets trong `HomeFragment`, `CaptureExpenseActivity` |
| A-PR2 | PR #2 `lamanh` → `develop` ở cả 2 repo (Phần 1 A-8 + toàn bộ Phần 2, 3) | ✅ | — | Merge 27/10/2026: [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2), [backend #2](https://github.com/quangkhuat-tap-code/backend-cap-money/pull/2). App `35a1465`, backend `3bce232` |
| A-9 | Tab Home, Thống kê, Ngân sách, màn chụp tự về Đăng nhập khi token hết hạn (401) | ✅ | — | Commit `d78bb8f`, đã merge [app #3](https://github.com/quangkhuat-tap-code/capmoney-app/pull/3) vào `develop` (`6e3c2ee`). Test 5 ca trên emulator đạt |
| A-10 | Tab Home xem được giao dịch đã lưu (Room) khi mất mạng, có banner offline | ✅ | — | Commit `eed6a90`, đã merge [app #4](https://github.com/quangkhuat-tap-code/capmoney-app/pull/4) vào `develop` (`010ae3f`). Test 4 ca trên emulator đạt |
| A-11 | Thẻ Chi tiêu / Thu nhập ở Tab Home hiện "0 đ" thay vì "-0 đ" / "+0 đ" khi tháng không có giao dịch | ✅ | — | Commit `cc3d00b`, đã merge [app #5](https://github.com/quangkhuat-tap-code/capmoney-app/pull/5) vào `develop` (`913d76d`). Test tháng có / không có giao dịch trên emulator đạt |

---

## 3. Phần 2 · Trung (Lâm Anh làm thay trên `lamanh`)

### 3.1. Backend (repo `backend-cap-money`, nhánh `lamanh`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| BE-1 | `POST /api/auth/register`, `POST /api/auth/login` | ✅ | — | [BE-1](phan-2/BE-1_Api_Dang_Ky_Dang_Nhap.md) · Lâm Anh làm thay, commit `389dd0e`. Đạt A1→A10, A13 |
| BE-2 | `GET /api/auth/profile` | ✅ | — | [BE-2](phan-2/BE-2_Api_Thong_Tin_Ca_Nhan.md) · Lâm Anh làm thay, commit `84d1b3c`. Đạt A11, A11b, A12 |
| BE-3 | `GET/POST /api/wallets`, `PUT /api/wallets/:id` | ✅ | — | [BE-3](phan-2/BE-3_Api_Danh_Sach_Them_Sua_Vi.md) · Lâm Anh làm thay, commit `34cf8a3`. Đạt W1→W10. `mapWallet` đã export sẵn cho BE-4 |
| BE-4 | `POST /api/wallets/transfer` | ✅ | — | [BE-4](phan-2/BE-4_Api_Chuyen_Tien.md) · commit `12797e5`. Đạt T1→T6 (kể cả 2 lệnh gửi cùng lúc: 1 lệnh 201, 1 lệnh 400, ví không âm) |

Lưu ý khi test bằng Git Bash trên Windows: `curl -d '{"name":"Tiết kiệm"}'` gửi sai dấu tiếng Việt (ra `Ti?t ki?m`). Ghi body ra file UTF-8 rồi gửi bằng `--data-binary @file.json`.

### 3.2. Android (repo `capmoney-app`, nhánh `lamanh`)

| Mã | Task | Trạng thái | Chờ | Ghi chú |
|---|---|---|---|---|
| AN-1 | Model, Retrofit, repository, `SessionManager`, strings | ✅ | — | [AN-1](phan-2/AN-1_Nen_Tang_Model_Api.md) · `30aa240`. `Wallet.is_default` lưu kiểu `Object` (Gson bỏ qua field `transient`), truyền qua `Bundle` được |
| AN-2 | Màn Đăng nhập, Đăng ký | ✅ | — | [AN-2](phan-2/AN-2_Man_Dang_Nhap_Dang_Ky.md) · `e97b8c3` + `75ed24e` (ô nhập không tô đỏ cả nền khi lỗi). Đạt L2→L8, R1→R3a |
| AN-3 | Đặt màn Đăng nhập làm màn mở app | ✅ | — | [AN-3](phan-2/AN-3_Doi_Man_Mo_Dau.md) · `6e67737`. Đạt L1, L5, L6, R3 |
| AN-4 | Tab Tài khoản: tổng tài sản, danh sách ví | ✅ | — | [AN-4](phan-2/AN-4_Tab_Tai_Khoan.md) · `ccebc52`. Đạt W1, W1b, W7, W8, X1 |
| AN-5 | Thêm/sửa ví, chuyển tiền | ✅ | — | [AN-5](phan-2/AN-5_Them_Sua_Vi_Chuyen_Tien.md) · `534a27b`. Đạt W2→W6, T1→T5, X2 |
| AN-6 | Tab Cá nhân, đăng xuất | ✅ | — | [AN-6](phan-2/AN-6_Tab_Ca_Nhan.md) · `b3d565f`. Đạt P1→P6, X1. P5 test bằng cách làm hỏng token trong app (không sửa `JWT_SECRET`) |
| 99 | Nghiệm thu, PR app | ✅ | — | [99](phan-2/99_Nghiem_Thu_Va_PR.md) · Luồng 6 bước đạt. Đã merge [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2) |

---

## 4. Phần 3 · Quang (Lâm Anh làm thay trên `lamanh`)

### 4.1. Backend (repo `backend-cap-money`, nhánh `lamanh`)

| Mã | Task | Trạng thái | Chờ | Ghi chú |
|---|---|---|---|---|
| BE-1 | `GET /api/analytics/summary` | ✅ | — | [BE-1](phan-3/BE-1_Api_Thong_Ke.md) · `b791cc4`. Đạt S1→S7 |
| BE-2 | `GET/POST /api/budgets`, `DELETE /api/budgets/:id` | ✅ | — | [BE-2](phan-3/BE-2_Api_Ngan_Sach.md) · `3d2225b`. Đạt B1→B13 |

Test backend Phần 3 chạy trên user test riêng (không xoá giao dịch của user 1 như bước dọn trong tài liệu).

### 4.2. Android (repo `capmoney-app`, nhánh `lamanh`)

| Mã | Task | Trạng thái | Chờ | Ghi chú |
|---|---|---|---|---|
| AN-1 | Model, Retrofit, repository, strings | ✅ | — | [AN-1](phan-3/AN-1_Nen_Tang_Model_Api.md) · `adce797` |
| AN-2 | Tab Thống kê: donut, Chi/Thu, chọn tháng | ✅ | — | [AN-2](phan-3/AN-2_Tab_Thong_Ke.md) · `1ca2b15`. Đạt ST1→ST9 (ST10 trùng OF3) |
| AN-3 | Cache offline bằng Room | ✅ | — | [AN-3](phan-3/AN-3_Cache_Offline_Room.md) · `82b43a8`. Đạt OF1→OF5, unit test U1→U4 |
| AN-4 | Tab Ngân sách: danh sách, thanh 3 màu | ✅ | — | [AN-4](phan-3/AN-4_Tab_Ngan_Sach.md) · `36361c8`. Đạt BU1, BU1b, BU8→BU11 |
| AN-5 | Thêm/sửa/xoá ngân sách | ✅ | — | [AN-5](phan-3/AN-5_Them_Sua_Xoa_Ngan_Sach.md) · `a7e8b79`. Đạt BU2→BU7, BU12 |
| 99 | Nghiệm thu, PR app | ✅ | — | [99](phan-3/99_Nghiem_Thu_Va_PR.md) · Luồng 5 bước đạt. Đã merge [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2) |

27/10/2026: màn Phần 1, 3 gặp 401 đã tự về màn Đăng nhập như Phần 2 ([app #3](https://github.com/quangkhuat-tap-code/capmoney-app/pull/3), `d78bb8f`).

---

## 5. Việc còn lại

| Người | Việc | Ghi chú |
|---|---|---|
| **Trung, Quang** | Kéo `develop` mới về **cả 2 repo** (Hướng dẫn mục 4.1). **Không** làm lại task Phần 2, 3 | Phần 2, 3 đã merge 27/10 |
| **Cả nhóm** | Sau khi merge: app mở vào màn Đăng nhập, đăng ký tài khoản để test (không cần dòng `DEV ONLY`) | Tài khoản seed `dev@example.test` không đăng nhập được; muốn dùng dữ liệu user 1 thì vẫn lấy token bằng `npm run dev:token` |
| **Cả nhóm** | Màn mới thêm vào `MainActivity` phải tự xử lý insets status bar (Android 15+) | Làm giống `HomeFragment.onViewCreated`; activity riêng làm giống `CaptureExpenseActivity.applyWindowInsets()` |

---

## 6. Lịch sử cập nhật

| Ngày | Người | Thay đổi |
|---|---|---|
| 24/10/2026 | Lâm Anh | Tạo file. Phần 1: BE xong 5/5, FE code xong 7/7. Phần 2, 3 chưa bắt đầu |
| 24/10/2026 | Lâm Anh | Phần 1: đã push `lamanh` app (commit đã bỏ dòng co-author, mã commit mới), mở PR app vào `develop` |
| 24/10/2026 | Lâm Anh | Phần 1: test xong Tab Home H1–H15; màn chụp C1–C15 đạt 14/15 bằng route giả (C15 chưa test được). Thêm `.env`, `/doc/` vào `.gitignore` (`5b5b616`) |
| 24/10/2026 | Lâm Anh | Phần 1: mở PR backend #1 `lamanh` → `develop` |
| 24/10/2026 | Lâm Anh | Phần 1: merge PR app #1 và PR backend #1 vào `develop` (không xung đột). Trung, Quang bắt đầu được AN-1 |
| 27/10/2026 | Lâm Anh | Phần 2: làm thay Trung BE-1, BE-2, BE-3 (`389dd0e`, `84d1b3c`, `34cf8a3`), test đạt hết, đã push lên `lamanh` backend |
| 27/10/2026 | Lâm Anh | Phần 1: A-T2b đạt 4/4, A-T3 đạt 7/7 với `GET /api/wallets` thật. Sửa Home + màn chụp bị đè status bar (`c026a0f`, đã push `lamanh` app). Còn A-T2c (C15 điện thoại thật), A-PR2 |
| 27/10/2026 | Lâm Anh | Quyết định: Lâm Anh làm tiếp toàn bộ task còn lại của Phần 2, 3 trên nhánh `lamanh`, không chờ |
| 27/10/2026 | Lâm Anh | Backend: Phần 2 BE-4 (`12797e5`), Phần 3 BE-1 (`b791cc4`), BE-2 (`3d2225b`), đã push `lamanh` |
| 27/10/2026 | Lâm Anh | App: Phần 2 AN-1→AN-6 và Phần 3 AN-1→AN-5 xong, test emulator đạt hết, 2 luồng nghiệm thu 99 đạt, unit test 12/12. Đã push `lamanh` (`b3d565f`). Còn mở PR #2 |
| 27/10/2026 | Lâm Anh | Mở PR [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2) và [backend #2](https://github.com/quangkhuat-tap-code/backend-cap-money/pull/2) (`lamanh` → `develop`), GitHub báo merge được |
| 27/10/2026 | Lâm Anh | Đăng 8 ảnh chụp màn hình (Đăng nhập, Tài khoản, Thêm ví, Chuyển tiền, Cá nhân, Thống kê, offline, Ngân sách) vào comment PR app #2 |
| 27/10/2026 | Lâm Anh | Merge PR [app #2](https://github.com/quangkhuat-tap-code/capmoney-app/pull/2) (`35a1465`) và [backend #2](https://github.com/quangkhuat-tap-code/backend-cap-money/pull/2) (`3bce232`) vào `develop`, chưa qua review của Trung, Quang (theo quyết định của Lâm Anh) |
| 27/10/2026 | Lâm Anh | Sửa: Tab Home, Thống kê, Ngân sách, màn chụp tự về Đăng nhập khi token hết hạn. Merge [app #3](https://github.com/quangkhuat-tap-code/capmoney-app/pull/3) vào `develop` |
| 27/10/2026 | Lâm Anh | Tab Home chạy offline bằng `TransactionCache`. Merge [app #4](https://github.com/quangkhuat-tap-code/capmoney-app/pull/4) vào `develop` |
| 27/10/2026 | Lâm Anh | A-T2c: C15 đạt trên điện thoại thật (ảnh 12MP 2,5MB → 98KB, không bị xoay). Phần 1 xong hết |
| 27/10/2026 | Lâm Anh | Sửa thẻ tổng ở Tab Home hiện "-0 đ". Merge [app #5](https://github.com/quangkhuat-tap-code/capmoney-app/pull/5) vào `develop` |
