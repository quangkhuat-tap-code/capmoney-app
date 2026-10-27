package com.example.comgroupcapmoney.ui.budget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Budget;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface Listener {
        /** Bam 1 dong: sua han muc. */
        void onBudgetClick(Budget budget);

        /** Nhan giu 1 dong: xoa. */
        void onBudgetLongClick(Budget budget);
    }

    private final List<Budget> items = new ArrayList<>();
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Budget> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = items.get(position);
        holder.bind(budget);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBudgetClick(budget);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener == null) return false;
            listener.onBudgetLongClick(budget);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** status server tra ve; thieu/la thi tinh tu percent theo cung luat (<80 ok, <100 warning, con lai over). */
    static String resolveStatus(String status, int percent) {
        if (Budget.STATUS_OK.equals(status) || Budget.STATUS_WARNING.equals(status) || Budget.STATUS_OVER.equals(status)) {
            return status;
        }
        if (percent < 80) return Budget.STATUS_OK;
        if (percent < 100) return Budget.STATUS_WARNING;
        return Budget.STATUS_OVER;
    }

    /** ok -> xanh la, warning -> vang, over -> do. */
    @ColorRes
    static int statusColorRes(String status) {
        if (Budget.STATUS_OVER.equals(status)) return R.color.color_expense;
        if (Budget.STATUS_WARNING.equals(status)) return R.color.color_warning;
        return R.color.color_income;
    }

    static int parseColor(String hex, int fallback) {
        if (hex == null || hex.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Color.parseColor(hex.trim());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcon;
        private final TextView tvName;
        private final TextView tvPercent;
        private final TextView tvSpent;
        private final LinearProgressIndicator progress;
        private final TextView tvRemaining;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.budget_item_icon);
            tvName = itemView.findViewById(R.id.budget_item_name);
            tvPercent = itemView.findViewById(R.id.budget_item_percent);
            tvSpent = itemView.findViewById(R.id.budget_item_spent);
            progress = itemView.findViewById(R.id.budget_item_progress);
            tvRemaining = itemView.findViewById(R.id.budget_item_remaining);
        }

        void bind(Budget b) {
            Context ctx = itemView.getContext();

            int catColor = parseColor(b.getCategoryColor(), ContextCompat.getColor(ctx, R.color.color_accent));
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.argb(0x33, Color.red(catColor), Color.green(catColor), Color.blue(catColor)));
            tvIcon.setBackground(circle);
            tvIcon.setText(b.getCategoryIcon() != null ? b.getCategoryIcon() : "");
            tvName.setText(b.getCategoryName());

            int statusColor = ContextCompat.getColor(ctx, statusColorRes(resolveStatus(b.getStatus(), b.getPercent())));
            tvPercent.setText(b.getPercent() + "%");
            tvPercent.setTextColor(statusColor);

            tvSpent.setText(ctx.getString(R.string.budget_item_spent,
                    CurrencyUtils.format(b.getSpentAmount()), CurrencyUtils.format(b.getLimitAmount())));

            progress.setIndicatorColor(statusColor);
            progress.setProgressCompat(Math.max(0, Math.min(b.getPercent(), 100)), false);

            if (b.getRemaining() >= 0) {
                tvRemaining.setText(ctx.getString(R.string.budget_item_remaining, CurrencyUtils.format(b.getRemaining())));
                tvRemaining.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary));
            } else {
                tvRemaining.setText(ctx.getString(R.string.budget_item_over, CurrencyUtils.format(-b.getRemaining())));
                tvRemaining.setTextColor(ContextCompat.getColor(ctx, R.color.color_expense));
            }
        }
    }
}
