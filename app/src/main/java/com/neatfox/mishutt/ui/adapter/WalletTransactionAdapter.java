package com.neatfox.mishutt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.model.Wallet;

import java.util.ArrayList;

import static com.neatfox.mishutt.Constants.addCommaString;

public class WalletTransactionAdapter extends RecyclerView.Adapter<WalletTransactionAdapter.ViewHolder> {

    ArrayList<Wallet> transaction_list;
    Context context;

    public WalletTransactionAdapter(ArrayList<Wallet> transaction_list, Context context) {
        this.transaction_list = transaction_list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_wallet_transaction, parent, false);
        return new ViewHolder(view, context, transaction_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Wallet wallet = transaction_list.get(position);
        holder.category.setText(wallet.getService_name());
        holder.date_time.setText(String.format("%s %s", wallet.getTransaction_date(), wallet.getTransaction_time()));
        holder.amount.setText(String.format("₹%s", addCommaString(wallet.getTransaction_amount())));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });
    }

    @Override
    public int getItemCount() {
        return transaction_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Wallet> transaction_list;
        TextView category,date_time,amount;

        ViewHolder(View itemView, Context context, ArrayList<Wallet> transaction_list) {
            super(itemView);
            this.context = context;
            this.transaction_list = transaction_list;
            category = itemView.findViewById(R.id.tv_category);
            date_time = itemView.findViewById(R.id.tv_transaction_date_time);
            amount = itemView.findViewById(R.id.tv_transaction_amount);
        }
    }
}
