# Hướng dẫn dùng AI để chạy CapMoney trên Android Studio

> v1.0 · 05/11/2026 · Áp dụng cho cả nhóm. Tài liệu được soạn với hỗ trợ của AI (Claude Code).

Tài liệu này dành cho thành viên mới clone project hoặc đang bị kẹt khi chạy app. Bạn giao phần kiểm tra lỗi, cài thư viện, khởi tạo database cho **AI trợ lý lập trình** (khuyến nghị **Claude Code**; Cursor, Copilot Chat ở chế độ agent cũng làm tương tự). Bạn chỉ làm những bước cần quyền Administrator hoặc phải bấm trong Android Studio.

---

## 1. Chuẩn bị (tự làm, 1 lần)

| Cần cài | Ghi chú |
|---|---|
| Android Studio **Stable** 2026.1.3 trở lên | Không dùng bản Beta/Canary |
| Node.js 18+ | Chạy backend |
| MySQL 8 | Chạy dưới dạng Windows service `MySQL80` |
| Git + quyền vào 2 repo | `capmoney-app` (FE) và `backend-cap-money` (BE) |
| Claude Code | App desktop (tab Code) hoặc CLI `claude` |

Clone **cả 2 repo vào chung 1 thư mục cha**, để AI nhìn thấy cả FE và BE cùng lúc:

```text
capmoney/
├── capmoney-app/        # git clone .../capmoney-app.git
└── capmoney-backend/    # git clone .../backend-cap-money.git capmoney-backend
```

Cả hai repo đều checkout nhánh `develop`.

## 2. Mở AI đúng chỗ

Mở Claude Code tại **thư mục cha `capmoney/`**, không mở riêng từng repo. Như vậy AI đọc được README của cả hai bên và tự nối FE với BE.

## 3. Các câu lệnh giao cho AI (copy dán theo thứ tự)

### Bước 1: Cho AI nắm project

```text
Đọc README của capmoney-app và capmoney-backend, cho tôi biết cần gì để chạy app trên Android Studio với máy ảo. Chưa sửa file nào.
```

### Bước 2: Chạy backend

```text
Chạy backend giúp tôi: kiểm tra Node, MySQL và file .env, cài thư viện, chạy npm run db:init, chạy migration trong thư mục migrations, rồi chạy npm run seed:demo. Báo lỗi nào cần tôi tự làm.
```

- File `.env`: copy từ `.env.example` rồi **tự điền** `DB_PASS` (mật khẩu MySQL của bạn). **Không dán mật khẩu vào khung chat với AI.**
- Nếu AI báo `MySQL80 Stopped` mà không bật được: bật bằng tay (xem mục 4).

### Bước 3: Tự chạy server trong terminal của bạn

```bash
cd capmoney-backend
npm run dev
```

Thấy `Server running on http://0.0.0.0:5000` là xong. Để cửa sổ này mở suốt lúc test.

> Nên tự chạy server trong terminal riêng thay vì để AI chạy nền: AI dừng phiên thì server có thể còn treo và giữ cổng 5000 (lỗi `EADDRINUSE`).

### Bước 4: Kiểm tra app build được

```text
Build thử capmoney-app bằng gradlew assembleDebug. Nếu lỗi, tìm nguyên nhân (đọc cả log Android Studio nếu cần) và đề xuất cách sửa, chưa commit gì.
```

### Bước 5: Chạy trên Android Studio (tự làm)

1. **File → Open** → chọn thư mục `capmoney-app` (không chọn thư mục cha).
2. **File → Sync Project with Gradle Files**, chờ xong.
3. Chọn cấu hình **app** + một máy ảo → bấm **Run ▶**.
4. Đăng nhập tài khoản demo `demo@capmoney.test`. Mật khẩu xem `DEMO_USER` trong `capmoney-backend/scripts/seed-demo.js`.

Nếu nút Run mờ hoặc sync lỗi, dán câu này cho AI:

```text
Android Studio sync lỗi / nút Run bị mờ. Đọc log mới nhất của Android Studio (idea.log trong %LOCALAPPDATA%\Google\AndroidStudio*\log) để tìm lỗi sync và cách sửa.
```

## 4. Việc AI không làm thay được

| Việc | Cách tự làm |
|---|---|
| Bật MySQL (cần Admin) | Win → `services.msc` → **MySQL80** → Start. Đặt Startup type = **Automatic** để khỏi bật lại mỗi lần mở máy. Hoặc PowerShell (Run as Administrator): `net start MySQL80` |
| Điền mật khẩu MySQL vào `.env` | Tự mở file và gõ |
| Bấm trong Android Studio | Open, Sync, chọn máy ảo, Run, cài SDK |
| Cài/cập nhật Android Studio, Node, MySQL | Tự tải và cài |

## 5. Lỗi hay gặp và câu hỏi AI

| Triệu chứng | Nguyên nhân | Hỏi AI |
|---|---|---|
| Sync báo `incompatible version (AGP x.y.z)` | Android Studio cũ hơn AGP của project | "AGP project là bao nhiêu, Android Studio của tôi hỗ trợ tối đa bao nhiêu?" → thường chỉ cần cập nhật Android Studio |
| `jlink ... does not exist` | Gradle chạy bằng JRE của extension Java trong VS Code | "Tắt Gradle daemon cũ bằng gradlew --stop rồi build lại" |
| Backend `EADDRINUSE :5000` | Đã có server khác giữ cổng 5000 | "Tìm tiến trình đang giữ cổng 5000, cho tôi xem nó là gì trước khi tắt" |
| Backend `ECONNREFUSED 3306` | MySQL chưa chạy | Bật MySQL80 (mục 4) |
| App báo lỗi mạng khi đăng nhập | Backend chưa chạy, hoặc chạy trên điện thoại thật | Máy ảo dùng `10.0.2.2:5000`. Điện thoại thật: "Đổi BASE_URL trong ApiClient.java sang IP LAN của máy tôi" (đừng commit thay đổi này) |
| Đăng nhập demo sai mật khẩu | Chưa chạy seed hoặc DB bị reset | `npm run seed:demo` |

## 6. Quy tắc khi dùng AI với repo chung

1. **Không để AI nâng AGP hoặc Gradle rồi commit.** Android Studio Upgrade Assistant cũng vậy. Cả nhóm dùng chung phiên bản ghi trong `README.md`; muốn đổi thì hỏi nhóm trước.
2. **Không commit file riêng từng máy:** `local.properties`, `.idea/`, `.env`, đổi IP trong `ApiClient.java`.
3. Bảo AI **"chưa commit"** khi chỉ nhờ sửa lỗi chạy máy mình. Khi commit, yêu cầu AI tạo **nhánh mới từ `develop`** và mở PR, không commit thẳng vào `develop`.
4. **Không dán mật khẩu, token, khoá API** vào khung chat. Để AI đọc từ file `.env` trên máy nếu cần.
5. AI báo "xong" thì vẫn tự mở app kiểm tra lại: đăng nhập, xem ví, giao dịch, ngân sách.

## 7. Tóm tắt 1 dòng mỗi lần mở máy

Bật MySQL → `npm run dev` trong `capmoney-backend` → Android Studio mở `capmoney-app` → Sync → Run.
