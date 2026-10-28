package com.example.comgroupcapmoney.ui.stats;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.CategoryStat;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private final List<CategoryStat> items = new ArrayList<>();

    public void setItems(List<CategoryStat> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** "#FF9F0A" -> mau; sai/thieu -> fallback. */
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

    /** 1 chu so thap phan kieu Viet Nam, bo ",0": 63.0 -> "63%", 36.5 -> "36,5%". */
    static String formatPercent(double percent) {
        double rounded = Math.round(percent * 10) / 10.0;
        if (rounded == Math.floor(rounded)) {
            return ((long) rounded) + "%";
        }
        return String.format(Locale.US, "%.1f", rounded).replace('.', ',') + "%";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcon;
        private final TextView tvName;
        private final TextView tvAmount;
        private final TextView tvCount;
        private final LinearProgressIndicator progress;
        private final TextView tvPercent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.stats_item_icon);
            tvName = itemView.findViewById(R.id.stats_item_name);
            tvAmount = itemView.findViewById(R.id.stats_item_amount);
            tvCount = itemView.findViewById(R.id.stats_item_count);
            progress = itemView.findViewById(R.id.stats_item_progress);
            tvPercent = itemView.findViewById(R.id.stats_item_percent);
        }

        void bind(CategoryStat item) {
            Context ctx = itemView.getContext();
            int color = parseColor(item.getCategoryColor(), ContextCompat.getColor(ctx, R.color.color_accent));

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.argb(0x33, Color.red(color), Color.green(color), Color.blue(color)));
            tvIcon.setBackground(circle);
            tvIcon.setText(item.getCategoryIcon() != null ? item.getCategoryIcon() : "");

            tvName.setText(item.getCategoryName());
            tvAmount.setText(CurrencyUtils.format(item.getAmount()));
            tvCount.setText(ctx.getString(R.string.stats_count, item.getCount()));

            progress.setIndicatorColor(color);
            progress.setProgressCompat((int) Math.round(item.getPercent()), false);
            tvPercent.setText(formatPercent(item.getPercent()));
        }
    }
}
