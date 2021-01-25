package com.neatfox.mishutt.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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

import static com.neatfox.mishutt.Constants.api_sign_up;

public class SignUpActivity extends AppCompatActivity {

    CoordinatorLayout layout;
    ImageView back;
    EditText name,email_id,mobile_number,password,referral_code;
    Button next;
    TextView sign_in;
    ProgressDialog progressDialog;
    String emailPattern;
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
        setContentView(R.layout.activity_sign_up);
        SignUpActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        layout = findViewById(R.id.layout_sign_up);
        back = findViewById(R.id.iv_back);
        name = findViewById(R.id.et_name);
        email_id = findViewById(R.id.et_email_id);
        mobile_number = findViewById(R.id.et_mobile_number);
        password = findViewById(R.id.et_password);
        referral_code = findViewById(R.id.et_referral_code);
        next = findViewById(R.id.button_sign_up);
        sign_in = findViewById(R.id.tv_sign_in);
        emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        /*..........................................Back..........................................*/
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });
        /*.........................................Sign Up........................................*/
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkName(v) && checkEmailId(v) && checkMobileNumber(v) && checkPassword(v)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        signUp();
                    }
                }
            }
        });
        /*.........................................Sign In........................................*/
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });
    }
    /*.........................................Check Name.........................................*/
    public boolean checkName (View view) {
        if (name.getText().toString().trim().length()<1){
            name.requestFocus();
            Snackbar.make(view, R.string.enter_name, Snackbar.LENGTH_SHORT).show();
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
    /*.......................................Check Password.......................................*/
    public boolean checkPassword (View view) {
        if (password.getText().toString().trim().length()<1){
            password.requestFocus();
            Snackbar.make(view, R.string.enter_password, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...........................................Sign Up..........................................*/
    public void signUp(){
        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("Signing Up...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_sign_up, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SignUp>>>",response);
                JSONObject resObj = null;
                int status = 0;
                //String msg = "";
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                    //msg = resObj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    Intent intent = new Intent(SignUpActivity.this, VerificationActivity.class);
                    intent.putExtra("type","Sign Up");
                    intent.putExtra("name",name.getText().toString().trim());
                    intent.putExtra("email_id",email_id.getText().toString().trim());
                    intent.putExtra("mobile_number",mobile_number.getText().toString().trim());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    try {
                        if (resObj != null) {
                            if ("Email Already Exits!".equalsIgnoreCase(resObj.getJSONObject("data").getString("msg"))){
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setTitle("Mishutt Finances")
                                        .setIcon(R.drawable.mishutt)
                                        .setMessage(email_id.getText().toString().trim()+" is already registered with Mishutt Finances.\n" +
                                        "Please Sign In to continue...")
                                        .setCancelable(false)
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else
                                Snackbar.make(layout, "Unable to Sign Up", Snackbar.LENGTH_SHORT).show();
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
                params.put("name", name.getText().toString().trim());
                params.put("email", email_id.getText().toString().trim());
                params.put("mobile", mobile_number.getText().toString().trim());
                params.put("pass", password.getText().toString().trim());
                params.put("cpass", password.getText().toString().trim());
                params.put("ref_code", referral_code.getText().toString().trim());
                Log.d("SignUp Params>>>",params.toString());
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
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
