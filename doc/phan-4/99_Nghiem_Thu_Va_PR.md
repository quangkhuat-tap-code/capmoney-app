# PHẦN 4 · Nghiệm thu, Pull Request, báo nhóm

> Làm **mỗi lần xong một phần con** (4A, 4B, 4C). Đọc kèm [../Huong_Dan_Lam_Viec_Nhom.md](../Huong_Dan_Lam_Viec_Nhom.md) mục 11.

## 1. Chuẩn bị

1. Kéo `develop` mới nhất vào `lamanh` ở **cả 2 repo**. Build app, chạy backend.
2. Đã chạy `migrations/phan4_users_avatar.sql` (từ 4B-3 trở đi).
3. Đăng nhập `demo@example.test` trên emulator.

## 2. Chạy lại các ca (đánh dấu ✅ / ❌)

| Phần con | Backend | Android | Unit test |
|---|---|---|---|
| 4A | E1–E9, S1–S6 | EA1–EA8, SA1–SA7, RA1–RA8 | R1–R7 |
| 4B | T1–T6, P1–P7, V1–V8 | TA1–TA5, PA1–PA5, VA1–VA6 | — |
| 4C | TR1–TR6 | TRA1–TRA6, NA1–NA8, W1–W8 | N1–N6 |

Mỗi phần con chạy thêm **1 luồng demo** (cũng chính là kịch bản bảo vệ trong [../Bao_Ve_Do_An.md](../Bao_Ve_Do_An.md)):

- **4A:** chụp hoá đơn → số tiền tự điền → lưu → tìm lại bằng từ khoá → sửa số tiền → số dư ví đổi đúng.
- **4B:** chuyển tiền 2 lần → xem Lịch sử → đổi ảnh đại diện → đổi mật khẩu → đăng xuất, đăng nhập bằng mật khẩu mới.
- **4C:** đặt ngân sách → ghi chi chạm 80% → thông báo → xem biểu đồ 6 tháng → tắt backend → Tab Tài khoản vẫn hiện số dư.

Cuối cùng chạy lại nhanh các luồng nghiệm thu 99 của Phần 1, 2, 3 để chắc không hỏng chỗ cũ.

## 3. Trước khi tạo PR

- [ ] `./gradlew assembleDebug testDebugUnitTest` đạt.
- [ ] `git diff develop --stat`: file dùng chung chỉ có đúng những file ở mục 4.3 file 00, mỗi file một commit riêng.
- [ ] Không có `DEV ONLY`, IP LAN trong `ApiClient.java`; không commit `.env`, `public/uploads`.
- [ ] Không có dòng co-author:

  ```bash
  git log origin/develop..HEAD --format=%B | grep -i -E "co-authored|signed-off|generated with"
  ```

## 4. Pull Request (mỗi phần con 1 cặp, `lamanh` → `develop`)

| Phần con | Tiêu đề PR (cả 2 repo) | Người trình bày dùng PR này khi bảo vệ |
|---|---|---|
| 4A | `Phan 4A: sua, tim kiem giao dich, doc so tien hoa don` | Lâm Anh |
| 4B | `Phan 4B: lich su chuyen tien, doi mat khau, anh dai dien` | Trung |
| 4C | `Phan 4C: bieu do 6 thang, thong bao ngan sach, vi offline` | Quang |

4A-3, 4C-2, 4C-3 không có backend → PR backend của 4A chỉ có 4A-1, 4A-2; phần con 4C chỉ có PR backend cho 4C-1.

Nội dung PR:
- Danh sách tính năng, API mới.
- Ảnh chụp màn hình từng tính năng (1 comment, giống PR app #2).
- Bảng kết quả mục 2.
- **File dùng chung đã sửa** và lý do.
- **Việc người khác cần biết** (mục 5).

## 5. Báo cho nhóm khi merge

| Phần con | Nội dung |
|---|---|
| 4B | Ai có DB cũ phải chạy `node scripts/run-sql.js migrations/phan4_users_avatar.sql`, nếu không Tab Cá nhân lỗi 500 |
| 4A | APK lớn hơn khoảng 4–5MB vì có mô hình ML Kit |
| 4C | Room lên `version = 2`: lần đầu mở app bản mới, cache offline cũ bị xoá một lần |
| Cả nhóm | Đọc phần mình trong [../Bao_Ve_Do_An.md](../Bao_Ve_Do_An.md) |
