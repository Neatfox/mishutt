package com.neatfox.mishutt;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {

    public static String basePath = "https://mishutt.com/";
    public static String middlePath = "api/";
    public static String imageMiddlePath = "assets/admin/uploads/";
    public static String billDeskBasePath = "https://billdesks.in/";

    public static String version = BuildConfig.VERSION_NAME;

    public static String UPI_ID = "ipay.137991@icici";

    public static final int REQUEST_IMAGE = 10;
    public static final int REQUEST_CONTACT = 20;
    public static final int REQUEST_FILE = 30;
    public static final int REQUEST_CALL_PERMISSION = 200;
    public static final int REQUEST_CAMERA_PERMISSION = 300;
    public static final int REQUEST_GALLERY_PERMISSION = 400;
    public static final int REQUEST_CONTACT_PERMISSION = 500;
    public static final int REQUEST_LOCATION_SETTINGS = 600;
    public static final int REQUEST_LOCATION_PERMISSION = 700;
    public static final int REQUEST_SMS_PERMISSION = 800;
    public static final int REQUEST_STORAGE_PERMISSION = 900;

    /*------------------------------------------Sign In-------------------------------------------*/
    public static String api_sign_in = basePath + middlePath + "dologin";
    public static String api_forgot_password = basePath + middlePath + "recover_password";
    /*------------------------------------------Sign Up-------------------------------------------*/
    public static String api_sign_up = basePath + middlePath + "register";
    /*-----------------------------------------Verify OTP-----------------------------------------*/
    public static String api_verify_otp = basePath + middlePath + "authenticate";
    public static String api_verify_login_otp = basePath + middlePath + "otp_olgin";
    /*-----------------------------------------Resend OTP-----------------------------------------*/
    public static String api_resend_otp = basePath + middlePath + "resend";
    /*--------------------------------------------Home--------------------------------------------*/
    public static String api_banner = basePath + middlePath + "sliderslist";
    public static String api_ad = basePath + middlePath + "adslist";
    /*------------------------------------------Profile-------------------------------------------*/
    public static String api_profile_details = basePath + middlePath + "userdtl";
    public static String api_profile_edit = basePath + middlePath + "updateuserData";
    public static String api_add_dependent = basePath + middlePath + "update_dependent";
    public static String api_profile_picture = basePath + middlePath + "updateuserphoto";
    public static String api_aadhaar_verification = basePath + middlePath + "verify_adhaar";
    public static String api_pan_verification = basePath + middlePath + "verify_pan";
    /*------------------------------------------Product-------------------------------------------*/
    public static String api_product_category = basePath + middlePath + "productcategorylist";
    public static String api_product_subcategory = basePath + middlePath + "productsubcategorylist";
    public static String api_product_add = basePath + middlePath + "addproduct";
    public static String api_product_list = basePath + middlePath + "products_list";
    public static String api_product_delete = basePath + middlePath + "deleteproduct";
    /*-----------------------------------------Portfolio------------------------------------------*/
    public static String api_portfolio_add = basePath + middlePath + "addportfolio";
    public static String api_portfolio_list = basePath + middlePath + "user_portfolio_list";
    /*-------------------------------------------Cards--------------------------------------------*/
    public static String api_debit_card_list = basePath + middlePath + "debitcardlist";
    public static String api_credit_card_list = basePath + middlePath + "creditcardlist";
    /*---------------------------------------Expense Manager--------------------------------------*/
    public static String api_transaction_category_interval = basePath + middlePath + "expenseintervallist";
    public static String api_transaction_category_add = basePath + middlePath + "expencecategoryadd";
    public static String api_transaction_category_list = basePath + middlePath + "expencecategorylist";
    public static String api_transaction_category_delete = basePath + middlePath + "delete_expencecategory";
    public static String api_transaction_type = basePath + middlePath + "entry_select_amount_type";
    public static String api_transaction_add = basePath + middlePath + "saveentry";
    public static String api_transaction_edit = basePath + middlePath + "saveenedit";
    public static String api_transaction_msg_date_time_add = basePath + middlePath + "txn_msg_date_time";
    public static String api_transaction_list = basePath + middlePath + "book_of_account_list";
    public static String api_transaction_delete = basePath + middlePath + "delete_book_of_account";
    public static String api_transaction_earning_spending_by_date = basePath + middlePath + "getdata";
    public static String api_transaction_earning_spending = basePath + middlePath + "getinitial";
    public static String api_transaction_earning_category_wise = basePath + middlePath + "categorywiseearning";
    public static String api_transaction_spending_category_wise = basePath + middlePath + "categorywiseexpense";
    /*----------------------------------------Goal Manager----------------------------------------*/
    public static String api_goal_list = basePath + middlePath + "MyGoal";
    public static String api_goal_delete = basePath + middlePath + "deletegoal";
    public static String api_goal_details = basePath + middlePath + "getgoal";
    public static String api_goal_loans = basePath + middlePath + "getLoans";
    public static String api_goal_investments = basePath + middlePath + "getInvestments";
    public static String api_goal_save = basePath + middlePath + "savegoal";
    /*----------------------------------------Loan Manager----------------------------------------*/
    public static String api_loan_manager_list = basePath + middlePath + "loanmanagerlist";
    /*-------------------------------------Investment Manager-------------------------------------*/
    public static String api_investment_manager_list = basePath + middlePath + "investmentmanagerlist";
    /*----------------------------------------Fund Manager----------------------------------------*/
    public static String api_store_fund = basePath + middlePath + "storefundrequest";
    /*----------------------------------------Store CIBIL-----------------------------------------*/
    public static String api_store_cibil = basePath + middlePath + "storecibil";
    /*------------------------------------=----Check Loan-----------------------------------------*/
    public static String api_loans_list = basePath + middlePath + "specificLoan";
    public static String api_loan_wishlist = basePath + middlePath + "sotrewishloan";
    public static String api_loan_apply = basePath + middlePath + "loanleadapply";
    /*--------------------------------------Check Insurance---------------------------------------*/
    public static String api_insurances_list = basePath + middlePath + "specificInsurance";
    public static String api_insurance_wishlist = basePath + middlePath + "sotrewishins";
    public static String api_insurance_apply = basePath + middlePath + "insleadapply";
    /*--------------------------------------Check Investment--------------------------------------*/
    public static String api_investments_list = basePath + middlePath + "specificInvestment";
    public static String api_investment_wishlist = basePath + middlePath + "sotrewishinv";
    public static String api_investment_apply = basePath + middlePath + "invleadapply";
    /*---------------------------------------Bill Payments----------------------------------------*/
    public static String api_user_add = billDeskBasePath + middlePath + "mishutt_user_add";
    public static String api_add_payment_flag = basePath + middlePath + "update_payment_flag";
    public static String api_onboarding_otp_send = billDeskBasePath + middlePath + "send_onboarding_otp";
    public static String api_onboarding_otp_validate = billDeskBasePath + middlePath + "onboarding_registration";
    public static String api_add_onboarding_flag = basePath + middlePath + "update_onboarding_flag";
    public static String api_service_type = billDeskBasePath + middlePath + "servicelist_typewise_product_code";
    //public static String api_get_web_view = billDeskBasePath + middlePath + "prime_send";
    /*-------------------------------------------Wallet-------------------------------------------*/
    public static String api_wallet_balance = billDeskBasePath + middlePath + "wallet";
    public static String api_wallet_transaction_list = billDeskBasePath + middlePath + "transaction_list";
    public static String api_wallet_add_money = billDeskBasePath + middlePath + "mishutt_instantpay_payment";
    /*--------------------------------------------ITR---------------------------------------------*/
    public static String api_itr = basePath + middlePath + "sendleads";
    /*-------------------------------------------About--------------------------------------------*/
    public static String api_about_us = basePath + "Home/About";
    /*-------------------------------------------Terms--------------------------------------------*/
    public static String api_terms_conditions = basePath + "Home/Terms";
    /*-------------------------------------Change Date Format-------------------------------------*/
    public static String changeDateFormatUI(String Date){
        if (Date.length() == 10){
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
            java.util.Date date = null;
            try {
                date = inputFormat.parse(Date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert date != null;
            return outputFormat.format(date);
        }  else
            return "";
    }

    public static String changeDateFormatDB(String Date){
        if (Date.length() == 10){
            DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
            java.util.Date date = null;
            try {
                date = inputFormat.parse(Date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert date != null;
            return outputFormat.format(date);
        } else
            return "";
    }
    /*..........................................Add Comma.........................................*/
    public static String addCommaString(String s){
        double amount = Double.parseDouble(s);
        DecimalFormat formatter;
        if (s.contains(".")){
            formatter = new DecimalFormat("#,###.00");
        } else {
            formatter = new DecimalFormat("#,###");
        }
        return formatter.format(amount);
    }

    public static String addCommaDouble(double amount){
        String string = String.valueOf(amount);
        DecimalFormat formatter;
        if (string.contains(".")){
            formatter = new DecimalFormat("#,###.00");
        } else {
            formatter = new DecimalFormat("#,###");
        }
        return formatter.format(amount);
    }
    /*...............................Base64 Image String From Bitmap..............................*/
    public static String getBase64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }
}
