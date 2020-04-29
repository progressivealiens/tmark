package com.trackkers.tmark.views.activity.fieldofficer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.trackkers.tmark.R;
import com.trackkers.tmark.adapter.fieldofficer.FOHistoryAdapter;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.model.fieldofficer.HistoryChild;
import com.trackkers.tmark.model.fieldofficer.HistoryParent;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseHistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignedHistory extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.root_history_fo)
    LinearLayout rootHistoryFo;
    @BindView(R.id.main_recycler)
    RecyclerView mainRecycler;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;
    FOHistoryAdapter mAdapter;
    List<HistoryParent> historyParents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_history);
        ButterKnife.bind(this);

        initialize();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        connectApiToGetVerificationHistory();

    }

    private void initialize() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.assigned_route_history);

        prefData = new PrefData(AssignedHistory.this);
        apiInterface = ApiClient.getClient(AssignedHistory.this).create(ApiInterface.class);
        progressView = new ProgressView(AssignedHistory.this);

        historyParents = new ArrayList<>();
    }

    private void connectApiToGetVerificationHistory() {
        if (CheckNetworkConnection.isConnection1(AssignedHistory.this, true)) {

            progressView.showLoader();
            Call<ApiResponseHistory> call = apiInterface.VerificationHistoryFo(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponseHistory>() {
                @Override
                public void onResponse(Call<ApiResponseHistory> call, Response<ApiResponseHistory> response) {

                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                historyParents.clear();

                                try {
                                    JSONObject json = new JSONObject(String.valueOf(new Gson().toJson(response.body())));
                                    JSONArray array = json.getJSONArray("data");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jsonObject = array.getJSONObject(i);
                                        String siteName = jsonObject.getString("siteName");
                                        String routeName = jsonObject.getString("routeName");
                                        JSONArray newArray = jsonObject.getJSONArray("checkpoints");
                                        List<HistoryChild> historyChildren = new ArrayList<>();
                                        for (int j = 0; j < newArray.length(); j++) {
                                            JSONObject newJson = newArray.getJSONObject(j);
                                            String name = newJson.getString("name");
                                            String dateTime = newJson.getString("dateTime");
                                            String address = newJson.getString("address");

                                            historyChildren.add(new HistoryChild(name, dateTime, address));
                                        }
                                        historyParents.add(new HistoryParent(String.valueOf(i), historyChildren, siteName, routeName));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mAdapter = new FOHistoryAdapter(historyParents);
                                mainRecycler.setLayoutManager(new LinearLayoutManager(AssignedHistory.this));
                                mainRecycler.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();

                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(AssignedHistory.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(AssignedHistory.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(AssignedHistory.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }

                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(AssignedHistory.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(AssignedHistory.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(AssignedHistory.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(AssignedHistory.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponseHistory> call, Throwable t) {
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

}
