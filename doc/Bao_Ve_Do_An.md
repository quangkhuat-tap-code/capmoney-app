# Bảo vệ đồ án CapMoney: chia phần trình bày

> Cập nhật: **27/10/2026**. Mỗi người trình bày **đúng 1 phần**: phần gốc (Phần 1, 2 hoặc 3) + phần mở rộng tương ứng (4A, 4B hoặc 4C).
> **Code và test:** Lâm Anh làm toàn bộ code Phần 1–4 trên nhánh `lamanh` (đã ghi trong [Tien_Do_Nhom.md](Tien_Do_Nhom.md)). Trung, Quang **đọc hiểu** phần mình theo file này để trình bày và trả lời câu hỏi. Được hỏi ai viết code thì trả lời đúng như vậy.

---

## 1. Chia phần

| Người | Trình bày | Màn hình | API | PR để chỉ |
|---|---|---|---|---|
| **Lâm Anh** | Phần 1 + **4A** · Ghi chi tiêu bằng ảnh, giao dịch | Màn chụp, Tab Home (lưới + lịch), chi tiết giao dịch, tìm kiếm | `/api/transactions` (GET, POST, PUT, DELETE, search), `/api/categories` | app #1, #4, #5, PR 4A |
| **Trung** | Phần 2 + **4B** · Tài khoản, ví | Đăng nhập, Đăng ký, Tab Tài khoản, chuyển tiền, lịch sử chuyển tiền, Tab Cá nhân | `/api/auth/*`, `/api/wallets/*` | app #2, #3, backend #2, PR 4B |
| **Quang** | Phần 3 + **4C** · Thống kê, ngân sách, offline | Tab Thống kê (donut + biểu đồ 6 tháng), Tab Ngân sách, thông báo, banner offline | `/api/analytics/*`, `/api/budgets` | app #2, backend #2, PR 4C |

**Phần chung** (ai mở đầu thì nói, đề xuất **Lâm Anh**, ~2 phút): ý tưởng, công nghệ, kiến trúc, 6 bảng DB, cách nhóm làm việc với Git (nhánh, PR, không push thẳng `develop`).

Thời lượng gợi ý: mở đầu 2 phút + mỗi người 5–6 phút (3 phút nói + 2–3 phút demo) + hỏi đáp.

---

## 2. Phần chung (mở đầu)

- **Ý tưởng:** ghi chi tiêu bằng **một tấm ảnh** (giống Locket), xem lại theo lưới ảnh hoặc lịch, có ví, chuyển tiền, thống kê, ngân sách.
- **Công nghệ:** Android Java (MVVM: Activity/Fragment → ViewModel + LiveData → Repository → Retrofit), Node.js + Express, MySQL. Ảnh lưu file trên server, DB chỉ lưu đường dẫn.
- **6 bảng:** `users`, `wallets`, `categories`, `transactions`, `wallet_transfers`, `budgets` (sơ đồ ở [Tai_Lieu_Ky_Thuat_Va_Phan_Cong_CapMoney.md](Tai_Lieu_Ky_Thuat_Va_Phan_Cong_CapMoney.md) mục 2).
- **Mọi API** trả cùng khuôn `{ success, message, data }`; cần đăng nhập thì gửi `Authorization: Bearer <token>`.
- **Chạy thật:** máy ảo gọi `10.0.2.2:5000`; điện thoại thật đã test qua cáp USB (`adb reverse`).

---

## 3. Lâm Anh · Phần 1 + 4A

### 3.1. Nói gì

1. **Màn chụp:** dùng camera/thư viện ảnh của hệ thống (`ActivityResultContracts.TakePicture`, `PickVisualMedia`) nên **không cần xin quyền CAMERA**. Ảnh được **nén trên máy** trước khi gửi: ảnh 12MP 2,5MB còn khoảng 100KB, xoay đúng chiều theo EXIF (đã test trên điện thoại thật, ca C15).
2. **Lưu giao dịch** (`POST /api/transactions`, multipart): server kiểm tra dữ liệu, rồi trong **1 DB transaction**: khoá ví (`SELECT … FOR UPDATE`), chặn chi vượt số dư, thêm giao dịch, cộng/trừ số dư. Lỗi giữa chừng thì rollback và xoá file ảnh vừa tải lên.
3. **Tab Home:** lưới ảnh 3 cột hoặc lịch tháng (tuần bắt đầu thứ Hai), thẻ tổng Chi/Thu; mất mạng vẫn xem tháng đã mở nhờ Room.
4. **4A-1 Sửa giao dịch:** hoàn tác giao dịch cũ rồi áp giao dịch mới, khoá các ví **theo thứ tự id** để tránh deadlock, ví nào âm thì chặn.
5. **4A-2 Tìm kiếm:** tìm trong ghi chú và tên danh mục, mọi tháng, **không phân biệt dấu** ("ca phe" ra "cà phê"), 20 dòng/trang, cuộn tới cuối tự tải trang sau.
6. **4A-3 Đọc hoá đơn:** ML Kit đọc chữ **ngay trên máy**, không cần mạng; ưu tiên dòng có chữ "Tổng / Thanh toán", tự điền số tiền, có nút Hoàn tác.

### 3.2. Demo (khoảng 3 phút)

Chụp hoá đơn → số tiền tự điền → chọn danh mục Ăn uống → Lưu → Tab Home có ảnh mới → kính lúp tìm "bun" → bấm kết quả → Sửa số tiền → Tab Tài khoản số dư đổi đúng.

### 3.3. File cần nắm

`ui/capture/CaptureExpenseActivity.java`, `utils/ImageUtils.java`, `ui/home/HomeFragment.java`, `ui/search/SearchActivity.java`, `ui/capture/ReceiptAmountParser.java`; backend `transaction.controller.js`, `upload.middleware.js`.

---

## 4. Trung · Phần 2 + 4B

### 4.1. Nói gì

1. **Đăng ký / đăng nhập:** mật khẩu mã hoá bằng **bcrypt** (10 vòng), không lưu mật khẩu gốc. Đăng nhập đúng thì server cấp **JWT** hạn 7 ngày; app lưu token vào `SharedPreferences` và tự gắn vào mọi request. Đăng ký xong server **tự tạo ví "Tiền mặt"**.
2. **Mở app:** có token thì vào thẳng màn chính, không thì vào Đăng nhập. API trả `401` (token hết hạn) ở **bất kỳ màn nào** → tự về màn Đăng nhập (`SessionManager`).
3. **Tab Tài khoản:** Tổng tài sản, ví thanh toán và sổ tiết kiệm, thêm/sửa ví.
4. **Chuyển tiền:** 1 DB transaction: khoá **cả hai ví theo thứ tự id** (2 lệnh chuyển ngược chiều cùng lúc không bị deadlock), trừ ví nguồn, cộng ví đích, ghi bảng `wallet_transfers`. Không đủ tiền thì rollback, không ví nào đổi.
5. **Tab Cá nhân:** thông tin, số giao dịch, tổng thu/chi **toàn thời gian**, đăng xuất.
6. **4B-1 Lịch sử chuyển tiền:** đọc bảng `wallet_transfers` theo tháng, JOIN 2 lần bảng `wallets` để ra tên ví nguồn và ví đích.
7. **4B-2 Đổi mật khẩu:** phải đúng mật khẩu cũ (`bcrypt.compare`), mật khẩu mới 6–72 ký tự (bcrypt chỉ đọc 72 byte đầu), sai mật khẩu cũ trả 400 chứ không 401 để app không tự đăng xuất.
8. **4B-3 Ảnh đại diện:** thêm cột `avatar_url` bằng **migration** chạy lại được nhiều lần; tải ảnh dùng lại middleware multer của Phần 1; ảnh cũ bị xoá sau khi DB cập nhật xong.

### 4.2. Demo (khoảng 3 phút)

Đăng ký tài khoản mới → Tab Tài khoản có sẵn ví Tiền mặt → thêm ví Ngân hàng → chuyển tiền → xem Lịch sử → Tab Cá nhân đổi ảnh đại diện → đổi mật khẩu → đăng xuất → đăng nhập bằng mật khẩu mới.

### 4.3. File cần nắm

`ui/auth/LoginActivity.java`, `ui/auth/SessionManager.java`, `ui/wallets/WalletsFragment.java`, `ui/wallets/TransferMoneyDialog.java`, `ui/profile/ProfileFragment.java`; backend `auth.controller.js`, `wallet.controller.js` (hàm `transferMoney`), `middlewares/auth.middleware.js`.

---

## 5. Quang · Phần 3 + 4C

### 5.1. Nói gì

1. **Thống kê** (`GET /api/analytics/summary`): `SUM(amount) … GROUP BY category_id` cho tháng chọn, trả tổng và **% từng danh mục**; app vẽ **donut** (MPAndroidChart, tổng tiền ở giữa), bấm Chi/Thu để đổi.
2. **Ngân sách:** mỗi danh mục chi một hạn mức/tháng; server tính **số đã tiêu** bằng truy vấn con; thanh màu **xanh < 80%, vàng 80–99%, đỏ ≥ 100%**. Đặt lại cùng danh mục cùng tháng thì **cập nhật**, không tạo trùng.
3. **Offline (Room):** giao dịch tải về được lưu vào SQLite trên máy; mất mạng thì Tab Thống kê và Tab Home vẫn hiện số liệu đã lưu kèm **banner giờ lưu**. Tính thống kê từ cache bằng `StatsCalculator` (có unit test).
4. **4C-1 Biểu đồ 6 tháng:** 1 câu SQL `GROUP BY năm, tháng, loại`, tháng không có giao dịch vẫn có cột 0; app vẽ `BarChart` 2 cột Chi/Thu mỗi tháng.
5. **4C-2 Thông báo ngân sách:** ghi chi xong, app hỏi ngân sách tháng đó; khoản chi này làm danh mục **vừa vượt** 80% hoặc 100% thì hiện thông báo Android. Đã vượt rồi thì không báo lại. Android 13+ phải xin quyền thông báo.
6. **4C-3 Ví offline:** Room thêm bảng ví (DB lên `version 2`), mất mạng Tab Tài khoản vẫn xem số dư, khoá các nút ghi dữ liệu.

### 5.2. Demo (khoảng 3 phút)

Tab Ngân sách đặt Ăn uống 500.000 → ghi 2 khoản chi → hiện thông báo "Sắp hết ngân sách" → Tab Ngân sách thanh vàng → Tab Thống kê donut + biểu đồ 6 tháng → tắt backend → Tab Thống kê và Tab Tài khoản vẫn hiện số kèm banner offline.

### 5.3. File cần nắm

`ui/stats/StatsFragment.java`, `ui/stats/StatsCalculator.java`, `ui/budget/BudgetFragment.java`, `ui/budget/BudgetAlertNotifier.java`, `data/local/TransactionCache.java`, `data/local/WalletCache.java`; backend `analytics.controller.js`, `budget.controller.js`.

---

## 6. Câu hỏi hay gặp

| Câu hỏi | Ai trả lời | Ý chính |
|---|---|---|
| Sao không lưu ảnh trong DB? | Lâm Anh | DB nhẹ, ảnh là file tĩnh server phát thẳng; DB chỉ lưu `/uploads/…`. App ghép host, đổi máy ảo/điện thoại thật chỉ sửa 1 chỗ |
| Hai người cùng trừ tiền một ví cùng lúc thì sao? | Lâm Anh / Trung | Khoá dòng ví bằng `SELECT … FOR UPDATE` trong transaction: lệnh sau phải chờ lệnh trước xong, đọc số dư mới rồi mới kiểm tra |
| Sao khoá ví theo thứ tự id? | Trung | Chuyển A→B và B→A cùng lúc mà khoá theo thứ tự khác nhau sẽ chờ nhau mãi (deadlock). Cùng thứ tự thì không |
| Chống SQL injection thế nào? | Ai cũng được | Mọi câu SQL dùng tham số `?` của `mysql2`, không ghép chuỗi dữ liệu người dùng. Tìm kiếm còn thoát `%`, `_` |
| Mật khẩu lưu thế nào? | Trung | bcrypt (có salt, chậm có chủ đích), không giải ngược được |
| JWT là gì, hết hạn thì sao? | Trung | Chuỗi ký bằng khoá bí mật của server chứa `id` user, hạn 7 ngày; hết hạn → API trả 401 → app tự về màn Đăng nhập |
| Đổi mật khẩu thì token cũ còn dùng được không? | Trung | Còn tới khi hết hạn, vì JWT không lưu phiên ở server. Muốn thu hồi phải thêm bảng phiên/phiên bản token (đã ghi là ngoài phạm vi) |
| Tiền dùng kiểu gì? | Quang | `DECIMAL(15,2)` trong MySQL để không sai số như số thực; server ép về number khi trả JSON |
| Offline hoạt động thế nào? | Quang | Có mạng thì lưu bản sao vào Room; lỗi mạng thì đọc bản sao + banner giờ lưu. Chỉ **xem**, không ghi khi mất mạng |
| Vì sao ngưỡng 80%? | Quang | Đủ sớm để kịp điều chỉnh; trùng màu vàng của Tab Ngân sách |
| Đọc hoá đơn có gửi ảnh lên mạng không? | Lâm Anh | Không, ML Kit bản gói sẵn chạy trên máy, chạy được cả khi máy bay |
| Xoay màn hình có mất dữ liệu không? | Ai cũng được | Không: dữ liệu giữ trong ViewModel (sống qua xoay màn hình), LiveData đẩy lại cho giao diện mới |
| Nhóm chia việc, tránh đè code nhau thế nào? | Lâm Anh | Mỗi phần có danh sách file riêng, file dùng chung sửa bằng commit riêng; làm trên nhánh riêng, vào `develop` qua Pull Request |

---

## 7. Chuẩn bị trước buổi bảo vệ

- [ ] Máy chạy demo: MySQL, backend (`npm run dev`), emulator đã cài bản `develop` mới nhất, đã đăng nhập `demo@example.test`.
- [ ] Có sẵn 1 ảnh hoá đơn rõ nét trong thư viện ảnh của emulator (cho 4A-3).
- [ ] Đã đặt ngân sách để demo 4C-2 chạm đúng 80% (ghi lại số tiền cần nhập).
- [ ] Mỗi người tự chạy demo phần mình **ít nhất 1 lần** trước buổi bảo vệ.
- [ ] Mở sẵn tab PR trên GitHub của từng phần.
