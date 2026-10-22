package com.example.comgroupcapmoney.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import java.util.Objects;

public class PhotoGridAdapter extends ListAdapter<Transaction, PhotoGridAdapter.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getAmount() == newItem.getAmount()
                    && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                    && Objects.equals(oldItem.getNote(), newItem.getNote())
                    && oldItem.getCategoryId() == newItem.getCategoryId()
                    && Objects.equals(oldItem.getTransactionDate(), newItem.getTransactionDate());
        }
    };

    private final OnTransactionClickListener listener;

    public PhotoGridAdapter(OnTransactionClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = getItem(position);
        holder.bind(tx, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivThumb;
        private final TextView tvPlaceholderIcon;
        private final TextView tvAmountLabel;
        private final TextView tvCategoryBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            tvPlaceholderIcon = itemView.findViewById(R.id.tv_placeholder_icon);
            tvAmountLabel = itemView.findViewById(R.id.tv_amount_label);
            tvCategoryBadge = itemView.findViewById(R.id.tv_category_badge);
        }

        public void bind(Transaction tx, OnTransactionClickListener listener) {
            boolean hasImage = tx.getImageUrl() != null && !tx.getImageUrl().trim().isEmpty();

            if (hasImage) {
                ivThumb.setVisibility(View.VISIBLE);
                tvPlaceholderIcon.setVisibility(View.GONE);
                tvCategoryBadge.setVisibility(View.VISIBLE);
                tvCategoryBadge.setText(tx.getCategoryIcon());

                int cornerPx = itemView.getResources().getDimensionPixelSize(R.dimen.corner_radius_small);
                Glide.with(itemView)
                        .load(tx.getFullImageUrl())
                        .transform(new CenterCrop(), new RoundedCorners(cornerPx))
                        .placeholder(R.color.bg_card_secondary)
                        .error(R.color.bg_card_secondary)
                        .into(ivThumb);
            } else {
                Glide.with(itemView).clear(ivThumb);
                ivThumb.setVisibility(View.GONE);
                tvCategoryBadge.setVisibility(View.GONE);
                tvPlaceholderIcon.setVisibility(View.VISIBLE);
                tvPlaceholderIcon.setText(tx.getCategoryIcon() != null ? tx.getCategoryIcon() : "💰");
            }

            tvAmountLabel.setText(CurrencyUtils.formatSigned(tx.getAmount(), tx.isExpense()));
            int color = ContextCompat.getColor(itemView.getContext(),
                    tx.isExpense() ? R.color.color_expense : R.color.color_income);
            tvAmountLabel.setTextColor(color);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(tx);
                }
            });
        }
    }
}
