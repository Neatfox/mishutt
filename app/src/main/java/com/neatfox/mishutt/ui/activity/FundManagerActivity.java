package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;

import net.gotev.uploadservice.data.UploadInfo;
import net.gotev.uploadservice.network.ServerResponse;
import net.gotev.uploadservice.observer.request.RequestObserverDelegate;
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.neatfox.mishutt.Constants.REQUEST_FILE;
import static com.neatfox.mishutt.Constants.REQUEST_STORAGE_PERMISSION;
import static com.neatfox.mishutt.Constants.api_store_fund;

public class FundManagerActivity extends MainActivity implements RequestObserverDelegate {

    LinearLayout layout_personal_info,layout_loan_info,layout_business_info;
    EditText name,mobile_number,email_id,date_of_birth,address,city,state,pin_code,user_photo,
            presentation,requested_loan_amount,number_of_deposits,total_amount_deposited,
            ending_balance,business_name,business_address,business_city,business_state,
            business_pin_code,number_of_employees,average_annual_revenue,best_time_to_call;
    AppCompatAutoCompleteTextView credit_quality,use_of_funds,business_type,years_in_business,
            tax_liens,credit_permission;
    ImageButton add_user_photo,add_presentation;
    Button submit;
    String time24 = "",_filePathOne,_filePathTwo;
    boolean has_user_photo = false,has_presentation = false;
    int backPress = 0,mHour,mMinute,time_minute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_fund_manager, contentFrameLayout);

        FundManagerActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        layout_personal_info = findViewById(R.id.layout_personal_info);
        layout_loan_info = findViewById(R.id.layout_loan_info);
        layout_business_info = findViewById(R.id.layout_business_info);
        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        email_id = findViewById(R.id.et_email_id);
        date_of_birth = findViewById(R.id.et_date_of_birth);
        address = findViewById(R.id.et_address);
        city = findViewById(R.id.et_city);
        state = findViewById(R.id.et_state);
        pin_code = findViewById(R.id.et_pin_code);
        user_photo = findViewById(R.id.et_user_photo);
        presentation = findViewById(R.id.et_upload_presentation);
        requested_loan_amount = findViewById(R.id.et_requested_loan_amount);
        number_of_deposits = findViewById(R.id.et_number_of_deposits);
        total_amount_deposited = findViewById(R.id.et_total_amount_deposit);
        ending_balance = findViewById(R.id.et_ending_balance);
        business_name = findViewById(R.id.et_business_name);
        business_address = findViewById(R.id.et_business_address);
        business_city = findViewById(R.id.et_business_city);
        business_state = findViewById(R.id.et_business_state);
        business_pin_code = findViewById(R.id.et_business_pin_code);
        number_of_employees = findViewById(R.id.et_number_of_employees);
        average_annual_revenue = findViewById(R.id.et_average_annual_revenue);
        credit_quality = findViewById(R.id.et_credit_quality);
        use_of_funds = findViewById(R.id.et_use_of_funds);
        business_type = findViewById(R.id.et_business_type);
        years_in_business = findViewById(R.id.et_years_in_business);
        tax_liens = findViewById(R.id.et_tax_liens);
        credit_permission = findViewById(R.id.et_permission_to_pull_credit);
        best_time_to_call = findViewById(R.id.et_best_time_to_call);
        add_user_photo = findViewById(R.id.ib_add_user_photo);
        add_presentation = findViewById(R.id.ib_add_presentation);
        submit = findViewById(R.id.button_submit);

        tv_toolbar.setText(getIntent().getStringExtra("item_name"));
        name.setText(_name);
        mobile_number.setText(_mobile_number);
        email_id.setText(_email_id);
        date_of_birth.setText(_date_of_birth);
        address.setText(_address);

        date_of_birth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    date_of_birth.setText("");
                    datePicker();
                }
            }
        });

        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date_of_birth.getText().toString().trim().length()<10)
                    datePicker();
                else
                    address.requestFocus();
            }
        });

        ArrayAdapter<CharSequence> adapter_one = ArrayAdapter.createFromResource(
                FundManagerActivity.this, R.array.credit_quality, R.layout.adapter_spinner_item);
        adapter_one.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        credit_quality.setAdapter(adapter_one);

        ArrayAdapter<CharSequence> adapter_two = ArrayAdapter.createFromResource(
                FundManagerActivity.this, R.array.use_of_funds, R.layout.adapter_spinner_item);
        adapter_two.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        use_of_funds.setAdapter(adapter_two);

        ArrayAdapter<CharSequence> adapter_three = ArrayAdapter.createFromResource(
                FundManagerActivity.this, R.array.business_type, R.layout.adapter_spinner_item);
        adapter_three.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        business_type.setAdapter(adapter_three);

        ArrayAdapter<CharSequence> adapter_four = ArrayAdapter.createFromResource(
                FundManagerActivity.this, R.array.years_in_business, R.layout.adapter_spinner_item);
        adapter_four.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        years_in_business.setAdapter(adapter_four);

        ArrayAdapter<CharSequence> adapter_five = ArrayAdapter.createFromResource(
                FundManagerActivity.this, R.array.tax_liens, R.layout.adapter_spinner_item);
        adapter_five.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tax_liens.setAdapter(adapter_five);

        ArrayAdapter<CharSequence> adapter_six = ArrayAdapter.createFromResource(
                FundManagerActivity.this, R.array.permission, R.layout.adapter_spinner_item);
        adapter_six.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        credit_permission.setAdapter(adapter_six);

        credit_quality.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    credit_quality.showDropDown();
                }
            }
        });

        credit_quality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (credit_quality.getText().toString().trim().length()<4){
                    credit_quality.showDropDown();
                } else
                    user_photo.requestFocus();
            }
        });

        use_of_funds.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    use_of_funds.showDropDown();
                }
            }
        });

        use_of_funds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (use_of_funds.getText().toString().trim().length()<4){
                    use_of_funds.showDropDown();
                }
            }
        });

        business_type.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    business_type.showDropDown();
                }
            }
        });

        business_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (business_type.getText().toString().trim().length()<3){
                    business_type.showDropDown();
                } else
                    number_of_employees.requestFocus();
            }
        });

        years_in_business.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    years_in_business.showDropDown();
                }
            }
        });

        years_in_business.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (years_in_business.getText().toString().trim().length()<3){
                    years_in_business.showDropDown();
                } else
                    tax_liens.requestFocus();
            }
        });

        tax_liens.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    tax_liens.showDropDown();
                }
            }
        });

        tax_liens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tax_liens.getText().toString().trim().length()<3){
                    tax_liens.showDropDown();
                } else
                    average_annual_revenue.requestFocus();
            }
        });

        credit_permission.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    credit_permission.showDropDown();
                }
            }
        });

        credit_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (credit_permission.getText().toString().trim().length()<3){
                    credit_permission.showDropDown();
                } else
                    best_time_to_call.requestFocus();
            }
        });

        best_time_to_call.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    timePicker();
                }
            }
        });

        best_time_to_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (best_time_to_call.getText().toString().trim().length()<4)
                    timePicker();
            }
        });

        add_user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                has_user_photo = true;
                has_presentation = false;
                user_photo.setText("");
                requestStoragePermission();
            }
        });

        add_presentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                has_presentation = true;
                has_user_photo = false;
                presentation.setText("");
                requestStoragePermission();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layout_personal_info.getVisibility() == View.VISIBLE){
                    if (checkName(v) && checkMobileNumber(v) && checkEmailId(v) && checkDateOfBirth(v) &&
                            checkAddress(v) && checkCity(v) && checkState(v) && checkPINCode(v) &&
                            checkCreditQuality(v) && checkUserPhoto(v) && checkPresentation(v)){
                        layout_personal_info.setVisibility(View.GONE);
                        layout_loan_info.setVisibility(View.VISIBLE);
                    }
                } else if (layout_loan_info.getVisibility() == View.VISIBLE){
                    if (checkLoanAmount(v) && checkNumberOfDeposits(v) && checkTotalAmountDeposit(v) &&
                            checkLastMonthEndingBalance(v) && checkUseOfFunds(v)){
                        layout_loan_info.setVisibility(View.GONE);
                        layout_business_info.setVisibility(View.VISIBLE);
                        submit.setText(R.string.submit);
                    }
                } else if (layout_business_info.getVisibility() == View.VISIBLE){
                    if (checkBusinessName(v) && checkBusinessAddress(v) && checkBusinessCity(v) &&
                            checkBusinessState(v) && checkBusinessPINCode(v) && checkBusinessType(v) &&
                            checkNumberOfEmployees(v) && checkYearsInBusiness(v) && checkTaxLiens(v) &&
                            checkAverageAnnualRevenue(v) && checkPermissionToPullCredit(v) && checkBestTimeToCall(v)){
                        if (isNetworkAvailable()) {
                            noNetwork();
                        } else {
                            submitDetails();
                        }
                    }
                }
            }
        });
    }
    /*.........................................Date Picker........................................*/
    public void datePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog (FundManagerActivity.this,
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
            date_of_birth.setText(date);
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
    /*........................................Time Picker.........................................*/
    public void timePicker(){
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time_minute = (hourOfDay * 60) + minute;
                        time24 = hourOfDay + ":" + minute;
                        best_time_to_call.setText(convertTime(time24));
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public String convertTime(String time){
        String newTime = "";
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
            final Date dateObj = sdf.parse(time);
            assert dateObj != null;
            newTime = (new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(dateObj));
        } catch (final ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        return newTime;
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
        if (date_of_birth.getText().toString().trim().length()<10){
            date_of_birth.setText("");
            date_of_birth.requestFocus();
            Snackbar.make(view, R.string.select_date_of_birth, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*........................................Check Address.......................................*/
    public boolean checkAddress (View view) {
        if (address.getText().toString().trim().length()<1){
            address.requestFocus();
            Snackbar.make(view, R.string.enter_address, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.........................................Check City.........................................*/
    public boolean checkCity (View view) {
        if (city.getText().toString().trim().length()<1){
            city.requestFocus();
            Snackbar.make(view, R.string.enter_city, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.........................................Check State........................................*/
    public boolean checkState (View view) {
        if (state.getText().toString().trim().length()<1){
            state.requestFocus();
            Snackbar.make(view, R.string.enter_state, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check PIN Code.......................................*/
    public boolean checkPINCode (View view) {
        if (pin_code.getText().toString().trim().length()<1){
            pin_code.requestFocus();
            Snackbar.make(view, R.string.enter_pin_code, Snackbar.LENGTH_SHORT).show();
            return false;
        } if (pin_code.getText().toString().trim().length()<6){
            pin_code.requestFocus();
            Snackbar.make(view, R.string.enter_pin_code_six_digit, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Credit Quality....................................*/
    public boolean checkCreditQuality(View view) {
        if (credit_quality.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < credit_quality.getAdapter().getCount(); i++) {
                if (credit_quality.getText().toString().trim().equalsIgnoreCase(credit_quality.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            credit_quality.setText("");
        }
        credit_quality.requestFocus();
        Snackbar.make(view, R.string.select_credit_quality, Snackbar.LENGTH_SHORT).show();
        return false;
    }/*......................................Check User Photo......................................*/
    public boolean checkUserPhoto (View view) {
        if (user_photo.getText().toString().trim().length()<1){
            Snackbar.make(view, R.string.add_photo, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Presentation.....................................*/
    public boolean checkPresentation (View view) {
        if (presentation.getText().toString().trim().length()<1){
            Snackbar.make(view, R.string.add_presentation, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Loan Amount......................................*/
    public boolean checkLoanAmount (View view) {
        if (requested_loan_amount.getText().toString().trim().length()<1){
            requested_loan_amount.requestFocus();
            Snackbar.make(view, R.string.enter_requested_loan_amount, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*..................................Check Number Of Deposits..................................*/
    public boolean checkNumberOfDeposits (View view) {
        if (number_of_deposits.getText().toString().trim().length()<1){
            number_of_deposits.requestFocus();
            Snackbar.make(view, R.string.enter_number_of_deposits, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.................................Check Total Amount Deposit.................................*/
    public boolean checkTotalAmountDeposit (View view) {
        if (total_amount_deposited.getText().toString().trim().length()<1){
            total_amount_deposited.requestFocus();
            Snackbar.make(view, R.string.enter_total_amount_deposit, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*..............................Check Last Month Ending Balance...............................*/
    public boolean checkLastMonthEndingBalance (View view) {
        if (ending_balance.getText().toString().trim().length()<1){
            ending_balance.requestFocus();
            Snackbar.make(view, R.string.enter_ending_balance, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Use Of Funds.....................................*/
    public boolean checkUseOfFunds(View view) {
        if (use_of_funds.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < use_of_funds.getAdapter().getCount(); i++) {
                if (use_of_funds.getText().toString().trim().equalsIgnoreCase(use_of_funds.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            use_of_funds.setText("");
        }
        use_of_funds.requestFocus();
        Snackbar.make(view, R.string.select_use_of_funds, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*....................................Check Business Name.....................................*/
    public boolean checkBusinessName (View view) {
        if (business_name.getText().toString().trim().length()<1){
            business_name.requestFocus();
            Snackbar.make(view, R.string.enter_business_name, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*........................................Check Address.......................................*/
    public boolean checkBusinessAddress (View view) {
        if (business_address.getText().toString().trim().length()<1){
            business_address.requestFocus();
            Snackbar.make(view, R.string.enter_address, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.........................................Check City.........................................*/
    public boolean checkBusinessCity (View view) {
        if (business_city.getText().toString().trim().length()<1){
            business_city.requestFocus();
            Snackbar.make(view, R.string.enter_city, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.........................................Check State........................................*/
    public boolean checkBusinessState (View view) {
        if (business_state.getText().toString().trim().length()<1){
            business_state.requestFocus();
            Snackbar.make(view, R.string.enter_state, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check PIN Code.......................................*/
    public boolean checkBusinessPINCode (View view) {
        if (business_pin_code.getText().toString().trim().length()<1){
            business_pin_code.requestFocus();
            Snackbar.make(view, R.string.enter_pin_code, Snackbar.LENGTH_SHORT).show();
            return false;
        } if (business_pin_code.getText().toString().trim().length()<6){
            business_pin_code.requestFocus();
            Snackbar.make(view, R.string.enter_pin_code_six_digit, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Business Type....................................*/
    public boolean checkBusinessType(View view) {
        if (business_type.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < business_type.getAdapter().getCount(); i++) {
                if (business_type.getText().toString().trim().equalsIgnoreCase(business_type.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            business_type.setText("");
        }
        business_type.requestFocus();
        Snackbar.make(view, R.string.select_business_type, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*.................................Check Number Of Employees..................................*/
    public boolean checkNumberOfEmployees (View view) {
        if (number_of_employees.getText().toString().trim().length()<1){
            number_of_employees.requestFocus();
            Snackbar.make(view, R.string.enter_number_of_employees, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*...................................Check Years In Business..................................*/
    public boolean checkYearsInBusiness(View view) {
        if (years_in_business.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < years_in_business.getAdapter().getCount(); i++) {
                if (years_in_business.getText().toString().trim().equalsIgnoreCase(years_in_business.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            years_in_business.setText("");
        }
        years_in_business.requestFocus();
        Snackbar.make(view, R.string.select_years_in_business, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*......................................Check Tax Liens.......................................*/
    public boolean checkTaxLiens(View view) {
        if (tax_liens.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < tax_liens.getAdapter().getCount(); i++) {
                if (tax_liens.getText().toString().trim().equalsIgnoreCase(tax_liens.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            tax_liens.setText("");
        }
        tax_liens.requestFocus();
        Snackbar.make(view, R.string.select_tax_liens, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*................................Check Average Annual Revenue................................*/
    public boolean checkAverageAnnualRevenue (View view) {
        if (average_annual_revenue.getText().toString().trim().length()<1){
            average_annual_revenue.requestFocus();
            Snackbar.make(view, R.string.enter_average_annual_revenue, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*..............................Check Permission to Pull Credit...............................*/
    public boolean checkPermissionToPullCredit(View view) {
        if (credit_permission.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < credit_permission.getAdapter().getCount(); i++) {
                if (credit_permission.getText().toString().trim().equalsIgnoreCase(credit_permission.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            credit_permission.setText("");
        }
        credit_permission.requestFocus();
        Snackbar.make(view, R.string.select_permission_to_pull_credit, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*..................................Check Best Time to Call...................................*/
    public boolean checkBestTimeToCall (View view) {
        if (best_time_to_call.getText().toString().trim().length()<1){
            best_time_to_call.requestFocus();
            Snackbar.make(view, R.string.enter_best_time_to_call, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Storage Permission.....................................*/
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(FundManagerActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions( FundManagerActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else
            showFileChooser();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //STORAGE PERMISSION
        if (requestCode == REQUEST_STORAGE_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showFileChooser();
            }
        }
    }
    /*.........................................Choose File........................................*/
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (has_user_photo)
            intent.setType("image/*");
        else
            intent.setType("application/pdf/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            String _filePath = filePath.toString();
            String file_name = filePath.getPath();
            String[] separated = Objects.requireNonNull(file_name).split("/");
            int len = separated.length;
            file_name = separated[len-1];
            String extension = file_name.substring(file_name.lastIndexOf("."));
            System.out.println(file_name+"/t/t"+extension);
            if (has_user_photo){
                _filePathOne = _filePath;
                user_photo.setText(file_name);
            } else if (has_presentation){
                _filePathTwo = _filePath;
                presentation.setText(file_name);
            }
            has_user_photo = has_presentation = false;
        } else {
            has_user_photo = has_presentation = false;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    /*................................Submit Details & Upload File................................*/
    private void submitDetails() {
        progressDialog = new ProgressDialog(FundManagerActivity.this);
        progressDialog.setMessage("Saving Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        System.out.println(_filePathOne+"\n"+_filePathTwo);

        if (_filePathOne == null || _filePathTwo == null) {
            Toast.makeText(FundManagerActivity.this, "Unable to Add Files", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        } else {
            try {
                String uploadId = UUID.randomUUID().toString();

                //Creating a multi part request
                new MultipartUploadRequest(FundManagerActivity.this, api_store_fund)
                        .setMethod("POST")
                        .addParameter("uid",sharedPreference.getString("register_id", ""))
                        .addParameter("name", name.getText().toString().trim())
                        .addParameter("mobile", mobile_number.getText().toString().trim())
                        .addParameter("email", email_id.getText().toString().trim())
                        .addParameter("dob", (date_of_birth.getText().toString().trim()))
                        .addParameter("address", address.getText().toString().trim())
                        .addParameter("city", city.getText().toString().trim())
                        .addParameter("state", state.getText().toString().trim())
                        .addParameter("zipcode", pin_code.getText().toString().trim())
                        .addParameter("cquality", credit_quality.getText().toString().trim())
                        .addParameter("lrequested", requested_loan_amount.getText().toString().trim())
                        .addParameter("ndeposits", number_of_deposits.getText().toString().trim())
                        .addParameter("tdeposits", total_amount_deposited.getText().toString().trim())
                        .addParameter("ebalance", ending_balance.getText().toString().trim())
                        .addParameter("use_of_funds", use_of_funds.getText().toString().trim())
                        .addParameter("bname", business_name.getText().toString().trim())
                        .addParameter("baddress", address.getText().toString().trim())
                        .addParameter("bcity", business_city.getText().toString().trim())
                        .addParameter("bstate", business_state.getText().toString().trim())
                        .addParameter("bzip", business_pin_code.getText().toString().trim())
                        .addParameter("etype", business_type.getText().toString().trim())
                        .addParameter("NOE", number_of_employees.getText().toString().trim())
                        .addParameter("YIB", years_in_business.getText().toString().trim())
                        .addParameter("tax", tax_liens.getText().toString().trim())
                        .addParameter("annual_revenue", average_annual_revenue.getText().toString().trim())
                        .addParameter("pull_credit", credit_permission.getText().toString().trim())
                        .addParameter("call_time", best_time_to_call.getText().toString().trim())
                        .addFileToUpload(_filePathOne, "profile") //Adding file
                        .addFileToUpload(_filePathTwo, "presentation")
                        .setUploadID(uploadId)
                        .setMaxRetries(2)
                        .subscribe(FundManagerActivity.this,FundManagerActivity.this,FundManagerActivity.this)
                        .register(); //Starting the upload

            } catch (Exception exc) {
                progressDialog.dismiss();
                System.out.println(exc.toString());
            }
        }
    }

    @Override
    public void onCompleted(@NotNull Context context, @NotNull UploadInfo uploadInfo) { }

    @Override
    public void onCompletedWhileNotObserving() { }

    @Override
    public void onError(@NotNull Context context, @NotNull UploadInfo uploadInfo, @NotNull Throwable throwable) {
        System.out.println("Failed"+"\n\n"+uploadInfo+"/n/n"+throwable);
        progressDialog.dismiss();
        snackBarError();
    }

    @Override
    public void onProgress(@NotNull Context context, @NotNull UploadInfo uploadInfo) { }

    @Override
    public void onSuccess(@NotNull Context context, @NotNull UploadInfo uploadInfo, @NotNull ServerResponse serverResponse) {
        System.out.println("Success"+"\n\n"+serverResponse+"\n\n"+uploadInfo);

        Toast.makeText(FundManagerActivity.this, "Applied for Credit Score Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (FundManagerActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        FundManagerActivity.this.startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
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
        if (layout_loan_info.getVisibility() == View.VISIBLE){
            backPress = 0;
            layout_personal_info.setVisibility(View.VISIBLE);
            layout_loan_info.setVisibility(View.GONE);

        } else if(layout_business_info.getVisibility() == View.VISIBLE){
            backPress = 0;
            layout_loan_info.setVisibility(View.VISIBLE);
            layout_business_info.setVisibility(View.GONE);
            submit.setText(R.string.next);

        } else {
            Intent intent = new Intent (FundManagerActivity.this, HomeActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            FundManagerActivity.this.startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }
}
