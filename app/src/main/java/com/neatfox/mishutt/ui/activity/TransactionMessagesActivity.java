package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.neatfox.mishutt.Constants.REQUEST_SMS_PERMISSION;
import static com.neatfox.mishutt.Constants.api_transaction_add;
import static com.neatfox.mishutt.Constants.api_transaction_msg_date_time_add;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;

public class TransactionMessagesActivity extends MainActivity{

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ListView smsList;
    ArrayList <String> sms = new ArrayList<>();
    String _date_time = "";
    ProgressDialog progressDialog;
    int backPress=0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_transaction_messages, contentFrameLayout);
        TransactionMessagesActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        smsList = findViewById(R.id.listViewSMS);

        if (ContextCompat.checkSelfPermission(TransactionMessagesActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions( TransactionMessagesActivity.this,
                    new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            messages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SMS_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                messages();
            }
        }
    }

    public void messages(){
        fetchInbox();
        if(sms!=null) {
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, sms);
            smsList.setAdapter(adapter);
        }
    }

    public void fetchInbox() {
        progressDialog = new ProgressDialog(TransactionMessagesActivity.this);
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
                    if (body.contains("Rs")) {
                        if(body.contains("credited")|| body.contains("debited") || body.contains("withdrawn") ||
                                body.contains("payment") || body.contains("Transaction ID") || body.contains("Txn ID") ||
                                    body.contains("recharge") || body.contains("added") || body.contains("paid") ||
                                        body.contains("received")) {

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
        String type = "";
        if (body.contains("credited") || body.contains("added")  || body.contains("received")) {
            type = "Earning";
        } else if (body.contains("debited") || body.contains("withdrawn") || body.contains("payment")
                    || body.contains("recharge") || body.contains("paid")){
            type = "Expense";
        }
        /*-----------------------------------Transaction Amount-----------------------------------*/
        if (body.contains("Rs.")){
            String[] separated = body.split("Rs.");
            if (separated[1].startsWith(" ")){
                body = separated[1];
                String[] _separated = body.split(" ");
                body = _separated[1].trim();
            }
            else
                body = separated[1].trim().substring(0,separated[1].indexOf(" "));

        } else if (body.contains("Rs")){
            String[] separated = body.split("Rs");
            if (separated[1].startsWith(" ")){
                body = separated[1];
                String[] _separated = body.split(" ");
                body = _separated[1].trim();
            }
            else
                body = separated[1].trim().substring(0,separated[1].indexOf(" "));
        }

        System.out.println("From : "+address);
        System.out.println("Body : "+body);
        System.out.println("Date : "+_date);

        if ("Earning".equalsIgnoreCase(type))
            type = "earning";
        else if ("Expense".equalsIgnoreCase(type))
            type = "spent";

        if (isNetworkAvailable()) noNetwork();
        else submitDetails(_date,type,body,address);

        sms.add("\nFrom : "+address+"\n\nRs. : "+body +" "+type+"\n\nDate : "+_date+"\n");
    }

    /*.......................................Submit Details.......................................*/
    private void submitDetails(final String date, final String type, final String amount, final String address){
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
                    //Toast.makeText(TransactionMessagesActivity.this, "Transaction Info Added Successfully", Toast.LENGTH_SHORT).show();
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
                params.put("expcategory", "Others");
                params.put("amount", amount);
                params.put("type", type);
                params.put("description", address);
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
                    //Toast.makeText(TransactionMessagesActivity.this, "Transaction Info Added Successfully", Toast.LENGTH_SHORT).show();
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
    /*......................................For BackPress.........................................*/
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
        Intent intent = new Intent (TransactionMessagesActivity.this, HomeActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TransactionMessagesActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
