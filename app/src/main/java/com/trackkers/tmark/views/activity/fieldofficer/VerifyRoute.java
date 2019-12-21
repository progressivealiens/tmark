package com.trackkers.tmark.views.activity.fieldofficer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.fieldofficer.AssignedPendingRouteRecycler;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyRoute extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_route_fo)
    RecyclerView recyclerRouteFo;
    @BindView(R.id.swipe_container_fo)
    SwipeRefreshLayout swipeContainerFo;
    @BindView(R.id.empty_view)
    MyTextview emptyView;
    @BindView(R.id.root_verify)
    LinearLayout rootVerify;
    @BindView(R.id.tv_header)
    MyTextview tvHeader;

    AssignedPendingRouteRecycler mAdapter;
    List<ApiResponse.DataBean> assignedPendingRouteModels;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    public String shownLayout = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_route);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            shownLayout = bundle.getString(FOMainActivity.purposeClass);
        }

        initialization();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerRouteFo.setLayoutManager(manager);
        mAdapter = new AssignedPendingRouteRecycler(this, assignedPendingRouteModels, shownLayout);
        recyclerRouteFo.setAdapter(mAdapter);

        swipeContainerFo.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerFo.setRefreshing(true);
                connectApiToFetchPendingRoutes();
            }
        });
    }

    private void initialization() {
        setSupportActionBar(toolbar);

        tvTitle.setVisibility(View.VISIBLE);

        if (shownLayout.equalsIgnoreCase("checkpoints")) {
            tvTitle.setText(getResources().getString(R.string.verify_routes));
        } else if (shownLayout.equalsIgnoreCase("assignment")) {
            tvTitle.setText(getResources().getString(R.string.assign_unassign_route));
        }

        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        prefData = new PrefData(VerifyRoute.this);
        apiInterface = ApiClient.getClient(VerifyRoute.this).create(ApiInterface.class);
        progressView = new ProgressView(VerifyRoute.this);
        assignedPendingRouteModels = new ArrayList<>();

        swipeContainerFo.setOnRefreshListener(this);
        swipeContainerFo.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);

        if (shownLayout.equalsIgnoreCase("assignment")) {
            tvHeader.setText(R.string.select_route);
        } else {
            tvHeader.setText(R.string.all_assigned_pending_route);
        }

    }

    private void connectApiToFetchPendingRoutes() {
        if (CheckNetworkConnection.isConnection1(VerifyRoute.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = null;

            if (shownLayout.equalsIgnoreCase("checkpoints")) {
                call = apiInterface.PendingRoutes(PrefData.readStringPref(PrefData.security_token));
            } else if (shownLayout.equalsIgnoreCase("assignment")) {
                call = apiInterface.verifiedAssignRoutes(PrefData.readStringPref(PrefData.security_token));
            }

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                    progressView.hideLoader();
                    swipeContainerFo.setRefreshing(false);

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                            assignedPendingRouteModels.clear();
                            assignedPendingRouteModels.addAll(response.body().getData());

                            if (assignedPendingRouteModels.isEmpty()) {
                                recyclerRouteFo.setVisibility(View.GONE);
                                swipeContainerFo.setVisibility(View.GONE);
                                emptyView.setVisibility(View.VISIBLE);

                                if (shownLayout.equalsIgnoreCase("assignment")) {
                                    emptyView.setText("No Route Assigned To You");
                                }else{
                                    emptyView.setText("No Pending Route Found ");
                                }

                            } else {
                                recyclerRouteFo.setVisibility(View.VISIBLE);
                                swipeContainerFo.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(VerifyRoute.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(VerifyRoute.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootVerify, response.body().getMsg(), VerifyRoute.this);
                            }
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(VerifyRoute.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(VerifyRoute.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(VerifyRoute.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyRoute.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                    swipeContainerFo.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        swipeContainerFo.post(new Runnable() {
            @Override
            public void run() {
                swipeContainerFo.setRefreshing(true);
                connectApiToFetchPendingRoutes();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
