package com.neatfox.mishutt.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.model.Card;

import java.util.ArrayList;

import static com.neatfox.mishutt.Constants.basePath;
import static com.neatfox.mishutt.Constants.imageMiddlePath;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    ArrayList<Card> card_list;
    Context context;
    Activity activity;

    public CardAdapter(ArrayList<Card> card_list, Context context, Activity activity) {
        this.card_list = card_list;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_card, parent, false);
        return new ViewHolder(view, context, card_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Card card = card_list.get(position);

        String image_path = basePath + imageMiddlePath + card.getImage();
        Glide.with(context).load(image_path).apply(new RequestOptions().override(720, 720)).into(holder.imageView);

        holder.bank_name.setText(card.getBank_name());
        holder.card_name.setText(card.getCard_name());
        holder.fee.setText(card.getFirst_year_fee());

        if (Build.VERSION.SDK_INT >= 24){
            holder.rewards.setText(Html.fromHtml(card.getRewards(), Html.FROM_HTML_MODE_LEGACY));
            holder.joining_perks.setText(Html.fromHtml(card.getJoining_perks(), Html.FROM_HTML_MODE_LEGACY));
            holder.documents.setText(Html.fromHtml(card.getDocuments(), Html.FROM_HTML_MODE_LEGACY));
            holder.fee_details.setText(Html.fromHtml(card.getFee_details(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.rewards.setText(Html.fromHtml(card.getRewards()));
            holder.joining_perks.setText(Html.fromHtml(card.getJoining_perks()));
            holder.documents.setText(Html.fromHtml(card.getDocuments()));
            holder.fee_details.setText(Html.fromHtml(card.getFee_details()));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });
    }

    @Override
    public int getItemCount() {
        return card_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Card> card_list;
        ImageView imageView;
        TextView bank_name,card_name,fee,rewards,joining_perks,documents,fee_details;

        ViewHolder(View itemView, Context context, ArrayList<Card> card_list) {
            super(itemView);
            this.context = context;
            this.card_list = card_list;
            imageView = itemView.findViewById(R.id.iv_image);
            bank_name = itemView.findViewById(R.id.tv_bank_name);
            card_name = itemView.findViewById(R.id.tv_card_name);
            fee = itemView.findViewById(R.id.tv_fee);
            rewards = itemView.findViewById(R.id.tv_rewards);
            joining_perks = itemView.findViewById(R.id.tv_joining_perks);
            documents = itemView.findViewById(R.id.tv_documents);
            fee_details = itemView.findViewById(R.id.tv_fee_details);
        }
    }
}
