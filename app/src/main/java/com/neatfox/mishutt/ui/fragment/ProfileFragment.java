package com.neatfox.mishutt.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.neatfox.mishutt.R;
import com.neatfox.mishutt.image_cropper.ImageCropper;
import com.neatfox.mishutt.singleton.Singleton;
import com.neatfox.mishutt.ui.activity.AddProfileActivity;
import com.neatfox.mishutt.ui.activity.ShowImageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.neatfox.mishutt.Constants.REQUEST_IMAGE;
import static com.neatfox.mishutt.Constants.REQUEST_STORAGE_PERMISSION;
import static com.neatfox.mishutt.Constants.api_profile_details;
import static com.neatfox.mishutt.Constants.api_profile_picture;
import static com.neatfox.mishutt.Constants.basePath;
import static com.neatfox.mishutt.Constants.changeDateFormatUI;
import static com.neatfox.mishutt.Constants.getBase64ImageStringFromBitmap;
import static com.neatfox.mishutt.ui.activity.MainActivity._pin_code;

public class ProfileFragment extends Fragment {

    NetworkInfo networkInfo;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    Activity activity;
    Context context;
    CoordinatorLayout layout;
    LinearLayout layout_profile_info,layout_email_id,layout_address,layout_date_of_birth,
            layout_spouse_name,layout_spouse_date_of_birth,layout_wedding_anniversary,
            layout_number_of_children,layout_profession,layout_annual_income,layout_number_of_dependents,
            layout_aadhaar_number, layout_pan_no;
    TextView name,mobile_number,email_id,address,date_of_birth,spouse_name,spouse_date_of_birth,
            wedding_anniversary,number_of_children,profession,annual_income,number_of_dependents,
            aadhaar_number,pan_no;
    ShimmerFrameLayout mShimmerViewContainer;
    CircleImageView profile_image;
    Uri filePath;
    Bitmap bitmap;
    Dialog dialog_image_picker;
    ProgressBar loading;
    String _filePath;

    public void noNetwork() {
        Snackbar.make(layout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarError() {
        Snackbar.make(layout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        activity = getActivity();
        context = getContext();

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();

        sharedPreference = activity.getSharedPreferences("user_pref", 0);
        editor = sharedPreference.edit();
        editor.apply();

        layout = view.findViewById(R.id.layout);
        layout_profile_info = view.findViewById(R.id.layout_profile_info);
        layout_email_id = view.findViewById(R.id.layout_email_id);
        layout_address = view.findViewById(R.id.layout_address);
        layout_date_of_birth = view.findViewById(R.id.layout_date_of_birth);
        layout_spouse_name = view.findViewById(R.id.layout_spouse_name);
        layout_spouse_date_of_birth = view.findViewById(R.id.layout_spouse_date_of_birth);
        layout_wedding_anniversary = view.findViewById(R.id.layout_wedding_anniversary);
        layout_number_of_children = view.findViewById(R.id.layout_number_of_children);
        layout_profession = view.findViewById(R.id.layout_profession);
        layout_annual_income = view.findViewById(R.id.layout_annual_income);
        layout_number_of_dependents = view.findViewById(R.id.layout_number_of_dependents);
        layout_aadhaar_number = view.findViewById(R.id.layout_aadhaar_number);
        layout_pan_no = view.findViewById(R.id.layout_pan_no);

        name = view.findViewById(R.id.tv_name);
        mobile_number = view.findViewById(R.id.tv_mobile_number);
        email_id = view.findViewById(R.id.tv_email_id);
        address = view.findViewById(R.id.tv_address);
        date_of_birth = view.findViewById(R.id.tv_date_of_birth);
        spouse_name = view.findViewById(R.id.tv_spouse_name);
        spouse_date_of_birth = view.findViewById(R.id.tv_spouse_date_of_birth);
        wedding_anniversary = view.findViewById(R.id.tv_wedding_anniversary);
        number_of_children = view.findViewById(R.id.tv_number_of_children);
        profession = view.findViewById(R.id.tv_profession);
        annual_income = view.findViewById(R.id.tv_annual_income);
        number_of_dependents = view.findViewById(R.id.tv_number_of_dependents);
        aadhaar_number = view.findViewById(R.id.tv_aadhaar_number);
        pan_no = view.findViewById(R.id.tv_pan_no);

        profile_image = view.findViewById(R.id.iv_profile_image);
        FloatingActionButton fab_camera = view.findViewById(R.id.fab_camera);
        TextView edit_profile = view.findViewById(R.id.tv_edit_profile);
        loading = view.findViewById(R.id.loading);

        mShimmerViewContainer = view.findViewById(R.id.shimmer_view);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        layout_profile_info.setVisibility(View.GONE);

        name.setText(sharedPreference.getString("name", ""));
        mobile_number.setText(String.format("+91 %s", sharedPreference.getString("mobile_number", "")));
        email_id.setText(sharedPreference.getString("email_id", ""));
        String image_path = sharedPreference.getString("profile_picture", "");
        if (image_path.trim().length()<5){
            profile_image.setImageResource(R.drawable.ic_profile_image);
        } else {
            Glide.with(context)
                    .load(basePath+image_path)
                    .apply(new RequestOptions()
                            .override(720, 720)).into(profile_image);
        }

        if (networkInfo != null && networkInfo.isConnected())
            setProfileDetails();
        else
            noNetwork();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowImageActivity.class);
                intent.putExtra("type","Profile");
                intent.putExtra("title","Profile Image");
                intent.putExtra("image",sharedPreference.getString("profile_picture", ""));
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions( activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    getImageDialog();
                }
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, AddProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });

        return view;
    }
    /*........................................Select Image........................................*/
    private void getImageDialog () {
        dialog_image_picker = new Dialog(context);
        dialog_image_picker.setContentView(R.layout.dialog_image_picker);
        dialog_image_picker.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog_image_picker.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(dialog_image_picker.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog_image_picker.getWindow().setAttributes(windowParams);
        dialog_image_picker.show();

        ImageView cancel = dialog_image_picker.findViewById(R.id.cancel_action);
        TextView title = dialog_image_picker.findViewById(R.id.tv_title);
        LinearLayout layout_camera = dialog_image_picker.findViewById(R.id.layout_camera);
        LinearLayout layout_gallery = dialog_image_picker.findViewById(R.id.layout_gallery);

        title.setText(R.string.set_profile_picture);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_image_picker.dismiss();
            }
        });

        layout_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageCropper.class);
                intent.putExtra(ImageCropper.INTENT_IMAGE_PICKER_OPTION, ImageCropper.REQUEST_IMAGE_CAPTURE);

                // setting aspect ratio
                intent.putExtra(ImageCropper.INTENT_LOCK_ASPECT_RATIO, true);
                intent.putExtra(ImageCropper.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
                intent.putExtra(ImageCropper.INTENT_ASPECT_RATIO_Y, 1);

                // setting maximum bitmap width and height
                intent.putExtra(ImageCropper.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
                intent.putExtra(ImageCropper.INTENT_BITMAP_MAX_WIDTH, 1080);
                intent.putExtra(ImageCropper.INTENT_BITMAP_MAX_HEIGHT, 1080);

                startActivityForResult(intent, REQUEST_IMAGE);
                dialog_image_picker.dismiss();
            }
        });

        layout_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageCropper.class);
                intent.putExtra(ImageCropper.INTENT_IMAGE_PICKER_OPTION, ImageCropper.REQUEST_GALLERY_IMAGE);

                // setting aspect ratio
                intent.putExtra(ImageCropper.INTENT_LOCK_ASPECT_RATIO, true);
                intent.putExtra(ImageCropper.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
                intent.putExtra(ImageCropper.INTENT_ASPECT_RATIO_Y, 1);
                startActivityForResult(intent, REQUEST_IMAGE);
                dialog_image_picker.dismiss();
            }
        });
    }
    /*.....................................Storage Permission.....................................*/
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageDialog();
            }
        }
    }
    /*........................................Upload Image........................................*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                filePath = data.getParcelableExtra("path");
                System.out.println(filePath);
                _filePath = filePath.toString();
                _filePath = _filePath.replace("file://", "");
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    bitmap = Bitmap.createScaledBitmap(bitmap,1080,1080,false);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                    System.out.println(_filePath);
                    System.out.println(bitmap);
                    loading.setVisibility(View.VISIBLE);
                    if (bitmap == null) {
                        Toast.makeText(context, "Select File", Toast.LENGTH_LONG).show();
                        loading.setVisibility(View.GONE);
                    } else {
                        if (networkInfo != null && networkInfo.isConnected())
                            uploadProfileImage();
                        else
                            noNetwork();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*......................................Profile Details.......................................*/
    private void setProfileDetails(){
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
                        name.setText(jsonObject.getString("name"));
                        email_id.setText(jsonObject.getString("emailid"));
                        mobile_number.setText(String.format("+91 %s", jsonObject.getString("phone_no")));

                        if (jsonObject.getString("address").length() <1){
                            address.setText("");
                            layout_address.setVisibility(View.GONE);
                        } else {
                            address.setText(String.format("%s %s", jsonObject.getString("address"), _pin_code));
                            layout_address.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("dob").length() <= 5){
                            date_of_birth.setText("");
                            layout_date_of_birth.setVisibility(View.GONE);
                        } else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("dob"))){
                            date_of_birth.setText("");
                            layout_date_of_birth.setVisibility(View.GONE);
                        } else {
                            date_of_birth.setText(changeDateFormatUI(jsonObject.getString("dob")));
                            layout_date_of_birth.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("name_of_spouse").length() <1){
                            spouse_name.setText("");
                            layout_spouse_name.setVisibility(View.GONE);
                        } else {
                            spouse_name.setText(jsonObject.getString("name_of_spouse"));
                            layout_spouse_name.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("spouse_dob").length() <= 5){
                            spouse_date_of_birth.setText("");
                            layout_spouse_date_of_birth.setVisibility(View.GONE);
                        } else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("spouse_dob"))){
                            spouse_date_of_birth.setText("");
                            layout_spouse_date_of_birth.setVisibility(View.GONE);
                        } else {
                            spouse_date_of_birth.setText(changeDateFormatUI(jsonObject.getString("spouse_dob")));
                            layout_spouse_date_of_birth.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("marriage_anniversary").length() <= 5){
                            wedding_anniversary.setText("");
                            layout_wedding_anniversary.setVisibility(View.GONE);
                        } else if ("0000-00-00".equalsIgnoreCase(jsonObject.getString("marriage_anniversary"))){
                            wedding_anniversary.setText("");
                            layout_wedding_anniversary.setVisibility(View.GONE);
                        } else {
                            wedding_anniversary.setText(changeDateFormatUI(jsonObject.getString("marriage_anniversary")));
                            layout_wedding_anniversary.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("no_of_children").length() <1){
                            number_of_children.setText("");
                            layout_number_of_children.setVisibility(View.GONE);
                        } else {
                            number_of_children.setText(jsonObject.getString("no_of_children"));
                            layout_number_of_children.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("profession").length() <1){
                            profession.setText("");
                            layout_profession.setVisibility(View.GONE);
                        } else {
                            profession.setText(jsonObject.getString("profession"));
                            layout_profession.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("montly_earning").length() <1){
                            annual_income.setText("");
                            layout_annual_income.setVisibility(View.GONE);
                        } else {
                            annual_income.setText(jsonObject.getString("montly_earning"));
                            layout_annual_income.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("no_of_dependents_in_family").length() <1){
                            number_of_dependents.setText("");
                            layout_number_of_dependents.setVisibility(View.GONE);
                        } else {
                            number_of_dependents.setText(jsonObject.getString("no_of_dependents_in_family"));
                            layout_number_of_dependents.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("aadhar_no").length() <1){
                            aadhaar_number.setText("");
                            layout_aadhaar_number.setVisibility(View.GONE);
                        } else {
                            aadhaar_number.setText(jsonObject.getString("aadhar_no"));
                            layout_aadhaar_number.setVisibility(View.VISIBLE);
                        }

                        if (jsonObject.getString("pan_no").length() <1){
                            pan_no.setText("");
                            layout_pan_no.setVisibility(View.GONE);
                        } else {
                            pan_no.setText(jsonObject.getString("pan_no"));
                            layout_pan_no.setVisibility(View.VISIBLE);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run(){
                                layout_profile_info.setVisibility(View.VISIBLE);
                                mShimmerViewContainer.setVisibility(View.GONE);
                            }
                        },1000);
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
        Singleton.getInstance(context).addToRequestQueue(request);
    }
    /*....................................Update Profile Image....................................*/
    private void uploadProfileImage(){
        StringRequest request = new StringRequest(Request.Method.POST, api_profile_picture, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Profile Image>>>", "onResponse::::: " + response);
                JSONObject resObj = null;
                int status = 0;
                try {
                    resObj = new JSONObject(response);
                    status = resObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loading.setVisibility(View.GONE);
                if (status == 1) {
                    try{
                        profile_image.setImageBitmap(bitmap);
                        editor.putString("profile_picture", resObj.getString("picture"));
                        editor.commit();
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
                loading.setVisibility(View.GONE);
                snackBarError();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uid", sharedPreference.getString("register_id", ""));
                params.put("picture", getBase64ImageStringFromBitmap(bitmap));
                return params;
            }
        };
        int socketTimeout = 5000; //5 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Singleton.getInstance(context).addToRequestQueue(request);
    }
}
