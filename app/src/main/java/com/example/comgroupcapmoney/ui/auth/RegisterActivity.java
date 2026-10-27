package com.example.comgroupcapmoney.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private static final String MSG_EMAIL_USED = "Email đã được sử dụng";

    private TextInputLayout layoutFullName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirm;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirm;
    private MaterialButton btnRegister;
    private ProgressBar progress;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        LoginActivity.applyWindowInsets(this);

        layoutFullName = findViewById(R.id.auth_layout_full_name);
        layoutEmail = findViewById(R.id.auth_layout_email);
        layoutPassword = findViewById(R.id.auth_layout_password);
        layoutConfirm = findViewById(R.id.auth_layout_confirm);
        etFullName = findViewById(R.id.auth_et_full_name);
        etEmail = findViewById(R.id.auth_et_email);
        etPassword = findViewById(R.id.auth_et_password);
        etConfirm = findViewById(R.id.auth_et_confirm);
        btnRegister = findViewById(R.id.auth_btn_register);
        progress = findViewById(R.id.auth_progress);
        TextView tvGoLogin = findViewById(R.id.auth_tv_go_login);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnRegister.setOnClickListener(v -> submit());
        tvGoLogin.setOnClickListener(v -> finish());

        viewModel.getLoading().observe(this, this::setLoading);
        viewModel.getSuccess().observe(this, result -> {
            if (result != null) {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
        viewModel.getError().observe(this, event -> {
            String msg = event != null ? event.getContentIfNotHandled() : null;
            if (msg == null) return;
            if (MSG_EMAIL_USED.equals(msg)) {
                layoutEmail.setError(msg);
            } else {
                Snackbar.make(btnRegister, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void submit() {
        layoutFullName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirm.setError(null);
        String fullName = LoginActivity.text(etFullName).trim();
        String email = LoginActivity.text(etEmail).trim();
        String password = LoginActivity.text(etPassword);
        String confirm = LoginActivity.text(etConfirm);

        boolean ok = true;
        if (fullName.isEmpty() || fullName.length() > 100) {
            layoutFullName.setError(getString(R.string.auth_error_full_name));
            ok = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.auth_error_email));
            ok = false;
        }
        if (password.length() < 6 || password.length() > 72) {
            layoutPassword.setError(getString(R.string.auth_error_password));
            ok = false;
        }
        if (!confirm.equals(password)) {
            layoutConfirm.setError(getString(R.string.auth_error_confirm));
            ok = false;
        }
        if (!ok) return;

        viewModel.register(fullName, email, password);
    }

    private void setLoading(Boolean loading) {
        boolean busy = Boolean.TRUE.equals(loading);
        btnRegister.setEnabled(!busy);
        btnRegister.setText(busy ? "" : getString(R.string.auth_btn_register));
        etFullName.setEnabled(!busy);
        etEmail.setEnabled(!busy);
        etPassword.setEnabled(!busy);
        etConfirm.setEnabled(!busy);
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
    }
}
