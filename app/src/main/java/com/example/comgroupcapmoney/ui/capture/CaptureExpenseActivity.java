package com.example.comgroupcapmoney.ui.capture;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CaptureExpenseActivity extends AppCompatActivity {

    private static final String KEY_PENDING_CAPTURE_URI = "pending_capture_uri";
    public static final String EXTRA_EDIT_TX = "edit_tx";

    public static Intent editIntent(Context context, Transaction tx) {
        Intent intent = new Intent(context, CaptureExpenseActivity.class);
        intent.putExtra(EXTRA_EDIT_TX, tx);
        return intent;
    }

    private CaptureViewModel viewModel;
    private boolean editing;
    private Uri pendingCaptureUri;

    // State Layouts
    private View layoutPhotoEmpty;
    private View layoutPhotoInput;
    private View layoutCalculatorNumpad;
    private View layoutConfirmedActions;

    // Viewfinder views
    private View btnTakePhoto;
    private View btnPickPhoto;
    private View btnSkipPhoto;

    // Amount & Overlays
    private ImageView ivPhoto;
    private View btnChangePhoto;
    private TextView tvPhotoEmoji;
    private Chip receiptChip;
    private TextView tvAmountPrefix;
    private TextView tvAmountDisplay;
    private TextView tvAmountSuffix;
    private EditText etNote;
    private EditText etAmountHidden;

    // Quick selection pills
    private TextView btnSelectCategoryPill;
    private TextView btnSelectWalletPill;
    private FrameLayout btnToggleChi;
    private FrameLayout btnToggleThu;
    private TextView tvDate;
    private CircularProgressIndicator progressSave;
    private LinearProgressIndicator progressLoading;

    // Calculator buffer
    private long currentAmount = 0;

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
        SessionManager.observeExpired(this, this);

        initViews();
        setupCalculatorNumpad();
        setupEditMode(savedInstanceState);
        setupActivityLaunchers();
        setupListeners();
        setupBackPressed();
        observeViewModel();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingCaptureUri != null) {
            outState.putString(KEY_PENDING_CAPTURE_URI, pendingCaptureUri.toString());
        }
    }

    private void applyWindowInsets() {
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void initViews() {
        layoutPhotoEmpty = findViewById(R.id.layout_photo_empty);
        layoutPhotoInput = findViewById(R.id.layout_photo_input);
        layoutCalculatorNumpad = findViewById(R.id.layout_calculator_numpad);
        layoutConfirmedActions = findViewById(R.id.layout_confirmed_actions);

        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnPickPhoto = findViewById(R.id.btn_pick_photo);
        btnSkipPhoto = findViewById(R.id.btn_skip_photo);

        ivPhoto = findViewById(R.id.iv_photo);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        tvPhotoEmoji = findViewById(R.id.edit_tv_photo_emoji);
        receiptChip = findViewById(R.id.receipt_chip);

        tvAmountPrefix = findViewById(R.id.tv_amount_prefix);
        tvAmountDisplay = findViewById(R.id.tv_amount_display);
        tvAmountSuffix = findViewById(R.id.tv_amount_suffix);
        etNote = findViewById(R.id.et_note);
        etAmountHidden = findViewById(R.id.et_amount);

        btnSelectCategoryPill = findViewById(R.id.btn_select_category_pill);
        btnSelectWalletPill = findViewById(R.id.btn_select_wallet_pill);
        btnToggleChi = findViewById(R.id.btn_toggle_chi);
        btnToggleThu = findViewById(R.id.btn_toggle_thu);
        tvDate = findViewById(R.id.tv_date);
        progressSave = findViewById(R.id.progress_save);
        progressLoading = findViewById(R.id.progress_loading);
    }

    private void setupCalculatorNumpad() {
        int[] numIds = {
                R.id.btn_calc_0, R.id.btn_calc_1, R.id.btn_calc_2, R.id.btn_calc_3,
                R.id.btn_calc_4, R.id.btn_calc_5, R.id.btn_calc_6, R.id.btn_calc_7,
                R.id.btn_calc_8, R.id.btn_calc_9
        };

        for (int i = 0; i < numIds.length; i++) {
            final int digit = i;
            View v = findViewById(numIds[i]);
            if (v != null) {
                v.setOnClickListener(btn -> appendDigit(digit));
            }
        }

        View btn000 = findViewById(R.id.btn_calc_000);
        if (btn000 != null) {
            btn000.setOnClickListener(v -> appendTripleZero());
        }

        View btnC = findViewById(R.id.btn_calc_c);
        if (btnC != null) {
            btnC.setOnClickListener(v -> {
                currentAmount = 0;
                updateAmountUI();
            });
        }

        View btnBackspace = findViewById(R.id.btn_calc_backspace);
        if (btnBackspace != null) {
            btnBackspace.setOnClickListener(v -> {
                currentAmount = currentAmount / 10;
                updateAmountUI();
            });
        }

        // Done button: Hide numpad, show confirmed preview actions (Image 7)
        View btnDone = findViewById(R.id.btn_calc_done);
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                if (currentAmount <= 0) {
                    Toast.makeText(this, "Vui lòng nhập số tiền lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                layoutCalculatorNumpad.setVisibility(View.GONE);
                layoutConfirmedActions.setVisibility(View.VISIBLE);
            });
        }

        // Tap amount overlay or (+) Add button to reopen calculator
        View amountOverlay = findViewById(R.id.layout_amount_pill);
        if (amountOverlay != null) {
            amountOverlay.setOnClickListener(v -> {
                layoutCalculatorNumpad.setVisibility(View.VISIBLE);
                layoutConfirmedActions.setVisibility(View.GONE);
            });
        }

        View btnAddMore = findViewById(R.id.btn_action_add_more);
        if (btnAddMore != null) {
            btnAddMore.setOnClickListener(v -> {
                layoutCalculatorNumpad.setVisibility(View.VISIBLE);
                layoutConfirmedActions.setVisibility(View.GONE);
            });
        }

        // Save checkmark button (Large coral button in Image 7)
        View btnSaveCheck = findViewById(R.id.btn_action_save_check);
        if (btnSaveCheck != null) {
            btnSaveCheck.setOnClickListener(v -> viewModel.save(this));
        }

        // Share button
        View btnShare = findViewById(R.id.btn_action_share);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                String shareText = "CapMoney: Giao dịch " + CurrencyUtils.format(currentAmount) +
                        (etNote.getText() != null && !etNote.getText().toString().isEmpty() ? " (" + etNote.getText() + ")" : "");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Chia sẻ chi tiêu"));
            });
        }
    }

    private void appendDigit(int digit) {
        if (currentAmount > 100_000_000_000L) return; // Prevent overflow
        currentAmount = (currentAmount * 10) + digit;
        updateAmountUI();
    }

    private void appendTripleZero() {
        if (currentAmount == 0) return;
        if (currentAmount > 100_000_000L) return;
        currentAmount = currentAmount * 1000;
        updateAmountUI();
    }

    private void updateAmountUI() {
        if (currentAmount == 0) {
            tvAmountDisplay.setText("0");
        } else {
            tvAmountDisplay.setText(CurrencyUtils.formatInput(currentAmount));
        }
        viewModel.setAmount(currentAmount);
        if (etAmountHidden != null) {
            etAmountHidden.setText(String.valueOf(currentAmount));
        }
    }

    private void setupEditMode(Bundle savedInstanceState) {
        Transaction tx = (Transaction) getIntent().getSerializableExtra(EXTRA_EDIT_TX);
        if (tx == null) {
            return;
        }
        editing = true;
        boolean firstInit = viewModel.initEdit(tx);

        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(R.string.edit_title);
        }

        layoutPhotoEmpty.setVisibility(View.GONE);
        layoutPhotoInput.setVisibility(View.VISIBLE);

        String imageUrl = tx.getFullImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).centerCrop().into(ivPhoto);
        } else {
            tvPhotoEmoji.setVisibility(View.VISIBLE);
            tvPhotoEmoji.setText(tx.getCategoryIcon() != null ? tx.getCategoryIcon() : "💰");
        }

        if (firstInit && savedInstanceState == null) {
            currentAmount = Math.round(tx.getAmount());
            updateAmountUI();
            etNote.setText(tx.getNote() != null ? tx.getNote() : "");
        }
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
        View btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> handleExitAttempt());
        }

        btnTakePhoto.setOnClickListener(v -> launchCamera());
        btnPickPhoto.setOnClickListener(v -> launchGallery());
        btnSkipPhoto.setOnClickListener(v -> {
            layoutPhotoEmpty.setVisibility(View.GONE);
            layoutPhotoInput.setVisibility(View.VISIBLE);
        });

        btnChangePhoto.setOnClickListener(v -> showChangePhotoDialog());

        // Category pill click -> open SelectCategoryBottomSheet
        btnSelectCategoryPill.setOnClickListener(v -> {
            SelectCategoryBottomSheet.newInstance().show(getSupportFragmentManager(), "category_sheet");
        });

        // Wallet pill click -> cycle wallet selection or open sheet
        btnSelectWalletPill.setOnClickListener(v -> {
            SelectCategoryBottomSheet.newInstance().show(getSupportFragmentManager(), "category_sheet");
        });

        // Chi / Thu Toggle
        btnToggleChi.setOnClickListener(v -> viewModel.setType(Transaction.TYPE_EXPENSE));
        btnToggleThu.setOnClickListener(v -> viewModel.setType(Transaction.TYPE_INCOME));

        // Date Picker
        tvDate.setOnClickListener(v -> showDateTimePicker());

        // Note watcher
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

        boolean hasData = editing
                ? viewModel.hasEditChanges()
                : viewModel.getPhotoUri().getValue() != null
                || currentAmount > 0
                || (etNote.getText() != null && !etNote.getText().toString().trim().isEmpty());

        if (hasData) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(editing ? R.string.edit_discard_title : R.string.capture_discard_title)
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
                        layoutPhotoInput.setVisibility(View.GONE);
                        layoutPhotoEmpty.setVisibility(View.VISIBLE);
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
            if (editing) return;
            if (uri != null) {
                layoutPhotoEmpty.setVisibility(View.GONE);
                layoutPhotoInput.setVisibility(View.VISIBLE);
                ivPhoto.setVisibility(View.VISIBLE);
                Glide.with(this).load(uri).centerCrop().into(ivPhoto);
            } else {
                ivPhoto.setVisibility(View.GONE);
            }
        });

        viewModel.getReceiptChip().observe(this, chip -> {
            if (chip == null) {
                receiptChip.setVisibility(View.GONE);
                return;
            }
            String money = CurrencyUtils.format(chip.amount);
            if (chip.mode == CaptureViewModel.ReceiptChip.FILLED) {
                currentAmount = chip.amount;
                updateAmountUI();
                receiptChip.setText(getString(R.string.receipt_filled, money));
                receiptChip.setOnClickListener(v -> {
                    viewModel.dismissReceiptChip();
                    currentAmount = 0;
                    updateAmountUI();
                });
            } else {
                receiptChip.setText(getString(R.string.receipt_suggest, money));
                receiptChip.setOnClickListener(v -> {
                    viewModel.dismissReceiptChip();
                    currentAmount = chip.amount;
                    updateAmountUI();
                });
            }
            receiptChip.setVisibility(View.VISIBLE);
        });

        viewModel.getType().observe(this, type -> {
            boolean isExpense = Transaction.TYPE_EXPENSE.equals(type);
            tvAmountPrefix.setText(isExpense ? "-  " : "+  ");
            int prefixColor = ContextCompat.getColor(this, isExpense ? R.color.color_expense : R.color.color_income);
            tvAmountPrefix.setTextColor(prefixColor);

            if (isExpense) {
                btnToggleChi.setBackgroundResource(R.drawable.bg_circle_icon_expense);
                btnToggleThu.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
                ((ImageView) btnToggleChi.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.color_expense));
                ((ImageView) btnToggleThu.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.text_tertiary));
            } else {
                btnToggleThu.setBackgroundResource(R.drawable.bg_circle_icon_income);
                btnToggleChi.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
                ((ImageView) btnToggleThu.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.color_income));
                ((ImageView) btnToggleChi.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.text_tertiary));
            }
        });

        viewModel.getSelectedWallet().observe(this, wallet -> {
            if (wallet != null) {
                btnSelectWalletPill.setText("🧾  " + wallet.getName() + " ⌄");
            }
        });

        viewModel.getSelectedCategory().observe(this, category -> {
            if (category != null) {
                String icon = category.getIcon() != null ? category.getIcon() : "📖";
                btnSelectCategoryPill.setText(icon + "  " + category.getName() + " ⌄");
            }
        });

        viewModel.getDateTime().observe(this, cal -> {
            if (cal != null) {
                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    tvDate.setText("📅  Hôm nay ⌄");
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvDate.setText("📅  " + sdf.format(cal.getTime()) + " ⌄");
                }
            }
        });

        viewModel.getLoadingOptions().observe(this, loading -> {
            if (progressLoading != null) {
                progressLoading.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getSaving().observe(this, saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);
            if (progressSave != null) {
                progressSave.setVisibility(isSaving ? View.VISIBLE : View.GONE);
            }
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
}
