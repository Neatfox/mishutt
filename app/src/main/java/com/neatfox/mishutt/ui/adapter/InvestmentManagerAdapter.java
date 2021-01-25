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
import com.neatfox.mishutt.ui.activity.CheckInvestmentActivity;
import com.neatfox.mishutt.ui.model.InvestmentType;

import java.util.List;

public class InvestmentManagerAdapter extends BaseExpandableListAdapter {

    private final List<InvestmentType> investment_list;
    private final Context context;

    public InvestmentManagerAdapter(Context context, List<InvestmentType> investment_list) {
        this.context = context;
        this.investment_list = investment_list;
    }

    @Override
    public int getGroupCount() {
        return investment_list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return investment_list.get(groupPosition).getInvestment().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return investment_list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return investment_list.get(groupPosition).getInvestment().get(childPosition);
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
        loan_header.setText(investment_list.get(groupPosition).getType());

        ImageView arrow = convertView.findViewById(R.id.iv_arrow);

        if (isExpanded){ arrow.setRotation(270); } else{ arrow.setRotation(90); }

        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_investment_manager, null);
        }
        TextView scheme_name = convertView.findViewById(R.id.tv_scheme_name);
        TextView investment_amount = convertView.findViewById(R.id.tv_investment_amount);
        TextView aum = convertView.findViewById(R.id.tv_aum);

        scheme_name.setText(investment_list.get(groupPosition).getInvestment().get(childPosition).getScheme_name());

        String min_investment_amount = investment_list.get(groupPosition).getInvestment().get(childPosition).getMin_invest_amount();
        String max_investment_amount = investment_list.get(groupPosition).getInvestment().get(childPosition).getMax_invest_amount();
        String _investment_amount;
        if ("null".equalsIgnoreCase(max_investment_amount) || "".equalsIgnoreCase(max_investment_amount)) {
            if ("".equalsIgnoreCase(min_investment_amount) || "null".equalsIgnoreCase(min_investment_amount))
                _investment_amount = " - ";
            else
                _investment_amount = "₹"+(min_investment_amount);
        }
        else
            _investment_amount = ("₹"+(min_investment_amount) +" - "+"₹"+(max_investment_amount));

        investment_amount.setText(_investment_amount);

        String _aum = investment_list.get(groupPosition).getInvestment().get(childPosition).getAum();

        if ("null".equalsIgnoreCase(_aum) || "".equalsIgnoreCase(_aum)){
            aum.setText(" - ");
        } else {
            aum.setText(String.format("₹%s",(_aum)));
        }

        Button more = convertView.findViewById(R.id.button_more);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, CheckInvestmentActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("type","Investment Manager");
                intent.putExtra("item_name",investment_list.get(groupPosition).getInvestment().get(childPosition).getInvestment_type());
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
