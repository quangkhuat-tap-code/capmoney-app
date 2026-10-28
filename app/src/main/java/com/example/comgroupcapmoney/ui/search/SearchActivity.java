package com.example.comgroupcapmoney.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.ui.home.HomeViewModel;
import com.example.comgroupcapmoney.ui.home.TransactionDetailDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

// Tim giao dich theo ghi chu hoac ten danh muc o moi thang (4A-2)
public class SearchActivity extends AppCompatActivity {

    private static final long DEBOUNCE_MS = 400;

    private SearchViewModel viewModel;
    private EditText etQuery;
    private View btnClear;
    private LinearProgressIndicator progress;
    private CircularProgressIndicator progressMore;
    private TextView tvState;
    private MaterialButton btnRetry;
    private RecyclerView rvResults;
    private SearchAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = () -> viewModel.setQuery(etQuery.getText().toString());
    private boolean firstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        applyWindowInsets();

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        SessionManager.observeExpired(this, this);

        etQuery = findViewById(R.id.search_et_query);
        btnClear = findViewById(R.id.search_btn_clear);
        progress = findViewById(R.id.search_progress);
        progressMore = findViewById(R.id.search_progress_more);
        tvState = findViewById(R.id.search_tv_state);
        btnRetry = findViewById(R.id.search_btn_retry);
        rvResults = findViewById(R.id.search_rv_results);

        findViewById(R.id.search_btn_back).setOnClickListener(v -> finish());
        btnClear.setOnClickListener(v -> etQuery.setText(""));
        btnRetry.setOnClickListener(v -> viewModel.retry());

        adapter = new SearchAdapter(tx -> TransactionDetailDialog.newInstance(tx)
                .show(getSupportFragmentManager(), "search_detail"));
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvResults.setLayoutManager(lm);
        rvResults.setAdapter(adapter);
        rvResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                int last = lm.findLastVisibleItemPosition();
                if (last >= adapter.getItemCount() - 5) {
                    viewModel.loadMore();
                }
            }
        });

        etQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, DEBOUNCE_MS);
            }
        });
        etQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handler.removeCallbacks(searchRunnable);
                viewModel.setQuery(etQuery.getText().toString());
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Hop chi tiet dung HomeViewModel cua Activity chua no: xoa xong thi bao va tim lai
        new ViewModelProvider(this).get(HomeViewModel.class).getEvent().observe(this, event -> {
            if (event == null) return;
            String msg = event.getContentIfNotHandled();
            if (msg == null) return;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            viewModel.refresh();
        });

        observeViewModel();

        if (savedInstanceState == null) {
            etQuery.requestFocus();
            etQuery.post(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etQuery, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Quay lai tu man sua giao dich: tim lai de thay so moi
        if (!firstResume) {
            viewModel.refresh();
        }
        firstResume = false;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(searchRunnable);
        super.onDestroy();
    }

    private void observeViewModel() {
        viewModel.getItems().observe(this, list -> adapter.submitList(list));

        viewModel.getState().observe(this, state -> {
            int s = state != null ? state : SearchViewModel.STATE_IDLE;
            progress.setVisibility(s == SearchViewModel.STATE_LOADING ? View.VISIBLE : View.GONE);
            rvResults.setVisibility(s == SearchViewModel.STATE_RESULTS ? View.VISIBLE : View.GONE);
            btnRetry.setVisibility(s == SearchViewModel.STATE_ERROR ? View.VISIBLE : View.GONE);

            if (s == SearchViewModel.STATE_IDLE) {
                tvState.setVisibility(View.VISIBLE);
                tvState.setText(R.string.search_idle);
            } else if (s == SearchViewModel.STATE_EMPTY) {
                tvState.setVisibility(View.VISIBLE);
                tvState.setText(R.string.search_empty);
            } else if (s == SearchViewModel.STATE_ERROR) {
                tvState.setVisibility(View.VISIBLE);
                tvState.setText(viewModel.getError().getValue());
            } else {
                tvState.setVisibility(View.GONE);
            }
        });

        viewModel.getLoadingMore().observe(this, loading ->
                progressMore.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);
        }
    }

    // targetSdk 35+ bat buoc edge-to-edge (giong CaptureExpenseActivity)
    private void applyWindowInsets() {
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
