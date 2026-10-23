# 06 · Kế hoạch thực hiện, commit và nghiệm thu (dành cho agent code)

> Làm **theo đúng thứ tự** dưới đây. Mỗi bước là **1 commit**. Xong bước nào thì phải build/chạy được ở bước đó rồi mới sang bước sau.

---

## 0. Chuẩn bị

### 0.1. Quy trình mỗi bước

1. **Phân tích**: đọc spec của bước, ghi rõ file cần sửa, hàm, API, tiêu chí nghiệm thu.
2. **Code**: viết code theo phân tích ở bước 1.
3. **Tự kiểm tra**: build, chạy app/server, xác nhận chạy được và không bị bug. Chưa đạt thì sửa tiếp, chưa chuyển sang bước 4.
4. **Test lại**: build, chạy, kiểm tra theo cột "Kiểm tra trước khi commit" và tiêu chí nghiệm thu. Có lỗi thì ghi rõ lỗi, quay lại bước 2.
5. **Commit**: chỉ commit khi bước 4 đạt.

### 0.2. Nhánh Git

Chỉ dùng **1 nhánh duy nhất: `lamanh`**. Không tạo nhánh `feature/...` nào khác.

```bash
git checkout develop
```

```bash
git pull origin develop
```

```bash
git checkout -b lamanh
```

Nếu nhánh đã tồn tại thì `git checkout lamanh` rồi `git merge develop`.

**Quy tắc commit:**
- Message tiếng Việt không dấu, theo Conventional Commits: `feat:`, `fix:`, `chore:`, `build:`, `docs:`.
- Message ghi **chi tiết đã làm gì**: dòng đầu tóm tắt, phần thân liệt kê từng thay đổi (file/màn hình/API nào, thêm/sửa/xoá gì, sửa lỗi gì). Ví dụ:
  ```
  feat: chi tiet va xoa giao dich

  - Them TransactionDetailActivity hien thi anh, so tien, danh muc, ghi chu, ngay
  - Them nut xoa goi DELETE /api/transactions/:id, xac nhan truoc khi xoa
  - HomeFragment tai lai danh sach sau khi xoa
  ```
- Commit message **chỉ ghi làm gì, sửa gì**. **Không** nhắc đến ai: không thêm dòng `Co-Authored-By`, `Signed-off-by`, không ghi tên người hay công cụ AI (kể cả dòng "Generated with ..."). Trước khi push, chạy `git log --format=%B | grep -i co-authored` phải ra rỗng; công cụ nào tự chèn dòng này thì tắt tính năng đó đi.
- **Không** commit: `local.properties`, `.idea/`, `doc/`, `.env`, `node_modules/`, file ảnh trong `public/uploads/`, và mọi dòng `DEV ONLY` (xem file 03, mục 8).
- Chỉ sửa file trong phạm vi Phần 1 (file 00, mục 3). Cần sửa file ngoài phạm vi thì commit **riêng** và ghi rõ lý do trong message.
- **Không** push thẳng lên `develop` hay `main`. Chỉ push nhánh `lamanh`, sau đó tạo Pull Request.

---

## 1. Backend (repo `capmoney-backend`)

| Bước | Commit message | Nội dung | Kiểm tra trước khi commit |
|---|---|---|---|
| B-1 | `chore: khung backend dung chung` | File 02, mục 2: `package.json`, `.env.example`, `.gitignore`, `src/app.js`, `src/config/db.js`, `src/middlewares/auth.middleware.js`, `src/utils/AppError.js`, `database_init.sql`, `database_dev_seed.sql`, `scripts/run-sql.js`, `scripts/dev-token.js`, `public/uploads/.gitkeep`. **Bỏ qua bước này nếu nhóm đã có khung** | `npm run dev` chạy được; `GET /api/xyz` trả 404 đúng khuôn JSON |
| B-2 | `feat: api danh muc thu chi` | `category.controller.js`, `category.routes.js`, mount vào `app.js` | Ca B1 + `GET /api/categories?type=expense` trả 8 danh mục |
| B-3 | `feat: middleware upload anh multer` | `upload.middleware.js` | Ca B7, B8 (tạm mount vào route POST để test) |
| B-4 | `feat: api tao va lay danh sach giao dich` | `transaction.query.js`, `createTransaction`, `getTransactions`, routes | Ca B2 → B6, B9 → B11, B14, B15 |
| B-5 | `feat: api xoa giao dich hoan so du` | `deleteTransaction` | Ca B12, B13 |

---

## 2. Android (repo `capmoney-app`)

| Bước | Commit message | Nội dung | Kiểm tra |
|---|---|---|---|
| A-0 | `build: sua cau hinh jitpack de build duoc` | **Chỉ làm nếu build `develop` đang lỗi** (xem file 00, mục 4) | `./gradlew :app:assembleDebug` thành công |
| A-1 | `chore: khai bao manifest cho man chup anh` | File 03, mục 2.1–2.2: `INTERNET`, `usesCleartextTraffic`, khai báo activity, `FileProvider`, `file_paths.xml` | Build OK |
| A-2 | `feat: model, api va repository giao dich` | File 03, mục 3–7: resource riêng, `TransactionApi`, các model, `WalletOption`, `TransactionRepository`, `CurrencyUtils`, `DateUtils`, `ImageUtils` | Build OK; có unit test cho `CurrencyUtils` (mục 3) |
| A-3 | `feat: man hinh chup anh ghi chi tieu` | Toàn bộ file 04 | Ca C1 → C15 |
| A-4 | `feat: tab home luoi anh` | File 05, mục 2–4 và mục 7 (chế độ lưới, chọn tháng, thẻ tổng) | Ca H1 → H6, H12 → H15 |
| A-5 | `feat: tab home che do lich` | File 05, mục 5 | Ca H7 → H10 |
| A-6 | `feat: chi tiet va xoa giao dich` | File 05, mục 6 | Ca H11 |

Lệnh build và test (từ thư mục `capmoney-app`):

```bash
./gradlew :app:assembleDebug
```

```bash
./gradlew :app:testDebugUnitTest
```

---

## 3. Unit test bắt buộc

Tạo `app/src/test/java/com/example/comgroupcapmoney/utils/CurrencyUtilsTest.java` (JUnit 4, đã có sẵn dependency):

| Hàm | Input | Kỳ vọng |
|---|---|---|
| `format` | `50000` | `"50.000 đ"` |
| `format` | `0` | `"0 đ"` |
| `format` | `1234567.6` | `"1.234.568 đ"` |
| `formatSigned` | `50000, true` | `"-50.000 đ"` |
| `formatSigned` | `50000, false` | `"+50.000 đ"` |
| `formatShort` | `500` / `50000` / `1200000` / `2000000` | `"500"` / `"50k"` / `"1,2tr"` / `"2tr"` |
| `parseDigits` | `"1.500.000"` / `""` / `"abc"` / `"9999999999999999"` | `1500000` / `0` / `0` / `999999999999` |
| `formatInput` | `1500000` | `"1.500.000"` |

Và `DateUtilsTest`: `toDisplay("2026-10-22 12:30:00")` phải ra `"22/10/2026 12:30"`; `monthTitle(1, 2027)` phải ra `"Tháng 1/2027"`.

---

## 4. Nghiệm thu tổng (end-to-end)

Chạy backend + emulator, dùng token dev (file 03, mục 8):

1. Ví 1 có 2.000.000 đ. Chụp ảnh bún chả, nhập 50.000, Chi tiêu, chọn Ăn uống, rồi Lưu. Home hiện ảnh mới với nhãn `-50.000 đ`; thẻ Chi tiêu = 50.000 đ.
2. Ghi thêm khoản Thu nhập 8.000.000 (Lương), không có ảnh. Ô mới hiện emoji 💼 và nhãn xanh `+8.000.000 đ`.
3. Chuyển sang Lịch. Ô hôm nay có thumbnail ảnh bún chả và chữ `-50k`. Bấm vào ô thì mở sheet có 2 giao dịch.
4. Xóa khoản 50.000. Ô biến mất; gọi `GET /api/wallets` thấy ví 1 = 2.000.000 + 8.000.000 = 10.000.000 đ; file ảnh bị xóa khỏi `public/uploads`.
5. Thử ghi Chi tiêu 99.000.000. Bị chặn ngay ở client.
6. Xoay màn hình, đổi tab, quay lại. Không crash, không mất trạng thái.
7. `git diff develop --stat` chỉ chứa file trong phạm vi Phần 1 (cộng `AndroidManifest.xml` và `settings.gradle` nếu có bước A-0).

---

## 5. Pull Request

- Android: PR `lamanh` → `develop`, tiêu đề `Phan 1: chup anh ghi chi tieu + Tab Home + API giao dich`.
- Backend: PR riêng trong repo backend.
- Mô tả PR gồm:
  - Danh sách tính năng đã làm
  - Ảnh chụp màn hình: màn chụp, lưới, lịch, chi tiết
  - **Danh sách file dùng chung đã sửa**: `AndroidManifest.xml`, và `settings.gradle` nếu có
  - **Việc cần thành viên khác biết**: khuôn `GET /api/wallets` mà Phần 1 đang đọc (file 01, mục 3.5); payload JWT `{ id }`; `CurrencyUtils` đã có; `TransactionApi` tách riêng khỏi `ApiService`
  - Kết quả các ca nghiệm thu (bảng tick ✅ / ❌)
