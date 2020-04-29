package com.trackkers.tmark.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_company_name)
    TextView tvCompanyName;
    @BindView(R.id.tv_emp_name)
    TextView tvEmpName;
    @BindView(R.id.tv_emp_id)
    TextView tvEmpId;
    @BindView(R.id.tv_emp_type)
    TextView tvEmpType;
    @BindView(R.id.tv_emp_dob)
    TextView tvEmpDob;
    @BindView(R.id.tv_emp_doj)
    TextView tvEmpDoj;
    @BindView(R.id.tv_emp_mob)
    TextView tvEmpMob;
    @BindView(R.id.tv_emp_nationality)
    TextView tvEmpNationality;
    @BindView(R.id.tv_emp_address)
    TextView tvEmpAddress;
    @BindView(R.id.root_profile)
    LinearLayout rootProfile;

    ApiInterface apiInterface;
    ProgressView progressView;
    PrefData prefData;
    @BindView(R.id.iv_company_logo)
    ImageView ivCompanyLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_fo);
        ButterKnife.bind(this);

        initializeToolbar();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        connectApiToFetchProfileDetails();

    }

    private void connectApiToFetchProfileDetails() {

        if (CheckNetworkConnection.isConnection1(ProfileActivity.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.Profile(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                tvCompanyName.setText(PrefData.readStringPref(PrefData.company_name));
                                tvEmpName.setText(response.body().getData().get(0).getName());
                                tvEmpId.setText(response.body().getData().get(0).getEmpCode());
                                tvEmpType.setText(PrefData.readStringPref(PrefData.employee_type));
                                tvEmpDob.setText(response.body().getData().get(0).getDob());
                                tvEmpDoj.setText(response.body().getData().get(0).getDoj());
                                tvEmpMob.setText(response.body().getData().get(0).getMobile());
                                tvEmpNationality.setText(response.body().getData().get(0).getNationality());
                                tvEmpAddress.setText(response.body().getData().get(0).getAddress());
                            } else {

                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(ProfileActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(ProfileActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showSnackBar(rootProfile, response.body().getMsg(), ProfileActivity.this);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(ProfileActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(ProfileActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(ProfileActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(ProfileActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    private void initializeToolbar() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.profile));

        apiInterface = ApiClient.getClient(ProfileActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(ProfileActivity.this);
        prefData = new PrefData(ProfileActivity.this);

        Picasso.get().load(Utils.BASE_IMAGE_COMPANY + PrefData.readStringPref(PrefData.company_logo)).placeholder(R.drawable.progress_animation).into(ivCompanyLogo);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
