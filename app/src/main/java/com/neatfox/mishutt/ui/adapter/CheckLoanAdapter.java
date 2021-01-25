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
import com.neatfox.mishutt.ui.model.Loan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_loan_apply;
import static com.neatfox.mishutt.Constants.api_loan_wishlist;

public class CheckLoanAdapter extends RecyclerView.Adapter<CheckLoanAdapter.ViewHolder> {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    ArrayList<Loan> loan_list;
    String _mAmount;
    Context context;
    Activity activity;

    public CheckLoanAdapter(ArrayList<Loan> loan_list, String _mAmount, Context context, Activity activity) {
        this.loan_list = loan_list;
        this._mAmount = _mAmount;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_check_loan, parent, false);
        return new ViewHolder(view, context, loan_list,_mAmount);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        sharedPreference = context.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        final Loan loan = loan_list.get(position);

        String min_interest_rate = loan.getMin_interest_rate();
        String max_interest_rate = loan.getMax_interest_rate();
        String _interest_rate;
        if ("null".equalsIgnoreCase(max_interest_rate) || "".equalsIgnoreCase(max_interest_rate))
            _interest_rate = min_interest_rate;
        else
            _interest_rate = (min_interest_rate +" - "+max_interest_rate);

        String min_loan_amount = loan.getMin_loan_amount();
        String max_loan_amount = loan.getMax_loan_amount();
        final String _loan_amount;
        if ("null".equalsIgnoreCase(max_loan_amount) || "".equalsIgnoreCase(max_loan_amount))
            _loan_amount = min_loan_amount;
        else
            _loan_amount = (min_loan_amount +" - "+max_loan_amount);

        String min_tenure = loan.getMin_tenure();
        String max_tenure = loan.getMax_tenure();
        String _tenure;
        if ("null".equalsIgnoreCase(max_tenure) || "".equalsIgnoreCase(max_tenure))
            _tenure = min_tenure;
        else
            _tenure = (min_tenure +" - "+max_tenure);

        holder.bank_name.setText(loan.getBank_name());
        holder.interest_rate.setText(_interest_rate);
        holder.processing_fee.setText(loan.getProcessing_fee());
        holder.loan_amount.setText(_loan_amount);
        holder.tenure.setText(_tenure);

        holder.wish_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) noNetwork();
                else addWishList(loan.getId());
            }
        });

        holder.apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) noNetwork();
                else apply(loan.getId(),loan.getLoan_type());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(_mAmount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return loan_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Loan> loan_list;
        String _mAmount;
        TextView bank_name,interest_rate,future_value,processing_fee,loan_amount,tenure;
        Button wish_list,apply;

        ViewHolder(View itemView, Context context, ArrayList<Loan> loan_list,String _mAmount) {
            super(itemView);
            this.context = context;
            this.loan_list = loan_list;
            this._mAmount = _mAmount;
            bank_name = itemView.findViewById(R.id.tv_bank_name);
            interest_rate = itemView.findViewById(R.id.tv_interest_rate);
            future_value = itemView.findViewById(R.id.tv_future_value);
            processing_fee = itemView.findViewById(R.id.tv_processing_fee);
            loan_amount = itemView.findViewById(R.id.tv_loan_amount);
            tenure = itemView.findViewById(R.id.tv_tenure);
            wish_list = itemView.findViewById(R.id.button_wish_list);
            apply = itemView.findViewById(R.id.button_apply);
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

        StringRequest request = new StringRequest(Request.Method.POST, api_loan_wishlist, new Response.Listener<String>() {
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
                params.put("loan", _mAmount);
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

        StringRequest request = new StringRequest(Request.Method.POST, api_loan_apply, new Response.Listener<String>() {
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
