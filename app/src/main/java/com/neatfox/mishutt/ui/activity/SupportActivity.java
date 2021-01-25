package com.neatfox.mishutt.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;

public class SupportActivity extends MainActivity {

    EditText name,email_id,message;
    Button submit;
    int backPress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_support, contentFrameLayout);

        SupportActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tv_toolbar.setText(R.string.support);

        name = findViewById(R.id.et_name);
        email_id = findViewById(R.id.et_email_id);
        message = findViewById(R.id.et_message);
        submit = findViewById(R.id.button_submit);

        name.setText(_name);
        email_id.setText(_email_id);
        /*.........................................Check..........................................*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkName(view) && checkEmailId(view) && checkMessage(view)){
                    if (isNetworkAvailable()) {
                        noNetwork();
                    } else {
                        submit();
                    }
                }
            }
        });
        /*....................................Bottom Navigation...................................*/
        bottom_navigation_id = 5;
        bottomNavigation.show(bottom_navigation_id,false);
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
    /*........................................Check Message.......................................*/
    public boolean checkMessage (View view) {
        if (message.getText().toString().trim().length()<1){
            message.requestFocus();
            Snackbar.make(view, R.string.enter_message, Snackbar.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    private void submit(){
        String _phone_number = "+919431348343";
        String _message = name.getText().toString().trim()+"\n"+email_id.getText().toString().trim()+
                "\n"+message.getText().toString().trim();
        startActivity(
                new Intent(Intent.ACTION_VIEW,
                        Uri.parse(
                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s", _phone_number, _message)
                        )
                )
        );
    }
    /*......................................For BackPress.........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            showWallet();
        } else {
            finishAffinity();
        }
    }
}
