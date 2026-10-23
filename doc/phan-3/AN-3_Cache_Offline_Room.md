# AN-3 · Cache giao dịch offline bằng Room

> **Phần 3** · Repo `capmoney-app` · Nhánh `feature/stats-budget` · Task trước: [AN-2](AN-2_Tab_Thong_Ke.md) · Task sau: [AN-4](AN-4_Tab_Ngan_Sach.md)
> **Đọc trước:** [00_Tong_Quan_Phan_3.md](00_Tong_Quan_Phan_3.md) mục 3.2 · [01_Database_Va_API_Phan_3.md](01_Database_Va_API_Phan_3.md) mục 2.1

## Mục tiêu

Lưu giao dịch tải về vào Room. Khi **mất mạng**, Tab Thống kê vẫn hiện số liệu tháng đã từng mở, kèm banner báo đang xem dữ liệu đã lưu.

## File

| File | Việc |
|---|---|
| `data/local/TransactionEntity.java` | Tạo mới |
| `data/local/TransactionDao.java` | Tạo mới |
| `data/local/AppDatabase.java` | Tạo mới |
| `data/local/TransactionCache.java` | Tạo mới |
| `ui/stats/StatsCalculator.java` | Tạo mới |
| `app/src/test/java/com/example/comgroupcapmoney/ui/stats/StatsCalculatorTest.java` | Tạo mới |
| `ui/stats/StatsViewModel.java`, `StatsFragment.java`, `fragment_stats.xml` | Nối luồng offline + banner |

Room đã có trong `app/build.gradle` (`room-runtime` + `annotationProcessor room-compiler` 2.6.1). **Không** sửa `build.gradle`.

## 1. `TransactionEntity` — bảng `cached_transactions`

```java
@Entity(tableName = "cached_transactions",
        indices = {@Index(value = {"user_id", "year", "month"})})
public class TransactionEntity { ... }
```

| Field Java | Cột (`@ColumnInfo(name = ...)`) | Kiểu | Ghi chú |
|---|---|---|---|
| `id` | `id` | int, `@PrimaryKey` (không autoGenerate) | id giao dịch trên server |
| `userId` | `user_id` | int | `SharedPrefManager.getUserId()` — máy có thể đăng nhập nhiều tài khoản |
| `month`, `year` | `month`, `year` | int | tháng đang lưu |
| `amount` | `amount` | double | |
| `type` | `type` | String | `expense` / `income` |
| `note`, `imageUrl`, `transactionDate` | `note`, `image_url`, `transaction_date` | String | |
| `walletId`, `walletName` | `wallet_id`, `wallet_name` | int, String | |
| `categoryId` | `category_id` | int | |
| `categoryName`, `categoryIcon`, `categoryColor` | `category_name`, `category_icon`, `category_color` | String | |
| `cachedAt` | `cached_at` | long | `System.currentTimeMillis()` lúc lưu |

Field để `public` (Room đọc được), có constructor rỗng. Thêm hàm tĩnh:

```java
public static TransactionEntity from(Transaction t, int userId, int month, int year, long now)
```

## 2. `TransactionDao` — dùng `abstract class` để viết được hàm `@Transaction`

```java
@Dao
public abstract class TransactionDao {
    @Query("SELECT * FROM cached_transactions WHERE user_id = :userId AND year = :year AND month = :month ORDER BY transaction_date DESC")
    public abstract List<TransactionEntity> getMonth(int userId, int month, int year);

    @Query("DELETE FROM cached_transactions WHERE user_id = :userId AND year = :year AND month = :month")
    public abstract void deleteMonth(int userId, int month, int year);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<TransactionEntity> items);

    @Transaction
    public void replaceMonth(int userId, int month, int year, List<TransactionEntity> items) {
        deleteMonth(userId, month, year);   // giao dịch đã xoá trên server cũng biến mất khỏi cache
        insertAll(items);
    }
}
```

## 3. `AppDatabase`

```java
@Database(entities = {TransactionEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract TransactionDao transactionDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "capmoney_cache.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
```

## 4. `TransactionCache` — lớp duy nhất các màn khác dùng

Room **cấm** chạy trên main thread → mọi thao tác chạy nền, kết quả trả về main thread.

```java
public class TransactionCache {
    public interface Callback<T> { void onResult(T data); }        // luôn gọi trên main thread

    public TransactionCache(Context context) { ... }               // Executors.newSingleThreadExecutor() + new Handler(Looper.getMainLooper())

    public void saveMonth(int month, int year, List<Transaction> items);                    // replaceMonth cho user hiện tại
    public void getMonth(int month, int year, Callback<List<TransactionEntity>> cb);        // rỗng nếu chưa có
    public void getCachedAt(int month, int year, Callback<Long> cb);                        // 0 nếu chưa có (lấy cached_at lớn nhất)
}
```

- `getUserId() == -1` (chưa đăng nhập) → không lưu, `getMonth` trả rỗng.
- Thêm vào DAO câu `SELECT MAX(cached_at) ...` cho `getCachedAt`.

## 5. `StatsCalculator` (tính thống kê từ cache)

```java
public static StatsSummary fromCache(List<TransactionEntity> items, int month, int year, String type)
```

Tính **giống hệt** backend (01, mục 2.1): tổng thu, tổng chi; gom theo `categoryId` của `type` đang xem; `percent` = `amount / total * 100` làm tròn 1 chữ số; sắp `amount` giảm dần (bằng nhau thì `categoryId` tăng dần); `total = 0` → `categories` rỗng.

**Không** dùng class Android trong `StatsCalculator` (để unit test chạy trên máy tính).

## 6. Nối luồng offline vào `StatsViewModel.load()`

1. Gọi `getSummary`. **Thành công** → hiện, ẩn banner. Đồng thời gọi `getTransactions(month, year)` → thành công thì `TransactionCache.saveMonth(...)` (lỗi bước này bỏ qua, không báo).
2. `getSummary` lỗi **mạng** (`isNetworkError = true`) → `TransactionCache.getMonth(...)`:
   - Có dữ liệu → `StatsCalculator.fromCache(...)` → hiện, kèm banner `stats_offline_banner` (giờ lưu `HH:mm dd/MM` từ `getCachedAt`).
   - Rỗng → hiện `stats_offline_no_cache` + nút Thử lại.
3. `getSummary` lỗi **khác** (401, 400, 500) → hiện message + nút Thử lại. **Không** dùng cache.
4. Đang offline mà đổi tab Chi/Thu → tính lại từ cache (vẫn thử gọi API trước theo bước 1).
5. Giữ cơ chế `requestId` của AN-2 cho cả nhánh đọc cache.

Banner: thanh nền `color_warning` độ mờ 15%, chữ `color_warning`, icon ⚠, nằm dưới dòng "Thu … · Chi …".

## 7. Unit test `StatsCalculatorTest` (JUnit 4, dependency có sẵn)

| Ca | Dữ liệu | Kỳ vọng |
|---|---|---|
| U1 | Ăn uống 300.000 + 250.000 + 300.000, Di chuyển 500.000 (expense), Lương 8.000.000 (income); `type=expense` | `total = 1350000`, `total_income = 8000000`; Ăn uống `850000`, `63.0`; Di chuyển `500000`, `37.0`; Ăn uống đứng trước |
| U2 | Cùng dữ liệu, `type=income` | 1 danh mục Lương, `100.0` |
| U3 | Danh sách rỗng | Tổng 0, `categories` rỗng |
| U4 | 2 danh mục cùng số tiền | Danh mục `categoryId` nhỏ hơn đứng trước |

## Test

Chuẩn bị dữ liệu như AN-2.

```bash
./gradlew :app:testDebugUnitTest
```

| Ca | Thao tác | Kỳ vọng |
|---|---|---|
| U1–U4 | Lệnh trên | `StatsCalculatorTest` đạt |
| OF1 | Online mở Tháng 10/2026 (để lưu cache). **Tắt backend**. Rời tab rồi quay lại Tab Thống kê | Vẫn hiện đúng số như ST1, có banner "Đang xem dữ liệu đã lưu lúc …" |
| OF2 | Backend vẫn tắt, bấm Thu nhập | Tính từ cache: Lương 8.000.000 đ |
| OF3 | Backend vẫn tắt, chọn tháng chưa từng mở | "Không có mạng và chưa có dữ liệu lưu sẵn cho tháng này" + Thử lại |
| OF4 | Bật backend, bấm Thử lại | Banner biến mất, số liệu online |
| OF5 | Online: xoá 1 giao dịch ở Tab Home, mở lại Tab Thống kê, rồi tắt backend và xem lại | Số liệu offline **không** còn giao dịch đã xoá |

## Commit

```text
feat: cache giao dich offline bang room

- Them data/local: AppDatabase, TransactionEntity, TransactionDao (replaceMonth trong 1 transaction), TransactionCache chay nen
- Tab Thong ke luu giao dich thang dang xem; mat mang thi tinh lai tu cache bang StatsCalculator va hien banner offline
- Them StatsCalculatorTest
```

## Xong khi

- [ ] Đạt U1–U4, OF1 → OF5.
- [ ] Không còn `DEV ONLY`.
- [ ] Đã commit, không có dòng co-author.
