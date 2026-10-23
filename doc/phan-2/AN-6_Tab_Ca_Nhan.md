# AN-6 · Tab Cá nhân và Đăng xuất

> **Phần 2** · Repo `capmoney-app` · Nhánh `feature/wallets-auth` · Task trước: [AN-5](AN-5_Them_Sua_Vi_Chuyen_Tien.md) · Task sau: [99_Nghiem_Thu_Va_PR](99_Nghiem_Thu_Va_PR.md)
> **Đọc trước:** [00_Tong_Quan_Phan_2.md](00_Tong_Quan_Phan_2.md) mục 5 · [01_Database_Va_API_Phan_2.md](01_Database_Va_API_Phan_2.md) mục 2.3

## Mục tiêu

Tab 5 hiển thị thông tin người dùng, 3 thẻ thống kê nhanh, nút Đăng xuất.

## File

| File | Việc |
|---|---|
| `ui/profile/ProfileFragment.java` | Viết lại (đang rỗng) |
| `ui/profile/ProfileViewModel.java` | Tạo mới |
| `res/layout/fragment_profile.xml` | Tạo mới |
| `res/drawable/bg_profile_avatar.xml`, `bg_profile_stat_card.xml` | Tạo mới |

## Bố cục `fragment_profile.xml`

```text
Cá nhân

        ( A )          avatar tròn 88dp, nền color_accent, chữ cái trắng 36sp
   Nguyễn Văn An       20sp đậm
     a@gmail.com       text_secondary
  Thành viên từ 22/10/2026

┌──────────┐ ┌───────────┐ ┌───────────┐
│    12    │ │8.000.000 đ│ │1.350.000 đ│   3 thẻ bg_profile_stat_card ngang nhau (layout_weight 1)
│ Giao dịch│ │ Tổng thu  │ │ Tổng chi  │   Tổng thu màu color_income, Tổng chi màu color_expense
└──────────┘ └───────────┘ └───────────┘

[        Đăng xuất        ]                   nút viền color_expense, chữ color_expense
```

## Hành vi

- Chữ cái avatar: ký tự đầu của **từ cuối** trong họ tên, viết hoa ("Nguyễn Văn An" → "A"). Tên rỗng → "?".
- "Thành viên từ": phần ngày `dd/MM/yyyy` của `created_at` (`"2026-10-22 10:09:00"` → `"22/10/2026"`), dùng `SimpleDateFormat`.
- Số tiền: `CurrencyUtils.format`. `TextView` số tiền tự co chữ để không bị cắt: `app:autoSizeTextType="uniform"`, `app:autoSizeMinTextSize="10sp"`, `app:autoSizeMaxTextSize="16sp"`, `maxLines="1"`.
- Tải ở `onResume` (ghi giao dịch xong quay lại thấy số mới).
- Lỗi mạng: hiện tên lấy từ `SharedPrefManager.getUserName()`, 3 số hiện "—", kèm nút "Thử lại".
- Đăng xuất → `AlertDialog` `profile_logout_confirm_title` / `profile_logout_confirm_msg` → đồng ý thì `SessionManager.logout(requireActivity(), false)`.
- API trả 401 → `SessionManager.logout(requireActivity(), true)`.
- Xoay màn hình: không gọi lại API.

## Test

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| P1 | Mở Tab Cá nhân | Avatar chữ cái đúng, tên, email, ngày tham gia, 3 thẻ số |
| P2 | Ghi thêm 1 giao dịch bằng màn chụp (+) rồi quay lại tab | Số giao dịch và tổng thu/chi tăng |
| P3 | Đăng xuất → Huỷ | Không có gì xảy ra |
| P4 | Đăng xuất → Đồng ý | Về màn Đăng nhập; bấm Back thoát app; mở lại app vẫn ở màn Đăng nhập |
| P5 | Đổi `JWT_SECRET` trong `.env` backend, khởi động lại backend, mở Tab Cá nhân | Toast "Phiên đăng nhập đã hết hạn…", về màn Đăng nhập. **Nhớ trả `JWT_SECRET` lại như cũ** |
| P6 | Tắt backend, mở tab | Tên từ máy, số hiện "—", có nút Thử lại |
| X1 | Xoay màn hình | Không crash, không gọi lại API |

## Commit

```text
feat: tab ca nhan va dang xuat

- ProfileFragment: avatar chu cai, ho ten, email, ngay tham gia, 3 the so giao dich, tong thu, tong chi
- Nut Dang xuat co xac nhan, xoa token va ve man dang nhap; tu ve dang nhap khi API bao 401
```

## Xong khi

- [ ] Đạt P1 → P6, X1.
- [ ] Đã commit, không có dòng co-author.
- [ ] Sang [99_Nghiem_Thu_Va_PR.md](99_Nghiem_Thu_Va_PR.md).
