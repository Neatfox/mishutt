package com.neatfox.mishutt.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.activity.AddBeneficiaryInfoActivity;
import com.neatfox.mishutt.ui.activity.MoneyTransferActivity;
import com.neatfox.mishutt.ui.model.Beneficiary;

import java.util.ArrayList;

public class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder> {

    ArrayList<Beneficiary> beneficiary_list;
    Context context;
    Activity activity;

    public BeneficiaryAdapter(ArrayList<Beneficiary> beneficiary_list, Context context, Activity activity) {
        this.beneficiary_list = beneficiary_list;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_beneficiary, parent, false);
        return new ViewHolder(view, context, beneficiary_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Beneficiary beneficiary = beneficiary_list.get(position);

        holder.name.setText(String.format("%s %s", beneficiary.getFirst_name(), beneficiary.getLast_name()));
        holder.mobile_number.setText(beneficiary.getMobile_number());
        holder.pin_code.setText(beneficiary.getPin_code());

        holder.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, AddBeneficiaryInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });

        holder.transfer_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, MoneyTransferActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });
    }

    @Override
    public int getItemCount() {
        return beneficiary_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Beneficiary> beneficiary_list;
        TextView name,mobile_number,pin_code;
        Button register,transfer_money;

        ViewHolder(View itemView, Context context, ArrayList<Beneficiary> beneficiary_list) {
            super(itemView);
            this.context = context;
            this.beneficiary_list = beneficiary_list;
            name = itemView.findViewById(R.id.tv_name);
            mobile_number = itemView.findViewById(R.id.tv_mobile_number);
            pin_code = itemView.findViewById(R.id.tv_pin_code);
            register = itemView.findViewById(R.id.button_register);
            transfer_money = itemView.findViewById(R.id.button_transfer_money);
        }
    }
}
