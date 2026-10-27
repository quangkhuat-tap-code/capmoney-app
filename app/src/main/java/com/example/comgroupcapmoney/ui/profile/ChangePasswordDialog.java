package com.example.comgroupcapmoney.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.repository.AuthRepository;
import com.example.comgroupcapmoney.data.repository.RepoCallback;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

// Doi mat khau tu Tab Ca nhan (4B-2). Doi xong van dang nhap.
public class ChangePasswordDialog extends BottomSheetDialogFragment {

    private static final String SERVER_WRONG_CURRENT = "Mật khẩu hiện tại không đúng";

    private TextInputLayout layoutCurrent;
    private TextInputLayout layoutNew;
    private TextInputLayout layoutConfirm;
    private EditText etCurrent;
    private EditText etNew;
    private EditText etConfirm;
    private MaterialButton btnSave;
    private View btnCancel;
    private View progress;
    private boolean sending;

    public static ChangePasswordDialog newInstance() {
        return new ChangePasswordDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutCurrent = view.findViewById(R.id.password_layout_current);
        layoutNew = view.findViewById(R.id.password_layout_new);
        layoutConfirm = view.findViewById(R.id.password_layout_confirm);
        etCurrent = view.findViewById(R.id.password_et_current);
        etNew = view.findViewById(R.id.password_et_new);
        etConfirm = view.findViewById(R.id.password_et_confirm);
        btnSave = view.findViewById(R.id.password_btn_save);
        btnCancel = view.findViewById(R.id.password_btn_cancel);
        progress = view.findViewById(R.id.password_progress);

        clearErrorOnTyping(etCurrent, layoutCurrent);
        clearErrorOnTyping(etNew, layoutNew);
        clearErrorOnTyping(etConfirm, layoutConfirm);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> submit());
        etConfirm.setOnEditorActionListener((v, actionId, event) -> {
            submit();
            return true;
        });
    }

    private void clearErrorOnTyping(EditText et, TextInputLayout layout) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                layout.setError(null);
            }
        });
    }

    // Kiem tra tai may truoc, dung thi moi goi API
    private void submit() {
        if (sending) return;
        String current = etCurrent.getText() != null ? etCurrent.getText().toString() : "";
        String next = etNew.getText() != null ? etNew.getText().toString() : "";
        String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

        boolean ok = true;
        if (current.isEmpty()) {
            layoutCurrent.setError(getString(R.string.password_err_current_empty));
            ok = false;
        }
        if (next.isEmpty()) {
            layoutNew.setError(getString(R.string.password_err_new_empty));
            ok = false;
        } else if (next.length() < 6 || next.length() > 72) {
            layoutNew.setError(getString(R.string.password_err_length));
            ok = false;
        }
        if (confirm.isEmpty()) {
            layoutConfirm.setError(getString(R.string.password_err_confirm_empty));
            ok = false;
        } else if (!confirm.equals(next)) {
            layoutConfirm.setError(getString(R.string.password_err_mismatch));
            ok = false;
        }
        if (!ok) return;

        setSending(true);
        new AuthRepository(requireContext()).changePassword(current, next, new RepoCallback<Object>() {
            @Override
            public void onSuccess(Object data, String message) {
                if (!isAdded()) return;
                setSending(false);
                Toast.makeText(requireContext(), R.string.password_changed, Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(String message, boolean isUnauthorized) {
                if (!isAdded()) return;
                setSending(false);
                if (isUnauthorized) {
                    dismiss();
                    SessionManager.logout(requireActivity(), true);
                    return;
                }
                // Sai mat khau hien tai thi bao duoi o hien tai, loi khac bao duoi o cuoi
                if (SERVER_WRONG_CURRENT.equals(message)) {
                    layoutCurrent.setError(message);
                } else {
                    layoutConfirm.setError(message);
                }
            }
        });
    }

    private void setSending(boolean value) {
        sending = value;
        setCancelable(!value);
        btnSave.setEnabled(!value);
        btnSave.setText(value ? "" : getString(R.string.common_save));
        progress.setVisibility(value ? View.VISIBLE : View.GONE);
        btnCancel.setEnabled(!value);
        etCurrent.setEnabled(!value);
        etNew.setEnabled(!value);
        etConfirm.setEnabled(!value);
    }
}
