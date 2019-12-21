package com.trackkers.tmark.views.activity.bulk_guard;

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
import android.widget.LinearLayout;
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
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.bulk_guard.BulkGuardRecycler;
import com.trackkers.tmark.customviews.CustomTypefaceSpan;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.views.activity.ResetPassword;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BulkGuardMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

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

    BulkGuardRecycler mAdapter;
    ApiInterface apiInterface;
    ProgressView progressView;
    PrefData prefData;

    String languageToLoad = "";

    public List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout_employee);
        ButterKnife.bind(this);

        initialize();

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
        mAdapter = new BulkGuardRecycler(this, employeeDetails);
        recyclerAllGuard.setAdapter(mAdapter);

        swipeContainerGuard.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerGuard.setRefreshing(true);
                connectApiToLoginOperations();
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


    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "aver_bold.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
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

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                            swipeContainerGuard.setRefreshing(false);

                            PrefData.writeStringPref(PrefData.route_id, String.valueOf(response.body().getData().get(0).getRouteId()));
                            PrefData.writeStringPref(PrefData.route_name, response.body().getData().get(0).getRouteName());
                            PrefData.writeStringPref(PrefData.route_start_address, response.body().getData().get(0).getRouteStartAddress());
                            PrefData.writeStringPref(PrefData.route_end_address, response.body().getData().get(0).getRouteEndAddress());
                            PrefData.writeStringPref(PrefData.site_name, response.body().getData().get(0).getSiteName());

                            employeeDetails.clear();
                            employeeDetails.addAll(response.body().getData().get(0).getEmployees());
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Utils.showSnackBar(rootEmployeeMain, response.body().getMsg(), BulkGuardMainActivity.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
        } else if (id == R.id.nav_resetpassword) {
            startActivity(new Intent(BulkGuardMainActivity.this, ResetPassword.class));
        } else if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.nav_rate_us) {
            rateUs();
        } else if (id == R.id.nav_logout) {
            connectApiToLogoutGuard();
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
            Toast.makeText(this, getResources().getString(R.string.unable_to_find), Toast.LENGTH_LONG).show();
        }
    }

    private void connectApiToLogoutGuard() {
        if (CheckNetworkConnection.isConnection1(BulkGuardMainActivity.this, true)) {

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
                            logout();
                        } else {
                            Utils.showSnackBar(drawerLayout, response.body().getMsg(), BulkGuardMainActivity.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BulkGuardMainActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

        Intent intent = new Intent(BulkGuardMainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

}
