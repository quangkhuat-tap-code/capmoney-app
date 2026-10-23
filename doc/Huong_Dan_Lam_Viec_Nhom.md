# Hướng dẫn làm việc nhóm CapMoney

> Đọc file này **một lần** trước khi bắt đầu. Sau đó mỗi lần code chỉ cần làm theo **mục 4 (Quy trình mỗi ngày)** và **mục 6 (Cách giao việc cho AI)**.
> Ngày viết: 23/10/2026.

---

## 1. Dự án gồm những gì

| Thứ | Đường dẫn | Ghi chú |
|---|---|---|
| App Android (Java + XML) | `https://github.com/quangkhuat-tap-code/capmoney-app` | Nhánh chính: `develop` |
| Backend (Node.js + Express + MySQL) | `https://github.com/quangkhuat-tap-code/backend-cap-money` | Nhánh chính: `develop` |

Chia việc:

| Phần | Người | Nội dung | Nhánh Git (dùng chung tên ở **cả 2 repo**) | Tài liệu |
|---|---|---|---|---|
| Phần 1 | Lâm Anh (Thành viên 1) | Chụp ảnh ghi chi tiêu, Tab Home (lưới ảnh + lịch), API giao dịch | `lamanh` | `doc/phan-1/` |
| Phần 2 | Trung (Thành viên 2) | Đăng nhập/đăng ký, Tab Tài khoản (ví + chuyển tiền), Tab Cá nhân | `feature/wallets-auth` | `doc/phan-2/` — bắt đầu từ `00_Tong_Quan_Phan_2.md` |
| Phần 3 | Quang (Thành viên 3) | Tab Thống kê (biểu đồ donut), Tab Ngân sách, cache offline Room | `feature/stats-budget` | `doc/phan-3/` — bắt đầu từ `00_Tong_Quan_Phan_3.md` |

**Tiến độ từng task (xong / chưa / đang chờ ai):** xem [Tien_Do_Nhom.md](Tien_Do_Nhom.md). Xong task nào thì cập nhật file đó.

Mỗi thư mục phần gồm:

| File | Dùng khi |
|---|---|
| `00_Tong_Quan_Phan_X.md` | Đọc 1 lần đầu. Có **bảng thứ tự các task** |
| `01_Database_Va_API_Phan_X.md` | Hợp đồng API, mọi task đều cần |
| `BE-1_...md`, `BE-2_...md`... | Task backend, **mỗi file = 1 commit** |
| `AN-1_...md`, `AN-2_...md`... | Task Android, **mỗi file = 1 commit** |
| `99_Nghiem_Thu_Va_PR.md` | Làm cuối: test toàn bộ, tạo PR, báo nhóm |

Mỗi người làm **trọn cả Android lẫn Backend** cho phần của mình.

---

## 2. Cài đặt máy (chỉ làm 1 lần)

### 2.1. Phần mềm cần có

- **Git**: `git --version` chạy được.
- **Android Studio** bản mới, có Android SDK 37 (Settings → Languages & Frameworks → Android SDK → tick API 37).
- **Node.js 18 trở lên**: `node -v`.
- **MySQL 8** (MySQL Installer hoặc XAMPP/Laragon đều được). Nhớ user/mật khẩu root.

### 2.2. Đặt tên người commit

Tên này sẽ hiện trên GitHub. Dùng đúng email tài khoản GitHub của mình:

```bash
git config --global user.name "ten-github-cua-ban"
```

```bash
git config --global user.email "email-github-cua-ban@example.com"
```

### 2.3. Lấy code về

Tạo 1 thư mục chung, ví dụ `D:\capmoney`, rồi mở terminal ở đó:

```bash
git clone https://github.com/quangkhuat-tap-code/capmoney-app.git
```

```bash
git clone https://github.com/quangkhuat-tap-code/backend-cap-money.git
```

### 2.4. Chạy backend lần đầu

```bash
cd backend-cap-money
```

```bash
npm install
```

Copy `.env.example` thành `.env`, sửa `DB_USER`, `DB_PASS` cho đúng MySQL trên máy. **Không bao giờ commit file `.env`.**

```bash
npm run db:init
```

```bash
node scripts/run-sql.js database_dev_seed.sql
```

```bash
npm run dev
```

Mở trình duyệt vào `http://localhost:5000/api/xyz`, thấy `{"success":false,"message":"Không tìm thấy API","data":null}` là chạy được. Chi tiết xem `README.md` trong repo backend.

`database_dev_seed.sql` tạo user id 1 và 2 ví (Tiền mặt 2.000.000, Ngân hàng 5.000.000). Chạy lại file này sẽ **đặt lại số dư 2 ví** về như cũ.

### 2.5. Chạy app lần đầu

1. Android Studio → Open → chọn thư mục `capmoney-app`.
2. Đợi Gradle Sync xong.
3. Tạo emulator (Device Manager → Create device, chọn Pixel bất kỳ, system image API 34 trở lên).
4. Bấm Run. App gọi backend qua `http://10.0.2.2:5000` (= `localhost` của máy tính khi chạy trên emulator).

Chạy trên điện thoại thật: tạm đổi `10.0.2.2` trong `ApiClient.java` thành IP LAN của máy tính (xem bằng `ipconfig`), điện thoại và máy tính chung WiFi. **Không commit thay đổi IP này.**

---

## 3. Nhánh Git

```text
develop                 ← nhánh chung, chỉ nhận code qua Pull Request
 ├── lamanh               ← Phần 1
 ├── feature/wallets-auth ← Phần 2
 └── feature/stats-budget ← Phần 3
```

- **Không bao giờ** commit hay push thẳng lên `develop` hoặc `main`.
- Mỗi người chỉ làm trên nhánh của mình, ở **cả 2 repo**.
- Code xong một mảng thì tạo **Pull Request (PR)** từ nhánh của mình vào `develop`.

Tạo nhánh lần đầu (làm ở **cả 2 repo**, thay tên nhánh cho đúng phần của mình):

```bash
git checkout develop
```

```bash
git pull origin develop
```

```bash
git checkout -b feature/wallets-auth
```

```bash
git push -u origin feature/wallets-auth
```

---

## 4. Quy trình mỗi ngày (BẮT BUỘC)

### 4.1. Trước khi code: kéo code mới nhất

Làm ở **cả 2 repo** (`capmoney-app` và `backend-cap-money`), mỗi lần ngồi vào code:

```bash
git status
```

Nếu còn file đang sửa dở thì commit hoặc cất tạm (`git stash`) trước. Sau đó:

```bash
git checkout develop
```

```bash
git pull origin develop
```

```bash
git checkout feature/wallets-auth
```

```bash
git merge develop
```

Nếu có xung đột (conflict) thì xem mục 7.

Sau khi kéo code mới:
- Backend: nếu `package.json` thay đổi thì chạy `npm install`. Nếu `database_init.sql` thay đổi thì chạy `npm run db:init`.
- Android: bấm **Sync Project with Gradle Files** rồi Build lại.

### 4.2. Trong khi code

- Chỉ sửa file thuộc phần của mình (danh sách nằm trong tài liệu từng phần, mục "Ranh giới file").
- Làm lần lượt từng file task (`BE-...`, `AN-...`) theo thứ tự ở file `00_Tong_Quan`. **Mỗi file task 1 commit.**
- Xong mỗi bước: build được, chạy được, test đúng các ca của bước đó, rồi mới commit.

### 4.3. Commit

```bash
git status
```

Xem kỹ danh sách file. **Không được có**: `.env`, `local.properties`, `.idea/`, `node_modules/`, ảnh trong `public/uploads/`, thư mục `doc/`.

```bash
git add <từng file của mình>
```

```bash
git commit
```

Quy tắc commit message:
- Tiếng Việt **không dấu**, theo dạng `feat:`, `fix:`, `chore:`, `docs:`, `build:`.
- Dòng đầu tóm tắt. Phần thân liệt kê **làm gì, sửa gì**: file/màn hình/API nào, thêm/sửa/xoá gì, sửa lỗi gì.
- **CHỈ ghi nội dung thay đổi. TUYỆT ĐỐI KHÔNG**:
  - thêm dòng `Co-Authored-By: ...` (kể cả `Co-Authored-By: Antigravity <antigravity@google.com>`, `Co-Authored-By: Claude ...`, `Co-Authored-By: Copilot ...`),
  - thêm `Signed-off-by`, `Generated with ...`,
  - ghi tên người hay tên công cụ AI.

  Lý do: GitHub đọc dòng `Co-Authored-By` và gắn **người lạ** vào danh sách Contributors của repo. Không xoá được nếu đã push.

Ví dụ đúng:

```text
feat: api dang ky dang nhap

- Them POST /api/auth/register: kiem tra email trung, ma hoa mat khau bcrypt, tao vi Tien mat mac dinh
- Them POST /api/auth/login: tra ve token JWT han 7 ngay va thong tin user
- Mount /api/auth trong app.js
```

**Kiểm tra trước khi push** (phải ra rỗng, không in ra dòng nào):

```bash
git log origin/develop..HEAD --format=%B | grep -i -E "co-authored|signed-off|generated with"
```

Nếu có dòng nào hiện ra: **dừng, không push**, nhờ người biết Git sửa lại message.

Nếu dùng công cụ AI tự commit (Antigravity, Cursor, Copilot…): vào cài đặt **tắt** tính năng tự thêm co-author / attribution trước khi dùng.

### 4.4. Push

```bash
git push
```

### 4.5. Tạo Pull Request

1. Lên GitHub repo tương ứng → tab **Pull requests** → **New pull request**.
2. `base: develop` ← `compare: <nhánh của mình>`.
3. Tiêu đề và nội dung theo mục "Pull Request" trong tài liệu phần mình.
4. Báo nhóm vào review. Người khác duyệt rồi mới merge.

---

## 5. Thứ tự merge (để không ai bị chặn)

| Thứ tự | Việc | Ai | Vì sao |
|---|---|---|---|
| 1 | Merge PR **Phần 1** (`lamanh` → `develop`) ở **cả 2 repo** | Thành viên 1 + trưởng nhóm | `develop` của app hiện **chưa build được** (thiếu cấu hình jitpack cho MPAndroidChart) và **chưa có** `CurrencyUtils`, `DateUtils`; `develop` của backend chưa có README và bản nâng `multer`. Phần 2, 3 cần những thứ này |
| 2 | Phần 2 merge **backend** `/api/auth` + `/api/wallets` càng sớm càng tốt | Thành viên 2 | Màn chụp ảnh (Phần 1) cần `GET /api/wallets`; cả nhóm cần đăng nhập thật |
| 3 | Các PR còn lại | Ai xong trước merge trước | |

Sau mỗi lần có PR được merge vào `develop`, **cả nhóm** làm lại mục 4.1 để kéo code mới.

---

## 6. Cách giao việc cho AI

### 6.1. Mỗi phiên làm việc với AI

1. Làm mục 4.1 (kéo code mới nhất) **trước khi** mở AI.
2. Mở AI ngay trong thư mục dự án (Antigravity, Cursor, Claude Code…), để AI đọc được code thật.
3. Dán **Prompt mở đầu** dưới đây, thay `<...>` cho đúng.
4. Giao **1 file task** mỗi lần. Đừng giao cả phần một lúc.
5. AI làm xong: **tự mình** chạy app/backend kiểm tra theo mục "Test" của file task. Đạt thì mới commit.
6. Trước khi push: chạy lệnh kiểm tra ở mục 4.3.

### 6.2. Prompt mở đầu (copy nguyên khối)

```text
Bạn là lập trình viên trong dự án CapMoney (app Android Java + backend Node.js/Express/MySQL).

Thư mục:
- App Android: <đường dẫn tới capmoney-app>
- Backend: <đường dẫn tới backend-cap-money>

Tài liệu bắt buộc đọc trước khi code (trong <đường dẫn tới capmoney-app>/doc/):
- Huong_Dan_Lam_Viec_Nhom.md (quy trình, quy tắc Git)
- <phan-2 hoặc phan-3>/00_Tong_Quan_<Phan_2 hoặc Phan_3>.md (phạm vi, code đang có, ranh giới file)
- <phan-2 hoặc phan-3>/01_Database_Va_API_<Phan_2 hoặc Phan_3>.md (hợp đồng API)
- <phan-2 hoặc phan-3>/<tên file task, ví dụ BE-1_Api_Dang_Ky_Dang_Nhap.md>  ← VIỆC LẦN NÀY, làm ĐÚNG theo file này

Tôi là <Thành viên 2 / Thành viên 3>. Nhánh làm việc: <feature/wallets-auth / feature/stats-budget> (ở cả 2 repo).

Việc lần này: làm ĐÚNG và CHỈ file task ở trên. Không làm trước task sau.

Quy tắc:
1. Trước khi sửa, chạy: git checkout develop && git pull origin develop && git checkout <nhánh> && git merge develop (ở repo sẽ sửa). Có conflict thì dừng và báo tôi.
2. Chỉ tạo/sửa file ghi trong mục "File" của file task (và "Ranh giới file" ở file 00). Cần sửa file ngoài danh sách thì DỪNG và hỏi tôi.
3. Đọc code đang có trước khi viết. Dùng lại class có sẵn (ApiClient, ApiResponse, SharedPrefManager, CurrencyUtils, DateUtils...), KHÔNG tạo trùng.
4. Viết Java ở mức Java 11: không dùng record, switch expression, text block, java.time.
5. Làm xong phải tự build/chạy và kiểm tra hết các ca ở mục "Test" của file task. Báo lại kết quả từng ca (đạt/lỗi).
6. Commit đúng message ở mục "Commit" của file task: tiếng Việt không dấu, chỉ ghi làm gì sửa gì.
   TUYỆT ĐỐI KHÔNG thêm "Co-Authored-By" (kể cả Co-Authored-By: Antigravity <antigravity@google.com>),
   "Signed-off-by", "Generated with", tên người hay tên công cụ AI.
7. KHÔNG push, KHÔNG tạo PR, KHÔNG commit .env / local.properties / .idea / doc / node_modules / public/uploads.
   Tôi sẽ tự kiểm tra rồi push.
8. Chỗ nào tài liệu chưa nói rõ: chọn cách đơn giản nhất và ghi lại để tôi biết.
```

### 6.3. Khi AI báo xong

- Tự chạy lại app/backend, bấm thử đúng các ca test.
- `git log -1 --format=%B` xem lại commit message, không có dòng co-author.
- `git status` không còn file lạ.
- Push (mục 4.4).

---

## 7. Xử lý xung đột (conflict) khi `git merge develop`

1. Git báo `CONFLICT (content): Merge conflict in <file>`.
2. Mở file đó, tìm đoạn:

   ```text
   <<<<<<< HEAD
   (code của mình)
   =======
   (code từ develop)
   >>>>>>> develop
   ```

3. **Giữ cả hai** nếu là dòng thêm mới của 2 người (thường gặp ở `app.js` khi mỗi người thêm 1 dòng `app.use(...)`, hoặc `AndroidManifest.xml` khi mỗi người khai báo activity). Xoá 3 dòng đánh dấu `<<<<<<<`, `=======`, `>>>>>>>`.
4. Không chắc thì **hỏi người viết đoạn code kia**, đừng xoá code của người khác.
5. Xong:

   ```bash
   git add <file>
   ```

   ```bash
   git commit
   ```

   (giữ nguyên message mặc định `Merge branch 'develop' ...`).

6. Build/chạy lại để chắc không hỏng.

---

## 8. File dùng chung, ai được sửa

| File | Quy tắc |
|---|---|
| `AndroidManifest.xml` | Chỉ **thêm** khai báo activity của mình. Riêng Phần 2 được chuyển `LAUNCHER` từ `MainActivity` sang `LoginActivity`. Commit riêng |
| `MainActivity.java`, `activity_main.xml`, `bottom_nav_menu.xml` | **Không ai sửa** |
| `ApiClient.java`, `ApiResponse.java`, `SharedPrefManager.java` | **Không ai sửa** (chỉ tạm đổi IP khi test, không commit) |
| `colors.xml`, `dimens.xml`, `strings.xml`, `themes.xml` | **Không sửa**. Màu/chữ riêng thì tạo file riêng: `colors_<phần>.xml`, `strings_<phần>.xml` |
| `app/build.gradle`, `settings.gradle` | **Không sửa**. Thư viện cần đã có sẵn: Retrofit, Gson, Glide, Room, MPAndroidChart |
| Backend `src/app.js` | Chỉ **thêm** dòng `require` + `app.use(...)` của mình, ở chỗ comment giữ chỗ sẵn |
| Backend `database_init.sql` | **Không sửa** bảng. Đã đủ 6 bảng |
| Backend `auth.middleware.js`, `db.js`, `AppError.js` | **Không sửa** |
| `CurrencyUtils`, `DateUtils` (Phần 1 tạo) | **Được dùng**, không sửa. Cần hàm mới thì tạo class tiện ích riêng trong package của mình |

Quy ước đặt tên để không trùng:
- Layout/drawable/string/color: thêm tiền tố theo màn của mình, ví dụ `auth_`, `wallet_`, `profile_`, `stats_`, `budget_`.
- Mỗi người tạo interface Retrofit riêng (`AuthApi`, `WalletApi`, `StatsApi`, `BudgetApi`). **Không** tạo `ApiService.java` chung.

---

## 9. Khuôn JSON mọi API (bắt buộc)

```json
{ "success": true, "message": "Lấy dữ liệu thành công", "data": { } }
```

```json
{ "success": false, "message": "Số dư ví không đủ để thực hiện giao dịch", "data": null }
```

- Tiền là **số** (không phải chuỗi). Ngày giờ là chuỗi `yyyy-MM-dd HH:mm:ss` giờ Việt Nam.
- Mọi API (trừ đăng ký, đăng nhập) cần header `Authorization: Bearer <token>`. Token JWT chứa `{ "id": <userId> }`; `auth.middleware.js` gán `req.user.id`.

---

## 10. Test khi chưa có màn đăng nhập

Trước khi Phần 2 merge màn đăng nhập:

1. Lấy token ở backend:

   ```bash
   npm run dev:token
   ```

2. Thêm **tạm** vào `onCreate` của màn mình đang test:

   ```java
   // DEV ONLY - KHONG COMMIT
   SharedPrefManager.getInstance(requireContext()).saveAuthData("<dán token>", 1, "Dev");
   ```

3. Trước khi commit: `git diff`, xác nhận **không còn** dòng `DEV ONLY`.

Test API bằng curl (Git Bash):

```bash
TOKEN=$(npm run -s dev:token)
```

```bash
curl -s http://localhost:5000/api/categories -H "Authorization: Bearer $TOKEN"
```

---

## 11. Checklist trước khi tạo PR

- [ ] Đã `git merge develop` mới nhất, build/chạy được.
- [ ] Đạt hết các ca test trong tài liệu phần mình.
- [ ] `git diff develop --stat` chỉ có file của mình (+ file dùng chung được phép).
- [ ] Không còn `DEV ONLY`, IP LAN, `console.log` thừa.
- [ ] Lệnh kiểm tra co-author ở mục 4.3 ra rỗng.
- [ ] Mô tả PR có: danh sách tính năng, ảnh chụp màn hình, bảng kết quả test, việc người khác cần biết.
