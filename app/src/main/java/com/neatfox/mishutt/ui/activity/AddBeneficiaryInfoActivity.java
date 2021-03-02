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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_beneficiary_register;

public class AddBeneficiaryInfoActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ConstraintLayout layout;
    LinearLayout layout_back;
    ImageButton ib_back;
    EditText name,mobile_number,account_number,ifsc_code;
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
        setContentView(R.layout.activity_add_beneficiary_info);

        AddBeneficiaryInfoActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        account_number = findViewById(R.id.et_account_number);
        ifsc_code = findViewById(R.id.et_ifsc_code);
        submit = findViewById(R.id.button_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkName(view) &&  checkMobileNumber(view) && checkAccountNumber(view) &&
                checkIFSCCode(view)){
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
    /*....................................Check Account Number....................................*/
    public boolean checkAccountNumber (View view) {
        if (account_number.getText().toString().trim().length()<1){
            account_number.requestFocus();
            Snackbar.make(view, R.string.enter_account_number, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check IFSC Code......................................*/
    public boolean checkIFSCCode (View view) {
        if (ifsc_code.getText().toString().trim().length()<1){
            ifsc_code.requestFocus();
            Snackbar.make(view, R.string.enter_ifsc_code, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*..................................Save Beneficiary Details..................................*/
    private void submit(){
        progressDialog = new ProgressDialog(AddBeneficiaryInfoActivity.this);
        progressDialog.setMessage("Saving Beneficiary Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_register, new Response.Listener<String>() {
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
                    Toast.makeText(AddBeneficiaryInfoActivity.this, "Beneficiary Details Added Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("account", account_number.getText().toString().trim());
                params.put("ifsc", ifsc_code.getText().toString().trim());
                params.put("remitterid", _beneficiary_id);
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
        Intent intent = new Intent (AddBeneficiaryInfoActivity.this, BeneficiaryListActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        AddBeneficiaryInfoActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
