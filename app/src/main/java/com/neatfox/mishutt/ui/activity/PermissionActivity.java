package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;

import static com.neatfox.mishutt.Constants.REQUEST_LOCATION_PERMISSION;
import static com.neatfox.mishutt.Constants.REQUEST_SMS_PERMISSION;

public class PermissionActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    CoordinatorLayout layout;
    ScrollView scrollView;
    TextView terms_privacy,exit,agree;
    ProgressBar loading;
    int backPress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        scrollView = findViewById(R.id.scrollView);
        terms_privacy = findViewById(R.id.tv_terms_privacy);
        exit = findViewById(R.id.tv_exit);
        agree = findViewById(R.id.tv_agree);
        loading = findViewById(R.id.loading);

        scrollView.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);

        Spanned html = Html.fromHtml("By clicking 'I Agree' you agree to Mishutt's <a href='https://mishutt.com/Home/Terms'>terms of use</a> " +
                "and <a href='https://mishutt.com/Home/Privacy'>privacy policy</a>.");
        terms_privacy.setText(html);


        if (ContextCompat.checkSelfPermission(PermissionActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED){
            scrollView.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        } else {
            checkLocationPermission();
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions( PermissionActivity.this,
                        new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
            }
        });

    }

    private void checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(PermissionActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions( PermissionActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            redirect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SMS_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                System.out.println("Permission Granted");
            }
            checkLocationPermission();

        } else if (requestCode == REQUEST_LOCATION_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                System.out.println("Permission Granted");
                checkLocationPermission();
            }
            redirect();
        }
    }

    private void redirect(){
        if (sharedPreference.getString("isLoggedIn", "").equals("1")) {
            startActivity(new Intent(PermissionActivity.this, HomeActivity.class));
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            finish();
        } else {
            editor.clear();
            editor.commit();
            startActivity(new Intent(PermissionActivity.this, SignUpActivity.class));
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
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
