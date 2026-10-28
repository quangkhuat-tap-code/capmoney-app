package com.example.comgroupcapmoney.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Transaction;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.example.comgroupcapmoney.utils.DateUtils;
import java.util.Objects;

public class SearchAdapter extends ListAdapter<Transaction, SearchAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Transaction tx);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.getAmount() == newItem.getAmount()
                    && Objects.equals(oldItem.getNote(), newItem.getNote())
                    && oldItem.getCategoryId() == newItem.getCategoryId()
                    && oldItem.getWalletId() == newItem.getWalletId()
                    && Objects.equals(oldItem.getWalletName(), newItem.getWalletName())
                    && Objects.equals(oldItem.getTransactionDate(), newItem.getTransactionDate());
        }
    };

    private final OnItemClickListener listener;

    public SearchAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcon;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.search_item_tv_icon);
            tvTitle = itemView.findViewById(R.id.search_item_tv_title);
            tvSubtitle = itemView.findViewById(R.id.search_item_tv_subtitle);
            tvAmount = itemView.findViewById(R.id.search_item_tv_amount);
        }

        void bind(Transaction tx, OnItemClickListener listener) {
            String categoryName = tx.getCategoryName() != null ? tx.getCategoryName() : "";
            tvIcon.setText(tx.getCategoryIcon() != null ? tx.getCategoryIcon() : "💰");
            String note = tx.getNote() != null ? tx.getNote().trim() : "";
            tvTitle.setText(note.isEmpty() ? categoryName : note);
            tvSubtitle.setText(categoryName + " · " + tx.getWalletName() + " · " + DateUtils.toDisplay(tx.getTransactionDate()));
            tvAmount.setText(CurrencyUtils.formatSigned(tx.getAmount(), tx.isExpense()));
            tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    tx.isExpense() ? R.color.color_expense : R.color.color_income));
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(tx);
                }
            });
        }
    }
}
