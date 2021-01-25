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

public class ImageSliderSplashScreenAdapter extends SliderViewAdapter<ImageSliderSplashScreenAdapter.ViewHolder> {

    private final Context context;
    private final int [] image;
    private final String [] text;

    public ImageSliderSplashScreenAdapter(Context context, int[] image, final String [] text) {
        this.context = context;
        this.image = image;
        this.text = text;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_image_slider_splash_screen,viewGroup,false);
        return new ViewHolder(view, context, image, text);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {
        Glide.with(context).load(image[i]).apply(new RequestOptions().override(1366, 768)).into(holder.imageView);
        holder.textView.setText(text[i]);
    }

    @Override
    public int getCount() {
        return image.length;
    }

    static class ViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        Context context;
        int [] image;
        String [] text;
        ImageView imageView;
        TextView textView;

        ViewHolder(View itemView, Context context, int[] image, String [] text) {
            super(itemView);
            this.itemView = itemView;
            this.context = context;
            this.image = image;
            this.text = text;
            imageView = itemView.findViewById(R.id.iv_slider_image);
            textView = itemView.findViewById(R.id.tv_slider_text);
        }
    }
}