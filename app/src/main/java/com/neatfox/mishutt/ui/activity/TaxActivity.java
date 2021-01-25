package com.neatfox.mishutt.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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

import static com.neatfox.mishutt.Constants.api_itr;

public class TaxActivity extends MainActivity {

    EditText name,mobile_number,email_id;
    Button submit;
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_tax, contentFrameLayout);

        TaxActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tv_toolbar.setText(getIntent().getStringExtra("item_name"));

        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        email_id = findViewById(R.id.et_email_id);
        submit = findViewById(R.id.button_submit);

        name.setText(_name);
        mobile_number.setText(_mobile_number);
        email_id.setText(_email_id);
        /*.........................................Check..........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkName(view) && checkMobileNumber(view) && checkEmailId(view)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        submit();
                    }
                }
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

    private void submit(){
        progressDialog = new ProgressDialog(TaxActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_itr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("ITR>>>", "onResponse::::: " + response);
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
                    String _toast = "Thank you for your query.\nOur team members will call you within 24 hrs to take the process further.";
                    Toast.makeText(TaxActivity.this,_toast,Toast.LENGTH_LONG).show();
                    backPressed();
                } else {
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
                //params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("name", name.getText().toString().trim());
                params.put("phone", mobile_number.getText().toString().trim());
                params.put("email", email_id.getText().toString().trim());
                Log.i("ITR>>>", "params::::: " + params);
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
        Intent intent = new Intent (TaxActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaxActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
