package com.neatfox.mishutt.ui.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

import static com.neatfox.mishutt.Constants.REQUEST_FILE;
import static com.neatfox.mishutt.Constants.REQUEST_STORAGE_PERMISSION;
import static com.neatfox.mishutt.Constants.api_store_cibil;

public class CreditScoreActivity extends MainActivity implements RequestObserverDelegate {

    LinearLayout layout_info_one,layout_info_two;
    EditText name,mobile_number,email_id,date_of_birth,address,user_photo,residence_proof,id_proof,
            office_name_address,bank_name_wBranch;
    AppCompatAutoCompleteTextView rectification,occupation,have_previous_credit_report,plan_opted,
            mode_of_payment;
    ImageButton add_user_photo,add_residence_id,add_id_proof;
    Button submit;
    boolean has_user_photo = false,has_residence_id = false, has_id_proof = false;
    String _filePathOne,_filePathTwo,_filePathThree;
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_credit_score, contentFrameLayout);

        CreditScoreActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tv_toolbar.setText(R.string.credit_score);
        layout_info_one = findViewById(R.id.layout_info_one);
        layout_info_two = findViewById(R.id.layout_info_two);
        name = findViewById(R.id.et_name);
        mobile_number = findViewById(R.id.et_mobile_number);
        email_id = findViewById(R.id.et_email_id);
        date_of_birth = findViewById(R.id.et_date_of_birth);
        address = findViewById(R.id.et_address);
        user_photo = findViewById(R.id.et_user_photo);
        residence_proof = findViewById(R.id.et_residence_proof);
        id_proof = findViewById(R.id.et_ID_proof);
        office_name_address = findViewById(R.id.et_office_name_address);
        bank_name_wBranch = findViewById(R.id.et_bank_name_wBranch);
        rectification = findViewById(R.id.et_rectification_for);
        occupation = findViewById(R.id.et_occupation);
        have_previous_credit_report = findViewById(R.id.et_have_previous_credit_report);
        plan_opted = findViewById(R.id.et_plan_opted);
        mode_of_payment = findViewById(R.id.et_mode_of_payment);
        add_user_photo = findViewById(R.id.ib_add_user_photo);
        add_residence_id = findViewById(R.id.ib_add_residence_id);
        add_id_proof = findViewById(R.id.ib_add_id_proof);
        submit = findViewById(R.id.button_submit);

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
                CreditScoreActivity.this, R.array.rectification_for, R.layout.adapter_spinner_item);
        adapter_one.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rectification.setAdapter(adapter_one);

        ArrayAdapter<CharSequence> adapter_two = ArrayAdapter.createFromResource(
                CreditScoreActivity.this, R.array.occupation, R.layout.adapter_spinner_item);
        adapter_two.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        occupation.setAdapter(adapter_two);

        ArrayAdapter<CharSequence> adapter_three = ArrayAdapter.createFromResource(
                CreditScoreActivity.this, R.array.permission, R.layout.adapter_spinner_item);
        adapter_three.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        have_previous_credit_report.setAdapter(adapter_three);

        ArrayAdapter<CharSequence> adapter_four = ArrayAdapter.createFromResource(
                CreditScoreActivity.this, R.array.credit_score_plan, R.layout.adapter_spinner_item);
        adapter_four.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plan_opted.setAdapter(adapter_four);

        ArrayAdapter<CharSequence> adapter_five = ArrayAdapter.createFromResource(
                CreditScoreActivity.this, R.array.mode_of_payment, R.layout.adapter_spinner_item);
        adapter_five.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mode_of_payment.setAdapter(adapter_five);

        rectification.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    rectification.showDropDown();
                }
            }
        });

        rectification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rectification.getText().toString().trim().length()<4){
                    rectification.showDropDown();
                } else
                    occupation.requestFocus();
            }
        });

        occupation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    occupation.showDropDown();
                }
            }
        });

        occupation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (occupation.getText().toString().trim().length()<4){
                    occupation.showDropDown();
                } else
                    office_name_address.requestFocus();
            }
        });

        have_previous_credit_report.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    have_previous_credit_report.showDropDown();
                }
            }
        });

        have_previous_credit_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (have_previous_credit_report.getText().toString().trim().length()<3){
                    have_previous_credit_report.showDropDown();
                } else
                    plan_opted.requestFocus();
            }
        });

        plan_opted.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    plan_opted.showDropDown();
                }
            }
        });

        plan_opted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plan_opted.getText().toString().trim().length()<4){
                    plan_opted.showDropDown();
                } else
                    mode_of_payment.requestFocus();
            }
        });

        mode_of_payment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mode_of_payment.showDropDown();
                }
            }
        });

        mode_of_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode_of_payment.getText().toString().trim().length()<3){
                    mode_of_payment.showDropDown();
                }
            }
        });

        add_user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                has_user_photo = true;
                has_residence_id = has_id_proof = false;
                user_photo.setText("");
                requestStoragePermission();
            }
        });

        add_residence_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                has_residence_id = true;
                has_user_photo = has_id_proof = false;
                residence_proof.setText("");
                requestStoragePermission();
            }
        });

        add_id_proof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                has_id_proof = true;
                has_user_photo = has_residence_id = false;
                id_proof.setText("");
                requestStoragePermission();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layout_info_one.getVisibility() == View.VISIBLE){
                    if (checkName(v) && checkMobileNumber(v) && checkEmailId(v) && checkDateOfBirth(v) &&
                            checkAddress(v) && checkUserPhoto(v) && checkResidenceProof(v) && checkIDProof(v)){
                        layout_info_one.setVisibility(View.GONE);
                        layout_info_two.setVisibility(View.VISIBLE);
                        submit.setText(R.string.submit);
                    }
                } else if (layout_info_two.getVisibility() == View.VISIBLE){
                    if (checkRectification(v) && checkOccupation(v) && checkBankNameWBranch(v) &&
                            checkPreviousCreditReport(v) && checkPlan(v) && checkPaymentMode(v)){
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
        DatePickerDialog datePickerDialog = new DatePickerDialog (CreditScoreActivity.this,
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
    /*......................................Check User Photo......................................*/
    public boolean checkUserPhoto (View view) {
        if (user_photo.getText().toString().trim().length()<1){
            Snackbar.make(view, R.string.add_photo, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*....................................Check Residence Proof...................................*/
    public boolean checkResidenceProof (View view) {
        if (residence_proof.getText().toString().trim().length()<1){
            Snackbar.make(view, R.string.add_residence_proof, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.......................................Check ID Proof.......................................*/
    public boolean checkIDProof (View view) {
        if (id_proof.getText().toString().trim().length()<1){
            Snackbar.make(view, R.string.add_ID_proof, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*.....................................Check Rectification....................................*/
    public boolean checkRectification(View view) {
        if (rectification.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < rectification.getAdapter().getCount(); i++) {
                if (rectification.getText().toString().trim().equalsIgnoreCase(rectification.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            rectification.setText("");
        }
        rectification.requestFocus();
        Snackbar.make(view, R.string.select_rectification_for, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*......................................Check Occupation......................................*/
    public boolean checkOccupation(View view) {
        if (occupation.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < occupation.getAdapter().getCount(); i++) {
                if (occupation.getText().toString().trim().equalsIgnoreCase(occupation.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            occupation.setText("");
        }
        occupation.requestFocus();
        Snackbar.make(view, R.string.select_occupation, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*................................Check Bank Name & Branch Name...............................*/
    public boolean checkBankNameWBranch(View view) {
        if (bank_name_wBranch.getText().toString().trim().length()<1){
            bank_name_wBranch.requestFocus();
            Snackbar.make(view, R.string.enter_bank_name_wBranch, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }
    /*................................Check Previous Credit Report................................*/
    public boolean checkPreviousCreditReport(View view) {
        if (have_previous_credit_report.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < have_previous_credit_report.getAdapter().getCount(); i++) {
                if (have_previous_credit_report.getText().toString().trim().equalsIgnoreCase(have_previous_credit_report.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            have_previous_credit_report.setText("");
        }
        have_previous_credit_report.requestFocus();
        Snackbar.make(view, R.string.select_have_previous_credit_report, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*.........................................Check Plan.........................................*/
    public boolean checkPlan(View view) {
        if (plan_opted.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < plan_opted.getAdapter().getCount(); i++) {
                if (plan_opted.getText().toString().trim().equalsIgnoreCase(plan_opted.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            plan_opted.setText("");
        }
        plan_opted.requestFocus();
        Snackbar.make(view, R.string.select_plan, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*.....................................Check Payment Mode.....................................*/
    public boolean checkPaymentMode(View view) {
        if (mode_of_payment.getText().toString().trim().length() >= 1) {
            for (int i = 0; i < mode_of_payment.getAdapter().getCount(); i++) {
                if (mode_of_payment.getText().toString().trim().equalsIgnoreCase(mode_of_payment.getAdapter().getItem(i).toString())) {
                    return true;
                }
            }
            mode_of_payment.setText("");
        }
        mode_of_payment.requestFocus();
        Snackbar.make(view, R.string.select_mode_of_payment, Snackbar.LENGTH_SHORT).show();
        return false;
    }
    /*.....................................Storage Permission.....................................*/
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(CreditScoreActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions( CreditScoreActivity.this,
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
            } else if (has_residence_id){
                _filePathTwo = _filePath;
                residence_proof.setText(file_name);
            } else if (has_id_proof){
                _filePathThree = _filePath;
                id_proof.setText(file_name);
            }
            has_user_photo = has_residence_id = has_id_proof = false;
        } else {
            has_user_photo = has_residence_id = has_id_proof = false;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    /*................................Submit Details & Upload File................................*/
    private void submitDetails() {
        progressDialog = new ProgressDialog(CreditScoreActivity.this);
        progressDialog.setMessage("Saving Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        System.out.println(_filePathOne+"\n"+_filePathTwo+"\n"+_filePathThree);

        if (_filePathOne == null || _filePathTwo == null || _filePathThree == null) {
            Toast.makeText(CreditScoreActivity.this, "Unable to Add Files", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        } else {
            try {
                String uploadId = UUID.randomUUID().toString();

                //Creating a multi part request
                new MultipartUploadRequest(CreditScoreActivity.this, api_store_cibil)
                        .setMethod("POST")
                        .addParameter("uid",sharedPreference.getString("register_id", ""))
                        .addParameter("name", name.getText().toString().trim())
                        .addParameter("mobile", mobile_number.getText().toString().trim())
                        .addParameter("email", email_id.getText().toString().trim())
                        .addParameter("dob", (date_of_birth.getText().toString().trim()))
                        .addParameter("address", address.getText().toString().trim())
                        .addParameter("rectification", rectification.getText().toString().trim())
                        .addParameter("occupation", occupation.getText().toString().trim())
                        .addParameter("office", office_name_address.getText().toString().trim())
                        .addParameter("bank", bank_name_wBranch.getText().toString().trim())
                        .addParameter("report", have_previous_credit_report.getText().toString().trim())
                        .addParameter("plan", plan_opted.getText().toString().trim())
                        .addParameter("payment", mode_of_payment.getText().toString().trim())
                        .addFileToUpload(_filePathOne, "profile") //Adding file
                        .addFileToUpload(_filePathTwo, "rproof")
                        .addFileToUpload(_filePathThree, "idproof")
                        .setUploadID(uploadId)
                        .setMaxRetries(2)
                        .subscribe(CreditScoreActivity.this,CreditScoreActivity.this,CreditScoreActivity.this)
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

        Toast.makeText(CreditScoreActivity.this, "Applied for Credit Score Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (CreditScoreActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        CreditScoreActivity.this.startActivity(intent);
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
        if (layout_info_two.getVisibility() == View.VISIBLE){
            backPress = 0;
            layout_info_one.setVisibility(View.VISIBLE);
            layout_info_two.setVisibility(View.GONE);
            submit.setText(R.string.next);

        } else {
            Intent intent = new Intent (CreditScoreActivity.this, HomeActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            CreditScoreActivity.this.startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }
}
