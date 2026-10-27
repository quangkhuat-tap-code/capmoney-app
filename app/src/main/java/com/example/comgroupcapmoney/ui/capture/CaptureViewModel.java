package com.example.comgroupcapmoney.ui.capture;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.TransactionRepository;
import com.example.comgroupcapmoney.model.Category;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.example.comgroupcapmoney.utils.ImageUtils;
import com.example.comgroupcapmoney.utils.SingleEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureViewModel extends AndroidViewModel {

    public static class UiEvent {
        public static final int KIND_ERROR = 1;
        public static final int KIND_SAVED = 2;
        public static final int KIND_OPEN_CATEGORY_SHEET = 3;

        private final int kind;
        private final String message;

        public UiEvent(int kind, String message) {
            this.kind = kind;
            this.message = message;
        }

        public int getKind() {
            return kind;
        }

        public String getMessage() {
            return message;
        }
    }

    private final TransactionRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Uri> photoUri = new MutableLiveData<>(null);
    private final MutableLiveData<String> type = new MutableLiveData<>(Transaction.TYPE_EXPENSE);
    private final MutableLiveData<Long> amount = new MutableLiveData<>(0L);
    private final MutableLiveData<List<WalletOption>> wallets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<WalletOption> selectedWallet = new MutableLiveData<>(null);
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>(null);
    private final MutableLiveData<Calendar> dateTime = new MutableLiveData<>(Calendar.getInstance());
    private final MutableLiveData<String> note = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> loadingOptions = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<SingleEvent<UiEvent>> event = new MutableLiveData<>();

    // Che do sua (4A-1): giao dich dang sua, null = them moi
    private Transaction editTx;

    public CaptureViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TransactionRepository(application);
        loadOptions();
    }

    public LiveData<Uri> getPhotoUri() { return photoUri; }
    public LiveData<String> getType() { return type; }
    public LiveData<Long> getAmount() { return amount; }
    public LiveData<List<WalletOption>> getWallets() { return wallets; }
    public LiveData<WalletOption> getSelectedWallet() { return selectedWallet; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<Category> getSelectedCategory() { return selectedCategory; }
    public LiveData<Calendar> getDateTime() { return dateTime; }
    public LiveData<String> getNote() { return note; }
    public LiveData<Boolean> getLoadingOptions() { return loadingOptions; }
    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<SingleEvent<UiEvent>> getEvent() { return event; }

    public boolean isEditing() {
        return editTx != null;
    }

    public Transaction getEditTx() {
        return editTx;
    }

    // Dien san du lieu giao dich can sua, chi lan dau (xoay man hinh khong dien lai)
    public boolean initEdit(Transaction tx) {
        if (editTx != null || tx == null) {
            return false;
        }
        editTx = tx;
        type.setValue(tx.isExpense() ? Transaction.TYPE_EXPENSE : Transaction.TYPE_INCOME);
        amount.setValue(Math.round(tx.getAmount()));
        note.setValue(tx.getNote() != null ? tx.getNote() : "");
        if (tx.getParsedDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(tx.getParsedDate());
            dateTime.setValue(cal);
        }
        selectEditWallet(wallets.getValue());
        selectEditCategory(categories.getValue());
        return true;
    }

    // Chon dung vi cua giao dich dang sua; vi khong con thi de trong de nguoi dung chon lai
    private void selectEditWallet(List<WalletOption> list) {
        if (editTx == null || list == null) return;
        WalletOption found = null;
        for (WalletOption w : list) {
            if (w.getId() == editTx.getWalletId()) {
                found = w;
                break;
            }
        }
        selectedWallet.setValue(found);
    }

    private void selectEditCategory(List<Category> list) {
        if (editTx == null || list == null) return;
        Category found = null;
        for (Category c : list) {
            if (c.getId() == editTx.getCategoryId()) {
                found = c;
                break;
            }
        }
        selectedCategory.setValue(found);
    }

    // Co thay doi so voi giao dich goc khong (de hoi truoc khi thoat)
    public boolean hasEditChanges() {
        if (editTx == null) return false;
        Long a = amount.getValue();
        WalletOption w = selectedWallet.getValue();
        Category c = selectedCategory.getValue();
        String n = note.getValue() != null ? note.getValue().trim() : "";
        String oldNote = editTx.getNote() != null ? editTx.getNote().trim() : "";
        Calendar cal = dateTime.getValue();
        String date = cal != null ? DateUtils.toServer(cal) : "";
        return a == null || a != Math.round(editTx.getAmount())
                || w == null || w.getId() != editTx.getWalletId()
                || c == null || c.getId() != editTx.getCategoryId()
                || !n.equals(oldNote)
                || !date.equals(editTx.getTransactionDate());
    }

    public void setPhoto(Uri uri) {
        photoUri.setValue(uri);
    }

    public void setAmount(long v) {
        amount.setValue(v);
    }

    public void setNote(String n) {
        note.setValue(n);
    }

    public void setDateTime(Calendar cal) {
        dateTime.setValue(cal);
    }

    public void selectWallet(WalletOption w) {
        selectedWallet.setValue(w);
    }

    public void selectCategory(Category c) {
        selectedCategory.setValue(c);
    }

    public void setType(String newType) {
        if (!newType.equals(type.getValue())) {
            type.setValue(newType);
            Category currentCat = selectedCategory.getValue();
            if (currentCat != null && !newType.equalsIgnoreCase(currentCat.getType())) {
                selectedCategory.setValue(null);
            }
        }
    }

    public void loadOptions() {
        loadingOptions.setValue(true);

        repository.getWallets(new TransactionRepository.ResultCallback<List<WalletOption>>() {
            @Override
            public void onSuccess(List<WalletOption> data) {
                loadingOptions.setValue(false);
                wallets.setValue(data != null ? data : new ArrayList<>());

                if (data == null || data.isEmpty()) {
                    event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR,
                            getApplication().getString(R.string.capture_err_no_wallet))));
                } else if (editTx != null) {
                    if (selectedWallet.getValue() == null) {
                        selectEditWallet(data);
                    }
                } else if (selectedWallet.getValue() == null) {
                    WalletOption defaultWallet = null;
                    for (WalletOption w : data) {
                        if (w.isDefault()) {
                            defaultWallet = w;
                            break;
                        }
                    }
                    if (defaultWallet == null) {
                        defaultWallet = data.get(0);
                    }
                    selectedWallet.setValue(defaultWallet);
                }
            }

            @Override
            public void onError(String message) {
                loadingOptions.setValue(false);
                event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR, message)));
            }
        });

        repository.getCategories(null, new TransactionRepository.ResultCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.setValue(data != null ? data : new ArrayList<>());
                if (editTx != null && selectedCategory.getValue() == null) {
                    selectEditCategory(data);
                }
            }

            @Override
            public void onError(String message) {
                // silently fail or handled by snackbar
            }
        });
    }

    public void save(Context context) {
        if (Boolean.TRUE.equals(saving.getValue())) {
            return;
        }

        Long currentAmount = amount.getValue();
        if (currentAmount == null || currentAmount <= 0) {
            event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR,
                    context.getString(R.string.capture_err_amount))));
            return;
        }

        WalletOption wallet = selectedWallet.getValue();
        if (wallet == null) {
            event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR,
                    context.getString(R.string.capture_err_wallet))));
            return;
        }

        Category category = selectedCategory.getValue();
        if (category == null) {
            event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_OPEN_CATEGORY_SHEET,
                    context.getString(R.string.capture_err_category))));
            return;
        }

        String currentType = type.getValue() != null ? type.getValue() : Transaction.TYPE_EXPENSE;
        // Sua khoan chi cung vi: so cu se duoc hoan lai nen duoc dung them so cu
        double available = wallet.getBalance();
        if (editTx != null && editTx.isExpense() && wallet.getId() == editTx.getWalletId()) {
            available += editTx.getAmount();
        }
        if (Transaction.TYPE_EXPENSE.equals(currentType) && currentAmount > available) {
            String msg = context.getString(R.string.capture_err_balance, CurrencyUtils.format(available));
            event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR, msg)));
            return;
        }

        saving.setValue(true);

        if (editTx != null) {
            update(context, currentAmount, wallet, category);
            return;
        }

        Uri uri = photoUri.getValue();
        executor.execute(() -> {
            File compressedFile = null;
            if (uri != null) {
                try {
                    compressedFile = ImageUtils.compressForUpload(getApplication(), uri);
                } catch (Exception e) {
                    saving.postValue(false);
                    event.postValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR,
                            context.getString(R.string.capture_err_image))));
                    return;
                }
            }

            final File finalFile = compressedFile;
            String noteText = note.getValue() != null ? note.getValue().trim() : "";
            Calendar cal = dateTime.getValue() != null ? dateTime.getValue() : Calendar.getInstance();
            String dateStr = DateUtils.toServer(cal);

            repository.createTransaction(finalFile, currentAmount, wallet.getId(), category.getId(),
                    currentType, noteText, dateStr,
                    new TransactionRepository.ResultCallback<Transaction>() {
                        @Override
                        public void onSuccess(Transaction data) {
                            if (finalFile != null && finalFile.exists()) {
                                finalFile.delete();
                            }
                            saving.setValue(false);
                            event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_SAVED,
                                    context.getString(R.string.capture_saved))));
                        }

                        @Override
                        public void onError(String message) {
                            if (finalFile != null && finalFile.exists()) {
                                finalFile.delete();
                            }
                            saving.setValue(false);
                            event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR, message)));
                        }
                    });
        });
    }

    private void update(Context context, long currentAmount, WalletOption wallet, Category category) {
        String noteText = note.getValue() != null ? note.getValue().trim() : "";
        Calendar cal = dateTime.getValue() != null ? dateTime.getValue() : Calendar.getInstance();
        repository.updateTransaction(editTx.getId(), currentAmount, wallet.getId(), category.getId(),
                noteText, DateUtils.toServer(cal),
                new TransactionRepository.ResultCallback<Transaction>() {
                    @Override
                    public void onSuccess(Transaction data) {
                        saving.setValue(false);
                        event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_SAVED,
                                context.getString(R.string.edit_saved))));
                    }

                    @Override
                    public void onError(String message) {
                        saving.setValue(false);
                        event.setValue(new SingleEvent<>(new UiEvent(UiEvent.KIND_ERROR, message)));
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
