package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.ViewPagerAdapter;
import com.neatfox.mishutt.ui.fragment.RemindersFragment;
import com.neatfox.mishutt.ui.fragment.TransactionChartFragment;
import com.neatfox.mishutt.ui.fragment.TransactionFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.neatfox.mishutt.Constants.REQUEST_SMS_PERMISSION;
import static com.neatfox.mishutt.Constants.api_add_reminder;
import static com.neatfox.mishutt.Constants.api_transaction_add;
import static com.neatfox.mishutt.Constants.api_transaction_msg_date_time_add;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class ExpenseManagerActivity extends MainActivity {

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    FloatingActionButton fab_add;
    ArrayList<String> sms = new ArrayList<>();
    String type = "", _date_time = "";
    int backPress=0,position = 0;
    int[] colorIntArray = {R.color.green,R.color.green,R.color.green,R.color.green};
    int[] iconIntArray = {R.drawable.fab_add,R.drawable.fab_add,R.drawable.fab_add,R.drawable.fab_add};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_expense_manager, contentFrameLayout);
        tv_toolbar.setText(R.string.expense_manager);

        ExpenseManagerActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (ContextCompat.checkSelfPermission(ExpenseManagerActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED){
            AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseManagerActivity.this);
            builder.setTitle("Mishutt needs access to SMS")
                    .setMessage("Mishutt auto-organises your earnings & expenses by reading your business SMS." +
                            "\nNo personal SMS are read.")
                    .setCancelable(false)
                    .setIcon(R.drawable.mishutt)
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions( ExpenseManagerActivity.this,
                                    new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            getTransactionMessages();
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fab_add = findViewById(R.id.fab_add);
        /*.......................................Tab Layout.......................................*/
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.AddFragment(new TransactionFragment(),"Transactions");
        viewPagerAdapter.AddFragment(new TransactionChartFragment(),"Analyzer");
        viewPagerAdapter.AddFragment(new RemindersFragment(),"Reminders");
        //viewPagerAdapter.AddFragment(new TransactionCategoryFragment(),"Settings");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.parseColor("#27AE60"),Color.parseColor("#27AE60"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                position = tab.getPosition();

                if (tab.getPosition() == 0) {
                    fab_add.setVisibility(View.VISIBLE);
                    animateFab(tab.getPosition());

                } else if (tab.getPosition() == 1){
                    fab_add.setVisibility(View.GONE);

                } else if (tab.getPosition() == 2){
                    fab_add.setVisibility(View.GONE);

                } else {
                    fab_add.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        fab_add.setVisibility(View.VISIBLE);
        animateFab(0);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    Intent intent = new Intent (ExpenseManagerActivity.this, AddTransactionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    ExpenseManagerActivity.this.startActivity(intent);
                } else if (position == 2) {
                    Intent intent = new Intent (ExpenseManagerActivity.this, AddTransactionCategoryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    ExpenseManagerActivity.this.startActivity(intent);
                }
            }
        });

        type = getIntent().getStringExtra("type");
        if ("Transactions".equalsIgnoreCase(type))
            setTab(0);
        else if ("Analyzer".equalsIgnoreCase(type))
            setTab(1);
        else if ("Transaction Category".equalsIgnoreCase(type))
            setTab(2);
        else if ("Reminders".equalsIgnoreCase(type))
            setTab(2);
    }

    public void setTab(int position){
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.select();
    }
    /*..................................Animate Floating Action Button............................*/
    protected void animateFab(final int position) {
        fab_add.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                fab_add.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(),colorIntArray[position]));
                //fab_add.setImageDrawable(getResources().getDrawable(iconIntArray[position], null)); // Applicable from API 21
                fab_add.setImageResource(iconIntArray[position]);

                // Scale up animation
                ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                fab_add.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        fab_add.startAnimation(shrink);
    }
    /*.....................................Message Permission.....................................*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SMS_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getTransactionMessages();
            }
        }
    }
    /*....................................Transaction Messages....................................*/
    public void getTransactionMessages() {
        progressDialog = new ProgressDialog(ExpenseManagerActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        sms = new ArrayList<>();
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body"},null,null,null);

        if (cursor != null) {
            while  (cursor.moveToNext()) {
                String address = cursor.getString(1);
                String date = cursor.getString(2);
                String body = cursor.getString(3);
                Date dateFormat = new Date(Long.parseLong(date));

                if (address.contains("-")) {
                    String[] separated = address.split("-");
                    address = separated[1];
                }

                if (address.trim().length()==6 && isAlpha(address)){
                    if (body.contains("Rs") || body.contains("rs") || body.contains("INR") || body.contains("₹")) {
                        if (body.contains("Cashback") || body.contains("credited")|| body.contains("debited") ||
                                body.contains("withdrawn") || body.contains("Credited")|| body.contains("Debited") ||
                                body.contains("Withdrawn") || body.contains("payment") || body.contains("Transaction ID") ||
                                body.contains("Txn ID") || body.contains("Payment") || body.contains("transaction ID") ||
                                body.contains("txn ID") || body.contains("recharge") || body.contains("added") ||
                                body.contains("paid") || body.contains("Recharge") || body.contains("Added") ||
                                body.contains("Paid") || body.contains("received") || body.contains("bill") ||
                                body.contains("rent") || body.contains("Received") || body.contains("Bill") ||
                                body.contains("Rent") || body.contains("loan") || body.contains("salary") ||
                                body.contains("Salary") || body.contains("Loan") || body.contains("Premium") ||
                                body.contains("ATM") || body.contains("premium") || body.contains("EMI") ||
                                body.contains("Due") || body.contains("due") || body.contains("Reminder") ||
                                body.contains("reminder") || body.contains("spent") || body.contains("Spent")) {

                            long message_date = sharedPreference.getLong("message_date", 0);
                            if (message_date == 0){
                                getMessage(address,body,dateFormat);
                            } else {
                                if (dateFormat.after(new Date(message_date))){
                                    getMessage(address,body,dateFormat);
                                }
                            }
                        }
                    }
                }
            }
            cursor.moveToFirst();
            String _date = cursor.getString(2);
            _date_time = _date;
            editor.putLong("message_date",Long.parseLong(_date));
            editor.commit();
            System.out.println(_date_time);
            submitDateTime();
            progressDialog.dismiss();
            cursor.moveToLast();
            cursor.close();
        } else {
            Toast.makeText(this, "No Messages", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isAlpha(String name) {
        return name.matches("[a-zA-Z]+");
    }

    public void getMessage(String address, String body, Date Date){
        SimpleDateFormat sdf= new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String _date = sdf.format(Date);
        /*------------------------------------Transaction Type------------------------------------*/
        String type = "",category;
        if (body.contains("due") || body.contains("Due")  || body.contains("Reminder") || body.contains("reminder")) {
            type = "Reminder";
        } else if (body.contains("debited") || body.contains("withdrawn") || body.contains("payment") ||
                body.contains("spent") || body.contains("Spent") || body.contains("recharge") ||
                body.contains("paid") || body.contains("Debited") || body.contains("Withdrawn") ||
                body.contains("Payment") || body.contains("Recharge") || body.contains("Paid") ||
                body.contains("Bill") || body.contains("Rent") || body.contains("swiggy") ||
                body.contains("Swiggy") || body.contains("zomato") || body.contains("Zomato") ||
                body.contains("McDonald") || body.contains("subway") || body.contains("Subway") ||
                body.contains("Domino") || body.contains("domino") || body.contains("Pizza") ||
                body.contains("pizza") || body.contains("ATM") || body.contains("EMI")){
            type = "Expense";
        } else if (body.contains("cashback") || body.contains("Cashback") || body.contains("credited") ||
                body.contains("added")  || body.contains("received") || body.contains("salary") ||
                body.contains("Salary") || body.contains("Credited") || body.contains("Added")  ||
                body.contains("Received")) {
            type = "Earning";
        }

        if (body.contains("Cashback") || body.contains("cashback")) {
            category = "Cashback";
        } else if (body.contains("swiggy") || body.contains("Swiggy") || body.contains("zomato") ||
                body.contains("Zomato") || body.contains("McDonald") || body.contains("subway") ||
                body.contains("Subway") || body.contains("Domino") || body.contains("domino") ||
                body.contains("Pizza") || body.contains("pizza")) {
            category = "Food";
        } else if (body.contains("ATM")) {
            category = "ATM";
        } else if (body.contains("Recharge") || body.contains("recharge") || body.contains("Topup") ||
                body.contains("TopUp") ||  body.contains("Top Up") || body.contains("Top up") ||
                body.contains("topup") || body.contains("topUp")) {
            category = "Recharge";
        } else if (body.contains("Bill") || body.contains("bill")) {
            category = "Bill";
        } else if (type.equalsIgnoreCase("Expense") &&
                (body.contains("card") || body.contains("Card"))){
            category = "Debit/Credit Card Expense";
        } else if (body.contains("Credit card") || body.contains("Debit Card") ||
                body.contains("credit card") || body.contains("debit Card")){
            category = "Debit/Credit Card Expense";
        } else if (body.contains("Rent") || body.contains("rent")) {
            category = "Rent";
        } else if (body.contains("UPI") || body.contains("upi")) {
            category = "UPI";
        }  else if (body.contains("loan") || body.contains("Loan") || body.contains("personal") ||
                body.contains("Personal") || body.contains("Home") || body.contains("home") ||
                body.contains("car") || body.contains("Car") || body.contains("bike") ||
                body.contains("Bike") || body.contains("vehicle") || body.contains("Vehicle")) {
            category = "Loan";
        } else if (body.contains("life") || body.contains("Life") || body.contains("General") ||
                body.contains("general") || body.contains("two wheeler") || body.contains("Two Wheeler") ||
                body.contains("Premium") || body.contains("premium")) {
            category = "Premium";
        } else {
            category = "Others";
        }
        /*-----------------------------------Transaction Amount-----------------------------------*/
        String amount;
        if (body.contains("INR")){
            amount = formatter(body,"INR");
            /*String[] separated = body.split("INR");
            if (separated[1].startsWith(" ")){
                body = separated[1];
                String[] _separated = body.split(" ");
                body = _separated[1].trim();
            }
            else
                body = separated[1].trim().substring(0,separated[1].indexOf(" "));*/

        } else if (body.contains("INR.")){
            amount = formatter(body,"INR.");
            
        } else if (body.contains("₹")){
            amount = formatter(body,"₹");

        } else if (body.contains("Rs.")){
            amount = formatter(body,"Rs.");
            /*String[] separated = body.split("Rs.");
            if (separated[1].startsWith(" ")){
                body = separated[1];
                String[] _separated = body.split(" ");
                body = _separated[1].trim();
            }
            else
                body = separated[1].trim().substring(0,separated[1].indexOf(" "));*/

        } else if (body.contains("Rs")){
            amount = formatter(body,"Rs");
            /*String[] separated = body.split("Rs");
            if (separated[1].startsWith(" ")){
                body = separated[1];
                String[] _separated = body.split(" ");
                body = _separated[1].trim();
            }
            else
                body = separated[1].trim().substring(0,separated[1].indexOf(" "));*/
        } else
            amount = "0";

        System.out.println("From : "+address);
        System.out.println("Body : "+amount);
        System.out.println("Date : "+_date);

        if ("Earning".equalsIgnoreCase(type))
            type = "earning";
        else if ("Expense".equalsIgnoreCase(type))
            type = "spent";

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            if (!"0".equalsIgnoreCase(amount) && type.length() > 1){
                if ("Reminder".equalsIgnoreCase(type)){
                    if (body.contains("due on")){
                        String due_date = formatter(body,"due on");
                        submitReminder(_date,due_date,category,amount,address,body);
                    }
                } else {
                    submitDetails(_date,type,category,amount,address, body);
                }
            } else {
                System.out.println();
            }
        }
        sms.add("\nFrom : "+address+"\n\nRs. : "+body +" "+type+"\n\nDate : "+_date+"\n");
    }

    private String formatter(String body, String reg){
        String[] separated = body.split(reg);
        if (separated[1].startsWith(" ")){
            body = separated[1];
            String[] _separated = body.split(" ");
            body = _separated[1].trim();
        }
        else
            body = separated[1].trim().substring(0,separated[1].indexOf(" "));
        if (body.contains(","))
            body = body.replace(",","");

        return body;
    }
    /*.......................................Submit Details.......................................*/
    private void submitDetails(final String date, final String type, final String category,
                               final String amount, final String address, final  String message){
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Transaction Info>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    System.out.println("Transaction Info Added Successfully");
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
                params.put("entdate", changeDateFormatDB(date));
                params.put("expcategory", category);
                params.put("amount", amount);
                params.put("type", type);
                params.put("description", message);
                params.put("remarks", address);
                params.put("date_time", date);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.......................................Submit Details.......................................*/
    private void submitReminder(final String date, final String due_date, final String category,
                                final String amount, final String address, final  String message){
        StringRequest request = new StringRequest(Request.Method.POST, api_add_reminder, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Reminder>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    System.out.println("Reminder Added Successfully");
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
                params.put("mess_res_date", changeDateFormatDB(date));
                params.put("amount", amount);
                params.put("due_date",due_date);
                params.put("description", address +"\t\t"+category);
                params.put("full_mess", message);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*..........................................Date Time.........................................*/
    private void submitDateTime(){
        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_msg_date_time_add, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("DateTime>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    System.out.println("Date Time Added Successfully");
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
                params.put("date_time", _date_time);
                Log.i("Transaction Info>>>", "params::::: " + params);
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
            showHome();
        } else {
            finishAffinity();
        }
    }

    public void backPressed(){
        Intent intent = new Intent (ExpenseManagerActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ExpenseManagerActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
