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
import com.neatfox.mishutt.ui.activity.CheckLoanActivity;
import com.neatfox.mishutt.ui.model.Loan;

import java.util.ArrayList;

public class GoalLoanAdapter extends RecyclerView.Adapter<GoalLoanAdapter.ViewHolder> {

    ArrayList<Loan> loan_list;
    Context context;
    Activity activity;

    public GoalLoanAdapter(ArrayList<Loan> loan_list, Context context, Activity activity) {
        this.loan_list = loan_list;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_goal_loan, parent, false);
        return new ViewHolder(view, context, loan_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Loan loan = loan_list.get(position);

        String min_interest_rate = loan.getMin_interest_rate();
        String max_interest_rate = loan.getMax_interest_rate();
        String _interest_rate;
        if ("null".equalsIgnoreCase(max_interest_rate) || "".equalsIgnoreCase(max_interest_rate))
            _interest_rate = min_interest_rate;
        else
            _interest_rate = (min_interest_rate +" - "+max_interest_rate);

        String min_loan_amount = loan.getMin_loan_amount();
        String max_loan_amount = loan.getMax_loan_amount();
        String _loan_amount;
        if ("null".equalsIgnoreCase(max_loan_amount) || "".equalsIgnoreCase(max_loan_amount))
            _loan_amount = min_loan_amount;
        else
            _loan_amount = (min_loan_amount +" - "+max_loan_amount);

        String min_tenure = loan.getMin_tenure();
        String max_tenure = loan.getMax_tenure();
        String _tenure;
        if ("null".equalsIgnoreCase(max_tenure) || "".equalsIgnoreCase(max_tenure))
            _tenure = min_tenure;
        else
            _tenure = (min_tenure +" - "+max_tenure);

        holder.bank_name.setText(loan.getBank_name());
        holder.interest_rate.setText(_interest_rate);
        holder.processing_fee.setText(loan.getProcessing_fee());
        holder.loan_amount.setText(_loan_amount);
        holder.tenure.setText(_tenure);

        holder.apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, CheckLoanActivity.class);
                intent.putExtra("type","Goal Manager");
                intent.putExtra("item_name",loan.getLoan_type());
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
        return loan_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Loan> loan_list;
        TextView bank_name,interest_rate,future_value,processing_fee,loan_amount,tenure;
        Button apply;

        ViewHolder(View itemView, Context context, ArrayList<Loan> loan_list) {
            super(itemView);
            this.context = context;
            this.loan_list = loan_list;
            bank_name = itemView.findViewById(R.id.tv_bank_name);
            interest_rate = itemView.findViewById(R.id.tv_interest_rate);
            future_value = itemView.findViewById(R.id.tv_future_value);
            processing_fee = itemView.findViewById(R.id.tv_processing_fee);
            loan_amount = itemView.findViewById(R.id.tv_loan_amount);
            tenure = itemView.findViewById(R.id.tv_tenure);
            apply = itemView.findViewById(R.id.button_apply);
        }
    }
}
