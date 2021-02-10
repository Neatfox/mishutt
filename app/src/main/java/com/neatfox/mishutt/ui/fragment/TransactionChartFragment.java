package com.neatfox.mishutt.ui.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.model.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.addCommaDouble;
import static com.neatfox.mishutt.Constants.api_transaction_earning_category_wise;
import static com.neatfox.mishutt.Constants.api_transaction_earning_spending;
import static com.neatfox.mishutt.Constants.api_transaction_earning_spending_by_date;
import static com.neatfox.mishutt.Constants.api_transaction_list;
import static com.neatfox.mishutt.Constants.api_transaction_spending_category_wise;
import static com.neatfox.mishutt.Constants.category;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;
import static com.neatfox.mishutt.Constants.changeDateFormatUI;
import static com.neatfox.mishutt.Constants.type;
import static com.neatfox.mishutt.ui.activity.MainActivity._annual_income;
import static com.neatfox.mishutt.ui.fragment.TransactionFragment._salary;

public class TransactionChartFragment extends Fragment {

    NetworkInfo networkInfo;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    Activity activity;
    Context context;
    CoordinatorLayout layout;
    LinearLayout layout_alert,layout_recommendation,layout_suggestions_earning_spending,layout_suggestions;
    TextView chart_title;
    AppCompatSpinner spinner;
    EditText alert,recommendation_one,recommendation_two,suggestion_one,suggestion_two,suggestion_three,
            suggestion_four,suggestion_five,suggestion_six,suggestion_seven,suggestion_eight;
    Button button_start_date,button_end_date;
    ImageView submit;
    ArrayList<Transaction> transaction_list = new ArrayList<>();
    ArrayList<String> categories_list = new ArrayList<>();
    ArrayList<Float> amount_list = new ArrayList<>();
    PieChart chart;
    ArrayList<PieEntry> entries = new ArrayList<>();
    ArrayList<Integer> colors = new ArrayList<>();
    ProgressBar loading;
    float total_earning = 0,total_spending = 0;
    double annual_income = 0.0;
    long milliseconds;
    int start_date_flag = 0,end_date_flag = 0, nightModeFlags;
    String start_date = "",end_date = "",_earnings,_expenses;

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    public TransactionChartFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_chart, container, false);

        activity = getActivity();
        context = getContext();

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();

        sharedPreference = activity.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        calendar = Calendar.getInstance();

        layout = view.findViewById(R.id.layout);
        layout_alert = view.findViewById(R.id.layout_alert);
        layout_recommendation = view.findViewById(R.id.layout_recommendation);
        layout_suggestions_earning_spending = view.findViewById(R.id.layout_suggestions_earning_spending);
        layout_suggestions = view.findViewById(R.id.layout_suggestions);
        spinner = view.findViewById(R.id.spinner_transaction_type);
        button_start_date = view.findViewById(R.id.button_start_date);
        button_end_date = view.findViewById(R.id.button_end_date);
        submit = view.findViewById(R.id.iv_submit);
        chart_title = view.findViewById(R.id.tv_chart_title);
        chart = view.findViewById(R.id.chart);
        alert = view.findViewById(R.id.et_alert);
        recommendation_one = view.findViewById(R.id.et_recommendation_one);
        recommendation_two = view.findViewById(R.id.et_recommendation_two);
        suggestion_one = view.findViewById(R.id.et_suggestion_one);
        suggestion_two = view.findViewById(R.id.et_suggestion_two);
        suggestion_three = view.findViewById(R.id.et_suggestion_three);
        suggestion_four = view.findViewById(R.id.et_suggestion_four);
        suggestion_five = view.findViewById(R.id.et_suggestion_five);
        suggestion_six = view.findViewById(R.id.et_suggestion_six);
        suggestion_seven = view.findViewById(R.id.et_suggestion_seven);
        suggestion_eight = view.findViewById(R.id.et_suggestion_eight);
        loading = view.findViewById(R.id.loading);

        layout_alert.setVisibility(View.GONE);
        layout_recommendation.setVisibility(View.GONE);
        layout_suggestions_earning_spending.setVisibility(View.GONE);
        layout_suggestions.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                recommendations_suggestions();
            }
        },3000);

        nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        Legend legend = chart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(10f);

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            legend.setTextColor(Color.parseColor("#FFFFFFFF"));
        } else {
            legend.setTextColor(Color.parseColor("#FF000000"));
        }

        chart.setDrawHoleEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.animate();
        chart.setEntryLabelTextSize(10f);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setExtraOffsets(10, 0, 10, 0);

        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.transaction_type_chart, R.layout.adapter_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        button_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_date_flag = 1;
                end_date_flag = 0;
                start_date = "";
                end_date = "";
                button_start_date.setText(R.string.start_date);
                button_end_date.setText(R.string.end_date);
                getDate();
            }
        });

        button_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start_date.length()>1) {
                    start_date_flag = 0;
                    end_date_flag = 1;
                    button_end_date.setText(R.string.end_date);
                    getDate();
                } else {
                    Snackbar.make(layout, R.string.select_start_date, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_date.length()>1 && end_date.length()>1){
                    if (networkInfo != null && networkInfo.isConnected()) {
                        loading.setVisibility(View.VISIBLE);
                        chart.clear();
                        getTransactionList();
                        //getTransactionTotalEarningSpendingByDate();
                    } else {
                        loading.setVisibility(View.GONE);
                        noNetwork();
                    }
                } else if (start_date.length()<1 && end_date.length()<1)
                    Snackbar.make(v, R.string.select_start_end_date, Snackbar.LENGTH_SHORT).show();
                else if (start_date.length()>1 && end_date.length()<1)
                    Snackbar.make(v, R.string.select_end_date, Snackbar.LENGTH_SHORT).show();
                else if (start_date.length()<1 && end_date.length()>1)
                    Snackbar.make(v, R.string.select_start_date, Snackbar.LENGTH_SHORT).show();
                else
                    Snackbar.make(v, R.string.select_start_end_date, Snackbar.LENGTH_SHORT).show();
            }
        });

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                System.out.println(e);
                System.out.println(h);
                if (e.toString().contains(":")){
                    String[] separated = e.toString().split(":");
                    if (spinner.getSelectedItemPosition() == 0){
                        if (Math.abs(Double.parseDouble(_earnings) - Double.parseDouble(separated[2].trim())) <= 1)
                            spinner.setSelection(1);
                        else if (Math.abs(Double.parseDouble(_expenses) - Double.parseDouble(separated[2].trim())) <= 1)
                            spinner.setSelection(2);
                    }
                }
            }

            @Override
            public void onNothingSelected() { }
        });

        return view;
    }

    private void spinnerSelection(int position){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                recommendations_suggestions();
            }
        },3000);

        if (networkInfo != null && networkInfo.isConnected()) {
            chart_title.setVisibility(View.INVISIBLE);
            chart.clear();
            loading.setVisibility(View.VISIBLE);
            layout_alert.setVisibility(View.GONE);
            layout_suggestions_earning_spending.setVisibility(View.GONE);
            getTransactionList();
            if (position == 0){
                //getTransactionTotalEarningSpending();
            } else if (position == 1) {
                //getTransactionEarningCategory();
            } else {
                //getTransactionSpendingCategory();
            }
        } else {
            loading.setVisibility(View.GONE);
            noNetwork();
        }
    }

    private void recommendations_suggestions(){
        /*------------------------------------Recommendations-------------------------------------*/
        if (Double.parseDouble(_annual_income) != 0)
            annual_income = Double.parseDouble( _annual_income);
        else if (Double.parseDouble(_salary) != 0)
            annual_income = Double.parseDouble( _salary)*12;

        System.out.println("Annual Income :"+annual_income);

        if (annual_income == 0)
            layout_recommendation.setVisibility(View.GONE);
        else {
            recommendation_one.setText(String.format("Our Recommendation : You have to save at least ₹%s of your current CTC for any emergency condition in a year.", addCommaDouble(annual_income * 25 / 100)));
            recommendation_two.setText(String.format("Our Recommendation : You have to save at least ₹%s of your total gross for household expenses in a year.", addCommaDouble(annual_income * 28 / 100)));
            layout_recommendation.setVisibility(View.VISIBLE);
        }

        if (annual_income == 0)
            layout_suggestions.setVisibility(View.GONE);
        else {
            double monthly_income = annual_income/12;
            suggestion_three.setText(String.format("Your minimum savings to avail loans should be : ₹%s/month", addCommaDouble(monthly_income * 30 / 100)));
            suggestion_four.setText(String.format("Your maximum expenses not more than : ₹%s/month", addCommaDouble(monthly_income * 60 / 100)));
            suggestion_five.setText(String.format("Your total maximum EMI should not be grater than : ₹%s/month", addCommaDouble(monthly_income * 40 / 100)));
            suggestion_six.setText(String.format("Your minimum retirement fund should be : ₹%s/month", addCommaDouble(monthly_income * 10 / 100)));
            suggestion_seven.setText(String.format("Your maximum withdrawal from retirement fund not more than : ₹%s/month", addCommaDouble(monthly_income * 4/1000)));
            suggestion_eight.setText(String.format("Your minimum savings should be : ₹%s/month", addCommaDouble(monthly_income * 40 / 100)));
            layout_suggestions.setVisibility(View.VISIBLE);
        }
    }
    /*........................................Date Picker.........................................*/
    private void getDate(){
        DatePickerDialog datePicker = new DatePickerDialog(context,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        if (end_date_flag == 1) {
            datePicker.getDatePicker().setMinDate(milliseconds);
        }
        datePicker.setCancelable(false);
        datePicker.show();
    }

    private final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            String date1 = (setDate(selectedYear, selectedMonth + 1, selectedDay));

            if (start_date_flag == 1){
                start_date = date1;
                end_date = "";
            } else if (end_date_flag == 1){
                end_date = date1;
            }

            DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
            DateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy",Locale.getDefault());
            Date date = null;
            try {
                date = inputFormat.parse(date1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert date != null;
            String outputDateStr = outputFormat.format(date);

            try {
                Date date2 = inputFormat.parse(start_date);
                assert date2 != null;
                milliseconds = date2.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (start_date_flag == 1){
                button_start_date.setText(outputDateStr);
            } else if (end_date_flag == 1){
                button_end_date.setText(outputDateStr);
            }
        }
    };

    private String setDate(int year, int month, int day){
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

    private void getTransactionTotalEarningSpendingByDate(){
        layout_suggestions_earning_spending.setVisibility(View.GONE);
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_earning_spending_by_date, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("EarningSpendingDate>>>", "onResponse::::: " + response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!entries.isEmpty())
                    entries.clear();

                if (status == 1) {
                    try {
                        _earnings = resObj.getString("total_earnings");
                        _expenses = resObj.getString("total_spents");
                        total_earning = Float.parseFloat(resObj.getString("total_earnings"));
                        total_spending = Float.parseFloat(resObj.getString("total_spents"));

                        /*---------------------------------Suggestions--------------------------------*/
                        suggestion_one.setText(String.format("Your total earnings : ₹%s", addCommaDouble(total_earning)));
                        suggestion_two.setText(String.format("Your total expenses : ₹%s", addCommaDouble(total_spending)));
                        layout_suggestions_earning_spending.setVisibility(View.VISIBLE);

                        if (total_earning < total_spending){
                            alert.setText(String.format("Your Expenses is more than your earnings by ₹%s", addCommaDouble(total_spending - total_earning)));
                            layout_alert.setVisibility(View.VISIBLE);
                        } else
                            layout_alert.setVisibility(View.GONE);

                        entries.add(new PieEntry(total_earning,"Earnings"));
                        entries.add(new PieEntry(total_spending,"Expenses"));

                        PieDataSet dataSet = new PieDataSet(entries, " ");
                        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                            //dataSet.setValueLineColor(Color.parseColor("#FFFFFFFF"));
                            dataSet.setValueTextColor(Color.parseColor("#FFFFFFFF"));
                        } else {
                            //dataSet.setValueLineColor(Color.parseColor("#FF000000"));
                            dataSet.setValueTextColor(Color.parseColor("#FF000000"));
                        }
                        dataSet.setColors(colors);
                        dataSet.setValueTextSize(10f);
                        PieData data = new PieData(dataSet);
                        chart.setData(data);
                        chart.highlightValues(null);
                        chart.invalidate();

                        chart_title.setText(String.format("Earnings vs Expenses\nfrom %s to %s", start_date, end_date));
                        chart_title.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    loading.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("from", changeDateFormatDB(start_date));
                params.put("to", changeDateFormatDB(end_date));
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
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

                if (!entries.isEmpty())
                    entries.clear();

                if (status == 1) { 
                    try {
                        _earnings = resObj.getString("total_earnings");
                        _expenses = resObj.getString("total_spents");
                        total_earning = Float.parseFloat(resObj.getString("total_earnings"));
                        total_spending = Float.parseFloat(resObj.getString("total_spents"));
                        entries.add(new PieEntry(total_earning,"Earnings"));
                        entries.add(new PieEntry(total_spending,"Expenses"));

                        /*---------------------------------Suggestions--------------------------------*/
                        suggestion_one.setText(String.format("Your total earnings : ₹%s", addCommaDouble(Double.parseDouble(_earnings))));
                        suggestion_two.setText(String.format("Your total expenses : ₹%s", addCommaDouble(Double.parseDouble(_expenses))));
                        layout_suggestions_earning_spending.setVisibility(View.VISIBLE);

                        if (total_earning < total_spending){
                            alert.setText(String.format("Your Expenses is more than your earnings by ₹%s", addCommaDouble(total_spending - total_earning)));
                            layout_alert.setVisibility(View.VISIBLE);
                        } else
                            layout_alert.setVisibility(View.GONE);

                        PieDataSet dataSet = new PieDataSet(entries, " ");
                        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                            //dataSet.setValueLineColor(Color.parseColor("#FFFFFFFF"));
                            dataSet.setValueTextColor(Color.parseColor("#FFFFFFFF"));
                        } else {
                            //dataSet.setValueLineColor(Color.parseColor("#FF000000"));
                            dataSet.setValueTextColor(Color.parseColor("#FF000000"));
                        }
                        dataSet.setColors(colors);
                        dataSet.setValueTextSize(10f);
                        PieData data = new PieData(dataSet);
                        chart.setData(data);
                        chart.highlightValues(null);
                        chart.invalidate();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());

                        Calendar firstDayOfCurrentYear = Calendar.getInstance();
                        firstDayOfCurrentYear.set(Calendar.DATE, 1);
                        firstDayOfCurrentYear.set(Calendar.MONTH, 0);
                        System.out.println(sdf.format(firstDayOfCurrentYear.getTime()));

                        chart_title.setText(String.format("Earnings vs Expenses\nfrom %s to %s", sdf.format(firstDayOfCurrentYear.getTime()), sdf.format(Calendar.getInstance().getTime())));
                        chart_title.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    loading.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
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

    private void getTransactionEarningCategory(){
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_earning_category_wise, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Earning>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!entries.isEmpty())
                    entries.clear();

                if (status == 1) {
                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            entries.add(new PieEntry(
                                    Float.parseFloat(jsonObject.getString("SUM(earning)")),
                                    jsonObject.getString("expcategory")));
                        }
                        PieDataSet dataSet = new PieDataSet(entries, " ");
                        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                            //dataSet.setValueLineColor(Color.parseColor("#FFFFFFFF"));
                            dataSet.setValueTextColor(Color.parseColor("#FFFFFFFF"));
                        } else {
                            //dataSet.setValueLineColor(Color.parseColor("#FF000000"));
                            dataSet.setValueTextColor(Color.parseColor("#FF000000"));
                        }
                        dataSet.setColors(colors);
                        dataSet.setValueTextSize(10f);
                        PieData data = new PieData(dataSet);
                        chart.setData(data);
                        chart.highlightValues(null);
                        chart.invalidate();

                        chart_title.setText(R.string.earnings_category_wise_chart);
                        chart_title.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    }   catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    loading.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
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

    private void getTransactionSpendingCategory(){
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_spending_category_wise, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Spending>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!entries.isEmpty())
                    entries.clear();

                if (status == 1) {
                    try {
                        double _p_expense_amount = 0;
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            entries.add(new PieEntry(
                                    Float.parseFloat(jsonObject.getString("SUM(spent)")),
                                    jsonObject.getString("expcategory")));
                            String c_expense_category,_p_expense_category;
                            c_expense_category = jsonObject.getString("expcategory");

                            double monthly_income = annual_income/12;
                            double _expense_limit = monthly_income * 60 / 100;
                            double _c_expense_amount = Double.parseDouble(jsonObject.getString("SUM(spent)"));
                            if (_c_expense_amount>_p_expense_amount){
                                _p_expense_amount = _c_expense_amount;
                                _p_expense_category = c_expense_category;
                                if (_expense_limit < _p_expense_amount){
                                    alert.setText(String.format("Your Expenses is high in category %s",_p_expense_category));
                                    layout_alert.setVisibility(View.VISIBLE);
                                } else if (alert.getText().toString().trim().length()<5)
                                    layout_alert.setVisibility(View.GONE);
                            }
                        }
                        PieDataSet dataSet = new PieDataSet(entries, " ");
                        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                            //dataSet.setValueLineColor(Color.parseColor("#FFFFFFFF"));
                            dataSet.setValueTextColor(Color.parseColor("#FFFFFFFF"));
                        } else {
                            //dataSet.setValueLineColor(Color.parseColor("#FF000000"));
                            dataSet.setValueTextColor(Color.parseColor("#FF000000"));
                        }
                        dataSet.setColors(colors);
                        dataSet.setValueTextSize(10f);
                        PieData data = new PieData(dataSet);
                        chart.setData(data);
                        chart.highlightValues(null);
                        chart.invalidate();

                        chart_title.setText(R.string.expenses_category_wise_chart);
                        chart_title.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    }   catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    loading.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
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

    private void getTransactionList(){
        chart_title.setVisibility(View.INVISIBLE);
        layout_alert.setVisibility(View.GONE);
        layout_suggestions_earning_spending.setVisibility(View.GONE);
        _salary = "0";
        total_earning = 0;
        total_spending = 0;
        transaction_list = new ArrayList<>();
        entries = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_list, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction List>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    transaction_list.clear();
                    entries.clear();
                    categories_list.clear();
                    amount_list.clear();

                    try {
                        JSONObject jsonRootObject = new JSONObject(response);
                        JSONArray jsonArray = jsonRootObject.getJSONArray("list");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Transaction transaction = new Transaction();
                            transaction.setUser_id(jsonObject.optString("userid"));
                            transaction.setTransaction_list_id(jsonObject.optString("expenceid"));
                            transaction.setCategory(jsonObject.optString("expcategory"));
                            transaction.setDuration(jsonObject.optString("duration"));
                            transaction.setEarning(jsonObject.optString("earning"));
                            transaction.setSpending(jsonObject.optString("spent"));
                            transaction.setDescription(jsonObject.optString("description"));
                            transaction.setRemarks(jsonObject.optString("remarks"));
                            transaction.setDate(jsonObject.optString("entdate"));
                            if ("Salary".equalsIgnoreCase(jsonObject.optString("expcategory")) &&
                                    _salary.equalsIgnoreCase("0"))
                                _salary = jsonObject.optString("earning");

                            if (type(jsonObject.optString("description"))){

                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());

                                Calendar firstDayOfCurrentYear = Calendar.getInstance();
                                firstDayOfCurrentYear.set(Calendar.DATE, 1);
                                firstDayOfCurrentYear.set(Calendar.MONTH, 0);
                                //System.out.println(sdf.format(firstDayOfCurrentYear.getTime()));

                                if (start_date.length()<2 && end_date.length()<2){
                                    start_date = sdf.format(firstDayOfCurrentYear.getTime());
                                    end_date = sdf.format(Calendar.getInstance().getTime());
                                }

                                String txn_date = changeDateFormatUI(jsonObject.optString("entdate"));

                                if (Objects.requireNonNull(sdf.parse(txn_date)).compareTo(sdf.parse(start_date)) >= 0 &&
                                        Objects.requireNonNull(sdf.parse(txn_date)).compareTo(sdf.parse(end_date)) <= 0){

                                    total_earning = total_earning + Float.parseFloat(jsonObject.optString("earning"));
                                    total_spending = total_spending + Float.parseFloat(jsonObject.optString("spent"));

                                    String type = "";
                                    if ("0.00".equalsIgnoreCase(jsonObject.optString("earning"))){
                                        type = "Expense";

                                    } else if ("0.00".equalsIgnoreCase(jsonObject.optString("spent"))){
                                        type = "Earning";
                                    }
                                    String category;
                                    category = category(jsonObject.optString("description"),type);
                                    if ("Others".equalsIgnoreCase(category) &&
                                            !category.equalsIgnoreCase(jsonObject.optString("expcategory"))){
                                        category = jsonObject.optString("expcategory");
                                    }

                                    if (spinner.getSelectedItemPosition() == 1 &&
                                            "0.00".equalsIgnoreCase(jsonObject.optString("spent"))) {
                                        if (!categories_list.contains(category)){
                                            categories_list.add(category);
                                        }
                                        for (int j = 0; j < categories_list.size(); j++){
                                            if (category.equalsIgnoreCase(categories_list.get(j))){
                                                if (amount_list.isEmpty() || j >= amount_list.size()){
                                                    amount_list.add(Float.parseFloat(jsonObject.optString("earning")));
                                                } else {
                                                    amount_list.set(j,amount_list.get(j) + Float.parseFloat(jsonObject.optString("earning")));
                                                }
                                                break;
                                            }
                                        }
                                    } else if (spinner.getSelectedItemPosition() == 2 &&
                                            "0.00".equalsIgnoreCase(jsonObject.optString("earning"))){
                                        if (!categories_list.contains(category)){
                                            categories_list.add(category);
                                        }
                                        for (int j = 0; j < categories_list.size(); j++){
                                            if (category.equalsIgnoreCase(categories_list.get(j))){
                                                if (amount_list.isEmpty() || j >= amount_list.size()){
                                                    amount_list.add(Float.parseFloat(jsonObject.optString("spent")));
                                                } else {
                                                    amount_list.set(j,amount_list.get(j) + Float.parseFloat(jsonObject.optString("spent")));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            transaction_list.add(transaction);
                        }
                        chartTitle();
                        chart();

                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
                loading.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.setVisibility(View.GONE);
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

    private void chartTitle(){
        if (spinner.getSelectedItemPosition() == 0 && start_date.length()<2 && end_date.length()<2){
            chart_title.setText(String.format("Earnings vs Expenses\nfrom %s to %s", start_date, end_date));
        } else if (spinner.getSelectedItemPosition() == 1 && start_date.length()<2 && end_date.length()<2){
            chart_title.setText(String.format("Earnings\nfrom %s to %s", start_date, end_date));
        } else if (spinner.getSelectedItemPosition() == 2 && start_date.length()<2 && end_date.length()<2){
            chart_title.setText(String.format("Expenses\nfrom %s to %s", start_date, end_date));
        } else if (spinner.getSelectedItemPosition() == 0 && start_date.length()>1 && end_date.length()>1){
            chart_title.setText(String.format("Earnings vs Expenses Chart and Suggestions\nfrom %s to %s", start_date, end_date));
        } else if (spinner.getSelectedItemPosition() == 1 && start_date.length()>1 && end_date.length()>1){
            chart_title.setText(String.format("Earnings\nfrom %s to %s", start_date, end_date));
        } else if (spinner.getSelectedItemPosition() == 2 && start_date.length()>1 && end_date.length()>1){
            chart_title.setText(String.format("Expenses\nfrom %s to %s", start_date, end_date));
        }

        chart_title.setVisibility(View.VISIBLE);
    }

    private void chart(){
        if (total_earning > 0 || total_spending > 0){
            _earnings = String.valueOf(total_earning);
            _expenses = String.valueOf(total_spending);

            if (spinner.getSelectedItemPosition() == 0){
                entries.add(new PieEntry(total_earning,"Earnings"));
                entries.add(new PieEntry(total_spending,"Expenses"));

            } else  {
                for (int i = 0; i < categories_list.size(); i++){
                    entries.add(new PieEntry(amount_list.get(i), categories_list.get(i)));
                }
            }
            /*---------------------------------Suggestions--------------------------------*/
            suggestion_one.setText(String.format("Your total earnings : ₹%s", addCommaDouble(Double.parseDouble(_earnings))));
            suggestion_two.setText(String.format("Your total expenses : ₹%s", addCommaDouble(Double.parseDouble(_expenses))));
            layout_suggestions_earning_spending.setVisibility(View.VISIBLE);

            if (total_earning < total_spending){
                alert.setText(String.format("Your Expenses is more than your earnings by ₹%s", addCommaDouble(total_spending - total_earning)));
                layout_alert.setVisibility(View.VISIBLE);
            } else
                layout_alert.setVisibility(View.GONE);

            PieDataSet dataSet = new PieDataSet(entries, " ");
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                //dataSet.setValueLineColor(Color.parseColor("#FFFFFFFF"));
                dataSet.setValueTextColor(Color.parseColor("#FFFFFFFF"));
            } else {
                //dataSet.setValueLineColor(Color.parseColor("#FF000000"));
                dataSet.setValueTextColor(Color.parseColor("#FF000000"));
            }
            dataSet.setColors(colors);
            dataSet.setValueTextSize(10f);
            PieData data = new PieData(dataSet);
            chart.setData(data);
            chart.highlightValues(null);
            chart.invalidate();
        }
    }
}
