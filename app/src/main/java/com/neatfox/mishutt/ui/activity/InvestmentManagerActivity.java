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
import com.neatfox.mishutt.ui.adapter.InvestmentManagerAdapter;
import com.neatfox.mishutt.ui.model.Investment;
import com.neatfox.mishutt.ui.model.InvestmentType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.neatfox.mishutt.Constants.api_investment_manager_list;

public class InvestmentManagerActivity extends MainActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    ExpandableListView expandableListView;
    InvestmentManagerAdapter adapter;
    ArrayList<InvestmentType> investment_type_list = new ArrayList<>();
    ArrayList<Investment> investment_list = new ArrayList<>();
    TextView no_list;
    ProgressBar loading;
    int backPress=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_investment_manager, contentFrameLayout);
        tv_toolbar.setText(R.string.investment_manager);

        InvestmentManagerActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        expandableListView = findViewById(R.id.expandable_list_view);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        if (isNetworkAvailable()) noNetwork();
        else getInvestments();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) noNetwork();
                else getInvestments();
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
                String item_name = investment_type_list.get(groupPosition).getType();
                Intent intent = new Intent (InvestmentManagerActivity.this, CheckInvestmentActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("type","Investment Manager");
                intent.putExtra("item_name",item_name);
                InvestmentManagerActivity.this.startActivity(intent);
                return false;
            }
        });*/
    }

    private void getInvestments(){
        investment_type_list = new ArrayList<>();
        investment_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_investment_manager_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("InvestmentList", "onResponse: " + response);
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

                    if (!investment_type_list.isEmpty())
                        investment_type_list.clear();

                    if (!investment_list.isEmpty())
                        investment_list.clear();

                    try {
                        JSONObject jsonObject = new JSONObject(resObj.getString("inv_list"));
                        investment_type_list = new ArrayList<>();

                        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
                            String value = iter.next();
                            investment_list = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray(value);
                            if (jsonArray.length()>0){
                                for (int j = 0; j < jsonArray.length(); j++) {
                                    JSONObject object = jsonArray.getJSONObject(j);
                                    Investment investment = new Investment();
                                    investment.setId(object.optString("id"));
                                    investment.setInvestment_type(object.optString("typeofinvestment"));
                                    investment.setScheme_name(object.optString("SchemeName"));
                                    investment.setMin_invest_amount(object.optString("InvestmentAmountmin"));
                                    investment.setMax_invest_amount(object.optString("InvestmentAmountmax"));
                                    investment.setAum(object.optString("AUM"));
                                    investment_list.add(investment);
                                }
                                investment_type_list.add(new InvestmentType(value, investment_list));
                            }
                        }
                        adapter = new InvestmentManagerAdapter(InvestmentManagerActivity.this, investment_type_list);
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
        Intent intent = new Intent (InvestmentManagerActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        InvestmentManagerActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
