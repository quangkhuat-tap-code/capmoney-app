package com.example.comgroupcapmoney.ui.wallets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Transfer;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class TransferHistoryAdapter extends RecyclerView.Adapter<TransferHistoryAdapter.ViewHolder> {

    private final List<Transfer> items = new ArrayList<>();

    public void submit(List<Transfer> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // "2026-10-21 09:15:00" -> "21/10 09:15"
    static String shortDate(String serverDate) {
        if (serverDate == null || serverDate.length() < 16) return "";
        return serverDate.substring(8, 10) + "/" + serverDate.substring(5, 7) + " " + serverDate.substring(11, 16);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute;
        private final TextView tvAmount;
        private final TextView tvNote;
        private final TextView tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.transfer_history_tv_route);
            tvAmount = itemView.findViewById(R.id.transfer_history_tv_amount);
            tvNote = itemView.findViewById(R.id.transfer_history_tv_note);
            tvDate = itemView.findViewById(R.id.transfer_history_tv_date);
        }

        void bind(Transfer t) {
            tvRoute.setText(itemView.getContext().getString(R.string.transfer_history_route,
                    t.getFromWalletName(), t.getToWalletName()));
            tvAmount.setText(CurrencyUtils.format(t.getAmount()));
            String note = t.getNote() != null ? t.getNote().trim() : "";
            // Khong an o ghi chu: o nay chiem cho de ngay luon nam ben phai
            tvNote.setText(note);
            tvDate.setText(shortDate(t.getTransferDate()));
        }
    }
}
