package com.example.comgroupcapmoney.ui.wallets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.data.repository.WalletRepository;
import com.example.comgroupcapmoney.model.TransferResult;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

public class TransferMoneyDialog extends BottomSheetDialogFragment {

    private static final String ARG_WALLETS = "wallets";
    private static final String KEY_FROM = "from_index";
    private static final String KEY_TO = "to_index";

    private final List<Wallet> wallets = new ArrayList<>();
    private int fromIndex;
    private int toIndex;

    private MaterialAutoCompleteTextView ddFrom;
    private MaterialAutoCompleteTextView ddTo;
    private TextView tvAvailable;
    private TextView tvError;
    private EditText etAmount;
    private EditText etNote;
    private MaterialButton btnSubmit;
    private boolean sending;

    public static TransferMoneyDialog newInstance(ArrayList<Wallet> wallets) {
        TransferMoneyDialog dialog = new TransferMoneyDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WALLETS, wallets);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transfer_money, container, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null && getArguments().getSerializable(ARG_WALLETS) != null) {
            wallets.addAll((ArrayList<Wallet>) getArguments().getSerializable(ARG_WALLETS));
        }

        ddFrom = view.findViewById(R.id.transfer_dd_from);
        ddTo = view.findViewById(R.id.transfer_dd_to);
        tvAvailable = view.findViewById(R.id.transfer_tv_available);
        tvError = view.findViewById(R.id.transfer_tv_error);
        etAmount = view.findViewById(R.id.transfer_et_amount);
        etNote = view.findViewById(R.id.transfer_et_note);
        btnSubmit = view.findViewById(R.id.transfer_btn_submit);

        List<String> labels = new ArrayList<>();
        for (Wallet w : wallets) {
            labels.add(w.getTypeEmoji() + " " + w.getName() + " · " + CurrencyUtils.format(w.getBalance()));
        }
        ddFrom.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, labels));
        ddTo.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, labels));

        if (savedInstanceState != null) {
            fromIndex = savedInstanceState.getInt(KEY_FROM);
            toIndex = savedInstanceState.getInt(KEY_TO);
        } else {
            // Mac dinh: Tu = vi mac dinh, Den = vi dau tien khac vi nguon
            fromIndex = 0;
            for (int i = 0; i < wallets.size(); i++) {
                if (wallets.get(i).isDefault()) {
                    fromIndex = i;
                    break;
                }
            }
            toIndex = fromIndex == 0 && wallets.size() > 1 ? 1 : 0;
        }
        showSelection();

        ddFrom.setOnItemClickListener((parent, v, position, id) -> {
            fromIndex = position;
            showSelection();
        });
        ddTo.setOnItemClickListener((parent, v, position, id) -> {
            toIndex = position;
            showSelection();
        });
        view.findViewById(R.id.transfer_btn_swap).setOnClickListener(v -> {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
            showSelection();
        });
        etAmount.addTextChangedListener(new WalletFormDialog.AmountFormatter(etAmount));
        btnSubmit.setOnClickListener(v -> submit());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FROM, fromIndex);
        outState.putInt(KEY_TO, toIndex);
    }

    private void showSelection() {
        if (wallets.isEmpty()) return;
        tvError.setVisibility(View.GONE);
        ddFrom.setText(ddFrom.getAdapter().getItem(fromIndex).toString(), false);
        ddTo.setText(ddTo.getAdapter().getItem(toIndex).toString(), false);
        tvAvailable.setText(getString(R.string.transfer_available,
                CurrencyUtils.format(wallets.get(fromIndex).getBalance())));
    }

    private void submit() {
        if (sending || wallets.isEmpty()) return;
        tvError.setVisibility(View.GONE);
        long amount = CurrencyUtils.parseDigits(etAmount.getText().toString());
        Wallet from = wallets.get(fromIndex);
        Wallet to = wallets.get(toIndex);

        // Kiem tra tai may truoc khi goi API
        if (from.getId() == to.getId()) {
            showError(getString(R.string.transfer_error_same));
            return;
        }
        if (amount <= 0) {
            showError(getString(R.string.transfer_error_amount));
            return;
        }
        if (amount > from.getBalance()) {
            showError(getString(R.string.transfer_error_balance));
            return;
        }

        sending = true;
        btnSubmit.setEnabled(false);
        String note = etNote.getText().toString();
        new WalletRepository(requireContext()).transfer(from.getId(), to.getId(), amount, note,
                new RepoCallback<TransferResult>() {
                    @Override
                    public void onSuccess(TransferResult data, String message) {
                        sending = false;
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                getString(R.string.transfer_success, CurrencyUtils.format(amount)),
                                Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().setFragmentResult(WalletFormDialog.RESULT_KEY, new Bundle());
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
                        btnSubmit.setEnabled(true);
                        showError(message);
                    }
                });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
