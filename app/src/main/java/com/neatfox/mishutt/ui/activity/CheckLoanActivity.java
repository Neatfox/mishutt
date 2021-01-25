package com.neatfox.mishutt.ui.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputLayout;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.CheckLoanAdapter;
import com.neatfox.mishutt.ui.model.InputFilterMinMax;
import com.neatfox.mishutt.ui.model.Loan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.neatfox.mishutt.Constants.api_loans_list;

public class CheckLoanActivity extends MainActivity {

    LinearLayout layout_info,layout_list,layout_button;
    TextView loan_header;
    TextInputLayout layout_tenure;
    EditText name,mobile_number,date_of_birth,annual_income,loan_amount,tenure,interest_rate,other_emi;
    Button check;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    CheckLoanAdapter adapter;
    ArrayList<Loan> loan_list = new ArrayList<>();
    TextView no_list;
    ProgressBar loading;
    boolean is_visible_list = false;
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_check_loan, contentFrameLayout);

        CheckLoanActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        layout_info = findViewById(R.id.layout_info);
        layout_list = findViewById(R.id.layout_list);
        layout_button = findViewById(R.id.layout_button);
        loan_header = findViewById(R.id.tv_loan_header);
        layout_tenure = findViewById(R.id.layout_tenure);
        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        date_of_birth = findViewById(R.id.et_date_of_birth);
        annual_income = findViewById(R.id.et_annual_income);
        loan_amount = findViewById(R.id.et_loan_amount);
        tenure = findViewById(R.id.et_tenure);
        interest_rate = findViewById(R.id.et_interest_rate);
        other_emi = findViewById(R.id.et_other_emi);
        check = findViewById(R.id.button_check);
        layoutManager = new LinearLayoutManager(CheckLoanActivity.this);
        recyclerView = findViewById(R.id.recyclerView);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);

        tv_toolbar.setText(getIntent().getStringExtra("item_name"));
        loan_header.setText(String.format("Get %s According to Your Eligibility", getIntent().getStringExtra("item_name")));
        name.setText(_name);
        mobile_number.setText(_mobile_number);
        date_of_birth.setText(_date_of_birth);
        annual_income.setText(_annual_income);

        if ("Student Finance".equalsIgnoreCase(tv_toolbar.getText().toString().trim())){
            layout_tenure.setSuffixText("Month (s)");
            loan_amount.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "50000")});
            tenure.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "12")});
            check.setText(R.string.apply);
        } else {
            layout_tenure.setSuffixText("Year (s)");
            tenure.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "50")});
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
                        checkAnnualIncome(view) && checkLoanAmount(view) && checkTenure(view) &&
                        checkInterestRate(view)){
                    if (isNetworkAvailable()) noNetwork();
                    else {
                        if ("Student Finance".equalsIgnoreCase(tv_toolbar.getText().toString().trim())){
                            progressDialog = new ProgressDialog(CheckLoanActivity.this);
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setIndeterminate(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run(){
                                    progressDialog.dismiss();
                                    Toast.makeText(CheckLoanActivity.this,
                                            "Thank you for applying to Student Finance.\nOur team members will call you within 24 hrs to take the process further.",
                                            Toast.LENGTH_LONG).show();
                                    backPressed();
                                }
                            },3000);
                        } else {
                            checkLoans();
                        }
                    }
                }
            }
        });
    }
    /*.........................................Date Picker........................................*/
    public void datePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog (CheckLoanActivity.this,
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
    /*.....................................Check Loan Amount......................................*/
    public boolean checkLoanAmount (View view) {
        if (loan_amount.getText().toString().trim().length()<1){
            loan_amount.requestFocus();
            Snackbar.make(view, R.string.enter_loan_amount, Snackbar.LENGTH_SHORT).show();
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

    private void checkLoans(){
        CheckLoanActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        layout_info.setVisibility(View.INVISIBLE);
        layout_list.setVisibility(View.VISIBLE);
        layout_button.setVisibility(View.GONE);
        is_visible_list = true;
        loading.setVisibility(View.VISIBLE);
        loan_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_loans_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Loan List>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!loan_list.isEmpty()) {
                        loan_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("rs");
                        if (jsonArray.length()>0){
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Loan loan = new Loan();
                                loan.setId(jsonObject.optString("id"));
                                loan.setBank_name(jsonObject.optString("name_of_bank"));
                                loan.setLoan_type(jsonObject.optString("typeofloan"));
                                loan.setMin_interest_rate(jsonObject.optString("interest_ratemin"));
                                loan.setMax_interest_rate(jsonObject.optString("interest_ratemax"));
                                loan.setProcessing_fee(jsonObject.optString("processing_fee"));
                                loan.setMin_loan_amount(jsonObject.optString("loan_amountmin"));
                                loan.setMax_loan_amount(jsonObject.optString("loan_amountmax"));
                                loan.setMin_tenure(jsonObject.optString("tenure_rangemin"));
                                loan.setMax_tenure(jsonObject.optString("tenure_rangemax"));
                                loan_list.add(loan);
                            }
                            recyclerView.setLayoutManager(layoutManager);
                            adapter = new CheckLoanAdapter(loan_list,loan_amount.getText().toString().trim(),CheckLoanActivity.this,CheckLoanActivity.this);
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
                params.put("loan", loan_amount.getText().toString().trim());
                params.put("tenure", tenure.getText().toString().trim());
                params.put("rate", interest_rate.getText().toString().trim());
                params.put("others", other_emi.getText().toString().trim());
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
            Intent intent = new Intent (CheckLoanActivity.this, AddGoalActivity.class);
            intent.putExtra("annual_income",_annual_income);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CheckLoanActivity.this.startActivity(intent);

        } else if ("Loan Manager".equalsIgnoreCase(getIntent().getStringExtra("type"))){
            Intent intent = new Intent (CheckLoanActivity.this, LoanManagerActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CheckLoanActivity.this.startActivity(intent);

        } else {
            Intent intent = new Intent (CheckLoanActivity.this, HomeActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CheckLoanActivity.this.startActivity(intent);
        }
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
