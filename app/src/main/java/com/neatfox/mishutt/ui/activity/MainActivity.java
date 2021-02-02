package com.neatfox.mishutt.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.adapter.NavListAdapter;
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder;
import com.omega_r.libs.omegaintentbuilder.handlers.FailCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static com.neatfox.mishutt.Constants.api_about_us;
import static com.neatfox.mishutt.Constants.api_profile_details;
import static com.neatfox.mishutt.Constants.api_terms_conditions;
import static com.neatfox.mishutt.Constants.basePath;
import static com.neatfox.mishutt.Constants.changeDateFormatUI;
import static com.neatfox.mishutt.Constants.version;
import static com.neatfox.mishutt.DescriptionStrings.aadhaar_card_description;
import static com.neatfox.mishutt.DescriptionStrings.bank_IFSC_code_description;
import static com.neatfox.mishutt.DescriptionStrings.cibil_description;
import static com.neatfox.mishutt.DescriptionStrings.driving_license_description;
import static com.neatfox.mishutt.DescriptionStrings.gas_connection_description;
import static com.neatfox.mishutt.DescriptionStrings.in_the_news_description;
import static com.neatfox.mishutt.DescriptionStrings.indian_holidays_description;
import static com.neatfox.mishutt.DescriptionStrings.pan_card_description;
import static com.neatfox.mishutt.DescriptionStrings.passport_description;
import static com.neatfox.mishutt.DescriptionStrings.privacy_policy;
import static com.neatfox.mishutt.DescriptionStrings.saving_schemes_description;
import static com.neatfox.mishutt.DescriptionStrings.visa_description;
import static com.neatfox.mishutt.DescriptionStrings.voter_id_description;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Calendar calendar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    CoordinatorLayout layout;
    LinearLayout layout_nav_header;
    TextView tv_toolbar,name,mobile_number,email_id;
    CircleImageView profile_image;
    MeowBottomNavigation bottomNavigation;
    ExpandableListView navListView;
    NavListAdapter navListAdapter;
    List<String> navListHeader;
    HashMap<String, List<String>> navListChild;
    Dialog dialog_theme,dialog_contact_us;
    ProgressDialog progressDialog;
    public static String emailPattern, _name = "",_mobile_number = "",_email_id = "",
            _date_of_birth = "",_address = "",_pin_code = "",_annual_income = "0",_aadhaar_number = "",_pan_no = "",
            _payment_flag = "",_onboarding_flag = "";
    int backPress = 0,bottom_navigation_id;

    public boolean isNetworkAvailable() {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreference = getApplicationContext().getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        calendar = Calendar.getInstance();
        emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        toolbar = findViewById(R.id.toolbar);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        setSupportActionBar(toolbar);
        layout = findViewById(R.id.layout_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        tv_toolbar.setText(R.string.mishutt_finances);
        /*.....................................Navigation Menu....................................*/
        View headView = navigationView.getHeaderView(0);
        layout_nav_header = headView.findViewById(R.id.layout_nav_header);
        name = headView.findViewById(R.id.tv_name);
        mobile_number = headView.findViewById(R.id.tv_mobile_number);
        email_id = headView.findViewById(R.id.tv_email_id);
        profile_image = headView.findViewById(R.id.iv_profile_image);
        name.setText(sharedPreference.getString("name", ""));
        mobile_number.setText(String.format("+91 %s", sharedPreference.getString("mobile_number", "")));
        email_id.setText(sharedPreference.getString("email_id", ""));
        String image_path = sharedPreference.getString("profile_picture", "");
        if (image_path.trim().length()<5){
            profile_image.setImageResource(R.drawable.ic_profile_image);
        } else {
            Glide.with(MainActivity.this)
                    .load(basePath+image_path)
                    .apply(new RequestOptions()
                            .override(720, 720)).into(profile_image);
        }
        layout_nav_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDashboard();
            }
        });
        /*.....................................Navigation List....................................*/
        navListView = findViewById(R.id.expandableListView);
        prepareListData();
        navListAdapter = new NavListAdapter(this, navListHeader, navListChild);
        navListView.setAdapter(navListAdapter);

        navListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                String groupHeader = (String) navListAdapter.getGroup(groupPosition);
                System.out.println(groupHeader);
                if ("Credit Score".equalsIgnoreCase(groupHeader)){
                    Intent intent = new Intent (MainActivity.this, CreditScoreActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    MainActivity.this.startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                } else if ("Privacy Policy".equalsIgnoreCase(groupHeader)){
                    Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("item_name",groupHeader);
                    intent.putExtra("item_description",privacy_policy);
                    MainActivity.this.startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                } else if ("Sign Out".equalsIgnoreCase(groupHeader)){
                    editor.clear();
                    editor.commit();
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

                } else if ("About Us".equalsIgnoreCase(groupHeader)){
                    Uri uri = Uri.parse(api_about_us);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                }  else if ("Terms & Conditions".equalsIgnoreCase(groupHeader)){
                    Uri uri = Uri.parse(api_terms_conditions);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                return false;
            }
        });

        navListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String child = (String) navListAdapter.getChild(groupPosition,childPosition);
                openNavItemPage(child);
                return false;
            }
        });

        if (isNetworkAvailable()) noNetwork();
        else getProfileDetails();

        setDialog_theme();     //set Theme option
        /*....................................Bottom Navigation...................................*/
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.rupee_bold));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_sign_up_user));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_home));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.wallet));
        bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.help));

        bottomNavigation.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                if (model.getId() == 1 && model.getId() != bottom_navigation_id)
                    showPayment();
                else if (model.getId() == 2 && model.getId() != bottom_navigation_id)
                    showDashboard();
                else if (model.getId() == 3 && model.getId() != bottom_navigation_id)
                    showHome();
                else if (model.getId() == 4 && model.getId() != bottom_navigation_id)
                    showWallet();
                else if (model.getId() == 5 && model.getId() != bottom_navigation_id)
                    showSupport();
                return null;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }
    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_theme){
            dialog_theme.show();

        } else if (id == R.id.action_settings) {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        } else if (id == R.id.action_contact_us) {
            contact_us();

        } else if (id == R.id.action_mobile_no) {
            startCall();

        } else if (id == R.id.action_mail) {
            OmegaIntentBuilder
                    .email()
                    .emailTo("info@mishutt.com")
                    .createIntentHandler(MainActivity.this)
                    .failCallback(new FailCallback() {
                        @Override
                        public void onActivityStartError(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "You don't have app for sending email", Toast.LENGTH_SHORT).show();
                        }
                    }).startActivity();

        } else if (id == R.id.action_sign_out) {
            editor.clear();
            editor.commit();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

        }
        return super.onOptionsItemSelected(item);
    }
    /*.......................................Navigation List......................................*/
    private void prepareListData() {
        navListHeader = new ArrayList<>();
        navListChild = new HashMap<>();

        // Adding header data
        navListHeader.add("Credit Score");
        navListHeader.add("Financial Tools");
        navListHeader.add("Cards");
        navListHeader.add("Loans");
        navListHeader.add("Insurances");
        navListHeader.add("Investments");
        navListHeader.add("Utility Documents");
        navListHeader.add("More");
        navListHeader.add("About Us");
        navListHeader.add("Terms & Conditions");
        navListHeader.add("Privacy Policy");
        navListHeader.add("Sign Out");
        navListHeader.add("Version : "+version);

        // Adding child data
        List<String> creditScore = new ArrayList<>();

        List<String> financialTools = new ArrayList<>();
        financialTools.add("EMI Calculator");
        financialTools.add("FD Calculator");

        List<String> cards = new ArrayList<>();
        cards.add("Debit Card");
        cards.add("Credit Card");

        List<String> loans = new ArrayList<>();
        loans.add("Home Loan");
        loans.add("Personal Loan");
        loans.add("Car Loan");
        loans.add("Two Wheeler Loan");
        loans.add("Commercial Vehicle Loan");
        loans.add("Short Term Loan");
        loans.add("Gold Loan");
        loans.add("Business Loan");
        loans.add("Start Up Funding");
        loans.add("Education Loan");
        loans.add("Education Loan Top Up");
        loans.add("Student Finance");

        List<String> insurance = new ArrayList<>();
        insurance.add("Term Insurance");
        insurance.add("Life Insurance");
        insurance.add("Commercial Vehicle Insurance");
        insurance.add("Corona Virus Health Insurance");
        insurance.add("Corporate Insurance");
        insurance.add("Critical Illness Insurance");
        insurance.add("Family Health Insurance Plan");
        insurance.add("General Insurance");
        insurance.add("Group Health Insurance");
        insurance.add("Group Insurance");
        insurance.add("Health Insurance");
        insurance.add("Home Insurance");
        insurance.add("Mediclaim");
        insurance.add("Money Back Policy");
        insurance.add("Pay as You Drive Insurance");
        insurance.add("Senior Citizens Health Insurance");
        insurance.add("Third Party Bike Insurance");
        insurance.add("Third Party Insurance");
        insurance.add("Travel Insurance");
        insurance.add("Two Wheeler Insurance");

        List<String> investment = new ArrayList<>();
        investment.add("Mutual Funds");
        investment.add("Guaranteed Income Plan");
        investment.add("Tax Savings Investments");
        investment.add("ULIP");
        investment.add("Fixed Deposit");
        investment.add("Recurring Deposit");
        investment.add("Pension Plan");

        List<String> utilityDocuments = new ArrayList<>();
        utilityDocuments.add("PAN Card");
        utilityDocuments.add("Aadhaar Card");
        utilityDocuments.add("Voter ID");
        utilityDocuments.add("Driving License");
        utilityDocuments.add("Passport");
        utilityDocuments.add("Visa");
        utilityDocuments.add("Gas Connection");

        List<String> more = new ArrayList<>();
        more.add("CIBIL");
        more.add("Tax");
        more.add("Bank IFSC Code");
        more.add("Indian Holidays");
        more.add("EMI Calculator");
        more.add("Saving Schemes");
        more.add("In The News");

        List<String> about = new ArrayList<>();

        List<String> terms = new ArrayList<>();

        List<String> privacyPolicy = new ArrayList<>();

        List<String> signOut = new ArrayList<>();

        List<String> version = new ArrayList<>();

        // Header, Child data
        navListChild.put(navListHeader.get(0), creditScore);
        navListChild.put(navListHeader.get(1), financialTools);
        navListChild.put(navListHeader.get(2), cards);
        navListChild.put(navListHeader.get(3), loans);
        navListChild.put(navListHeader.get(4), insurance);
        navListChild.put(navListHeader.get(5), investment);
        navListChild.put(navListHeader.get(6), utilityDocuments);
        navListChild.put(navListHeader.get(7), more);
        navListChild.put(navListHeader.get(8), about);
        navListChild.put(navListHeader.get(9), terms);
        navListChild.put(navListHeader.get(10), privacyPolicy);
        navListChild.put(navListHeader.get(11), signOut);
        navListChild.put(navListHeader.get(12), version);
    }

    public void openNavItemPage(String item_name){
        if ("EMI Calculator".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, EMICalculatorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            MainActivity.this.startActivity(intent);

        } else if ("FD Calculator".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, FDCalculatorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            MainActivity.this.startActivity(intent);

        } else if ("Start Up Funding".equalsIgnoreCase(item_name)) {
            Intent intent = new Intent(MainActivity.this, FundManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            MainActivity.this.startActivity(intent);

        } else if (item_name.contains("Loan") || "Student Finance".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, CheckLoanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            MainActivity.this.startActivity(intent);

        } else if (item_name.contains("Insurance") || "Mediclaim".equalsIgnoreCase(item_name) ||
                "Money Back Policy".equalsIgnoreCase(item_name)){

            Intent intent = new Intent (MainActivity.this, CheckInsuranceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            MainActivity.this.startActivity(intent);

        } else if ("Mutual Funds".equalsIgnoreCase(item_name) ||
                "Guaranteed Income Plan".equalsIgnoreCase(item_name) ||
                "Tax Savings Investments".equalsIgnoreCase(item_name) ||
                "ULIP".equalsIgnoreCase(item_name) || "Fixed Deposit".equalsIgnoreCase(item_name) ||
                "Recurring Deposit".equalsIgnoreCase(item_name) || "Pension Plan".equalsIgnoreCase(item_name)){

            Intent intent = new Intent (MainActivity.this, CheckInvestmentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            MainActivity.this.startActivity(intent);

        } else if ("Debit Card".equalsIgnoreCase(item_name) || "Credit Card".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, CardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            MainActivity.this.startActivity(intent);

        } else if ("Voter ID".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",voter_id_description);
            MainActivity.this.startActivity(intent);

        } else if ("PAN Card".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",pan_card_description);
            MainActivity.this.startActivity(intent);

        } else if ("Aadhaar Card".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",aadhaar_card_description);
            MainActivity.this.startActivity(intent);

        } else if ("Driving License".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",driving_license_description);
            MainActivity.this.startActivity(intent);

        } else if ("Passport".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",passport_description);
            MainActivity.this.startActivity(intent);

        } else if ("Visa".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",visa_description);
            MainActivity.this.startActivity(intent);

        } else if ("Gas Connection".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",gas_connection_description);
            MainActivity.this.startActivity(intent);

        } else if ("CIBIL".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",cibil_description);
            MainActivity.this.startActivity(intent);

        } else if ("Tax".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, TaxActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            MainActivity.this.startActivity(intent);

        } else if ("Bank IFSC Code".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",bank_IFSC_code_description);
            MainActivity.this.startActivity(intent);

        } else if ("Indian Holidays".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",indian_holidays_description);
            MainActivity.this.startActivity(intent);

        } else if ("Saving Schemes".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",saving_schemes_description);
            MainActivity.this.startActivity(intent);

        } else if ("In The News".equalsIgnoreCase(item_name)){
            Intent intent = new Intent (MainActivity.this, DescriptionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("item_name",item_name);
            intent.putExtra("item_description",in_the_news_description);
            MainActivity.this.startActivity(intent);
        }
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
    /*...........................................Theme............................................*/
    public void setDialog_theme () {
        dialog_theme = new Dialog(MainActivity.this);
        dialog_theme.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_theme.setContentView(R.layout.dialog_theme);
        dialog_theme.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog_theme.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog_theme.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog_theme.getWindow().setAttributes(windowParams);

        /*RadioButton action_light_theme, action_dark_theme, action_system_default;
        action_light_theme = dialog_theme.findViewById(R.id.radio_light_mode);
        action_dark_theme = dialog_theme.findViewById(R.id.radio_dark_mode);
        action_system_default = dialog_theme.findViewById(R.id.radio_system_default);*/

        TextView action_cancel = dialog_theme.findViewById(R.id.tv_action_cancel);
        action_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_theme.dismiss();
            }
        });
    }
    /*.........................................Contact Us.........................................*/
    public void contact_us(){
        dialog_contact_us = new Dialog(MainActivity.this);
        dialog_contact_us.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_contact_us.setContentView(R.layout.dialog_contact_us);
        dialog_contact_us.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog_contact_us.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog_contact_us.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog_contact_us.getWindow().setAttributes(windowParams);
        dialog_contact_us.show();

        LinearLayout layout_service_mobile = dialog_contact_us.findViewById(R.id.layout_service_mobile);
        LinearLayout layout_service_email = dialog_contact_us.findViewById(R.id.layout_service_email);

        layout_service_mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCall();
            }
        });

        layout_service_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OmegaIntentBuilder
                        .email()
                        .emailTo("info@mishutt.com")
                        .createIntentHandler(MainActivity.this)
                        .failCallback(new FailCallback() {
                            @Override
                            public void onActivityStartError(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "You don't have app for sending email", Toast.LENGTH_SHORT).show();
                            }
                        }).startActivity();
            }
        });

        TextView action_cancel = dialog_contact_us.findViewById(R.id.tv_action_cancel);
        action_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_contact_us.dismiss();
            }
        });
    }
    /*..........................................Payment...........................................*/
    public void showPayment () {
        startActivity(new Intent(MainActivity.this, PaymentActivity.class));
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
    /*.........................................Dashboard..........................................*/
    public void showDashboard () {
        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        if (bottom_navigation_id == 1)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        else
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
    /*............................................Home............................................*/
    public void showHome () {
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        if (bottom_navigation_id == 1 || bottom_navigation_id == 2)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        else
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
    /*...........................................Wallet...........................................*/
    public void showWallet () {
        startActivity(new Intent(MainActivity.this, WalletActivity.class));
        if (bottom_navigation_id == 5)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        else
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
    /*..........................................Support...........................................*/
    public void showSupport () {
        startActivity(new Intent(MainActivity.this, SupportActivity.class));
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
    /*............................................Call............................................*/
    private void startCall(){
        OmegaIntentBuilder
                .call("+91 94313 48343")
                .createIntentHandler(MainActivity.this)
                .failCallback(new FailCallback() {
                    @Override
                    public void onActivityStartError(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "You don't have app for calling", Toast.LENGTH_SHORT).show();
                    }
                }).startActivity();
    }
    /*.....................................Profile Details........................................*/
    private void getProfileDetails(){
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
                if (status == 1) {
                    try {
                        JSONObject jsonObject = resObj.getJSONObject("user_dtl");
                        _name = jsonObject.getString("name");
                        _mobile_number = jsonObject.getString("phone_no");
                        _email_id = jsonObject.getString("emailid");

                        if (jsonObject.getString("address").length() <1)
                            _address = "";
                        else
                            _address = jsonObject.getString("address");

                        if (jsonObject.getString("zip_code").length() <1)
                            _pin_code = "";
                        else
                            _pin_code = jsonObject.getString("zip_code");

                        if (jsonObject.getString("dob").length() <= 5)
                            _date_of_birth = "";
                        else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("dob")))
                            _date_of_birth = "";
                        else
                            _date_of_birth = changeDateFormatUI(jsonObject.getString("dob"));

                        if (jsonObject.getString("montly_earning").length() < 1)
                            _annual_income = "0";
                        else
                            _annual_income = jsonObject.getString("montly_earning");

                        if (jsonObject.getString("aadhar_no").length() <1)
                            _aadhaar_number = "";
                        else
                            _aadhaar_number = jsonObject.getString("aadhar_no");

                        if (jsonObject.getString("pan_no").length() <1)
                            _pan_no = "";
                        else
                            _pan_no = jsonObject.getString("pan_no");

                        _payment_flag = jsonObject.getString("payment_flag");
                        _onboarding_flag = jsonObject.getString("onboarding_flag");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    snackBarError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
    /*.......................................For BackPress........................................*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            Snackbar.make(layout, R.string.press_again_to_exit, Snackbar.LENGTH_SHORT).show();
        } else {
            finishAffinity();
        }
    }
}
