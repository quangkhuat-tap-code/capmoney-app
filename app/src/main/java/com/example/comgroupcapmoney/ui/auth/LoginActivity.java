package com.example.comgroupcapmoney.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.main.MainActivity;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progress;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Da co token thi vao thang man chinh
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            openMain();
            return;
        }
        setContentView(R.layout.activity_login);
        applyWindowInsets(this);

        layoutEmail = findViewById(R.id.auth_layout_email);
        layoutPassword = findViewById(R.id.auth_layout_password);
        etEmail = findViewById(R.id.auth_et_email);
        etPassword = findViewById(R.id.auth_et_password);
        btnLogin = findViewById(R.id.auth_btn_login);
        progress = findViewById(R.id.auth_progress);
        TextView tvGoRegister = findViewById(R.id.auth_tv_go_register);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnLogin.setOnClickListener(v -> submit());
        tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        viewModel.getLoading().observe(this, this::setLoading);
        viewModel.getSuccess().observe(this, result -> {
            if (result != null) openMain();
        });
        viewModel.getError().observe(this, event -> {
            String msg = event != null ? event.getContentIfNotHandled() : null;
            if (msg != null) {
                Snackbar.make(btnLogin, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void submit() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        String email = text(etEmail).trim();
        String password = text(etPassword);

        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.auth_error_email));
            ok = false;
        }
        if (password.length() < 6 || password.length() > 72) {
            layoutPassword.setError(getString(R.string.auth_error_password));
            ok = false;
        }
        if (!ok) return;

        viewModel.login(email, password);
    }

    private void setLoading(Boolean loading) {
        boolean busy = Boolean.TRUE.equals(loading);
        btnLogin.setEnabled(!busy);
        btnLogin.setText(busy ? "" : getString(R.string.auth_btn_login));
        etEmail.setEnabled(!busy);
        etPassword.setEnabled(!busy);
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    private void openMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    static String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }

    // targetSdk 35+ bat buoc edge-to-edge: chua cho status bar, thanh dieu huong va ban phim
    static void applyWindowInsets(AppCompatActivity activity) {
        View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
