package com.example.comgroupcapmoney.ui.capture;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private Category selectedCategory;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Category> newItems, Category selected) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        this.selectedCategory = selected;
        notifyDataSetChanged();
    }

    public void setSelectedCategory(Category selected) {
        this.selectedCategory = selected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category cat = items.get(position);
        holder.bind(cat, selectedCategory != null && selectedCategory.getId() == cat.getId(), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final View viewCircleBg;
        private final TextView tvIcon;
        private final TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCircleBg = itemView.findViewById(R.id.view_circle_bg);
            tvIcon = itemView.findViewById(R.id.tv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }

        public void bind(Category cat, boolean isSelected, OnCategoryClickListener listener) {
            tvIcon.setText(cat.getIcon());
            tvName.setText(cat.getName());

            int color = cat.getColorInt();
            int alphaColor = Color.argb(0x33, Color.red(color), Color.green(color), Color.blue(color));

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(alphaColor);

            if (isSelected) {
                int strokeWidth = Math.round(2 * itemView.getResources().getDisplayMetrics().density);
                drawable.setStroke(strokeWidth, color);
            } else {
                drawable.setStroke(0, Color.TRANSPARENT);
            }

            viewCircleBg.setBackground(drawable);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(cat);
                }
            });
        }
    }
}
