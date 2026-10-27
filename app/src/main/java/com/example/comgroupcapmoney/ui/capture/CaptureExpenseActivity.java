package com.example.comgroupcapmoney.ui.capture;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Category;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.example.comgroupcapmoney.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class CaptureExpenseActivity extends AppCompatActivity {

    private static final String KEY_PENDING_CAPTURE_URI = "pending_capture_uri";

    private CaptureViewModel viewModel;
    private Uri pendingCaptureUri;

    private ImageButton btnClose;
    private LinearProgressIndicator progressLoading;
    private ImageView ivPhoto;
    private LinearLayout layoutPhotoEmpty;
    private MaterialButton btnTakePhoto;
    private MaterialButton btnPickPhoto;
    private MaterialButton btnChangePhoto;
    private EditText etAmount;
    private TextView tvAmountSuffix;
    private MaterialButtonToggleGroup toggleType;
    private MaterialButton btnTypeExpense;
    private MaterialButton btnTypeIncome;
    private MaterialCardView rowWalletCategory;
    private TextView tvWalletCategory;
    private MaterialCardView rowDate;
    private TextView tvDate;
    private EditText etNote;
    private MaterialButton btnSave;
    private CircularProgressIndicator progressSave;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_expense);
        applyWindowInsets();

        if (savedInstanceState != null) {
            String uriStr = savedInstanceState.getString(KEY_PENDING_CAPTURE_URI);
            if (uriStr != null) {
                pendingCaptureUri = Uri.parse(uriStr);
            }
        }

        Executors.newSingleThreadExecutor().execute(() -> ImageUtils.clearOldUploads(getApplicationContext()));

        viewModel = new ViewModelProvider(this).get(CaptureViewModel.class);
        // API bao 401 (token het han) thi ve man dang nhap
        SessionManager.observeExpired(this, this);

        initViews();
        setupActivityLaunchers();
        setupListeners();
        setupBackPressed();
        observeViewModel();

        // Focus etAmount and show soft keyboard
        etAmount.requestFocus();
        etAmount.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingCaptureUri != null) {
            outState.putString(KEY_PENDING_CAPTURE_URI, pendingCaptureUri.toString());
        }
    }

    // targetSdk 35+ bat buoc edge-to-edge, adjustResize khong con tu day layout len tren ban phim
    private void applyWindowInsets() {
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void initViews() {
        btnClose = findViewById(R.id.btn_close);
        progressLoading = findViewById(R.id.progress_loading);
        ivPhoto = findViewById(R.id.iv_photo);
        layoutPhotoEmpty = findViewById(R.id.layout_photo_empty);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnPickPhoto = findViewById(R.id.btn_pick_photo);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        etAmount = findViewById(R.id.et_amount);
        tvAmountSuffix = findViewById(R.id.tv_amount_suffix);
        toggleType = findViewById(R.id.toggle_type);
        btnTypeExpense = findViewById(R.id.btn_type_expense);
        btnTypeIncome = findViewById(R.id.btn_type_income);
        rowWalletCategory = findViewById(R.id.row_wallet_category);
        tvWalletCategory = findViewById(R.id.tv_wallet_category);
        rowDate = findViewById(R.id.row_date);
        tvDate = findViewById(R.id.tv_date);
        etNote = findViewById(R.id.et_note);
        btnSave = findViewById(R.id.btn_save);
        progressSave = findViewById(R.id.progress_save);
    }

    private void setupActivityLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success) && pendingCaptureUri != null) {
                        viewModel.setPhoto(pendingCaptureUri);
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        viewModel.setPhoto(uri);
                    }
                }
        );
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> handleExitAttempt());

        btnTakePhoto.setOnClickListener(v -> launchCamera());
        btnPickPhoto.setOnClickListener(v -> launchGallery());

        btnChangePhoto.setOnClickListener(v -> showChangePhotoDialog());

        etAmount.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                long v = CurrencyUtils.parseDigits(s.toString());
                String formatted = v == 0 ? "" : CurrencyUtils.formatInput(v);
                if (!s.toString().equals(formatted)) {
                    etAmount.setText(formatted);
                    etAmount.setSelection(formatted.length());
                }
                viewModel.setAmount(v);

                isFormatting = false;
            }
        });

        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_type_expense) {
                viewModel.setType(Transaction.TYPE_EXPENSE);
            } else if (checkedId == R.id.btn_type_income) {
                viewModel.setType(Transaction.TYPE_INCOME);
            }
        });

        rowWalletCategory.setOnClickListener(v -> {
            SelectCategoryBottomSheet.newInstance().show(getSupportFragmentManager(), "category_sheet");
        });

        rowDate.setOnClickListener(v -> showDateTimePicker());

        etNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setNote(s.toString());
            }
        });

        btnSave.setOnClickListener(v -> viewModel.save(this));
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExitAttempt();
            }
        });
    }

    private void handleExitAttempt() {
        if (Boolean.TRUE.equals(viewModel.getSaving().getValue())) {
            return;
        }

        boolean hasData = viewModel.getPhotoUri().getValue() != null
                || (viewModel.getAmount().getValue() != null && viewModel.getAmount().getValue() > 0)
                || (viewModel.getNote().getValue() != null && !viewModel.getNote().getValue().trim().isEmpty());

        if (hasData) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.capture_discard_title)
                    .setPositiveButton(R.string.capture_discard_keep, null)
                    .setNegativeButton(R.string.capture_discard_confirm, (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void launchCamera() {
        try {
            pendingCaptureUri = ImageUtils.createCaptureUri(this);
            takePictureLauncher.launch(pendingCaptureUri);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.capture_err_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery() {
        pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    private void showChangePhotoDialog() {
        String[] options = {
                getString(R.string.capture_take_photo),
                getString(R.string.capture_pick_photo),
                getString(R.string.capture_remove_photo)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.capture_change_photo)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else if (which == 1) {
                        launchGallery();
                    } else if (which == 2) {
                        viewModel.setPhoto(null);
                    }
                })
                .show();
    }

    private void showDateTimePicker() {
        final Calendar current = viewModel.getDateTime().getValue() != null
                ? viewModel.getDateTime().getValue()
                : Calendar.getInstance();

        int y = current.get(Calendar.YEAR);
        int m = current.get(Calendar.MONTH);
        int d = current.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(Calendar.YEAR, year);
            selectedCal.set(Calendar.MONTH, month);
            selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            int h = current.get(Calendar.HOUR_OF_DAY);
            int mi = current.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCal.set(Calendar.MINUTE, minute);
                selectedCal.set(Calendar.SECOND, 0);

                Calendar now = Calendar.getInstance();
                if (selectedCal.after(now)) {
                    selectedCal.setTimeInMillis(now.getTimeInMillis());
                }
                viewModel.setDateTime(selectedCal);
            }, h, mi, true);
            timePicker.show();
        }, y, m, d);

        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void observeViewModel() {
        viewModel.getPhotoUri().observe(this, uri -> {
            if (uri != null) {
                ivPhoto.setVisibility(View.VISIBLE);
                layoutPhotoEmpty.setVisibility(View.GONE);
                btnChangePhoto.setVisibility(View.VISIBLE);
                Glide.with(this).load(uri).centerCrop().into(ivPhoto);
            } else {
                ivPhoto.setVisibility(View.GONE);
                layoutPhotoEmpty.setVisibility(View.VISIBLE);
                btnChangePhoto.setVisibility(View.GONE);
            }
        });

        viewModel.getType().observe(this, type -> {
            boolean isExpense = Transaction.TYPE_EXPENSE.equals(type);
            int colorRes = isExpense ? R.color.color_expense : R.color.color_income;
            int color = ContextCompat.getColor(this, colorRes);

            etAmount.setTextColor(color);
            tvAmountSuffix.setTextColor(color);

            int bgCard = ContextCompat.getColor(this, R.color.bg_card);
            int textSec = ContextCompat.getColor(this, R.color.text_secondary);
            int white = ContextCompat.getColor(this, R.color.white);

            if (isExpense) {
                btnTypeExpense.setBackgroundTintList(ColorStateList.valueOf(color));
                btnTypeExpense.setTextColor(white);
                btnTypeExpense.setStrokeColor(ColorStateList.valueOf(color));

                btnTypeIncome.setBackgroundTintList(ColorStateList.valueOf(bgCard));
                btnTypeIncome.setTextColor(textSec);
                btnTypeIncome.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bg_card_secondary)));
            } else {
                btnTypeIncome.setBackgroundTintList(ColorStateList.valueOf(color));
                btnTypeIncome.setTextColor(white);
                btnTypeIncome.setStrokeColor(ColorStateList.valueOf(color));

                btnTypeExpense.setBackgroundTintList(ColorStateList.valueOf(bgCard));
                btnTypeExpense.setTextColor(textSec);
                btnTypeExpense.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bg_card_secondary)));
            }

            updateWalletCategoryRow(viewModel.getSelectedWallet().getValue(), viewModel.getSelectedCategory().getValue());
        });

        viewModel.getSelectedWallet().observe(this, wallet -> {
            updateWalletCategoryRow(wallet, viewModel.getSelectedCategory().getValue());
        });

        viewModel.getSelectedCategory().observe(this, category -> {
            updateWalletCategoryRow(viewModel.getSelectedWallet().getValue(), category);
        });

        viewModel.getDateTime().observe(this, cal -> {
            if (cal != null) {
                tvDate.setText("📅 " + DateUtils.toDisplay(DateUtils.toServer(cal)));
            }
        });

        viewModel.getLoadingOptions().observe(this, loading -> {
            progressLoading.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        viewModel.getSaving().observe(this, saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);
            progressSave.setVisibility(isSaving ? View.VISIBLE : View.GONE);
            btnSave.setText(isSaving ? "" : getString(R.string.capture_save));
            btnSave.setEnabled(!isSaving);
            btnClose.setEnabled(!isSaving);
            etAmount.setEnabled(!isSaving);
            etNote.setEnabled(!isSaving);
            btnTakePhoto.setEnabled(!isSaving);
            btnPickPhoto.setEnabled(!isSaving);
            btnChangePhoto.setEnabled(!isSaving);
            rowWalletCategory.setEnabled(!isSaving);
            rowDate.setEnabled(!isSaving);
            toggleType.setEnabled(!isSaving);
        });

        viewModel.getEvent().observe(this, singleEvent -> {
            if (singleEvent == null) return;
            CaptureViewModel.UiEvent uiEvent = singleEvent.getContentIfNotHandled();
            if (uiEvent == null) return;

            if (uiEvent.getKind() == CaptureViewModel.UiEvent.KIND_ERROR) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), uiEvent.getMessage(), Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.capture_retry, v -> viewModel.loadOptions());
                snackbar.show();
            } else if (uiEvent.getKind() == CaptureViewModel.UiEvent.KIND_OPEN_CATEGORY_SHEET) {
                Snackbar.make(findViewById(android.R.id.content), uiEvent.getMessage(), Snackbar.LENGTH_SHORT).show();
                SelectCategoryBottomSheet.newInstance().show(getSupportFragmentManager(), "category_sheet");
            } else if (uiEvent.getKind() == CaptureViewModel.UiEvent.KIND_SAVED) {
                Toast.makeText(this, uiEvent.getMessage(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void updateWalletCategoryRow(WalletOption wallet, Category category) {
        String walletText = wallet != null ? (getWalletIcon(wallet.getType()) + " " + wallet.getName()) : "";

        if (category != null) {
            String full = walletText + " · " + category.getIcon() + " " + category.getName();
            tvWalletCategory.setText(full);
        } else {
            String chooseCat = getString(R.string.capture_choose_category);
            String full = (walletText.isEmpty() ? "" : walletText + " · ") + chooseCat;
            SpannableString ss = new SpannableString(full);
            int start = full.indexOf(chooseCat);
            if (start >= 0) {
                ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.capture_text_hint)),
                        start, start + chooseCat.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvWalletCategory.setText(ss);
        }
    }

    private String getWalletIcon(String type) {
        if (type == null) return "👛";
        switch (type.toLowerCase()) {
            case "cash": return "💵";
            case "bank": return "🏦";
            case "saving": return "🐷";
            default: return "👛";
        }
    }
}