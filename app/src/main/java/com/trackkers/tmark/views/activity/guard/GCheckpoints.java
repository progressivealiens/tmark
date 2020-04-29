package com.trackkers.tmark.views.activity.guard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.guard.GuardCheckpointsRecycler;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PictureCapturingListener;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.services.APictureCapturingService;
import com.trackkers.tmark.services.AlarmService;
import com.trackkers.tmark.services.PictureCapturingServiceImpl;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GCheckpoints extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener,
        OnMapReadyCallback,
        PictureCapturingListener {

    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.recycler_guard_checkpoints)
    RecyclerView recyclerGuardCheckpoints;
    @BindView(R.id.tv_checkpoints_instruction)
    LinearLayout tvCheckpointsInstruction;
    @BindView(R.id.btn_checkin)
    Button btnCheckin;
    @BindView(R.id.lin_recycler_layout)
    LinearLayout linRecyclerLayout;
    @BindView(R.id.lin_current_trip)
    LinearLayout linCurrentTrip;
    @BindView(R.id.root_gcheckpoints)
    LinearLayout rootGcheckpoints;
    @BindView(R.id.tv_site_address)
    TextView tvSiteAddress;
    @BindView(R.id.tv_route_start_address)
    TextView tvRouteStartAddress;
    @BindView(R.id.tv_route_end_address)
    TextView tvRouteEndAddress;
    @BindView(R.id.tv_current_trip)
    TextView tvCurrentTrip;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LocationCallback locationCallback;
    LatLng mLatLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    FragmentManager fragmentManager;
    ShutdownReceiver mReceiver;
    PrefData prefData;
    ApiInterface apiInterface;
    public static ProgressView progressView;

    public static IntentIntegrator qrScan;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9009;
    private static final int PERMISSIONS_REQUEST_CODE = 666;
    public static final int NOTIFICATION_CHANNEL_ID = 151;
    public static final int REQUEST_CODE_FOR_CAMERA = 200;

    String scannedCheckpointId = "", scannedCheckpointType = "", batterPercentage = "", pictureFilePath = "";
    public double currentLatitude;
    public double currentLongitude;
    public static boolean isCheckedIn = false;
    File imageFile = null;
    public File imageScannedFile = null;
    public boolean isLiveTrackingEnabled = false, isAlarmRinging = false;
    public static boolean isSpyImageTaken = false, isSpyImageFound = false;
    public static APictureCapturingService pictureService;

    GuardCheckpointsRecycler mAdapter;
    List<ApiResponse.DataBean> guardCheckpointsModels;
    List<ApiResponse.DataBean> guardCheckpointsUpdatedValuesModels;

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
        setContentView(R.layout.activity_gcheckpoints);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        if (startRequestPermission()) {
            fragmentManager = getSupportFragmentManager();
            mFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
            mFragment.getMapAsync(this);
        }

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerGuardCheckpoints.setLayoutManager(manager);
        mAdapter = new GuardCheckpointsRecycler(this, guardCheckpointsModels, this);
        recyclerGuardCheckpoints.setAdapter(mAdapter);

        connectApiToGetGuardPartialDetails();
    }

    private void initialize() {
        linToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.checkpoint);

        qrScan = new IntentIntegrator(GCheckpoints.this);
        prefData = new PrefData(GCheckpoints.this);
        apiInterface = ApiClient.getClient(GCheckpoints.this).create(ApiInterface.class);
        progressView = new ProgressView(GCheckpoints.this);
        guardCheckpointsModels = new ArrayList<>();
        guardCheckpointsUpdatedValuesModels = new ArrayList<>();

        btnCheckin.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pictureService = PictureCapturingServiceImpl.getInstance(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    Log.e("LocationWrapperNull", "LocationWrapperNull");
                    return;
                } else {
                    Log.e("locationWrapper", String.valueOf(locationResult.getLastLocation()));
                    onLocationChanged(locationResult.getLastLocation());
                }
            }
        };
    }

    private boolean startRequestPermission() {

        if (ActivityCompat.checkSelfPermission(GCheckpoints.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(GCheckpoints.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(GCheckpoints.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(GCheckpoints.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void connectApiToGetGuardPartialDetails() {
        if (CheckNetworkConnection.isConnection1(GCheckpoints.this, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.GetGuardPartialDetails(
                    PrefData.readStringPref(PrefData.security_token),
                    PrefData.readStringPref(PrefData.route_id));

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                if (response.body().isIsCheckedIn()) {
                                    PrefData.writeStringPref(PrefData.employee_id, String.valueOf(response.body().getEmployeeId()));
                                    btnCheckin.setText(getString(R.string.checkout));
                                    if (response.body().getFlag().equalsIgnoreCase(getString(R.string.scan))) {

                                        isCheckedIn = true;
                                        //registerShutdownReceiver();

                                        guardCheckpointsModels.clear();
                                        guardCheckpointsModels.addAll(response.body().getData());

                                        mAdapter.notifyDataSetChanged();

                                        linRecyclerLayout.setVisibility(View.VISIBLE);
                                        linCurrentTrip.setVisibility(View.VISIBLE);

                                        tvSiteAddress.setText(response.body().getSiteAddress());
                                        tvRouteStartAddress.setText(response.body().getRouteStartAddress());
                                        tvRouteEndAddress.setText(response.body().getRouteEndAddress());
                                        tvCurrentTrip.setText(String.valueOf(response.body().getCurrentRound()));
                                        isLiveTrackingEnabled = response.body().isIsLiveTracking();


                                        PrefData.writeStringPref(PrefData.current_trip, String.valueOf(response.body().getCurrentRound()));
                                        PrefData.writeLongPref(PrefData.checkin_time, Utils.fromDateToMillis(response.body().getCheckInTime()));
                                        PrefData.writeStringPref(PrefData.timeInterval, String.valueOf(response.body().getAlarmInterval()));

                                        if (isLiveTrackingEnabled) {
                                            scheduleTracking();
                                        }

                                        scheduleAlarm();


                                    } else if (response.body().getFlag().equalsIgnoreCase(getString(R.string.noscan))) {

                                        tvSiteAddress.setText(response.body().getSiteAddress());
                                        tvRouteStartAddress.setText(response.body().getRouteStartAddress());
                                        tvRouteEndAddress.setText(response.body().getRouteEndAddress());
                                        isLiveTrackingEnabled = response.body().isIsLiveTracking();

                                        if (isLiveTrackingEnabled) {
                                            scheduleTracking();
                                        }
                                        scheduleAlarm();

                                        linCurrentTrip.setVisibility(View.GONE);
                                        linRecyclerLayout.setVisibility(View.GONE);

                                        isCheckedIn = true;
                                        //registerShutdownReceiver();
                                    }
                                } else {
                                    btnCheckin.setText(getString(R.string.check_in));
                                    PrefData.writeStringPref(PrefData.employee_id, String.valueOf(response.body().getEmployeeId()));
                                    tvSiteAddress.setText(response.body().getSiteAddress());
                                    tvRouteStartAddress.setText(response.body().getRouteStartAddress());
                                    tvRouteEndAddress.setText(response.body().getRouteEndAddress());
                                    linCurrentTrip.setVisibility(View.GONE);
                                    linRecyclerLayout.setVisibility(View.GONE);
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(GCheckpoints.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(GCheckpoints.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(GCheckpoints.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    private void connectApiToCheckInGuard() {

        if (CheckNetworkConnection.isConnection1(GCheckpoints.this, true)) {
            progressView.showLoader();

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("selfie", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile));
            RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), currentLatitude + "");
            RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), currentLongitude + "");
            RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token) + "");
            RequestBody RouteId = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.route_id) + "");
            RequestBody DeviceId = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.deviceID) + "");
            RequestBody Battery = RequestBody.create(MediaType.parse("text/plain"), Utils.getBatteryPercentage(GCheckpoints.this) + "");

            Call<ApiResponse> call = apiInterface.GuardCheckIn(
                    SecurityToken,
                    RouteId,
                    filePart,
                    Latitude,
                    Longitude,
                    DeviceId,
                    Battery
            );
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                Utils.showToast(GCheckpoints.this, getResources().getString(R.string.checkin_successfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                btnCheckin.setText("Check Out");

                                if (response.body().getFlag().equalsIgnoreCase("Scan")) {
                                    isCheckedIn = true;
                                    //registerShutdownReceiver();

                                    guardCheckpointsModels.clear();
                                    guardCheckpointsModels.addAll(response.body().getData());

                                    mAdapter.notifyDataSetChanged();

                                    linRecyclerLayout.setVisibility(View.VISIBLE);
                                    linCurrentTrip.setVisibility(View.VISIBLE);

                                    tvCurrentTrip.setText(String.valueOf(response.body().getCurrentRound()));

                                    isLiveTrackingEnabled = response.body().isIsLiveTracking();

                                    PrefData.writeStringPref(PrefData.current_trip, String.valueOf(response.body().getCurrentRound()));
                                    PrefData.writeLongPref(PrefData.checkin_time, Utils.fromDateToMillis(response.body().getCheckInTime()));
                                    PrefData.writeStringPref(PrefData.timeInterval, String.valueOf(response.body().getAlarmInterval()));
                                    PrefData.writeStringPref(PrefData.route_id_service, PrefData.readStringPref(PrefData.route_id));
                                    PrefData.writeStringPref(PrefData.route_name_service, PrefData.readStringPref(PrefData.route_name));

                                    if (isLiveTrackingEnabled) {
                                        scheduleTracking();
                                    }
                                    scheduleAlarm();

                                } else {
                                    isCheckedIn = true;
                                    //registerShutdownReceiver();

                                    PrefData.writeStringPref(PrefData.route_id_service, PrefData.readStringPref(PrefData.route_id));
                                    PrefData.writeStringPref(PrefData.route_name_service, PrefData.readStringPref(PrefData.route_name));

                                    if (isLiveTrackingEnabled) {
                                        scheduleTracking();
                                    }
                                    scheduleAlarm();

                                    linRecyclerLayout.setVisibility(View.GONE);
                                    linCurrentTrip.setVisibility(View.GONE);
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(GCheckpoints.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(GCheckpoints.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(GCheckpoints.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            sendTakePictureIntent();
        } else {
            Utils.showToast(GCheckpoints.this, "Your Phone doesn\'t have camera", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }
    }

    private void sendTakePictureIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFile = null;
        try {
            pictureFile = getPictureFile(GCheckpoints.this);
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.trackkers.tmark.fileprovider", pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CODE_FOR_CAMERA);
            }
        } catch (IOException ex) {
            Utils.showToast(GCheckpoints.this, "Photo file can't be created, please try again", Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
        }
    }

    private File getPictureFile(Context context) throws IOException {
        String timeStamp = Utils.currentTimeStamp();
        File storageDir = context.getExternalFilesDir(null);
        File image = File.createTempFile(timeStamp, ".png", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    private void scheduleAlarm() {
        isAlarmRinging = true;
        AlarmService.setAlarm(true, GCheckpoints.this);
    }

    private void scheduleTracking() {
        startService(new Intent(this, AlarmService.class));
    }

    private void registerShutdownReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        mReceiver = new ShutdownReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void unregisterShutdownReceiver() {
        this.unregisterReceiver(mReceiver);
    }

    private void ConnectApiToVerifyGuardScanCheckpoint(String scannedCheckpointId, String scannedCheckpointType, boolean isFilePresent) {
        if (CheckNetworkConnection.isConnection1(GCheckpoints.this, true)) {

            MultipartBody.Part filePart = null;
            if (isFilePresent) {
                filePart = MultipartBody.Part.createFormData("guardScanImage", imageScannedFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageScannedFile));
            } else {
                filePart = MultipartBody.Part.createFormData("guardScanImage", "", RequestBody.create(MediaType.parse("text/plain"), ""));
            }

            RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), currentLatitude + "");
            RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), currentLongitude + "");
            RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token) + "");
            RequestBody CheckpointId = RequestBody.create(MediaType.parse("text/plain"), scannedCheckpointId);
            RequestBody DeviceId = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.deviceID) + "");
            RequestBody Battery = RequestBody.create(MediaType.parse("text/plain"), Utils.getBatteryPercentage(GCheckpoints.this) + "");

            Call<ApiResponse> call = apiInterface.GuardScanCheckpoint(
                    SecurityToken,
                    CheckpointId,
                    DeviceId,
                    Latitude,
                    Longitude,
                    Battery,
                    filePart);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                tvCurrentTrip.setText(String.valueOf(response.body().getCurrentRound()));
                                guardCheckpointsModels.get(Integer.valueOf(PrefData.readStringPref(PrefData.checkpoint_id_position))).setIsVerified(true);

                                if (Integer.valueOf(PrefData.readStringPref(PrefData.current_trip)) < response.body().getCurrentRound()) {
                                    for (int i = 0; i < guardCheckpointsModels.size(); i++) {
                                        guardCheckpointsModels.get(i).setIsVerified(false);
                                    }
                                    PrefData.writeStringPref(PrefData.current_trip, String.valueOf(response.body().getCurrentRound()));
                                    Utils.showToast(GCheckpoints.this, "Trip " + (Integer.valueOf(PrefData.readStringPref(PrefData.current_trip)) - 1) + " Completed. New Trip Started", Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                } else {
                                    Utils.showToast(GCheckpoints.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                }
                                mAdapter.notifyDataSetChanged();
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(GCheckpoints.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(GCheckpoints.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(GCheckpoints.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
        } else {
            progressView.hideLoader();
        }
    }

    private void connectApiToCheckOutGuard() {

        if (CheckNetworkConnection.isConnection1(GCheckpoints.this, true)) {

            progressView.showLoader();

            batterPercentage = String.valueOf(Utils.getBatteryPercentage(GCheckpoints.this));

            Call<ApiResponse> call = apiInterface.GuardCheckOut(
                    PrefData.readStringPref(PrefData.security_token),
                    PrefData.readStringPref(PrefData.route_id),
                    currentLatitude + "",
                    currentLongitude + "",
                    batterPercentage);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                isCheckedIn = false;

                                //unregisterShutdownReceiver();

                                btnCheckin.setText(getString(R.string.check_in));
                                Utils.showToast(GCheckpoints.this, getResources().getString(R.string.checkout_sucessfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                dismissAlarmAndTracking();
                                startActivity(new Intent(GCheckpoints.this, GMainActivity.class));
                                finish();

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(GCheckpoints.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(GCheckpoints.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(GCheckpoints.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(GCheckpoints.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    private void dismissAlarmAndTracking() {

        if (isLiveTrackingEnabled) {
            AlarmService.removeDatabaseValues();
            AlarmService.stopLocationUpdate();
            Utils.stopAlarmService(GCheckpoints.this);
            AlarmService.cancelNotification(GCheckpoints.this, NOTIFICATION_CHANNEL_ID);
            stopService(new Intent(GCheckpoints.this, AlarmService.class));
        }

        if (isAlarmRinging) {
            AlarmService.cancelAlarm(GCheckpoints.this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                progressView.hideLoader();
                Utils.showToast(GCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            } else {
                try {
                    JSONObject jObj = new JSONObject(result.getContents());
                    scannedCheckpointId = jObj.getString("id");
                    scannedCheckpointType = jObj.getString("type");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (scannedCheckpointId.equalsIgnoreCase(PrefData.readStringPref(PrefData.checkpoint_id)) && scannedCheckpointType.equalsIgnoreCase("checkpoint")) {
                    if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                        progressView.hideLoader();
                        Utils.showToast(GCheckpoints.this, getResources().getString(R.string.unable_to_fetch), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                    } else {
                        ConnectApiToVerifyGuardScanCheckpoint(scannedCheckpointId, scannedCheckpointType, isSpyImageFound);
                    }
                } else {
                    progressView.hideLoader();
                    Utils.showToast(GCheckpoints.this, getResources().getString(R.string.wrong_qr), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                }
            }
        }

        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Log.e("gpsSuccess", "gpsSuccess");
                        break;
                    case Activity.RESULT_CANCELED:
                        //Log.e("gpsFail", "gpsFail");
                        break;
                    default:
                        break;
                }
                break;
        }

        if (requestCode == REQUEST_CODE_FOR_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                imageFile = new File(pictureFilePath);

                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream(imageFile));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                getLocationFromService(0);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.showToast(GCheckpoints.this, getResources().getString(R.string.camera_closed), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i("tag", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                fragmentManager = getSupportFragmentManager();
                mFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
                mFragment.getMapAsync(this);

                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED) {
                Utils.showToast(GCheckpoints.this, getResources().getString(R.string.sorry_cant_use), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                startRequestPermission();
            }
        }
    }

    private void getLocationFromService(final int Flag) {
        if (Flag == 0) {

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastLocation();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectApiToCheckInGuard();
                    }
                }, 500);

            } else {
                connectApiToCheckInGuard();
            }
        } /*else if (Flag == 1) {
            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                Utils.showToast(GCheckpoints.this, getResources().getString(R.string.unable_to_fetch), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSpyImageTaken = false;
                        progressView.showLoader();
                        pictureService.startCapturing(GCheckpoints.this);
                    }
                }, 200);
            }
        } */ else if (Flag == 2) {

            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastLocation();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectApiToCheckOutGuard();
                    }
                }, 500);

            } else {
                connectApiToCheckOutGuard();
            }
        }
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
    public void onConnected(Bundle bundle) {
        /*requestLocation();*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
            if (mLastLocation.getAccuracy() <= 50) {
                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();

                if (mGoogleMap != null) {
                    mGoogleMap.clear();
                    mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(mLatLng).title(getResources().getString(R.string.you_r_here));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
                    mGoogleMap.addMarker(markerOptions);
                }
            }
        }
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
                                    GCheckpoints.this,
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

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(GCheckpoints.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_checkin:
                if (btnCheckin.getText().toString().equalsIgnoreCase(getString(R.string.check_in))) {
                    checkIn();
                } else if (btnCheckin.getText().toString().equalsIgnoreCase(getString(R.string.checkout))) {
                    getLocationFromService(2);
                }
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
        }
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
        /*if (fusedLocationProviderClient != null) {

        }*/

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }*/
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (!isSpyImageTaken) {
            if (pictureData != null && pictureUrl != null) {
                imageScannedFile = new File(pictureUrl);
                //ConnectApiToVerifyGuardScanCheckpoint(true);
                Utils.showToast(this, getResources().getString(R.string.volume_up_for_torch), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                qrScan.setOrientationLocked(true);
                qrScan.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = true;
            } else {
                //ConnectApiToVerifyGuardScanCheckpoint(false);
                Utils.showToast(this, getResources().getString(R.string.volume_up_for_torch), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                qrScan.setOrientationLocked(true);
                qrScan.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = false;
            }
        }
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (!isSpyImageTaken) {
            if (picturesTaken != null && !picturesTaken.isEmpty()) {
                imageScannedFile = new File(picturesTaken.lastEntry().getKey());
                Utils.showToast(this, getResources().getString(R.string.volume_up_for_torch), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                qrScan.setOrientationLocked(true);
                qrScan.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = true;
                /*ConnectApiToVerifyGuardScanCheckpoint(true);
                isSpyImageTaken = true;*/
            } else {
                Utils.showToast(this, getResources().getString(R.string.volume_up_for_torch), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                qrScan.setOrientationLocked(true);
                qrScan.initiateScan();
                isSpyImageTaken = true;
                isSpyImageFound = false;
                /*ConnectApiToVerifyGuardScanCheckpoint(false);
                isSpyImageTaken = true;*/
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mGoogleMap = googleMap;
            mGoogleMap.clear();
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public class ShutdownReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isCheckedIn) {

                if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    PrefData.writeStringPref(PrefData.switchoff_time, Utils.currentTimeStamp());
                    PrefData.writeStringPref(PrefData.switchoff_battery_status, String.valueOf(Utils.getBatteryPercentage(context)));

                    Log.e("currentTime", PrefData.readStringPref(PrefData.switchoff_time));
                    Log.e("batteryStatus", PrefData.readStringPref(PrefData.switchoff_battery_status));
                }
            }
        }
    }

}
