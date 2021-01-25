package com.neatfox.mishutt.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.neatfox.mishutt.ui.adapter.GoalInvestmentAdapter;
import com.neatfox.mishutt.ui.adapter.GoalLoanAdapter;
import com.neatfox.mishutt.ui.model.InputFilterMinMax;
import com.neatfox.mishutt.ui.model.Investment;
import com.neatfox.mishutt.ui.model.Loan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.addCommaString;
import static com.neatfox.mishutt.Constants.api_goal_details;
import static com.neatfox.mishutt.Constants.api_goal_investments;
import static com.neatfox.mishutt.Constants.api_goal_loans;
import static com.neatfox.mishutt.Constants.api_goal_save;

public class AddGoalActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_info,layout_analyze_goal,layout_button;
    ImageButton ib_back;
    TextView goal_title;
    TextInputLayout layout_suggestion_two;
    EditText goal_amount,year,month,inflation_rate,suggestion_one,suggestion_two,suggestion_three,
            suggestion_four,suggestion_five;
    AppCompatAutoCompleteTextView goal_type;
    Button submit;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    GoalLoanAdapter goalLoanAdapter;
    GoalInvestmentAdapter goalInvestmentAdapter;
    ArrayList<Loan> loan_list = new ArrayList<>();
    ArrayList<Investment> investment_list = new ArrayList<>();
    String future_value = "",down_payment = "",emi = "";
    boolean is_visible_analyze_goal = false;
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
        setContentView(R.layout.activity_add_goal);

        AddGoalActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        layout_info = findViewById(R.id.layout_info);
        layout_analyze_goal = findViewById(R.id.layout_analyze_goal);
        layout_button = findViewById(R.id.layout_button);
        goal_type = findViewById(R.id.et_goal_type);
        goal_amount = findViewById(R.id.et_goal_amount);
        year = findViewById(R.id.et_year);
        month = findViewById(R.id.et_month);
        inflation_rate = findViewById(R.id.et_inflation_rate);
        goal_title = findViewById(R.id.tv_goal_title);
        layout_suggestion_two = findViewById(R.id.layout_suggestion_two);
        suggestion_one = findViewById(R.id.et_suggestion_one);
        suggestion_two = findViewById(R.id.et_suggestion_two);
        suggestion_three = findViewById(R.id.et_suggestion_three);
        suggestion_four = findViewById(R.id.et_suggestion_four);
        suggestion_five = findViewById(R.id.et_suggestion_five);
        layoutManager = new LinearLayoutManager(AddGoalActivity.this);
        recyclerView = findViewById(R.id.recyclerView);
        submit = findViewById(R.id.button_submit);

        year.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "35")});
        month.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "11")});

        layout_info.setVisibility(View.VISIBLE);
        layout_analyze_goal.setVisibility(View.GONE);

        /*........................................Spinner.........................................*/
        ArrayAdapter<CharSequence> transactionAdapter = ArrayAdapter.createFromResource(
                AddGoalActivity.this, R.array.goal_type, R.layout.adapter_spinner_item);
        transactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goal_type.setAdapter(transactionAdapter);

        goal_type.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    goal_type.showDropDown();
                }
            }
        });

        goal_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goal_type.getText().toString().trim().length()<4){
                    goal_type.showDropDown();
                } else
                    goal_amount.requestFocus();
            }
        });

        year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (year.getText().toString().trim().length()>0)
                    month.requestFocus();
            }
        });
        /*.........................................Submit.........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_visible_analyze_goal){
                    if (isNetworkAvailable()) noNetwork();
                    else submitDetails();
                } else {
                    if (checkGoalType(view) && checkGoalAmount(view) && checkYear(view) &&
                            checkMonth(view) && checkInflationRate(view)){
                        if (isNetworkAvailable()) noNetwork();
                        else {
                            recyclerView.setVisibility(View.GONE);
                            getGoalDetails();
                        }
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
    /*.......................................Check Goal Type......................................*/
    public boolean checkGoalType(View view) {
        if (goal_type.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < goal_type.getAdapter().getCount(); i++) {
                if (goal_type.getText().toString().trim().equalsIgnoreCase(goal_type.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            goal_type.setText("");
        }
        goal_type.requestFocus();
        Snackbar.make(view, R.string.select_goal_type, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*.....................................Check Goal Amount......................................*/
    public boolean checkGoalAmount(View view) {
        if (goal_amount.getText().toString().trim().length()<1){
            goal_amount.requestFocus();
            Snackbar.make(view, R.string.enter_goal_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.........................................Check Year.........................................*/
    public boolean checkYear(View view) {
        if (year.getText().toString().trim().length()<1){
            year.requestFocus();
            Snackbar.make(view, R.string.enter_year, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*........................................Check Month.........................................*/
    public boolean checkMonth(View view) {
        if (month.getText().toString().trim().length()<1){
            month.requestFocus();
            Snackbar.make(view, R.string.enter_month, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Inflation Rate....................................*/
    public boolean checkInflationRate(View view) {
        if (inflation_rate.getText().toString().trim().length()<1){
            inflation_rate.requestFocus();
            Snackbar.make(view, R.string.enter_inflation_rate, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    private void getGoalDetails(){
        progressDialog = new ProgressDialog(AddGoalActivity.this);
        progressDialog.setMessage("Generating Goal Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_goal_details, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Goal Details>>>", "onResponse::::: " + response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 0 || status == 1){
                    try {
                        if (resObj != null) {
                            String _year = resObj.getString("year");
                            String _rate_of_interest = resObj.getString("rate");
                            String _minimum_savings = resObj.getString("msaving");
                            String _down_savings = resObj.getString("downsavings");
                            String _total_memi = resObj.getString("total_memi");
                            emi = _minimum_savings;
                            if ("null".equalsIgnoreCase(_total_memi) || "".equalsIgnoreCase(_total_memi))
                                _total_memi = "0";
                            double _total_monthly_emi = Double.parseDouble(_total_memi);
                            double _annual_income = Double.parseDouble(getIntent().getStringExtra("annual_income"));
                            double _monthly_savings = (_annual_income/12)*40/100;
                            double difference;
                            _monthly_savings = _monthly_savings - _total_monthly_emi;

                            String _term_of_goal;
                            if (Double.parseDouble(_year) <= 3)
                                _term_of_goal = "Short Term Goal";
                            else if (Double.parseDouble(_year) > 3 && Double.parseDouble(_year) <= 5)
                                _term_of_goal = "Middle Term Goal";
                            else
                                _term_of_goal = "Long Term Goal";

                            goal_title.setText(resObj.getString("loantype"));
                            future_value = resObj.getString("fval");
                            down_payment = resObj.getString("downpayment");
                            suggestion_one.setText(String.format("Future value will be ₹%s @%s%% for your %s",
                                    addCommaString(future_value), _rate_of_interest, _term_of_goal));
                            suggestion_two.setText(String.format("Minimum down payment required : ₹%s",
                                    addCommaString(down_payment)));

                            if ("Home".equalsIgnoreCase(goal_type.getText().toString().trim()) ||
                                    "Buy a car".equalsIgnoreCase(goal_type.getText().toString().trim())) {
                                layout_suggestion_two.setVisibility(View.VISIBLE);
                                suggestion_three.setText(String.format("Your monthly savings should be : " +
                                        "₹%s to achieve this goal, or at least ₹%s to pay down payment", addCommaString(_minimum_savings), addCommaString(_down_savings)));
                            } else {
                                layout_suggestion_two.setVisibility(View.GONE);
                                suggestion_three.setText(String.format("Your monthly savings should be : " +
                                        "₹%s to achieve this goal", addCommaString(_minimum_savings)));
                            }
                            if (_monthly_savings > Double.parseDouble(_minimum_savings)){
                                difference = _monthly_savings - Double.parseDouble(_minimum_savings);
                                suggestion_four.setText(String.format("Your present monthly savings is : " +
                                        "₹%s which is ₹%s extra on monthly basis to achieve your goal",
                                        addCommaString(String.valueOf(_monthly_savings)), addCommaString(String.valueOf(difference))));
                            } else {
                                difference = Double.parseDouble(_minimum_savings) - _monthly_savings;
                                if (difference == 0)
                                    suggestion_four.setText(String.format("Your present monthly savings is : " +
                                            "₹%s which is sufficient on monthly basis to achieve your goal",
                                            addCommaString(String.valueOf(_monthly_savings))));
                                else {
                                    suggestion_four.setText(String.format("Your present monthly savings is : " +
                                            "₹%s which is ₹%s less on monthly basis to achieve your goal",
                                            addCommaString(String.valueOf(_monthly_savings)), addCommaString(String.valueOf(difference))));
                                }

                                if ("Short Term Goal".equalsIgnoreCase(_term_of_goal)){
                                    String string = "You can achieve your goal by choose a loan";
                                    suggestion_five.setText(string);
                                    if (isNetworkAvailable()) noNetwork();
                                    else getLoanList();
                                } else {
                                    String string = "You can try for investment";
                                    suggestion_five.setText(string);
                                    if (isNetworkAvailable()) noNetwork();
                                    else getInvestmentList();
                                }
                            }
                            layout_info.setVisibility(View.GONE);
                            layout_analyze_goal.setVisibility(View.VISIBLE);
                            is_visible_analyze_goal = true;
                            submit.setText(R.string.save_goal);
                        } else {
                            snackBarError();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                params.put("user_id", sharedPreference.getString("register_id", ""));
                params.put("typeofgoal", goal_type.getText().toString().trim());
                params.put("amount", goal_amount.getText().toString().trim());
                params.put("year", year.getText().toString().trim());
                params.put("month", month.getText().toString().trim());
                params.put("rate", inflation_rate.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*....................................Get Investment List.....................................*/
    private void getInvestmentList(){
        investment_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_goal_investments, new Response.Listener<String>() {
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
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

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
                        goalInvestmentAdapter = new GoalInvestmentAdapter(investment_list,AddGoalActivity.this, AddGoalActivity.this);
                        recyclerView.setAdapter(goalInvestmentAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                recyclerView.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("type", goal_type.getText().toString().trim());
                params.put("year", year.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(this).addToRequestQueue(request);
    }
    /*.......................................Get Loan List........................................*/
    private void getLoanList(){
        loan_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_goal_loans, new Response.Listener<String>() {
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
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

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
                        goalLoanAdapter = new GoalLoanAdapter(loan_list,AddGoalActivity.this, AddGoalActivity.this);
                        recyclerView.setAdapter(goalLoanAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                recyclerView.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("type", goal_type.getText().toString().trim());
                params.put("year", year.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(this).addToRequestQueue(request);
    }
    /*.......................................Submit Details.......................................*/
    private void submitDetails(){
        progressDialog = new ProgressDialog(AddGoalActivity.this);
        progressDialog.setMessage("Saving Goal Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_goal_save, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Save Goal>>>", "onResponse::::: " + response);
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
                    Toast.makeText(AddGoalActivity.this, "Goal Saved Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("goaltype", goal_type.getText().toString().trim());
                params.put("goalamnnt", goal_amount.getText().toString().trim());
                params.put("year", year.getText().toString().trim());
                params.put("month", month.getText().toString().trim());
                params.put("fval", future_value);
                params.put("downpay", down_payment);
                params.put("memi", emi);
                Log.i("Save Goal>>>", "params::::: " + params);
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
        if (is_visible_analyze_goal){
            layout_info.setVisibility(View.VISIBLE);
            layout_analyze_goal.setVisibility(View.GONE);
            is_visible_analyze_goal = false;
            backPress = 0;
            submit.setText(R.string.proceed);
        } else {
            Intent intent = new Intent (AddGoalActivity.this, GoalManagerActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intent.putExtra("type","Goal");
            AddGoalActivity.this.startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }
}
