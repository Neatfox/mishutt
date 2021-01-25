package com.neatfox.mishutt.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.model.Investment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_investment_apply;
import static com.neatfox.mishutt.Constants.api_investment_wishlist;

public class CheckInvestmentAdapter extends RecyclerView.Adapter<CheckInvestmentAdapter.ViewHolder> {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    ArrayList<Investment> investment_list;
    String mAmount;
    Context context;
    Activity activity;

    public CheckInvestmentAdapter(ArrayList<Investment> investment_list, String mAmount, Context context, Activity activity) {
        this.investment_list = investment_list;
        this.mAmount = mAmount;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_check_investment, parent, false);
        return new ViewHolder(view, context, investment_list,mAmount);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        sharedPreference = context.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        final Investment investment = investment_list.get(position);

        holder.investment_type.setText(investment.getInvestment_type());
        holder.scheme_name.setText(investment.getScheme_name());

        String min_investment_amount = investment.getMin_invest_amount();
        String max_investment_amount = investment.getMax_invest_amount();
        String _investment_amount;
        if ("null".equalsIgnoreCase(max_investment_amount) || "".equalsIgnoreCase(max_investment_amount)) {
            if ("".equalsIgnoreCase(min_investment_amount) || "null".equalsIgnoreCase(min_investment_amount))
                _investment_amount = " - ";
            else
                _investment_amount = "₹"+(min_investment_amount);
        }
        else
            _investment_amount = ("₹"+(min_investment_amount) +" - "+"₹"+(max_investment_amount));

        holder.investment_amount.setText(_investment_amount);

        String _aum = investment.getAum();

        if ("null".equalsIgnoreCase(_aum) || "".equalsIgnoreCase(_aum)){
            holder.aum.setText(" - ");
        } else {
            holder.aum.setText(String.format("₹%s",(_aum)));
        }

        String _exit_load = investment.getExit_load();

        if ("null".equalsIgnoreCase(_exit_load) || "".equalsIgnoreCase(_exit_load)){
            holder.exit_load.setText(" - ");
        } else {
            holder.exit_load.setText(_exit_load);
        }

        holder.wish_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) noNetwork();
                else addWishList(investment.getId());
            }
        });

        holder.apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) noNetwork();
                else apply(investment.getId(),investment.getInvestment_type());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(mAmount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return investment_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Investment> investment_list;
        String mAmount;
        TextView investment_type,scheme_name,investment_amount,aum,exit_load;
        Button wish_list,apply;

        ViewHolder(View itemView, Context context, ArrayList<Investment> investment_list,String mAmount) {
            super(itemView);
            this.context = context;
            this.investment_list = investment_list;
            this.mAmount = mAmount;
            investment_type = itemView.findViewById(R.id.tv_investment_type);
            scheme_name = itemView.findViewById(R.id.tv_scheme_name);
            investment_amount = itemView.findViewById(R.id.tv_investment_amount);
            aum = itemView.findViewById(R.id.tv_aum);
            exit_load = itemView.findViewById(R.id.tv_exit_load);
            apply = itemView.findViewById(R.id.button_apply);
            wish_list = itemView.findViewById(R.id.button_wish_list);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = Objects.requireNonNull(connectivityManager)
                .getActiveNetworkInfo();
        return activeNetworkInfo == null;
    }

    private void noNetwork() {
        Toast.makeText(context,"No internet connection",Toast.LENGTH_SHORT).show();
    }

    private void snackBarError() {
        Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
    }

    private void addWishList(final String id){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_investment_wishlist, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("WishList>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    System.out.println("Added to Wishlist");
                    Toast.makeText(context,"Added to Wishlist",Toast.LENGTH_SHORT).show();
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
                params.put("id", id);
                params.put("invest", mAmount);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }

    private void apply(final String id, final String type){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_investment_apply, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Apply>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    System.out.println("Applied");
                    String _toast = "Thank you for applying to " +type+
                            ".\nOur team members will call you within 24 hrs to take the process further.";
                    Toast.makeText(context,_toast,Toast.LENGTH_LONG).show();
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
                params.put("id", id);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
