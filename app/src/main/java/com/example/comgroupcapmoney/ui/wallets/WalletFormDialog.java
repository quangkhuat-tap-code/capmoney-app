package com.example.comgroupcapmoney.ui.wallets;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.data.repository.WalletRepository;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class WalletFormDialog extends BottomSheetDialogFragment {

    static final String RESULT_KEY = "wallet_changed";
    private static final String ARG_WALLET = "wallet";

    private Wallet wallet;
    private TextInputLayout layoutName;
    private TextInputEditText etName;
    private MaterialButtonToggleGroup toggleType;
    private EditText etBalance;
    private MaterialButton btnSave;
    private boolean sending;

    // null = Them vi moi
    public static WalletFormDialog newInstance(@Nullable Wallet walletOrNull) {
        WalletFormDialog dialog = new WalletFormDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WALLET, walletOrNull);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wallet = getArguments() != null ? (Wallet) getArguments().getSerializable(ARG_WALLET) : null;

        TextView tvTitle = view.findViewById(R.id.wallet_form_tv_title);
        layoutName = view.findViewById(R.id.wallet_form_layout_name);
        etName = view.findViewById(R.id.wallet_form_et_name);
        toggleType = view.findViewById(R.id.wallet_form_toggle_type);
        etBalance = view.findViewById(R.id.wallet_form_et_balance);
        btnSave = view.findViewById(R.id.wallet_form_btn_save);

        tvTitle.setText(wallet == null ? R.string.wallet_form_add_title : R.string.wallet_form_edit_title);
        etBalance.addTextChangedListener(new AmountFormatter(etBalance));
        // Go lai ten thi bo loi cu (vd "Ten vi da ton tai")
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                layoutName.setError(null);
            }
        });

        if (savedInstanceState == null) {
            if (wallet == null) {
                toggleType.check(R.id.wallet_form_btn_cash);
            } else {
                etName.setText(wallet.getName());
                toggleType.check(typeToButtonId(wallet.getType()));
                etBalance.setText(CurrencyUtils.formatInput(Math.round(wallet.getBalance())));
            }
        }

        btnSave.setOnClickListener(v -> submit());
    }

    private void submit() {
        if (sending) return;
        layoutName.setError(null);
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.wallet_error_name));
            return;
        }
        String type = buttonIdToType(toggleType.getCheckedButtonId());
        long balance = CurrencyUtils.parseDigits(etBalance.getText().toString());

        if (wallet == null) {
            send(() -> new WalletRepository(requireContext()).createWallet(name, type, balance, callback(R.string.wallet_created)));
            return;
        }

        // Sua: chi gui field da doi
        String newName = name.equals(wallet.getName()) ? null : name;
        String newType = type.equals(wallet.getType()) ? null : type;
        long oldBalance = Math.round(wallet.getBalance());
        Long newBalance = balance == oldBalance ? null : balance;
        if (newName == null && newType == null && newBalance == null) {
            dismiss();
            return;
        }

        Runnable doUpdate = () -> send(() -> new WalletRepository(requireContext())
                .updateWallet(wallet.getId(), newName, newType, newBalance, callback(R.string.wallet_updated)));
        if (newBalance == null) {
            doUpdate.run();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.wallet_confirm_balance_title)
                .setMessage(getString(R.string.wallet_confirm_balance_msg,
                        CurrencyUtils.format(oldBalance), CurrencyUtils.format(newBalance)))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> doUpdate.run())
                .show();
    }

    private void send(Runnable request) {
        sending = true;
        btnSave.setEnabled(false);
        request.run();
    }

    private RepoCallback<Wallet> callback(int successMsgRes) {
        return new RepoCallback<Wallet>() {
            @Override
            public void onSuccess(Wallet data, String message) {
                sending = false;
                if (!isAdded()) return;
                Toast.makeText(requireContext(), successMsgRes, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().setFragmentResult(RESULT_KEY, new Bundle());
                dismiss();
            }

            @Override
            public void onError(String message, boolean unauthorized) {
                sending = false;
                if (!isAdded()) return;
                if (unauthorized) {
                    SessionManager.logout(requireActivity(), true);
                    return;
                }
                btnSave.setEnabled(true);
                layoutName.setError(message);
            }
        };
    }

    private static int typeToButtonId(String type) {
        if ("bank".equals(type)) return R.id.wallet_form_btn_bank;
        if ("saving".equals(type)) return R.id.wallet_form_btn_saving;
        return R.id.wallet_form_btn_cash;
    }

    private static String buttonIdToType(int id) {
        if (id == R.id.wallet_form_btn_bank) return "bank";
        if (id == R.id.wallet_form_btn_saving) return "saving";
        return "cash";
    }

    // Tu chen dau cham khi go so tien: 1500000 -> 1.500.000
    static class AmountFormatter implements TextWatcher {
        private final EditText editText;

        AmountFormatter(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            long v = CurrencyUtils.parseDigits(s.toString());
            String formatted = v == 0 ? "" : CurrencyUtils.formatInput(v);
            if (formatted.equals(s.toString())) return;
            editText.removeTextChangedListener(this);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }
}
