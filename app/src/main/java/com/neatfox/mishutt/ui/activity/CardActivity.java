package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.CardAdapter;
import com.neatfox.mishutt.ui.model.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.neatfox.mishutt.Constants.api_credit_card_list;
import static com.neatfox.mishutt.Constants.api_debit_card_list;

public class CardActivity extends MainActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    ArrayList<Card> card_list = new ArrayList<>();
    CardAdapter adapter;
    TextView no_list;
    ProgressBar loading;
    String _api;
    int backPress=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_card, contentFrameLayout);

        CardActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        layoutManager = new LinearLayoutManager(CardActivity.this);
        recyclerView = findViewById(R.id.recyclerView);
        no_list = findViewById(R.id.tv_no_list);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        tv_toolbar.setText(getIntent().getStringExtra("item_name"));

        if ("Debit Card".equalsIgnoreCase(getIntent().getStringExtra("item_name")))
            _api = api_debit_card_list;
        else if ("Credit Card".equalsIgnoreCase(getIntent().getStringExtra("item_name")))
            _api = api_credit_card_list;

        if (isNetworkAvailable()) noNetwork();
        else getCards(_api);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) noNetwork();
                else getCards(_api);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });
    }

    private void getCards(final String api){
        card_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(api);
                Log.i("Card List>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!card_list.isEmpty()) {
                        card_list.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Card card = new Card();
                            card.setId(jsonObject.optString("cardid"));
                            card.setBank_name(jsonObject.optString("name_of_bank"));
                            card.setCard_name(jsonObject.optString("cardname"));
                            card.setImage(jsonObject.optString("image"));
                            card.setFirst_year_fee(jsonObject.optString("1styearfee"));
                            card.setRewards(jsonObject.optString("Rewards"));
                            card.setJoining_perks(jsonObject.optString("JoiningPerks"));
                            card.setWhat_love(jsonObject.optString("Whatlove"));
                            card.setDocuments(jsonObject.optString("Documents"));
                            card.setYou_love(jsonObject.optString("Youlove"));
                            card.setFee_details(jsonObject.optString("FeeDetails"));
                            card_list.add(card);
                        }
                        recyclerView.setLayoutManager(layoutManager);
                        adapter = new CardAdapter(card_list,CardActivity.this,CardActivity.this);
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
        Intent intent = new Intent (CardActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        CardActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
