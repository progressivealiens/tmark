package com.trackkers.tmark.views.activity.fieldofficer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

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
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PictureCapturingListener;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.receiver.RestartServiceReceiver;
import com.trackkers.tmark.services.APictureCapturingService;
import com.trackkers.tmark.services.LiveTrackingForOperations;
import com.trackkers.tmark.services.PictureCapturingServiceImpl;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.operations.OperationsMainActivity;
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
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FOMarkAttendance extends AppCompatActivity implements
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
    @BindView(R.id.tv_name)
    MyTextview tvName;
    @BindView(R.id.tv_view_notice)
    MyTextview tvNotice;
    @BindView(R.id.tv_company_name)
    MyTextview tvCompanyName;
    @BindView(R.id.tv_emp_id)
    MyTextview tvEmpId;
    @BindView(R.id.tv_emp_type)
    MyTextview tvEmpType;
    @BindView(R.id.et_checkin_msg)
    TextInputEditText etCheckinMsg;
    @BindView(R.id.ti_checkin_msg)
    TextInputLayout tiCheckinMsg;
    @BindView(R.id.et_comment)
    TextInputEditText etComment;
    @BindView(R.id.ti_comments)
    TextInputLayout tiComments;
    @BindView(R.id.tv_attach_image)
    ImageView tvAttachImage;
    @BindView(R.id.btn_submit_comments)
    Button btnSubmitComments;
    @BindView(R.id.card_view_comments)
    LinearLayout cardViewComments;
    @BindView(R.id.btn_checkin)
    Button btnCheckin;
    @BindView(R.id.fab_scan)
    FloatingActionButton fabScan;
    @BindView(R.id.root_fo_attendance)
    RelativeLayout rootFoAttendance;
    @BindView(R.id.btn_site_visit)
    Button btnSiteVisit;
    @BindView(R.id.tv_last_checkin)
    MyTextview tvLastCheckin;
    @BindView(R.id.tv_total_qr_scan)
    MyTextview tvTotalQrScan;
    @BindView(R.id.tv_sos)
    MyTextview tvSos;


    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SettingsClient mSettingsClient;
    LocationSettingsRequest mLocationSettingsRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LocationCallback locationCallback;
    Intent restartService;
    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9009;
    private static final int REQUEST_CHECK_SETTINGS = 5005;
    private static final int REQUEST_CODE_FOR_BACK_CAMERA = 200;
    private static final int REQUEST_CODE_FOR_FRONT_CAMERA = 215;
    public static final int NOTIFICATION_CHANNEL_ID = 151;

    File file = null, commentFile = null, spyFile = null;
    MultipartBody.Part filePart = null;

    double currentLatitude = 0.0, currentLongitude = 0.0;
    public boolean isConveyanceAsked = false, isLiveTrackingEnabled = false, isSpyImageTaken = false, isSpyImageFound = false;
    public static boolean isServiceRunning = false;
    String checkinMessage = "", commentText = "", commentType = "", deviceName = "", pictureFilePathCheckin = "", pictureFilePathComment = "", scannedCheckpointId = "", scannedCheckpointType = "";
    String notice = "", currentDateAndTime = "";
    int totalScannedQr = 0;
    IntentIntegrator qrScanFo;


    private APictureCapturingService pictureService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fomark_attendance);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        connectApiToFetchPartialDetails();

    }

    private void initialize() {
        ivBack.setOnClickListener(this);
        btnCheckin.setOnClickListener(this);
        btnSubmitComments.setOnClickListener(this);
        tvAttachImage.setOnClickListener(this);
        fabScan.setOnClickListener(this);
        btnSiteVisit.setOnClickListener(this);
        tvNotice.setOnClickListener(this);
        tvSos.setOnClickListener(this);
        tvNotice.setSelected(true);

        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.mark_your_attendance);

        prefData = new PrefData(FOMarkAttendance.this);
        apiInterface = ApiClient.getClient(FOMarkAttendance.this).create(ApiInterface.class);
        progressView = new ProgressView(FOMarkAttendance.this);
        qrScanFo = new IntentIntegrator(FOMarkAttendance.this);

        tvCompanyName.setText(PrefData.readStringPref(PrefData.company_name));
        tvName.setText(getResources().getString(R.string.welcome) + " " + PrefData.readStringPref(PrefData.employee_name));
        tvEmpId.setText(PrefData.readStringPref(PrefData.employee_code));
        tvEmpType.setText(PrefData.readStringPref(PrefData.employee_type));
        if (PrefData.readStringPref(PrefData.last_checkin_fo).equalsIgnoreCase("")) {
            tvLastCheckin.setText("Last Check-In:\n" + "0");
        } else {
            tvLastCheckin.setText("Last Check-In:\n" + PrefData.readStringPref(PrefData.last_checkin_fo));
        }

        if (PrefData.readStringPref(PrefData.total_scan_count_fo).equalsIgnoreCase("")) {
            tvTotalQrScan.setText("Total Scan:\n" + "0");
        } else {
            tvTotalQrScan.setText("Total Scan:\n" + PrefData.readStringPref(PrefData.total_scan_count_fo));
        }

        deviceName = Build.MANUFACTURER;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pictureService = PictureCapturingServiceImpl.getInstance(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        restartService = new Intent(FOMarkAttendance.this, RestartServiceReceiver.class);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("locationWrapper", String.valueOf(locationResult.getLastLocation()));
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void connectApiToFetchPartialDetails() {
        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.operationalPartial(PrefData.readStringPref(PrefData.security_token));

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
                                    btnSiteVisit.setVisibility(View.VISIBLE);

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
                                    btnSiteVisit.setVisibility(View.GONE);

                                    if (deviceName.trim().toLowerCase().equalsIgnoreCase("huawei")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("sony")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("asus")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("xiaomi")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("oppo")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("samsung")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("realme")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("letv")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("honor")
                                            || deviceName.trim().toLowerCase().equalsIgnoreCase("vivo")) {

                                        if (response.body().isIsLiveTracking()) {
                                            if (!PrefData.readBooleanPref(PrefData.autostartup)) {
                                                showDialogToAskForAutoStartUp();
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    if (Utils.isMyServiceRunning(LiveTrackingForOperations.class, FOMarkAttendance.this)) {
                                        stopServiceForTracking();
                                    }
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    private void showDialogToAskForAutoStartUp() {
        final Dialog dialog = new Dialog(FOMarkAttendance.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_layout_autostart);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = (int) (displaymetrics.heightPixels * 0.6);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView deny = dialog.findViewById(R.id.tv_deny);
        TextView allow = dialog.findViewById(R.id.tv_allow);

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.permission_required), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_checkin:
                if (btnCheckin.getText().toString().equalsIgnoreCase(getResources().getString(R.string.check_in))) {
                    checkIn();
                } else if (btnCheckin.getText().toString().equalsIgnoreCase(getResources().getString(R.string.checkout))) {

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
                    Utils.showToast(this, getResources().getString(R.string.write_comment), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
                    Utils.showToast(this, "Your Phone doesn\'t have camera", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
                        pictureService.startCapturing(FOMarkAttendance.this);
                    }
                }, 200);

                break;

            case R.id.btn_site_visit:
                startActivity(new Intent(FOMarkAttendance.this, SiteDetailsActivity.class));
                break;

            case R.id.tv_view_notice:
                openDialogToShowNotice();
                break;

            case R.id.iv_back:
                onBackPressed();
                break;

            case R.id.tv_sos:
                connectApiToCallSOS();
                break;
        }
    }

    private void connectApiToCallSOS() {
        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.sos(PrefData.readStringPref(PrefData.security_token));

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase("success")) {

                                Utils.showToast(FOMarkAttendance.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {
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
                            if (response.body().getStatus().equalsIgnoreCase("success")) {
                                Utils.showToast(FOMarkAttendance.this, "Posted successfully !!!", Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                etComment.setText("");
                                tvAttachImage.setImageResource(R.drawable.ic_add_camera);
                                commentType = "";
                                commentText = "";
                                pictureFilePathComment = "";

                            } else {

                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
            Utils.showToast(this, "Your Phone doesn\'t have camera", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }

    }

    private void sendTakePictureIntentForCheckin() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFileCheckin = null;
        try {
            pictureFileCheckin = getPictureFileCheckin();
            if (pictureFileCheckin != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.trackkers.tmark.fileprovider", pictureFileCheckin);
                cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, REQUEST_CODE_FOR_FRONT_CAMERA);
            }
        } catch (IOException ex) {
            Utils.showToast(this, "Photo file can't be created, please try again", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
            Utils.showToast(this, "Photo file can't be created, please try again", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {
            progressView.showLoader();
            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastKnownLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("selfie", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
                    RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), currentLatitude + "");
                    RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), currentLongitude + "");
                    RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token));
                    RequestBody CheckinMessage = RequestBody.create(MediaType.parse("text/plain"), checkinMessage);
                    RequestBody Battery = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(Utils.getBatteryPercentage(FOMarkAttendance.this)));

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

                                        Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.checkin_successfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                                        btnCheckin.setText(getString(R.string.checkout));
                                        btnCheckin.setBackground(getResources().getDrawable(R.drawable.gradient_circle_operations_red));
                                        etCheckinMsg.setText("");
                                        tiCheckinMsg.setVisibility(View.GONE);
                                        fabScan.setVisibility(View.VISIBLE);
                                        btnSiteVisit.setVisibility(View.VISIBLE);

                                        isConveyanceAsked = response.body().isIsConveyanceAsked();
                                        isLiveTrackingEnabled = response.body().isIsLiveTracking();

                                        if (isLiveTrackingEnabled) {
                                            scheduleServiceForTracking();
                                        }

                                        if (PrefData.readStringPref(PrefData.last_checkin_fo).equalsIgnoreCase("")) {
                                            currentDateAndTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                                            PrefData.writeStringPref(PrefData.last_checkin_fo, currentDateAndTime);
                                            tvLastCheckin.setText("Last Check-In:\n" + PrefData.readStringPref(PrefData.last_checkin_fo));

                                        } else {
                                            currentDateAndTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                                            PrefData.writeStringPref(PrefData.last_checkin_fo, currentDateAndTime);

                                            tvLastCheckin.setText("Last Check-In:\n" + PrefData.readStringPref(PrefData.last_checkin_fo));
                                        }

                                        openDialogForSuccessfullCheckin();

                                    } else {

                                        if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                            Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                        } else {
                                            Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 500) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 404) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    private void openDialogForSuccessfullCheckin() {
        final Dialog dialog = new Dialog(FOMarkAttendance.this);
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

        final Dialog dialog = new Dialog(FOMarkAttendance.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_layout_conveyance);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = (int) (displaymetrics.widthPixels * 0.9);
        int height = (int) (displaymetrics.heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText distance = dialog.findViewById(R.id.et_distance);
        TextInputEditText fare = dialog.findViewById(R.id.et_fare);
        ImageView close = dialog.findViewById(R.id.iv_dialog_close);
        Button button = dialog.findViewById(R.id.btn_submit);
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
                    Utils.showToast(FOMarkAttendance.this, "Please fill the distance travelled", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (fare.getText().toString().equalsIgnoreCase("")) {
                    Utils.showToast(FOMarkAttendance.this, "Please fill the Fare", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else {
                    connectApiToConveyanceDetails(distance.getText().toString(), fare.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void connectApiToConveyanceDetails(String Distance, String Fare) {
        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {
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

                                connectApiToCheckoutOperations();

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {

                                    if (Utils.isMyServiceRunning(LiveTrackingForOperations.class, FOMarkAttendance.this)) {
                                        stopServiceForTracking();
                                    }

                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {
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
                            String.valueOf(Utils.getBatteryPercentage(FOMarkAttendance.this)));

                    call.enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            progressView.hideLoader();

                            try {
                                if (response.body() != null && response.body().getStatus() != null) {
                                    if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                        btnCheckin.setText(getString(R.string.check_in));
                                        btnCheckin.setBackground(getResources().getDrawable(R.drawable.gradient_circle_operations_green));
                                        etCheckinMsg.setText("");
                                        tiCheckinMsg.setVisibility(View.VISIBLE);
                                        cardViewComments.setVisibility(View.GONE);
                                        fabScan.setVisibility(View.GONE);
                                        btnSiteVisit.setVisibility(View.GONE);
                                        Utils.showToast(FOMarkAttendance.this, "Checkout Successful", Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                        stopServiceForTracking();

                                        //unregisterReceiver(trackingReceiver);

                                    } else {
                                        if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                            Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                        } else {
                                            Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 500) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else if (response.code() == 404) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
        isServiceRunning = true;
    }

    private void stopServiceForTracking() {
        LiveTrackingForOperations.removeDatabaseValues(FOMarkAttendance.this);
        LiveTrackingForOperations.stopLocationUpdate();
        LiveTrackingForOperations.cancelNotification(FOMarkAttendance.this, NOTIFICATION_CHANNEL_ID);
        Utils.stopAlarmService(FOMarkAttendance.this);

        restartService = new Intent(FOMarkAttendance.this, LiveTrackingForOperations.class);
        stopService(restartService);
        isServiceRunning = false;
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
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.location_not_detected), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.exact_location_not_detected), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                progressView.hideLoader();
                Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.result_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
                    Utils.showToast(FOMarkAttendance.this, "Wrong Qr Code Scanned", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else {
                    progressView.showLoader();
                    connectApiToScanQrCodeFO(scannedCheckpointId, scannedCheckpointType, isSpyImageFound);
                }
            }
        } else if (requestCode == REQUEST_CODE_FOR_FRONT_CAMERA) {
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
                Utils.showToast(FOMarkAttendance.this, "Camera Closed", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

                Bitmap myBitmap = BitmapFactory.decodeFile(commentFile.getPath());
                tvAttachImage.setImageBitmap(myBitmap);
            } else {
                commentType = "";
                commentText = "";
                pictureFilePathComment = "";
                Utils.showToast(FOMarkAttendance.this, "Camera Closed", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e("gpsSucess", "gpsSucess");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.enable_gps), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                finish();
            }
        }
    }

    private void connectApiToScanQrCodeFO(String scannedCheckpointId, String scannedCheckpointType, boolean isFilePresent) {
        if (CheckNetworkConnection.isConnection1(FOMarkAttendance.this, true)) {

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
                                Utils.showToast(FOMarkAttendance.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                                if (PrefData.readStringPref(PrefData.total_scan_count_fo).equalsIgnoreCase("")) {
                                    totalScannedQr++;
                                    PrefData.writeStringPref(PrefData.total_scan_count_fo, totalScannedQr + "");
                                    tvTotalQrScan.setText("Total Scan:\n" + PrefData.readStringPref(PrefData.total_scan_count_fo));

                                } else {
                                    totalScannedQr = Integer.parseInt(PrefData.readStringPref(PrefData.total_scan_count_fo));
                                    totalScannedQr++;
                                    PrefData.writeStringPref(PrefData.total_scan_count_fo, totalScannedQr + "");
                                    tvTotalQrScan.setText("Total Scan:\n" + PrefData.readStringPref(PrefData.total_scan_count_fo));
                                }

                            } else {

                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(FOMarkAttendance.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootFoAttendance, response.body().getMsg(), FOMarkAttendance.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(FOMarkAttendance.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
    public void onConnected(@Nullable Bundle bundle) {
    }

    public void requestLocation() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(FOMarkAttendance.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(FOMarkAttendance.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(FOMarkAttendance.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.e("GPS", "Unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.e("GPS", "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                        }
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
                connectionResult.startResolutionForResult(FOMarkAttendance.this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(FOMarkAttendance.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && fusedLocationProviderClient != null) {
            requestLocation();
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceRunning) {
            Intent broadIntent = new Intent(this, RestartServiceReceiver.class);
            sendBroadcast(broadIntent);
        }
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (!isSpyImageTaken) {
            if (pictureData != null && pictureUrl != null) {
                spyFile = new File(pictureUrl);
                qrScanFo.setOrientationLocked(true);
                qrScanFo.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = true;
            } else {
                qrScanFo.setOrientationLocked(true);
                qrScanFo.initiateScan();
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
                //progressView.hideLoader();
                qrScanFo.setOrientationLocked(true);
                qrScanFo.initiateScan();
                //connectApiToScanQrCodeFO(scannedCheckpointId, scannedCheckpointType,true);
                isSpyImageTaken = true;
                isSpyImageFound = true;
            } else {
                //progressView.hideLoader();
                qrScanFo.setOrientationLocked(true);
                qrScanFo.initiateScan();
                //connectApiToScanQrCodeFO(scannedCheckpointId, scannedCheckpointType,false);
                isSpyImageTaken = true;
                isSpyImageFound = false;
            }
        }
    }

}
