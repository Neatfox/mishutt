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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
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
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.TransactionCategoryAdapter;
import com.neatfox.mishutt.ui.model.TransactionCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_transaction_category_list;

public class TransactionCategoryFragment extends Fragment {

    NetworkInfo networkInfo;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Activity activity;
    Context context;
    CoordinatorLayout layout;
    LinearLayout layout_category_headers;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    TransactionCategoryAdapter adapter;
    ArrayList<TransactionCategory> transaction_category = new ArrayList<>();
    TextView no_list;
    ProgressBar loading;

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    public TransactionCategoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_category, container, false);

        activity = getActivity();
        context = getContext();

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();

        sharedPreference = activity.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = view.findViewById(R.id.layout);
        layout_category_headers = view.findViewById(R.id.layout_category_headers);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        no_list = view.findViewById(R.id.tv_no_list);
        loading = view.findViewById(R.id.loading);
        layout_category_headers.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);

        if (networkInfo != null && networkInfo.isConnected()) {
            getTransactionCategories();
        } else {
            noNetwork();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkInfo != null && networkInfo.isConnected()) {
                    getTransactionCategories();
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

    private void getTransactionCategories(){
        transaction_category = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_category_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction Category>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!transaction_category.isEmpty()) {
                        transaction_category.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            TransactionCategory transactionCategory = new TransactionCategory();
                            transactionCategory.setTransaction_category_id(jsonObject.optString("id"));
                            transactionCategory.setCategory(jsonObject.optString("cname"));
                            transactionCategory.setFrequency(jsonObject.optString("frequency"));
                            transaction_category.add(transactionCategory);
                        }
                        recyclerView.setLayoutManager(layoutManager);
                        adapter = new TransactionCategoryAdapter(transaction_category,context);
                        recyclerView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.GONE);
                    layout_category_headers.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    no_list.setVisibility(View.GONE);
                } else {
                    loading.setVisibility(View.GONE);
                    layout_category_headers.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    no_list.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
                layout_category_headers.setVisibility(View.GONE);
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
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
