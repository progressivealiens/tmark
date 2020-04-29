package com.trackkers.tmark.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.helper.Validation;
import com.trackkers.tmark.views.activity.bulk_guard.BulkGuardLogin;
import com.trackkers.tmark.views.activity.fieldofficer.FOMainActivity;
import com.trackkers.tmark.views.activity.guard.GMainActivity;
import com.trackkers.tmark.views.activity.operations.OperationsMainActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_login_mobile)
    TextInputEditText etLoginMobile;
    @BindView(R.id.tiMobile)
    TextInputLayout tiMobile;
    @BindView(R.id.et_login_password)
    TextInputEditText etLoginPassword;
    @BindView(R.id.tiPassword)
    TextInputLayout tiPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.et_company_name)
    TextInputEditText etCompanyName;
    @BindView(R.id.tiCompanyName)
    TextInputLayout tiCompanyName;
    @BindView(R.id.tv_operations_login)
    MyTextview tvOperationsLogin;
    @BindView(R.id.root_login)
    ScrollView rootLogin;
    ApiInterface apiInterface;
    ProgressView progressView;
    PrefData prefData;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient(LoginActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(LoginActivity.this);
        prefData = new PrefData(LoginActivity.this);

        setupUI(rootLogin);
        getFirebaseToken();

        deviceId = Utils.getDeviceId(LoginActivity.this);
        PrefData.writeStringPref(PrefData.deviceID, deviceId);

        Log.e("fcmToken",PrefData.readStringPref(PrefData.firebase_token));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validation.nullValidator(etCompanyName.getText().toString())) {
                    Utils.showSnackBar(rootLogin, getString(R.string.enter_company_name), etCompanyName, LoginActivity.this);
                } else if (Validation.nullValidator(etLoginMobile.getText().toString())) {
                    Utils.showSnackBar(rootLogin, getString(R.string.registered_mobile_number), etLoginMobile, LoginActivity.this);
                } else if (Validation.nullValidator(etLoginPassword.getText().toString())) {
                    Utils.showSnackBar(rootLogin, getString(R.string.enter_your_password), etLoginPassword, LoginActivity.this);
                } else {
                    connectApiToLogin(etCompanyName.getText().toString(), etLoginMobile.getText().toString(), etLoginPassword.getText().toString());
                }
            }
        });

        tvOperationsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, BulkGuardLogin.class));
            }
        });

    }

    private void connectApiToLogin(final String CompanyName, String MobileNumber, String Password) {
        if (CheckNetworkConnection.isConnection1(LoginActivity.this, true)) {
            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.Login(CompanyName, MobileNumber, Password, PrefData.readStringPref(PrefData.firebase_token));
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                    progressView.hideLoader();
                    try {
                        if (response.body() != null) {
                            if (response.body().getStatus() != null) {
                                if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                    PrefData.writeBooleanPref(PrefData.PREF_LOGINSTATUS, true);
                                    PrefData.writeStringPref(PrefData.security_token, response.body().getToken());
                                    PrefData.writeStringPref(PrefData.employee_type, response.body().getType());
                                    PrefData.writeStringPref(PrefData.company_name, response.body().getCompanyName());
                                    PrefData.writeStringPref(PrefData.company_logo, response.body().getLogo());

                                    if (response.body().getType().equalsIgnoreCase("Field Officer")) {

                                        Intent intent = new Intent(LoginActivity.this, FOMainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    } else if (response.body().getType().equalsIgnoreCase("Guard")) {

                                        Intent intent = new Intent(LoginActivity.this, GMainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    } else {

                                        Intent intent = new Intent(LoginActivity.this, OperationsMainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                } else {
                                    Utils.showSnackBar(rootLogin, response.body().getMsg(), LoginActivity.this);
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Status Is Null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Body Is Null", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(LoginActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(LoginActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(LoginActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(LoginActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Utils.hideSoftKeyboard(LoginActivity.this);
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

    public void getFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getToken();
                            PrefData.writeStringPref(PrefData.firebase_token, token);
                            //Log.e("Firebase Token : ", token);
                            //Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
                        } else {
                            //Log.e("FirebaseTokenFail", "getInstanceId failed", task.getException());
                            return;
                        }
                    }
                });
    }
}
