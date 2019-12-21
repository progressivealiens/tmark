package com.trackkers.tmark.views.activity.bulk_guard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.bulk_guard.BulkGuardHistoryAdapter;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.customviews.customSpinner;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BulkGuardHistory extends AppCompatActivity {

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
    @BindView(R.id.tv_selected_date)
    MyTextview tvSelectedDate;
    @BindView(R.id.sp_guards)
    customSpinner spGuards;
    @BindView(R.id.btn_history_search)
    MyButton btnHistorySearch;
    @BindView(R.id.recycler_bulk_guard_history)
    RecyclerView recyclerBulkGuardHistory;
    @BindView(R.id.root_bulk_guards_history)
    LinearLayout rootBulkGuardsHistory;

    ApiInterface apiInterface;
    ProgressView progressView;
    PrefData prefData;

    String empId = "";
    String SelectedDate = "", formatSelectedDate = "";
    long timeStamp;

    List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails;
    List<ApiResponse.DataBean> bulkGuardHistoryData;
    BulkGuardHistoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_guard_history);
        ButterKnife.bind(this);

        initialize();

        connectApiToGetGuardsDetails();

        spGuards.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                empId = String.valueOf(employeeDetails.get(position).getEmployeeId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvSelectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showDatePicker(BulkGuardHistory.this, tvSelectedDate);
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnHistorySearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    SelectedDate = tvSelectedDate.getText().toString();
                    formatSelectedDate = Utils.formatDate(SelectedDate, "dd/MM/yyyy", "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if (empId.equalsIgnoreCase("")) {
                    Toast.makeText(BulkGuardHistory.this, R.string.please_select_guard, Toast.LENGTH_SHORT).show();
                } else {
                    connectApiToFetchBulkGuardHistory();
                }
            }
        });

        mAdapter = new BulkGuardHistoryAdapter(BulkGuardHistory.this, bulkGuardHistoryData);
        recyclerBulkGuardHistory.setLayoutManager(new LinearLayoutManager(BulkGuardHistory.this));
        recyclerBulkGuardHistory.setAdapter(mAdapter);

    }

    private void initialize() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.history);

        employeeDetails = new ArrayList<>();
        bulkGuardHistoryData = new ArrayList<>();
        spGuards.setPrompt(getString(R.string.select_guard));

        prefData = new PrefData(BulkGuardHistory.this);
        apiInterface = ApiClient.getClient(BulkGuardHistory.this).create(ApiInterface.class);
        progressView = new ProgressView(BulkGuardHistory.this);

        timeStamp = Long.parseLong(Utils.currentTimeStamp());
        SelectedDate = Utils.selectedDateAndTimeFormat(timeStamp, tvSelectedDate);
        try {
            formatSelectedDate = Utils.formatDate(SelectedDate, "dd/MM/yyyy", "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void connectApiToGetGuardsDetails() {

        if (CheckNetworkConnection.isConnection1(BulkGuardHistory.this, true)) {
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

                            PrefData.writeStringPref(PrefData.route_id, String.valueOf(response.body().getData().get(0).getRouteId()));
                            PrefData.writeStringPref(PrefData.route_name, response.body().getData().get(0).getRouteName());
                            PrefData.writeStringPref(PrefData.route_start_address, response.body().getData().get(0).getRouteStartAddress());
                            PrefData.writeStringPref(PrefData.route_end_address, response.body().getData().get(0).getRouteEndAddress());
                            PrefData.writeStringPref(PrefData.site_name, response.body().getData().get(0).getSiteName());

                            employeeDetails.clear();
                            employeeDetails.addAll(response.body().getData().get(0).getEmployees());

                            spGuards.setAdapter(new GuardDetailsAdapter(BulkGuardHistory.this, employeeDetails));

                        } else {
                            Utils.showSnackBar(rootBulkGuardsHistory, response.body().getMsg(), BulkGuardHistory.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    private void connectApiToFetchBulkGuardHistory() {
        if (CheckNetworkConnection.isConnection1(BulkGuardHistory.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.guardMultipleHistory(
                    empId,
                    PrefData.readStringPref(PrefData.route_id),
                    formatSelectedDate);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase("success")) {

                            bulkGuardHistoryData.clear();
                            bulkGuardHistoryData.addAll(response.body().getData());

                            mAdapter.notifyDataSetChanged();
                        } else {
                            bulkGuardHistoryData.clear();
                            mAdapter.notifyDataSetChanged();
                            Utils.showSnackBar(rootBulkGuardsHistory, response.body().getMsg(), BulkGuardHistory.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BulkGuardHistory.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
        List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails;

        public GuardDetailsAdapter(Context context, List<ApiResponseOperations.DataBean.EmployeesBean> employeeDetails) {
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
            names.setText(employeeDetails.get(position).getEmployeeName());

            return convertView;
        }
    }

}
