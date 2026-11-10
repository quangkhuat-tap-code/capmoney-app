---
name: CapMoney
description: Hệ thống thiết kế Material Design 3 Dark Mode (OLED) cho ứng dụng quản lý tài chính cá nhân và nhật ký ảnh chi tiêu
colors:
  bg-main: "#0E0F12"
  bg-card: "#1B1C20"
  bg-card-secondary: "#26282E"
  text-primary: "#FFFFFF"
  text-secondary: "#8E8E93"
  color-expense: "#FF6464"
  color-income: "#4CD964"
  color-accent: "#5E7CE2"
  color-warning: "#F5A623"
  capture-overlay: "#99000000"
  capture-divider: "#2C2C2E"
  capture-hint: "#636366"
typography:
  money-large:
    fontSize: "32sp"
    fontWeight: 700
    letterSpacing: "-0.02em"
  title:
    fontSize: "18sp"
    fontWeight: 700
    letterSpacing: "0em"
  body:
    fontSize: "14sp"
    fontWeight: 400
    letterSpacing: "0em"
  small:
    fontSize: "12sp"
    fontWeight: 400
    letterSpacing: "0.01em"
  micro:
    fontSize: "10sp"
    fontWeight: 700
    letterSpacing: "0.02em"
rounded:
  cell: "8dp"
  small: "12dp"
  card: "20dp"
  pill: "50dp"
spacing:
  tiny: "4dp"
  small: "8dp"
  medium: "16dp"
  large: "24dp"
components:
  button-primary:
    backgroundColor: "{colors.color-accent}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.pill}"
    height: "48dp"
  button-disabled:
    backgroundColor: "{colors.bg-card-secondary}"
    textColor: "{colors.text-secondary}"
    rounded: "{rounded.pill}"
    height: "48dp"
  card-surface:
    backgroundColor: "{colors.bg-card}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.card}"
    padding: "16dp"
  calendar-cell:
    backgroundColor: "transparent"
    rounded: "{rounded.cell}"
    size: "1:1"
  input-field:
    backgroundColor: "{colors.bg-card-secondary}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.small}"
    padding: "12dp 16dp"
---

# Design System CapMoney

## Overview

CapMoney áp dụng phong cách thiết kế **Material Design 3 Dark Mode (OLED)** tối giản, thanh lịch và tập trung vào thị giác. 
Giao diện kết hợp giữa tính chính xác của một ứng dụng tài chính cá nhân với sự ấm áp, sống động của một cuốn nhật ký hình ảnh:
- **Tối ưu OLED & Thị giác:** Nền đen sâu `#0E0F12` tiết kiệm pin, giảm mỏi mắt khi sử dụng buổi tối và làm nổi bật màu sắc ảnh chụp hóa đơn/kỷ niệm.
- **Tonal Layering:** Thay vì dùng bóng đổ rườm rà (drop shadow), giao diện sử dụng các lớp nền tông sáng dần (`#0E0F12` → `#1B1C20` → `#26282E`) để thể hiện phân cấp chiều sâu và tương tác.
- **Dấu ấn hình ảnh:** Ô lịch tỷ lệ 1:1 làm nổi bật các bức ảnh thu chi gắn với từng ngày, tạo trải nghiệm trực quan độc đáo.

## Colors

Hệ màu tuân thủ nghiêm ngặt nguyên tắc Material Color Roles và độ tương phản cao trên nền tối:

### Background & Surface (Tonal Elevation)
- `bg-main` (`#0E0F12`): Nền gốc của toàn bộ màn hình (Scaffold / Root Layout).
- `bg-card` (`#1B1C20`): Bề mặt thẻ cấp 1 (Total Expense/Income Card, Wallet Card, Budget Card).
- `bg-card-secondary` (`#26282E`): Bề mặt cấp 2 (Input field background, secondary buttons, divider nhẹ).

### Semantic & Data
- `color-expense` (`#FF6464`): Màu đỏ san hô dịu mắt đại diện cho Chi tiêu, cảnh báo vượt ngân sách. Không dùng đỏ gắt gây ức chế thị giác.
- `color-income` (`#4CD964`): Màu xanh lục tươi mát đại diện cho Thu nhập, trạng thái an toàn tài chính.
- `color-accent` (`#5E7CE2`): Xanh cornflower hiện đại, là màu nhận diện thương hiệu chính của CapMoney. Dùng cho nút bấm chính (Primary CTA), viền đánh dấu ngày hôm nay (Today Indicator), tab đang chọn.
- `color-warning` (`#F5A623`): Cam hổ phách ấm áp dùng cho banner cảnh báo mất mạng (Offline mode), sắp chạm hạn mức ngân sách.

### Text & Iconography
- `text-primary` (`#FFFFFF`): Tiêu đề, số tiền chính, nội dung quan trọng nhất.
- `text-secondary` (`#8E8E93`): Nhãn phụ, danh mục, thời gian, mô tả bổ trợ.
- `capture-hint` (`#636366`): Placeholder trong ô nhập liệu.

## Typography

Sử dụng thang đo **Material Type Scale** với font hệ thống Android (Roboto) và luôn luôn dùng đơn vị `sp`:
- **Money Large (`32sp`, Bold):** Dành riêng cho số dư tổng ví, tổng tiền chi/thu tháng trên dashboard.
- **Title (`18sp`, Bold):** Tiêu đề màn hình, tên tháng, tên ví, số tiền của từng thẻ tổng kết.
- **Body (`14sp`, Normal):** Nội dung danh sách giao dịch, mô tả chi tiêu, ghi chú.
- **Small (`12sp`, Normal):** Tiêu đề phụ của thẻ (như "Tổng chi", "Tổng thu"), số ngày trên lịch tháng.
- **Micro (`10sp`, Bold):** Số tiền tóm tắt ở đáy ô lịch ngày.

## Layout

- **Hệ thống Spacing (Thang đo 8-dp grid):**
  - `spacing_tiny` = `4dp` (Khoảng cách giữa các thành phần nhỏ, nhãn và số tiền).
  - `spacing_small` = `8dp` (Khoảng cách giữa các item nội bộ trong card).
  - `spacing_medium` = `16dp` (Padding tiêu chuẩn hai bên màn hình và lề trong thẻ card).
  - `spacing_large` = `24dp` (Khoảng cách giữa các phân đoạn section lớn).
- **Navigation:** Bottom Navigation Bar cố định ở đáy màn hình chính, chiều cao 56–64dp với 4–5 tab chính (`Home`, `Budget`, `Wallets`, `Stats`, `Profile`). Nút chụp chi tiêu nhanh (FAB / Quick Action) được đặt ở vị trí thuận tiện nhất cho thao tác 1 tay.
- **Edge-to-Edge:** Áp dụng WindowInsets xử lý khoảng trống thanh trạng thái (Status Bar) và thanh điều hướng (System Navigation Bar) để nội dung không bị che khuất.

## Elevation & Depth

- Không sử dụng shadow nhân tạo lớn trên nền đen.
- **Phân cấp chiều sâu bằng độ sáng bề mặt (Tonal Elevation):**
  - Mức 0 (Base Canvas): `#0E0F12`
  - Mức 1 (Cards, Bottom Sheets): `#1B1C20`
  - Mức 2 (Inputs, Dialog surfaces, Chips): `#26282E`
- **Modal Overlay:** `capture-overlay` (`#99000000`) làm tối nền phía sau khi mở Dialog hoặc BottomSheet chọn danh mục / ngày giao dịch.

## Shapes

- **Cell (`8dp`):** Bo góc các ô ngày lịch (`item_calendar_day.xml`).
- **Small (`12dp`):** Bo góc ô nhập dữ liệu (`bg_input_rounded.xml`), danh mục nhỏ, thẻ chi tiêu trong danh sách.
- **Card (`20dp`):** Bo góc các thẻ lớn (`bg_wallet_list_card.xml`, thẻ ngân sách, biểu đồ thống kê).
- **Pill (`50dp`):** Nút CTA chính (`bg_auth_button.xml`), badge trạng thái ví, pill hiển thị số tiền trên camera (`bg_capture_amount_pill.xml`).

## Components

### 1. Calendar Day Tile (`SquareFrameLayout`)
- Tỷ lệ vuông 1:1.
- Nếu có ảnh chi tiêu: Hiển thị ảnh chụp hóa đơn/khoảnh khắc làm nền với `scaleType="centerCrop"` và `alpha="0.85"`.
- Nếu là ngày hiện tại: Viền stroke `2dp` màu `color-accent` (`#5E7CE2`).
- Số ngày đặt ở góc trên bên trái; tổng chi tiêu đặt ở đáy ô.

### 2. Primary Button (`bg_auth_button.xml`)
- Chiều cao tối thiểu 48dp (đảm bảo chuẩn touch target).
- Bo tròn dạng Pill (`50dp`).
- Nền `color-accent` (`#5E7CE2`) khi kích hoạt, chuyển sang `bg-card-secondary` (`#26282E`) khi `state_enabled="false"`.

### 3. Summary Cards (Tổng Thu / Tổng Chi)
- Đặt cạnh nhau theo chiều ngang, chia đều tỷ lệ `layout_weight="1"`.
- Nền `bg-card` (`#1B1C20`), bo góc `12dp` hoặc `20dp`.
- Số tiền hiển thị màu ngữ nghĩa rõ ràng: Đỏ san hô cho chi tiêu, Xanh lục cho thu nhập.

### 4. Input Fields (`bg_input_rounded.xml`)
- Nền `#26282E`, không viền stroke, bo góc `12dp`.
- Padding trong `12dp` top/bottom, `16dp` start/end.
- Chữ màu trắng `#FFFFFF`, hint màu `#636366`.

### 5. Offline Banner (`bg_home_offline_banner.xml`)
- Tự động xuất hiện khi mất kết nối mạng.
- Nền tông cam nhạt với chữ màu `color-warning` (`#F5A623`), thông báo dữ liệu đang được xem từ bộ nhớ đệm Room DB.

## Do's and Don'ts

### Do's
- **DO** luôn sử dụng đơn vị `sp` cho cỡ chữ và `dp` cho kích thước/khoảng cách.
- **DO** duy trì vùng chạm (touch target) tối thiểu 48×48 dp cho tất cả các nút và icon có thể click.
- **DO** sử dụng màu ngữ nghĩa nhất quán: đỏ chỉ dùng cho Chi tiêu / Cảnh báo, xanh chỉ dùng cho Thu nhập / An toàn.
- **DO** tận dụng lưới ảnh trên ô lịch để biến chi tiêu thành trải nghiệm trực quan sống động.
- **DO** sử dụng Room DB để cache dữ liệu và hiển thị ngay lập tức trước khi gọi API cập nhật.

### Don'ts
- **DON'T** dùng nền trắng tinh (`#FFFFFF`) làm nền chính hoặc nền thẻ trong chế độ Dark Mode của CapMoney.
- **DON'T** dùng shadow mờ đục hoặc màu đen đặc trên nền OLED tối.
- **DON'T** gán cứng kích thước text bằng `px` hoặc `dp`, vì sẽ phá vỡ tính năng Accessibility (phóng to chữ) của người dùng Android.
- **DON'T** đưa các nút điều hướng kiểu iOS (như nút Back dạng chevron không hỗ trợ gesture vuốt viền của Android) vào ứng dụng.
- **DON'T** nhồi nhét quá nhiều thông tin vào trong một ô lịch ngày; chỉ hiển thị ảnh nền đại diện, số ngày và con số tổng kết gọn gàng.

