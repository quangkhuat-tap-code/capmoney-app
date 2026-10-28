package com.example.comgroupcapmoney.ui.wallets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comgroupcapmoney.R;
import com.example.comgroupcapmoney.model.Wallet;
import com.example.comgroupcapmoney.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    private final List<Wallet> items = new ArrayList<>();
    private final OnWalletClickListener listener;

    public WalletAdapter(OnWalletClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Wallet> wallets) {
        items.clear();
        if (wallets != null) {
            items.addAll(wallets);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet wallet = items.get(position);
        holder.tvIcon.setText(wallet.getTypeEmoji());
        holder.tvName.setText(wallet.getName());
        holder.tvBadge.setVisibility(wallet.isDefault() ? View.VISIBLE : View.GONE);
        holder.tvBalance.setText(CurrencyUtils.format(wallet.getBalance()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWalletClick(wallet);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon;
        final TextView tvName;
        final TextView tvBadge;
        final TextView tvBalance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.wallet_item_icon);
            tvName = itemView.findViewById(R.id.wallet_item_name);
            tvBadge = itemView.findViewById(R.id.wallet_item_badge);
            tvBalance = itemView.findViewById(R.id.wallet_item_balance);
        }
    }
}
