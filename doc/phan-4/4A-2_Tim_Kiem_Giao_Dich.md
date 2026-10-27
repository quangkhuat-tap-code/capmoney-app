# 4A-2 · Tìm kiếm giao dịch

> **Phần 4A** · Trình bày: **Lâm Anh** · Task trước: [4A-1](4A-1_Sua_Giao_Dich.md) · Task sau: [4A-3](4A-3_Doc_So_Tien_Hoa_Don.md)
> **Đọc trước:** [01_Database_Va_API_Phan_4.md](01_Database_Va_API_Phan_4.md) mục 2.2

## Mục tiêu

Gõ "cà phê", "grab" hay "Ăn uống" là ra mọi giao dịch khớp **ở mọi tháng**, không phải lật từng tháng trên Tab Home.

## Backend

| File | Việc |
|---|---|
| `src/controllers/transaction.controller.js` | Thêm `searchTransactions`, export |
| `src/routes/transaction.routes.js` | `router.get('/search', auth, searchTransactions);` đặt **ngay sau** `router.get('/', ...)`, trước `put/delete '/:id'` |

```js
const PAGE_SIZE = 20;
// q đã trim, đã kiểm tra 1–50 ký tự; page 1–1000
const like = `%${q.replace(/[\\%_]/g, '\\$&')}%`;
const sql = `${BASE_TRANSACTION_QUERY}
  WHERE t.user_id = ? AND (t.note LIKE ? OR c.name LIKE ?)
  ORDER BY t.transaction_date DESC, t.id DESC
  LIMIT ? OFFSET ?`;
const [rows] = await pool.query(sql, [req.user.id, like, like, PAGE_SIZE + 1, (page - 1) * PAGE_SIZE]);
const hasMore = rows.length > PAGE_SIZE;
// data: { items: rows.slice(0, PAGE_SIZE).map(mapTransaction), page, has_more: hasMore }
```

> Kiểm tra bí danh bảng danh mục trong `BASE_TRANSACTION_QUERY` (đang là `c`) trước khi dùng `c.name`.
> Collation `utf8mb4_unicode_ci` đã không phân biệt hoa thường và **không phân biệt dấu** ("ca phe" khớp "cà phê"). Đây là điểm cộng khi demo.

### Test backend

| Ca | Làm | Kỳ vọng |
|---|---|---|
| S1 | `q=ăn` | Giao dịch có ghi chú chứa "ăn" **và** mọi giao dịch danh mục "Ăn uống" |
| S2 | `q=AN UONG` | Giống S1 (không phân biệt hoa thường, dấu) |
| S3 | Tạo 25 giao dịch ghi chú "test-tim" rồi `q=test-tim` | Trang 1: 20 dòng, `has_more: true`; `page=2`: 5 dòng, `has_more: false` |
| S4 | `q=%` | Chỉ ra giao dịch có ký tự `%` thật (thường là `[]`), **không** ra tất cả |
| S5 | `q=` (rỗng), `q` 51 ký tự, `page=0`, `page=abc` | `400 "Từ khoá tìm kiếm không hợp lệ"` |
| S6 | Token user khác tìm cùng từ | Không thấy giao dịch của `demo@example.test` |

Xoá 25 giao dịch test ở S3 qua `DELETE /api/transactions/:id` (để số dư ví hoàn lại đúng).

**Commit backend:** `feat: api tim kiem giao dich`.

## Android

| File | Việc |
|---|---|
| `model/SearchPage.java` | `List<Transaction> items`, `int page`, `@SerializedName("has_more") boolean hasMore` |
| `data/api/TransactionApi.java` | `@GET("transactions/search") Call<ApiResponse<SearchPage>> search(@Query("q") String q, @Query("page") int page);` |
| `data/repository/TransactionRepository.java` | `search(q, page, cb)` |
| `ui/search/SearchActivity.java`, `SearchViewModel.java`, `SearchAdapter.java` | Màn tìm kiếm |
| `res/layout/activity_search.xml`, `item_search_result.xml` | Giao diện |
| `ui/home/HomeFragment.java` + `fragment_home.xml` | Nút kính lúp `btn_search` ở **bên phải** thanh chọn tháng |
| `AndroidManifest.xml` | Khai báo `.ui.search.SearchActivity` (`exported="false"`). **Commit riêng** |

### Màn tìm kiếm

```text
 ←  [ 🔍  Tìm ghi chú hoặc danh mục…        ✕ ]
 ───────────────────────────────────────────────
 🍜  Bún bò                       -45.000 đ
     Ăn uống · Tiền mặt · 20/10/2026 12:30
 🚗  Grab đi làm                  -32.000 đ
     Di chuyển · Techcombank · 19/10/2026 08:05
 …                        (cuộn gần cuối thì tải trang sau)
```

- Ô tìm kiếm tự focus, mở bàn phím. Gõ xong **chờ 400ms** không gõ thêm mới gọi API (dùng `Handler.postDelayed` + huỷ lệnh cũ). Bấm nút tìm trên bàn phím thì gọi ngay.
- Kết quả về **không đúng từ khoá đang gõ** (người dùng đã gõ tiếp) thì bỏ qua. ViewModel giữ `currentQuery`, chỉ nhận kết quả có `query` trùng.
- Dòng kết quả: emoji danh mục, ghi chú (không có thì hiện tên danh mục), số tiền đỏ/xanh theo loại (`CurrencyUtils.formatSigned`), dòng phụ `danh mục · ví · DateUtils.toDisplay(...)`.
- Cuộn còn 5 dòng tới cuối và `hasMore` → tải trang sau, hiện vòng xoay nhỏ ở cuối danh sách. Không gọi trùng khi đang tải.
- Trạng thái: chưa gõ → chữ "Nhập từ khoá để tìm"; không có kết quả → "Không tìm thấy giao dịch nào"; lỗi mạng → chữ lỗi + nút Thử lại.
- Bấm 1 dòng → `TransactionDetailDialog.newInstance(tx)` (có sẵn). Xoá hoặc sửa xong, quay lại màn tìm kiếm thì **tìm lại** từ khoá hiện tại (trong `onResume`, chỉ khi đã có từ khoá).
- Xoay màn hình giữ từ khoá và danh sách đã tải.

### Test Android

| Ca | Làm | Kỳ vọng |
|---|---|---|
| SA1 | Home → kính lúp | Mở màn tìm kiếm, bàn phím bật sẵn |
| SA2 | Gõ "bun" | Sau ~0,4 giây ra "Bún bò"; chỉ gọi API 1 lần (xem log OkHttp) |
| SA3 | 25 giao dịch test, cuộn xuống cuối | Tự tải trang 2, đủ 25 dòng, không trùng |
| SA4 | Gõ "zzzz" | "Không tìm thấy giao dịch nào" |
| SA5 | Bấm 1 kết quả → Xoá | Quay về danh sách, dòng đó biến mất |
| SA6 | Tắt backend, gõ từ khoá | Báo lỗi mạng + Thử lại |
| SA7 | Xoay màn hình | Giữ từ khoá và kết quả |

**Commit app:** `chore: khai bao man tim kiem trong manifest` (riêng) và `feat: man tim kiem giao dich`.

## Xong khi

- [ ] Đạt S1 → S6, SA1 → SA7.
- [ ] 3 commit (backend, manifest, app).
