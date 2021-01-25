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
import com.neatfox.mishutt.ui.model.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_transaction_category_add;
import static com.neatfox.mishutt.Constants.api_transaction_category_interval;

public class AddTransactionCategoryActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_info,layout_button;
    ImageButton ib_back;
    EditText category_name;
    AppCompatAutoCompleteTextView interval;
    ArrayList<Spinner> interval_list = new ArrayList<>();
    Button submit;
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
        setContentView(R.layout.activity_add_transaction_category);

        AddTransactionCategoryActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        layout_info = findViewById(R.id.layout_info);
        layout_button = findViewById(R.id.layout_button);
        category_name = findViewById(R.id.et_category);
        interval = findViewById(R.id.et_interval);
        submit = findViewById(R.id.button_submit);

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            layout_info.setVisibility(View.INVISIBLE);
            layout_button.setVisibility(View.INVISIBLE);
            getTransactionCategory();
        }
        /*........................................Spinner.........................................*/
        interval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    interval.showDropDown();
                }
            }
        });

        interval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interval.showDropDown();
            }
        });
        /*.........................................Submit.........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCategoryName(view) && checkInterval(view)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        submitDetails();
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
    /*....................................Check Category Name.....................................*/
    public boolean checkCategoryName(View view) {
        if (category_name.getText().toString().trim().length()<1){
            category_name.requestFocus();
            Snackbar.make(view, R.string.enter_category_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check Interval.......................................*/
    public boolean checkInterval(View view) {
        if (interval.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < interval_list.size(); i++) {
                if (interval.getText().toString().trim().equals(interval_list.get(i).toString())) {
                    return true;
                }
            }
            interval.setText("");
        }
        interval.requestFocus();
        Snackbar.make(view, R.string.select_interval, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*..........................................Interval..........................................*/
    public void getTransactionCategory() {
        interval_list = new ArrayList<>();
        progressDialog = new ProgressDialog(AddTransactionCategoryActivity.this);
        progressDialog.setMessage("Loading Interval...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_category_interval, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Interval", "Interval>>>: "+response);
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
                    if (!interval_list.isEmpty()) {
                        interval_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");
                        Spinner spinner;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            spinner = new Spinner(jsonObject.getString("value"));
                            interval_list.add(spinner);
                        }
                        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(AddTransactionCategoryActivity.this, android.R.layout.simple_spinner_dropdown_item, interval_list);
                        interval.setAdapter(adapter);
                        layout_info.setVisibility(View.VISIBLE);
                        layout_button.setVisibility(View.VISIBLE);
                    }   catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    snackBarError();
                    layout_info.setVisibility(View.GONE);
                    layout_button.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                layout_info.setVisibility(View.GONE);
                layout_button.setVisibility(View.GONE);
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
    /*.......................................Submit Details.......................................*/
    private void submitDetails(){
        progressDialog = new ProgressDialog(AddTransactionCategoryActivity.this);
        progressDialog.setMessage("Saving Category Info...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_category_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Category Info>>>", "onResponse::::: " + response);
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
                    Toast.makeText(AddTransactionCategoryActivity.this, "Category Info Added Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("cname", category_name.getText().toString().trim());
                params.put("interval", interval.getText().toString().trim());
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
        Intent intent = new Intent (AddTransactionCategoryActivity.this, ExpenseManagerActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("type","Transaction Category");
        AddTransactionCategoryActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
