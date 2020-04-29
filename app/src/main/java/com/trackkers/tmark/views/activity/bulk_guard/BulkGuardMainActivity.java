package com.trackkers.tmark.views.activity.bulk_guard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.bulk_guard.BulkGuardRecycler;
import com.trackkers.tmark.customviews.CustomTypefaceSpan;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BulkGuardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    @BindView(R.id.tv_site_all_guard)
    MyTextview tvSiteAllGuard;
    @BindView(R.id.recycler_all_guard)
    RecyclerView recyclerAllGuard;
    @BindView(R.id.swipe_container_guard)
    SwipeRefreshLayout swipeContainerGuard;
    @BindView(R.id.root_employee_main)
    LinearLayout rootEmployeeMain;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.switch_language)
    SwitchCompat switchLanguage;

    public TextInputEditText etSearchName;

    BulkGuardRecycler mAdapter;
    ApiInterface apiInterface;
    ProgressView progressView;
    PrefData prefData;

    String languageToLoad = "", searchedName = "", siteName = "", siteNameCapitalLetter = "";
    public static String pictureFilePathCheckin = "";
    File imageFile = null;

    public static double currentLatitude, currentLongitude;

    public List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails;
    public List<ApiResponseOperations.DataBean.EmployeesBean> searchedResult;

    public static final int REQUEST_CODE_FOR_BACK_CAMERA = 201;
    private static final int REQUEST_CHECK_SETTINGS = 5004;
    private static final int PERMISSIONS_REQUEST_CODE = 666;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9008;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SettingsClient mSettingsClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    LocationCallback locationCallback;

    public static boolean isReloadNeeded=false;
    boolean isDataSearched=false;

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
        setContentView(R.layout.main_layout_employee);
        ButterKnife.bind(this);

        initialize();

        buildGoogleApiClient();

        Menu m = navView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
            //for applying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            applyFontToMenuItem(mi);
        }

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerAllGuard.setLayoutManager(manager);
        mAdapter = new BulkGuardRecycler(this, employeeDetails,isDataSearched);
        recyclerAllGuard.setAdapter(mAdapter);

        swipeContainerGuard.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerGuard.setRefreshing(true);
                connectApiToLoginOperations();
            }
        });


        etSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchedName = s.toString().toLowerCase().trim();
                searchedResult.clear();

                for (int i = 0; i < employeeDetails.size(); i++) {
                    if (employeeDetails.get(i).getEmployeeName().toLowerCase().contains(searchedName) || employeeDetails.get(i).getEmpCode().toLowerCase().contains(searchedName)) {
                        searchedResult.add(employeeDetails.get(i));
                    }
                }

                LinearLayoutManager manager = new LinearLayoutManager(BulkGuardMainActivity.this);
                recyclerAllGuard.setLayoutManager(manager);

                if (count == 0) {
                    isDataSearched=false;
                    mAdapter = new BulkGuardRecycler(BulkGuardMainActivity.this, employeeDetails,isDataSearched);
                } else {
                    isDataSearched=true;
                    mAdapter = new BulkGuardRecycler(BulkGuardMainActivity.this, searchedResult,isDataSearched);
                }
                recyclerAllGuard.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSearchName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etSearchName.getRight() - etSearchName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        etSearchName.setText("");

                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initialize() {
        setSupportActionBar(toolbar);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.home_caps));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        prefData = new PrefData(BulkGuardMainActivity.this);
        navView.setNavigationItemSelectedListener(this);
        apiInterface = ApiClient.getClient(BulkGuardMainActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(BulkGuardMainActivity.this);
        employeeDetails = new ArrayList<>();
        searchedResult = new ArrayList<>();
        swipeContainerGuard.setOnRefreshListener(this);
        etSearchName = findViewById(R.id.et_search_name);
        swipeContainerGuard.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);
        tvSiteAllGuard.setText(PrefData.readStringPref(PrefData.site_name) + " - " + getString(R.string.all_guard_list));

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

                    startActivity(new Intent(BulkGuardMainActivity.this, BulkGuardMainActivity.class));
                    finish();
                } else {

                    languageToLoad = "en";
                    Locale locale = new Locale(languageToLoad);
                    PrefData.writeStringPref(PrefData.PREF_selected_language, languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                    startActivity(new Intent(BulkGuardMainActivity.this, BulkGuardMainActivity.class));
                    finish();
                }
            }
        });


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

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "aver_bold.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(BulkGuardMainActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean startRequestPermission() {

        if (ActivityCompat.checkSelfPermission(BulkGuardMainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(BulkGuardMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(BulkGuardMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(BulkGuardMainActivity.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void connectApiToLoginOperations() {
        if (CheckNetworkConnection.isConnection1(BulkGuardMainActivity.this, true)) {
            progressView.showLoader();

            Call<ApiResponseOperations> call = apiInterface.MultipleGuardsLogin(
                    PrefData.readStringPref(PrefData.company_email),
                    PrefData.readStringPref(PrefData.site_code),
                    PrefData.readStringPref(PrefData.route_code));

            call.enqueue(new Callback<ApiResponseOperations>() {
                @Override
                public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                swipeContainerGuard.setRefreshing(false);

                                PrefData.writeStringPref(PrefData.route_id, String.valueOf(response.body().getData().get(0).getRouteId()));
                                PrefData.writeStringPref(PrefData.route_name, response.body().getData().get(0).getRouteName());
                                PrefData.writeStringPref(PrefData.route_start_address, response.body().getData().get(0).getRouteStartAddress());
                                PrefData.writeStringPref(PrefData.route_end_address, response.body().getData().get(0).getRouteEndAddress());

                                siteName = response.body().getData().get(0).getSiteName();
                                siteNameCapitalLetter = siteName.substring(0, 1).toUpperCase() + siteName.substring(1);

                                PrefData.writeStringPref(PrefData.site_name, siteNameCapitalLetter);

                                employeeDetails.clear();
                                employeeDetails.addAll(response.body().getData().get(0).getEmployees());
                                mAdapter.notifyDataSetChanged();
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(BulkGuardMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(BulkGuardMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    @Override
    public void onRefresh() {
        swipeContainerGuard.setRefreshing(true);
        connectApiToLoginOperations();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_home) {
            drawerLayout.closeDrawers();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(BulkGuardMainActivity.this, BulkGuardHistory.class));
        } /*else if (id == R.id.nav_resetpassword) {
            startActivity(new Intent(BulkGuardMainActivity.this, ResetPassword.class));
        } */ else if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.nav_rate_us) {
            rateUs();
        } else if (id == R.id.nav_logout) {
            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.logout_sucessfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
            Utils.logout(BulkGuardMainActivity.this, LoginActivity.class);
            //connectApiToLogoutGuard();
        }

        drawerLayout = findViewById(R.id.drawer_layout);
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
            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.unable_to_find), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
                Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.sorry_cant_use), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                startRequestPermission();
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && fusedLocationProviderClient != null) {
            requestLocation();
        } else {
            buildGoogleApiClient();
        }

        if (isReloadNeeded){
            onRefresh();
        }
        isReloadNeeded = false;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e("gpsSucess", "gpsSucess");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.enable_gps), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                finish();
            }
        } else if (requestCode == REQUEST_CODE_FOR_BACK_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                isReloadNeeded=false;
                progressView.showLoader();
                imageFile = new File(pictureFilePathCheckin);

                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                bitmap=Utils.watermarkOnImage(BulkGuardMainActivity.this,bitmap);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rotateBitmap(bitmap, imageFile.getPath(), imageFile);
                } else {
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, new FileOutputStream(imageFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                connectApiToCheckInGuard();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.camera_closed), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
            }
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
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, new FileOutputStream(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectApiToCheckInGuard() {
        if (CheckNetworkConnection.isConnection1(BulkGuardMainActivity.this, true)) {
            if (String.valueOf(currentLatitude).equalsIgnoreCase("0.0") && String.valueOf(currentLongitude).equalsIgnoreCase("0.0")) {
                getLastLocation();
            }

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("selfie", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile));
            RequestBody Latitude = RequestBody.create(MediaType.parse("text/plain"), currentLatitude + "");
            RequestBody Longitude = RequestBody.create(MediaType.parse("text/plain"), currentLongitude + "");
            RequestBody SecurityToken = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.employee_id) + "");
            RequestBody RouteId = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.route_id) + "");
            RequestBody DeviceId = RequestBody.create(MediaType.parse("text/plain"), PrefData.readStringPref(PrefData.deviceID) + "");
            RequestBody Battery = RequestBody.create(MediaType.parse("text/plain"), Utils.getBatteryPercentage(BulkGuardMainActivity.this) + "");

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
                @Override
                public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                    progressView.hideLoader();
                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                                if (isDataSearched){
                                    onRefresh();
                                }else{
                                    ApiResponseOperations.DataBean.EmployeesBean data = new ApiResponseOperations.DataBean.EmployeesBean(PrefData.readStringPref(PrefData.employee_name), Integer.valueOf(PrefData.readStringPref(PrefData.employee_id)), PrefData.readStringPref(PrefData.employee_code), true);
                                    employeeDetails.set(Integer.valueOf(PrefData.readStringPref(PrefData.e_register_checkin_position)), data);
                                    mAdapter.notifyDataSetChanged();
                                }

                                Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.checkin_sucessfull), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));
                                if (!etSearchName.getText().toString().equalsIgnoreCase("")) {
                                    etSearchName.setText("");
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {

                                    Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(BulkGuardMainActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(BulkGuardMainActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(BulkGuardMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
                    rae.startResolutionForResult(BulkGuardMainActivity.this, REQUEST_CHECK_SETTINGS);
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
}
