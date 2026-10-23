# PHẦN 3 · Nghiệm thu toàn bộ, Pull Request, báo nhóm

> **Phần 3** · Làm sau khi xong [AN-5](AN-5_Them_Sua_Xoa_Ngan_Sach.md). Đọc kèm [../Huong_Dan_Lam_Viec_Nhom.md](../Huong_Dan_Lam_Viec_Nhom.md) mục 11.

## 1. Chuẩn bị

1. Kéo `develop` mới nhất vào nhánh ở **cả 2 repo** (Hướng dẫn, mục 4.1). Build/chạy lại.
2. Backend: dọn dữ liệu cũ (cuối [BE-2](BE-2_Api_Ngan_Sach.md)), tạo lại dữ liệu theo [BE-1](BE-1_Api_Thong_Ke.md) + ca B1, B2, B3 của BE-2.
3. Nếu Phần 2 đã merge (app mở vào màn Đăng nhập): vẫn dùng token dev của user 1 như Hướng dẫn mục 10 để có sẵn dữ liệu, **không commit**.

## 2. Chạy lại toàn bộ các ca (đánh dấu ✅ / ❌)

| Nhóm | Ca | Có ở task |
|---|---|---|
| Tab Thống kê | ST1 → ST10 | AN-2 |
| Offline | OF1 → OF5 | AN-3 |
| Unit test | U1 → U4 (`./gradlew :app:testDebugUnitTest`) | AN-3 |
| Tab Ngân sách | BU1, BU1b, BU8 → BU11 | AN-4 |
| Thêm/sửa/xoá ngân sách | BU2 → BU7, BU12 | AN-5 |

Thêm 1 luồng đầy đủ:

1. Tab Ngân sách tháng hiện tại: đặt Ăn uống 500.000.
2. Bấm (+) ghi 3 khoản chi Ăn uống 150.000 (màn của Phần 1).
3. Tab Ngân sách: Ăn uống 90% thanh **vàng**. Ghi thêm 100.000 → 110% thanh **đỏ**, "Vượt 50.000 đ".
4. Tab Thống kê tháng hiện tại: Ăn uống 550.000 đ; số khớp với Tab Home.
5. Tắt backend, quay lại Tab Thống kê: vẫn thấy số cũ kèm banner offline.

## 3. Trước khi tạo PR

- [ ] `git diff develop --stat` chỉ có file Phần 3 (không có `AndroidManifest.xml`, `build.gradle`).
- [ ] Không còn `DEV ONLY`, IP LAN trong `ApiClient.java`.
- [ ] Kiểm tra co-author (phải ra rỗng):

  ```bash
  git log origin/develop..HEAD --format=%B | grep -i -E "co-authored|signed-off|generated with"
  ```

## 4. Pull Request app

- `feature/stats-budget` → `develop` trên repo `capmoney-app`.
- Tiêu đề: `Phan 3: Tab Thong ke + Tab Ngan sach + cache offline`.
- Nội dung:
  - Danh sách tính năng đã làm.
  - Ảnh chụp: Tab Thống kê (Chi, Thu, offline có banner), Tab Ngân sách (đủ 3 màu), form đặt ngân sách.
  - Bảng kết quả mục 2 (✅ / ❌).
  - **File dùng chung đã sửa:** không có (Android). Backend: `src/app.js` (đã ghi ở PR backend).
  - **Việc người khác cần biết:** mục 5.

(PR backend đã tạo ở cuối task BE-2.)

## 5. Báo cho nhóm khi merge

| Với | Nội dung |
|---|---|
| Thành viên 1 | Có `TransactionCache` (`data/local`): `saveMonth(month, year, List<Transaction>)` và `getMonth(...)`. Tab Home muốn chạy offline thì gọi `saveMonth` sau khi tải giao dịch và đọc `getMonth` khi lỗi mạng. Phần 3 **không** sửa code Tab Home |
| Thành viên 2 | Tab Thống kê là số **theo tháng**; Tab Cá nhân là **toàn thời gian** → khác nhau là đúng. Màn Phần 3 gặp 401 chỉ báo lỗi; sau khi Phần 2 merge có thể đổi sang `SessionManager.logout(activity, true)` (commit riêng) |
| Cả nhóm | Room tạo file `capmoney_cache.db` trên máy. Đổi cấu trúc bảng thì tăng `version` (đang dùng `fallbackToDestructiveMigration`: cache cũ bị xoá, không mất dữ liệu thật) |
