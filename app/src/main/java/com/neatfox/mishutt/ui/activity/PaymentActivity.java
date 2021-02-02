package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.GridViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.REQUEST_LOCATION_PERMISSION;
import static com.neatfox.mishutt.Constants.REQUEST_LOCATION_SETTINGS;
import static com.neatfox.mishutt.Constants.api_add_onboarding_flag;
import static com.neatfox.mishutt.Constants.api_add_payment_flag;
import static com.neatfox.mishutt.Constants.api_onboarding_otp_send;
import static com.neatfox.mishutt.Constants.api_onboarding_otp_validate;
import static com.neatfox.mishutt.Constants.api_user_add;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class PaymentActivity extends MainActivity {

    LinearLayout layout_gridView;
    GridView gridViewRecharge,gridViewBillPayment;
    GridViewAdapter adapterRecharge,adapterBillPayment;
    ShimmerFrameLayout mShimmerViewContainerOne,mShimmerViewContainerTwo;
    WebView webView;
    ProgressBar loading;
    ImageView action_cancel;
    TextView email_id,mobile_number;
    EditText one,two,three,four,five,six;
    Button resend,submit;
    Dialog dialog_otp;
    ProgressDialog progressDialog;
    static final long START_TIME_IN_MILLIS = 120000;
    long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    String verification_code = "";
    String TAG = "LOCATION";
    boolean has_location_permission = false;
    String _page_name;
    boolean flag = false;
    int backPress = 0;

    final String[] recharge_string = { "Mobile Prepaid", "DTH", "FASTag"};

    final int[] recharge_image = {
            R.drawable.smartphone,
            R.drawable.satellite_tv,
            R.drawable.toll_road,
    };

    final String[] bill_payment_string = { "Mobile Postpaid", "Landline", "Broadband",
            "Electricity", "Gas", "Municipal & Water"};

    final int[] bill_payment_image = {
            R.drawable.smartphone,
            R.drawable.landline,
            R.drawable.router,
            R.drawable.light_bulb,
            R.drawable.gas,
            R.drawable.water_tap
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_payment, contentFrameLayout);
        tv_toolbar.setText(R.string.bill_payments);

        PaymentActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        layout_gridView = findViewById(R.id.layout_gridView);
        gridViewRecharge = findViewById(R.id.grid_view_recharge);
        gridViewBillPayment = findViewById(R.id.grid_view_bill_payment);
        mShimmerViewContainerOne = findViewById(R.id.shimmer_view_container_one);
        mShimmerViewContainerTwo = findViewById(R.id.shimmer_view_container_two);
        webView = findViewById(R.id.webView);
        loading = findViewById(R.id.loading);

        layout_gridView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        adapterRecharge = new GridViewAdapter(recharge_image, recharge_string, PaymentActivity.this);
        adapterBillPayment = new GridViewAdapter(bill_payment_image, bill_payment_string, PaymentActivity.this);

        mShimmerViewContainerOne.setVisibility(View.VISIBLE);
        mShimmerViewContainerTwo.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                gridViewRecharge.setAdapter(adapterRecharge);
                setDynamicHeight(gridViewRecharge);

                gridViewBillPayment.setAdapter(adapterBillPayment);
                setDynamicHeight(gridViewBillPayment);
                // stop animating Shimmer and hide the layout
                mShimmerViewContainerOne.stopShimmer();
                mShimmerViewContainerOne.setVisibility(View.GONE);

                mShimmerViewContainerTwo.stopShimmer();
                mShimmerViewContainerTwo.setVisibility(View.GONE);
            }
        },1000);

        gridViewRecharge.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (flag){
                    if (checkPermission()){
                        displayLocationSettingsRequest(PaymentActivity.this);
                        hideLayout();
                        _page_name = recharge_string[+position];
                        loadWebView();
                    }
                } else {
                    checkFlag();
                }
            }
        });

        gridViewBillPayment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (flag){
                    if (checkPermission()){
                        displayLocationSettingsRequest(PaymentActivity.this);
                        hideLayout();
                        _page_name = bill_payment_string[+position];
                        loadWebView();
                    }
                } else {
                    checkFlag();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    if ("Mobile Prepaid".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/prepaid");
                    else if ("DTH".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/dth");
                    else if ("FASTag".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/fastag");
                    else if ("Mobile Postpaid".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/postpaid");
                    else if ("Landline".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/landline");
                    else if ("Broadband".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/broadband");
                    else if ("Electricity".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/electricity");
                    else if ("Gas".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/gas");
                    else if ("Municipal & Water".equalsIgnoreCase(_page_name))
                        webView.loadUrl("https://prime.billdesks.in/utility/payments/water");
                    _page_name = "";
                }
            }
        });

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
            }
        }
        /*.......................................Dialog OTP.......................................*/
        dialog_otp = new Dialog(PaymentActivity.this);
        dialog_otp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_otp.setContentView(R.layout.dialog_otp);
        dialog_otp.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog_otp.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog_otp.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog_otp.getWindow().setAttributes(windowParams);

        action_cancel = dialog_otp.findViewById(R.id.iv_cancel_action);
        email_id = dialog_otp.findViewById(R.id.tv_email_id);
        mobile_number = dialog_otp.findViewById(R.id.tv_mobile_number);
        one = dialog_otp.findViewById(R.id.edit_text_one);
        two = dialog_otp.findViewById(R.id.edit_text_two);
        three = dialog_otp.findViewById(R.id.edit_text_three);
        four = dialog_otp.findViewById(R.id.edit_text_four);
        five = dialog_otp.findViewById(R.id.edit_text_five);
        six = dialog_otp.findViewById(R.id.edit_text_six);
        resend = dialog_otp.findViewById(R.id.button_resend);
        submit = dialog_otp.findViewById(R.id.button_submit);

        String sourceStringEmailId = "<b>"+ _email_id + "</b>";
        email_id.setText(Html.fromHtml(sourceStringEmailId));

        String sourceStringMobileNumber = "<b>"+"+91 " + _mobile_number + "</b>";
        mobile_number.setText(Html.fromHtml(sourceStringMobileNumber));

        one.addTextChangedListener(new GenericTextWatcher(one));
        two.addTextChangedListener(new GenericTextWatcher(two));
        three.addTextChangedListener(new GenericTextWatcher(three));
        four.addTextChangedListener(new GenericTextWatcher(four));
        five.addTextChangedListener(new GenericTextWatcher(five));
        six.addTextChangedListener(new GenericTextWatcher(six));

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    noNetwork();
                } else {
                    startTimer();
                    sendOTP();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verification_code.length()<1)
                    Toast.makeText(PaymentActivity.this,R.string.enter_verification_code,Toast.LENGTH_SHORT).show();
                else if (verification_code.length()<6)
                    Toast.makeText(PaymentActivity.this,R.string.verification_code_length_six,Toast.LENGTH_SHORT).show();
                else {
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        verifyOTP();
                    }
                }
            }
        });

        action_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_otp.dismiss();
            }
        });

        checkFlag();

        /*....................................Bottom Navigation...................................*/
        bottom_navigation_id = 1;
        bottomNavigation.show(bottom_navigation_id,false);
    }

    private void checkFlag(){
        if ("N".equalsIgnoreCase(_payment_flag)){
            if (_pan_no.trim().length()<10){
                AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
                builder.setMessage("Add PAN No")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                startActivity(new Intent(PaymentActivity.this, AddProfileActivity.class));
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                if (isNetworkAvailable()) {
                    noNetwork();
                } else {
                    addUser();
                }
            }
        } else if ("N".equalsIgnoreCase(_onboarding_flag)){
            if (isNetworkAvailable()) {
                noNetwork();
            } else {
                dialogOTP();
            }
        } else if ("Y".equalsIgnoreCase(_payment_flag) && "Y".equalsIgnoreCase(_onboarding_flag)){
            if (isNetworkAvailable()) {
                noNetwork();
            } else {
                flag = true;
                layout_gridView.setVisibility(View.VISIBLE);
                if (checkPermission()){
                    displayLocationSettingsRequest(PaymentActivity.this);
                }
                loadWebView();
            }
        }
    }

    private void loadWebView(){
        webView.loadUrl("https://billdesks.in/api/prime_send?sp_key=&pan_no="+_pan_no);
        //webView.loadUrl("https://billdesks.in/api/prime_send?sp_key=&pan_no=BSTPM6647R");
    }

    private void hideLayout(){
        progressDialog = new ProgressDialog(PaymentActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                layout_gridView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
            }
        },5000);
    }

    /*....................................Set Grid View Height....................................*/
    private void setDynamicHeight(GridView gridView) {
        ListAdapter gridViewAdapter = gridView.getAdapter();
        if (gridViewAdapter == null) {
            return;
        }

        int totalHeight;
        int items = gridViewAdapter.getCount();
        int rows;

        View listItem = gridViewAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        int x;
        if( items > 3 ){
            x = items/3;
            rows = x;
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }

    public static class webClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view,url,favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String url){
            view.loadUrl(url);
            return true;
        }
    }

    public void webView(){
        has_location_permission = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainerOne.startShimmer();
        mShimmerViewContainerTwo.startShimmer();
    }

    @Override
    public void onPause() {
        mShimmerViewContainerOne.stopShimmer();
        mShimmerViewContainerTwo.stopShimmer();
        super.onPause();
    }

    /*...............................Permission for Access Location...............................*/
    private boolean checkPermission(){
        if (ContextCompat.checkSelfPermission(PaymentActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions( PaymentActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //APP LOCATION PERMISSION
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                webView();
            } else {
                Toast.makeText(PaymentActivity.this,"Allow app to access location",Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*......................................LOCATION SERVICE......................................*/
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    System.out.println(response);
                    // All location settings satisfied. The client can initialize location requests here.
                    webView();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings not satisfied. Fixed by showing user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),check the result in onActivityResult().
                                resolvable.startResolutionForResult(PaymentActivity.this, REQUEST_LOCATION_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // LOCATION SERVICE
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    webView();
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    break;
            }
        }
    }

    /*......................................Verification Code.....................................*/
    private class GenericTextWatcher implements TextWatcher {
        private final View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            int id = view.getId();

            if (id == R.id.edit_text_one) {
                if (text.length() == 1) two.requestFocus();
            } else if (id == R.id.edit_text_two) {
                if (text.length() == 1) three.requestFocus();
                else if (text.length() == 0) one.requestFocus();
            } else if (id == R.id.edit_text_three) {
                if (text.length() == 1) four.requestFocus();
                else if (text.length() == 0) two.requestFocus();
            } else if (id == R.id.edit_text_four) {
                if (text.length() == 1) five.requestFocus();
                else if (text.length() == 0) three.requestFocus();
            } else if (id == R.id.edit_text_five) {
                if (text.length() == 1) six.requestFocus();
                else if (text.length() == 0) four.requestFocus();
            } else if (id == R.id.edit_text_six) {
                if (text.length() == 0) five.requestFocus();
            }

            String stringOne = one.getText().toString().trim();
            String stringTwo = two.getText().toString().trim();
            String stringThree = three.getText().toString().trim();
            String stringFour = four.getText().toString().trim();
            String stringFive = five.getText().toString().trim();
            String stringSix = six.getText().toString().trim();
            verification_code = stringOne + stringTwo + stringThree + stringFour + stringFive + stringSix;
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
    }
    /*...........................................Timer............................................*/
    public void startTimer(){
        resend.setEnabled(false);
        new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
                int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
                String timeLeftFormat = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                String sResend = "Resend";
                resend.setText(String.format("%s (%s)", sResend, timeLeftFormat));
            }

            @Override
            public void onFinish() {
                resend.setText(R.string.resend);
                resend.setEnabled(true);
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
            }
        }.start();
    }

    public void dialogOTP(){
        dialog_otp.show();

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            startTimer();
            sendOTP();
        }
    }

    public void addUser(){
        loading.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, api_user_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("User Add>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    System.out.println("Add User");
                    paymentFlag();
                } else {
                    System.out.println("Failed to Add User");
                    loading.setVisibility(View.GONE);
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error");
                loading.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("full_name", _name);
                params.put("email_id", _email_id);
                params.put("mobile_no", _mobile_number);
                params.put("address",_address);
                params.put("dob",changeDateFormatDB(_date_of_birth));
                params.put("zip_code", _pin_code);
                params.put("pan_no", _pan_no);
                params.put("aadhar_no", _aadhaar_number);
                Log.d("Add User Params>>>",params.toString());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    public void paymentFlag(){
        StringRequest request = new StringRequest(Request.Method.POST, api_add_payment_flag, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Payment Flag>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loading.setVisibility(View.GONE);
                if (status == 1) {
                    System.out.println("Payment Flag Added");
                    dialogOTP();
                } else {
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*..........................................Send OTP..........................................*/
    public void sendOTP(){
        StringRequest request = new StringRequest(Request.Method.POST, api_onboarding_otp_send, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SendOTP>>>",response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    Snackbar.make(layout, "OTP Sent Successfully", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(layout, "Unable to sent OTP", Snackbar.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mobile_no", _mobile_number);
                Log.d("SendOTP Params>>>",params.toString());
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.........................................Verify OTP.........................................*/
    public void verifyOTP(){
        progressDialog = new ProgressDialog(PaymentActivity.this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_onboarding_otp_validate, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("VerifyOTP>>>",response);
                JSONObject resObj;
                int status = 0;
                String msg = "";
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                    msg = resObj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    System.out.println("OTP Verified");
                    onboardFlag();
                } else {
                    if ("All Fields Are Mandatory".equalsIgnoreCase(msg))
                        Toast.makeText(PaymentActivity.this, "Update Profile Details First", Toast.LENGTH_SHORT).show();
                    else if(msg.contains("PAN already registered"))
                        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(PaymentActivity.this,"OTP Mismatch", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
                progressDialog.dismiss();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("mobile_no", _mobile_number);
                params.put("otp", verification_code.trim());
                params.put("full_name", _name);
                params.put("email_id", _email_id);
                params.put("address",_address);
                params.put("zip_code", _pin_code);
                params.put("pan_no", _pan_no);
                params.put("company", "Mishutt Finance");
                Log.d("VerifyOTP Params>>>",params.toString());
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    public void onboardFlag(){
        loading.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, api_add_onboarding_flag, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Onboard Flag>>>",response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loading.setVisibility(View.GONE);
                if (status == 1) {
                    System.out.println("Onboard Flag Added");
                    flag = true;
                    if (checkPermission()){
                        displayLocationSettingsRequest(PaymentActivity.this);
                    }
                } else {
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
                loading.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*......................................For BackPress.........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            /*if(webView.canGoBack()) {
                backPress = 0;
                webView.goBack();
            } else */if (webView.getVisibility() == View.VISIBLE){
                backPress = 0;
                webView.setVisibility(View.GONE);
                layout_gridView.setVisibility(View.VISIBLE);
            } else {
                showDashboard();
            }
        } else {
            finishAffinity();
        }
    }
}
