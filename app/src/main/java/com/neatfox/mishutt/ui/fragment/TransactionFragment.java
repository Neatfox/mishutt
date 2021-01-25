package com.neatfox.mishutt.ui.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
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
import com.neatfox.mishutt.ui.adapter.TransactionAdapter;
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

import static com.neatfox.mishutt.Constants.api_transaction_list;

public class TransactionFragment extends Fragment {

    NetworkInfo networkInfo;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    Activity activity;
    Context context;
    CoordinatorLayout layout;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    TransactionAdapter adapter;
    ArrayList<Transaction> transaction_list = new ArrayList<>();
    AppCompatSpinner spinner;
    Button button_start_date,button_end_date;
    ImageView submit;
    TextView no_list;
    ProgressBar loading;
    long milliseconds;
    int start_date_flag = 0,end_date_flag = 0,spinner_position;
    String start_date = "",end_date = "";

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    public TransactionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        activity = getActivity();
        context = getContext();

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();

        sharedPreference = activity.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        calendar = Calendar.getInstance();

        layout = view.findViewById(R.id.layout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        spinner = view.findViewById(R.id.spinner_transaction_type);
        button_start_date = view.findViewById(R.id.button_start_date);
        button_end_date = view.findViewById(R.id.button_end_date);
        submit = view.findViewById(R.id.iv_submit);
        no_list = view.findViewById(R.id.tv_no_list);
        loading = view.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.transaction_type, R.layout.adapter_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(0);
        spinner_position = 0;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setSelection(position);
                spinner_position = position;
                if (networkInfo != null && networkInfo.isConnected()) {
                    recyclerView.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);
                    getTransactionList();
                } else {
                    loading.setVisibility(View.GONE);
                    noNetwork();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkInfo != null && networkInfo.isConnected()) {
                    getTransactionList();
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
                        recyclerView.setVisibility(View.GONE);
                        loading.setVisibility(View.VISIBLE);
                        getTransactionList();
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

        return view;
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
        // when dialog box is closed, below method will be called.
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

    private void getTransactionList(){
        transaction_list = new ArrayList<>();
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
                    if (!transaction_list.isEmpty()) {
                        transaction_list.clear();
                    }
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
                            if (spinner_position == 0)
                                transaction_list.add(transaction);
                            else if (spinner_position == 1 && "0.00".equalsIgnoreCase(jsonObject.optString("spent")))
                                transaction_list.add(transaction);
                            else if (spinner_position == 2 && "0.00".equalsIgnoreCase(jsonObject.optString("earning")))
                                transaction_list.add(transaction);
                        }
                        recyclerView.setLayoutManager(layoutManager);
                        adapter = new TransactionAdapter(transaction_list,context);
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
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
