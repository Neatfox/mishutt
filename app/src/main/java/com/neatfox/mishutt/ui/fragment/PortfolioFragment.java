package com.neatfox.mishutt.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.neatfox.mishutt.ui.adapter.PortfolioAdapter;
import com.neatfox.mishutt.ui.model.PortfolioCategory;
import com.neatfox.mishutt.ui.model.Portfolio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_portfolio_list;

public class PortfolioFragment extends Fragment {

    NetworkInfo networkInfo;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Activity activity;
    Context context;
    CoordinatorLayout layout;
    SwipeRefreshLayout swipeRefreshLayout;
    ExpandableListView expandableListView;
    PortfolioAdapter adapter;
    ArrayList<PortfolioCategory> portfolio_category = new ArrayList<>();
    ArrayList<Portfolio> portfolio_list = new ArrayList<>();
    TextView no_list;
    ProgressBar loading;

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        activity = getActivity();
        context = getContext();

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();

        sharedPreference = activity.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = view.findViewById(R.id.layout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        expandableListView = view.findViewById(R.id.expandable_list_view);
        no_list = view.findViewById(R.id.tv_no_list);
        loading = view.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        if (networkInfo != null && networkInfo.isConnected()) {
            getPortfolioList();
        } else {
            noNetwork();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkInfo != null && networkInfo.isConnected()) {
                    getPortfolioList();
                } else {
                    noNetwork();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });

        return view;
    }

    private void getPortfolioList(){
        portfolio_category = new ArrayList<>();
        portfolio_list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_portfolio_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("PortfolioList", "onResponse: " + response);
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

                    if (!portfolio_category.isEmpty())
                        portfolio_category.clear();

                    if (!portfolio_list.isEmpty())
                        portfolio_list.clear();

                    try {
                        JSONObject jsonObject = new JSONObject(resObj.getString("pro_list"));
                        portfolio_category = new ArrayList<>();

                        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
                            String value = iter.next();
                            portfolio_list = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray(value);
                            for (int j = 0; j < jsonArray.length(); j++) {
                                JSONObject object = jsonArray.getJSONObject(j);
                                Portfolio portfolio = new Portfolio();
                                portfolio.setUser_id(object.optString("uid"));
                                portfolio.setPortfolio_id(object.optString("id"));
                                portfolio.setProduct_name(object.optString("pname"));
                                portfolio.setProduct_category(object.optString("pcat"));
                                portfolio.setInvestment_date(object.optString("idate"));
                                portfolio.setCredit_debit(object.optString("atype"));
                                portfolio.setCredit(object.optString("deposit"));
                                portfolio.setDebit(object.optString("withdraw"));
                                portfolio.setProfit_loss(object.optString("profittype"));
                                portfolio.setProfit(object.optString("gain"));
                                portfolio.setLoss(object.optString("loss"));
                                portfolio_list.add(portfolio);
                            }
                            portfolio_category.add(new PortfolioCategory(value, portfolio_list));
                        }
                        adapter = new PortfolioAdapter(context, portfolio_category);
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
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                return params;
            }
        };
        int socketTimeout = 5000;// 5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
