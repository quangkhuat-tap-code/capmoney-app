# Tiến độ nhóm CapMoney

> Cập nhật: **24/10/2026**. Ai xong task nào thì sửa cột "Trạng thái" của task đó và ghi ngày cập nhật ở dòng này.
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
| Phần 1 | **Lâm Anh** | Chụp ảnh ghi chi tiêu, Tab Home (lưới + lịch), API giao dịch, API danh mục | `lamanh` | ✅ 5/5 | ✅ 7/7 code xong, đã test | PR app đang mở; còn PR backend, test lại với API ví thật |
| Phần 2 | **Trung** | Đăng nhập/đăng ký, Tab Tài khoản (ví, chuyển tiền), Tab Cá nhân | `feature/wallets-auth` | ⬜ 0/4 | ⏳ 0/6 | **BE-3 (`GET /api/wallets`) đang chặn Lâm Anh** |
| Phần 3 | **Quang** | Tab Thống kê (donut), Tab Ngân sách, cache offline Room | `feature/stats-budget` | ⬜ 0/2 | ⏳ 0/5 | |

### Chuỗi phụ thuộc quan trọng

```text
Lâm Anh: PR app lamanh → develop ─────────┬──► Trung bắt đầu FE (AN-1)
  (đưa CurrencyUtils, DateUtils,          └──► Quang bắt đầu FE (AN-1)
   sửa jitpack vào develop)

Lâm Anh: PR backend lamanh → develop ─────────► Trung, Quang có README + multer mới
  (không chặn ai: develop backend đã có API Phần 1)

Trung: BE-3 GET /api/wallets ─────────────────► Lâm Anh test lại màn chụp ảnh với API thật + nghiệm thu
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
| B-PR | PR `lamanh` → `develop` | 🔄 | — | PR #1 **đang mở**, chờ duyệt và merge. Đưa README + `multer@2` vào `develop` cho cả nhóm |

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
| A-PR | Push `lamanh` + PR `lamanh` → `develop` | 🔄 | — | Đã push, PR **đang mở**, chờ duyệt và merge. **Trung, Quang chờ PR này** để bắt đầu FE. Đã bỏ hết dòng co-author trong 8 commit |
| A-T2 | Test màn chụp ảnh (C1–C15) bằng **route giả** `/api/wallets` | ✅ | — | 14/15 đạt, 0 lỗi. C15 (ảnh camera 12MP) chưa test được trên emulator. Route giả đã xoá, không commit |
| A-T2b | Test lại màn chụp ảnh với `GET /api/wallets` **thật** | ⏳ | Trung BE-3 | Chạy lại C1, C6, C9, C10 + thử C15 trên điện thoại thật |
| A-T3 | Nghiệm thu toàn luồng (7 bước, file 06 mục 4) | ⏳ | Trung BE-3 | Bước 4 cần `GET /api/wallets` thật để xem số dư ví |

---

## 3. Phần 2 · Trung

### 3.1. Backend (repo `backend-cap-money`, nhánh `feature/wallets-auth`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| BE-1 | `POST /api/auth/register`, `POST /api/auth/login` | ⬜ | — | [BE-1](phan-2/BE-1_Api_Dang_Ky_Dang_Nhap.md) |
| BE-2 | `GET /api/auth/profile` | ⬜ | — (nên làm sau BE-1) | [BE-2](phan-2/BE-2_Api_Thong_Tin_Ca_Nhan.md) |
| BE-3 | `GET/POST /api/wallets`, `PUT /api/wallets/:id` | ⬜ | — | [BE-3](phan-2/BE-3_Api_Danh_Sach_Them_Sua_Vi.md) · **Lâm Anh đang chờ** |
| BE-4 | `POST /api/wallets/transfer` + push + PR backend | ⬜ | BE-3 (dùng chung `mapWallet`) | [BE-4](phan-2/BE-4_Api_Chuyen_Tien.md) |

Backend làm được **ngay**: `develop` backend đã có khung, middleware và API Phần 1.

### 3.2. Android (repo `capmoney-app`, nhánh `feature/wallets-auth`)

| Mã | Task | Trạng thái | Chờ | File task |
|---|---|---|---|---|
| AN-1 | Model, Retrofit, repository, `SessionManager`, strings | ⏳ | Lâm Anh A-PR merge | [AN-1](phan-2/AN-1_Nen_Tang_Model_Api.md) |
| AN-2 | Màn Đăng nhập, Đăng ký | ⏳ | AN-1, BE-1 | [AN-2](phan-2/AN-2_Man_Dang_Nhap_Dang_Ky.md) |
| AN-3 | Đặt màn Đăng nhập làm màn mở app | ⏳ | AN-2 (test R3 cần BE-3) | [AN-3](phan-2/AN-3_Doi_Man_Mo_Dau.md) |
| AN-4 | Tab Tài khoản: tổng tài sản, danh sách ví | ⏳ | AN-1, BE-3 | [AN-4](phan-2/AN-4_Tab_Tai_Khoan.md) |
| AN-5 | Thêm/sửa ví, chuyển tiền | ⏳ | AN-4, BE-3, BE-4 | [AN-5](phan-2/AN-5_Them_Sua_Vi_Chuyen_Tien.md) |
| AN-6 | Tab Cá nhân, đăng xuất | ⏳ | AN-1, BE-2 | [AN-6](phan-2/AN-6_Tab_Ca_Nhan.md) |
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
| AN-1 | Model, Retrofit, repository, strings | ⏳ | Lâm Anh A-PR merge | [AN-1](phan-3/AN-1_Nen_Tang_Model_Api.md) |
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
| **Lâm Anh** | A-PR: nhờ duyệt và merge PR app `lamanh` → `develop` (đã mở) | Trung và Quang chưa bắt đầu FE được khi `develop` app chưa có Phần 1 |
| **Lâm Anh** | B-PR: nhờ duyệt và merge PR backend #1 `lamanh` → `develop` (đã mở) | Cả nhóm có README + `multer@2` |
| **Lâm Anh** | Báo Trung khi `GET /api/wallets` xong thì nhắn để test lại màn chụp (A-T2b, A-T3) | Màn chụp mới test bằng route giả |
| **Trung** | BE-1 → BE-2 → BE-3 → BE-4, rồi push + PR backend | BE-3 mở khoá test màn chụp ảnh của Lâm Anh |
| **Quang** | BE-1 → BE-2, rồi push + PR backend | Không phụ thuộc ai |

---

## 6. Lịch sử cập nhật

| Ngày | Người | Thay đổi |
|---|---|---|
| 24/10/2026 | Lâm Anh | Tạo file. Phần 1: BE xong 5/5, FE code xong 7/7. Phần 2, 3 chưa bắt đầu |
| 24/10/2026 | Lâm Anh | Phần 1: đã push `lamanh` app (commit đã bỏ dòng co-author, mã commit mới), mở PR app vào `develop` |
| 24/10/2026 | Lâm Anh | Phần 1: test xong Tab Home H1–H15; màn chụp C1–C15 đạt 14/15 bằng route giả (C15 chưa test được). Thêm `.env`, `/doc/` vào `.gitignore` (`5b5b616`) |
| 24/10/2026 | Lâm Anh | Phần 1: mở PR backend #1 `lamanh` → `develop` |
