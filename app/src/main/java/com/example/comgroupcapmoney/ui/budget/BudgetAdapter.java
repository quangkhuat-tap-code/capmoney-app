package com.example.comgroupcapmoney.ui.budget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Budget;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter hiển thị danh sách ngân sách tháng (Tab 4).
 *
 * Quy tắc đổi màu ProgressBar:
 *   < 80%        → Xanh lá (income_green / #4CD964)
 *   80% – 99%   → Vàng cảnh báo (#F5A623)
 *   ≥ 100%      → Đỏ báo động (#FF6464)
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(Budget budget, int position);
    }

    private final Context context;
    private List<Budget> items = new ArrayList<>();
    private OnDeleteClickListener deleteListener;

    private static final NumberFormat VND_FORMAT;
    static {
        VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        VND_FORMAT.setMaximumFractionDigits(0);
    }

    // Ngưỡng màu
    private static final int COLOR_GREEN  = Color.parseColor("#4CD964");
    private static final int COLOR_YELLOW = Color.parseColor("#F5A623");
    private static final int COLOR_RED    = Color.parseColor("#FF6464");

    public BudgetAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<Budget> newItems) {
        items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = items.get(position);

        // Icon + tên danh mục
        holder.tvCategoryIcon.setText(budget.getCategoryIcon() != null
                ? budget.getCategoryIcon() : "💰");
        holder.tvCategoryName.setText(budget.getCategoryName());

        // Số tiền
        String spent = VND_FORMAT.format(budget.getSpentAmount()) + " đ";
        String limit = VND_FORMAT.format(budget.getLimitAmount()) + " đ";
        holder.tvSpent.setText(spent);
        holder.tvLimit.setText(limit);

        // ProgressBar
        int percent = budget.getProgressPercent();
        holder.progressBudget.setProgress(percent);

        // Màu ProgressBar theo %
        int color;
        if (percent >= 100) {
            color = COLOR_RED;
        } else if (percent >= 80) {
            color = COLOR_YELLOW;
        } else {
            color = COLOR_GREEN;
        }
        holder.progressBudget.setProgressTintList(ColorStateList.valueOf(color));
        holder.tvSpent.setTextColor(color);

        // Nhãn %
        holder.tvProgressLabel.setText(String.format(Locale.getDefault(), "%d%%", percent));

        // Nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(budget, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Xóa item khỏi danh sách local sau khi xóa thành công trên server */
    public void removeAt(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon;
        TextView tvCategoryName;
        TextView tvSpent;
        TextView tvLimit;
        ProgressBar progressBudget;
        TextView tvProgressLabel;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon   = itemView.findViewById(R.id.tv_category_icon);
            tvCategoryName   = itemView.findViewById(R.id.tv_category_name);
            tvSpent          = itemView.findViewById(R.id.tv_spent);
            tvLimit          = itemView.findViewById(R.id.tv_limit);
            progressBudget   = itemView.findViewById(R.id.progress_budget);
            tvProgressLabel  = itemView.findViewById(R.id.tv_progress_label);
            btnDelete        = itemView.findViewById(R.id.btn_delete);
        }
    }
}
