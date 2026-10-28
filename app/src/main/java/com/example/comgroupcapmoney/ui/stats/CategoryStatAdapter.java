package com.example.comgroupcapmoney.ui.stats;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.CategoryStat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter hiển thị danh sách % từng danh mục
 * ở phần dưới biểu đồ Donut Chart (Tab 2 StatsFragment).
 */
public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private final Context context;
    private List<CategoryStat> items = new ArrayList<>();
    private static final NumberFormat VND_FORMAT;

    static {
        VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        VND_FORMAT.setMaximumFractionDigits(0);
    }

    public CategoryStatAdapter(Context context) {
        this.context = context;
    }

    /** Cập nhật dữ liệu — gọi sau khi nhận response từ API */
    public void setItems(List<CategoryStat> newItems) {
        items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStat stat = items.get(position);

        // Tên danh mục
        holder.tvName.setText(stat.getName());

        // Icon danh mục (emoji)
        holder.tvIcon.setText(stat.getIcon() != null ? stat.getIcon() : "");

        // Phần trăm (ví dụ: "35.2%")
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", stat.getPercentage()));

        // Số tiền định dạng VND
        holder.tvAmount.setText(VND_FORMAT.format(stat.getTotal()) + " đ");

        // Chấm màu — đặt màu từ color_hex của danh mục
        try {
            int color = Color.parseColor(stat.getColorHex());
            holder.viewColorDot.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color));
            holder.progressCategory.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(color));
        } catch (IllegalArgumentException e) {
            // Màu hex không hợp lệ → dùng màu mặc định accent
            holder.viewColorDot.setBackgroundTintList(null);
        }

        // Thanh tiến độ (0–100)
        int progress = (int) Math.min(stat.getPercentage(), 100);
        holder.progressCategory.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewColorDot;
        TextView tvIcon;
        TextView tvName;
        TextView tvPercentage;
        TextView tvAmount;
        ProgressBar progressCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorDot      = itemView.findViewById(R.id.view_color_dot);
            tvIcon            = itemView.findViewById(R.id.tv_category_icon);
            tvName            = itemView.findViewById(R.id.tv_category_name);
            tvPercentage      = itemView.findViewById(R.id.tv_percentage);
            tvAmount          = itemView.findViewById(R.id.tv_amount);
            progressCategory  = itemView.findViewById(R.id.progress_category);
        }
    }
}
