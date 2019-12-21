package com.trackkers.tmark.views.activity.fieldofficer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.fieldofficer.FOCheckpointsRecycler;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FOCheckpoints extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_fo_checkpoints)
    RecyclerView recyclerFoCheckpoints;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.root_checkpoints)
    LinearLayout rootCheckpoints;
    @BindView(R.id.tv_site_address)
    TextView tvSiteAddress;
    @BindView(R.id.tv_route_start_address)
    TextView tvRouteStartAddress;
    @BindView(R.id.tv_route_end_address)
    TextView tvRouteEndAddress;

    FOCheckpointsRecycler mAdapter;
    List<ApiResponse.DataBean> foCheckpointsModels;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    String scannedCheckpointId = "",scannedCheckpointType="";

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LatLng mLatLng;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    FragmentManager fragmentManager;
    LocationCallback locationCallback;

    private double currentLatitude;
    private double currentLongitude;

    public static IntentIntegrator qrScanFO;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9009;
    private static final int PERMISSIONS_REQUEST_CODE = 666;

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
        setContentView(R.layout.activity_fo_checkpoints);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        if (startRequestPermission()) {
            fragmentManager = getSupportFragmentManager();
            mFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
            mFragment.getMapAsync(this);
        }


        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerFoCheckpoints.setLayoutManager(manager);
        mAdapter = new FOCheckpointsRecycler(this, foCheckpointsModels);
        recyclerFoCheckpoints.setAdapter(mAdapter);

        connectApiToFetchAllCheckpoints();
    }

    private void initialize() {
        linToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.checkpoint);

        prefData = new PrefData(FOCheckpoints.this);
        apiInterface = ApiClient.getClient(FOCheckpoints.this).create(ApiInterface.class);
        progressView = new ProgressView(FOCheckpoints.this);
        foCheckpointsModels = new ArrayList<>();
        foCheckpointsModels.clear();

        qrScanFO = new IntentIntegrator(FOCheckpoints.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("locationWrapper", String.valueOf(locationResult.getLastLocation()));
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void connectApiToFetchAllCheckpoints() {

        if (CheckNetworkConnection.isConnection1(FOCheckpoints.this, true)) {

            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.AllCheckpoints(
                    PrefData.readStringPref(PrefData.security_token),
                    PrefData.readStringPref(PrefData.route_id));

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            foCheckpointsModels.clear();
                            foCheckpointsModels.addAll(response.body().getData());

                            mAdapter.notifyDataSetChanged();

                            tvSiteAddress.setText(response.body().getSiteAddress());
                            tvRouteStartAddress.setText(response.body().getRouteStartAddress());
                            tvRouteEndAddress.setText(response.body().getRouteEndAddress());
                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(FOCheckpoints.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootCheckpoints, response.body().getMsg(), FOCheckpoints.this);
                            }

                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

        if (ActivityCompat.checkSelfPermission(FOCheckpoints.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(FOCheckpoints.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(FOCheckpoints.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(FOCheckpoints.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                break;
        }


        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getString(R.string.result_not_found), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jObj = new JSONObject(result.getContents());
                    scannedCheckpointId = jObj.getString("id");
                    scannedCheckpointType = jObj.getString("type");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (scannedCheckpointId.equalsIgnoreCase(PrefData.readStringPref(PrefData.checkpoint_id)) && scannedCheckpointType.equalsIgnoreCase("checkpoint")) {

                    connectApiToVerifyCheckpoint();

                } else {
                    Toast.makeText(this, R.string.wrong_qr, Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void connectApiToVerifyCheckpoint() {
        if (CheckNetworkConnection.isConnection1(FOCheckpoints.this, true)) {
            progressView.showLoader();
            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                Toast.makeText(this, R.string.unable_to_fetch, Toast.LENGTH_SHORT).show();

                progressView.hideLoader();
            } else {

                Call<ApiResponse> call = apiInterface.VerifyCheckpoint(
                        PrefData.readStringPref(PrefData.security_token),
                        PrefData.readStringPref(PrefData.checkpoint_id),
                        currentLatitude + "",
                        currentLongitude + ""
                );

                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                        progressView.hideLoader();

                        try {

                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                Toast.makeText(FOCheckpoints.this, R.string.chk_verified, Toast.LENGTH_SHORT).show();

                                foCheckpointsModels.clear();
                                foCheckpointsModels.addAll(response.body().getData());

                                tvSiteAddress.setText(response.body().getSiteAddress());
                                tvRouteStartAddress.setText(response.body().getRouteStartAddress());
                                tvRouteEndAddress.setText(response.body().getRouteEndAddress());

                                mAdapter.notifyDataSetChanged();

                            }  else {

                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                    Utils.logout(FOCheckpoints.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootCheckpoints, response.body().getMsg(), FOCheckpoints.this);
                                }
                            }

                        } catch (Exception e) {
                            if (response.code() == 400) {
                                Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 500) {
                                Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 404) {
                                Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(FOCheckpoints.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onConnected(Bundle bundle) {
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

        Log.e("onLOcationChanged",location.toString());
        if (mLastLocation != null && mLastLocation.hasAccuracy()) {
            if (mLastLocation.getAccuracy() <= 30) {
                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();



                if (mGoogleMap != null) {
                    mGoogleMap.clear();

                    mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(mLatLng).title(getString(R.string.you_r_here));
                    CameraUpdate cameraUpdate =CameraUpdateFactory.newLatLngZoom(mLatLng,15);
                    mGoogleMap.animateCamera(cameraUpdate);
                    mGoogleMap.addMarker(markerOptions);

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mGoogleMap = googleMap;
            mGoogleMap.clear();
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
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
                                    FOCheckpoints.this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("tag", "onRequestPermissionResult");
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
                Toast.makeText(FOCheckpoints.this, R.string.sorry_cant_use, Toast.LENGTH_LONG).show();
                startRequestPermission();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(FOCheckpoints.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

}
