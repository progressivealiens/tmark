package com.trackkers.tmark.views.activity.operations;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.operations.OperationalHistoryRecycler;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseHistoryOperational;

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

public class OperationalHistory extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_selected_date)
    MyTextview tvSelectedDate;
    @BindView(R.id.btn_history_search)
    MyButton btnHistorySearch;
    @BindView(R.id.empty_view)
    MyTextview emptyView;
    @BindView(R.id.root_operations_history)
    LinearLayout rootOperationsHistory;
    @BindView(R.id.recycler_checkin_history)
    RecyclerView recyclerCheckinHistory;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    String SelectedDate = "", formatSelectedDate = "";

    List<ApiResponseHistoryOperational.DataBean> parentList;
    OperationalHistoryRecycler mAdapter;
    long timeStamp;

    String message = "", workingHours = "", checkinTime = "", checkoutTime = "", startImageName = "", type = "", text = "", image = "", time = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operational_history);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.checkin_history));

        ivBack.setOnClickListener(this);
        tvSelectedDate.setOnClickListener(this);
        btnHistorySearch.setOnClickListener(this);

        prefData = new PrefData(OperationalHistory.this);
        apiInterface = ApiClient.getClient(OperationalHistory.this).create(ApiInterface.class);
        progressView = new ProgressView(OperationalHistory.this);

        parentList = new ArrayList<>();

        timeStamp = Long.parseLong(Utils.currentTimeStamp());
        SelectedDate = Utils.selectedDateAndTimeFormat(timeStamp, tvSelectedDate);
        try {
            formatSelectedDate = Utils.formatDate(SelectedDate, "dd/MM/yyyy", "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_selected_date:
                Utils.showDatePicker(OperationalHistory.this, tvSelectedDate);
                break;

            case R.id.btn_history_search:
                try {
                    SelectedDate = tvSelectedDate.getText().toString();
                    formatSelectedDate = Utils.formatDate(SelectedDate, "dd/mm/yyyy", "yyyy-mm-dd");

                    connectApiToGetOperationalHistory();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void connectApiToGetOperationalHistory() {
        if (CheckNetworkConnection.isConnection1(OperationalHistory.this, true)) {
            progressView.showLoader();
            Call<ApiResponseHistoryOperational> call =
                    apiInterface.operationalHistory(PrefData.readStringPref(PrefData.security_token), formatSelectedDate);

            call.enqueue(new Callback<ApiResponseHistoryOperational>() {
                @Override
                public void onResponse(Call<ApiResponseHistoryOperational> call, Response<ApiResponseHistoryOperational> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {


                            parentList.clear();

                            try {
                                JSONObject json = new JSONObject(String.valueOf(new Gson().toJson(response.body())));
                                JSONArray array = json.getJSONArray("data");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = array.getJSONObject(i);
                                    checkinTime = jsonObject.optString("checkInTime");
                                    checkoutTime = jsonObject.optString("checkOutTime");
                                    message = jsonObject.optString("message");
                                    workingHours = jsonObject.optString("workingHours");
                                    startImageName = jsonObject.optString("startImageName");

                                    JSONArray newArray = jsonObject.getJSONArray("communicationsListData");
                                    List<ApiResponseHistoryOperational.DataBean.CommunicationsListDataBean> childList = new ArrayList<>();
                                    for (int j = 0; j < newArray.length(); j++) {
                                        if (newArray.length() == 0) {
                                            Log.e("length", String.valueOf(newArray.length()));
                                        } else {
                                            JSONObject newJson = newArray.getJSONObject(j);
                                            type = newJson.optString("type");
                                            text = newJson.optString("text");
                                            image = newJson.optString("image");
                                            time = newJson.optString("time");
                                        }
                                        childList.add(new ApiResponseHistoryOperational.DataBean.CommunicationsListDataBean(type, text, image, time));
                                    }
                                    parentList.add(new ApiResponseHistoryOperational.DataBean(String.valueOf(i), childList, checkinTime, checkoutTime, message, workingHours, startImageName));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mAdapter = new OperationalHistoryRecycler(parentList, OperationalHistory.this);
                            recyclerCheckinHistory.setLayoutManager(new LinearLayoutManager(OperationalHistory.this));
                            recyclerCheckinHistory.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(OperationalHistory.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(OperationalHistory.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootOperationsHistory, response.body().getMsg(), OperationalHistory.this);
                            }


                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(OperationalHistory.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(OperationalHistory.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(OperationalHistory.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OperationalHistory.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();

                    }

                }

                @Override
                public void onFailure(Call<ApiResponseHistoryOperational> call, Throwable t) {
                    progressView.hideLoader();
                    t.printStackTrace();
                }
            });


        }
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

}
