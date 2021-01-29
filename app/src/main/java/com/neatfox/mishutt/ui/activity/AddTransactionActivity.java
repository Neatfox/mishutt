package com.neatfox.mishutt.ui.activity;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_transaction_add;
import static com.neatfox.mishutt.Constants.api_transaction_category_list;
import static com.neatfox.mishutt.Constants.api_transaction_edit;
import static com.neatfox.mishutt.Constants.api_transaction_type;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class AddTransactionActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    ProgressDialog progressDialog;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_info,layout_button;
    ImageButton ib_back;
    TextView tv_toolbar_title;
    EditText transaction_date,amount,description,remarks;
    AppCompatAutoCompleteTextView category,transaction_type;
    ArrayList<Spinner> category_list = new ArrayList<>();
    ArrayList<Spinner> transaction_type_list = new ArrayList<>();
    Button submit;
    String _api;
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
        setContentView(R.layout.activity_add_transaction);

        AddTransactionActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        calendar = Calendar.getInstance();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        layout_info = findViewById(R.id.layout_info);
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title);
        layout_button = findViewById(R.id.layout_button);
        transaction_date = findViewById(R.id.et_transaction_date);
        category = findViewById(R.id.et_category);
        amount = findViewById(R.id.et_amount);
        transaction_type = findViewById(R.id.et_transaction_type);
        description = findViewById(R.id.et_description);
        remarks = findViewById(R.id.et_remarks);
        submit = findViewById(R.id.button_submit);

        if ("Edit Transaction".equalsIgnoreCase(getIntent().getStringExtra("type"))) {
            _api = api_transaction_edit;
            tv_toolbar_title.setText(getIntent().getStringExtra("type"));
            transaction_date.setText(getIntent().getStringExtra("transaction_date"));
            category.setText(getIntent().getStringExtra("category"));
            amount.setText(getIntent().getStringExtra("amount"));
            transaction_type.setText(getIntent().getStringExtra("transaction_type"));
            description.setText(getIntent().getStringExtra("description"));
            remarks.setText(getIntent().getStringExtra("remarks"));
        } else
            _api = api_transaction_add;

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            layout_info.setVisibility(View.INVISIBLE);
            layout_button.setVisibility(View.INVISIBLE);
            getTransactionCategory();
            getTransactionType();
        }
        /*........................................Spinner.........................................*/
        category.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    category.showDropDown();
                }
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category.getText().toString().trim().length()<4){
                    category.showDropDown();
                } else
                    amount.requestFocus();
            }
        });

        transaction_type.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    transaction_type.showDropDown();
                }
            }
        });

        transaction_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaction_type.getText().toString().trim().length()<4){
                    transaction_type.showDropDown();
                } else
                    description.requestFocus();
            }
        });
        /*......................................Date Picker.......................................*/
        transaction_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    transaction_date.setText("");
                    datePicker();
                }
            }
        });

        transaction_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (transaction_date.getText().toString().trim().length()<10)
                    datePicker();
                else
                    category.requestFocus();
            }
        });
        /*.........................................Submit.........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkTransactionDate(view) && checkTransactionCategory(view) &&
                        checkTransactionAmount(view) && checkTransactionType(view) &&
                        checkDescription(view) && checkRemarks(view)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        submitDetails(_api);
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

    /*.........................................Date Picker........................................*/
    public void datePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog (AddTransactionActivity.this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    public DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
            String date = (setDate(selectedYear, selectedMonth + 1, selectedDay));
            transaction_date.setText(date);
        }
    };

    public String setDate(int year, int month, int day){
        String fDay;
        if (day<10){
            fDay = "0"+day;
        } else {
            fDay = ""+day;
        }
        String fMonth;
        if (month<10){
            fMonth = "0"+month;
        } else{
            fMonth = ""+month;
        }
        return fDay + "-" + fMonth + "-" + year;
    }

    /*...................................Check Transaction Date...................................*/
    public boolean checkTransactionDate(View view) {
        if (transaction_date.getText().toString().trim().length()<1){
            transaction_date.requestFocus();
            Snackbar.make(view, R.string.select_transaction_date, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.................................Check Transaction Category.................................*/
    public boolean checkTransactionCategory(View view) {
        if (category.getText().toString().trim().length() < 1) {
            category.requestFocus();
            Snackbar.make(view, R.string.enter_transaction_category, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*..................................Check Transaction Amount..................................*/
    public boolean checkTransactionAmount(View view) {
        if (amount.getText().toString().trim().length()<1){
            amount.requestFocus();
            Snackbar.make(view, R.string.enter_transaction_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Transaction Type...................................*/
    public boolean checkTransactionType(View view) {
        if (transaction_type.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < transaction_type_list.size(); i++) {
                if (transaction_type.getText().toString().trim().equals(transaction_type_list.get(i).toString())) {
                    return true;
                }
            }
            transaction_type.setText("");
        }
        transaction_type.requestFocus();
        Snackbar.make(view, R.string.select_transaction_type, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*...............................Check Transaction Description................................*/
    public boolean checkDescription(View view) {
        if (description.getText().toString().trim().length()<1){
            description.requestFocus();
            Snackbar.make(view, R.string.enter_description, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.................................Check Transaction Remarks..................................*/
    public boolean checkRemarks(View view) {
        if (remarks.getText().toString().trim().length()<1){
            remarks.requestFocus();
            Snackbar.make(view, R.string.enter_remarks, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Transaction Category....................................*/
    public void getTransactionCategory() {
        category_list = new ArrayList<>();
        progressDialog = new ProgressDialog(AddTransactionActivity.this);
        progressDialog.setMessage("Loading Transaction Category...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_category_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction Category", "Transaction Category>>>: "+response);
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
                    if (!category_list.isEmpty()) {
                        category_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");
                        Spinner spinner;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            spinner = new Spinner(jsonObject.getString("cname"));
                            category_list.add(spinner);
                        }
                        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(AddTransactionActivity.this, android.R.layout.simple_spinner_dropdown_item, category_list);
                        category.setAdapter(adapter);
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
    /*......................................Transaction Type......................................*/
    public void getTransactionType() {
        transaction_type_list = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_type, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction Type", "Transaction Type>>>: "+response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!transaction_type_list.isEmpty()) {
                        transaction_type_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");
                        Spinner spinner;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            spinner = new Spinner(jsonObject.getString("value"));
                            transaction_type_list.add(spinner);
                        }
                        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(AddTransactionActivity.this, android.R.layout.simple_spinner_dropdown_item, transaction_type_list);
                        transaction_type.setAdapter(adapter);
                    }   catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                return new HashMap<>();
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.......................................Submit Details.......................................*/
    private void submitDetails(String api){
        progressDialog = new ProgressDialog(AddTransactionActivity.this);
        progressDialog.setMessage("Saving Transaction Info...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction Info>>>", "onResponse::::: " + response);
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
                    if ("Edit Transaction".equalsIgnoreCase(getIntent().getStringExtra("type")))
                        Toast.makeText(AddTransactionActivity.this, "Transaction Info Updated Successfully", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(AddTransactionActivity.this, "Transaction Info Added Successfully", Toast.LENGTH_SHORT).show();
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
                if ("Edit Transaction".equalsIgnoreCase(getIntent().getStringExtra("type")))
                    params.put("expenceid",getIntent().getStringExtra("transaction_list_id"));
                params.put("entdate", changeDateFormatDB(transaction_date.getText().toString().trim()));
                params.put("expcategory", category.getText().toString().trim());
                params.put("amount", amount.getText().toString().trim());
                if ("Earning".equalsIgnoreCase(transaction_type.getText().toString().trim()))
                    params.put("type","earning" );
                else if ("Expense".equalsIgnoreCase(transaction_type.getText().toString().trim()))
                    params.put("type","spent" );
                params.put("description", description.getText().toString().trim());
                params.put("remarks", remarks.getText().toString().trim());
                Log.i("Transaction Info>>>", "Params::::: " + params);
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
        Intent intent = new Intent (AddTransactionActivity.this, ExpenseManagerActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("type","Transactions");
        AddTransactionActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
