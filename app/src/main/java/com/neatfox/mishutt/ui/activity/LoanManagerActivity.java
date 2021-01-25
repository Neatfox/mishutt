package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.LoanManagerAdapter;
import com.neatfox.mishutt.ui.model.Loan;
import com.neatfox.mishutt.ui.model.LoanType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.neatfox.mishutt.Constants.api_loan_manager_list;

public class LoanManagerActivity extends MainActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    ExpandableListView expandableListView;
    LoanManagerAdapter adapter;
    ArrayList<LoanType>loan_type_list = new ArrayList<>();
    ArrayList<Loan> loan_list = new ArrayList<>();
    TextView no_list;
    ProgressBar loading;
    int backPress=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_loan_manager, contentFrameLayout);
        tv_toolbar.setText(R.string.loan_manager);

        LoanManagerActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        expandableListView = findViewById(R.id.expandable_list_view);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        if (isNetworkAvailable()) noNetwork();
        else getLoans();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) noNetwork();
                else getLoans();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });

        /*expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String item_name = loan_type_list.get(groupPosition).getType();
                Intent intent = new Intent (LoanManagerActivity.this, CheckLoanActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("type","Loan Manager");
                intent.putExtra("item_name",item_name);
                LoanManagerActivity.this.startActivity(intent);
                return false;
            }
        });*/
    }

    private void getLoans(){
        loan_type_list = new ArrayList<>();
        loan_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_loan_manager_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("LoanList", "onResponse: " + response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    expandableListView.setVisibility(View.VISIBLE);
                    no_list.setVisibility(View.GONE);
                    loading.setVisibility(View.GONE);

                    if (!loan_type_list.isEmpty())
                        loan_type_list.clear();

                    if (!loan_list.isEmpty())
                        loan_list.clear();

                    try {
                        JSONObject jsonObject = new JSONObject(resObj.getString("list"));
                        loan_type_list = new ArrayList<>();

                        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
                            String value = iter.next();
                            loan_list = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray(value);
                            if (jsonArray.length()>0){
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    JSONObject object = jsonArray.getJSONObject(j);
                                    Loan loan = new Loan();
                                    loan.setId(object.optString("id"));
                                    loan.setBank_name(object.optString("name_of_bank"));
                                    loan.setLoan_type(object.optString("typeofloan"));
                                    loan.setMin_interest_rate(object.optString("interest_ratemin"));
                                    loan.setMax_interest_rate(object.optString("interest_ratemax"));
                                    loan.setProcessing_fee(object.optString("processing_fee"));
                                    loan.setMin_loan_amount(object.optString("loan_amountmin"));
                                    loan.setMax_loan_amount(object.optString("loan_amountmax"));
                                    loan.setMin_tenure(object.optString("tenure_rangemin"));
                                    loan.setMax_tenure(object.optString("tenure_rangemax"));
                                    loan_list.add(loan);
                                }
                                loan_type_list.add(new LoanType(value, loan_list));
                            }
                        }
                        adapter = new LoanManagerAdapter(LoanManagerActivity.this, loan_type_list);
                        expandableListView.setAdapter(adapter);
                        for(int i = 0; i < adapter.getGroupCount(); i++)
                            expandableListView.expandGroup(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    loading.setVisibility(View.GONE);
                    expandableListView.setVisibility(View.GONE);
                    no_list.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
                no_list.setVisibility(View.VISIBLE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return new HashMap<>();
            }
        };
        int socketTimeout = 5000;// 5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(this).addToRequestQueue(request);
    }
    /*......................................For BackPress.........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            showHome();
        } else {
            finishAffinity();
        }
    }

    public void backPressed(){
        Intent intent = new Intent (LoanManagerActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        LoanManagerActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
