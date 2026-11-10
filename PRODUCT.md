# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Cá nhân, sinh viên và người đi làm trẻ tuổi có nhu cầu quản lý chi tiêu hằng ngày, theo dõi ngân sách và kiểm soát dòng tiền giữa các ví cá nhân trong cuộc sống bận rộn.

## Product Purpose

Biến việc ghi chép tài chính cá nhân vốn khô khan thành một trải nghiệm thú vị và trực quan. CapMoney giúp người dùng nắm bắt tình hình tài chính, hạn chế bội chi và lưu giữ lại những khoảnh khắc, kỷ niệm gắn liền với các khoản chi tiêu hằng ngày.

## Positioning

Khác biệt với các ứng dụng sổ thu chi truyền thống chỉ hiển thị con số và danh mục đơn điệu, CapMoney định vị là **"Sổ thu chi kết hợp nhật ký trải nghiệm bằng hình ảnh"**:
- Cho phép chụp hoặc đính kèm ảnh hóa đơn/hoạt động thực tế như một kỷ niệm.
- Đặt trực tiếp hình ảnh đại diện vào từng ô ngày trên khung lịch tháng (Calendar Photo Grid), giúp người dùng chỉ cần lướt qua màn hình lịch là thấy ngay hôm đó mình đã đi đâu, làm gì, trải nghiệm gì và chi tiêu bao nhiêu.
- Kết hợp đầy đủ khả năng quản lý đa ví tiền và phân bổ ngân sách thông minh.

## Operating Context

- Sử dụng chủ yếu trên điện thoại di động Android khi người dùng đang di chuyển, thanh toán tại quầy ăn uống, mua sắm hoặc cuối ngày xem lại chi tiêu.
- Thao tác nhanh bằng một tay, cần độ phản hồi tức thì và luồng chụp/ghi nhận chi tiêu ít bước nhất có thể.

## Capabilities and Constraints

- **Nền tảng & Công nghệ:** Android Native (Java, Min SDK 24, Target SDK 37, compileSdk 37).
- **Giao diện & Thành phần:** XML Layouts kết hợp Google Material Components (Material Design 3).
- **Lưu trữ & Dữ liệu:** Hỗ trợ lưu trữ cục bộ Room Database và đồng bộ REST API qua Retrofit với `capmoney-backend` (Node.js + MySQL).
- **Chức năng chính đã triển khai:**
  - Lịch trực quan theo tháng hiển thị ảnh kỷ niệm/chi tiêu từng ngày (`item_calendar_day`, `item_photo_grid`, `bottom_sheet_day_transactions`).
  - Ghi chép & Chụp ảnh chi tiêu (`CaptureExpenseActivity`).
  - Quản lý đa ví (tiền mặt, thẻ ngân hàng, ví điện tử) và luân chuyển dòng tiền giữa các ví (`wallets`, `dialog_transfer_money`).
  - Thiết lập và theo dõi hạn mức ngân sách (`budget`, `dialog_add_budget`).
  - Báo cáo thống kê trực quan theo danh mục và thời gian (`stats`).
  - Tìm kiếm và lọc lịch sử giao dịch (`search`).

## Brand Commitments

- **Tên ứng dụng:** CapMoney (`comgroupcapmoney`).
- **Ngôn ngữ:** Tiếng Việt là ngôn ngữ giao diện chính.
- **Tone of Voice:** Thân thiện, hiện đại, gần gũi, rõ ràng và truyền cảm hứng tiết kiệm/quản lý tài chính lành mạnh.
- **Định hướng thiết kế:** Chuẩn Material Design 3, sử dụng hệ thống màu Tonal Elevation, bo góc hiện đại, hỗ trợ Dark Mode.

## Evidence on Hand

- Toàn bộ source code Android hoàn chỉnh tại `app/src/main/` với hệ thống Activity, Fragment, Dialog và BottomSheet đã cấu hình sẵn.
- Backend REST API tại `capmoney-backend` chạy trên cổng 5000.
- Tài liệu hướng dẫn phát triển nội bộ: `Guide_ChayAppVoiAI_v1.0.md` và `README.md`.

## Product Principles

1. **Visual Memory Over Raw Numbers:** Gắn con số chi tiêu với trải nghiệm thực tế; xem lịch chi tiêu như lướt nhật ký hình ảnh sinh động.
2. **Speed & Low Friction:** Thao tác nhập liệu hoặc chụp ảnh hóa đơn diễn ra trong vòng vài giây, loại bỏ rào cản lười ghi chép.
3. **Wallet & Budget Clarity:** Minh bạch dòng tiền – người dùng luôn biết tiền đang ở ví nào và ngân sách danh mục còn bao nhiêu trước khi quyết định chi tiêu.
4. **Android Native Excellence:** Tuân thủ triệt để nguyên tắc Material Design 3, đảm bảo gesture Back mượt mà, edge-to-edge window insets và độ ổn định cao.

## Accessibility & Inclusion

- Kích thước vùng chạm (touch target) tối thiểu 48×48 dp trên toàn bộ nút bấm và icon tương tác.
- Sử dụng đơn vị `sp` cho typography để tự động co giãn theo thiết lập cỡ chữ của hệ điều hành Android.
- Đảm bảo độ tương phản màu chuẩn WCAG AA giữa chữ và nền trên cả Light Mode và Dark Mode.

