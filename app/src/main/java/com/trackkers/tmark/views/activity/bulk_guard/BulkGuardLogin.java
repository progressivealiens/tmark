package com.trackkers.tmark.views.activity.bulk_guard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyButton;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.helper.Validation;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponseOperations;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BulkGuardLogin extends AppCompatActivity {

    @BindView(R.id.et_company_email)
    TextInputEditText etCompanyEmail;
    @BindView(R.id.et_site_code)
    TextInputEditText etSiteCode;
    @BindView(R.id.et_route_code)
    TextInputEditText etRouteCode;
    @BindView(R.id.btn_emp_login)
    MyButton btnEmpLogin;
    @BindView(R.id.root_emp_login)
    ScrollView rootEmpLogin;

    ApiInterface apiInterface;
    ProgressView progressView;
    PrefData prefData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operations_login);
        ButterKnife.bind(this);

        progressView = new ProgressView(BulkGuardLogin.this);
        prefData = new PrefData(BulkGuardLogin.this);
        apiInterface = ApiClient.getClient(BulkGuardLogin.this).create(ApiInterface.class);

        setupUI(rootEmpLogin);

        btnEmpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validation.nullValidator(etCompanyEmail.getText().toString().trim())) {
                    Utils.showSnackBar(rootEmpLogin, getString(R.string.enter_company_email), etCompanyEmail, BulkGuardLogin.this);
                } else if (Validation.nullValidator(etSiteCode.getText().toString().trim())) {
                    Utils.showSnackBar(rootEmpLogin, getString(R.string.enter_site_code), etSiteCode, BulkGuardLogin.this);
                } else if (Validation.nullValidator(etRouteCode.getText().toString().trim())) {
                    Utils.showSnackBar(rootEmpLogin, getString(R.string.enter_route_code), etRouteCode, BulkGuardLogin.this);
                } else {
                    connectApiToLoginOperations(etCompanyEmail.getText().toString().trim(), etSiteCode.getText().toString().trim(), etRouteCode.getText().toString().trim());
                }
            }
        });
    }

    private void connectApiToLoginOperations(String EmailId, String siteCode, String routeCode) {

        if (CheckNetworkConnection.isConnection1(BulkGuardLogin.this, true)) {
            progressView.showLoader();

            Call<ApiResponseOperations> call = apiInterface.MultipleGuardsLogin(EmailId, siteCode, routeCode);
            call.enqueue(new Callback<ApiResponseOperations>() {
                @Override
                public void onResponse(Call<ApiResponseOperations> call, Response<ApiResponseOperations> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            PrefData.writeBooleanPref(PrefData.PREF_LOGINSTATUS, true);
                            PrefData.writeStringPref(PrefData.employee_type, "bulk_guard");
                            PrefData.writeStringPref(PrefData.route_id, String.valueOf(response.body().getData().get(0).getRouteId()));
                            PrefData.writeStringPref(PrefData.company_email, EmailId);
                            PrefData.writeStringPref(PrefData.site_code, siteCode);
                            PrefData.writeStringPref(PrefData.route_code, routeCode);

                            startActivity(new Intent(BulkGuardLogin.this, BulkGuardMainActivity.class));
                            finishAffinity();

                        } else {
                            Utils.showSnackBar(rootEmpLogin, response.body().getMsg(), BulkGuardLogin.this);
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(BulkGuardLogin.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(BulkGuardLogin.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(BulkGuardLogin.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BulkGuardLogin.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Utils.hideSoftKeyboard(BulkGuardLogin.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

}
