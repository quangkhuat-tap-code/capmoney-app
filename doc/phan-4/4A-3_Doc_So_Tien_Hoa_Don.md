# 4A-3 · Đọc số tiền từ ảnh hoá đơn

> **Phần 4A** · Trình bày: **Lâm Anh** · Chỉ có Android · Task trước: [4A-2](4A-2_Tim_Kiem_Giao_Dich.md) · Task sau: [4B-1](4B-1_Lich_Su_Chuyen_Tien.md)
> **Đọc trước:** [00_Tong_Quan_Phan_4.md](00_Tong_Quan_Phan_4.md) mục 3.2, 4.3

## Mục tiêu

Chụp hoá đơn xong, app **tự điền số tiền** đọc được trên ảnh. Đây là điểm nhấn đúng với khẩu hiệu "Ghi lại chi tiêu bằng một tấm ảnh".

Dùng **ML Kit Text Recognition** bản qua **Google Play services**: nhận chữ chạy **ngay trên máy**, không gửi ảnh đi đâu, không tốn phí. Mô hình do Play services tải **một lần** (máy ảo và điện thoại test đã có sẵn); sau đó chạy được cả khi mất mạng.

> **Đã đổi so với bản đầu của tài liệu (27/10/2026):** bản gói sẵn `com.google.mlkit:text-recognition:16.0.1` làm APK debug tăng từ 9,7MB lên 54MB (thư viện native khoảng 11MB × 4 kiến trúc CPU), nên dùng bản Play services: APK gần như không tăng (9,4MB). Đổi lại máy phải có Google Play services.

## File

| File | Việc |
|---|---|
| `app/build.gradle` | Thêm `implementation 'com.google.android.gms:play-services-mlkit-text-recognition:19.0.1'`. **Commit riêng** |
| `ui/capture/ReceiptAmountParser.java` | **Tạo mới**. Hàm thuần Java (không dùng Android), nhận các dòng chữ, trả số tiền đoán được hoặc `-1` |
| `ui/capture/ReceiptScanner.java` | **Tạo mới**. Gọi ML Kit trên ảnh, trả danh sách dòng chữ qua callback trên main thread |
| `ui/capture/CaptureExpenseActivity.java`, `CaptureViewModel.java` | Gọi quét sau khi có ảnh, điền số tiền, hiện chip |
| `res/layout/activity_capture_expense.xml` | Thêm `Chip` `receipt_chip` ngay **trên** ô số tiền, mặc định ẩn |
| `test/.../ui/capture/ReceiptAmountParserTest.java` | **Tạo mới** |

## Cách đoán số tiền (`ReceiptAmountParser.parse(List<String> lines)`)

1. Chuẩn hoá từng dòng: bỏ dấu tiếng Việt (`Normalizer.normalize(s, NFD)` rồi xoá `\p{M}`, đổi `đ`/`Đ` thành `d`), viết hoa.
2. Tìm số tiền trong dòng bằng regex:
   - Có dấu phân cách nghìn: `\d{1,3}(?:[.,]\d{3})+` (ví dụ `125.000`, `1,250,000`).
   - Hoặc số liền: `\d{4,9}` (ví dụ `125000`).
   - **Bỏ** số nằm trong ngày/giờ (dòng có mẫu `\d{1,2}[/-]\d{1,2}[/-]\d{2,4}` hoặc `\d{1,2}:\d{2}`), số có từ 10 chữ số trở lên (số điện thoại, mã số thuế), số < 1.000.
   - Bỏ dấu phân cách rồi đổi sang `long`.
3. **Ưu tiên dòng từ khoá**: dòng chứa `TONG`, `TOTAL`, `THANH TOAN`, `CONG`, `PHAI TRA`, `KHACH TRA` (sau khi bỏ dấu). Lấy số **lớn nhất** trên dòng đó; dòng đó không có số thì xét dòng **ngay sau**. Có nhiều dòng từ khoá thì lấy dòng **cuối cùng** (thường là tổng sau giảm giá).
4. Không có dòng từ khoá: lấy số **lớn nhất** trong cả hoá đơn.
5. Kết quả > `CurrencyUtils.MAX_ALLOWED_AMOUNT` hoặc không tìm được → `-1`.

## Luồng trên màn chụp

- Chạy khi có ảnh mới (chụp hoặc chọn), **chỉ ở chế độ thêm mới** và **chỉ khi loại là Chi tiêu**.
- Quét trên file đã nén (`ImageUtils.compressForUpload`, ảnh ≤ 1280px, đã xoay đúng chiều) để nhanh; chạy nền, không chặn giao diện.
- Có kết quả:
  - Ô số tiền đang **trống** → điền luôn, chip hiện "Đã đọc 125.000 đ từ hoá đơn · Hoàn tác". Bấm chip → xoá số vừa điền, ẩn chip.
  - Ô số tiền **đã có số** (người dùng gõ trước) → **không ghi đè**; chip hiện "Hoá đơn ghi 125.000 đ · Dùng số này".
- Không có kết quả hoặc ML Kit lỗi → im lặng, không báo gì (người dùng vẫn tự nhập như cũ).
- Người dùng tự sửa ô số tiền → ẩn chip.
- Kết quả quét giữ trong ViewModel, xoay màn hình **không** quét lại.

## Test

### Unit test `ReceiptAmountParserTest`

| Ca | Dòng vào | Kỳ vọng |
|---|---|---|
| R1 | `["Bún bò Huế", "SL 2 x 45.000", "Tổng cộng: 90.000"]` | `90000` |
| R2 | `["TOTAL", "125,000"]` (số ở dòng sau từ khoá) | `125000` |
| R3 | `["Tổng tiền hàng 200.000", "Giảm giá 20.000", "Thanh toán 180.000"]` | `180000` (dòng từ khoá cuối) |
| R4 | `["Ngày 20/10/2026 12:30", "Cà phê 35000"]` | `35000` (bỏ ngày, giờ) |
| R5 | `["Hotline 0901234567", "Trà sữa 45.000"]` | `45000` (bỏ số điện thoại) |
| R6 | `["Cảm ơn quý khách"]` | `-1` |
| R7 | `["TONG CONG 1.250.000 VND"]` (không dấu) | `1250000` |

Chạy: `./gradlew :app:testDebugUnitTest`.

### Test trên máy (emulator: kéo ảnh hoá đơn vào thư viện bằng `adb push` rồi chọn ảnh; điện thoại thật: chụp)

| Ca | Làm | Kỳ vọng |
|---|---|---|
| RA1 | Chọn ảnh hoá đơn in rõ | Ô số tiền tự điền đúng tổng, chip "Đã đọc…" |
| RA2 | Bấm Hoàn tác | Ô số tiền trống, chip ẩn |
| RA3 | Gõ 50.000 trước rồi mới chọn ảnh | Không ghi đè; chip "Hoá đơn ghi … · Dùng số này"; bấm → đổi thành số trên hoá đơn |
| RA4 | Ảnh không có chữ (đồ ăn) | Không điền gì, không báo lỗi |
| RA5 | Chuyển sang Thu nhập rồi chọn ảnh | Không quét |
| RA6 | Tắt mạng (chế độ máy bay) rồi chọn ảnh hoá đơn | Vẫn đọc được (ML Kit chạy trên máy, mô hình đã có sẵn) |
| RA7 | Xoay màn hình sau khi đọc | Không quét lại, chip giữ nguyên |
| RA8 | Điện thoại thật (Vivo), hoá đơn thật | Ghi kết quả đúng/sai vào PR (hoá đơn mờ, nhàu có thể sai: chấp nhận được vì có Hoàn tác) |

**Commit:** `chore: them thu vien ml kit doc chu` (riêng, chỉ `build.gradle`) và `feat: tu doc so tien tu anh hoa don`.

## Xong khi

- [ ] Đạt R1 → R7, RA1 → RA8.
- [x] APK gần như không tăng (9,4MB) nhờ dùng bản Play services.
