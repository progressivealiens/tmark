package com.trackkers.tmark.views.activity.operations;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.CustomTypefaceSpan;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PictureCapturingListener;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.services.APictureCapturingService;
import com.trackkers.tmark.services.LiveTrackingForOperations;
import com.trackkers.tmark.services.PictureCapturingServiceImpl;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.ProfileActivity;
import com.trackkers.tmark.views.activity.ResetPassword;
import com.trackkers.tmark.views.activity.fieldofficer.FOMarkAttendance;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperationsMainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PictureCapturingListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tv_name)
    MyTextview tvName;
    @BindView(R.id.tv_view_notice)
    MyTextview tvViewNotice;
    @BindView(R.id.tv_company_name)
    MyTextview tvCompanyName;
    @BindView(R.id.tv_emp_id)
    MyTextview tvEmpId;
    @BindView(R.id.btn_checkin)
    Button btnCheckin;
    @BindView(R.id.tv_emp_type)
    MyTextview tvEmpType;
    @BindView(R.id.et_checkin_msg)
    TextInputEditText etCheckinMsg;
    @BindView(R.id.ti_checkin_msg)
    TextInputLayout tiCheckinMsg;
    @BindView(R.id.et_comment)
    TextInputEditText etComment;
    @BindView(R.id.btn_submit_comments)
    Button btnSubmitComments;
    @BindView(R.id.tv_attach_image)
    ImageView tvAttachImage;
    @BindView(R.id.card_view_comments)
    LinearLayout cardViewComments;
    @BindView(R.id.iv_company_logo)
    RoundedImageView ivCompanyLogo;
    @BindView(R.id.switch_language)
    SwitchCompat switchLanguage;
    @BindView(R.id.fab_scan)
    FloatingActionButton fabScan;
    @BindView(R.id.tv_last_checkin)
    MyTextview tvLastCheckin;
    @BindView(R.id.tv_total_qr_scan)
    MyTextview tvTotalQrScan;
    @BindView(R.id.tv_sos)
    MyTextview tvSos;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    String checkinMessage = "", commentText = "", commentType = "";
    String notice = "", currentDateAndTime = "";
    int totalScannedQr = 0;
    double currentLatitude, currentLongitude;
    File file, commentFile, spyFile;
    public boolean isConveyanceAsked = false, isLiveTrackingEnabled = false, isSpyImageTaken = false, isSpyImageFound = false;
    Dialog dialog;

    MyTextview companyName, employeeType;
    ImageView drawerProfile;
    String deviceName = "";

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LocationCallback locationCallback;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9009;
    private static final int PERMISSIONS_REQUEST_CODE = 666;
    private static final int REQUEST_CODE_FOR_BACK_CAMERA = 200;
    private static final int REQUEST_CODE_FOR_FRONT_CAMERA = 215;
    public static final int NOTIFICATION_CHANNEL_ID = 151;

    MultipartBody.Part filePart = null;
    Intent stopServiceIntent;
    String languageToLoad = "";
    String pictureFilePathCheckin = "", pictureFilePathComment = "";

    IntentIntegrator qrScanOperations;
    String scannedCheckpointId = "", scannedCheckpointType = "";


    private APictureCapturingService pictureService;

    @Override
    protected void onStart() {
        super.onStart();
        if (!startRequestPermission()) {
            startRequestPermission();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operations_main);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        Menu m = navView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            applyFontToMenuItem(mi);
        }

        connectApiToFetchProfileDetails();
    }

    private void initialize() {
        progressView = new ProgressView(OperationsMainActivity.this);
        setSupportActionBar(toolbar);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.home_caps));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);
        View headerView = navView.getHeaderView(0);
        companyName = headerView.findViewById(R.id.drawer_company_name);
        employeeType = headerView.findViewById(R.id.drawer_employee_type);
        drawerProfile = headerView.findViewById(R.id.drawer_profile);

        deviceName = Build.MANUFACTURER;

        companyName.setText(PrefData.readStringPref(PrefData.company_name));
        employeeType.setText(PrefData.readStringPref(PrefData.employee_type));
        Picasso.get().load(Utils.BASE_IMAGE_COMPANY + PrefData.readStringPref(PrefData.company_logo)).placeholder(R.drawable.progress_animation).into(drawerProfile);
        Picasso.get().load(Utils.BASE_IMAGE_COMPANY + PrefData.readStringPref(PrefData.company_logo)).placeholder(R.drawable.progress_animation).into(ivCompanyLogo);

        if (PrefData.readStringPref(PrefData.last_checkin_operations).equalsIgnoreCase("")) {
            tvLastCheckin.setText("Last Check-In:\n" + "0");
        } else {
            tvLastCheckin.setText("Last Check-In:\n" + PrefData.readStringPref(PrefData.last_checkin_operations));
        }

        if (PrefData.readStringPref(PrefData.total_scan_count_operations).equalsIgnoreCase("")) {
            tvTotalQrScan.setText("Total Scan:\n" + "0");
        } else {
            tvTotalQrScan.setText("Total Scan:\n" + PrefData.readStringPref(PrefData.total_scan_count_operations));
        }

        prefData = new PrefData(OperationsMainActivity.this);
        apiInterface = ApiClient.getClient(OperationsMainActivity.this).create(ApiInterface.class);
        qrScanOperations = new IntentIntegrator(OperationsMainActivity.this);

        btnCheckin.setOnClickListener(this);
        btnSubmitComments.setOnClickListener(this);
        tvAttachImage.setOnClickListener(this);
        fabScan.setOnClickListener(this);
        tvViewNotice.setOnClickListener(this);
        tvSos.setOnClickListener(this);
        tvViewNotice.setSelected(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pictureService = PictureCapturingServiceImpl.getInstance(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("locationWrapper", String.valueOf(locationResult.getLastLocation()));
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        if (PrefData.readStringPref(PrefData.PREF_selected_language).equalsIgnoreCase("hi")) {
            switchLanguage.setChecked(true);
        } else {
            switchLanguage.setChecked(false);
        }

        switchLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    languageToLoad = "hi";
                    Locale locale = new Locale(languageToLoad);
                    PrefData.writeStringPref(PrefData.PREF_selected_language, languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                    startActivity(new Intent(OperationsMainActivity.this, OperationsMainActivity.class));
                    finish();
                } else {

                    languageToLoad = "en";
                    Locale locale = new Locale(languageToLoad);
                    PrefData.writeStringPref(PrefData.PREF_selected_language, languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                    startActivity(new Intent(OperationsMainActivity.this, OperationsMainActivity.class));
                    finish();
                }
            }
        });

    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "aver_bold.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void showDialogToAskForAutoStartUp() {
        final Dialog dialog = new Dialog(OperationsMainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_layout_autostart);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView deny = dialog.findViewById(R.id.tv_deny);
        TextView allow = dialog.findViewById(R.id.tv_allow);

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.permission_required), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        });
        allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                PrefData.writeBooleanPref(PrefData.autostartup, true);
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    private void connectApiToFetchProfileDetails() {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            Call<ApiResponse> call = apiInterface.Profile(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                tvCompanyName.setText(PrefData.readStringPref(PrefData.company_name));
                                PrefData.writeStringPref(PrefData.employee_name, response.body().getData().get(0).getName());
                                PrefData.writeStringPref(PrefData.employee_type, response.body().getType());
                                PrefData.writeStringPref(PrefData.employee_id, String.valueOf(response.body().getData().get(0).getEmployeeId()));
                                tvName.setText("Welcome " + " " + response.body().getData().get(0).getName());
                                tvEmpId.setText(response.body().getData().get(0).getEmpCode());
                                tvEmpType.setText(PrefData.readStringPref(PrefData.employee_type));
                                connectApiToFetchPartialDetails();
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void connectApiToFetchPartialDetails() {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.operationalPartial(
                    PrefData.readStringPref(PrefData.security_token)
            );
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();
                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                notice = response.body().getNotice();
                                if (response.body().isIsCheckedIn()) {
                                    btnCheckin.setText(getString(R.string.checkout));
                                    btnCheckin.setBackground(getResources().getDrawable(R.drawable.gradient_circle_operations_red));
                                    cardViewComments.setVisibility(View.VISIBLE);
                                    fabScan.setVisibility(View.VISIBLE);
                                    isConveyanceAsked = response.body().isIsConveyanceAsked();
                                    isLiveTrackingEnabled = response.body().isIsLiveTracking();
                                    etCheckinMsg.setText("");
                                    tiCheckinMsg.setVisibility(View.GONE);


                                    if (isLiveTrackingEnabled) {
                                        scheduleServiceForTracking();
                                    }

                                } else {
                                    btnCheckin.setText(getString(R.string.check_in));
                                    btnCheckin.setBackground(getResources().getDrawable(R.drawable.gradient_circle_operations_green));
                                    etCheckinMsg.setText("");
                                    tiCheckinMsg.setVisibility(View.VISIBLE);
                                    fabScan.setVisibility(View.GONE);

                                    if (deviceName.equalsIgnoreCase("Micromax") || deviceName.equalsIgnoreCase("Nokia") || deviceName.equalsIgnoreCase("Samsung") || deviceName.equalsIgnoreCase("Motorola")) {
                                        //Do Nothing
                                        //Toast.makeText(OperationsMainActivity.this, "Great Manu???", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (response.body().isIsLiveTracking()) {
                                            Log.e("liveTracking", "LiveTrackingEnabled");
                                            if (!PrefData.readBooleanPref(PrefData.autostartup)) {
                                                Log.e("liveTrackingInner", "AutostartupNeverCalled");
                                                showDialogToAskForAutoStartUp();
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    if (Utils.isMyServiceRunning(LiveTrackingForOperations.class, OperationsMainActivity.this)) {
                                        stopServiceForTracking();
                                    }
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });

        }
    }

    private boolean startRequestPermission() {

        if (ActivityCompat.checkSelfPermission(OperationsMainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(OperationsMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(OperationsMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(OperationsMainActivity.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_checkin:
                if (btnCheckin.getText().toString().equalsIgnoreCase(getString(R.string.check_in))) {
                    checkIn();
                } else if (btnCheckin.getText().toString().equalsIgnoreCase(getString(R.string.checkout))) {
                    if (isConveyanceAsked) {
                        openDialogForConveyance();
                    } else {
                        connectApiToCheckoutOperations();
                    }
                }
                break;
            case R.id.btn_submit_comments:

                commentText = etComment.getText().toString();

                if (commentText.equalsIgnoreCase("") && pictureFilePathComment.equalsIgnoreCase("")) {
                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.write_comment), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (!commentText.equalsIgnoreCase("") && pictureFilePathComment.equalsIgnoreCase("")) {
                    commentType = "TEXT";
                } else if (commentText.equalsIgnoreCase("") && !pictureFilePathComment.equalsIgnoreCase("")) {
                    commentType = "IMAGE";
                } else if (!commentText.equalsIgnoreCase("") && !pictureFilePathComment.equalsIgnoreCase("")) {
                    commentType = "BOTH";
                }

                if (!commentType.equalsIgnoreCase("")) {
                    connectApiToComment(commentType);
                }

                break;
            case R.id.tv_attach_image:
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    sendTakePictureIntentForComment();
                } else {
                    Utils.showToast(OperationsMainActivity.this, "Your Phone doesn\'t have camera", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                }
                break;
            case R.id.fab_scan:
                isSpyImageTaken = false;
                isSpyImageFound = false;
                progressView.showLoader();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pictureService.startCapturing(OperationsMainActivity.this);
                    }
                }, 200);
                break;
            case R.id.tv_view_notice:
                openDialogToShowNotice();
                break;

            case R.id.tv_sos:
                connectApiToCallSOS();
                break;

        }
    }

    private void connectApiToCallSOS() {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.sos(PrefData.readStringPref(PrefData.security_token));

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase("success")) {

                                Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(drawerLayout, response.body().getMsg(), OperationsMainActivity.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });
        }
    }

    private void openDialogToShowNotice() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_show_notice);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = (int) (displaymetrics.heightPixels * 0.8);
        dialog.getWindow().setLayout(width, height);

        MyButton done;
        MyTextview tvNotice;

        done = dialog.findViewById(R.id.btn_dialog_done);
        tvNotice = dialog.findViewById(R.id.tv_notice);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvNotice.setText(Html.fromHtml(notice, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvNotice.setText(Html.fromHtml(notice));
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void connectApiToComment(String commType) {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            progressView.showLoader();

            if (commType.equalsIgnoreCase("TEXT")) {
                filePart = MultipartBody.Part.createFormData("image", "", RequestBody.create(MediaType.parse("text/plain"), ""));
            } else {
                filePart = MultipartBody.Part.createFormData("image", commentFile.getName(), RequestBody.create(MediaType.parse("image/*"), commentFile));
            }

            RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token) + "");
            RequestBody Type = RequestBody.create(MediaType.parse("text/plain"), commentType);
            RequestBody CommentText = RequestBody.create(MediaType.parse("text/plain"), commentText);
            RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLatitude));
            RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLongitude));
            RequestBody isOperational = RequestBody.create(MediaType.parse("text/plain"), "true");

            Call<ApiResponse> call = apiInterface.communication(
                    SecurityToken,
                    Type,
                    CommentText,
                    Latitude,
                    Longitude,
                    filePart,
                    isOperational
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.posted_sucessfully), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                etComment.setText("");
                                tvAttachImage.setImageResource(R.drawable.ic_add_camera);
                                commentType = "";
                                commentText = "";
                                pictureFilePathComment = "";

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });
        }
    }

    private void checkIn() {
        checkinMessage = etCheckinMsg.getText().toString().trim();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            sendTakePictureIntentForCheckin();
        } else {
            Utils.showToast(OperationsMainActivity.this, "Your Phone doesn\'t have camera", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }
    }

    private void sendTakePictureIntentForCheckin() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFile = null;
        try {
            pictureFile = getPictureFileCheckin();
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.trackkers.tmark.fileprovider", pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, REQUEST_CODE_FOR_FRONT_CAMERA);
            }
        } catch (IOException ex) {
            Utils.showToast(OperationsMainActivity.this, "Photo file can't be created, please try again", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }
    }

    private void sendTakePictureIntentForComment() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFileComment = null;
        try {
            pictureFileComment = getPictureFileComment();
            if (pictureFileComment != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.trackkers.tmark.fileprovider", pictureFileComment);
                intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_CODE_FOR_BACK_CAMERA);
            }
        } catch (IOException ex) {
            Utils.showToast(OperationsMainActivity.this, "Photo file can't be created, please try again", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void rotateBitmap(Bitmap bitmap, String filePath, File fileName) {

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (exifInterface != null) {
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            default:
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        try {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File getPictureFileCheckin() throws IOException {
        String timeStamp = Utils.currentTimeStamp();
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(timeStamp, ".png", storageDir);
        pictureFilePathCheckin = image.getAbsolutePath();
        return image;
    }

    private File getPictureFileComment() throws IOException {
        String timeStamp = Utils.currentTimeStamp();
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(timeStamp, ".png", storageDir);
        pictureFilePathComment = image.getAbsolutePath();
        return image;
    }

    private void connectApiToCheckinOperations() {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            progressView.showLoader();

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastKnownLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("selfie", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
                    RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLatitude));
                    RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLongitude));
                    RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token));
                    RequestBody CheckinMessage = RequestBody.create(MediaType.parse("text/plain"), checkinMessage);
                    RequestBody Battery = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(Utils.getBatteryPercentage(OperationsMainActivity.this)));

                    Call<ApiResponse> call = apiInterface.operationalCheckIn(
                            SecurityToken,
                            Latitude,
                            Longitude,
                            Battery,
                            CheckinMessage,
                            filePart
                    );
                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            progressView.hideLoader();

                            try {
                                if (response.body() != null && response.body().getStatus() != null) {
                                    if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                        Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.checkin_successfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                                        btnCheckin.setText(getString(R.string.checkout));
                                        btnCheckin.setBackground(getResources().getDrawable(R.drawable.gradient_circle_operations_red));
                                        etCheckinMsg.setText("");
                                        tiCheckinMsg.setVisibility(View.GONE);
                                        fabScan.setVisibility(View.VISIBLE);
                                        isConveyanceAsked = response.body().isIsConveyanceAsked();
                                        isLiveTrackingEnabled = response.body().isIsLiveTracking();

                                        if (isLiveTrackingEnabled) {
                                            scheduleServiceForTracking();
                                        }

                                        if (PrefData.readStringPref(PrefData.last_checkin_operations).equalsIgnoreCase("")) {
                                            currentDateAndTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                                            PrefData.writeStringPref(PrefData.last_checkin_operations, currentDateAndTime);
                                            tvLastCheckin.setText("Last Check-In:\n" + PrefData.readStringPref(PrefData.last_checkin_operations));

                                        } else {
                                            currentDateAndTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                                            PrefData.writeStringPref(PrefData.last_checkin_operations, currentDateAndTime);

                                            tvLastCheckin.setText("Last Check-In:\n" + PrefData.readStringPref(PrefData.last_checkin_operations));
                                        }


                                        openDialogForSuccessfullCheckin();

                                    } else {
                                        if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                            Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                        } else {
                                            Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 500) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 404) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            progressView.hideLoader();
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.location_not_detected), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                            t.printStackTrace();
                        }
                    });
                }
            }, 200);
        }
    }

    private void openDialogForSuccessfullCheckin() {

        final Dialog dialog = new Dialog(OperationsMainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_layout_checkin);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = (int) (displaymetrics.widthPixels * 0.9);
        int height = (int) (displaymetrics.heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView selfie = dialog.findViewById(R.id.iv_selfie);
        TextView name = dialog.findViewById(R.id.tv_name);
        TextView date = dialog.findViewById(R.id.tv_date);
        TextView time = dialog.findViewById(R.id.tv_time);
        Button btnDone = dialog.findViewById(R.id.btn_done);
        Picasso.get().load(file).into(selfie);
        name.setText("Name : " + PrefData.readStringPref(PrefData.employee_name));
        Utils.selectedDateFormat(Long.valueOf(Utils.currentTimeStamp()), date);
        Utils.selectedTimeFormat(Long.valueOf(Utils.currentTimeStamp()), time);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                cardViewComments.setVisibility(View.VISIBLE);
            }
        });
        dialog.show();
    }

    private void openDialogForConveyance() {

        dialog = new Dialog(OperationsMainActivity.this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout_conveyance);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = (int) (displaymetrics.widthPixels * 0.9);
        int height = (int) (displaymetrics.heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText distance = dialog.findViewById(R.id.et_distance);
        TextInputEditText fare = dialog.findViewById(R.id.et_fare);
        Button button = dialog.findViewById(R.id.btn_submit);
        ImageView close = dialog.findViewById(R.id.iv_dialog_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (distance.getText().toString().equalsIgnoreCase("")) {
                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.fill_distance), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (fare.getText().toString().equalsIgnoreCase("")) {
                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.fill_fare), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else {
                    connectApiToconveyanceDeetails(distance.getText().toString(), fare.getText().toString());
                }
            }
        });
        dialog.show();
    }

    private void connectApiToconveyanceDeetails(String Distance, String Fare) {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.empConenyanceData(
                    PrefData.readStringPref(PrefData.security_token),
                    Distance,
                    Fare
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                dialog.dismiss();
                                connectApiToCheckoutOperations();

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {

                                    if (Utils.isMyServiceRunning(LiveTrackingForOperations.class, OperationsMainActivity.this)) {
                                        stopServiceForTracking();
                                    }

                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });
        }
    }

    private void connectApiToCheckoutOperations() {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {
            progressView.showLoader();

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastKnownLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Call<ApiResponse> call = apiInterface.operationalCheckOut(
                            PrefData.readStringPref(PrefData.security_token),
                            String.valueOf(currentLatitude),
                            String.valueOf(currentLongitude),
                            String.valueOf(Utils.getBatteryPercentage(OperationsMainActivity.this)));

                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            progressView.hideLoader();

                            try {
                                if (response.body() != null && response.body().getStatus() != null) {
                                    if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                        Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.checkout_sucessfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                                        btnCheckin.setText(getResources().getString(R.string.checkin));
                                        etCheckinMsg.setText("");
                                        btnCheckin.setBackground(getResources().getDrawable(R.drawable.gradient_circle_operations_green));
                                        tiCheckinMsg.setVisibility(View.VISIBLE);
                                        cardViewComments.setVisibility(View.GONE);
                                        fabScan.setVisibility(View.GONE);
                                        stopServiceForTracking();
                                        //settingsForReleaseCPUUsage();
                                    } else {
                                        if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                            Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                        } else {
                                            Utils.showSnackBar(drawerLayout, response.body().getMsg(), OperationsMainActivity.this);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 500) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 404) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            progressView.hideLoader();
                            t.printStackTrace();
                        }
                    });
                }
            }, 200);
        }
    }

    private void scheduleServiceForTracking() {

        startService(new Intent(this, LiveTrackingForOperations.class));
        FOMarkAttendance.isServiceRunning = true;
        //LiveTrackingForOperations.setAlarmForLocationUpdate(true, OperationsMainActivity.this);
    }

    private void stopServiceForTracking() {
        LiveTrackingForOperations.removeDatabaseValues(OperationsMainActivity.this);
        LiveTrackingForOperations.stopLocationUpdate();
        LiveTrackingForOperations.cancelNotification(OperationsMainActivity.this, NOTIFICATION_CHANNEL_ID);
        Utils.stopAlarmService(OperationsMainActivity.this);

        stopServiceIntent = new Intent(OperationsMainActivity.this, LiveTrackingForOperations.class);
        stopService(stopServiceIntent);
        FOMarkAttendance.isServiceRunning = false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_home) {
            drawerLayout.closeDrawers();

            Intent intent = new Intent(OperationsMainActivity.this, OperationsMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(OperationsMainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_verification_history) {
            startActivity(new Intent(OperationsMainActivity.this, OperationalHistory.class));
        } else if (id == R.id.nav_show_docs) {
            startActivity(new Intent(OperationsMainActivity.this, ViewDocuments.class));
        } else if (id == R.id.nav_resetpassword) {
            startActivity(new Intent(OperationsMainActivity.this, ResetPassword.class));
        } else if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.nav_rate_us) {
            rateUs();
        } else if (id == R.id.nav_logout) {
            connectApiToLogoutGuard();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void share() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "TMARK");
            String sAux = getResources().getString(R.string.share_text);
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.trackkers.tmark";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rateUs() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.unable_to_find), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }
    }

    private void connectApiToLogoutGuard() {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {

            progressView.showLoader();

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastKnownLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Call<ApiResponse> call = apiInterface.logoutEmp(
                            PrefData.readStringPref(PrefData.security_token)
                    );

                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            progressView.hideLoader();
                            try {
                                if (response.body() != null && response.body().getStatus() != null) {
                                    if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                        Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.checkout_sucessfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                        logout();
                                    } else {
                                        if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                            Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                        } else {
                                            Utils.showSnackBar(drawerLayout, response.body().getMsg(), OperationsMainActivity.this);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 500) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 404) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            progressView.hideLoader();
                            t.printStackTrace();
                        }
                    });
                }
            }, 200);
        }
    }

    private void logout() {
        PrefData.writeBooleanPref(PrefData.PREF_LOGINSTATUS, false);
        PrefData.writeStringPref(PrefData.last_checkin_operations, "");
        PrefData.writeStringPref(PrefData.total_scan_count_operations, "");

        Intent intent = new Intent(OperationsMainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i("tag", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED) {
                Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.sorry_cant_use), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                startRequestPermission();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @SuppressLint("MissingPermission")
    public void requestLocation() {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this)
                        .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);

                fusedLocationProviderClient.requestLocationUpdates(
                        mLocationRequest,
                        locationCallback,
                        Looper.myLooper()
                );

            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(
                                    OperationsMainActivity.this,
                                    LocationRequest.PRIORITY_HIGH_ACCURACY);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    public void getLastKnownLocation() {

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            Log.e("lastCurrentLatitude", String.valueOf(currentLatitude));
                            Log.e("lastCurrentLongitude", String.valueOf(currentLongitude));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.location_not_detected), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.exact_location_not_detected), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(OperationsMainActivity.this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null && mLastLocation.hasAccuracy()) {
            if (mLastLocation.getAccuracy() <= 100) {
                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();
                Log.e("currentLatitude", String.valueOf(currentLatitude));
                Log.e("currentLongitude", String.valueOf(currentLongitude));
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(OperationsMainActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                progressView.hideLoader();
                Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.result_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            } else {
                try {
                    JSONObject jObj = new JSONObject(result.getContents());
                    scannedCheckpointId = jObj.getString("id");
                    scannedCheckpointType = jObj.getString("type");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (scannedCheckpointId.equalsIgnoreCase("") && scannedCheckpointType.equalsIgnoreCase("")) {
                    progressView.hideLoader();
                    Utils.showToast(OperationsMainActivity.this, "Wrong Qr Code Scanned", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else {
                    progressView.showLoader();
                    connectApiToScanQrCodeOperations(scannedCheckpointId, scannedCheckpointType, isSpyImageFound);
                }
            }
        }


        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e("gpsSuccess", "gpsSuccess");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("gpsFail", "gpsFail");
                        break;
                    default:
                        break;
                }
        }

        if (requestCode == REQUEST_CODE_FOR_FRONT_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {

                file = new File(pictureFilePathCheckin);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rotateBitmap(bitmap, file.getPath(), file);
                } else {
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream(file));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                connectApiToCheckinOperations();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Utils.showToast(OperationsMainActivity.this, "Camera Closed", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        } else if (requestCode == REQUEST_CODE_FOR_BACK_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {

                commentFile = new File(pictureFilePathComment);
                Bitmap bitmap = BitmapFactory.decodeFile(commentFile.getPath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rotateBitmap(bitmap, commentFile.getPath(), commentFile);
                } else {
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream(commentFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                Bitmap myBitmap = BitmapFactory.decodeFile(commentFile.getAbsolutePath());
                tvAttachImage.setImageBitmap(myBitmap);
            } else {
                commentType = "";
                commentText = "";
                pictureFilePathComment = "";
                Utils.showToast(OperationsMainActivity.this, "Camera CLosed", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        }
    }

    private void connectApiToScanQrCodeOperations(String scannedCheckpointId, String scannedCheckpointType, boolean isFilePresent) {
        if (CheckNetworkConnection.isConnection1(OperationsMainActivity.this, true)) {

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastKnownLocation();
            }

            MultipartBody.Part filePart = null;
            if (isFilePresent) {
                filePart = MultipartBody.Part.createFormData("image", spyFile.getName(), RequestBody.create(MediaType.parse("image/*"), spyFile));
            } else {
                filePart = MultipartBody.Part.createFormData("image", "", RequestBody.create(MediaType.parse("text/plain"), ""));
            }

            RequestBody token = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token));
            RequestBody checkpointType = RequestBody.create(MediaType.parse("text/plain"), scannedCheckpointType);
            RequestBody checkpointId = RequestBody.create(MediaType.parse("text/plain"), scannedCheckpointId);
            RequestBody latitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLatitude));
            RequestBody longitude = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLongitude));

            Call<ApiResponse> call = apiInterface.operationVisitByScanQr(
                    token,
                    checkpointType,
                    checkpointId,
                    latitude,
                    longitude,
                    filePart
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();
                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                Utils.showToast(OperationsMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                                if (PrefData.readStringPref(PrefData.total_scan_count_operations).equalsIgnoreCase("")) {
                                    totalScannedQr++;
                                    PrefData.writeStringPref(PrefData.total_scan_count_operations, totalScannedQr + "");
                                    tvTotalQrScan.setText("Total Scan:\n" + PrefData.readStringPref(PrefData.total_scan_count_operations));
                                } else {
                                    totalScannedQr = Integer.parseInt(PrefData.readStringPref(PrefData.total_scan_count_operations));
                                    totalScannedQr++;
                                    PrefData.writeStringPref(PrefData.total_scan_count_operations, totalScannedQr + "");
                                    tvTotalQrScan.setText("Total Scan:\n" + PrefData.readStringPref(PrefData.total_scan_count_operations));
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(OperationsMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(drawerLayout, response.body().getMsg(), OperationsMainActivity.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(OperationsMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && fusedLocationProviderClient != null) {
            requestLocation();
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (!isSpyImageTaken) {
            if (pictureData != null && pictureUrl != null) {
                spyFile = new File(pictureUrl);
                qrScanOperations.setOrientationLocked(true);
                qrScanOperations.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = true;
            } else {
                qrScanOperations.setOrientationLocked(true);
                qrScanOperations.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = false;
            }
        }
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (!isSpyImageTaken) {
            if (picturesTaken != null && !picturesTaken.isEmpty()) {
                spyFile = new File(picturesTaken.lastEntry().getKey());
                qrScanOperations.setOrientationLocked(true);
                qrScanOperations.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = true;
            } else {
                qrScanOperations.setOrientationLocked(true);
                qrScanOperations.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = false;
            }
        }
    }
}
