package com.example.comgroupcapmoney.ui.profile;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.data.api.ApiClient;
import com.example.comgroupcapmoney.model.UserProfile;
import com.example.comgroupcapmoney.ui.auth.SessionManager;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.SharedPrefManager;
import com.google.android.material.snackbar.Snackbar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String NO_VALUE = "—";

    private ProfileViewModel viewModel;
    private TextView tvAvatar;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvMemberSince;
    private TextView tvTransactions;
    private TextView tvIncome;
    private TextView tvExpense;
    private View btnRetry;
    private ImageView ivAvatar;
    private View avatarContainer;
    private View avatarProgress;
    private String shownAvatarUrl;

    // Xoay man hinh: ViewModel da co du lieu thi lan onResume dau tien khong goi lai API
    private boolean skipNextResumeLoad;

    // Chon anh dai dien tu thu vien, khong can xin quyen (4B-3)
    private ActivityResultLauncher<PickVisualMediaRequest> pickAvatarLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAvatarLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                viewModel.uploadAvatar(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // targetSdk 35+ bat buoc edge-to-edge: day noi dung xuong duoi status bar
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, v.getPaddingBottom());
            return insets;
        });

        tvAvatar = view.findViewById(R.id.profile_tv_avatar);
        tvName = view.findViewById(R.id.profile_tv_name);
        tvEmail = view.findViewById(R.id.profile_tv_email);
        tvMemberSince = view.findViewById(R.id.profile_tv_member_since);
        tvTransactions = view.findViewById(R.id.profile_tv_stat_transactions);
        tvIncome = view.findViewById(R.id.profile_tv_stat_income);
        tvExpense = view.findViewById(R.id.profile_tv_stat_expense);
        btnRetry = view.findViewById(R.id.profile_btn_retry);
        ivAvatar = view.findViewById(R.id.profile_iv_avatar);
        avatarContainer = view.findViewById(R.id.avatar_container);
        avatarProgress = view.findViewById(R.id.avatar_progress);
        avatarContainer.setOnClickListener(v -> pickAvatarLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        btnRetry.setOnClickListener(v -> viewModel.loadProfile());
        view.findViewById(R.id.profile_btn_logout).setOnClickListener(v -> confirmLogout());
        view.findViewById(R.id.profile_btn_change_password).setOnClickListener(v ->
                ChangePasswordDialog.newInstance().show(getChildFragmentManager(), "change_password"));

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        skipNextResumeLoad = savedInstanceState != null && viewModel.getProfile().getValue() != null;

        viewModel.getProfile().observe(getViewLifecycleOwner(), p -> render());
        viewModel.getError().observe(getViewLifecycleOwner(), e -> render());
        viewModel.getUnauthorized().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getContentIfNotHandled() != null) {
                SessionManager.logout(requireActivity(), true);
            }
        });
        viewModel.getUploadingAvatar().observe(getViewLifecycleOwner(), uploading -> {
            boolean busy = Boolean.TRUE.equals(uploading);
            avatarProgress.setVisibility(busy ? View.VISIBLE : View.GONE);
            avatarContainer.setEnabled(!busy);
        });
        viewModel.getAvatarUpdated().observe(getViewLifecycleOwner(), event -> {
            String msg = event != null ? event.getContentIfNotHandled() : null;
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getAvatarError().observe(getViewLifecycleOwner(), event -> {
            String msg = event != null ? event.getContentIfNotHandled() : null;
            if (msg != null) {
                Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tai lai moi lan quay ve tab de thay so giao dich moi
        if (skipNextResumeLoad) {
            skipNextResumeLoad = false;
            return;
        }
        viewModel.loadProfile();
    }

    private void render() {
        UserProfile profile = viewModel.getProfile().getValue();
        String error = viewModel.getError().getValue();

        if (error != null || profile == null) {
            // Loi mang (hoac chua tai xong): dung ten luu tren may, so lieu hien "—"
            String name = SharedPrefManager.getInstance(requireContext()).getUserName();
            tvAvatar.setText(avatarLetter(name));
            tvName.setText(name != null ? name : "");
            tvEmail.setText("");
            tvMemberSince.setText("");
            tvTransactions.setText(NO_VALUE);
            tvIncome.setText(NO_VALUE);
            tvExpense.setText(NO_VALUE);
            btnRetry.setVisibility(error != null ? View.VISIBLE : View.GONE);
            // Mat mang nhung da tung tai: giu anh dang hien
            showAvatar(profile != null ? profile.getAvatarUrl() : shownAvatarUrl);
            return;
        }

        btnRetry.setVisibility(View.GONE);
        tvAvatar.setText(avatarLetter(profile.getFullName()));
        showAvatar(profile.getAvatarUrl());
        tvName.setText(profile.getFullName());
        tvEmail.setText(profile.getEmail());
        String date = toDisplayDate(profile.getCreatedAt());
        tvMemberSince.setText(date != null ? getString(R.string.profile_member_since, date) : "");
        tvTransactions.setText(String.valueOf(profile.getTotalTransactions()));
        tvIncome.setText(CurrencyUtils.format(profile.getTotalIncome()));
        tvExpense.setText(CurrencyUtils.format(profile.getTotalExpense()));
    }

    // Co anh thi chong anh tron len chu cai; khong co anh hoac tai anh loi thi hien chu cai
    private void showAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            shownAvatarUrl = null;
            ivAvatar.setVisibility(View.GONE);
            return;
        }
        if (avatarUrl.equals(shownAvatarUrl) && ivAvatar.getVisibility() == View.VISIBLE) {
            return;
        }
        shownAvatarUrl = avatarUrl;
        ivAvatar.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(ApiClient.IMAGE_BASE_URL + avatarUrl)
                .circleCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        ivAvatar.setVisibility(View.GONE);
                        shownAvatarUrl = null;
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(ivAvatar);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_msg)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.profile_btn_logout,
                        (d, w) -> SessionManager.logout(requireActivity(), false))
                .show();
    }

    // Chu cai dau cua tu cuoi trong ho ten: "Nguyen Van An" -> "A"
    static String avatarLetter(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        String last = parts[parts.length - 1];
        return last.substring(0, 1).toUpperCase(Locale.getDefault());
    }

    // "2026-10-22 10:09:00" -> "22/10/2026"
    static String toDisplayDate(String createdAt) {
        if (createdAt == null) return null;
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(createdAt);
            return d != null ? new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(d) : null;
        } catch (ParseException e) {
            return null;
        }
    }
}
