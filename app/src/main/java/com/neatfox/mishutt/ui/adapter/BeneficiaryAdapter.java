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
import com.neatfox.mishutt.ui.activity.SendMoneyActivity;
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

        if (beneficiary.getHas_account_info().equalsIgnoreCase("Y")){
            holder.account_number.setText(beneficiary.getAccount_number());
            holder.ifsc_code.setText(beneficiary.getIfsc_code());
            holder.register.setVisibility(View.GONE);
            holder.send_money.setVisibility(View.VISIBLE);
        } else {
            String _string = "Not Added Yet";
            holder.account_number.setText(_string);
            holder.ifsc_code.setText(_string);
            holder.register.setVisibility(View.VISIBLE);
            holder.send_money.setVisibility(View.GONE);
        }

        holder.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, AddBeneficiaryInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("name",beneficiary.getFirst_name());
                intent.putExtra("mobile_number",beneficiary.getMobile_number());
                intent.putExtra("remitterId",beneficiary.getRemitterId());
                intent.putExtra("remitter_id",beneficiary.getRemitter_id());
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        holder.send_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, SendMoneyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("name",beneficiary.getFirst_name());
                intent.putExtra("mobile_number",beneficiary.getMobile_number());
                intent.putExtra("beneficiary_id",beneficiary.getBeneficiary_id());
                intent.putExtra("remitter_id",beneficiary.getRemitter_id());
                intent.putExtra("account_number",beneficiary.getAccount_number());
                intent.putExtra("ifsc_code",beneficiary.getIfsc_code());
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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
        TextView name,mobile_number,pin_code,account_number,ifsc_code;
        Button register,send_money;

        ViewHolder(View itemView, Context context, ArrayList<Beneficiary> beneficiary_list) {
            super(itemView);
            this.context = context;
            this.beneficiary_list = beneficiary_list;
            name = itemView.findViewById(R.id.tv_name);
            mobile_number = itemView.findViewById(R.id.tv_mobile_number);
            pin_code = itemView.findViewById(R.id.tv_pin_code);
            account_number = itemView.findViewById(R.id.tv_account_number);
            ifsc_code = itemView.findViewById(R.id.tv_ifsc_code);
            register = itemView.findViewById(R.id.button_register);
            send_money = itemView.findViewById(R.id.button_send_money);
        }
    }
}
