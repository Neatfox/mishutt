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
import com.neatfox.mishutt.ui.activity.CheckInvestmentActivity;
import com.neatfox.mishutt.ui.model.Investment;

import java.util.ArrayList;

public class GoalInvestmentAdapter extends RecyclerView.Adapter<GoalInvestmentAdapter.ViewHolder> {

    ArrayList<Investment> investment_list;
    Context context;
    Activity activity;

    public GoalInvestmentAdapter(ArrayList<Investment> investment_list, Context context, Activity activity) {
        this.investment_list = investment_list;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_goal_investment, parent, false);
        return new ViewHolder(view, context, investment_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Investment investment = investment_list.get(position);

        holder.investment_type.setText(investment.getInvestment_type());
        holder.scheme_name.setText(investment.getScheme_name());

        String min_investment_amount = investment.getMin_invest_amount();
        String max_investment_amount = investment.getMax_invest_amount();
        String _investment_amount;
        if ("null".equalsIgnoreCase(max_investment_amount) || "".equalsIgnoreCase(max_investment_amount)) {
            if ("".equalsIgnoreCase(min_investment_amount) || "null".equalsIgnoreCase(min_investment_amount))
                _investment_amount = " - ";
            else
                _investment_amount = "₹"+(min_investment_amount);
        }
        else
            _investment_amount = ("₹"+(min_investment_amount) +" - "+"₹"+(max_investment_amount));

        holder.investment_amount.setText(_investment_amount);

        String _aum = investment.getAum();

        if ("null".equalsIgnoreCase(_aum) || "".equalsIgnoreCase(_aum)){
            holder.aum.setText(" - ");
        } else {
            holder.aum.setText(String.format("₹%s",(_aum)));
        }

        String _exit_load = investment.getExit_load();

        if ("null".equalsIgnoreCase(_exit_load) || "".equalsIgnoreCase(_exit_load)){
            holder.exit_load.setText(" - ");
        } else {
            holder.exit_load.setText(_exit_load);
        }

        holder.apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, CheckInvestmentActivity.class);
                intent.putExtra("type","Goal Manager");
                intent.putExtra("item_name",investment.getInvestment_type());
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        return investment_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Investment> investment_list;
        TextView investment_type,scheme_name,investment_amount,aum,exit_load;
        Button apply;

        ViewHolder(View itemView, Context context, ArrayList<Investment> investment_list) {
            super(itemView);
            this.context = context;
            this.investment_list = investment_list;
            investment_type = itemView.findViewById(R.id.tv_investment_type);
            scheme_name = itemView.findViewById(R.id.tv_scheme_name);
            investment_amount = itemView.findViewById(R.id.tv_investment_amount);
            aum = itemView.findViewById(R.id.tv_aum);
            exit_load = itemView.findViewById(R.id.tv_exit_load);
            apply = itemView.findViewById(R.id.button_apply);
        }
    }
}
