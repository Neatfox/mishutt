package com.neatfox.mishutt.ui.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.neatfox.mishutt.Constants.api_aadhaar_verification;
import static com.neatfox.mishutt.Constants.api_pan_verification;
import static com.neatfox.mishutt.Constants.api_profile_details;
import static com.neatfox.mishutt.Constants.api_profile_edit;
import static com.neatfox.mishutt.Constants.changeDateFormatDB;
import static com.neatfox.mishutt.Constants.changeDateFormatUI;

public class AddProfileActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    ProgressDialog progressDialog;
    ConstraintLayout layout;
    LinearLayout layout_back,layout_profile_info,layout_update;
    ImageButton ib_back;
    TextInputLayout layout_aadhaar_number,layout_pan_no;
    EditText name,address,mobile_number,email_id,date_of_birth,spouse_name,spouse_date_of_birth,
            wedding_anniversary,number_of_children,profession,annual_income,number_of_dependents,
            aadhaar_number,pan_no;
    Button update_profile;
    boolean valid_aadhar_number = false,valid_pan_no = false;
    String emailPattern,date_picker_type;
    int backPress = 0;

    int[] colorGreenArray = {R.color.green};
    int[] colorRedArray = {R.color.red};

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        AddProfileActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        calendar = Calendar.getInstance();

        layout = findViewById(R.id.layout);
        layout_back = findViewById(R.id.layout_back);
        ib_back = findViewById(R.id.ib_back);
        layout_profile_info = findViewById(R.id.layout_profile_info);
        layout_update = findViewById(R.id.layout_update);
        layout_aadhaar_number = findViewById(R.id.layout_aadhaar_number);
        layout_pan_no = findViewById(R.id.layout_pan_no);
        name = findViewById(R.id.et_name);
        address = findViewById(R.id.et_address);
        mobile_number = findViewById(R.id.et_mobile_number);
        email_id = findViewById(R.id.et_email_id);
        date_of_birth = findViewById(R.id.et_date_of_birth);
        spouse_name = findViewById(R.id.et_spouse_name);
        spouse_date_of_birth = findViewById(R.id.et_spouse_date_of_birth);
        wedding_anniversary = findViewById(R.id.et_wedding_anniversary);
        number_of_children = findViewById(R.id.et_number_of_children);
        profession = findViewById(R.id.et_profession);
        annual_income = findViewById(R.id.et_annual_income);
        number_of_dependents = findViewById(R.id.et_number_of_dependents);
        aadhaar_number = findViewById(R.id.et_aadhaar_number);
        pan_no = findViewById(R.id.et_pan_no);
        update_profile = findViewById(R.id.button_update_profile);
        emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (isNetworkAvailable()) {
            noNetwork();
        } else {
            setProfileDetails();
        }
        /*......................................Date Picker.......................................*/
        date_of_birth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    date_of_birth.setText("");
                    datePicker("Date_of_Birth");
                }
            }
        });

        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date_of_birth.getText().toString().trim().length()<10)
                    datePicker("Date_of_Birth");
                else
                    spouse_name.requestFocus();
            }
        });

        spouse_date_of_birth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    spouse_date_of_birth.setText("");
                    datePicker("Spouse_Date_of_Birth");
                }
            }
        });

        spouse_date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spouse_date_of_birth.getText().toString().trim().length()<10)
                    datePicker("Spouse_Date_of_Birth");
                else
                    wedding_anniversary.requestFocus();
            }
        });

        wedding_anniversary.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    wedding_anniversary.setText("");
                    datePicker("Wedding_Anniversary");
                }
            }
        });

        wedding_anniversary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wedding_anniversary.getText().toString().trim().length()<10)
                    datePicker("Wedding_Anniversary");
                else
                    number_of_children.requestFocus();
            }
        });

        aadhaar_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                valid_aadhar_number = false;
                if(s.length()==12){
                    if (isNetworkAvailable()) noNetwork();
                    else {
                        verifyAadhaar();
                        layout_aadhaar_number.setHelperText("Verifying Aadhaar Number...");
                        layout_aadhaar_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorGreenArray[0]));
                    }
                } else {
                    layout_aadhaar_number.setHelperTextEnabled(false);
                }
            }
        });

        pan_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                valid_pan_no = false;
                if(s.length()==10){
                    if (isNetworkAvailable()) noNetwork();
                    else {
                        verifyPAN();
                        layout_pan_no.setHelperText("Verifying PAN No...");
                        layout_pan_no.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorGreenArray[0]));
                    }
                } else
                    layout_pan_no.setHelperTextEnabled(false);
            }
        });

        /*..................................Update Profile Info.................................*/
        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkName(view) && checkMobileNumber(view) && checkEmailId(view)  &&
                        checkDateOfBirth(view) && checkSpouseDateOfBirth(view) &&
                        checkWeddingAnniversary(view) && checkAadhaarNumber(view) && checkPANNo(view)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        updateProfileDetails();
                    }
                }
            }
        });
        /*..................................Back To Previous Page.................................*/
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backPressed(); }
        });

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backPressed(); }
        });
    }
    /*.........................................Date Picker........................................*/
    public void datePicker(String type){
        date_picker_type = type;
        DatePickerDialog datePickerDialog = new DatePickerDialog (AddProfileActivity.this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    public DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
            String date = (setDate(selectedYear, selectedMonth + 1, selectedDay));
            if ("Date_of_Birth".equalsIgnoreCase(date_picker_type))
                date_of_birth.setText(date);
            else if ("Spouse_Date_of_Birth".equalsIgnoreCase(date_picker_type))
                spouse_date_of_birth.setText(date);
            else if ("Wedding_Anniversary".equalsIgnoreCase(date_picker_type))
                wedding_anniversary.setText(date);
        }
    };

    public String setDate(int year, int month, int day){
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
    /*.........................................Check Name.........................................*/
    public boolean checkName (View view) {
        if (name.getText().toString().trim().length()<1){
            name.requestFocus();
            Snackbar.make(view, R.string.enter_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Mobile Number.....................................*/
    public boolean checkMobileNumber (View view) {
        if (mobile_number.getText().toString().trim().length()<1){
            mobile_number.requestFocus();
            Snackbar.make(view, R.string.enter_mobile_number, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (mobile_number.getText().toString().trim().length()<10){
            mobile_number.requestFocus();
            Snackbar.make(view, R.string.mobile_number_length, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check Email ID.......................................*/
    public boolean checkEmailId (View view) {
        if (email_id.getText().toString().trim().length()<1){
            email_id.requestFocus();
            Snackbar.make(view, R.string.enter_email_id, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (!email_id.getText().toString().trim().matches(emailPattern)){
            email_id.requestFocus();
            Snackbar.make(view, R.string.enter_valid_email_id, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Date of Birth....................................*/
    public boolean checkDateOfBirth (View view) {
        if (date_of_birth.getText().toString().trim().length()>0 &&
                date_of_birth.getText().toString().trim().length()<10){
            date_of_birth.setText("");
            date_of_birth.requestFocus();
            Snackbar.make(view, R.string.select_date_of_birth, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*................................Check Spouse's Date of Birth................................*/
    public boolean checkSpouseDateOfBirth (View view) {
        if (spouse_date_of_birth.getText().toString().trim().length()>0 &&
                spouse_date_of_birth.getText().toString().trim().length()<10){
            spouse_date_of_birth.setText("");
            spouse_date_of_birth.requestFocus();
            Snackbar.make(view, R.string.select_spouse_date_of_birth, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*..................................Check Wedding Anniversary.................................*/
    public boolean checkWeddingAnniversary (View view) {
        if (wedding_anniversary.getText().toString().trim().length()>0 &&
                wedding_anniversary.getText().toString().trim().length()<10){
            wedding_anniversary.setText("");
            wedding_anniversary.requestFocus();
            Snackbar.make(view, R.string.select_wedding_anniversary, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Aadhaar Number....................................*/
    public boolean checkAadhaarNumber (View view) {
        if (aadhaar_number.getText().toString().trim().length()>0 &&
                aadhaar_number.getText().toString().trim().length()<12){
            aadhaar_number.requestFocus();
            Snackbar.make(view, R.string.enter_aadhaar_number, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (aadhaar_number.getText().toString().trim().length()==10 && !valid_aadhar_number) {
            aadhaar_number.requestFocus();
            Snackbar.make(view, R.string.invalid_aadhaar_number, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*........................................Check PAN No........................................*/
    public boolean checkPANNo (View view) {
        if (pan_no.getText().toString().trim().length()>0 &&
                pan_no.getText().toString().trim().length()<10){
            pan_no.requestFocus();
            Snackbar.make(view, R.string.enter_pan_no, Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (pan_no.getText().toString().trim().length()==10 && !valid_pan_no) {
            pan_no.requestFocus();
            Snackbar.make(view, R.string.invalid_pan_no, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Verify Aadhaar.......................................*/
    private void verifyAadhaar(){
        Snackbar.make(layout, R.string.verifying_aadhaar, Snackbar.LENGTH_LONG).show();

        StringRequest request = new StringRequest(Request.Method.POST, api_aadhaar_verification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Verifying Aadhaar>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    valid_aadhar_number = true;
                    layout_aadhaar_number.setHelperText("Aadhaar Number Verified");
                    layout_aadhaar_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorGreenArray[0]));
                } else {
                    layout_aadhaar_number.setHelperText("Invalid Aadhaar Number");
                    layout_aadhaar_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorRedArray[0]));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_aadhaar_number.setHelperText("Failed to verify Aadhaar Number");
                layout_aadhaar_number.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorRedArray[0]));
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("aadhar_NO", aadhaar_number.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.........................................Verify PAN.........................................*/
    private void verifyPAN(){
        Snackbar.make(layout, R.string.verifying_pan, Snackbar.LENGTH_LONG).show();

        StringRequest request = new StringRequest(Request.Method.POST, api_pan_verification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Verifying PAN>>>", "onResponse::::: " + response);
                JSONObject resObj;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    valid_pan_no = true;
                    layout_pan_no.setHelperText("PAN No Verified");
                    layout_pan_no.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorGreenArray[0]));
                } else {
                    layout_pan_no.setHelperText("Invalid PAN No");
                    layout_pan_no.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorRedArray[0]));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_pan_no.setHelperText("Failed to verify Aadhaar Number");
                layout_pan_no.setHelperTextColor(AppCompatResources.getColorStateList(getApplicationContext(),colorRedArray[0]));
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("pan_NO", pan_no.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*.....................................Profile Details........................................*/
    private void setProfileDetails(){
        progressDialog = new ProgressDialog(AddProfileActivity.this);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_profile_details, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Profile Details>>>", "onResponse::::: " + response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                if (status == 1) {
                    try {
                        JSONObject jsonObject = resObj.getJSONObject("user_dtl");
                        name.setText(jsonObject.getString("name"));
                        email_id.setText(jsonObject.getString("emailid"));
                        mobile_number.setText(jsonObject.getString("phone_no"));

                        if (jsonObject.getString("address").length() < 1)
                            address.setText("");
                        else
                            address.setText(jsonObject.getString("address"));

                        if (jsonObject.getString("dob").length() <= 5)
                            date_of_birth.setText("");
                        else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("dob")))
                            date_of_birth.setText("");
                        else
                            date_of_birth.setText(changeDateFormatUI(jsonObject.getString("dob")));

                        if (jsonObject.getString("name_of_spouse").length() < 1)
                            spouse_name.setText("");
                        else
                            spouse_name.setText(jsonObject.getString("name_of_spouse"));

                        if (jsonObject.getString("spouse_dob").length() <= 5)
                            spouse_date_of_birth.setText("");
                        else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("spouse_dob")))
                            spouse_date_of_birth.setText("");
                        else
                            spouse_date_of_birth.setText(changeDateFormatUI(jsonObject.getString("spouse_dob")));

                        if (jsonObject.getString("marriage_anniversary").length() <= 5)
                            wedding_anniversary.setText("");
                        else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("marriage_anniversary")))
                            wedding_anniversary.setText("");
                        else
                            wedding_anniversary.setText(changeDateFormatUI(jsonObject.getString("marriage_anniversary")));

                        if (jsonObject.getString("no_of_children").length() < 1)
                            number_of_children.setText("");
                        else
                            number_of_children.setText(jsonObject.getString("no_of_children"));

                        if (jsonObject.getString("profession").length() < 1)
                            profession.setText("");
                        else
                            profession.setText(jsonObject.getString("profession"));

                        if (jsonObject.getString("montly_earning").length() < 1)
                            annual_income.setText("");
                        else
                            annual_income.setText(jsonObject.getString("montly_earning"));

                        if (jsonObject.getString("no_of_dependents_in_family").length() < 1)
                            number_of_dependents.setText("");
                        else
                            number_of_dependents.setText(jsonObject.getString("no_of_dependents_in_family"));

                        if (jsonObject.getString("aadhar_no").length() < 1)
                            aadhaar_number.setText("");
                        else
                            aadhaar_number.setText(jsonObject.getString("aadhar_no"));

                        if (jsonObject.getString("pan_no").length() < 1)
                            pan_no.setText("");
                        else
                            pan_no.setText(jsonObject.getString("pan_no"));

                        if ("N".equalsIgnoreCase(jsonObject.getString("payment_flag")))
                            pan_no.setEnabled(true);
                        else if ("Y".equalsIgnoreCase(jsonObject.getString("payment_flag")))
                            pan_no.setEnabled(false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    layout_profile_info.setVisibility(View.VISIBLE);
                    layout_update.setVisibility(View.VISIBLE);
                } else {
                    layout_profile_info.setVisibility(View.GONE);
                    layout_update.setVisibility(View.GONE);
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_profile_info.setVisibility(View.GONE);
                layout_update.setVisibility(View.GONE);
                progressDialog.dismiss();
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
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    /*..................................Update Profile Details....................................*/
    private void updateProfileDetails(){
        progressDialog = new ProgressDialog(AddProfileActivity.this);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, api_profile_edit, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Update Profile>>>", "onResponse::::: " + response);
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
                    editor.putString("name", name.getText().toString().trim());
                    editor.commit();
                    Toast.makeText(AddProfileActivity.this, "Profile Info Updated Successfully", Toast.LENGTH_SHORT).show();
                    backPressed();

                    /*if ("".equalsIgnoreCase(number_of_dependents.getText().toString().trim()) ||
                            "0".equalsIgnoreCase(number_of_dependents.getText().toString().trim()))
                        backPressed();
                    else {
                        Intent intent = new Intent (AddProfileActivity.this, AddDependentActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        AddProfileActivity.this.startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }*/

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
                params.put("name", name.getText().toString().trim());
                params.put("address", address.getText().toString().trim());
                params.put("phone_no", mobile_number.getText().toString().trim());
                params.put("dob", changeDateFormatDB(date_of_birth.getText().toString().trim()));
                params.put("name_of_spouse", spouse_name.getText().toString().trim());
                params.put("spouse_dob", changeDateFormatDB(spouse_date_of_birth.getText().toString().trim()));
                params.put("marriage_anniversary", changeDateFormatDB(wedding_anniversary.getText().toString().trim()));
                params.put("no_of_children", number_of_children.getText().toString().trim());
                params.put("children_age", "");
                params.put("profession", profession.getText().toString().trim());
                params.put("montly_earning", annual_income.getText().toString().trim());
                params.put("no_of_dependents_in_family", number_of_dependents.getText().toString().trim());
                params.put("aadhar", aadhaar_number.getText().toString().trim());
                params.put("pan", pan_no.getText().toString().trim());
                params.put("picture", "");
                params.put("aadharpicture", "");
                params.put("panpicture", "");
                Log.i("Update Profile>>>", "params::::: " + params);
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
            backPressed();
        } else {
            finishAffinity();
        }
    }

    public void backPressed(){
        Intent intent = new Intent (AddProfileActivity.this, DashboardActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("type","Profile");
        AddProfileActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
