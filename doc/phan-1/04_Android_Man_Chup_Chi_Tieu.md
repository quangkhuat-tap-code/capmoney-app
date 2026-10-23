# 04 · Android: Màn hình chụp ảnh ghi chi tiêu (`CaptureExpenseActivity`)

> Làm sau khi xong [03](03_Android_Nen_Tang_Chung.md). Gọi API theo [01](01_Database_Va_API_Contract.md): `GET /wallets`, `GET /categories`, `POST /transactions`.

Màn này mở khi bấm nút tròn (+) `fab_capture_expense` trên `MainActivity` (đã nối sẵn).

---

## 1. File cần tạo hoặc sửa

| File | Loại |
|---|---|
| `ui/capture/CaptureExpenseActivity.java` | Sửa (đang là class rỗng) |
| `ui/capture/CaptureViewModel.java` | Mới |
| `ui/capture/SelectCategoryBottomSheet.java` | Mới (`BottomSheetDialogFragment`) |
| `ui/capture/CategoryAdapter.java` | Mới |
| `ui/capture/WalletOption.java` | Mới (DTO, xem file 03, mục 5.4) |
| `res/layout/activity_capture_expense.xml` | Mới |
| `res/layout/bottom_sheet_select_category.xml` | Mới |
| `res/layout/item_category.xml` | Mới |

---

## 2. Bố cục `activity_capture_expense.xml`

Nền gốc `@color/bg_main`. Bọc nội dung trong `NestedScrollView`, để khi bàn phím hiện vẫn cuộn được; nút Lưu ghim ở đáy, ngoài vùng cuộn.

```
┌─────────────────────────────────────┐
│ [X]        Ghi chi tiêu             │  ← thanh trên, cao 56dp
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │                                 │ │
│ │     (ảnh preview vuông 1:1)     │ │  ← MaterialCardView bo 20dp
│ │                                 │ │
│ │    ╭─────────────────────╮      │ │
│ │    │   50.000 đ  (32sp)  │      │ │  ← ô nhập tiền đè trên lớp mờ
│ │    ╰─────────────────────╯      │ │
│ └─────────────────────────────────┘ │
│        [Đổi ảnh]                    │  ← chỉ hiện khi đã có ảnh
│                                     │
│ [  Chi tiêu  |  Thu nhập  ]         │  ← MaterialButtonToggleGroup
│                                     │
│ ┌ 💵 Tiền mặt · 🍜 Ăn uống      ›  ┐│  ← nút mở BottomSheet
│ ┌ 📅 22/10/2026 12:30           ›  ┐│  ← chọn ngày giờ
│ ┌ Ghi chú (tối đa 500 ký tự)      ┐ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ [            LƯU            ]       │  ← ghim đáy, cao 52dp
└─────────────────────────────────────┘
```

### 2.1. Chi tiết từng view

| id | View | Thuộc tính chính |
|---|---|---|
| `btn_close` | `ImageButton` | `ic_close_24`, 48×48dp, nền trong suốt, `contentDescription="@string/capture_close"` |
| `tv_title` | `TextView` | `@string/capture_title` = "Ghi chi tiêu", `@dimen/text_size_title`, `@color/text_primary`, bold, căn giữa |
| `card_photo` | `MaterialCardView` | Rộng `match_parent`, tỷ lệ **1:1** (ConstraintLayout `dimensionRatio="1:1"`), `cardCornerRadius="@dimen/corner_radius_card"`, `cardBackgroundColor="@color/bg_card"`, `strokeWidth=0`, margin ngang `@dimen/spacing_medium` |
| `iv_photo` | `ImageView` | Nằm trong card, `match_parent`, `scaleType="centerCrop"`, ẩn (`gone`) khi chưa có ảnh |
| `layout_photo_empty` | `LinearLayout` dọc | Nằm trong card, căn giữa, chứa 2 nút `btn_take_photo` ("📷 Chụp ảnh") và `btn_pick_photo` ("🖼️ Chọn từ thư viện"); kiểu `MaterialButton` outlined, chữ `@color/text_primary`, viền `@color/text_secondary`, bo `@dimen/corner_radius_pill`. Ẩn khi đã có ảnh |
| `et_amount` | `EditText` | Nằm trong card, neo đáy, cách đáy `@dimen/spacing_medium`; nền `@drawable/bg_capture_amount_pill`; padding 16/8dp; `inputType="number"`; `textSize="@dimen/text_size_money_large"`; bold; `textColor` đổi theo loại (mục 4.3); `hint="0"`, `textColorHint="@color/capture_text_hint"`; `gravity="center"`; `maxLength=19`; `imeOptions="actionDone"`. Hậu tố " đ" là một `TextView` cạnh bên (`tv_amount_suffix`) cùng màu và cỡ chữ, để không làm rối việc định dạng ô nhập |
| `btn_change_photo` | `MaterialButton` text button | `@string/capture_change_photo` = "Đổi ảnh"; chỉ hiện khi đã có ảnh. Bấm thì hiện dialog 2 lựa chọn: Chụp ảnh / Chọn từ thư viện / Xóa ảnh |
| `toggle_type` | `MaterialButtonToggleGroup` | `singleSelection=true`, `selectionRequired=true`; hai nút `btn_type_expense` ("Chi tiêu"), `btn_type_income` ("Thu nhập"); mỗi nút rộng `0dp`, `weight=1`; mặc định chọn **Chi tiêu** |
| `row_wallet_category` | `MaterialCardView` bấm được | Nền `@color/bg_card`, bo `@dimen/corner_radius_small`, cao tối thiểu 56dp; bên trong `tv_wallet_category`, mũi tên `ic_chevron_right_24` |
| `row_date` | `MaterialCardView` bấm được | Giống trên; bên trong `tv_date` |
| `et_note` | `EditText` | Nền `@drawable/bg_input_rounded`, padding 16dp, `hint="@string/capture_note_hint"`, `maxLength=500`, `inputType="textMultiLine\|textCapSentences"`, `minLines=2`, `maxLines=4` |
| `btn_save` | `MaterialButton` | `@string/capture_save` = "Lưu", nền `@color/color_accent`, chữ `@color/white`, cao 52dp, bo `@dimen/corner_radius_pill`, margin `@dimen/spacing_medium` |
| `progress_save` | `CircularProgressIndicator` | Đè giữa `btn_save`, `indeterminate`, ẩn mặc định |
| `progress_loading` | `LinearProgressIndicator` | Dưới thanh trên, hiện khi đang tải ví/danh mục |

Khoảng cách dọc giữa các khối là `@dimen/spacing_medium`.

---

## 3. Trạng thái: `CaptureViewModel` (extends `AndroidViewModel`)

`CaptureViewModel` giữ toàn bộ trạng thái để **xoay màn hình không mất dữ liệu**.

| LiveData | Kiểu | Mặc định |
|---|---|---|
| `photoUri` | `Uri` | `null` |
| `type` | `String` | `Transaction.TYPE_EXPENSE` |
| `amount` | `Long` | `0L` |
| `wallets` | `List<WalletOption>` | rỗng |
| `selectedWallet` | `WalletOption` | `null` |
| `categories` | `List<Category>` | rỗng (tất cả loại) |
| `selectedCategory` | `Category` | `null` |
| `dateTime` | `Calendar` | thời điểm mở màn |
| `note` | `String` | `""` |
| `loadingOptions` | `Boolean` | `false` |
| `saving` | `Boolean` | `false` |
| `event` | `SingleEvent<UiEvent>` | Sự kiện một lần: toast lỗi, lưu thành công |

> `SingleEvent<T>` là wrapper nhỏ có `getContentIfNotHandled()`, đặt trong `utils/SingleEvent.java` (Home cũng dùng), để toast không hiện lại khi xoay màn.
> `UiEvent` là class lồng trong `CaptureViewModel`, gồm `int kind` (`KIND_ERROR`, `KIND_SAVED`, `KIND_OPEN_CATEGORY_SHEET`) và `String message`.

Khi tạo ViewModel thì gọi `loadOptions()`: gọi song song `getWallets()` và `getCategories(null)`. Lấy danh mục cả 2 loại một lần, rồi lọc theo `type` ở client.
- Ví mặc định: ví đầu tiên có `isDefault()`; không có thì lấy ví đầu tiên.
- Danh sách ví rỗng → bắn sự kiện lỗi "Bạn chưa có ví nào. Hãy tạo ví ở tab Tài khoản" và **vô hiệu nút Lưu**.
- Lỗi mạng → hiện `Snackbar` có nút "Thử lại", bấm thì gọi lại `loadOptions()`.

`photoUri` của ảnh chụp **cũng phải** lưu vào `onSaveInstanceState`, vì app có thể bị hệ thống kill khi đang mở camera. Khôi phục trong `onCreate`.

---

## 4. Hành vi chi tiết

### 4.1. Lấy ảnh

```java
ActivityResultLauncher<Uri> takePicture =
    registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> { ... });

ActivityResultLauncher<PickVisualMediaRequest> pickImage =
    registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> { ... });
```

- **Chụp:** `pendingCaptureUri = ImageUtils.createCaptureUri(this)`, rồi `takePicture.launch(pendingCaptureUri)`. Callback `success == true` thì `viewModel.setPhoto(pendingCaptureUri)`; `false` thì giữ ảnh cũ.
- Bọc `launch` trong `try/catch (ActivityNotFoundException)` → toast "Không tìm thấy ứng dụng camera trên máy".
- **Thư viện:** `pickImage.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build())`. Callback `uri != null` thì `setPhoto(uri)`.
- Hiển thị: `Glide.with(this).load(uri).centerCrop().into(iv_photo)`. Ẩn `layout_photo_empty`, hiện `iv_photo` và `btn_change_photo`.
- Mở màn hình xong thì **không** tự mở camera. Người dùng tự chọn: chụp, chọn từ thư viện, hoặc không dùng ảnh.
- **Ảnh không bắt buộc** (ví dụ khoản Lương). Không có ảnh thì card hiện nút chọn ảnh, ô tiền vẫn nằm ở đáy card.

### 4.2. Ô nhập số tiền, định dạng khi gõ

- Dùng `TextWatcher` có cờ `isFormatting` để tránh lặp vô hạn.
- `afterTextChanged`: `long v = CurrencyUtils.parseDigits(s)`. Chuỗi mới là `v == 0 ? "" : CurrencyUtils.formatInput(v)`. Nếu khác chuỗi hiện tại thì `setText` và đặt con trỏ về cuối.
- Gõ `50000` thì hiển thị `50.000`; giới hạn 999.999.999.999.
- Cập nhật `viewModel.setAmount(v)`.
- Mở màn hình xong thì **focus vào `et_amount`** và hiện bàn phím số.

### 4.3. Chuyển loại Chi tiêu / Thu nhập

| | Chi tiêu | Thu nhập |
|---|---|---|
| Màu chữ số tiền và hậu tố | `@color/color_expense` | `@color/color_income` |
| Nút đang chọn trong toggle | nền `@color/color_expense`, chữ trắng | nền `@color/color_income`, chữ trắng |
| Nút không chọn | nền `@color/bg_card`, chữ `@color/text_secondary` | như bên trái |

Đổi loại mà `selectedCategory.type` khác loại mới thì đặt `selectedCategory = null`, và dòng chọn hiện "Chọn danh mục".

### 4.4. Dòng chọn Ví & Danh mục → `SelectCategoryBottomSheet`

Text của `tv_wallet_category`:
- Đủ cả hai: `"💵 Tiền mặt · 🍜 Ăn uống"`. Icon ví: `cash` 💵, `bank` 🏦, `saving` 🐷, loại khác 👛.
- Thiếu danh mục: `"💵 Tiền mặt · Chọn danh mục"`, phần "Chọn danh mục" màu `@color/capture_text_hint`.

Bấm dòng này thì mở `SelectCategoryBottomSheet.newInstance()`. Bottom sheet **lấy dữ liệu từ chính `CaptureViewModel`** (`new ViewModelProvider(requireActivity()).get(CaptureViewModel.class)`), nên không cần truyền qua `Bundle`.

Bố cục `bottom_sheet_select_category.xml` (nền `@color/bg_card`, bo 2 góc trên 20dp):
1. Thanh kéo 32×4dp màu `@color/text_secondary`, căn giữa.
2. Tiêu đề "Chọn ví" (`text_size_body`, `text_secondary`).
3. `ChipGroup chip_group_wallets` (`singleSelection`, `selectionRequired`), cuộn ngang trong `HorizontalScrollView`. Mỗi chip: `"💵 Tiền mặt"` và dòng phụ số dư `CurrencyUtils.format(balance)`, ví dụ `"💵 Tiền mặt · 1.950.000 đ"`. Chip đang chọn có nền `@color/color_accent`.
4. Tiêu đề "Chọn danh mục".
5. `RecyclerView rv_categories`, `GridLayoutManager(4 cột)`, chỉ hiện danh mục có `type` trùng loại đang chọn.

`item_category.xml`, cao 88dp:
- Vòng tròn 48dp nền là `category.getColorInt()` với alpha 0x33; giữa vòng là emoji `icon`, 24sp.
- Dưới vòng là tên danh mục, `text_size_small`, `text_primary`, tối đa 1 dòng, `ellipsize=end`.
- Item đang chọn: viền vòng tròn 2dp màu `category.getColorInt()`.

Hành vi:
- Bấm chip ví thì `viewModel.selectWallet(w)`; sheet **vẫn mở**.
- Bấm danh mục thì `viewModel.selectCategory(c)` và **đóng sheet**.
- Danh sách danh mục rỗng: hiện "Chưa có danh mục".

### 4.5. Chọn ngày giờ

Bấm `row_date`:
1. Mở `DatePickerDialog` (theme `ThemeOverlay.Material3.Dialog`) với ngày đang chọn; `datePicker.setMaxDate(now)`, không cho chọn ngày tương lai.
2. Chọn ngày xong thì mở `TimePickerDialog`, định dạng 24h.
3. Cập nhật `viewModel.setDateTime(cal)`. `tv_date` hiển thị `"📅 22/10/2026 12:30"`.
4. Chọn hôm nay mà giờ lớn hơn hiện tại thì kẹp về giờ hiện tại.

### 4.6. Bấm Lưu

Kiểm tra theo thứ tự. Gặp lỗi đầu tiên thì dừng và hiện `Snackbar`:

| Điều kiện | Thông báo |
|---|---|
| `amount <= 0` | "Vui lòng nhập số tiền" (focus `et_amount`) |
| `selectedWallet == null` | "Vui lòng chọn ví" |
| `selectedCategory == null` | "Vui lòng chọn danh mục" (bắn event `KIND_OPEN_CATEGORY_SHEET` để Activity tự mở bottom sheet) |
| `type == expense` và `amount > selectedWallet.balance` | "Số dư ví không đủ (còn X đ)". Server cũng chặn; kiểm ở client để báo sớm |

Hợp lệ thì:
1. `saving = true`: hiện `progress_save`, ẩn chữ nút, vô hiệu mọi input và `btn_close`.
2. Có ảnh thì nén bằng `ImageUtils.compressForUpload`, chạy trên `Executors.newSingleThreadExecutor()` đặt trong ViewModel (shutdown ở `onCleared`). Nén lỗi → "Không xử lý được ảnh, hãy chọn ảnh khác"; `saving = false`.
3. Gọi `repository.createTransaction(file, amount, walletId, categoryId, type, note.trim(), DateUtils.toServer(dateTime), cb)`.
4. **Thành công:** xóa file nén tạm, `Toast` "Đã lưu giao dịch", `setResult(RESULT_OK)`, rồi `finish()`. `HomeFragment` sẽ tự tải lại trong `onResume`.
5. **Lỗi:** `saving = false`, hiện `Snackbar` với `message` server trả về (ví dụ "Số dư ví không đủ để thực hiện giao dịch"). Giữ nguyên mọi dữ liệu người dùng đã nhập.

Chống bấm đúp: khi `saving == true` thì bỏ qua mọi cú bấm Lưu.

### 4.7. Thoát màn hình

`btn_close` và nút Back (dùng `getOnBackPressedDispatcher().addCallback`):
- Đang lưu → bỏ qua.
- Đã nhập gì (có ảnh, hoặc `amount > 0`, hoặc `note` không rỗng) → `MaterialAlertDialogBuilder` hỏi "Bỏ giao dịch này?", hai nút "Tiếp tục nhập" / "Bỏ" (chữ đỏ). Chọn Bỏ thì `finish()`.
- Chưa nhập gì → `finish()` luôn.

`onCreate` gọi `ImageUtils.clearOldUploads(this)` trên background thread.

---

## 5. Chữ hiển thị (`strings_capture_home.xml`)

```
capture_title              Ghi chi tiêu
capture_close              Đóng
capture_take_photo         📷 Chụp ảnh
capture_pick_photo         🖼️ Chọn từ thư viện
capture_change_photo       Đổi ảnh
capture_remove_photo       Xóa ảnh
capture_type_expense       Chi tiêu
capture_type_income        Thu nhập
capture_choose_category    Chọn danh mục
capture_choose_wallet      Chọn ví
capture_note_hint          Ghi chú (không bắt buộc)
capture_save               Lưu
capture_saved              Đã lưu giao dịch
capture_err_amount         Vui lòng nhập số tiền
capture_err_wallet         Vui lòng chọn ví
capture_err_category       Vui lòng chọn danh mục
capture_err_balance        Số dư ví không đủ (còn %1$s)
capture_err_no_wallet      Bạn chưa có ví nào. Hãy tạo ví ở tab Tài khoản
capture_err_no_camera      Không tìm thấy ứng dụng camera trên máy
capture_err_image          Không xử lý được ảnh, hãy chọn ảnh khác
capture_error_network      Không kết nối được máy chủ. Kiểm tra mạng và thử lại
capture_error_unauthorized Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại
capture_error_unknown      Đã có lỗi xảy ra, vui lòng thử lại
capture_retry              Thử lại
capture_discard_title      Bỏ giao dịch này?
capture_discard_keep       Tiếp tục nhập
capture_discard_confirm    Bỏ
capture_no_category        Chưa có danh mục
```

---

## 6. Nghiệm thu màn hình chụp

| # | Ca kiểm thử | Kỳ vọng |
|---|---|---|
| C1 | Bấm (+) trên Home | Mở màn hình; ô tiền được focus; ví mặc định đã chọn sẵn |
| C2 | Chụp ảnh bằng camera emulator | Ảnh hiện trong khung vuông bo góc 20dp |
| C3 | Chọn ảnh từ thư viện | Như trên |
| C4 | Gõ `1500000` | Hiển thị `1.500.000` đỏ, hậu tố " đ" |
| C5 | Đổi sang Thu nhập | Số chuyển xanh; danh mục chi bị bỏ chọn |
| C6 | Mở bottom sheet | Thấy chip ví và số dư; lưới 4 cột chỉ có danh mục đúng loại |
| C7 | Xoay màn hình sau khi đã nhập hết | Không mất ảnh, số tiền, ví, danh mục, ghi chú |
| C8 | Lưu khi thiếu danh mục | Snackbar báo lỗi, bottom sheet tự mở |
| C9 | Lưu expense lớn hơn số dư | Báo "Số dư ví không đủ (còn …)", không gọi API |
| C10 | Lưu hợp lệ có ảnh | Toast "Đã lưu giao dịch"; quay về Home; ảnh mới hiện đầu lưới |
| C11 | Lưu hợp lệ không ảnh | Lưu được; ô lưới hiện emoji danh mục |
| C12 | Tắt backend rồi bấm Lưu | Snackbar lỗi mạng; dữ liệu đã nhập còn nguyên; bấm Lưu lại được |
| C13 | Bấm Back khi đã nhập | Hiện hộp hỏi "Bỏ giao dịch này?" |
| C14 | Bấm Lưu 2 lần liên tục | Chỉ tạo **1** giao dịch |
| C15 | Ảnh 12MP từ camera | File upload ≤ ~1MB, ảnh không bị xoay ngang |
