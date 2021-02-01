package com.neatfox.mishutt.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.WalletTransactionAdapter;
import com.neatfox.mishutt.ui.model.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.addCommaString;
import static com.neatfox.mishutt.Constants.api_add_onboarding_flag;
import static com.neatfox.mishutt.Constants.api_add_payment_flag;
import static com.neatfox.mishutt.Constants.api_onboarding_otp_send;
import static com.neatfox.mishutt.Constants.api_onboarding_otp_validate;
import static com.neatfox.mishutt.Constants.api_user_add;
import static com.neatfox.mishutt.Constants.api_wallet_balance;
import static com.neatfox.mishutt.Constants.api_wallet_transaction_list;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class WalletActivity extends MainActivity {

    ImageView action_cancel;
    TextView email_id,mobile_number;
    EditText one,two,three,four,five,six;
    Button resend,submit;
    Dialog dialog_otp;
    ProgressDialog progressDialog;
    static final long START_TIME_IN_MILLIS = 120000;
    long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    String verification_code = "";
    TextView name,mobile_number_wallet,wallet_balance,add_money,transfer_money;
    ShimmerFrameLayout mShimmerViewContainer;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    WalletTransactionAdapter adapter;
    ArrayList<Wallet> transaction_list;
    TextView no_list;
    ProgressBar loading;
    int backPress=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_wallet, contentFrameLayout);
        tv_toolbar.setText(R.string.wallet);

        WalletActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mShimmerViewContainer = findViewById(R.id.shimmer_view);
        name = findViewById(R.id.tv_name);
        mobile_number_wallet = findViewById(R.id.tv_mobile_number);
        wallet_balance = findViewById(R.id.tv_wallet_balance);
        add_money = findViewById(R.id.tv_add_money);
        transfer_money = findViewById(R.id.tv_transfer_money);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        layoutManager = new LinearLayoutManager(WalletActivity.this);
        recyclerView = findViewById(R.id.recyclerView);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);

        name.setText(_name);
        mobile_number_wallet.setText(String.format("+91 %s", _mobile_number));
        wallet_balance.setText(String.format("₹%s", addCommaString("0")));

        swipeRefreshLayout.setEnabled(false);
        wallet_balance.setVisibility(View.INVISIBLE);
        add_money.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);

        /*.......................................Dialog OTP.......................................*/
        dialog_otp = new Dialog(WalletActivity.this);
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
                    Toast.makeText(WalletActivity.this,R.string.enter_verification_code,Toast.LENGTH_SHORT).show();
                else if (verification_code.length()<6)
                    Toast.makeText(WalletActivity.this,R.string.verification_code_length_six,Toast.LENGTH_SHORT).show();
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

        if ("N".equalsIgnoreCase(_payment_flag)){
            if (_pan_no.trim().length()<10){
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                builder.setMessage("Add PAN No")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                startActivity(new Intent(WalletActivity.this, AddProfileActivity.class));
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
            swipeRefreshLayout.setEnabled(true);
            if (isNetworkAvailable()) {
                noNetwork();
            } else {
                getWalletBalance();
                getTransactionHistory();
            }
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    noNetwork();
                } else {
                    getWalletBalance();
                    getTransactionHistory();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });

        add_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (WalletActivity.this, AddMoneyActivity.class);
                intent.putExtra("wallet_balance",wallet_balance.getText().toString().trim());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                WalletActivity.this.startActivity(intent);
            }
        });

        transfer_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WalletActivity.this,
                        "This feature will get active in the next release. Thank You",Toast.LENGTH_LONG).show();
            }
        });
        /*....................................Bottom Navigation...................................*/
        bottom_navigation_id = 4;
        bottomNavigation.show(bottom_navigation_id,false);
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

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmer();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmer();
        super.onPause();
    }

    public void addUser(){
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
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error");
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
                params.put("zip_code", "111111");
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
        progressDialog = new ProgressDialog(WalletActivity.this);
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
                        Toast.makeText(WalletActivity.this, "Update Profile Details First", Toast.LENGTH_SHORT).show();
                    else if(msg.contains("PAN already registered"))
                        Toast.makeText(WalletActivity.this, msg, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(WalletActivity.this,"OTP Mismatch", Toast.LENGTH_SHORT).show();
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
                params.put("zip_code", "111111");
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
                if (status == 1) {
                    System.out.println("Onboard Flag Added");
                    swipeRefreshLayout.setEnabled(true);
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        getWalletBalance();
                        getTransactionHistory();
                    }
                } else {
                    snackBarError();
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
                params.put("uid", sharedPreference.getString("register_id", ""));
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.......................................Wallet Balance.......................................*/
    public void getWalletBalance(){
        StringRequest request = new StringRequest(Request.Method.POST, api_wallet_balance, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Wallet Balance>>>",response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    try {
                        String _wallet_balance = resObj.getJSONObject("show").getString("wallet_amount");
                        wallet_balance.setText(String.format("₹%s", addCommaString(_wallet_balance)));
                        mShimmerViewContainer.setVisibility(View.GONE);
                        wallet_balance.setVisibility(View.VISIBLE);
                        add_money.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    snackBarError();
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
                params.put("uid", sharedPreference.getString("register_id", ""));
                //params.put("uid", "5f67778e63561");
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.....................................Transaction History....................................*/
    private void getTransactionHistory(){
        loading.setVisibility(View.VISIBLE);
        transaction_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_wallet_transaction_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!transaction_list.isEmpty()) {
                        transaction_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Wallet wallet = new Wallet();
                            wallet.setPrime_transaction_id(jsonObject.optString("prime_tran_id"));
                            wallet.setTransaction_date(jsonObject.optString("tran_date"));
                            wallet.setTransaction_time(jsonObject.optString("tran_time"));
                            wallet.setTransaction_amount(jsonObject.optString("amount"));
                            wallet.setService_name(jsonObject.optString("service_name"));
                            wallet.setService_type(jsonObject.optString("service_type"));
                            wallet.setCommission_amount(jsonObject.optString("commission_amount"));
                            wallet.setCustomer_params(jsonObject.optString("customer_params"));
                            transaction_list.add(wallet);
                        }
                        recyclerView.setLayoutManager(layoutManager);
                        adapter = new WalletTransactionAdapter(transaction_list,WalletActivity.this);
                        recyclerView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    no_list.setVisibility(View.GONE);
                } else {
                    loading.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    no_list.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                //params.put("uid", "5f67778e63561");
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
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
