# AN-3 · Đặt màn Đăng nhập làm màn mở app

> **Phần 2** · Repo `capmoney-app` · Nhánh `feature/wallets-auth` · Task trước: [AN-2](AN-2_Man_Dang_Nhap_Dang_Ky.md) · Task sau: [AN-4](AN-4_Tab_Tai_Khoan.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 4.3

## Mục tiêu

Mở app thì vào màn Đăng nhập; đã đăng nhập rồi thì vào thẳng `MainActivity`. Task này **chỉ sửa `AndroidManifest.xml`**, commit riêng vì là file dùng chung.

## File

| File | Việc |
|---|---|
| `AndroidManifest.xml` | Chuyển LAUNCHER sang `LoginActivity` |

## Cách làm

1. Kéo code mới nhất (Hướng dẫn, mục 4.1). Có conflict ở manifest (người khác cũng thêm activity) thì **giữ cả hai**.
2. `LoginActivity`: thêm `intent-filter`, đổi `exported` thành `true`:

   ```xml
   <activity
       android:name=".ui.auth.LoginActivity"
       android:exported="true"
       android:screenOrientation="portrait"
       android:windowSoftInputMode="adjustResize">
       <intent-filter>
           <action android:name="android.intent.action.MAIN" />
           <category android:name="android.intent.category.LAUNCHER" />
       </intent-filter>
   </activity>
   ```

3. `MainActivity`: **xoá** khối `<intent-filter>` LAUNCHER, đổi `android:exported="true"` thành `"false"`. Giữ nguyên `windowSoftInputMode`.
4. **Không** đụng `CaptureExpenseActivity`, `provider`, quyền `INTERNET`, `usesCleartextTraffic`.

## Test

Gỡ app khỏi emulator trước (để xoá token cũ), rồi Run lại.

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| L1 | Mở app lần đầu | Vào màn **Đăng nhập** |
| L5 | Đăng nhập đúng, rồi bấm Back | Bấm Back **thoát app** (không quay về Đăng nhập) |
| L6 | Tắt hẳn app (vuốt khỏi đa nhiệm), mở lại | Vào thẳng `MainActivity` |
| R3 | Gỡ app, mở lại, Đăng ký tài khoản mới | Vào `MainActivity`; bấm (+) mở màn chụp (Phần 1): danh sách ví có "Tiền mặt" được chọn sẵn (cần backend đã có BE-3) |

## Commit

```text
chore: dat man dang nhap lam man hinh mo dau

- AndroidManifest: chuyen intent-filter LAUNCHER tu MainActivity sang LoginActivity
- MainActivity doi exported thanh false
```

## Xong khi

- [ ] Đạt L1, L5, L6, R3.
- [ ] `git diff HEAD~1 --stat` chỉ có `AndroidManifest.xml`.
- [ ] Đã commit, không có dòng co-author.
