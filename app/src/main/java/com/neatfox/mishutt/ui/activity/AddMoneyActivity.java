package com.neatfox.mishutt.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import androidx.constraintlayout.widget.ConstraintLayout;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.UPI_ID;
import static com.neatfox.mishutt.Constants.api_wallet_add_money;

public class AddMoneyActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ConstraintLayout layout;
    LinearLayout layout_back;
    ImageButton ib_back;
    TextView available_balance;
    EditText amount;
    Button proceed;
    Dialog dialog;
    final int UPI_PAYMENT = 0;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_add_money);

        AddMoneyActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        available_balance = findViewById(R.id.tv_available_balance);
        amount = findViewById(R.id.et_amount);
        proceed = findViewById(R.id.button_submit);

        /*.........................................Dialog.........................................*/
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(windowParams);

        available_balance.setText(String.format("Available Balance %s", getIntent().getStringExtra("wallet_balance")));

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amount.getText().toString().trim().length()>0){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        payUsingUPI();
                    }
                } else {
                    amount.requestFocus();
                    Snackbar.make(v, R.string.enter_wallet_amount, Snackbar.LENGTH_SHORT).show();

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
    /*......................................Pay Using UPI.........................................*/
    public void payUsingUPI(){
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", UPI_ID)
                .appendQueryParameter("pn", "Mishutt Finances")
                .appendQueryParameter("tn", "Add Money to Wallet")
                .appendQueryParameter("am", amount.getText().toString().trim())
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        System.out.println(upiPayIntent);

        // check if intent resolves
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            System.out.println("No UPI app found, please install one to continue");
            //Toast.makeText(PaymentOptionsActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    Log.d("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else {
                Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                upiPaymentDataOperation(dataList);
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        System.out.println(data);
        if (isConnectionAvailable(AddMoneyActivity.this)) {
            String str = data.get(0);
            Log.d("UPI_PAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                // Toast.makeText(PaymentOptionsActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);
                if (isConnectionAvailable(AddMoneyActivity.this)){
                    System.out.println("TXN_SUCCESS");
                    sendPaymentSummary(approvalRefNo);
                } else {
                    snackBarError();
                }
            } else if (status.equals("failure")) {
                //Toast.makeText(PaymentOptionsActivity.this, "Transaction failed.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);

                if (isConnectionAvailable(AddMoneyActivity.this)){
                    System.out.println("TXN_FAILED");
                    //sendPaymentSummary("TXN_FAILED",approvalRefNo,"UPI");
                } else {
                    snackBarError();
                }
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                System.out.println("Payment cancelled by user");
                //Toast.makeText(PaymentOptionsActivity.this, "Payment cancelled by user", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(AddMoneyActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AddMoneyActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    /*..........................................Add Money.........................................*/
    public void sendPaymentSummary(final String id){
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, api_wallet_add_money, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Add Money>>>",response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                if (status == 1) {
                    Toast.makeText(AddMoneyActivity.this, "Transaction Successful", Toast.LENGTH_LONG).show();
                    backPressed();
                } else {
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
                dialog.dismiss();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("payment_amount", amount.getText().toString().trim());
                params.put("payment_date", date);
                params.put("transaction_id", id);
                params.put("upi_id", id);
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
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
        Intent intent = new Intent (AddMoneyActivity.this, WalletActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        AddMoneyActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
