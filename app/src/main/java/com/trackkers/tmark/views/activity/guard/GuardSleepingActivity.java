package com.trackkers.tmark.views.activity.guard;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.CheckNetworkConnection;
import com.trackkers.tmark.helper.ExitActivity;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.ProgressView;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.services.AlarmService;
import com.trackkers.tmark.views.activity.LoginActivity;
import com.trackkers.tmark.webApi.ApiClient;
import com.trackkers.tmark.webApi.ApiInterface;
import com.trackkers.tmark.webApi.ApiResponse;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuardSleepingActivity extends AppCompatActivity {

    Uri alarmUri;
    Ringtone ringtone;

    @BindView(R.id.btn_not_sleeping)
    Button btnNotSleeping;
    @BindView(R.id.tv_alarm_for)
    TextView tvAlarmFor;
    @BindView(R.id.root_guard_sleeping)
    LinearLayout rootGuardSleeping;

    PrefData prefData;
    ApiInterface apiInterface;
    ProgressView progressView;

    Handler handler;
    Vibrator vibrate;

    boolean sleepingButtonIsClicked = false;
    public ActivityManager activityManager;
    int numOfActivities = 0, totalActivitiesInStack = 0;
    String topActivity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_sleeping);
        ButterKnife.bind(this);

        initialize();
        tvAlarmFor.setText(getResources().getString(R.string.alarm_for) + " " + PrefData.readStringPref(PrefData.route_name_service));
        knowPresentStackActivities();
        vibrate = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate.vibrate(VibrationEffect.createOneShot(15000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrate.vibrate(15000);
        }

        alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        ringtone = RingtoneManager.getRingtone(GuardSleepingActivity.this, alarmUri);
        ringtone.play();

        btnNotSleeping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sleepingButtonIsClicked = true;
                ringtone.stop();
                vibrate.cancel();

                AlarmService.setAlarm(true, GuardSleepingActivity.this);

                if (totalActivitiesInStack > 1) {
                    finish();
                } else if (totalActivitiesInStack == 1) {
                    ExitActivity.exitApplication(GuardSleepingActivity.this);
                } else {
                    finish();
                }
            }
        });

        autoDismissTheAlarm();
    }

    private void initialize() {
        prefData = new PrefData(GuardSleepingActivity.this);
        apiInterface = ApiClient.getClient(GuardSleepingActivity.this).create(ApiInterface.class);
        progressView = new ProgressView(GuardSleepingActivity.this);
    }

    private void autoDismissTheAlarm() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sleepingButtonIsClicked) {
                    sleepingButtonIsClicked = false;
                } else {
                    vibrate.cancel();
                    connectApiToCallAlarmMissingGuard();
                }
            }
        }, 15000);
    }

    private void connectApiToCallAlarmMissingGuard() {
        if (CheckNetworkConnection.isConnection1(GuardSleepingActivity.this, true)) {

            progressView.showLoader();

            Call<ApiResponse> call = apiInterface.AlarmMissingGuard(
                    PrefData.readStringPref(PrefData.security_token),
                    PrefData.readStringPref(PrefData.route_id));

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressView.hideLoader();

                    try {
                        if (response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equalsIgnoreCase("success")) {
                                ringtone.stop();
                                AlarmService.setAlarm(true, GuardSleepingActivity.this);
                                if (totalActivitiesInStack > 1) {
                                    finish();
                                } else if (totalActivitiesInStack == 1) {
                                    ExitActivity.exitApplication(GuardSleepingActivity.this);
                                } else {
                                    finish();
                                }
                            } else {
                                if (response.body().getMsg().toLowerCase().equalsIgnoreCase("invalid token")) {
                                    Utils.showToast(GuardSleepingActivity.this, getResources().getString(R.string.login_session_expired), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                    Utils.logout(GuardSleepingActivity.this, LoginActivity.class);
                                } else {
                                    Utils.showToast(GuardSleepingActivity.this, response.body().getMsg(), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (response.code() == 400) {
                            Utils.showToast(GuardSleepingActivity.this, getResources().getString(R.string.bad_request), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 500) {
                            Utils.showToast(GuardSleepingActivity.this, getResources().getString(R.string.network_busy), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else if (response.code() == 404) {
                            Utils.showToast(GuardSleepingActivity.this, getResources().getString(R.string.resource_not_found), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
                        } else {
                            Utils.showToast(GuardSleepingActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG, getResources().getColor(R.color.colorPink), getResources().getColor(R.color.colorWhite));
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

    public void knowPresentStackActivities() {
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList = activityManager.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();

        for (int i = 0; i < runningTaskInfoList.size(); i++) {
            int id = runningTaskInfoList.get(i).id;
            CharSequence desc = runningTaskInfoList.get(i).description;
            numOfActivities = runningTaskInfoList.get(i).numActivities;
            topActivity = runningTaskInfoList.get(i).topActivity.getShortClassName();
        }
        totalActivitiesInStack = runningTaskInfoList.get(0).numActivities;
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
