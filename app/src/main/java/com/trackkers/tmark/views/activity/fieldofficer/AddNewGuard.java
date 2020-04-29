package com.trackkers.tmark.views.activity.fieldofficer;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.trackkers.tmark.R;
import com.trackkers.tmark.customviews.MyEdittext;
import com.trackkers.tmark.customviews.MyTextview;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.helper.Validation;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewGuard extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    MyTextview tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_name)
    MyEdittext etName;
    @BindView(R.id.et_mobile_number)
    MyEdittext etMobileNumber;
    @BindView(R.id.et_address)
    MyEdittext etAddress;
    @BindView(R.id.cb_live_tracking)
    CheckBox cbLiveTracking;
    @BindView(R.id.btn_add_guard)
    Button btnAddGuard;
    @BindView(R.id.et_emp_code)
    MyEdittext etEmpCode;
    @BindView(R.id.et_pass)
    MyEdittext etPassword;
    @BindView(R.id.rb_guard)
    RadioButton rbGuard;
    @BindView(R.id.rb_guard_only)
    RadioButton rbGuardOnly;
    @BindView(R.id.rg_guard_type)
    RadioGroup rgGuardType;
    @BindView(R.id.root_add_guard)
    LinearLayout rootAddGuard;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    private boolean passwordShown = false;

    String liveTrackingEnabled = "0";

    String guardType = "Guard";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_guard);
        ButterKnife.bind(this);

        initialization();

        etPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPasswordViewToggle();
            }
        });

        rgGuardType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_guard:
                        guardType = "Guard";
                        break;
                    case R.id.rb_guard_only:
                        guardType = "Guards(Only Operationals)";
                        break;
                }
            }
        });

        btnAddGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (guardType.equalsIgnoreCase("")) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.guard_type), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                }

                if (cbLiveTracking.isChecked()) {
                    liveTrackingEnabled = "1";
                } else {
                    liveTrackingEnabled = "0";
                }

                if (Validation.nullValidator(etEmpCode.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.enter_employee_code), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (Validation.nullValidator(etName.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.enter_guard_name), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (Validation.nullValidator(etMobileNumber.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.enter_mobile_number), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (!Validation.mobileValidator(etMobileNumber.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.mobile_number_10_digits), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (Validation.nullValidator(etAddress.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.enter_address), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (Validation.nullValidator(etPassword.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.fill_password), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else if (!Validation.passValidator(etPassword.getText().toString())) {
                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.password_6_digits), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                } else {
                    connectApiToAddGuard(etEmpCode.getText().toString(), etName.getText().toString(), etMobileNumber.getText().toString(), etAddress.getText().toString(), etPassword.getText().toString());
                }
            }
        });
    }

    private void initialization() {
        setSupportActionBar(toolbar);
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getResources().getString(R.string.add_new_guard));

        prefData = new PrefData(AddNewGuard.this);
        apiInterface = ApiClient.getClient(AddNewGuard.this).create(ApiInterface.class);
        progressView = new ProgressView(AddNewGuard.this);


    }


    private void addPasswordViewToggle() {
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2; //index

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (passwordShown) {
                            passwordShown = false;
                            // 129 is obtained by bitwise ORing InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                            etPassword.setInputType(129);

                            // Need to call following as the font is changed to mono-space by default for password fields
                            etPassword.setTypeface(Typeface.SANS_SERIF);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_red_eye_black_24dp, 0); // This is lock icon
                        } else {
                            passwordShown = true;
                            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_red_eye_black_24dp, 0); // Unlock icon
                        }

                        return true;
                    }
                }
                return false;
            }
        });
    }


    private void connectApiToAddGuard(String empCode, String guardName, String mobileNumber, String address, String password) {
        if (CheckNetworkConnection.isConnection1(AddNewGuard.this, true)) {

            progressView.showLoader();
            Call<ApiResponse> call = apiInterface.addFiledOfficerGuard(
                    PrefData.readStringPref(PrefData.security_token),
                    guardName,
                    mobileNumber,
                    address,
                    liveTrackingEnabled,
                    empCode,
                    password,
                    guardType
            );

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase(getString(R.string.success))) {
                                Utils.showToast(AddNewGuard.this, getResources().getString(R.string.guard_added_successfully), Toast.LENGTH_LONG, getResources().getColor(R.color.colorLightGreen), getResources().getColor(R.color.colorWhite));

                                etEmpCode.setText("");
                                etName.setText("");
                                etMobileNumber.setText("");
                                etAddress.setText("");
                                etPassword.setText("");
                                cbLiveTracking.setChecked(false);
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(AddNewGuard.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(AddNewGuard.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(AddNewGuard.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(AddNewGuard.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(AddNewGuard.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(AddNewGuard.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(AddNewGuard.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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
}
