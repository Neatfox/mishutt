package com.neatfox.mishutt.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.neatfox.mishutt.ui.activity.AddTransactionActivity;
import com.neatfox.mishutt.ui.activity.ExpenseManagerActivity;
import com.neatfox.mishutt.ui.model.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.addCommaString;
import static com.neatfox.mishutt.Constants.api_transaction_delete;
import static com.neatfox.mishutt.Constants.changeDateFormatUI;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    ArrayList<Transaction> transaction_list;
    Context context;
    ProgressDialog progressDialog;

    public TransactionAdapter(ArrayList<Transaction> transaction_list, Context context) {
        this.transaction_list = transaction_list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_transaction, parent, false);
        return new ViewHolder(view, context, transaction_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Transaction transaction = transaction_list.get(position);

        holder.date.setText(changeDateFormatUI(transaction.getDate()));
        holder.category.setText(transaction.getCategory());
        holder.earning.setText(String.format("₹%s", addCommaString(transaction.getEarning())));
        holder.spending.setText(String.format("₹%s", addCommaString(transaction.getSpending())));
        holder.description.setText(transaction.getDescription());
        holder.remarks.setText(transaction.getRemarks());


        if (Double.parseDouble(transaction.getEarning()) == 0.0){
            holder.layout_earning.setVisibility(View.GONE);
            holder.layout_spending.setVisibility(View.VISIBLE);
        } else {
            holder.layout_earning.setVisibility(View.VISIBLE);
            holder.layout_spending.setVisibility(View.GONE);
        }

        if (transaction.getRemarks().trim().length()<1)
            holder.remarks.setVisibility(View.GONE);
        else
            holder.remarks.setVisibility(View.VISIBLE);

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()){
                    noNetwork();
                } else {
                    Intent intent=new Intent(context, AddTransactionActivity.class);
                    intent.putExtra("type","Edit Transaction");
                    intent.putExtra("transaction_list_id", transaction.getTransaction_list_id());
                    intent.putExtra("transaction_date", changeDateFormatUI(transaction.getDate()));
                    intent.putExtra("category", transaction.getCategory());
                    if (Double.parseDouble(transaction.getEarning()) == 0.0){
                        intent.putExtra("amount", transaction.getSpending());
                        intent.putExtra("transaction_type", "Expense");
                    } else {
                        intent.putExtra("amount", transaction.getEarning());
                        intent.putExtra("transaction_type", "Earning");
                    }
                    intent.putExtra("description", transaction.getDescription());
                    intent.putExtra("remarks", transaction.getRemarks());
                    context.startActivity(intent);
                }
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (isNetworkAvailable()){
                    noNetwork();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you want to delete this transaction?")
                            .setCancelable(false)
                            .setIcon(R.drawable.mishutt)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    delete(transaction.getTransaction_list_id());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });
    }

    @Override
    public int getItemCount() {
        return transaction_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Transaction> transaction_list;
        LinearLayout layout_earning,layout_spending,layout_remarks;
        TextView date,category,earning,spending,description,remarks;
        ImageView edit,delete;

        ViewHolder(View itemView, Context context, ArrayList<Transaction> transaction_list) {
            super(itemView);
            this.context = context;
            this.transaction_list = transaction_list;
            layout_earning = itemView.findViewById(R.id.layout_earning);
            layout_spending = itemView.findViewById(R.id.layout_spending);
            layout_remarks = itemView.findViewById(R.id.layout_remarks);
            date = itemView.findViewById(R.id.tv_date);
            category = itemView.findViewById(R.id.tv_category);
            earning = itemView.findViewById(R.id.tv_earning);
            spending = itemView.findViewById(R.id.tv_spending);
            description = itemView.findViewById(R.id.tv_description);
            remarks = itemView.findViewById(R.id.tv_remarks);
            edit = itemView.findViewById(R.id.iv_edit);
            delete = itemView.findViewById(R.id.iv_delete);
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

    private void delete(final String id){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_transaction_delete, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Delete>>>", "onResponse::::: " + response);
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
                    Intent intent = new Intent (context, ExpenseManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("type","Transactions");
                    context.startActivity(intent);
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
                params.put("expenceid", id);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
