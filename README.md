# CapMoney App (Android)

App Android quản lý chi tiêu cá nhân, gọi API từ `capmoney-backend`.

## Yêu cầu phiên bản (cả nhóm dùng chung)

| Thành phần | Phiên bản |
|---|---|
| Android Studio | 2026.1.3 trở lên, kênh **Stable** |
| Android Gradle Plugin (AGP) | 9.3.0 (`gradle/libs.versions.toml`) |
| Gradle wrapper | 9.6.0 (`gradle/wrapper/gradle-wrapper.properties`) |
| JDK chạy Gradle | 25 (Android Studio tự chọn bản `jbr` đi kèm) |
| compileSdk / targetSdk | 37, minSdk 24 |

Android Studio gợi ý nâng AGP hoặc Gradle (Upgrade Assistant) thì **hỏi nhóm trước**, không tự nâng rồi commit:
bản AGP mới hơn mức Android Studio Stable hỗ trợ sẽ làm máy khác sync lỗi và không bấm Run được.

## Chạy app

1. Chạy backend trước (xem README của `capmoney-backend`), server ở cổng 5000.
2. Android Studio: **File → Open** chọn thư mục `capmoney-app` (không mở thư mục cha).
3. **File → Sync Project with Gradle Files**, chờ sync xong.
4. Chọn cấu hình **app** và máy ảo, bấm **Run**.

Máy ảo gọi backend qua `http://10.0.2.2:5000/api/` (`data/api/ApiClient.java`).
Điện thoại thật: đổi `10.0.2.2` thành IP LAN của máy chạy backend, hai máy chung WiFi.

## File riêng từng máy (không commit)

`local.properties`, `.idea/`, `.env` đã nằm trong `.gitignore`, mỗi máy tự sinh.

## Lỗi hay gặp

- **Nút Run mờ, sync báo `incompatible version (AGP x.y.z)`**: Android Studio cũ hơn AGP của project. Cập nhật Android Studio, đừng tự đổi AGP rồi commit.
- **`jlink ... does not exist`**: Gradle đang chạy bằng JRE của extension Java trong VS Code. Đóng VS Code, chạy `./gradlew --stop`, rồi Sync lại.
- **Backend báo `EADDRINUSE` cổng 5000**: đã có server khác chạy. `netstat -ano | findstr :5000` rồi `taskkill /PID <PID> /F`.

## Dùng AI hỗ trợ chạy app

Xem [Guide_ChayAppVoiAI_v1.0.md](Guide_ChayAppVoiAI_v1.0.md): các câu lệnh giao cho AI (Claude Code) để chạy backend, build app, xử lý lỗi sync, và quy tắc khi dùng AI với repo chung.
