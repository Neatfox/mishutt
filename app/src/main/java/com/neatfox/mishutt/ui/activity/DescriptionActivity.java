package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.neatfox.mishutt.R;

public class DescriptionActivity extends MainActivity {

    TextView title,description,credit_score;
    int backPress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_description, contentFrameLayout);

        DescriptionActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        title = findViewById(R.id.tv_title);
        description = findViewById(R.id.tv_description);
        credit_score = findViewById(R.id.tv_credit_score);
        tv_toolbar.setText(getIntent().getStringExtra("item_name"));
        title.setText(getIntent().getStringExtra("item_name"));
        description.setText(getIntent().getStringExtra("item_description"));

        if ("CIBIL".equalsIgnoreCase(title.getText().toString().trim()))
            credit_score.setVisibility(View.VISIBLE);
        else
            credit_score.setVisibility(View.GONE);

        credit_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( DescriptionActivity.this, CreditScoreActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                DescriptionActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
    }
    /*......................................For BackPress.........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            showHome();
        } else {
            finishAffinity();
        }
    }

    public void backPressed(){
        Intent intent = new Intent (DescriptionActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        DescriptionActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
