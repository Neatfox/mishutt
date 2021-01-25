package com.neatfox.mishutt.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.model.ImageSlider;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

import static com.neatfox.mishutt.Constants.basePath;
import static com.neatfox.mishutt.Constants.imageMiddlePath;

public class ImageSliderBannerAdapter extends SliderViewAdapter<ImageSliderBannerAdapter.ViewHolder> {

    private final Context context;
    ArrayList<ImageSlider> image_list;

    public ImageSliderBannerAdapter(Context context, ArrayList<ImageSlider> image_list) {
        this.context = context;
        this.image_list = image_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_image_slider_banner,viewGroup,false);
        return new ViewHolder(view, context, image_list);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ImageSlider imageSlider = image_list.get(position);
        Glide.with(context)
                .load(basePath+imageMiddlePath+imageSlider.getImage_path())
                .apply(new RequestOptions().override(1366, 768))
                .into(holder.imageView);
    }

    @Override
    public int getCount() {
        return image_list.size();
    }

    static class ViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        Context context;
        ArrayList<ImageSlider> image_list;
        ImageView imageView;

        ViewHolder(View itemView, Context context, ArrayList<ImageSlider> image_list) {
            super(itemView);
            this.itemView = itemView;
            this.context = context;
            this.image_list = image_list;
            imageView = itemView.findViewById(R.id.iv_slider_image);
        }
    }
}