package com.trackkers.tmark.views.activity.guard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.guard.ScannedHistoryGuardRecycler;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseHistoryGuard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanedHistory extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.tv_selected_date)
    TextView tvSelectedDate;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.root_scan_history)
    LinearLayout rootScanHistory;
    @BindView(R.id.btn_history_search)
    Button btnHistorySearch;
    @BindView(R.id.recycler_scan_history)
    RecyclerView recyclerScanHistory;

    long timeStamp;
    String SelectedDate = "", formatSelectedDate = "";

    ScannedHistoryGuardRecycler mAdapter;
    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    List<ApiResponseHistoryGuard.DataBean> parentList;

    String siteName = "", routeName = "", name = "", date = "", time = "", checkinTime = "", checkoutTime = "", imageName = "";
    int roundNo = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaned_history);
        ButterKnife.bind(this);

        initialize();

        connectApiToGetGuardHistory();
    }

    private void initialize() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.scanned_history);

        ivBack.setOnClickListener(this);
        tvSelectedDate.setOnClickListener(this);
        btnHistorySearch.setOnClickListener(this);

        prefData = new PrefData(ScanedHistory.this);
        apiInterface = ApiClient.getClient(ScanedHistory.this).create(ApiInterface.class);
        progressView = new ProgressView(ScanedHistory.this);

        parentList = new ArrayList<>();

        timeStamp = Long.parseLong(Utils.currentTimeStamp());
        SelectedDate = Utils.selectedDateAndTimeFormat(timeStamp, tvSelectedDate);
        try {
            formatSelectedDate = Utils.formatDate(SelectedDate, "dd/MM/yyyy", "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void connectApiToGetGuardHistory() {

        if (CheckNetworkConnection.isConnection1(ScanedHistory.this, true)) {
            progressView.showLoader();

            Call<ApiResponseHistoryGuard> call =
                    apiInterface.guardScanHistory(PrefData.readStringPref(PrefData.security_token), formatSelectedDate);
            call.enqueue(new Callback<ApiResponseHistoryGuard>() {
                @Override
                public void onResponse(Call<ApiResponseHistoryGuard> call, Response<ApiResponseHistoryGuard> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                parentList.clear();
                                try {
                                    JSONObject json = new JSONObject(String.valueOf(new Gson().toJson(response.body())));
                                    JSONArray array = json.getJSONArray("data");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jsonObject = array.getJSONObject(i);
                                        siteName = jsonObject.optString("siteName");
                                        routeName = jsonObject.optString("routeName");
                                        checkinTime = jsonObject.optString("checkInTime");
                                        checkoutTime = jsonObject.optString("checkOutTime");
                                        imageName = jsonObject.optString("startImageName");

                                        JSONArray newArray = jsonObject.getJSONArray("checkPointsScanDetails");
                                        List<ApiResponseHistoryGuard.DataBean.CheckPointsScanDetailsBean> childList = new ArrayList<>();
                                        for (int j = 0; j < newArray.length(); j++) {
                                            if (newArray.length() == 0) {
                                                Log.e("length", String.valueOf(newArray.length()));
                                            } else {
                                                JSONObject newJson = newArray.getJSONObject(j);
                                                name = newJson.optString("checkpointName");
                                                date = newJson.optString("scanDate");
                                                time = newJson.optString("scanTime");
                                                roundNo = newJson.optInt("trip");
                                            }
                                            childList.add(new ApiResponseHistoryGuard.DataBean.CheckPointsScanDetailsBean(name, date, time, roundNo));
                                        }
                                        parentList.add(new ApiResponseHistoryGuard.DataBean(String.valueOf(i), childList, siteName, routeName, checkinTime, checkoutTime, imageName));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mAdapter = new ScannedHistoryGuardRecycler(parentList, ScanedHistory.this);
                                recyclerScanHistory.setLayoutManager(new LinearLayoutManager(ScanedHistory.this));
                                recyclerScanHistory.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(ScanedHistory.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(ScanedHistory.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(ScanedHistory.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(ScanedHistory.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(ScanedHistory.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink),getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(ScanedHistory.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(ScanedHistory.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();

                    }

                }

                @Override
                public void onFailure(Call<ApiResponseHistoryGuard> call, Throwable t) {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_selected_date:
                Utils.showDatePicker(ScanedHistory.this, tvSelectedDate);
                break;

            case R.id.btn_history_search:
                try {
                    SelectedDate = tvSelectedDate.getText().toString();
                    formatSelectedDate = Utils.formatDate(SelectedDate, "dd/mm/yyyy", "yyyy-mm-dd");
                    connectApiToGetGuardHistory();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
