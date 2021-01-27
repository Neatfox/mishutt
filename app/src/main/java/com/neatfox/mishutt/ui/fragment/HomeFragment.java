package com.neatfox.mishutt.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.activity.AddMoneyActivity;
import com.neatfox.mishutt.ui.activity.CardActivity;
import com.neatfox.mishutt.ui.activity.CheckInsuranceActivity;
import com.neatfox.mishutt.ui.activity.CheckLoanActivity;
import com.neatfox.mishutt.ui.activity.CreditScoreActivity;
import com.neatfox.mishutt.ui.activity.DescriptionActivity;
import com.neatfox.mishutt.ui.activity.EMICalculatorActivity;
import com.neatfox.mishutt.ui.activity.ExpenseManagerActivity;
import com.neatfox.mishutt.ui.activity.FDCalculatorActivity;
import com.neatfox.mishutt.ui.activity.FundManagerActivity;
import com.neatfox.mishutt.ui.activity.GoalManagerActivity;
import com.neatfox.mishutt.ui.activity.InvestmentManagerActivity;
import com.neatfox.mishutt.ui.activity.LoanManagerActivity;
import com.neatfox.mishutt.ui.activity.TaxActivity;
import com.neatfox.mishutt.ui.activity.WalletActivity;
import com.neatfox.mishutt.ui.adapter.GridViewAdapter;
import com.neatfox.mishutt.ui.adapter.ImageSliderAdAdapter;
import com.neatfox.mishutt.ui.adapter.ImageSliderBannerAdapter;
import com.neatfox.mishutt.ui.adapter.ImageSliderGoalAdapter;
import com.neatfox.mishutt.ui.model.ImageSlider;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.REQUEST_SMS_PERMISSION;
import static com.neatfox.mishutt.Constants.addCommaDouble;
import static com.neatfox.mishutt.Constants.addCommaString;
import static com.neatfox.mishutt.Constants.api_ad;
import static com.neatfox.mishutt.Constants.api_banner;
import static com.neatfox.mishutt.Constants.api_goal_list;
import static com.neatfox.mishutt.Constants.api_transaction_earning_spending;
import static com.neatfox.mishutt.Constants.api_wallet_balance;
import static com.neatfox.mishutt.DescriptionStrings.bank_IFSC_code_description;
import static com.neatfox.mishutt.DescriptionStrings.cibil_description;

public class HomeFragment extends Fragment {

     NetworkInfo networkInfo;
     SharedPreferences sharedPreference;
     SharedPreferences.Editor editor;
     Activity activity;
     Context context;
     CoordinatorLayout layout;
     CardView cardView_expense_manager,cardView_goal,cardView_wallet;
     LinearLayout layout_banner,layout_expense_manager,layout_goal_manager;
     TextView date,earning,spending,alert,no_expense,goal_type,goal_amount,duration,down_payment,emi,
             no_goal,wallet_balance,add_money;
     ProgressBar loading_expense_manager,loading_goal_manager;
     SliderView sliderView_banner,sliderView_ad;
     ArrayList<ImageSlider> image_list_banner = new ArrayList<>();
    ArrayList<ImageSlider> image_list_ad = new ArrayList<>();
    GridView gridViewLess,gridViewMore;
     GridViewAdapter adapterLess;
     ShimmerFrameLayout mShimmerViewContainer;
     TabLayout tabLayout;

     final String[] image_title = { "Loans", "Investment", "Funding", "Student Finance",
             "Start Up Funding", "More"
     };

     final int[] image = {
             R.drawable.loans,
             R.drawable.investment,
             R.drawable.funding,
             R.drawable.student,
             R.drawable.startup,
             R.drawable.down
     };

     final String[] image_title_more = { "Loans", "Investment","Funding", "Student Finance",
             "Start Up Funding", "Education Loan", "Term Insurance", "Credit Score",
             "EMI Calculator", "FD Calculator", "Credit Card", "Tax", "CIBIL", "Bank IFSC Code",
             "Less"
     };

    final int[] image_more = {
            R.drawable.loans,
            R.drawable.investment,
            R.drawable.funding,
            R.drawable.student,
            R.drawable.startup,
            R.drawable.education,
            R.drawable.term_insurance,
            R.drawable.credit_score,
            R.drawable.financial_tools,
            R.drawable.fd_calculator,
            R.drawable.card,
            R.drawable.tax,
            R.drawable.cibil,
            R.drawable.bank,
            R.drawable.up,
    };

    final int [] slider_goal_image_first = {
            R.drawable.target,
            R.drawable.trophy_yellow,
    };

    final String[] slider_goal_header_first = {
            "Targeting 1 Million +",
            "Bokaro Best",
    };

    final String[] slider_goal_description_first = {
            "Happy customers",
            "Fintech Award",
    };

    final int [] slider_goal_image_second = {
            R.drawable.team,
            R.drawable.five
    };

    final String[] slider_goal_header_second = {
            "Partners",
            "Target Rating"
    };

    final String[] slider_goal_description_second = {
            "Happy customers",
            "on Play Store"
    };

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        activity = getActivity();
        context = getContext();

        sharedPreference = activity.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();

        layout = view.findViewById(R.id.layout);
        layout_banner = view.findViewById(R.id.layout_banner);
        /*.................................Horizontal Scroll View.................................*/
        cardView_expense_manager = view.findViewById(R.id.cardView_expense_manager);
        cardView_goal = view.findViewById(R.id.cardView_goal);
        cardView_wallet = view.findViewById(R.id.cardView_wallet);
        layout_expense_manager = view.findViewById(R.id.layout_expense_manager);
        layout_goal_manager = view.findViewById(R.id.layout_goal_manager);
        date = view.findViewById(R.id.tv_date);
        earning = view.findViewById(R.id.tv_earning);
        spending = view.findViewById(R.id.tv_spending);
        alert = view.findViewById(R.id.tv_alert);
        no_expense = view.findViewById(R.id.tv_no_expense);
        goal_type = view.findViewById(R.id.tv_goal_type);
        goal_amount = view.findViewById(R.id.tv_goal_amount);
        duration = view.findViewById(R.id.tv_duration);
        down_payment = view.findViewById(R.id.tv_down_payment);
        emi = view.findViewById(R.id.tv_emi);
        no_goal = view.findViewById(R.id.tv_no_goal);
        wallet_balance = view.findViewById(R.id.tv_wallet_balance);
        add_money = view.findViewById(R.id.tv_add_money);
        loading_expense_manager = view.findViewById(R.id.loading_expense_manager);
        loading_goal_manager = view.findViewById(R.id.loading_goal_manager);

        if (networkInfo != null && networkInfo.isConnected()) {
            layout_expense_manager.setVisibility(View.GONE);
            layout_goal_manager.setVisibility(View.GONE);
            loading_expense_manager.setVisibility(View.VISIBLE);
            loading_goal_manager.setVisibility(View.VISIBLE);
            cardView_wallet.setVisibility(View.GONE);
            getTransactionTotalEarningSpending();
            getGoalList();
            getWalletBalance();
        } else {
            noNetwork();
        }

        cardView_expense_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemClick("Expense Manager");
            }
        });

        cardView_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemClick("Goal Manager");
            }
        });

        cardView_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, WalletActivity.class));
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        add_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, AddMoneyActivity.class);
                intent.putExtra("wallet_balance",wallet_balance.getText().toString().trim());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
        /*.......................................Grid View........................................*/
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container_one);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        gridViewLess = view.findViewById(R.id.grid_view_less);
        gridViewMore = view.findViewById(R.id.grid_view_more);
        adapterLess = new GridViewAdapter(image,image_title,getContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                gridViewLess.setAdapter(adapterLess);
                setDynamicHeight(gridViewLess);
                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        },1000);

        GridViewAdapter adapterMore = new GridViewAdapter(image_more, image_title_more, getContext());
        gridViewMore.setAdapter(adapterMore);
        setDynamicHeight(gridViewMore);

        gridViewLess.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ("More".equalsIgnoreCase(image_title[+position])) {
                    gridViewLess.setVisibility(View.GONE);
                    gridViewMore.setVisibility(View.VISIBLE);

                } else
                    setItemClick(image_title[+position]);
            }
        });

        gridViewMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ("Less".equalsIgnoreCase(image_title_more[+position])) {
                    gridViewMore.setVisibility(View.GONE);
                    gridViewLess.setVisibility(View.VISIBLE);
                } else
                    setItemClick(image_title_more[+position]);
            }
        });
        /*....................................Image Slider........................................*/
        sliderView_banner = view.findViewById(R.id.imageSlider_banner);
        sliderView_ad = view.findViewById(R.id.imageSlider_ad);

        if (networkInfo != null && networkInfo.isConnected()) {
            getBanner();
            getAd();
        } else {
            noNetwork();
        }

        SliderView sliderView_goal = view.findViewById(R.id.imageSlider_goal);
        ImageSliderGoalAdapter imageSliderGoalAdapter = new ImageSliderGoalAdapter(context, slider_goal_image_first,
                slider_goal_header_first, slider_goal_description_first, slider_goal_image_second,
                slider_goal_header_second, slider_goal_description_second);
        sliderView_goal.setSliderAdapter(imageSliderGoalAdapter);
        sliderView_goal.setIndicatorAnimation(IndicatorAnimations.SWAP);
        sliderView_goal.setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION);
        sliderView_goal.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView_goal.setScrollTimeInSec(5); //set scroll delay in seconds :
        sliderView_goal.startAutoCycle();
        /*.....................................Tab Layout.........................................*/
        tabLayout = view.findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Gold Loan"));
        tabLayout.addTab(tabLayout.newTab().setText("Business Loan"));
        tabLayout.addTab(tabLayout.newTab().setText("Personal Loan"));
        tabLayout.addTab(tabLayout.newTab().setText("Car Loan"));
        tabLayout.addTab(tabLayout.newTab().setText("Life Insurance"));
        tabLayout.addTab(tabLayout.newTab().setText("Health Insurance"));
        tabLayout.addTab(tabLayout.newTab().setText("Home Insurance"));
        tabLayout.addTab(tabLayout.newTab().setText("Corona Virus Health Insurance"));

        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.gold);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.business_loan);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.personal_loan);
        Objects.requireNonNull(tabLayout.getTabAt(3)).setIcon(R.drawable.car);
        Objects.requireNonNull(tabLayout.getTabAt(4)).setIcon(R.drawable.health_insurance);
        Objects.requireNonNull(tabLayout.getTabAt(5)).setIcon(R.drawable.health_care);
        Objects.requireNonNull(tabLayout.getTabAt(6)).setIcon(R.drawable.home_insurance);
        Objects.requireNonNull(tabLayout.getTabAt(7)).setIcon(R.drawable.virus);

        tabLayout.setTabTextColors(Color.parseColor("#27AE60"),Color.parseColor("#27AE60"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String _tab_name = Objects.requireNonNull(tab.getText()).toString().trim();
                setItemClick(_tab_name);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String _tab_name = Objects.requireNonNull(tab.getText()).toString().trim();
                setItemClick(_tab_name);
            }
        });

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions( activity,
                    new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmer();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmer();
        super.onPause();
    }
    /*....................................Set Grid View Height....................................*/
    private void setDynamicHeight(GridView gridView) {
        ListAdapter gridViewAdapter = gridView.getAdapter();
        if (gridViewAdapter == null) {
            return;
        }

        int totalHeight;
        int items = gridViewAdapter.getCount();
        int rows;

        View listItem = gridViewAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        int x;
        if( items > 3 ){
            x = items/3;
            rows = x;
            totalHeight *= rows;
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }
    /*.........................................Item Click.........................................*/
    private void setItemClick(String itemType){
        if (networkInfo != null && networkInfo.isConnected()) {
            if ("Expense Manager".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, ExpenseManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("Goal Manager".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, GoalManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("Loans".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, LoanManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("Investment".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, InvestmentManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("Funding".equalsIgnoreCase(itemType) ||
                    "Start Up Funding".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, FundManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                context.startActivity(intent);

            }  else if ("Education Loan".equalsIgnoreCase(itemType) ||
                    "Student Finance".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent (context, CheckLoanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                context.startActivity(intent);

            } else if ("Credit Score".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, CreditScoreActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("EMI Calculator".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, EMICalculatorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("FD Calculator".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent(context, FDCalculatorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

            } else if ("Tax".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent (context, TaxActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                context.startActivity(intent);

            } else if ("Credit Card".equalsIgnoreCase(itemType)) {
                Intent intent = new Intent (context, CardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                context.startActivity(intent);

            } else if ("CIBIL".equalsIgnoreCase(itemType)){
                Intent intent = new Intent (context, DescriptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                intent.putExtra("item_description",cibil_description);
                context.startActivity(intent);

            } else if ("Bank IFSC Code".equalsIgnoreCase(itemType)){
                Intent intent = new Intent (context, DescriptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                intent.putExtra("item_description",bank_IFSC_code_description);
                context.startActivity(intent);

            } else if (itemType.contains("Loan")) {
                Intent intent = new Intent (context, CheckLoanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                context.startActivity(intent);

            } else if (itemType.contains("Insurance")) {
                Intent intent = new Intent (context, CheckInsuranceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("item_name",itemType);
                context.startActivity(intent);
            }
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            noNetwork();
        }
    }
    /*...........................................Banner...........................................*/
    private void getBanner(){
        image_list_banner = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_banner, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Banner>>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!image_list_banner.isEmpty()) {
                        image_list_banner.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ImageSlider imageSlider = new ImageSlider();
                            imageSlider.setImage_path(jsonObject.optString("sliderimage"));
                            image_list_banner.add(imageSlider);
                            /*....................................Image Slider........................................*/
                            ImageSliderBannerAdapter bannerAdapter = new ImageSliderBannerAdapter(context, image_list_banner);
                            sliderView_banner.setSliderAdapter(bannerAdapter);
                            sliderView_banner.setIndicatorAnimation(IndicatorAnimations.SWAP);
                            sliderView_banner.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                            sliderView_banner.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
                            sliderView_banner.setScrollTimeInSec(5); //set scroll delay in seconds :
                            sliderView_banner.startAutoCycle();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    layout_banner.setVisibility(View.VISIBLE);
                } else {
                    layout_banner.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_banner.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return new HashMap<>();
            }
        };
        int socketTimeout = 5000; // 5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }

    /*............................................Adv.............................................*/
    private void getAd(){
        image_list_ad = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_ad, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Ad>>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    if (!image_list_ad.isEmpty()) {
                        image_list_ad.clear();
                    }
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            ImageSlider imageSlider = new ImageSlider();
                            imageSlider.setImage_path(jsonObject.optString("image"));
                            imageSlider.setUrl(jsonObject.optString("backlink"));
                            image_list_ad.add(imageSlider);
                            /*....................................Image Slider........................................*/
                            ImageSliderAdAdapter imageSliderAdAdapter = new ImageSliderAdAdapter(context,image_list_ad);
                            sliderView_ad.setSliderAdapter(imageSliderAdAdapter);
                            sliderView_ad.setIndicatorAnimation(IndicatorAnimations.SWAP);
                            sliderView_ad.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                            sliderView_ad.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
                            sliderView_ad.setScrollTimeInSec(5); //set scroll delay in seconds :
                            sliderView_ad.startAutoCycle();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    layout_banner.setVisibility(View.VISIBLE);
                } else {
                    layout_banner.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_banner.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return new HashMap<>();
            }
        };
        int socketTimeout = 5000; // 5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }

    private void getTransactionTotalEarningSpending(){
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_earning_spending, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("EarningSpending>>>", "onResponse::::: " + response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (status == 1) {
                    try {
                        float total_earning = Float.parseFloat(resObj.getString("total_earnings"));
                        float total_spending = Float.parseFloat(resObj.getString("total_spents"));

                        earning.setText(String.format(":\b₹%s", addCommaString(resObj.getString("total_earnings"))));
                        spending.setText(String.format(":\b₹%s", addCommaString(resObj.getString("total_spents"))));

                        if (total_earning < total_spending){
                            alert.setText(String.format("Your Expenses is more than your earnings by ₹%s", addCommaDouble(total_spending - total_earning)));
                            alert.setVisibility(View.VISIBLE);
                        } else
                            alert.setVisibility(View.GONE);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                        Calendar firstDayOfCurrentYear = Calendar.getInstance();
                        firstDayOfCurrentYear.set(Calendar.DATE, 1);
                        firstDayOfCurrentYear.set(Calendar.MONTH, 0);
                        System.out.println(sdf.format(firstDayOfCurrentYear.getTime()));

                        date.setText(String.format("%s - %s", sdf.format(firstDayOfCurrentYear.getTime()), sdf.format(Calendar.getInstance().getTime())));
                        layout_expense_manager.setVisibility(View.VISIBLE);
                        no_expense.setVisibility(View.GONE);
                        loading_expense_manager.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    layout_expense_manager.setVisibility(View.GONE);
                    no_expense.setVisibility(View.VISIBLE);
                    loading_expense_manager.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading_expense_manager.setVisibility(View.GONE);
                layout_expense_manager.setVisibility(View.GONE);
                no_expense.setVisibility(View.VISIBLE);
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
    /*........................................Goal Manager........................................*/
    private void getGoalList(){
        StringRequest request = new StringRequest(Request.Method.POST, api_goal_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Goal List>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray("goals");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        goal_type.setText(String.format(":\b%s", jsonObject.optString("goaltype")));
                        goal_amount.setText(String.format(":\b₹%s", addCommaString(jsonObject.optString("goalamnnt"))));
                        duration.setText(String.format(":\b%s Year(s) %s Month(s)", jsonObject.optString("year"), jsonObject.optString("month")));
                        down_payment.setText(String.format(":\b₹%s", addCommaString(jsonObject.optString("downpay"))));
                        emi.setText(String.format(":\b₹%s", addCommaString(jsonObject.optString("memi"))));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loading_goal_manager.setVisibility(View.GONE);
                    layout_goal_manager.setVisibility(View.VISIBLE);
                    no_goal.setVisibility(View.GONE);
                } else {
                    loading_goal_manager.setVisibility(View.GONE);
                    no_goal.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading_goal_manager.setVisibility(View.GONE);
                no_goal.setVisibility(View.VISIBLE);
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
    /*.......................................Wallet Balance.......................................*/
    public void getWalletBalance(){
        StringRequest request = new StringRequest(Request.Method.POST, api_wallet_balance, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Wallet Balance>>>",response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    try {
                        String _wallet_balance = resObj.getJSONObject("show").getString("wallet_amount");
                        wallet_balance.setText(String.format("₹%s", addCommaString(_wallet_balance)));
                        cardView_wallet.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                //params.put("uid", "5f67778e63561");
                return params;
            }
        };
        int socketTimeout = 5000;//5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
