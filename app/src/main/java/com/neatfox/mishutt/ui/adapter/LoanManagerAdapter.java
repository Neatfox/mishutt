package com.neatfox.mishutt.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.activity.CheckLoanActivity;
import com.neatfox.mishutt.ui.model.LoanType;

import java.util.List;

public class LoanManagerAdapter extends BaseExpandableListAdapter {

    private final List<LoanType> loan_list;
    private final Context context;

    public LoanManagerAdapter(Context context, List<LoanType> loan_list) {
        this.context = context;
        this.loan_list = loan_list;
    }

    @Override
    public int getGroupCount() {
        return loan_list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return loan_list.get(groupPosition).getLoan().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return loan_list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return loan_list.get(groupPosition).getLoan().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_list_header, null);
        }

        TextView loan_header = convertView.findViewById(R.id.tv_header);
        loan_header.setText(loan_list.get(groupPosition).getType());

        ImageView arrow = convertView.findViewById(R.id.iv_arrow);

        if (isExpanded){ arrow.setRotation(270); } else{ arrow.setRotation(90); }

        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_loan_manager, null);
        }
        TextView bank_name = convertView.findViewById(R.id.tv_bank_name);
        TextView interest_rate = convertView.findViewById(R.id.tv_interest_rate);
        TextView processing_fee = convertView.findViewById(R.id.tv_processing_fee);
        TextView loan_amount = convertView.findViewById(R.id.tv_loan_amount);
        TextView tenure = convertView.findViewById(R.id.tv_tenure);

        String min_interest_rate = loan_list.get(groupPosition).getLoan().get(childPosition).getMin_interest_rate();
        String max_interest_rate = loan_list.get(groupPosition).getLoan().get(childPosition).getMax_interest_rate();
        String _interest_rate;
        if ("null".equalsIgnoreCase(max_interest_rate) || "".equalsIgnoreCase(max_interest_rate))
            _interest_rate = min_interest_rate;
        else
            _interest_rate = (min_interest_rate +" - "+max_interest_rate);

        String min_loan_amount = loan_list.get(groupPosition).getLoan().get(childPosition).getMin_loan_amount();
        String max_loan_amount = loan_list.get(groupPosition).getLoan().get(childPosition).getMax_loan_amount();
        String _loan_amount;
        if ("null".equalsIgnoreCase(max_loan_amount) || "".equalsIgnoreCase(max_loan_amount))
            _loan_amount = min_loan_amount;
        else
            _loan_amount = (min_loan_amount +" - "+max_loan_amount);

        String min_tenure = loan_list.get(groupPosition).getLoan().get(childPosition).getMin_tenure();
        String max_tenure = loan_list.get(groupPosition).getLoan().get(childPosition).getMax_tenure();
        String _tenure;
        if ("null".equalsIgnoreCase(max_tenure) || "".equalsIgnoreCase(max_tenure))
            _tenure = min_tenure;
        else
            _tenure = (min_tenure +" - "+max_tenure);

        bank_name.setText(loan_list.get(groupPosition).getLoan().get(childPosition).getBank_name());
        interest_rate.setText(_interest_rate);
        processing_fee.setText(loan_list.get(groupPosition).getLoan().get(childPosition).getProcessing_fee());
        loan_amount.setText(_loan_amount);
        tenure.setText(_tenure);

        Button more = convertView.findViewById(R.id.button_more);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, CheckLoanActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("type","Loan Manager");
                intent.putExtra("item_name",loan_list.get(groupPosition).getLoan().get(childPosition).getLoan_type());
                context.startActivity(intent);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
