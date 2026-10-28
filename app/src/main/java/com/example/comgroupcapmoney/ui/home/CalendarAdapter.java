package com.example.comgroupcapmoney.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private final List<CalendarDay> items = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CalendarDay> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = items.get(position);
        holder.bind(day, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardDayContainer;
        private final ImageView ivDayThumb;
        private final View viewTodayBorder;
        private final TextView tvDayNumber;
        private final TextView tvDayTotal;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDayContainer = itemView.findViewById(R.id.card_day_container);
            ivDayThumb = itemView.findViewById(R.id.iv_day_thumb);
            viewTodayBorder = itemView.findViewById(R.id.view_today_border);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            tvDayTotal = itemView.findViewById(R.id.tv_day_total);
        }

        public void bind(CalendarDay day, OnDayClickListener listener) {
            if (!day.isInMonth()) {
                itemView.setVisibility(View.INVISIBLE);
                itemView.setOnClickListener(null);
                return;
            }

            itemView.setVisibility(View.VISIBLE);
            Context context = itemView.getContext();

            tvDayNumber.setText(String.valueOf(day.getDay()));
            viewTodayBorder.setVisibility(day.isToday() ? View.VISIBLE : View.GONE);

            if (day.getThumbUrl() != null) {
                ivDayThumb.setVisibility(View.VISIBLE);
                cardDayContainer.setCardBackgroundColor(Color.TRANSPARENT);
                tvDayNumber.setTextColor(Color.WHITE);
                tvDayNumber.setShadowLayer(4f, 0f, 0f, Color.BLACK);
                Glide.with(itemView).load(day.getThumbUrl()).centerCrop().into(ivDayThumb);
            } else if (!day.getItems().isEmpty()) {
                Glide.with(itemView).clear(ivDayThumb);
                ivDayThumb.setVisibility(View.GONE);
                cardDayContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bg_card_secondary));
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                tvDayNumber.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT);
            } else {
                Glide.with(itemView).clear(ivDayThumb);
                ivDayThumb.setVisibility(View.GONE);
                cardDayContainer.setCardBackgroundColor(Color.TRANSPARENT);
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                tvDayNumber.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT);
            }

            if (day.getTotalExpense() > 0) {
                tvDayTotal.setVisibility(View.VISIBLE);
                tvDayTotal.setText("-" + CurrencyUtils.formatShort(day.getTotalExpense()));
                tvDayTotal.setTextColor(ContextCompat.getColor(context, R.color.color_expense));
            } else if (day.getTotalIncome() > 0) {
                tvDayTotal.setVisibility(View.VISIBLE);
                tvDayTotal.setText("+" + CurrencyUtils.formatShort(day.getTotalIncome()));
                tvDayTotal.setTextColor(ContextCompat.getColor(context, R.color.color_income));
            } else {
                tvDayTotal.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }
    }
}
