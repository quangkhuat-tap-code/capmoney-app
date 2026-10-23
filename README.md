# Tài liệu nhóm CapMoney

Nhánh này **chỉ chứa tài liệu** (thư mục `doc/`), không có code. **Không merge** nhánh này vào `develop`.

## Lấy tài liệu về máy

Đứng ở thư mục `capmoney-app` (đang ở nhánh nào cũng được), chạy:

```bash
git fetch origin tai-lieu
```

```bash
git checkout origin/tai-lieu -- doc
```

```bash
git restore --staged doc
```

Lệnh thứ 2 chép thư mục `doc/` vào máy, lệnh thứ 3 bỏ `doc/` khỏi danh sách chuẩn bị commit.
Thêm dòng `/doc/` vào `.gitignore` để không commit nhầm tài liệu vào nhánh code.

Khi tài liệu được cập nhật, chạy lại 3 lệnh trên.

## Bắt đầu đọc từ đâu

1. `doc/Huong_Dan_Lam_Viec_Nhom.md`: quy trình Git, cách giao việc cho AI.
2. `doc/Tien_Do_Nhom.md`: task nào xong, chưa xong, đang chờ ai.
3. Thư mục phần của mình: `doc/phan-1/` (Lâm Anh), `doc/phan-2/` (Trung), `doc/phan-3/` (Quang). Bắt đầu từ file `00_Tong_Quan_...`.
