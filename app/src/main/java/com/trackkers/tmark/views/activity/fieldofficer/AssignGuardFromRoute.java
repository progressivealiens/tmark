package com.trackkers.tmark.views.activity.fieldofficer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.fieldofficer.AssignedGuardOnRouteAdapter;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.customviews.customSpinner;
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

public class AssignGuardFromRoute extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_assign_guard)
    Button btnAssignGuard;
    @BindView(R.id.tv_route_name)
    MyTextview tvRouteName;
    @BindView(R.id.spinner_guards)
    customSpinner spinnerGuards;
    @BindView(R.id.recycler_assigned_guard)
    RecyclerView recyclerAssignedGuard;
    @BindView(R.id.root_assign_guard)
    LinearLayout rootAssignGuard;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    List<ApiResponse.DataBean> allGuardList = new ArrayList<>();
    String empId = "";

    AssignedGuardOnRouteAdapter mAdapter;
    List<ApiResponse.DataBean> listGuardsOnRoute = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_guard_from_route);
        ButterKnife.bind(this);

        initialization();

        getDataForSpinner();

        spinnerGuards.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                empId = String.valueOf(allGuardList.get(position).getEmployeeId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAssignGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (empId.equalsIgnoreCase("")) {
                    Toast.makeText(AssignGuardFromRoute.this, R.string.please_select_guard, Toast.LENGTH_SHORT).show();
                } else {
                    connectApiToAssignGuardOnThisRoute();
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerAssignedGuard.setLayoutManager(manager);
        mAdapter = new AssignedGuardOnRouteAdapter(this, listGuardsOnRoute);
        recyclerAssignedGuard.setAdapter(mAdapter);


        connectToGetGuardsOnThisRoute();

    }

    private void initialization() {
        setSupportActionBar(toolbar);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvTitle.setText(getResources().getString(R.string.assign_unassign));
        tvRouteName.setText(getResources().getString(R.string.route_name) + PrefData.readStringPref(PrefData.route_name));
        spinnerGuards.setPrompt(getString(R.string.select_guard));

        prefData = new PrefData(AssignGuardFromRoute.this);
        apiInterface = ApiClient.getClient(AssignGuardFromRoute.this).create(ApiInterface.class);
        progressView = new ProgressView(AssignGuardFromRoute.this);

    }

    private void getDataForSpinner() {

        if (CheckNetworkConnection.isConnection1(AssignGuardFromRoute.this, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.getAllGuardsListInFieldOffice(
                    PrefData.readStringPref(PrefData.security_token)
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            allGuardList.clear();
                            allGuardList.addAll(response.body().getData());

                            spinnerGuards.setAdapter(new GuardDetailsAdapter(AssignGuardFromRoute.this, allGuardList));


                        }  else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(AssignGuardFromRoute.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootAssignGuard, response.body().getMsg(), AssignGuardFromRoute.this);
                            }

                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    private void connectToGetGuardsOnThisRoute() {

        if (CheckNetworkConnection.isConnection1(AssignGuardFromRoute.this, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.partialListGuard(
                    PrefData.readStringPref(PrefData.security_token),
                    PrefData.readStringPref(PrefData.route_id)
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            listGuardsOnRoute.clear();
                            listGuardsOnRoute.addAll(response.body().getData());

                            mAdapter.notifyDataSetChanged();

                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(AssignGuardFromRoute.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootAssignGuard, response.body().getMsg(), AssignGuardFromRoute.this);
                            }

                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    private void connectApiToAssignGuardOnThisRoute() {

        if (CheckNetworkConnection.isConnection1(AssignGuardFromRoute.this, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.fielOfficerToAssignGuard(
                    empId,
                    PrefData.readStringPref(PrefData.route_id)
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.guard_assigned_to) + PrefData.readStringPref(PrefData.route_name) + getResources().getString(R.string.successfully), Toast.LENGTH_SHORT).show();

                            listGuardsOnRoute.clear();
                            listGuardsOnRoute.addAll(response.body().getData());

                            mAdapter.notifyDataSetChanged();


                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(AssignGuardFromRoute.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootAssignGuard, response.body().getMsg(), AssignGuardFromRoute.this);
                            }

                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AssignGuardFromRoute.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class GuardDetailsAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflter;
        List<ApiResponse.DataBean> employeeDetails;

        public GuardDetailsAdapter(Context context, List<ApiResponse.DataBean> employeeDetails) {
            this.context = context;
            this.employeeDetails = employeeDetails;
            inflter = (LayoutInflater.from(context));
        }


        @Override
        public int getCount() {
            return employeeDetails.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflter.inflate(R.layout.spinner_layout, null);
            TextView names = convertView.findViewById(R.id.value);
            names.setText(employeeDetails.get(position).getName());

            return convertView;
        }
    }

}
