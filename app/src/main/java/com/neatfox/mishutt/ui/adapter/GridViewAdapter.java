package com.neatfox.mishutt.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neatfox.mishutt.R;

import java.util.Objects;

public class GridViewAdapter extends BaseAdapter {

    private final Context context;
    private final int [] image;
    private final String [] imageTitle;

    public GridViewAdapter(int[] image, String[] imageTitle, Context context){
        this.context = context;
        this.image = image;
        this.imageTitle = imageTitle;
    }

    @Override
    public int getCount() {
        return image.length;
    }

    @Override
    public Object getItem(int position) {
        return image[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        if (convertView == null) {
            view = Objects.requireNonNull(inflater).inflate(R.layout.adapter_grid_view, null);
            TextView message = view.findViewById(R.id.tv_message);
            TextView title = view.findViewById(R.id.tv_grid_text);
            ImageView imageView = view.findViewById(R.id.iv_grid_image);
            title.setText(imageTitle[position]);
            imageView.setImageResource(image[position]);
            message.setText(R.string.string_new);
            if ("Student Finance".equalsIgnoreCase(title.getText().toString().trim()) ||
                    "FASTag".equalsIgnoreCase(title.getText().toString().trim()))
                message.setVisibility(View.VISIBLE);
            else
                message.setVisibility(View.INVISIBLE);
        } else {
            view = convertView;
        }
        return view;
    }
}