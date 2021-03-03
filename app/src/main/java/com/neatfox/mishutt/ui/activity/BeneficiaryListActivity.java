package com.neatfox.mishutt.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.BeneficiaryAdapter;
import com.neatfox.mishutt.ui.model.Beneficiary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_beneficiary_list;

public class BeneficiaryListActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    LinearLayout layout,layout_back;
    ImageButton ib_back;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    BeneficiaryAdapter adapter;
    ArrayList<Beneficiary> beneficiary_list = new ArrayList<>();
    FloatingActionButton fab_add;
    TextView no_list;
    ProgressBar loading;
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
        setContentView(R.layout.activity_beneficiary_list);

        BeneficiaryListActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        layoutManager = new LinearLayoutManager(BeneficiaryListActivity.this);
        recyclerView = findViewById(R.id.recyclerView);
        fab_add = findViewById(R.id.fab_add);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            getBeneficiaryList();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    noNetwork();
                } else {
                    getBeneficiaryList();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (BeneficiaryListActivity.this, AddBeneficiaryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                BeneficiaryListActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

    private void getBeneficiaryList(){
        beneficiary_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Beneficiary List>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!beneficiary_list.isEmpty()) {
                        beneficiary_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Beneficiary beneficiary = new Beneficiary();
                            beneficiary.setFirst_name(jsonObject.optString("name"));
                            beneficiary.setLast_name(jsonObject.optString("surname"));
                            beneficiary.setMobile_number(jsonObject.optString("mobile"));
                            beneficiary.setPin_code(jsonObject.optString("pincode"));
                            beneficiary.setBeneficiary_id(jsonObject.optString("beneficiaryid"));
                            beneficiary.setHas_account_info(jsonObject.optString("beneficiary_flag"));
                            beneficiary.setRemitter_id(jsonObject.optString("remitter_id"));
                            beneficiary.setRemitterId(jsonObject.optString("remitterid"));
                            beneficiary.setAccount_number(jsonObject.optString("account"));
                            beneficiary.setIfsc_code(jsonObject.optString("ifsc"));
                            beneficiary_list.add(beneficiary);
                        }
                        recyclerView.setLayoutManager(layoutManager);
                        adapter = new BeneficiaryAdapter(beneficiary_list,BeneficiaryListActivity.this,BeneficiaryListActivity.this);
                        recyclerView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    no_list.setVisibility(View.GONE);
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
                params.put("uid", sharedPreference.getString("register_id", ""));
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
        Intent intent = new Intent (BeneficiaryListActivity.this, WalletActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        BeneficiaryListActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
