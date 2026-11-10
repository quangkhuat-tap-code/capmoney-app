# Hướng Dẫn Phát Triển & Thiết Kế UI/UX (CapMoney)

Tài liệu này là quy chuẩn bắt buộc dành cho AI Coding Agents và lập trình viên khi phát triển giao diện, tính năng và trải nghiệm người dùng trong dự án **CapMoney**.

---

## 1. Vai Trò Kết Hợp Của 2 Skill Thiết Kế

Dự án tích hợp 2 skill hàng đầu để loại bỏ hoàn toàn "AI slop" (giao diện mẫu khuôn sáo, nhạt nhòa, thiếu điểm nhấn) và đảm bảo chất lượng phần mềm đạt tiêu chuẩn cao nhất:

| Thành phần | Skill đảm nhiệm | Vai trò cốt lõi |
|---|---|---|
| **Thư viện tri thức thiết kế** | **UI/UX Pro Max** (`.agents/skills/ui-ux-pro-max/`) | Cung cấp database tra cứu offline (phong cách, bảng màu, thang chữ, biểu đồ, mẫu landing/dashboard, UX guidelines cho Android/Jetpack Compose). |
| **Giám đốc nghệ thuật & Kiểm định** | **Impeccable** (`.agent/skills/impeccable/`) | Định hướng thẩm mỹ, phân cấp thị giác (visual hierarchy), thẩm định UX (`critique`), kiểm tra accessibility/contrast (`audit`), tinh chỉnh pixel (`polish`) và engine phát hiện lỗi (`detect`). |

---

## 2. Quy Trình 4 Bước Khi Xây Dựng Hoặc Sửa Giao Diện (Workflow)

Mỗi khi tạo mới màn hình, cập nhật dialog, layout hoặc refactor UI, Agent **bắt buộc** tuân theo quy trình 4 bước sau:

```
[Bước 1: Tra cứu] ──► [Bước 2: Xây dựng] ──► [Bước 3: Thẩm định] ──► [Bước 4: Kiểm tra]
(UI/UX Pro Max)      (DESIGN.md tokens)      (Impeccable review)     (Engine detect)
```

### Bước 1: Tra cứu & Lên ý tưởng (với UI/UX Pro Max)
Trước khi viết mã giao diện XML hoặc Compose:
1. Đọc [PRODUCT.md](PRODUCT.md) để nắm mục đích tính năng và [DESIGN.md](DESIGN.md) để lấy đúng tokens chuẩn.
2. Dùng công cụ tìm kiếm nội bộ để lấy gợi ý UX/UI phù hợp nhất:
   ```bash
   # Tra cứu mẫu sản phẩm tài chính tương tự:
   python .agents/skills/ui-ux-pro-max/scripts/search.py "personal finance expense tracker" --domain product

   # Tra cứu hướng dẫn phong cách Dark Mode OLED:
   python .agents/skills/ui-ux-pro-max/scripts/search.py "dark mode oled" --domain style

   # Tra cứu quy chuẩn UX cho form nhập liệu / touch targets:
   python .agents/skills/ui-ux-pro-max/scripts/search.py "mobile touch target form input" --domain ux
   ```

### Bước 2: Hiện thực hóa theo chuẩn Android Native
Khi triển khai layout XML (`res/layout/`) hoặc View logic:
- **Tuyệt đối tuân thủ tokens:** Sử dụng các giá trị định sẵn trong `colors.xml` và `dimens.xml` (xem [DESIGN.md](DESIGN.md)).
- **Chuẩn Native Android (xem [reference/android.md](.agent/skills/impeccable/reference/android.md)):**
  - **Touch target:** Tối thiểu `48×48 dp` cho mọi nút bấm, icon button và phần tử có thể click.
  - **Typography:** Luôn sử dụng đơn vị `sp` (không dùng `dp` hay `px` cho text) để tôn trọng cài đặt font scale của hệ điều hành.
  - **Tonal Elevation:** Dùng sự chênh lệch độ sáng màu nền (`#0E0F12` → `#1B1C20` → `#26282E`) thay cho shadow đen nhân tạo.
  - **Navigation:** Giữ cử chỉ vuốt viền (System Back gesture) tự nhiên, không trap người dùng.

### Bước 3: Đánh giá & Tinh chỉnh chuyên sâu (với Impeccable)
Sau khi hoàn thành bản nháp giao diện, dùng các lệnh Impeccable trong chat để kiểm định:
- `/impeccable critique [layout_file]`: Đánh giá phân cấp thị giác, luồng thao tác, nhãn text và độ thân thiện.
- `/impeccable audit [layout_file]`: Kiểm tra kỹ thuật: tương phản màu (WCAG AA), khoảng cách chạm, cắt xén text khi phóng to font chữ.
- `/impeccable polish`: Rà soát vi mô về padding/margin, căn thẳng hàng và viền stroke.
- Điều chỉnh sắc thái nếu cần:
  - `/bolder`: Tăng điểm nhấn thị giác nếu màn hình bị chìm, đều đều, thiếu cá tính.
  - `/quieter`: Giảm bớt chi tiết thừa nếu màn hình bị rối mắt, quá nhiều màu sắc cạnh tranh nhau.

### Bước 4: Kiểm tra tự động bằng Engine (Automated Gate)
Trước khi bàn giao hoặc kết thúc task, chạy engine kiểm tra lỗi tự động:
```bash
npx impeccable detect app
```
Lệnh này quét toàn bộ layout và code để tìm các anti-patterns phổ biến mà không tốn token AI.

---

## 3. Bảng Lệnh Tra Cứu Nhanh (Cheat Sheet)

| Nhu cầu công việc | Lệnh khuyên dùng | Công cụ / Skill |
|---|---|---|
| Tra cứu phong cách / UX guidelines | `python .agents/skills/ui-ux-pro-max/scripts/search.py "<query>" --domain <domain>` | UI/UX Pro Max |
| Sinh bộ Design System khuyến nghị | `python .agents/skills/ui-ux-pro-max/scripts/search.py "CapMoney" --design-system` | UI/UX Pro Max |
| Nắm bối cảnh sản phẩm cốt lõi | Đọc [PRODUCT.md](PRODUCT.md) hoặc chạy `/impeccable init` | Impeccable |
| Tra cứu chuẩn màu, typography, shapes | Đọc [DESIGN.md](DESIGN.md) | Design System |
| Review UX & độ rõ ràng của màn hình | `/impeccable critique <tên_file>` | Impeccable |
| Kiểm tra Accessibility & Spacing | `/impeccable audit <tên_file>` | Impeccable |
| Tinh chỉnh padding & alignment vi mô | `/impeccable polish` | Impeccable |
| Quét tự động anti-patterns mã nguồn | `npx impeccable detect app` | Impeccable Engine |

---

## 4. Những Điều Tuyệt Đối Cấm (Anti-patterns & Bans)

1. ❌ **Không hardcode màu sắc bừa bãi:** Không điền mã màu trực tiếp (raw hex như `#123456`) vào file layout XML; phải tham chiếu qua `@color/...`.
2. ❌ **Không dùng `px` hoặc `dp` cho cỡ chữ:** Bắt buộc dùng `sp` để hỗ trợ người dùng thị lực kém phóng to chữ trong cài đặt máy.
3. ❌ **Không đưa tư duy iOS sang Android:** Không dùng các nút điều hướng hay switch dạng iOS; tuân thủ Material Design 3 và hệ thống gesture của Android.
4. ❌ **Không làm ô lịch bị quá tải thông tin:** Tính năng trọng tâm của CapMoney là **Lưới lịch ảnh chi tiêu theo ngày**. Trong mỗi ô ngày, chỉ hiển thị ảnh nền đại diện (crop vuông), số ngày và tổng tiền tóm tắt ở chân ô. Mọi chi tiết cụ thể phải mở qua BottomSheet hoặc Dialog.
5. ❌ **Không bỏ qua Dark Mode:** CapMoney lấy Dark Mode (OLED) làm bản sắc chính. Luôn kiểm tra độ tương phản giữa chữ và nền để đảm bảo dễ đọc trong mọi điều kiện ánh sáng.

