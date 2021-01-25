package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.adapter.ViewPagerAdapter;
import com.neatfox.mishutt.ui.fragment.GoalFragment;

public class GoalManagerActivity extends MainActivity {

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    FloatingActionButton fab_add;
    int backPress=0;
    int[] colorIntArray = {R.color.green,R.color.green,R.color.green};
    int[] iconIntArray = {R.drawable.fab_add,R.drawable.fab_add,R.drawable.fab_add};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_goal_manager, contentFrameLayout);
        tv_toolbar.setText(R.string.goal_manager);

        GoalManagerActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewPager = findViewById(R.id.viewPager);
        fab_add = findViewById(R.id.fab_add);
        fab_add.setVisibility(View.VISIBLE);
        animateFab();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.AddFragment(new GoalFragment(),"");
        viewPager.setAdapter(viewPagerAdapter);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (GoalManagerActivity.this, AddGoalActivity.class);
                intent.putExtra("annual_income",_annual_income);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                GoalManagerActivity.this.startActivity(intent);
            }
        });
    }
    /*..................................Animate Floating Action Button............................*/
    protected void animateFab() {
        fab_add.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                fab_add.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(),colorIntArray[0]));
                //fab_add.setImageDrawable(getResources().getDrawable(iconIntArray[position], null)); // Applicable from API 21
                fab_add.setImageResource(iconIntArray[0]);

                // Scale up animation
                ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                fab_add.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        fab_add.startAnimation(shrink);
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
        Intent intent = new Intent (GoalManagerActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        GoalManagerActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
