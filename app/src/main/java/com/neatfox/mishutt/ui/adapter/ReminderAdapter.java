package com.neatfox.mishutt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.model.Transaction;

import java.util.ArrayList;

import static com.neatfox.mishutt.Constants.addCommaString;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    ArrayList<Transaction> transaction_list;
    Context context;

    public ReminderAdapter(ArrayList<Transaction> transaction_list, Context context) {
        this.transaction_list = transaction_list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_reminder, parent, false);
        return new ViewHolder(view, context, transaction_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Transaction transaction = transaction_list.get(position);

        holder.date.setText(transaction.getDate());
        holder.category.setText(transaction.getCategory());
        holder.amount.setText(String.format("₹%s", addCommaString(transaction.getSpending())));
        holder.description.setText(transaction.getDescription());

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
        ArrayList<Transaction> transaction_list;
        TextView date,category,amount,description;
        ImageView edit,delete;

        ViewHolder(View itemView, Context context, ArrayList<Transaction> transaction_list) {
            super(itemView);
            this.context = context;
            this.transaction_list = transaction_list;
            date = itemView.findViewById(R.id.tv_date);
            category = itemView.findViewById(R.id.tv_category);
            amount = itemView.findViewById(R.id.tv_amount);
            description = itemView.findViewById(R.id.tv_description);
            edit = itemView.findViewById(R.id.iv_edit);
            delete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
