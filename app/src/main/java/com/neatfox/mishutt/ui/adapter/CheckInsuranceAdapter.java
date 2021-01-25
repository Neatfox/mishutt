package com.neatfox.mishutt.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.model.Insurance;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_insurance_apply;
import static com.neatfox.mishutt.Constants.api_insurance_wishlist;
import static com.neatfox.mishutt.Constants.basePath;
import static com.neatfox.mishutt.Constants.imageMiddlePath;

public class CheckInsuranceAdapter extends RecyclerView.Adapter<CheckInsuranceAdapter.ViewHolder> {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    ArrayList<Insurance> insurance_list;
    String mAmount;
    Context context;
    Activity activity;

    public CheckInsuranceAdapter(ArrayList<Insurance> insurance_list, String mAmount, Context context, Activity activity) {
        this.insurance_list = insurance_list;
        this.mAmount = mAmount;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_check_insurance, parent, false);
        return new ViewHolder(view, context, insurance_list,mAmount);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        sharedPreference = context.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        final Insurance insurance = insurance_list.get(position);

        String image_path = basePath + imageMiddlePath + insurance.getInsurance_logo();
        Glide.with(context).load(image_path).apply(new RequestOptions().override(720, 720)).into(holder.imageView);

        holder.insurance_name.setText(insurance.getInsurance_name());
        holder.life_cover.setText(insurance.getLife_cover());
        holder.cover_up_to.setText(insurance.getCover_up_to());

        if (Build.VERSION.SDK_INT >= 24)
            holder.add_on.setText(Html.fromHtml(insurance.getAdd_on(), Html.FROM_HTML_MODE_LEGACY));
        else
            holder.add_on.setText(Html.fromHtml(insurance.getAdd_on()));

        holder.wish_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) noNetwork();
                else addWishList(insurance.getId());
            }
        });

        holder.apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) noNetwork();
                else apply(insurance.getId(),insurance.getInsurance_type());
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
        return insurance_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Insurance> insurance_list;
        String mAmount;
        ImageView imageView;
        TextView insurance_name,life_cover,cover_up_to,add_on;
        Button wish_list,apply;

        ViewHolder(View itemView, Context context, ArrayList<Insurance> insurance_list, String mAmount) {
            super(itemView);
            this.context = context;
            this.insurance_list = insurance_list;
            this.mAmount = mAmount;
            imageView = itemView.findViewById(R.id.iv_image);
            insurance_name = itemView.findViewById(R.id.tv_insurance_name);
            life_cover = itemView.findViewById(R.id.tv_life_cover);
            cover_up_to = itemView.findViewById(R.id.tv_cover_up_to);
            add_on = itemView.findViewById(R.id.tv_add_on);
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

        StringRequest request = new StringRequest(Request.Method.POST, api_insurance_wishlist, new Response.Listener<String>() {
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
                params.put("want", mAmount);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }

    private void apply(final String id,final String type){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_insurance_apply, new Response.Listener<String>() {
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
