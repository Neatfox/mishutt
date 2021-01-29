package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.ui.adapter.ImageSliderSplashScreenAdapter;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import static com.neatfox.mishutt.Constants.REQUEST_SMS_PERMISSION;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    CoordinatorLayout layout,layout_slider,layout_loading;
    FloatingActionButton fab_next;
    long milliseconds = 2000;
    int backPress = 0;

    final int [] slider_image = {
            R.drawable.partnership_handshake,
            R.drawable.money_eligibility,
            R.drawable.intrusion,
            R.drawable.mishutt,
    };

    final String[] slider_text = {
            "75+ Partners & Over 300 Products",
            "Do Your financial eligibility Check",
            "Secured systems to keep your data safe",
            "A Company You can rely upon\n\nMost unique fintech to solve\nyour all financial need"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_slider = findViewById(R.id.layout_slider);
        layout_loading = findViewById(R.id.layout_loading);
        fab_next = findViewById(R.id.fab_next);
        layout_slider.setVisibility(View.GONE);
        layout_loading.setVisibility(View.GONE);

        final SliderView sliderView = findViewById(R.id.imageSlider);
        ImageSliderSplashScreenAdapter adapter = new ImageSliderSplashScreenAdapter(SplashScreenActivity.this,slider_image,slider_text);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.SWAP);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setScrollTimeInSec(5); //set scroll delay in seconds

        if (ContextCompat.checkSelfPermission(SplashScreenActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED){
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
            builder.setTitle("Mishutt needs access to SMS")
                    .setMessage("Mishutt auto-organises your earnings & expenses by reading your business SMS." +
                            "\nNo personal SMS are read in any circumstances.")
                    .setCancelable(false)
                    .setIcon(R.drawable.mishutt)
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions( SplashScreenActivity.this,
                                    new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finishAffinity();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            showLayout();
        }

        fab_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = sliderView.getCurrentPagePosition();
                System.out.println(i);
                if (i == 3){
                    layout_slider.setVisibility(View.GONE);
                    layout_loading.setVisibility(View.VISIBLE);
                    milliseconds = 0;
                    runInBackground();
                } else {
                    sliderView.setCurrentPagePosition(i+1);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SMS_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                System.out.println("Permission Granted");
               showLayout();
            } else {
                finishAffinity();
            }
        }
    }

    private void showLayout(){
        if (sharedPreference.getString("isLoggedIn", "").equals("1")) {
            layout_slider.setVisibility(View.GONE);
            layout_loading.setVisibility(View.VISIBLE);
            runInBackground();
        } else {
            layout_slider.setVisibility(View.VISIBLE);
            layout_loading.setVisibility(View.GONE);
        }
    }

    private void runInBackground(){
        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(milliseconds);
                    if (sharedPreference.getString("isLoggedIn", "").equals("1")) {
                        startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                        finish();
                    } else {
                        editor.clear();
                        editor.commit();
                        startActivity(new Intent(SplashScreenActivity.this, SignUpActivity.class));
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        background.start();
    }

    /*.......................................For BackPress........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            Snackbar.make(layout, R.string.press_again_to_exit, Snackbar.LENGTH_SHORT).show();
        } else {
            finishAffinity();
        }
    }
}
