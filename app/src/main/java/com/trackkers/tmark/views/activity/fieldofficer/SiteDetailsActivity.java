package com.trackkers.tmark.views.activity.fieldofficer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.fieldofficer.SiteDetailsAdapter;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.trackkers.tmark.adapter.fieldofficer.SiteDetailsAdapter.commentFile;

public class SiteDetailsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SiteDetailsAdapter.MyCallBack {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.tv_subtitle)
    MyTextview tvSubtitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_header)
    MyTextview tvHeader;
    @BindView(R.id.recycler_site_details)
    RecyclerView recyclerSiteDetails;
    @BindView(R.id.swipe_site_details)
    SwipeRefreshLayout swipeSiteDetails;
    @BindView(R.id.empty_view)
    MyTextview emptyView;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    SiteDetailsAdapter mAdapter;
    ArrayList<ApiResponse.DataBean> siteDetails = new ArrayList<>();

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SettingsClient mSettingsClient;
    LocationSettingsRequest mLocationSettingsRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LocationCallback locationCallback;

    public static double currentLatitude = 0.0, currentLongitude = 0.0;
    public static String imagePath = "";
    File file;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9009;
    private static final int REQUEST_CHECK_SETTINGS = 5005;
    public static final int CAMERA_REQUEST = 127;
    public static final int REQUEST_CODE_FOR_IMAGE = 126;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_details);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerSiteDetails.setLayoutManager(manager);
        mAdapter = new SiteDetailsAdapter(SiteDetailsActivity.this, siteDetails, this);
        recyclerSiteDetails.setAdapter(mAdapter);

        swipeSiteDetails.post(new Runnable() {
            @Override
            public void run() {
                swipeSiteDetails.setRefreshing(true);
                connectApiToFetchSiteDetails();
            }
        });
    }

    private void initialize() {
        setSupportActionBar(toolbar);

        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.all_assigned_sites));
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        prefData = new PrefData(SiteDetailsActivity.this);
        apiInterface = ApiClient.getClient(SiteDetailsActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(SiteDetailsActivity.this);

        swipeSiteDetails.setOnRefreshListener(this);
        swipeSiteDetails.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e("locationWrapper", String.valueOf(locationResult.getLastLocation()));
                onLocationChanged(locationResult.getLastLocation());
            }
        };

    }

    private void connectApiToFetchSiteDetails() {
        if (CheckNetworkConnection.isConnection1(SiteDetailsActivity.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.SiteDetails(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();
                    swipeSiteDetails.setRefreshing(false);

                    if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                        siteDetails.clear();
                        siteDetails.addAll(response.body().getData());

                        if (siteDetails.isEmpty()) {
                            recyclerSiteDetails.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("No Site Assigned To You");
                        } else {
                            recyclerSiteDetails.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                        }

                    } else {
                        Toast.makeText(SiteDetailsActivity.this, response.body().getMsg(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    swipeSiteDetails.setRefreshing(false);
                    t.printStackTrace();
                }
            });
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void requestLocation() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(SiteDetailsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(SiteDetailsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                    rae.startResolutionForResult(SiteDetailsActivity.this, REQUEST_CHECK_SETTINGS);
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
    public void onRefresh() {
        swipeSiteDetails.post(new Runnable() {
            @Override
            public void run() {
                swipeSiteDetails.setRefreshing(true);
                connectApiToFetchSiteDetails();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
            if (mLastLocation.getAccuracy() <= 100) {
                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();
                Log.e("currentLatitude", String.valueOf(currentLatitude));
                Log.e("currentLongitude", String.valueOf(currentLongitude));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                file = new File(imagePath);
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream(file));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                    Toast.makeText(this, getResources().getString(R.string.unable_to_fetch_exact_location), Toast.LENGTH_SHORT).show();
                } else {
                    connectApiToStartSiteVisit();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Camera Closed", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_FOR_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                commentFile = new File(SiteDetailsAdapter.commentImagePath);

                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(commentFile.getPath());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream(commentFile));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                mAdapter.notifyItemChanged(Integer.valueOf(PrefData.readStringPref(PrefData.site_attach_image_position)), new SiteDetailsAdapter.SetImage());

            } else {
                Toast.makeText(this, "Camera Closed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectApiToStartSiteVisit() {
        if (CheckNetworkConnection.isConnection1(SiteDetailsActivity.this, true)) {
            progressView.showLoader();

            RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.security_token));
            RequestBody SUID = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.suid));
            RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), currentLatitude + "");
            RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), currentLongitude + "");
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("selfie", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));

            Call<ApiResponse> call = apiInterface.StartSiteVisit(
                    SecurityToken,
                    SUID,
                    Latitude,
                    Longitude,
                    filePart
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();
                    swipeSiteDetails.setRefreshing(false);

                    if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                        Toast.makeText(SiteDetailsActivity.this, getResources().getString(R.string.site_visit_started_successfully), Toast.LENGTH_SHORT).show();

                        swipeSiteDetails.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeSiteDetails.setRefreshing(true);
                                connectApiToFetchSiteDetails();
                            }
                        });

                    } else {
                        Toast.makeText(SiteDetailsActivity.this, response.body().getMsg(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    swipeSiteDetails.setRefreshing(false);
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
            mGoogleApiClient = null;
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void listenerMethod() {
        swipeSiteDetails.post(new Runnable() {
            @Override
            public void run() {
                swipeSiteDetails.setRefreshing(true);
                connectApiToFetchSiteDetails();
            }
        });
    }
}
