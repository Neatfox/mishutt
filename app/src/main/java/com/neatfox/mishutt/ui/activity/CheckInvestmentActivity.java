package com.neatfox.mishutt.ui.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.neatfox.mishutt.ui.adapter.CheckInvestmentAdapter;
import com.neatfox.mishutt.ui.model.InputFilterMinMax;
import com.neatfox.mishutt.ui.model.Investment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.neatfox.mishutt.Constants.api_investments_list;

public class CheckInvestmentActivity extends MainActivity {

    LinearLayout layout_info,layout_list,layout_button;
    TextView investment_header;
    EditText name,mobile_number,date_of_birth,annual_income,invest_amount,tenure,interest_rate,monthly_savings;
    Button check;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    CheckInvestmentAdapter adapter;
    ArrayList<Investment> investment_list = new ArrayList<>();
    TextView no_list;
    ProgressBar loading;
    boolean is_visible_list = false;
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_check_investment, contentFrameLayout);

        CheckInvestmentActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        layout_info = findViewById(R.id.layout_info);
        layout_list = findViewById(R.id.layout_list);
        layout_button = findViewById(R.id.layout_button);
        investment_header = findViewById(R.id.tv_investment_header);
        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        date_of_birth = findViewById(R.id.et_date_of_birth);
        annual_income = findViewById(R.id.et_annual_income);
        invest_amount = findViewById(R.id.et_invest_amount);
        tenure = findViewById(R.id.et_tenure);
        interest_rate = findViewById(R.id.et_interest_rate);
        monthly_savings = findViewById(R.id.et_monthly_savings);
        check = findViewById(R.id.button_check);
        layoutManager = new LinearLayoutManager(CheckInvestmentActivity.this);
        recyclerView = findViewById(R.id.recyclerView);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);
        tenure.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "50")});

        tv_toolbar.setText(getIntent().getStringExtra("item_name"));
        investment_header.setText(String.format("Get %s According to Your Eligibility", getIntent().getStringExtra("item_name")));
        name.setText(_name);
        mobile_number.setText(_mobile_number);
        date_of_birth.setText(_date_of_birth);
        annual_income.setText(_annual_income);
        if (!"".equalsIgnoreCase(_annual_income)){
            double _monthly_savings = Double.parseDouble(_annual_income)/12*40/100;
            String _savings = String.valueOf(_monthly_savings);
            monthly_savings.setText(_savings);
        }
        /*......................................Date Picker.......................................*/
        date_of_birth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    date_of_birth.setText("");
                    datePicker();
                }
            }
        });

        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date_of_birth.getText().toString().trim().length()<10)
                    datePicker();
                else
                    annual_income.requestFocus();
            }
        });
        /*.........................................Check..........................................*/
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkName(view) && checkMobileNumber(view) && checkDateOfBirth(view) &&
                        checkAnnualIncome(view) && checkInvestAmount(view) && checkTenure(view) &&
                        checkInterestRate(view) && checkMonthlySavings(view)){
                    if (isNetworkAvailable()) noNetwork();
                    else checkInvestment();
                }
            }
        });
    }
    /*.........................................Date Picker........................................*/
    public void datePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog (CheckInvestmentActivity.this,
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
            date_of_birth.setText(date);
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
    /*.....................................Check Date of Birth....................................*/
    public boolean checkDateOfBirth (View view) {
        if (date_of_birth.getText().toString().trim().length()<10){
            date_of_birth.setText("");
            date_of_birth.requestFocus();
            Snackbar.make(view, R.string.select_date_of_birth, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Annual Income....................................*/
    public boolean checkAnnualIncome (View view) {
        if (annual_income.getText().toString().trim().length()<1){
            annual_income.requestFocus();
            Snackbar.make(view, R.string.enter_annual_income, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Invest Amount.....................................*/
    public boolean checkInvestAmount (View view) {
        if (invest_amount.getText().toString().trim().length()<1){
            invest_amount.requestFocus();
            Snackbar.make(view, R.string.enter_invest_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*........................................Check Tenure........................................*/
    public boolean checkTenure (View view) {
        if (tenure.getText().toString().trim().length()<1){
            tenure.requestFocus();
            Snackbar.make(view, R.string.enter_tenure, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Interest Rate....................................*/
    public boolean checkInterestRate (View view) {
        if (interest_rate.getText().toString().trim().length()<1){
            interest_rate.requestFocus();
            Snackbar.make(view, R.string.enter_interest_rate, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Monthly Savings....................................*/
    public boolean checkMonthlySavings (View view) {
        if (monthly_savings.getText().toString().trim().length()<1){
            monthly_savings.requestFocus();
            Snackbar.make(view, R.string.enter_monthly_savings, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    private void checkInvestment(){
        CheckInvestmentActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        layout_info.setVisibility(View.INVISIBLE);
        layout_list.setVisibility(View.VISIBLE);
        layout_button.setVisibility(View.GONE);
        is_visible_list = true;
        loading.setVisibility(View.VISIBLE);
        investment_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_investments_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Investment List>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!investment_list.isEmpty()) {
                        investment_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("rs");
                        if (jsonArray.length()>0){
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Investment investment = new Investment();
                                investment.setId(jsonObject.optString("id"));
                                investment.setInvestment_type(jsonObject.optString("typeofinvestment"));
                                investment.setScheme_name(jsonObject.optString("SchemeName"));
                                investment.setMin_invest_amount(jsonObject.optString("InvestmentAmountmin"));
                                investment.setMax_invest_amount(jsonObject.optString("InvestmentAmountmax"));
                                investment.setAum(jsonObject.optString("AUM"));
                                investment.setExit_load(jsonObject.optString("Exitload"));
                                investment_list.add(investment);
                            }
                            recyclerView.setLayoutManager(layoutManager);
                            adapter = new CheckInvestmentAdapter(investment_list,
                                    invest_amount.getText().toString().trim(),
                                    CheckInvestmentActivity.this,CheckInvestmentActivity.this);
                            recyclerView.setAdapter(adapter);

                            loading.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            no_list.setVisibility(View.GONE);
                        } else {
                            loading.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            no_list.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                params.put("type", getIntent().getStringExtra("item_name"));
                params.put("income", annual_income.getText().toString().trim());
                params.put("invest", invest_amount.getText().toString().trim());
                params.put("tenure", tenure.getText().toString().trim());
                params.put("rate", interest_rate.getText().toString().trim());
                params.put("others", monthly_savings.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(this).addToRequestQueue(request);
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
        if (is_visible_list){
            layout_info.setVisibility(View.VISIBLE);
            layout_list.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            layout_button.setVisibility(View.VISIBLE);
            no_list.setVisibility(View.GONE);
            backPress = 0;
            is_visible_list = false;

        } else if ("Goal Manager".equalsIgnoreCase(getIntent().getStringExtra("type"))){
            Intent intent = new Intent (CheckInvestmentActivity.this, AddGoalActivity.class);
            intent.putExtra("annual_income",_annual_income);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CheckInvestmentActivity.this.startActivity(intent);

        } else if ("Investment Manager".equalsIgnoreCase(getIntent().getStringExtra("type"))){
            Intent intent = new Intent (CheckInvestmentActivity.this, InvestmentManagerActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CheckInvestmentActivity.this.startActivity(intent);

        } else {
            Intent intent = new Intent (CheckInvestmentActivity.this, HomeActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CheckInvestmentActivity.this.startActivity(intent);
        }
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
