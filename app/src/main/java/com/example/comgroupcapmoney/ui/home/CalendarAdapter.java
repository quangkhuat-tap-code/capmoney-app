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
import java.util.ArrayList;
import java.util.Calendar;
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
        private final View viewCircleFuture;
        private final View layoutCirclePast;
        private final ImageView ivDayThumb;
        private final View viewTodayBorder;
        private final TextView tvDayNumber;
        private final View viewTodayDot;
        private final TextView tvDayTotal;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCircleFuture = itemView.findViewById(R.id.view_circle_future);
            layoutCirclePast = itemView.findViewById(R.id.layout_circle_past);
            ivDayThumb = itemView.findViewById(R.id.iv_day_thumb);
            viewTodayBorder = itemView.findViewById(R.id.view_today_border);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            viewTodayDot = itemView.findViewById(R.id.view_today_dot);
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

            // Determine if future day
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1;
            int currentDay = now.get(Calendar.DAY_OF_MONTH);

            boolean isFuture = (day.getYear() > currentYear) ||
                    (day.getYear() == currentYear && day.getMonth() > currentMonth) ||
                    (day.getYear() == currentYear && day.getMonth() == currentMonth && day.getDay() > currentDay);

            if (day.isToday()) {
                viewCircleFuture.setVisibility(View.GONE);
                layoutCirclePast.setVisibility(View.VISIBLE);
                viewTodayBorder.setVisibility(View.VISIBLE);
                viewTodayDot.setVisibility(View.VISIBLE);
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.color_accent));
            } else if (isFuture) {
                viewCircleFuture.setVisibility(View.VISIBLE);
                layoutCirclePast.setVisibility(View.GONE);
                viewTodayBorder.setVisibility(View.GONE);
                viewTodayDot.setVisibility(View.GONE);
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                viewCircleFuture.setVisibility(View.GONE);
                layoutCirclePast.setVisibility(View.VISIBLE);
                viewTodayBorder.setVisibility(View.GONE);
                viewTodayDot.setVisibility(View.GONE);
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
            }

            // Thumbnail
            if (day.getThumbUrl() != null && !day.getThumbUrl().trim().isEmpty()) {
                ivDayThumb.setVisibility(View.VISIBLE);
                Glide.with(itemView).load(day.getThumbUrl()).centerCrop().into(ivDayThumb);
            } else {
                ivDayThumb.setVisibility(View.GONE);
            }

            // Total amount under day
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
