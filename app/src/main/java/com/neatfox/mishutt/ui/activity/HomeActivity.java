package com.neatfox.mishutt.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.fragment.HomeFragment;

public class HomeActivity extends MainActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    FragmentManager fragmentManager;
    int backPress=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_home, contentFrameLayout);
        HomeActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        /*........................................Fragment........................................*/
        fragmentManager = this.getSupportFragmentManager();
        Fragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.frame_layout, homeFragment).addToBackStack("home").commit();
        /*...................................Pull to Refresh......................................*/
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    noNetwork();
                } else {
                    Fragment homeFragment = new HomeFragment();
                    fragmentManager.beginTransaction().replace(R.id.frame_layout, homeFragment).addToBackStack("home").commit();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });
        /*....................................Bottom Navigation...................................*/
        bottom_navigation_id = 3;
        bottomNavigation.show(bottom_navigation_id,false);
    }
    /*......................................For BackPress.........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        } else {
            finishAffinity();
        }
    }
}
