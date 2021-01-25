package com.neatfox.mishutt.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.model.PortfolioCategory;

import java.util.List;

import static com.neatfox.mishutt.Constants.addCommaString;

public class PortfolioAdapter extends BaseExpandableListAdapter {

    private final List<PortfolioCategory> portfolio_list;
    private final Context context;

    public PortfolioAdapter(Context context, List<PortfolioCategory> portfolio_list) {
        this.context = context;
        this.portfolio_list = portfolio_list;
    }

    @Override
    public int getGroupCount() {
        return portfolio_list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return portfolio_list.get(groupPosition).getPortfolio().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return portfolio_list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return portfolio_list.get(groupPosition).getPortfolio().get(childPosition);
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

        TextView portfolio_header = convertView.findViewById(R.id.tv_header);
        portfolio_header.setText(portfolio_list.get(groupPosition).getCategory());

        ImageView arrow = convertView.findViewById(R.id.iv_arrow);

        if (isExpanded){ arrow.setRotation(270); } else{ arrow.setRotation(90); }

        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_portfolio_item, null);
        }
        LinearLayout layout_invest = convertView.findViewById(R.id.layout_invest);
        LinearLayout layout_withdraw = convertView.findViewById(R.id.layout_withdraw);
        LinearLayout layout_profit = convertView.findViewById(R.id.layout_profit);
        LinearLayout layout_loss = convertView.findViewById(R.id.layout_loss);
        TextView product_name = convertView.findViewById(R.id.tv_product_name);
        TextView invest = convertView.findViewById(R.id.tv_invest);
        TextView withdraw = convertView.findViewById(R.id.tv_withdraw);
        TextView profit = convertView.findViewById(R.id.tv_profit);
        TextView loss = convertView.findViewById(R.id.tv_loss);

        if ("0".equalsIgnoreCase(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getCredit())){
            layout_invest.setVisibility(View.GONE);
            layout_withdraw.setVisibility(View.VISIBLE);
        } else {
            layout_invest.setVisibility(View.VISIBLE);
            layout_withdraw.setVisibility(View.GONE);
        }

        if ("0".equalsIgnoreCase(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getDebit())){
            layout_invest.setVisibility(View.VISIBLE);
            layout_withdraw.setVisibility(View.GONE);
        } else {
            layout_invest.setVisibility(View.GONE);
            layout_withdraw.setVisibility(View.VISIBLE);
        }

        if ("0".equalsIgnoreCase(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getProfit())){
            layout_profit.setVisibility(View.GONE);
            layout_loss.setVisibility(View.VISIBLE);
        } else {
            layout_profit.setVisibility(View.VISIBLE);
            layout_loss.setVisibility(View.GONE);
        }

        if ("0".equalsIgnoreCase(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getLoss())){
            layout_profit.setVisibility(View.VISIBLE);
            layout_loss.setVisibility(View.GONE);
        } else {
            layout_profit.setVisibility(View.GONE);
            layout_loss.setVisibility(View.VISIBLE);
        }

        product_name.setText(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getProduct_name());
        invest.setText(String.format("₹%s", addCommaString(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getCredit())));
        withdraw.setText(String.format("₹%s", addCommaString(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getDebit())));
        profit.setText(String.format("₹%s", addCommaString(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getProfit())));
        loss.setText(String.format("₹%s", addCommaString(portfolio_list.get(groupPosition).getPortfolio().get(childPosition).getLoss())));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
