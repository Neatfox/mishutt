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
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_beneficiary_add;
import static com.neatfox.mishutt.Constants.api_beneficiary_mobile_check;
import static com.neatfox.mishutt.Constants.api_beneficiary_otp;

public class AddBeneficiaryActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_otp;
    ImageButton ib_back;
    TextInputLayout layout_mobile_number;
    EditText first_name,last_name,mobile_number,pin_code,otp;
    TextView resend;
    Button submit;
    ProgressDialog progressDialog;
    static final long START_TIME_IN_MILLIS = 120000;
    long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    boolean valid_mobile_number = false,has_otp = false;
    String _beneficiary_id;
    int backPress = 0;

    int[] colorGreenArray = {R.color.green};
    int[] colorRedArray = {R.color.red};

    private boolean isNetworkAvailable() {
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beneficiary);

        AddBeneficiaryActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        layout_mobile_number = findViewById(R.id.layout_mobile_number);
        layout_otp = findViewById(R.id.layout_otp);
        first_name = findViewById(R.id.et_first_name);
        last_name = findViewById(R.id.et_last_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        pin_code = findViewById(R.id.et_pin_code);
        otp = findViewById(R.id.et_otp);
        resend = findViewById(R.id.tv_resend);
        submit = findViewById(R.id.button_submit);
        layout_otp.setVisibility(View.GONE);

        mobile_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                valid_mobile_number = false;
                if(s.length()==10){
                    if (isNetworkAvailable()) noNetwork();
                    else {
                        checkMobileNumber();
                        layout_mobile_number.setHelperText("Checking Mobile Number Please Wait…");
                        layout_mobile_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorGreenArray[0]));
                    }
                } else {
                    layout_mobile_number.setHelperTextEnabled(false);
                }
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sent_otp();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (has_otp){
                    if (checkOTP(view)) {
                        if (isNetworkAvailable()) {
                            noNetwork();
                        } else {
                            register();
                        }
                    }
                } else {
                    if (checkFirstName(view) && checkLastName(view) && checkMobileNumber(view) && checkPINCode(view) ){
                        if (valid_mobile_number){
                            if (isNetworkAvailable()) {
                                noNetwork();
                            } else {
                                has_otp = true;
                                submit.setText(R.string.register);
                                layout_otp.setVisibility(View.VISIBLE);
                                startTimer();
                                sent_otp();
                            }
                        } else {
                            mobile_number.requestFocus();
                        }
                    }
                }
            }
        });

        /*..................................Back To Previous Page.................................*/
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backPressed(); }
        });

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backPressed(); }
        });
    }

    /*......................................Check First Name......................................*/
    public boolean checkFirstName(View view) {
        if (first_name.getText().toString().trim().length()<1){
            first_name.requestFocus();
            Snackbar.make(view, R.string.enter_first_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*......................................Check Last Name......................................*/
    public boolean checkLastName(View view) {
        if (last_name.getText().toString().trim().length()<1){
            last_name.requestFocus();
            Snackbar.make(view, R.string.enter_last_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Mobile Number.....................................*/
    public boolean checkMobileNumber (View view) {
        if (mobile_number.getText().toString().trim().length()<1){
            mobile_number.requestFocus();
            Snackbar.make(view, R.string.enter_mobile_number, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (mobile_number.getText().toString().trim().length()<10){
            mobile_number.requestFocus();
            Snackbar.make(view, R.string.mobile_number_length, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check PIN Code.......................................*/
    public boolean checkPINCode (View view) {
        if (pin_code.getText().toString().trim().length()<1){
            pin_code.requestFocus();
            Snackbar.make(view, R.string.enter_pin_code, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.........................................Check OTP..........................................*/
    public boolean checkOTP (View view) {
        if (otp.getText().toString().trim().length()<1){
            otp.requestFocus();
            Snackbar.make(view, R.string.enter_otp, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (otp.getText().toString().trim().length()<6){
            otp.requestFocus();
            Snackbar.make(view, R.string.otp_length, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
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

    /*....................................Verify Mobile Number....................................*/
    private void checkMobileNumber(){
        Snackbar.make(layout, R.string.checking_mobile_number, Snackbar.LENGTH_LONG).show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_mobile_check, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Verifying Mobile>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    valid_mobile_number = true;
                    layout_mobile_number.setHelperText("");
                } else {
                    layout_mobile_number.setHelperText("Mobile Number already registered");
                    layout_mobile_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorRedArray[0]));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_mobile_number.setHelperText("Failed to verify Mobile Number");
                layout_mobile_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorRedArray[0]));
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("mobile", mobile_number.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*..........................................Sent OTP..........................................*/
    private void sent_otp(){
        progressDialog = new ProgressDialog(AddBeneficiaryActivity.this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("OTP Info>>>", "onResponse::::: " + response);
                JSONObject resObj = null;
                String msg = "";
                try {
                    resObj = new JSONObject(response);
                    msg = resObj.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if ("OTP sent successfully".equalsIgnoreCase(msg) ||
                        "Transaction Successful".equalsIgnoreCase(msg)) {
                    Toast.makeText(AddBeneficiaryActivity.this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
                    try {
                        _beneficiary_id = resObj.getJSONObject("data").getJSONObject("remitter").getString("id");
                        System.out.println(_beneficiary_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AddBeneficiaryActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("name", first_name.getText().toString().trim());
                params.put("surname", last_name.getText().toString().trim());
                params.put("mobile", mobile_number.getText().toString().trim());
                params.put("pincode", pin_code.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    /*..................................Save Beneficiary Details..................................*/
    private void register(){
        progressDialog = new ProgressDialog(AddBeneficiaryActivity.this);
        progressDialog.setMessage("Saving Beneficiary Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Save Beneficiary>>>", "onResponse::::: " + response);
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
                    Toast.makeText(AddBeneficiaryActivity.this, "Beneficiary Details Added Successfully", Toast.LENGTH_SHORT).show();
                    backPressed();
                } else {
                    System.out.println(msg);
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("name", first_name.getText().toString().trim());
                params.put("surname", last_name.getText().toString().trim());
                params.put("mobile", mobile_number.getText().toString().trim());
                params.put("pincode", pin_code.getText().toString().trim());
                params.put("otp", otp.getText().toString().trim());
                params.put("remitterid", _beneficiary_id);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
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
        Intent intent = new Intent (AddBeneficiaryActivity.this, BeneficiaryListActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        AddBeneficiaryActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
