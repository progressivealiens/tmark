package com.trackkers.tmark.views.activity.guard;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.guard.AssignedRouteRecycler;
import com.trackkers.tmark.customviews.CustomTypefaceSpan;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.services.AlarmService;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.ProfileActivity;
import com.trackkers.tmark.views.activity.ResetPassword;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.trackkers.tmark.views.activity.guard.GCheckpoints.NOTIFICATION_CHANNEL_ID;

public class GMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.recycler_route_guard)
    RecyclerView recyclerRouteGuard;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.swipe_container_guard)
    SwipeRefreshLayout swipeContainerGuard;
    @BindView(R.id.switch_language)
    SwitchCompat switchLanguage;

    MyTextview companyName, employeeType;
    ImageView drawerProfile;

    AssignedRouteRecycler mAdapter;
    List<ApiResponse.DataBean> assignedRouteModels;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    String languageToLoad = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmain);
        ButterKnife.bind(this);

        initialize();

        Menu m = navView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerRouteGuard.setLayoutManager(manager);
        mAdapter = new AssignedRouteRecycler(this, assignedRouteModels);
        recyclerRouteGuard.setAdapter(mAdapter);

        swipeContainerGuard.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerGuard.setRefreshing(true);
                connectApiToGetRoutesOfGuard();
            }
        });

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
        tvTitle.setText(getString(R.string.home_caps));

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

        prefData = new PrefData(GMainActivity.this);
        apiInterface = ApiClient.getClient(GMainActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(GMainActivity.this);
        assignedRouteModels = new ArrayList();

        swipeContainerGuard.setOnRefreshListener(this);
        swipeContainerGuard.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);

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

                    startActivity(new Intent(GMainActivity.this, GMainActivity.class));
                    finish();
                } else {

                    languageToLoad = "en";
                    Locale locale = new Locale(languageToLoad);
                    PrefData.writeStringPref(PrefData.PREF_selected_language, languageToLoad);
                    Locale.setDefault(locale);
                    Configuration config = new Configuration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                    startActivity(new Intent(GMainActivity.this, GMainActivity.class));
                    finish();
                }
            }
        });


    }

    private void connectApiToGetRoutesOfGuard() {
        if (CheckNetworkConnection.isConnection1(GMainActivity.this, true)) {
            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.getAllRouteForGuard(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                            assignedRouteModels.clear();
                            assignedRouteModels.addAll(response.body().getData());
                            PrefData.writeStringPref(PrefData.guard_name, response.body().getGuardName());
                            if (assignedRouteModels.isEmpty()) {
                                recyclerRouteGuard.setVisibility(View.GONE);
                                emptyView.setVisibility(View.VISIBLE);
                                emptyView.setText(response.body().getMsg());
                                swipeContainerGuard.setRefreshing(false);
                            } else {
                                recyclerRouteGuard.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                swipeContainerGuard.setRefreshing(false);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(GMainActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(GMainActivity.this, LoginActivity.class);
                                swipeContainerGuard.setRefreshing(false);
                            } else {
                                Utils.showSnackBar(drawerLayout, response.body().getMsg(), GMainActivity.this);
                                swipeContainerGuard.setRefreshing(false);
                            }

                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();

                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                    swipeContainerGuard.setRefreshing(false);
                }
            });
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
            startActivity(new Intent(GMainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_scan_history) {
            startActivity(new Intent(GMainActivity.this, ScanedHistory.class));
        } else if (id == R.id.nav_resetpassword) {
            startActivity(new Intent(GMainActivity.this, ResetPassword.class));
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
        if (CheckNetworkConnection.isConnection1(GMainActivity.this, true)) {

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
                            Toast.makeText(GMainActivity.this, R.string.logout_sucessfull, Toast.LENGTH_SHORT).show();
                            logout();
                        } else {
                            Utils.showSnackBar(drawerLayout, response.body().getMsg(), GMainActivity.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

        Intent intent = new Intent(GMainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        AlarmService.cancelAlarm(GMainActivity.this);
        AlarmService.removeDatabaseValues();
        AlarmService.stopLocationUpdate();
        Utils.stopAlarmService(GMainActivity.this);
        AlarmService.cancelNotification(GMainActivity.this, NOTIFICATION_CHANNEL_ID);
        stopService(new Intent(GMainActivity.this, AlarmService.class));

        startActivity(intent);
        finish();
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

    @Override
    public void onRefresh() {

        swipeContainerGuard.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerGuard.setRefreshing(true);
                connectApiToGetRoutesOfGuard();
            }
        });


    }

}