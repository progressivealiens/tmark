package com.trackkers.tmark.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.views.activity.bulk_guard.BulkGuardMainActivity;
import com.trackkers.tmark.views.activity.fieldofficer.FOMainActivity;
import com.trackkers.tmark.views.activity.guard.GMainActivity;
import com.trackkers.tmark.views.activity.operations.OperationsMainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    @BindView(R.id.iv_logo)
    ImageView ivLogo;

    PrefData prefData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        startSplashScreenTimer();
    }

    private void startSplashScreenTimer() {
        prefData = new PrefData(SplashActivity.this);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!PrefData.readBooleanPref(PrefData.isFirstTimeRun)) {
                    startActivity(new Intent(SplashActivity.this, SelectLanguageActivity.class));
                } else {

                    if (PrefData.readBooleanPref(PrefData.PREF_LOGINSTATUS)) {

                        if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("Field Officer")) {
                            startActivity(new Intent(SplashActivity.this, FOMainActivity.class));
                        } else if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("Guard")) {
                            startActivity(new Intent(SplashActivity.this, GMainActivity.class));
                        } else if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("bulk_guard")) {
                            startActivity(new Intent(SplashActivity.this, BulkGuardMainActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, OperationsMainActivity.class));
                        }
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                }

                finish();
            }
        }, 3000);
    }
}
