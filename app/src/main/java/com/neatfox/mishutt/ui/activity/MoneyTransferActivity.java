package com.neatfox.mishutt.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_beneficiary_money_transfer;

public class MoneyTransferActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ConstraintLayout layout;
    LinearLayout layout_back;
    ImageButton ib_back;
    EditText name,mobile_number,amount;
    AppCompatAutoCompleteTextView transaction_mode;
    Button submit;
    ProgressDialog progressDialog;
    String _beneficiary_id = "",_beneficiaryId = "";
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
        setContentView(R.layout.activity_money_transfer);

        MoneyTransferActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        amount = findViewById(R.id.et_amount);
        transaction_mode = findViewById(R.id.et_transaction_mode);
        submit = findViewById(R.id.button_submit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                MoneyTransferActivity.this, R.array.transaction_mode, R.layout.adapter_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transaction_mode.setAdapter(adapter);

        transaction_mode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    transaction_mode.showDropDown();
                }
            }
        });

        transaction_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaction_mode.getText().toString().trim().length()<3){
                    transaction_mode.showDropDown();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkName(view) &&  checkMobileNumber(view) && checkAmount(view) && checkTransactionMode(view)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        submit();
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
    public boolean checkName(View view) {
        if (name.getText().toString().trim().length()<1){
            name.requestFocus();
            Snackbar.make(view, R.string.enter_name, Snackbar.LENGTH_SHORT).show();
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
    /*........................................Check Amount........................................*/
    public boolean checkAmount (View view) {
        if (amount.getText().toString().trim().length()<1){
            amount.requestFocus();
            Snackbar.make(view, R.string.enter_transaction_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Transaction Mode...................................*/
    public boolean checkTransactionMode(View view) {
        if (transaction_mode.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < transaction_mode.getAdapter().getCount(); i++) {
                if (transaction_mode.getText().toString().trim().equalsIgnoreCase(transaction_mode.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            transaction_mode.setText("");
        }
        transaction_mode.requestFocus();
        Snackbar.make(view, R.string.select_transaction_mode, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*.......................................Sending Money........................................*/
    private void submit(){
        progressDialog = new ProgressDialog(MoneyTransferActivity.this);
        progressDialog.setMessage("Sending Money...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_money_transfer, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Money Transferred>>>", "onResponse::::: " + response);
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
                    Toast.makeText(MoneyTransferActivity.this, "Money Transferred Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("name", name.getText().toString().trim());
                params.put("mobile", mobile_number.getText().toString().trim());
                params.put("amount", amount.getText().toString().trim());
                params.put("mode", transaction_mode.getText().toString().trim());
                params.put("beneficiaryid", _beneficiary_id);
                params.put("remitter_id", _beneficiaryId);
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
        Intent intent = new Intent (MoneyTransferActivity.this, BeneficiaryListActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        MoneyTransferActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
