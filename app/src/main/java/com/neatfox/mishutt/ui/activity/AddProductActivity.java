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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_product_add;
import static com.neatfox.mishutt.Constants.api_product_category;
import static com.neatfox.mishutt.Constants.api_product_subcategory;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class AddProductActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    ProgressDialog progressDialog;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_info,layout_button;
    ImageButton ib_back;
    EditText product_name,investment_amount,product_purchaser,product_value,premium_frequency,
            premium_amount,purchase_date,next_due_date,maturity_value,valid_till,nominee_name;
    AppCompatAutoCompleteTextView product_category,product_subcategory;
    ArrayList<Spinner> category_list = new ArrayList<>();
    ArrayList<Spinner> subcategory_list = new ArrayList<>();
    Button submit;
    String date_picker_type;
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
        setContentView(R.layout.activity_add_product);

        AddProductActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        calendar = Calendar.getInstance();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        layout_info = findViewById(R.id.layout_info);
        layout_button = findViewById(R.id.layout_button);
        product_name = findViewById(R.id.et_product_name);
        investment_amount = findViewById(R.id.et_investment_amount);
        product_category = findViewById(R.id.et_product_category);
        product_subcategory = findViewById(R.id.et_product_subcategory);
        product_purchaser = findViewById(R.id.et_product_purchaser);
        product_value = findViewById(R.id.et_product_value);
        premium_frequency = findViewById(R.id.et_premium_frequency);
        premium_amount = findViewById(R.id.et_premium_amount);
        purchase_date = findViewById(R.id.et_purchase_date);
        next_due_date = findViewById(R.id.et_next_due_date);
        maturity_value = findViewById(R.id.et_maturity_value);
        valid_till = findViewById(R.id.et_valid_till);
        nominee_name = findViewById(R.id.et_nominee_name);
        submit = findViewById(R.id.button_submit);

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            layout_info.setVisibility(View.INVISIBLE);
            layout_button.setVisibility(View.INVISIBLE);
            getProductCategory();
        }
        /*........................................Spinner.........................................*/
        product_category.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    product_category.showDropDown();
                    product_subcategory.setText("");
                    resetProductSubcategory();
                }
            }
        });

        product_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (product_category.getText().toString().trim().length()<4){
                    product_category.showDropDown();
                    product_subcategory.setText("");
                    resetProductSubcategory();
                } else
                    product_subcategory.requestFocus();
            }
        });

        product_category.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                if (isNetworkAvailable()) noNetwork();
                else getProductSubcategory();
            }
        });

        product_subcategory.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && product_category.getText().toString().trim().length()<4){
                    product_subcategory.dismissDropDown();
                    product_category.requestFocus();
                } else if (hasFocus){
                    product_subcategory.showDropDown();
                }
            }
        });

        product_subcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (product_category.getText().toString().trim().length()<4){
                    product_subcategory.dismissDropDown();
                    product_category.requestFocus();
                } else if (product_subcategory.getText().toString().trim().length()<4)
                    product_subcategory.showDropDown();
                else
                    product_purchaser.requestFocus();
            }
        });
        /*......................................Date Picker.......................................*/
        purchase_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    purchase_date.setText("");
                    datePicker("Purchase_Date");
                }
            }
        });

        purchase_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (purchase_date.getText().toString().trim().length()<10)
                    datePicker("Purchase_Date");
                else
                    next_due_date.requestFocus();
            }
        });

        next_due_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    next_due_date.setText("");
                    datePicker("Due_Date");
                }
            }
        });

        next_due_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (next_due_date.getText().toString().trim().length()<10)
                    datePicker("Due_Date");
                else
                    maturity_value.requestFocus();
            }
        });

        valid_till.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    valid_till.setText("");
                    datePicker("Valid_Till");
                }
            }
        });

        valid_till.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valid_till.getText().toString().trim().length()<10)
                    datePicker("Valid_Till");
                else
                    nominee_name.requestFocus();
            }
        });
        /*.........................................Submit.........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkProductName(view) && checkInvestmentAmount(view) && checkProductCategory(view) &&
                        checkProductSubcategory(view) && checkProductPurchaser(view) && checkProductValue(view) &&
                            checkPremiumFrequency(view) && checkPremiumAmount(view) && checkPurchaseDate(view) &&
                                checkDueDate(view) && checkMaturityValue(view) && checkValidTill(view) &&
                                    checkNomineeName(view)){
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
    /*.........................................Date Picker........................................*/
    public void datePicker(String type){
        date_picker_type = type;
        DatePickerDialog datePickerDialog = new DatePickerDialog (AddProductActivity.this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        if ("Purchase_Date".equalsIgnoreCase(type))
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        else
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    public DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
            String date = (setDate(selectedYear, selectedMonth + 1, selectedDay));
            if ("Purchase_Date".equalsIgnoreCase(date_picker_type))
                purchase_date.setText(date);
            else if ("Due_Date".equalsIgnoreCase(date_picker_type))
                next_due_date.setText(date);
            else if ("Valid_Till".equalsIgnoreCase(date_picker_type))
                valid_till.setText(date);
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
    /*.....................................Check Product Name.....................................*/
    public boolean checkProductName(View view) {
        if (product_name.getText().toString().trim().length()<1){
            product_name.requestFocus();
            Snackbar.make(view, R.string.enter_product_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Investment Amount..................................*/
    public boolean checkInvestmentAmount(View view) {
        if (investment_amount.getText().toString().trim().length()<1){
            investment_amount.requestFocus();
            Snackbar.make(view, R.string.enter_investment_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Product Category...................................*/
    public boolean checkProductCategory(View view) {
        if (product_category.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < category_list.size(); i++) {
                if (product_category.getText().toString().trim().equals(category_list.get(i).toString())) {
                    return true;
                }
            }
            product_category.setText("");
        }
        product_category.requestFocus();
        Snackbar.make(view, R.string.select_product_category, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*..................................Check Product Subcategory.................................*/
    public boolean checkProductSubcategory(View view) {
        if (product_subcategory.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < subcategory_list.size(); i++) {
                if (product_subcategory.getText().toString().trim().equals(subcategory_list.get(i).toString())) {
                    return true;
                }
            }
            product_subcategory.setText("");
        }
        product_subcategory.requestFocus();
        Snackbar.make(view, R.string.select_product_subcategory, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*...................................Check Product Purchaser..................................*/
    public boolean checkProductPurchaser(View view) {
        if (product_purchaser.getText().toString().trim().length()<1){
            product_purchaser.requestFocus();
            Snackbar.make(view, R.string.enter_product_purchaser, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Product Value.....................................*/
    public boolean checkProductValue(View view) {
        if (product_value.getText().toString().trim().length()<1){
            product_value.requestFocus();
            Snackbar.make(view, R.string.enter_product_value, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Premium Frequency..................................*/
    public boolean checkPremiumFrequency(View view) {
        if (premium_frequency.getText().toString().trim().length()<1){
            premium_frequency.requestFocus();
            Snackbar.make(view, R.string.enter_premium_frequency, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Premium Amount....................................*/
    public boolean checkPremiumAmount(View view) {
        if (premium_amount.getText().toString().trim().length()<1){
            premium_amount.requestFocus();
            Snackbar.make(view, R.string.enter_premium_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Purchase Date.....................................*/
    public boolean checkPurchaseDate(View view) {
        if (purchase_date.getText().toString().trim().length()<1){
            purchase_date.requestFocus();
            Snackbar.make(view, R.string.select_purchase_date, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check Due Date.......................................*/
    public boolean checkDueDate(View view) {
        if (next_due_date.getText().toString().trim().length()<1){
            next_due_date.requestFocus();
            Snackbar.make(view, R.string.select_next_due_date, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Maturity Value....................................*/
    public boolean checkMaturityValue(View view) {
        if (maturity_value.getText().toString().trim().length()<1){
            maturity_value.requestFocus();
            Snackbar.make(view, R.string.enter_maturity_value, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*......................................Check Valid Till......................................*/
    public boolean checkValidTill(View view) {
        if (valid_till.getText().toString().trim().length()<1){
            valid_till.requestFocus();
            Snackbar.make(view, R.string.select_valid_till, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Nominee Name.....................................*/
    public boolean checkNomineeName(View view) {
        if (nominee_name.getText().toString().trim().length()<1){
            nominee_name.requestFocus();
            Snackbar.make(view, R.string.enter_nominee_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*......................................Product Category......................................*/
    public void getProductCategory() {
        category_list = new ArrayList<>();
        progressDialog = new ProgressDialog(AddProductActivity.this);
        progressDialog.setMessage("Loading Product Category...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_product_category, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Product Category", "Product Category>>>: "+response);
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
                        JSONArray jsonArray = jsonRootObject.getJSONArray("cat_list");
                        Spinner spinner;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            spinner = new Spinner(jsonObject.getString("key"));
                            category_list.add(spinner);
                        }
                        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(AddProductActivity.this, android.R.layout.simple_spinner_dropdown_item, category_list);
                        product_category.setAdapter(adapter);
                        layout_info.setVisibility(View.VISIBLE);
                        layout_button.setVisibility(View.VISIBLE);
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
                progressDialog.dismiss();
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
    /*.....................................Product Subcategory....................................*/
    public void resetProductSubcategory(){
        subcategory_list = new ArrayList<>();
        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(AddProductActivity.this, android.R.layout.simple_spinner_dropdown_item, subcategory_list);
        product_subcategory.setAdapter(adapter);
    }

    public void getProductSubcategory() {
        resetProductSubcategory();
        progressDialog = new ProgressDialog(AddProductActivity.this);
        progressDialog.setMessage("Loading Product Subcategory...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_product_subcategory, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Product Subcategory", "Product Subcategory>>>: "+response);
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
                    if (!subcategory_list.isEmpty()) {
                        subcategory_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("sub_cat_list");
                        Spinner spinner;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if ("Loan".equalsIgnoreCase(product_category.getText().toString().trim()))
                                spinner = new Spinner(jsonObject.getString("typeofloan"));
                            else
                                spinner = new Spinner(jsonObject.getString("typeofIns"));
                            subcategory_list.add(spinner);
                        }
                        ArrayAdapter<Spinner> adapter = new ArrayAdapter<>(AddProductActivity.this, android.R.layout.simple_spinner_dropdown_item, subcategory_list);
                        product_subcategory.setAdapter(adapter);
                    }   catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No Data Found");
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
                params.put("category_type", product_category.getText().toString().trim());
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
        progressDialog = new ProgressDialog(AddProductActivity.this);
        progressDialog.setMessage("Saving Product Info...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_product_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Product Info>>>", "onResponse::::: " + response);
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
                    Toast.makeText(AddProductActivity.this, "Product Info Added Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("pname", product_name.getText().toString().trim());
                params.put("investment", investment_amount.getText().toString().trim());
                params.put("pcat", product_category.getText().toString().trim());
                params.put("pscat", product_subcategory.getText().toString().trim());
                params.put("purchaser", product_purchaser.getText().toString().trim());
                params.put("value", product_value.getText().toString().trim());
                params.put("frequency", premium_frequency.getText().toString().trim());
                params.put("p_amount", premium_amount.getText().toString().trim());
                params.put("dop", changeDateFormatDB(purchase_date.getText().toString().trim()));
                params.put("n_date", changeDateFormatDB(next_due_date.getText().toString().trim()));
                params.put("m_value", maturity_value.getText().toString().trim());
                params.put("valid_till", changeDateFormatDB(valid_till.getText().toString().trim()));
                params.put("nominee", nominee_name.getText().toString().trim());
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
        Intent intent = new Intent (AddProductActivity.this, DashboardActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("type","Product");
        AddProductActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
