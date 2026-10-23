# PHẦN 2 · Nghiệm thu toàn bộ, Pull Request, báo nhóm

> **Phần 2** · Làm sau khi xong [AN-6](AN-6_Tab_Ca_Nhan.md). Đọc kèm [../Huong_Dan_Lam_Viec_Nhom.md](../Huong_Dan_Lam_Viec_Nhom.md) mục 11.

## 1. Chuẩn bị

1. Kéo `develop` mới nhất vào nhánh ở **cả 2 repo** (Hướng dẫn, mục 4.1). Build/chạy lại.
2. Backend: `npm run db:init`, `node scripts/run-sql.js database_dev_seed.sql`, `npm run dev`.
3. Gỡ app khỏi emulator rồi Run lại (xoá token cũ).

## 2. Chạy lại toàn bộ các ca (đánh dấu ✅ / ❌)

| Nhóm | Ca | Có ở task |
|---|---|---|
| Đăng nhập | L1 → L8 | AN-2, AN-3 |
| Đăng ký | R1 → R3 | AN-2, AN-3 |
| Tab Tài khoản | W1, W1b, W7, W8 | AN-4 |
| Thêm/sửa ví | W2 → W6 | AN-5 |
| Chuyển tiền | T1 → T5 | AN-5 |
| Tab Cá nhân | P1 → P6 | AN-6 |
| Xoay màn hình | X1, X2 | AN-4, AN-5, AN-6 |

Thêm 1 luồng đầy đủ từ đầu đến cuối:

1. Đăng ký tài khoản mới → vào app, Tab Tài khoản có "Tiền mặt 0 đ".
2. Sửa số dư Tiền mặt thành 2.000.000 (xác nhận).
3. Thêm ví "Vietcombank" loại Ngân hàng 5.000.000. Tổng tài sản 7.000.000.
4. Chuyển 500.000 từ Vietcombank sang Tiền mặt. Tổng vẫn 7.000.000.
5. Bấm (+) ghi 1 khoản chi 50.000 vào Tiền mặt (màn của Phần 1). Tab Tài khoản: Tiền mặt 2.450.000. Tab Cá nhân: 1 giao dịch, Tổng chi 50.000 đ.
6. Đăng xuất → đăng nhập lại → dữ liệu còn nguyên.

## 3. Trước khi tạo PR

- [ ] `git diff develop --stat` chỉ có file Phần 2 + `AndroidManifest.xml`.
- [ ] Không còn `DEV ONLY`, IP LAN trong `ApiClient.java`.
- [ ] Kiểm tra co-author (phải ra rỗng):

  ```bash
  git log origin/develop..HEAD --format=%B | grep -i -E "co-authored|signed-off|generated with"
  ```

## 4. Pull Request app

- `feature/wallets-auth` → `develop` trên repo `capmoney-app`.
- Tiêu đề: `Phan 2: dang nhap dang ky + Tab Tai khoan + Tab Ca nhan`.
- Nội dung:
  - Danh sách tính năng đã làm.
  - Ảnh chụp: Đăng nhập, Đăng ký, Tab Tài khoản, dialog Thêm ví, dialog Chuyển tiền, Tab Cá nhân.
  - Bảng kết quả mục 2 (✅ / ❌).
  - **File dùng chung đã sửa:** `AndroidManifest.xml` (khai báo 2 activity, đổi màn mở đầu sang `LoginActivity`).
  - **Việc người khác cần biết:** mục 5.

(PR backend đã tạo ở cuối task BE-4.)

## 5. Báo cho nhóm khi merge

| Với | Nội dung |
|---|---|
| Thành viên 1 | `GET /api/wallets` đúng khuôn `{ id, name, balance, type, is_default }`, `is_default` là boolean, ví mặc định đứng đầu |
| Cả nhóm | App giờ mở vào màn Đăng nhập. Không cần dòng `DEV ONLY` lưu token nữa — đăng ký 1 tài khoản để test. Tài khoản seed `dev@example.test` **không đăng nhập được** (mật khẩu giả); muốn dùng dữ liệu user 1 thì vẫn lấy token bằng `npm run dev:token` |
| Cả nhóm | Token hết hạn sau 7 ngày. Màn Phần 2 tự về Đăng nhập khi gặp 401; màn khác muốn làm giống thì gọi `SessionManager.logout(activity, true)` |
| Thành viên 3 | Tab Cá nhân là tổng thu/chi **toàn thời gian**; Tab Thống kê là **theo tháng** → 2 số khác nhau là đúng |
