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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_portfolio_add;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class AddPortfolioActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    ProgressDialog progressDialog;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_info,layout_button;
    ImageButton ib_back;
    EditText product_name,product_category,date_of_investment,invest_withdraw_amount,profit_loss_amount;
    AppCompatAutoCompleteTextView transaction_type,income_type;
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
        setContentView(R.layout.activity_add_portfolio);

        AddPortfolioActivity.this.getWindow().setSoftInputMode(
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
        product_category = findViewById(R.id.et_product_category);
        date_of_investment = findViewById(R.id.et_date_of_investment);
        transaction_type = findViewById(R.id.et_transaction_type);
        invest_withdraw_amount = findViewById(R.id.et_invest_withdraw_amount);
        income_type = findViewById(R.id.et_type_of_income);
        profit_loss_amount = findViewById(R.id.et_profit_loss_amount);
        submit = findViewById(R.id.button_submit);
        /*........................................Spinner.........................................*/
        ArrayAdapter<CharSequence> transactionAdapter = ArrayAdapter.createFromResource(
                AddPortfolioActivity.this, R.array.portfolio_transaction_type, R.layout.adapter_spinner_item);
        transactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transaction_type.setAdapter(transactionAdapter);

        ArrayAdapter<CharSequence> incomeAdapter = ArrayAdapter.createFromResource(
                AddPortfolioActivity.this, R.array.income_type, R.layout.adapter_spinner_item);
        incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        income_type.setAdapter(incomeAdapter);

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
                    invest_withdraw_amount.requestFocus();
            }
        });

        income_type.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    income_type.showDropDown();
                }
            }
        });

        income_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (income_type.getText().toString().trim().length()<4){
                    income_type.showDropDown();
                } else
                    profit_loss_amount.requestFocus();
            }
        });
        /*......................................Date Picker.......................................*/
        date_of_investment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    date_of_investment.setText("");
                    datePicker();
                }
            }
        });

        date_of_investment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date_of_investment.getText().toString().trim().length()<10)
                    datePicker();
                else
                    transaction_type.requestFocus();
            }
        });
        /*.........................................Submit.........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkProductName(view) && checkProductCategory(view) && checkInvestmentDate(view) &&
                        checkTransactionType(view) && checkTransactionAmount(view) &&
                        checkIncomeType(view) && checkIncomeAmount(view)){
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
    public void datePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog (AddPortfolioActivity.this,
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
            date_of_investment.setText(date);
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
    /*...................................Check Product Category...................................*/
    public boolean checkProductCategory(View view) {
        if (product_category.getText().toString().trim().length()<1){
            product_category.requestFocus();
            Snackbar.make(view, R.string.enter_product_category, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Investment Date....................................*/
    public boolean checkInvestmentDate(View view) {
        if (date_of_investment.getText().toString().trim().length()<1){
            date_of_investment.requestFocus();
            Snackbar.make(view, R.string.select_date_of_investment, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Transaction Type...................................*/
    public boolean checkTransactionType(View view) {
        if (transaction_type.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < transaction_type.getAdapter().getCount(); i++) {
                if (transaction_type.getText().toString().trim().equalsIgnoreCase(transaction_type.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            transaction_type.setText("");
        }
        transaction_type.requestFocus();
        Snackbar.make(view, R.string.select_transaction_type, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*..................................Check Transaction Amount..................................*/
    public boolean checkTransactionAmount(View view) {
        if (invest_withdraw_amount.getText().toString().trim().length()<1){
            invest_withdraw_amount.requestFocus();
            Snackbar.make(view, R.string.enter_invest_withdraw_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Income Type......................................*/
    public boolean checkIncomeType(View view) {
        if (income_type.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < income_type.getAdapter().getCount(); i++) {
                if (income_type.getText().toString().trim().equalsIgnoreCase(income_type.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            income_type.setText("");
        }
        income_type.requestFocus();
        Snackbar.make(view, R.string.select_income_type, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*....................................Check Income Amount.....................................*/
    public boolean checkIncomeAmount(View view) {
        if (profit_loss_amount.getText().toString().trim().length()<1){
            profit_loss_amount.requestFocus();
            Snackbar.make(view, R.string.enter_profit_loss_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Submit Details.......................................*/
    private void submitDetails(){
        progressDialog = new ProgressDialog(AddPortfolioActivity.this);
        progressDialog.setMessage("Saving Portfolio Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_portfolio_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Portfolio Info>>>", "onResponse::::: " + response);
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
                    Toast.makeText(AddPortfolioActivity.this, "Portfolio Details Added Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("name", product_name.getText().toString().trim());
                params.put("cat", product_category.getText().toString().trim());
                params.put("DOI", changeDateFormatDB(date_of_investment.getText().toString().trim()));
                if ("Invest".equalsIgnoreCase(transaction_type.getText().toString().trim()))
                    params.put("atype","1" );
                else if ("Withdraw".equalsIgnoreCase(transaction_type.getText().toString().trim()))
                    params.put("atype","0" );
                params.put("investment", invest_withdraw_amount.getText().toString().trim());
                if ("Profit".equalsIgnoreCase(income_type.getText().toString().trim()))
                    params.put("ptype","1" );
                else if ("Loss".equalsIgnoreCase(income_type.getText().toString().trim()) )
                    params.put("ptype","0" );
                params.put("gain", profit_loss_amount.getText().toString().trim());
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
        Intent intent = new Intent (AddPortfolioActivity.this, DashboardActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("type","Portfolio");
        AddPortfolioActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
