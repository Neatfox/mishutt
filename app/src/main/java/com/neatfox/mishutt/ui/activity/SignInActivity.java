package com.neatfox.mishutt.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_forgot_password;
import static com.neatfox.mishutt.Constants.api_sign_in;

public class SignInActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    CoordinatorLayout layout;
    ImageView action_cancel;
    EditText mobile_number,email_id,password,_email_id;
    Button next,sign_in;
    TextView forgot_password,sign_up;
    Dialog dialog_forgot_password;
    ProgressDialog progressDialog;
    String emailPattern;
    int backPress = 0;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        SignInActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout_sign_in);
        mobile_number = findViewById(R.id.et_mobile_number);
        email_id = findViewById(R.id.et_email_id);
        password = findViewById(R.id.et_password);
        forgot_password = findViewById(R.id.tv_forgot_password);
        next = findViewById(R.id.button_next);
        sign_in = findViewById(R.id.button_sign_in);
        sign_up = findViewById(R.id.tv_sign_up);
        emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        /*.................................Dialog Forgot Password.................................*/
        dialog_forgot_password = new Dialog(SignInActivity.this);
        dialog_forgot_password.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_forgot_password.setContentView(R.layout.dialog_forgot_password);
        dialog_forgot_password.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog_forgot_password.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog_forgot_password.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog_forgot_password.getWindow().setAttributes(windowParams);

        action_cancel = dialog_forgot_password.findViewById(R.id.iv_cancel_action);
        _email_id = dialog_forgot_password.findViewById(R.id.et_email_id);
        Button _submit = dialog_forgot_password.findViewById(R.id.button_submit);

        _submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_email_id.getText().toString().trim().length()<1){
                    _email_id.requestFocus();
                    Toast.makeText(SignInActivity.this, R.string.enter_email_id, Toast.LENGTH_SHORT).show();
                } else if (!_email_id.getText().toString().trim().matches(emailPattern)){
                    _email_id.requestFocus();
                    Toast.makeText(SignInActivity.this, R.string.enter_valid_email_id, Toast.LENGTH_SHORT).show();
                } else {
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        sentResetPassword(_email_id.getText().toString().trim());
                    }
                }
            }
        });

        action_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_forgot_password.dismiss();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMobileNumber(v)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        Intent intent = new Intent(SignInActivity.this, VerificationActivity.class);
                        intent.putExtra("type","Sign In wMobile Number");
                        intent.putExtra("mobile_number",mobile_number.getText().toString().trim());
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                }
            }
        });
        /*.........................................Sign In........................................*/
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEmailId(v) && checkPassword(v)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        signIn();
                    }
                }
            }
        });
        /*....................................Forgot Password.....................................*/
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_forgot_password.show();
            }
        });
        /*.........................................Sign Up........................................*/
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
        });
    }
    /*....................................Check Mobile Number.....................................*/
    public boolean checkMobileNumber (View view) {
        if (mobile_number.getText().toString().trim().length()<1){
            Snackbar.make(view, R.string.enter_mobile_number, Snackbar.LENGTH_SHORT).show();
            mobile_number.requestFocus();
            return false;
        } else if (mobile_number.getText().toString().trim().length()<10){
            mobile_number.requestFocus();
            Snackbar.make(view, R.string.mobile_number_length, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check Email ID.......................................*/
    public boolean checkEmailId (View view) {
        if (email_id.getText().toString().trim().length()<1){
            email_id.requestFocus();
            Snackbar.make(view, R.string.enter_email_id, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (!email_id.getText().toString().trim().matches(emailPattern)){
            email_id.requestFocus();
            Snackbar.make(view, R.string.enter_valid_email_id, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check Password.......................................*/
    public boolean checkPassword (View view) {
        if (password.getText().toString().trim().length()<1){
            password.requestFocus();
            Snackbar.make(view, R.string.enter_password, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Sent Reset Password....................................*/
    public void sentResetPassword(final String emailId){
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_forgot_password, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Password>>>",response);
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
                    dialog_forgot_password.dismiss();
                    Toast.makeText(SignInActivity.this,
                            "A password reset link sent to Email ID : "+emailId,Toast.LENGTH_LONG).show();
                } else {
                    System.out.println(msg);
                    snackBarError();
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
                params.put("email",emailId);
                Log.d("Password Params>>>",params.toString());
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*...........................................Sign In..........................................*/
    public void signIn(){
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setMessage("Signing In...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_sign_in, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SignIn>>>",response);
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
                        startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (resObj != null) {
                            if ("Invalid Data".equalsIgnoreCase(resObj.getString("msg"))) {
                                Snackbar.make(layout, "Wrong Email ID or Password", Snackbar.LENGTH_SHORT).show();

                            } else if ("Not Activated".equalsIgnoreCase(resObj.getString("msg"))){
                                Intent intent = new Intent(SignInActivity.this, VerificationActivity.class);
                                intent.putExtra("type","Sign In");
                                intent.putExtra("name",resObj.getString("user_name"));
                                intent.putExtra("email_id",email_id.getText().toString().trim());
                                intent.putExtra("mobile_number",resObj.getString("mobileno"));
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            }
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
                params.put("email", email_id.getText().toString().trim());
                params.put("password", password.getText().toString().trim());
                Log.d("SignIn Params>>>",params.toString());
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
            Snackbar.make(layout, R.string.press_again_to_exit, Snackbar.LENGTH_SHORT).show();
        } else {
            finishAffinity();
        }
    }
}
