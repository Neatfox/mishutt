package com.neatfox.mishutt.ui.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.neatfox.mishutt.ui.activity.AddBeneficiaryInfoActivity;
import com.neatfox.mishutt.ui.activity.BeneficiaryListActivity;
import com.neatfox.mishutt.ui.activity.SendMoneyActivity;
import com.neatfox.mishutt.ui.model.Beneficiary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_beneficiary_remove_send_otp;
import static com.neatfox.mishutt.Constants.api_beneficiary_remove_verify_otp;

public class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder> {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    ArrayList<Beneficiary> beneficiary_list;
    Context context;
    Activity activity;
    ImageView action_cancel;
    TextView mobile_number;
    EditText one,two,three,four,five,six;
    Button resend,submit;
    Dialog dialog_otp;
    ProgressDialog progressDialog;
    static final long START_TIME_IN_MILLIS = 120000;
    long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    String verification_code = "";

    public BeneficiaryAdapter(ArrayList<Beneficiary> beneficiary_list, Context context, Activity activity) {
        this.beneficiary_list = beneficiary_list;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_beneficiary, parent, false);
        return new ViewHolder(view, context, beneficiary_list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        sharedPreference = context.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        final Beneficiary beneficiary = beneficiary_list.get(position);

        /*.......................................Dialog OTP.......................................*/
        dialog_otp = new Dialog(context);
        dialog_otp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_otp.setContentView(R.layout.dialog_otp);
        dialog_otp.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog_otp.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog_otp.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog_otp.getWindow().setAttributes(windowParams);

        action_cancel = dialog_otp.findViewById(R.id.iv_cancel_action);
        mobile_number = dialog_otp.findViewById(R.id.tv_mobile_number);
        one = dialog_otp.findViewById(R.id.edit_text_one);
        two = dialog_otp.findViewById(R.id.edit_text_two);
        three = dialog_otp.findViewById(R.id.edit_text_three);
        four = dialog_otp.findViewById(R.id.edit_text_four);
        five = dialog_otp.findViewById(R.id.edit_text_five);
        six = dialog_otp.findViewById(R.id.edit_text_six);
        resend = dialog_otp.findViewById(R.id.button_resend);
        submit = dialog_otp.findViewById(R.id.button_submit);

        one.addTextChangedListener(new BeneficiaryAdapter.GenericTextWatcher(one));
        two.addTextChangedListener(new BeneficiaryAdapter.GenericTextWatcher(two));
        three.addTextChangedListener(new BeneficiaryAdapter.GenericTextWatcher(three));
        four.addTextChangedListener(new BeneficiaryAdapter.GenericTextWatcher(four));
        five.addTextChangedListener(new BeneficiaryAdapter.GenericTextWatcher(five));
        six.addTextChangedListener(new BeneficiaryAdapter.GenericTextWatcher(six));

        String sourceStringMobileNumber = "<b>"+"+91 " + beneficiary.getMobile_number() + "</b>";
        mobile_number.setText(Html.fromHtml(sourceStringMobileNumber));

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    noNetwork();
                } else {
                    startTimer();
                    removeSendOTP(beneficiary.getBeneficiary_id(),beneficiary.getRemitterId(),beneficiary.getRemitter_id());
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verification_code.length()<1)
                    Toast.makeText(context,R.string.enter_verification_code,Toast.LENGTH_SHORT).show();
                else if (verification_code.length()<6)
                    Toast.makeText(context,R.string.verification_code_length_six,Toast.LENGTH_SHORT).show();
                else {
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        removeVerifyOTP(beneficiary.getBeneficiary_id(),beneficiary.getRemitterId(),beneficiary.getRemitter_id(),verification_code);

                    }
                }
            }
        });

        action_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_otp.dismiss();
            }
        });

        holder.name.setText(String.format("%s %s", beneficiary.getFirst_name(), beneficiary.getLast_name()));
        holder.mobile_number.setText(beneficiary.getMobile_number());
        holder.pin_code.setText(beneficiary.getPin_code());

        if (beneficiary.getHas_account_info().equalsIgnoreCase("Y")){
            holder.account_number.setText(beneficiary.getAccount_number());
            holder.ifsc_code.setText(beneficiary.getIfsc_code());
            holder.register.setVisibility(View.GONE);
            holder.send_money.setVisibility(View.VISIBLE);
            holder.edit.setVisibility(View.VISIBLE);
        } else {
            String _string = "Not Added Yet";
            holder.account_number.setText(_string);
            holder.ifsc_code.setText(_string);
            holder.register.setVisibility(View.VISIBLE);
            holder.send_money.setVisibility(View.GONE);
            holder.edit.setVisibility(View.INVISIBLE);
        }

        holder.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, AddBeneficiaryInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("name",beneficiary.getFirst_name());
                intent.putExtra("mobile_number",beneficiary.getMobile_number());
                intent.putExtra("remitterId",beneficiary.getRemitterId());
                intent.putExtra("remitter_id",beneficiary.getRemitter_id());
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        holder.send_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, SendMoneyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("name",beneficiary.getFirst_name());
                intent.putExtra("mobile_number",beneficiary.getMobile_number());
                intent.putExtra("beneficiary_id",beneficiary.getBeneficiary_id());
                intent.putExtra("remitter_id",beneficiary.getRemitter_id());
                intent.putExtra("account_number",beneficiary.getAccount_number());
                intent.putExtra("ifsc_code",beneficiary.getIfsc_code());
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (isNetworkAvailable()){
                    noNetwork();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you want to delete this beneficiary?")
                            .setCancelable(false)
                            .setIcon(R.drawable.mishutt)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    removeSendOTP(beneficiary.getBeneficiary_id(),beneficiary.getRemitterId(),beneficiary.getRemitter_id());
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
        return beneficiary_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Beneficiary> beneficiary_list;
        TextView name,mobile_number,pin_code,account_number,ifsc_code;
        Button register,send_money;
        ImageView edit;

        ViewHolder(View itemView, Context context, ArrayList<Beneficiary> beneficiary_list) {
            super(itemView);
            this.context = context;
            this.beneficiary_list = beneficiary_list;
            name = itemView.findViewById(R.id.tv_name);
            mobile_number = itemView.findViewById(R.id.tv_mobile_number);
            pin_code = itemView.findViewById(R.id.tv_pin_code);
            account_number = itemView.findViewById(R.id.tv_account_number);
            ifsc_code = itemView.findViewById(R.id.tv_ifsc_code);
            register = itemView.findViewById(R.id.button_register);
            send_money = itemView.findViewById(R.id.button_send_money);
            edit = itemView.findViewById(R.id.iv_edit);
        }
    }

    /*......................................Verification Code.....................................*/
    private class GenericTextWatcher implements TextWatcher {
        private final View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            int id = view.getId();

            if (id == R.id.edit_text_one) {
                if (text.length() == 1) two.requestFocus();
            } else if (id == R.id.edit_text_two) {
                if (text.length() == 1) three.requestFocus();
                else if (text.length() == 0) one.requestFocus();
            } else if (id == R.id.edit_text_three) {
                if (text.length() == 1) four.requestFocus();
                else if (text.length() == 0) two.requestFocus();
            } else if (id == R.id.edit_text_four) {
                if (text.length() == 1) five.requestFocus();
                else if (text.length() == 0) three.requestFocus();
            } else if (id == R.id.edit_text_five) {
                if (text.length() == 1) six.requestFocus();
                else if (text.length() == 0) four.requestFocus();
            } else if (id == R.id.edit_text_six) {
                if (text.length() == 0) five.requestFocus();
            }

            String stringOne = one.getText().toString().trim();
            String stringTwo = two.getText().toString().trim();
            String stringThree = three.getText().toString().trim();
            String stringFour = four.getText().toString().trim();
            String stringFive = five.getText().toString().trim();
            String stringSix = six.getText().toString().trim();
            verification_code = stringOne + stringTwo + stringThree + stringFour + stringFive + stringSix;
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
    }

    /*...........................................Timer............................................*/
    public void startTimer(){
        resend.setEnabled(false);
        new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
                int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
                String timeLeftFormat = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                String sResend = "Resend";
                resend.setText(String.format("%s (%s)", sResend, timeLeftFormat));
            }

            @Override
            public void onFinish() {
                resend.setText(R.string.resend);
                resend.setEnabled(true);
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
            }
        }.start();
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

    private void removeSendOTP(final String beneficiary_id,final String remitterId,final String remitter_id){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_remove_send_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Send OTP>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                String msg = "";
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                    msg = resObj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    dialog_otp.show();
                    startTimer();
                } else {
                    Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
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
                params.put("beneficiaryid", beneficiary_id);
                params.put("remitterid", remitterId);
                params.put("remitter_id", remitter_id);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }

    private void removeVerifyOTP(final String beneficiary_id,final String remitterId,final String remitter_id,final String otp){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_beneficiary_remove_verify_otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Verify OTP>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                String msg = "";
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                    msg = resObj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    Intent intent = new Intent (context, BeneficiaryListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
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
                params.put("beneficiaryid", beneficiary_id);
                params.put("remitterid", remitterId);
                params.put("remitter_id", remitter_id);
                params.put("otp", otp);
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
