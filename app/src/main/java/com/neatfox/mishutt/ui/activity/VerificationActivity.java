package com.neatfox.mishutt.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_resend_otp;
import static com.neatfox.mishutt.Constants.api_verify_login_otp;
import static com.neatfox.mishutt.Constants.api_verify_otp;

public class VerificationActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    CoordinatorLayout layout;
    ImageView back;
    Button submit;
    TextView email_id,mobile_number,resend;
    EditText one,two,three,four;
    ProgressDialog progressDialog;
    static final long START_TIME_IN_MILLIS = 120000;
    long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    String verification_code = "";
    int backPress = 0;

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        VerificationActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout_verification);
        back = findViewById(R.id.iv_back);
        email_id = findViewById(R.id.tv_email_id);
        mobile_number = findViewById(R.id.tv_mobile_number);
        submit = findViewById(R.id.button_submit);
        resend = findViewById(R.id.tv_resend);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });

        String sourceStringEmailId = "<b>"+ getIntent().getStringExtra("email_id") + "</b>";
        email_id.setText(Html.fromHtml(sourceStringEmailId));

        String sourceStringMobileNumber = "and <b>"+"+91 " + getIntent().getStringExtra("mobile_number") + "</b>";
        mobile_number.setText(Html.fromHtml(sourceStringMobileNumber));
        if ("Sign In wMobile Number".equalsIgnoreCase(getIntent().getStringExtra("type"))){
            email_id.setVisibility(View.GONE);
        }
        /*....................................Verification Code...................................*/
        one = findViewById(R.id.edit_text_one);
        two = findViewById(R.id.edit_text_two);
        three = findViewById(R.id.edit_text_three);
        four = findViewById(R.id.edit_text_four);

        one.addTextChangedListener(new GenericTextWatcher(one));
        two.addTextChangedListener(new GenericTextWatcher(two));
        three.addTextChangedListener(new GenericTextWatcher(three));
        four.addTextChangedListener(new GenericTextWatcher(four));
        /*................................Submit Verification Code................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verification_code.length()<1)
                    Snackbar.make(layout, R.string.enter_verification_code, Snackbar.LENGTH_SHORT).show();
                else if (verification_code.length()<4)
                    Snackbar.make(layout, R.string.verification_code_length, Snackbar.LENGTH_SHORT).show();
                else {
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        if ("Sign In wMobile Number".equalsIgnoreCase(getIntent().getStringExtra("type"))){
                            verifyLoginOTP();
                        } else {
                            verifyOTP();
                        }
                    }
                }
            }
        });
        /*....................................Start Countdown.....................................*/
        startTimer();
        /*if (isNetworkAvailable()) {
            noNetwork();
        } else {
            sendOTP();
        }*/
        /*................................Resend Verification Code................................*/
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
                if (text.length() == 0) three.requestFocus();
            }

            String stringOne = one.getText().toString().trim();
            String stringTwo = two.getText().toString().trim();
            String stringThree = three.getText().toString().trim();
            String stringFour = four.getText().toString().trim();
            verification_code = stringOne + stringTwo + stringThree + stringFour;
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
    /*..........................................Send OTP..........................................*/
    public void sendOTP(){
        progressDialog = new ProgressDialog(VerificationActivity.this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_resend_otp, new Response.Listener<String>() {
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
                progressDialog.dismiss();
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
                progressDialog.dismiss();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mob", getIntent().getStringExtra("mobile_number"));
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
        progressDialog = new ProgressDialog(VerificationActivity.this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_verify_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("VerifyOTP>>>",response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    try {
                        editor.putString("register_id", resObj.getString("registerid"));
                        editor.putString("profile_picture", resObj.getString("picture"));
                        editor.putString("name",getIntent().getStringExtra("name"));
                        editor.putString("email_id", getIntent().getStringExtra("email_id"));
                        editor.putString("mobile_number", getIntent().getStringExtra("mobile_number"));
                        editor.putString("isLoggedIn", "1");
                        editor.commit();
                        startActivity(new Intent(VerificationActivity.this, DashboardActivity.class));
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Snackbar.make(layout, "OTP Mismatch", Snackbar.LENGTH_SHORT).show();
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
                params.put("phone", getIntent().getStringExtra("mobile_number"));
                params.put("otp", verification_code.trim());
                Log.d("VerifyOTP Params>>>",params.toString());
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*......................................Verify Login OTP......................................*/
    public void verifyLoginOTP(){
        progressDialog = new ProgressDialog(VerificationActivity.this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_verify_login_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("VerifyOTP>>>",response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    try {
                        editor.putString("register_id", resObj.getString("registerid"));
                        editor.putString("profile_picture", resObj.getString("picture"));
                        editor.putString("name", resObj.getString("user_name"));
                        editor.putString("mobile_number", resObj.getString("mobileno"));
                        if ("null".equalsIgnoreCase(resObj.getString("date_time")) ||
                                ("".equalsIgnoreCase(resObj.getString("date_time"))))
                            editor.putLong("message_date",0);
                        else
                            editor.putLong("message_date", Long.parseLong(resObj.getString("date_time")));
                        editor.putString("email_id", email_id.getText().toString().trim());
                        editor.putString("isLoggedIn", "1");
                        editor.commit();
                        startActivity(new Intent(VerificationActivity.this, HomeActivity.class));
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        if (resObj != null) {
                            if ("OTP not matched, go back to verify otp page.".equalsIgnoreCase(resObj.getJSONObject("data").getString("msg"))) {
                                Snackbar.make(layout, "OTP Mismatch", Snackbar.LENGTH_SHORT).show();
                            }  else
                                Toast.makeText(VerificationActivity.this, "Mobile Number is not registered yet, Sign Up to Continue", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                params.put("phone", getIntent().getStringExtra("mobile_number"));
                params.put("otp", verification_code.trim());
                Log.d("VerifyOTP Params>>>",params.toString());
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.......................................For BackPress........................................*/
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
        if ("Sign Up".equalsIgnoreCase(getIntent().getStringExtra("type"))) {
            startActivity(new Intent(VerificationActivity.this, SignUpActivity.class));
        } else {
            startActivity(new Intent(VerificationActivity.this, SignInActivity.class));
        }
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
