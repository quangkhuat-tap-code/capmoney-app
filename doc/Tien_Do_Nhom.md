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
| Phần 1 | **Lâm Anh** | Chụp ảnh ghi chi tiêu, Tab Home (lưới + lịch), API giao dịch, API danh mục | `lamanh` | ✅ 5/5 | ✅ 7/7 code xong, đã test, nghiệm thu xong với API ví thật | Đã merge cả 2 repo. Còn: C15 trên điện thoại thật, PR #2 app (sửa đè status bar) |
| Phần 2 | **Trung** | Đăng nhập/đăng ký, Tab Tài khoản (ví, chuyển tiền), Tab Cá nhân | `feature/wallets-auth` | ✅ 3/4 (BE-1→BE-3 Lâm Anh làm thay) | ⬜ 0/6 | **Lâm Anh làm tiếp toàn bộ trên nhánh `lamanh`** (đã push BE-1→BE-3). Trung, Quang đừng làm trùng |
| Phần 3 | **Quang** | Tab Thống kê (donut), Tab Ngân sách, cache offline Room | `feature/stats-budget` | ⬜ 0/2 | ⬜ 0/5 | **Lâm Anh làm tiếp toàn bộ trên nhánh `lamanh`** |

### Chuỗi phụ thuộc quan trọng

```text
✅ Lâm Anh: PR app lamanh → develop (đã merge) ─┬──► Trung bắt đầu FE (AN-1)
                                                └──► Quang bắt đầu FE (AN-1)

✅ Lâm Anh: PR backend lamanh → develop (đã merge) ──► Trung, Quang có README + multer mới

✅ BE-3 GET /api/wallets (Lâm Anh làm thay) ───► ✅ Lâm Anh test lại màn chụp ảnh với API thật + nghiệm thu
✅ Push lamanh backend (BE-1→BE-3) ────────────► Phần 2, 3 làm tiếp trên lamanh (Lâm Anh)
Trung: AN-3 màn Đăng nhập làm màn mở app ──────► Cả nhóm bỏ dòng DEV ONLY, đăng nhập thật để test
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
| A-T2 | Test màn chụp ảnh (C1–C15) bằng **route giả** `/api/wallets` | ✅ | — | 14/15 đạt, 0 lỗi. C15 (ảnh camera 12MP) chưa test được trên emulator. Route giả đã xoá, không commit |
| A-T2b | Test lại màn chụp ảnh với `GET /api/wallets` **thật** | ✅ | — | 27/10/2026, emulator Pixel 7 Pro API 36: C1, C6, C9, C10 đạt 4/4. Ví mặc định, chip số dư đọc đúng từ API thật; C9 chặn ở client, không gọi API |
| A-T2c | Thử C15 (ảnh camera 12MP) trên **điện thoại thật** | ⬜ | — | Emulator chỉ ra ảnh 14KB, không xoay. Cần điện thoại thật (đổi IP LAN tạm, không commit) |
| A-T3 | Nghiệm thu toàn luồng (7 bước, file 06 mục 4) | ✅ | — | 27/10/2026, đạt 7/7. Bước 4: `GET /api/wallets` thật trả ví 1 = 10.000.000 đ, file ảnh đã xoá. DB có sẵn giao dịch cũ nên thẻ Chi tiêu so theo chênh lệch (+50.000) |
| A-8 | Sửa Tab Home + màn chụp bị đè lên status bar (Android 15+) | ✅ | — | Commit `c026a0f`, đã push `lamanh`. `targetSdk 37` bắt buộc edge-to-edge; thêm padding theo insets trong `HomeFragment`, `CaptureExpenseActivity` |
| A-PR2 | Push `lamanh` + PR #2 app `lamanh` → `develop` (chứa A-8) | ⬜ | — | |

---

## 3. Phần 2 · Trung

### 3.1. Backend (repo `backend-cap-money`, nhánh `feature/wallets-auth`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| BE-1 | `POST /api/auth/register`, `POST /api/auth/login` | ✅ | — | [BE-1](phan-2/BE-1_Api_Dang_Ky_Dang_Nhap.md) · Lâm Anh làm thay, commit `389dd0e`. Đạt A1→A10, A13 |
| BE-2 | `GET /api/auth/profile` | ✅ | — | [BE-2](phan-2/BE-2_Api_Thong_Tin_Ca_Nhan.md) · Lâm Anh làm thay, commit `84d1b3c`. Đạt A11, A11b, A12 |
| BE-3 | `GET/POST /api/wallets`, `PUT /api/wallets/:id` | ✅ | — | [BE-3](phan-2/BE-3_Api_Danh_Sach_Them_Sua_Vi.md) · Lâm Anh làm thay, commit `34cf8a3`. Đạt W1→W10. `mapWallet` đã export sẵn cho BE-4 |
| BE-4 | `POST /api/wallets/transfer` + push + PR backend | ⬜ | — | [BE-4](phan-2/BE-4_Api_Chuyen_Tien.md) |

27/10/2026: BE-1→BE-3 đã push lên nhánh **`lamanh`** backend (không dùng `feature/wallets-auth`). Lâm Anh làm tiếp các task còn lại của Phần 2 và Phần 3 trên `lamanh` ở cả 2 repo.

Lưu ý khi test bằng Git Bash trên Windows: `curl -d '{"name":"Tiết kiệm"}'` gửi sai dấu tiếng Việt (ra `Ti?t ki?m`). Ghi body ra file UTF-8 rồi gửi bằng `--data-binary @file.json`.

### 3.2. Android (repo `capmoney-app`, nhánh `feature/wallets-auth`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| AN-1 | Model, Retrofit, repository, `SessionManager`, strings | ⬜ | — | [AN-1](phan-2/AN-1_Nen_Tang_Model_Api.md) |
| AN-2 | Màn Đăng nhập, Đăng ký | ⏳ | AN-1 | [AN-2](phan-2/AN-2_Man_Dang_Nhap_Dang_Ky.md) |
| AN-3 | Đặt màn Đăng nhập làm màn mở app | ⏳ | AN-2 | [AN-3](phan-2/AN-3_Doi_Man_Mo_Dau.md) |
| AN-4 | Tab Tài khoản: tổng tài sản, danh sách ví | ⏳ | AN-1 | [AN-4](phan-2/AN-4_Tab_Tai_Khoan.md) |
| AN-5 | Thêm/sửa ví, chuyển tiền | ⏳ | AN-4, BE-4 | [AN-5](phan-2/AN-5_Them_Sua_Vi_Chuyen_Tien.md) |
| AN-6 | Tab Cá nhân, đăng xuất | ⏳ | AN-1 | [AN-6](phan-2/AN-6_Tab_Ca_Nhan.md) |
| 99 | Nghiệm thu, PR app | ⏳ | Tất cả task trên | [99](phan-2/99_Nghiem_Thu_Va_PR.md) |

---

## 4. Phần 3 · Quang

### 4.1. Backend (repo `backend-cap-money`, nhánh `feature/stats-budget`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| BE-1 | `GET /api/analytics/summary` | ⬜ | — | [BE-1](phan-3/BE-1_Api_Thong_Ke.md) |
| BE-2 | `GET/POST /api/budgets`, `DELETE /api/budgets/:id` + push + PR backend | ⬜ | BE-1 (dùng `monthRange.js`) | [BE-2](phan-3/BE-2_Api_Ngan_Sach.md) |

Backend làm được **ngay**: test dùng API giao dịch Phần 1 (đã có trên `develop`) và token dev, không cần chờ đăng nhập.

### 4.2. Android (repo `capmoney-app`, nhánh `feature/stats-budget`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| AN-1 | Model, Retrofit, repository, strings | ⬜ | — | [AN-1](phan-3/AN-1_Nen_Tang_Model_Api.md) |
| AN-2 | Tab Thống kê: donut, Chi/Thu, chọn tháng | ⏳ | AN-1, BE-1 | [AN-2](phan-3/AN-2_Tab_Thong_Ke.md) |
| AN-3 | Cache offline bằng Room | ⏳ | AN-2 | [AN-3](phan-3/AN-3_Cache_Offline_Room.md) |
| AN-4 | Tab Ngân sách: danh sách, thanh 3 màu | ⏳ | AN-1, BE-2 | [AN-4](phan-3/AN-4_Tab_Ngan_Sach.md) |
| AN-5 | Thêm/sửa/xoá ngân sách | ⏳ | AN-4 | [AN-5](phan-3/AN-5_Them_Sua_Xoa_Ngan_Sach.md) |
| 99 | Nghiệm thu, PR app | ⏳ | Tất cả task trên | [99](phan-3/99_Nghiem_Thu_Va_PR.md) |

Màn Đăng nhập của Trung **không** chặn Quang: test bằng token dev (Hướng dẫn, mục 10).

---

## 5. Việc làm được ngay hôm nay

| Người | Việc | Vì sao gấp |
|---|---|---|
| **Lâm Anh** | Làm tiếp Phần 2 (BE-4, AN-1→AN-6) và Phần 3 (BE-1, BE-2, AN-1→AN-5) trên `lamanh`, push dần | Quyết định 27/10/2026: không chờ, làm luôn |
| **Lâm Anh** | Thử C15 trên điện thoại thật (A-T2c) | Emulator không thay được |
| **Trung, Quang** | **Không** làm trùng các task trên. Kéo `lamanh` về để xem/review | Code đang nằm trên `lamanh` |
| **Trung, Quang** | Tab của mình (`WalletsFragment`, `ProfileFragment`, `StatsFragment`, `BudgetFragment`) sẽ bị **đè lên status bar** trên Android 15+ | `targetSdk 37` bắt buộc edge-to-edge, `MainActivity` không xử lý insets. Làm giống `HomeFragment.onViewCreated`: `ViewCompat.setOnApplyWindowInsetsListener` + padding trên. Activity riêng (Login, Register) làm giống `CaptureExpenseActivity.applyWindowInsets()` |

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
