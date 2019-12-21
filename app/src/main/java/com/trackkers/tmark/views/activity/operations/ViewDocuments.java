package com.trackkers.tmark.views.activity.operations;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewDocuments extends AppCompatActivity {

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
    @BindView(R.id.iv_medical)
    ImageView ivMedical;
    @BindView(R.id.iv_uanc)
    ImageView ivUanc;
    @BindView(R.id.iv_esic)
    ImageView ivEsic;
    @BindView(R.id.root_documents)
    LinearLayout rootDocuments;
    @BindView(R.id.tv_medical)
    MyTextview tvMedical;
    @BindView(R.id.tv_uanc)
    MyTextview tvUanc;
    @BindView(R.id.tv_esic)
    MyTextview tvEsic;
    @BindView(R.id.tv_aadhar)
    MyTextview tvAadhar;
    @BindView(R.id.iv_aadhar)
    ImageView ivAadhar;
    @BindView(R.id.tv_driving)
    MyTextview tvDriving;
    @BindView(R.id.iv_driving)
    ImageView ivDriving;


    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_documents);
        ButterKnife.bind(this);

        initialize();

        connectApiToFetchDocuments();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initialize() {
        setSupportActionBar(toolbar);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.docs));

        prefData = new PrefData(ViewDocuments.this);
        apiInterface = ApiClient.getClient(ViewDocuments.this).create(ApiInterface.class);
        progressView = new ProgressView(ViewDocuments.this);

    }

    private void connectApiToFetchDocuments() {
        if (CheckNetworkConnection.isConnection1(ViewDocuments.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.viewDocument(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();
                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            if (response.body().getData().get(0).getMedical().equalsIgnoreCase("")) {
                                tvMedical.setText(getString(R.string.medical) + " :- " + getResources().getString(R.string.no_documents));
                                ivMedical.setVisibility(View.GONE);
                            } else {
                                ivMedical.setVisibility(View.VISIBLE);
                                Picasso.get().load(Utils.BASE_IMAGE_MEDICAL + response.body().getData().get(0).getMedical()).placeholder(R.drawable.progress_animation).into(ivMedical);
                            }

                            if (response.body().getData().get(0).getUAN().equalsIgnoreCase("")) {
                                tvUanc.setText(getString(R.string.uanc) + " :- " + getResources().getString(R.string.no_documents));
                                ivUanc.setVisibility(View.GONE);
                            } else {
                                ivUanc.setVisibility(View.VISIBLE);
                                Picasso.get().load(Utils.BASE_IMAGE_UANC + response.body().getData().get(0).getUAN()).placeholder(R.drawable.progress_animation).into(ivUanc);
                            }

                            if (response.body().getData().get(0).getESIC().equalsIgnoreCase("")) {
                                tvEsic.setText(getString(R.string.esic) + " :- " + getResources().getString(R.string.no_documents));
                                ivEsic.setVisibility(View.GONE);
                            } else {
                                ivEsic.setVisibility(View.VISIBLE);
                                Picasso.get().load(Utils.BASE_IMAGE_ESIC + response.body().getData().get(0).getESIC()).placeholder(R.drawable.progress_animation).into(ivEsic);
                            }

                            if (response.body().getData().get(0).getAdhaarCard().equalsIgnoreCase("")) {
                                tvAadhar.setText(getString(R.string.aadhar) + " :- " + getResources().getString(R.string.no_documents));
                                ivAadhar.setVisibility(View.GONE);
                            } else {
                                ivAadhar.setVisibility(View.VISIBLE);
                                Picasso.get().load(Utils.BASE_IMAGE_ADHAR + response.body().getData().get(0).getAdhaarCard()).placeholder(R.drawable.progress_animation).into(ivAadhar);
                            }

                            if (response.body().getData().get(0).getDrivingLicence().equalsIgnoreCase("")) {
                                tvDriving.setText(getString(R.string.driving) + " :- " + getResources().getString(R.string.no_documents));
                                ivDriving.setVisibility(View.GONE);
                            } else {
                                ivDriving.setVisibility(View.VISIBLE);
                                Picasso.get().load(Utils.BASE_IMAGE_DRIVING + response.body().getData().get(0).getDrivingLicence()).placeholder(R.drawable.progress_animation).into(ivDriving);
                            }


                        } else {
                            tvMedical.setText(getString(R.string.medical) + " :- " + getResources().getString(R.string.no_documents));
                            tvUanc.setText(getString(R.string.uanc) + " :- " + getResources().getString(R.string.no_documents));
                            tvEsic.setText(getString(R.string.esic) + " :- " + getResources().getString(R.string.no_documents));
                            tvAadhar.setText(getString(R.string.aadhar) + " :- " + getResources().getString(R.string.no_documents));
                            tvDriving.setText(getString(R.string.driving) + " :- " + getResources().getString(R.string.no_documents));

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(ViewDocuments.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(ViewDocuments.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootDocuments, response.body().getMsg(), ViewDocuments.this);
                            }
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(ViewDocuments.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(ViewDocuments.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(ViewDocuments.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ViewDocuments.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
