package com.trackkers.tmark.views.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResetPassword extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.lin_toolbar)
    LinearLayout linToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_reset_admin_mobile)
    TextView tvResetAdminMobile;
    @BindView(R.id.tv_reset_admin_name)
    TextView tvResetAdminName;
    @BindView(R.id.root_reset_password)
    LinearLayout rootResetPassword;
    @BindView(R.id.et_current_password)
    TextInputEditText etCurrentPassword;
    @BindView(R.id.et_new_password)
    TextInputEditText etNewPassword;
    @BindView(R.id.btn_reset_password)
    MyButton btnResetPassword;

    @BindView(R.id.et_app_current_password)
    TextInputEditText etAppCurrentPassword;
    @BindView(R.id.et_app_new_password)
    TextInputEditText etAppNewPassword;
    @BindView(R.id.btn_app_reset_password)
    MyButton btnAppResetPassword;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    List<ApiResponse.DataBean> resetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);

        initialize();

        connectApiToGetResetData();
    }

    private void initialize() {
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        linToolbar.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.reset_password));

        resetData = new ArrayList<>();

        prefData = new PrefData(ResetPassword.this);
        apiInterface = ApiClient.getClient(ResetPassword.this).create(ApiInterface.class);
        progressView = new ProgressView(ResetPassword.this);
        btnResetPassword.setOnClickListener(this);
        btnAppResetPassword.setOnClickListener(this);
        ivBack.setOnClickListener(this);

    }

    private void connectApiToGetResetData() {

        if (CheckNetworkConnection.isConnection1(ResetPassword.this, true)) {

            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.AdministratorDetailsHelp(PrefData.readStringPref(PrefData.security_token));
            call.enqueue(new Callback<ApiResponse>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            resetData.clear();

                            resetData.addAll(response.body().getData());

                            tvResetAdminName.setText(getResources().getString(R.string.admin_name) + resetData.get(0).getAdministratorName());
                            tvResetAdminMobile.setText(getResources().getString(R.string.admin_number) + resetData.get(0).getAdministratorMobile());
                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(ResetPassword.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG).show();
                                Utils.logout(ResetPassword.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootResetPassword, response.body().getMsg(), ResetPassword.this);
                            }


                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    t.printStackTrace();

                }
            });

        }
    }




    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_reset_password) {

            if (Validation.nullValidator(etCurrentPassword.getText().toString().trim())) {
                Utils.showSnackBar(rootResetPassword, getResources().getString(R.string.current_password), etCurrentPassword, ResetPassword.this);
            } else if (Validation.nullValidator(etNewPassword.getText().toString().trim())) {
                Utils.showSnackBar(rootResetPassword, getResources().getString(R.string.new_password), etNewPassword, ResetPassword.this);
            } else if (!Validation.passValidator(etNewPassword.getText().toString().trim())) {
                Utils.showSnackBar(rootResetPassword, getResources().getString(R.string.password_six_characters), etNewPassword, ResetPassword.this);
            } else {
                connectApiToResetPassword(etCurrentPassword.getText().toString().trim(), etNewPassword.getText().toString().trim());
            }
        } else if (v.getId() == R.id.btn_app_reset_password) {

            if (Validation.nullValidator(etAppCurrentPassword.getText().toString().trim())) {
                Utils.showSnackBar(rootResetPassword, getResources().getString(R.string.please_enter_your_current_passcode), etAppCurrentPassword, ResetPassword.this);
            } else if (Validation.nullValidator(etAppNewPassword.getText().toString().trim())) {
                Utils.showSnackBar(rootResetPassword, getResources().getString(R.string.please_enter_your_new_passcode), etAppNewPassword, ResetPassword.this);
            }else{

                String currentPassword=PrefData.readStringPref(PrefData.lockPassword);
                if (currentPassword.equalsIgnoreCase("")){
                    PrefData.writeStringPref(PrefData.lockPassword,etAppNewPassword.getText().toString());
                    etAppCurrentPassword.setText("");
                    etAppNewPassword.setText("");
                    Toast.makeText(this, "Your Password has been Changed Successfully", Toast.LENGTH_LONG).show();

                }else if(!currentPassword.equalsIgnoreCase("")){

                    if (currentPassword.equalsIgnoreCase(etAppCurrentPassword.getText().toString())){
                        PrefData.writeStringPref(PrefData.lockPassword,etAppNewPassword.getText().toString());
                        etAppCurrentPassword.setText("");
                        etAppNewPassword.setText("");
                        Toast.makeText(this, "Your Password has been Changed Successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "Current password is wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } else if (v.getId() == R.id.iv_back) {
            onBackPressed();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void connectApiToResetPassword(String oldPass, String newPass) {
        if (CheckNetworkConnection.isConnection1(ResetPassword.this, true)) {

            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.updatePassword(
                    PrefData.readStringPref(PrefData.security_token),
                    oldPass,
                    newPass);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {

                        if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {

                            etCurrentPassword.setText("");
                            etNewPassword.setText("");

                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                            Utils.showSnackBar(rootResetPassword, getResources().getString(R.string.password_updated), ResetPassword.this);

                        } else {

                            if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                Toast.makeText(ResetPassword.this, getResources().getString(R.string.login), Toast.LENGTH_LONG).show();
                                Utils.logout(ResetPassword.this, LoginActivity.class);
                            } else {
                                Utils.showSnackBar(rootResetPassword, response.body().getMsg(), ResetPassword.this);
                            }
                        }

                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.bad_request), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 500) {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.network_busy), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 404) {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPassword.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    t.printStackTrace();

                }
            });

        }
    }
}
