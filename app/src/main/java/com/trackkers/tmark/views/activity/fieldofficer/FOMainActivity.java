package com.trackkers.tmark.views.activity.fieldofficer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.CustomTypefaceSpan;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.ProfileActivity;
import com.trackkers.tmark.views.activity.ResetPassword;
import com.trackkers.tmark.views.activity.operations.ViewDocuments;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FOMainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.card_mark_attendance)
    CardView cardMarkAttendance;
    @BindView(R.id.card_verify_route)
    CardView cardVerifyRoute;
    @BindView(R.id.card_add_new_guard)
    CardView cardAddNewGuard;
    @BindView(R.id.card_assign_unassign_guard)
    CardView cardAssignUnassignGuard;
    @BindView(R.id.card_site_details)
    CardView cardSiteDetails;
    @BindView(R.id.iv_company_logo)
    ImageView ivCompanyLogo;
    @BindView(R.id.switch_language)
    SwitchCompat switchLanguage;
    @BindView(R.id.tv_welcome)
    MyTextview tvWelcome;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    MyTextview companyName, employeeType;
    ImageView drawerProfile;

    public static final int NOTIFICATION_CHANNEL_ID = 151;
    private static final int PERMISSIONS_REQUEST_CODE = 666;

    public static String purposeClass = "";
    String languageToLoad = "";

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
        setContentView(R.layout.activity_fomain);
        ButterKnife.bind(this);

        initialize();

        connectApiToFetchProfileDetails();

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

    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "aver_bold.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void initialize() {
        setSupportActionBar(toolbar);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.home_caps));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        View headerView = navView.getHeaderView(0);
        companyName = headerView.findViewById(R.id.drawer_company_name);
        employeeType = headerView.findViewById(R.id.drawer_employee_type);
        drawerProfile = headerView.findViewById(R.id.drawer_profile);

        companyName.setText(PrefData.readStringPref(PrefData.company_name));
        employeeType.setText(PrefData.readStringPref(PrefData.employee_type));
        Picasso.get().load(Utils.BASE_IMAGE_COMPANY + PrefData.readStringPref(PrefData.company_logo)).placeholder(R.drawable.progress_animation).into(drawerProfile);

        prefData = new PrefData(FOMainActivity.this);
        apiInterface = ApiClient.getClient(FOMainActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(FOMainActivity.this);
        Picasso.get().load(Utils.BASE_IMAGE_COMPANY + PrefData.readStringPref(PrefData.company_logo)).placeholder(R.drawable.progress_animation).into(ivCompanyLogo);

        cardMarkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FOMainActivity.this, FOMarkAttendance.class));
            }
        });

        cardVerifyRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FOMainActivity.this, VerifyRoute.class);
                intent.putExtra(purposeClass, "checkpoints");
                startActivity(intent);
            }
        });

        cardAddNewGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FOMainActivity.this, AddNewGuard.class));
            }
        });

        cardAssignUnassignGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FOMainActivity.this, VerifyRoute.class);
                intent.putExtra(purposeClass, "assignment");
                startActivity(intent);
            }
        });

        cardSiteDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FOMainActivity.this,SiteDetailsActivity.class));
            }
        });

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

                    startActivity(new Intent(FOMainActivity.this, FOMainActivity.class));
                    finish();
                } else {

                    languageToLoad = "en";
                    Locale locale = new Locale(languageToLoad);
                    PrefData.writeStringPref(PrefData.PREF_selected_language, languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                    startActivity(new Intent(FOMainActivity.this, FOMainActivity.class));
                    finish();
                }

            }
        });

    }

    private void connectApiToFetchProfileDetails() {
        if (CheckNetworkConnection.isConnection1(FOMainActivity.this, true)) {
            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.Profile(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();
                    try {
                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            PrefData.writeStringPref(PrefData.employee_name, response.body().getData().get(0).getName());
                            PrefData.writeStringPref(PrefData.employee_id, String.valueOf(response.body().getData().get(0).getEmployeeId()));
                            PrefData.writeStringPref(PrefData.employee_code, response.body().getData().get(0).getEmpCode());

                            tvWelcome.setText(getResources().getString(R.string.welcome) + ", " + PrefData.readStringPref(PrefData.employee_name));
                        } else {
                            Utils.showSnackBar(drawerLayout, response.body().getMsg(), FOMainActivity.this);
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                        }
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    t.printStackTrace();
                    progressView.hideLoader();
                }
            });
        }
    }

    private boolean startRequestPermission() {

        if (ActivityCompat.checkSelfPermission(FOMainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(FOMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(FOMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(FOMainActivity.this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);

            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_home) {
            drawerLayout.closeDrawers();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(FOMainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_attendance) {
            startActivity(new Intent(FOMainActivity.this, FOMarkAttendance.class));
        } else if (id == R.id.nav_verification_history) {
            startActivity(new Intent(FOMainActivity.this, AssignedHistory.class));
        } else if (id == R.id.nav_show_docs) {
            startActivity(new Intent(FOMainActivity.this, ViewDocuments.class));
        } else if (id == R.id.nav_resetpassword) {
            startActivity(new Intent(FOMainActivity.this, ResetPassword.class));
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
            Toast.makeText(this, getResources().getString(R.string.unable_to_find), Toast.LENGTH_LONG).show();
        }
    }

    private void connectApiToLogoutGuard() {
        if (CheckNetworkConnection.isConnection1(FOMainActivity.this, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.logoutEmp(
                    PrefData.readStringPref(PrefData.security_token)
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                            Toast.makeText(FOMainActivity.this, R.string.logout_sucessfull, Toast.LENGTH_SHORT).show();

                            logout();
                        } else {
                            Utils.showSnackBar(drawerLayout, response.body().getMsg(), FOMainActivity.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FOMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    private void logout() {
        PrefData.writeBooleanPref(PrefData.PREF_LOGINSTATUS, false);

        Intent intent = new Intent(FOMainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i("permissionProblem", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(FOMainActivity.this, R.string.sorry_cant_use, Toast.LENGTH_LONG).show();
                startRequestPermission();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        super.onBackPressed();
    }

}

