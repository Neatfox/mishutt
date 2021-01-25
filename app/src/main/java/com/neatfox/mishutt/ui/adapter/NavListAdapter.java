package com.neatfox.mishutt.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neatfox.mishutt.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NavListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private final HashMap<String, List<String>> listDataChild;

    public NavListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(this.listDataChild.get(this.listDataHeader.get(groupPosition)))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_nav_list_item, null);
        }

        TextView listChild = convertView.findViewById(R.id.tv_nav_list_item);
        listChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(this.listDataChild.get(this.listDataHeader.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_nav_list_header, null);
        }

        TextView listHeader = convertView.findViewById(R.id.tv_nav_list_header);
        listHeader.setText(headerTitle);

         ImageView arrow = convertView.findViewById(R.id.iv_arrow);

        if ("Credit Score".equalsIgnoreCase(headerTitle) || "About Us".equalsIgnoreCase(headerTitle) ||
                "Terms & Conditions".equalsIgnoreCase(headerTitle) || "Privacy Policy".equalsIgnoreCase(headerTitle) ||
                "Sign Out".equalsIgnoreCase(headerTitle) ||
                headerTitle.contains("Version"))
            arrow.setVisibility(View.GONE);
        else
            arrow.setVisibility(View.VISIBLE);

        if (headerTitle.contains("Version")){
            listHeader.setPadding(0,50,0,50);
            listHeader.setGravity(Gravity.CENTER);
        } else {
            listHeader.setPadding(0,0,0,0);
            listHeader.setGravity(Gravity.START);
        }

        if (isExpanded){
            arrow.setRotation(270);
        } else{
            arrow.setRotation(90);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

