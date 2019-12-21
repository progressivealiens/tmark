package com.trackkers.tmark.views.activity.bulk_guard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CameraActivity;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BulkGuardCheckin extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    @BindView(R.id.btn_emp_checkin)
    MyButton btnEmpCheckin;
    @BindView(R.id.tv_checkin_time)
    MyTextview tvCheckinTime;
    @BindView(R.id.tv_checkout_time)
    MyTextview tvCheckoutTime;
    @BindView(R.id.iv_emp_pic)
    ImageView ivEmpPic;
    @BindView(R.id.tv_site_name)
    MyTextview tvSiteName;
    @BindView(R.id.tv_route_name)
    MyTextview tvRouteName;
    @BindView(R.id.tv_route_start_address)
    MyTextview tvRouteStartAddress;
    @BindView(R.id.tv_route_end_address)
    MyTextview tvRouteEndAddress;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.tv_emp_name)
    MyTextview tvEmpName;
    @BindView(R.id.tv_emp_id)
    MyTextview tvEmpId;
    @BindView(R.id.view_checkintime)
    View viewCheckintime;
    @BindView(R.id.view_checkouttime)
    View viewCheckouttime;
    @BindView(R.id.root_emp_checkin)
    ScrollView rootEmpCheckin;

    private double currentLatitude;
    private double currentLongitude;

    String imagePath = "", batterPercentage = "";
    File imageFile = null;

    ApiInterface apiInterface;
    ProgressView progressView;

    private static final int PERMISSIONS_REQUEST_CODE = 666;
    private static final int REQUEST_CHECK_SETTINGS = 5005;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9009;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SettingsClient mSettingsClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LocationCallback locationCallback;

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
        setContentView(R.layout.activity_employee_checkin);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        connectApiToGetGuardPartialDetails();
    }

    @SuppressLint("SetTextI18n")
    private void initialize() {
        linToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.guard_checkin);

        tvEmpName.setText(getResources().getString(R.string.name) + " " + PrefData.readStringPref(PrefData.employee_name));
        tvEmpId.setText(getResources().getString(R.string.employee_code) + " " + PrefData.readStringPref(PrefData.employee_code));
        tvSiteName.setText(getResources().getString(R.string.site) + " " + PrefData.readStringPref(PrefData.site_name));
        tvRouteName.setText(getResources().getString(R.string.route) + " " + PrefData.readStringPref(PrefData.route_name));
        tvRouteStartAddress.setText(getResources().getString(R.string.start_address) + " " + PrefData.readStringPref(PrefData.route_start_address));
        tvRouteEndAddress.setText(getResources().getString(R.string.end_address) + " " + PrefData.readStringPref(PrefData.route_end_address));

        apiInterface = ApiClient.getClient(BulkGuardCheckin.this).create(ApiInterface.class);
        progressView = new ProgressView(BulkGuardCheckin.this);
        btnEmpCheckin.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("locationWrapper", String.valueOf(locationResult.getLastLocation()));
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
    }

    private boolean startRequestPermission() {

        if (ActivityCompat.checkSelfPermission(BulkGuardCheckin.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(BulkGuardCheckin.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(BulkGuardCheckin.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(BulkGuardCheckin.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
            if (mLastLocation.getAccuracy() <= 40) {
                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void requestLocation() {

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (LocationSettingsResponse response) -> {

                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

                }).addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(BulkGuardCheckin.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sie) {
                    Log.e("GPS", "Unable to execute request.");
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            Log.e("lastCurrentLatitude", String.valueOf(currentLatitude));
                            Log.e("lastCurrentLongitude", String.valueOf(currentLongitude));
                        }
                    }
                });
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_emp_checkin:
                if (btnEmpCheckin.getText().toString().equalsIgnoreCase(getString(R.string.check_in))) {
                    checkIn();
                } else if (btnEmpCheckin.getText().toString().equalsIgnoreCase(getString(R.string.checkout))) {
                    getLocationFromService(2);
                }
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    private void checkIn() {
        Intent i = new Intent(BulkGuardCheckin.this, CameraActivity.class);
        startActivityForResult(i, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                imagePath = data.getStringExtra("result");

                getLocationFromService(0);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.camera_closed, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e("gpsSucess", "gpsSucess");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.enable_gps, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getLocationFromService(final int Flag) {
        if (Flag == 0) {
            connectApiToCheckInGuard();
        } else if (Flag == 2) {
            connectApiToCheckOutGuard();
        }
    }

    private void connectApiToGetGuardPartialDetails() {
        if (CheckNetworkConnection.isConnection1(BulkGuardCheckin.this, true)) {

            progressView.showLoader();
            Call<ApiResponseOperations> call = apiInterface.multipleGuardPartialDetails(
                    PrefData.readStringPref(PrefData.employee_id),
                    PrefData.readStringPref(PrefData.route_id));

            call.enqueue(new Callback<ApiResponseOperations>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                            if (response.body().isIsCheckedIn()) {
                                btnEmpCheckin.setText(R.string.checkout);

                                tvCheckinTime.setVisibility(View.VISIBLE);
                                tvCheckoutTime.setVisibility(View.VISIBLE);
                                viewCheckintime.setVisibility(View.VISIBLE);
                                viewCheckouttime.setVisibility(View.VISIBLE);
                                tvCheckinTime.setText(getString(R.string.checkin_time_) + " " + response.body().getCheckInTime());
                                tvCheckoutTime.setText(R.string.guard_duty);

                                Picasso.get().load(Utils.BASE_IMAGE + response.body().getStartImageName()).placeholder(R.drawable.progress_animation).into(ivEmpPic);

                            } else {
                                btnEmpCheckin.setText(R.string.checkin);
                                tvCheckinTime.setVisibility(View.GONE);
                                tvCheckoutTime.setVisibility(View.GONE);
                            }
                        } else {
                            Utils.showSnackBar(rootEmpCheckin, response.body().getMsg(), BulkGuardCheckin.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();

                    }

                }

                @Override
                public void onFailure(Call<ApiResponseOperations> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });

        }
    }

    private void connectApiToCheckInGuard() {

        if (CheckNetworkConnection.isConnection1(BulkGuardCheckin.this, true)) {
            progressView.showLoader();
            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    imageFile = new File(imagePath);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("selfie", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile));
                    RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), currentLatitude + "");
                    RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), currentLongitude + "");
                    RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.employee_id) + "");
                    RequestBody RouteId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(PrefData.readStringPref(PrefData.route_id)) + "");
                    RequestBody DeviceId = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.deviceID) + "");
                    RequestBody Battery = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(Utils.getBatteryPercentage(BulkGuardCheckin.this)) + "");

                    Call<ApiResponseOperations> call = apiInterface.guardMultipleCheckIn(
                            SecurityToken,
                            RouteId,
                            filePart,
                            Latitude,
                            Longitude,
                            DeviceId,
                            Battery
                    );
                    call.enqueue(new Callback<ApiResponseOperations>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                            progressView.hideLoader();

                            try {

                                if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                    Toast.makeText(BulkGuardCheckin.this, R.string.checkin_sucessfull, Toast.LENGTH_SHORT).show();
                                    btnEmpCheckin.setText(R.string.checkout);
                                    tvCheckinTime.setVisibility(View.VISIBLE);
                                    tvCheckoutTime.setVisibility(View.VISIBLE);
                                    viewCheckintime.setVisibility(View.VISIBLE);
                                    viewCheckouttime.setVisibility(View.VISIBLE);
                                    tvCheckinTime.setText(R.string.checkin_time_ + " " + response.body().getCheckInTime());
                                    tvCheckoutTime.setText(R.string.guard_duty);

                                    Picasso.get().load(Utils.BASE_IMAGE + response.body().getStartImageName()).placeholder(R.drawable.progress_animation).into(ivEmpPic);

                                } else {
                                    Utils.showSnackBar(rootEmpCheckin, response.body().getMsg(), BulkGuardCheckin.this);
                                }

                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Toast.makeText(BulkGuardCheckin.this, "Bad Request!! Please retry.", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 500) {
                                    Toast.makeText(BulkGuardCheckin.this, "Network Busy.", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 404) {
                                    Toast.makeText(BulkGuardCheckin.this, "Resource Not Found.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(BulkGuardCheckin.this, "Something went heywire!! please retry.", Toast.LENGTH_SHORT).show();
                                }
                                e.printStackTrace();

                            }

                        }

                        @Override
                        public void onFailure(Call<ApiResponseOperations> call, Throwable t) {
                            progressView.hideLoader();
                            t.printStackTrace();
                        }
                    });
                }
            }, 500);
        }
    }

    private void connectApiToCheckOutGuard() {

        if (CheckNetworkConnection.isConnection1(BulkGuardCheckin.this, true)) {
            progressView.showLoader();
            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastLocation();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    batterPercentage = String.valueOf(Utils.getBatteryPercentage(BulkGuardCheckin.this));

                    Call<ApiResponseOperations> call = apiInterface.guardMultipleCheckOut(
                            PrefData.readStringPref(PrefData.employee_id),
                            PrefData.readStringPref(PrefData.route_id),
                            currentLatitude + "",
                            currentLongitude + "",
                            batterPercentage);

                    call.enqueue(new Callback<ApiResponseOperations>() {
                        @Override
                        public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                            progressView.hideLoader();

                            try {

                                if (response.body().getStatus().equalsIgnoreCase("success")) {

                                    btnEmpCheckin.setText(R.string.checkin);
                                    startActivity(new Intent(BulkGuardCheckin.this, BulkGuardMainActivity.class));
                                    finish();

                                } else {
                                    Utils.showSnackBar(rootEmpCheckin, response.body().getMsg(), BulkGuardCheckin.this);
                                }

                            } catch (Exception e) {
                                if (response.code() == 400) {
                                    Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 500) {
                                    Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 404) {
                                    Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(BulkGuardCheckin.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }
                                e.printStackTrace();

                            }

                        }

                        @Override
                        public void onFailure(Call<ApiResponseOperations> call, Throwable t) {
                            progressView.hideLoader();
                            t.printStackTrace();
                        }
                    });
                }
            }, 500);
        }
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
                Toast.makeText(BulkGuardCheckin.this, R.string.sorry_cant_use, Toast.LENGTH_LONG).show();
                startRequestPermission();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(BulkGuardCheckin.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

}
