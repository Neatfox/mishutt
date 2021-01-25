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
import com.neatfox.mishutt.ui.activity.DashboardActivity;
import com.neatfox.mishutt.ui.model.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.addCommaString;
import static com.neatfox.mishutt.Constants.api_product_delete;
import static com.neatfox.mishutt.Constants.changeDateFormatUI;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    ArrayList<Product> product_list;
    Context context;
    ProgressDialog progressDialog;

    public ProductAdapter(ArrayList<Product> product_list, Context context) {
        this.product_list = product_list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product, parent, false);
        return new ViewHolder(view, context, product_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Product product = product_list.get(position);

        holder.product.setText(product.getProduct_subcategory());
        holder.product_value.setText(String.format("₹%s", addCommaString(product.getProduct_value())));
        holder.investment_amount.setText(String.format("₹%s", addCommaString(product.getInvestment_amount())));
        holder.premium_amount.setText(String.format("₹%s", addCommaString(product.getPremium_amount())));
        holder.due_date.setText(changeDateFormatUI(product.getDue_date()));

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (isNetworkAvailable()){
                    noNetwork();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you want to delete this product?")
                            .setCancelable(false)
                            .setIcon(R.drawable.mishutt)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    delete(product.getProduct_id());
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
        return product_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Product> product_list;
        TextView product,product_value,investment_amount,premium_amount,due_date;
        ImageView delete;

        ViewHolder(View itemView, Context context, ArrayList<Product> product_list) {
            super(itemView);
            this.context = context;
            this.product_list = product_list;
            product = itemView.findViewById(R.id.tv_product);
            product_value = itemView.findViewById(R.id.tv_product_value);
            investment_amount = itemView.findViewById(R.id.tv_investment_amount);
            premium_amount = itemView.findViewById(R.id.tv_premium_amount);
            due_date = itemView.findViewById(R.id.tv_next_due_date);
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

        StringRequest request = new StringRequest(Request.Method.POST, api_product_delete, new Response.Listener<String>() {
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
                    Intent intent = new Intent (context, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("type","Product");
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
