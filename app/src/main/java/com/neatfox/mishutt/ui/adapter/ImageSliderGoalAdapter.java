package com.neatfox.mishutt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.neatfox.mishutt.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class ImageSliderGoalAdapter extends SliderViewAdapter<ImageSliderGoalAdapter.ViewHolder> {

    private final Context context;
    private final int [] image_first,image_second;
    private final String [] text_header_first,text_header_second;
    private final String [] text_description_first,text_description_second;

    public ImageSliderGoalAdapter(Context context, int[] image_first, String[] text_header_first,
                                  String [] text_description_first, int[] image_second, String[] text_header_second,
                                  String [] text_description_second) {
        this.context = context;
        this.image_first = image_first;
        this.text_header_first = text_header_first;
        this.text_description_first = text_description_first;
        this.image_second = image_second;
        this.text_header_second = text_header_second;
        this.text_description_second = text_description_second;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_image_slider_goal,viewGroup,false);
        return new ViewHolder(view, context, image_first, text_header_first, text_description_first,
                image_second,text_header_second,text_description_second);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        Glide.with(context).load(image_first[i]).apply(new RequestOptions().override(1366, 768)).into(holder.imageView_first);
        holder.textView_header_first.setText(text_header_first[i]);
        holder.textView_description_first.setText(text_description_first[i]);

        Glide.with(context).load(image_second[i]).apply(new RequestOptions().override(1366, 768)).into(holder.imageView_second);
        holder.textView_header_second.setText(text_header_second[i]);
        holder.textView_description_second.setText(text_description_second[i]);
    }

    @Override
    public int getCount() {
        return image_first.length;
    }

    static class ViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        Context context;
        int [] image_first,image_second;
        String [] text_header_first,text_header_second;
        String [] text_description_first,text_description_second;
        ImageView imageView_first,imageView_second;
        TextView textView_header_first,textView_description_first,
                textView_header_second,textView_description_second;

        ViewHolder(View itemView, Context context, int[] image_first, String [] text_header_first, String [] text_description_first,
                   int[] image_second, String[] text_header_second, String [] text_description_second) {
            super(itemView);
            this.itemView = itemView;
            this.context = context;
            this.image_first = image_first;
            this.text_header_first = text_header_first;
            this.text_description_first = text_description_first;
            this.image_second = image_second;
            this.text_header_second = text_header_second;
            this.text_description_second = text_description_second;
            imageView_first = itemView.findViewById(R.id.iv_slider_image_first);
            imageView_second = itemView.findViewById(R.id.iv_slider_image_second);
            textView_header_first = itemView.findViewById(R.id.tv_slider_text_header_first);
            textView_description_first = itemView.findViewById(R.id.tv_slider_text_description_first);
            textView_header_second = itemView.findViewById(R.id.tv_slider_text_header_second);
            textView_description_second = itemView.findViewById(R.id.tv_slider_text_description__second);
        }
    }
}