package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.neatfox.mishutt.R;

import static com.neatfox.mishutt.Constants.basePath;

public class ShowImageActivity extends AppCompatActivity {

    LinearLayout layout_back;
    ImageButton ib_back;
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        TextView toolbar_title = findViewById(R.id.tv_toolbar_title);
        ImageView imageView = findViewById(R.id.iv_image);

        toolbar_title.setText(getIntent().getStringExtra("title"));
        String image_string = getIntent().getStringExtra("image");
        String image_path;
        if (image_string.contains(basePath))
            image_path = getIntent().getStringExtra("image");
        else
            image_path = basePath + getIntent().getStringExtra("image");

        if  (image_string.trim().length() < 5 ) {
            imageView.setImageResource(R.drawable.ic_profile_image);
        } else {
            Glide.with(ShowImageActivity.this).load(image_path)
                    .apply(new RequestOptions().override(1080, 1080)).into(imageView);
        }

        /*..................................Back To Previous Page.................................*/
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            backPressed();
        } else {
            finishAffinity();
        }
    }

    public void backPressed(){
        Intent intent;
        if ("Profile".equalsIgnoreCase(getIntent().getStringExtra("type"))){
            intent = new Intent (ShowImageActivity.this, DashboardActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("type","Profile");
            ShowImageActivity.this.startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }

    }
}
