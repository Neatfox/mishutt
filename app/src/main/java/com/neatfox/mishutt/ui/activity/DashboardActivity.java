package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.android.material.tabs.TabLayout;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.adapter.ViewPagerAdapter;
import com.neatfox.mishutt.ui.fragment.PortfolioFragment;
import com.neatfox.mishutt.ui.fragment.ProductFragment;
import com.neatfox.mishutt.ui.fragment.ProfileFragment;

public class DashboardActivity extends MainActivity {

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    FloatingActionButton fab_add;
    String type = "";
    int backPress=0,position = 0;
    int[] colorIntArray = {R.color.green,R.color.green,R.color.green};
    int[] iconIntArray = {R.drawable.ic_edit,R.drawable.fab_add,R.drawable.fab_add};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_dashboard, contentFrameLayout);
        tv_toolbar.setText(R.string.dashboard);

        DashboardActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        fab_add = findViewById(R.id.fab_add);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        /*.......................................Tab Layout.......................................*/
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.AddFragment(new ProfileFragment(),"My Profile");
        viewPagerAdapter.AddFragment(new ProductFragment(),"My Product(s)");
        viewPagerAdapter.AddFragment(new PortfolioFragment(),"My Portfolio");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.parseColor("#27AE60"),Color.parseColor("#27AE60"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                position = tab.getPosition();

                if (tab.getPosition() == 0) {
                    fab_add.setVisibility(View.VISIBLE);
                    animateFab(tab.getPosition());

                } else if (tab.getPosition() == 1){
                    fab_add.setVisibility(View.VISIBLE);
                    animateFab(tab.getPosition());

                } else if (tab.getPosition() == 2){
                    fab_add.setVisibility(View.VISIBLE);
                    animateFab(tab.getPosition());

                } else {
                    fab_add.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        fab_add.setVisibility(View.VISIBLE);
        animateFab(0);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    Intent intent = new Intent (DashboardActivity.this, AddProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    DashboardActivity.this.startActivity(intent);
                } else if (position == 1) {
                    Intent intent = new Intent (DashboardActivity.this, AddProductActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    DashboardActivity.this.startActivity(intent);
                } else if (position == 2) {
                    Intent intent = new Intent (DashboardActivity.this, AddPortfolioActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    DashboardActivity.this.startActivity(intent);
                }
            }
        });

        type = getIntent().getStringExtra("type");
        if ("Profile".equalsIgnoreCase(type))
            setTab(0);
        else if ("Product".equalsIgnoreCase(type))
            setTab(1);
        else if ("Portfolio".equalsIgnoreCase(type))
            setTab(2);
        /*....................................Bottom Navigation...................................*/
        bottom_navigation_id = 2;
        bottomNavigation.show(bottom_navigation_id,false);
    }

    public void setTab(int position){
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.select();
    }
    /*..................................Animate Floating Action Button............................*/
    protected void animateFab(final int position) {
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
                fab_add.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(),colorIntArray[position]));
                //fab_add.setImageDrawable(getResources().getDrawable(iconIntArray[position], null)); // Applicable from API 21
                fab_add.setImageResource(iconIntArray[position]);

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
}
