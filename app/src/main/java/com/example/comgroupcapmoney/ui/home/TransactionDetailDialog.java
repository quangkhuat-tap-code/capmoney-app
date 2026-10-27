package com.example.comgroupcapmoney.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.ui.capture.CaptureExpenseActivity;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class TransactionDetailDialog extends BottomSheetDialogFragment {

    private static final String ARG_TX = "tx";

    public static TransactionDetailDialog newInstance(Transaction tx) {
        TransactionDetailDialog dialog = new TransactionDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TX, tx);
        dialog.setArguments(args);
        return dialog;
    }

    private Transaction tx;
    private HomeViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tx = (Transaction) getArguments().getSerializable(ARG_TX);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transaction_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (tx == null) {
            dismiss();
            return;
        }

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        View layoutPhotoContainer = view.findViewById(R.id.layout_photo_container);
        ImageView ivDetailPhoto = view.findViewById(R.id.iv_detail_photo);
        TextView tvDetailAmount = view.findViewById(R.id.tv_detail_amount);
        TextView tvDetailCategory = view.findViewById(R.id.tv_detail_category);
        TextView tvDetailWallet = view.findViewById(R.id.tv_detail_wallet);
        TextView tvDetailTime = view.findViewById(R.id.tv_detail_time);
        LinearLayout rowDetailNote = view.findViewById(R.id.row_detail_note);
        TextView tvDetailNote = view.findViewById(R.id.tv_detail_note);
        MaterialButton btnDeleteTx = view.findViewById(R.id.btn_delete_tx);
        CircularProgressIndicator progressDelete = view.findViewById(R.id.progress_delete);

        // Photo
        String imageUrl = tx.getFullImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            layoutPhotoContainer.setVisibility(View.VISIBLE);
            int cornerPx = getResources().getDimensionPixelSize(R.dimen.corner_radius_card);
            Glide.with(this)
                    .load(imageUrl)
                    .transform(new RoundedCorners(cornerPx))
                    .into(ivDetailPhoto);
        } else {
            layoutPhotoContainer.setVisibility(View.GONE);
        }

        // Amount
        tvDetailAmount.setText(CurrencyUtils.formatSigned(tx.getAmount(), tx.isExpense()));
        tvDetailAmount.setTextColor(ContextCompat.getColor(requireContext(),
                tx.isExpense() ? R.color.color_expense : R.color.color_income));

        // Category & Type
        String typeLabel = tx.isExpense() ? getString(R.string.capture_type_expense) : getString(R.string.capture_type_income);
        String categoryIcon = tx.getCategoryIcon() != null ? tx.getCategoryIcon() : "💰";
        String categoryName = tx.getCategoryName() != null ? tx.getCategoryName() : "";
        tvDetailCategory.setText(categoryIcon + " " + categoryName + " · " + typeLabel);

        // Wallet
        tvDetailWallet.setText(tx.getWalletName());

        // Time
        tvDetailTime.setText(DateUtils.toDisplay(tx.getTransactionDate()));

        // Note
        if (tx.getNote() != null && !tx.getNote().trim().isEmpty()) {
            rowDetailNote.setVisibility(View.VISIBLE);
            tvDetailNote.setText(tx.getNote().trim());
        } else {
            rowDetailNote.setVisibility(View.GONE);
        }

        // Sua: mo man chup o che do sua, Home tu tai lai trong onResume khi quay ve
        view.findViewById(R.id.btn_edit_tx).setOnClickListener(v -> {
            dismiss();
            startActivity(CaptureExpenseActivity.editIntent(requireContext(), tx));
        });

        // Delete button
        btnDeleteTx.setOnClickListener(v -> {
            String msg = tx.isExpense()
                    ? getString(R.string.home_delete_msg_expense, CurrencyUtils.format(tx.getAmount()), tx.getWalletName())
                    : getString(R.string.home_delete_msg_income, CurrencyUtils.format(tx.getAmount()), tx.getWalletName());

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.home_delete_title)
                    .setMessage(msg)
                    .setNegativeButton(R.string.home_delete_cancel, null)
                    .setPositiveButton(R.string.home_delete_confirm, (dialog, which) -> {
                        btnDeleteTx.setEnabled(false);
                        btnDeleteTx.setText("");
                        progressDelete.setVisibility(View.VISIBLE);
                        viewModel.delete(tx.getId());
                        dismiss();
                    })
                    .show();
        });
    }
}
