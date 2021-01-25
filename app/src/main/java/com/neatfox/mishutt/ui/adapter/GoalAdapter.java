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
import com.neatfox.mishutt.ui.activity.GoalManagerActivity;
import com.neatfox.mishutt.ui.model.Goal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.addCommaString;
import static com.neatfox.mishutt.Constants.api_goal_delete;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    ArrayList<Goal> goal_list;
    Context context;
    ProgressDialog progressDialog;

    public GoalAdapter(ArrayList<Goal> goal_list, Context context) {
        this.goal_list = goal_list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_goal, parent, false);
        return new ViewHolder(view, context, goal_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Goal goal = goal_list.get(position);

        holder.goal_type.setText(goal.getGoal_type());
        holder.goal_amount.setText(String.format("₹%s", addCommaString(goal.getGoal_amount())));
        holder.future_value.setText(String.format("₹%s", addCommaString(goal.getFuture_value())));

        if ("0".equalsIgnoreCase(goal.getYears())
                && "0".equalsIgnoreCase(goal.getMonths()))
            holder.layout_duration.setVisibility(View.GONE);
        else if (!"0".equalsIgnoreCase(goal.getYears())
                && "0".equalsIgnoreCase(goal.getMonths())){
            holder.layout_duration.setVisibility(View.VISIBLE);
            if ("1".equalsIgnoreCase(goal.getYears()))
                holder.duration.setText(String.format("%s Year", goal.getYears()));
            else
                holder.duration.setText(String.format("%s Years", goal.getYears()));
        } else if ("0".equalsIgnoreCase(goal.getYears())
                && !"0".equalsIgnoreCase(goal.getMonths())){
            holder.layout_duration.setVisibility(View.VISIBLE);
            if ("1".equalsIgnoreCase(goal.getMonths()))
                holder.duration.setText(String.format("%s Month", goal.getMonths()));
            else
                holder.duration.setText(String.format("%s Months", goal.getMonths()));
        } else {
            holder.layout_duration.setVisibility(View.VISIBLE);
            if ("1".equalsIgnoreCase(goal.getYears()) && "1".equalsIgnoreCase(goal.getMonths()))
                holder.duration.setText(String.format("%s Year %s Month", goal.getYears(), goal.getMonths()));
            else if (!"1".equalsIgnoreCase(goal.getYears()) && "1".equalsIgnoreCase(goal.getMonths()))
                holder.duration.setText(String.format("%s Years %s Month", goal.getYears(), goal.getMonths()));
            else if ("1".equalsIgnoreCase(goal.getYears()) && !"1".equalsIgnoreCase(goal.getMonths()))
                holder.duration.setText(String.format("%s Year %s Months", goal.getYears(), goal.getMonths()));
            else
                holder.duration.setText(String.format("%s Years %s Months", goal.getYears(), goal.getMonths()));
        }

        holder.down_payment.setText(String.format("₹%s", addCommaString(goal.getDown_payment())));
        holder.emi.setText(String.format("₹%s", addCommaString(goal.getEmi())));

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (isNetworkAvailable()){
                    noNetwork();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you want to delete this goal?")
                            .setCancelable(false)
                            .setIcon(R.drawable.mishutt)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    delete(goal.getGoal_id());
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
        return goal_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Goal> goal_list;
        LinearLayout layout_duration;
        TextView goal_type,goal_amount,future_value,duration,down_payment,emi;
        ImageView delete;

        ViewHolder(View itemView, Context context, ArrayList<Goal> goal_list) {
            super(itemView);
            this.context = context;
            this.goal_list = goal_list;
            layout_duration = itemView.findViewById(R.id.layout_duration);
            goal_type = itemView.findViewById(R.id.tv_goal_type);
            goal_amount = itemView.findViewById(R.id.tv_goal_amount);
            future_value = itemView.findViewById(R.id.tv_future_value);
            duration = itemView.findViewById(R.id.tv_duration);
            down_payment = itemView.findViewById(R.id.tv_down_payment);
            emi = itemView.findViewById(R.id.tv_emi);
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

        StringRequest request = new StringRequest(Request.Method.POST, api_goal_delete, new Response.Listener<String>() {
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
                    Intent intent = new Intent (context, GoalManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //intent.putExtra("type","Goal");
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
