package com.trackkers.tmark.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.helper.Utils;
import com.trackkers.tmark.model.guard.LocationModel;
import com.trackkers.tmark.views.activity.guard.GCheckpoints;
import com.trackkers.tmark.views.activity.guard.GuardSleepingActivity;

public class AlarmService extends Service {

    public static final String CUSTOM_INTENT = "com.test.intent.action.ALARM";

    public static FusedLocationProviderClient client;
    public static LocationCallback locationCallback;
    public static LocationRequest request;
    FirebaseAuth mAuth;
    String currentTimeStamp, currentDateTime;

    public AlarmService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null && location.hasAccuracy()) {
                    if (location.getAccuracy() <= 40) {

                        currentTimeStamp = Utils.currentTimeStamp();
                        currentDateTime = Utils.selectedDateAndTime(Long.valueOf(currentTimeStamp));

                        LocationModel locationModel = new LocationModel(
                                location.getLatitude(),
                                location.getLongitude(),
                                PrefData.readStringPref(PrefData.employee_id),
                                PrefData.readStringPref(PrefData.guard_name),
                                PrefData.readStringPref(PrefData.route_name_service),
                                currentDateTime);
                        final String path = PrefData.readStringPref(PrefData.company_name) + "*" + PrefData.readStringPref(PrefData.route_id_service);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                        DatabaseReference locationOneRef = ref.child(PrefData.readStringPref(PrefData.employee_id));
                        locationOneRef.setValue(locationModel, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e("dataError", "Data could not be saved" + databaseError.getMessage());
                                } else {
                                    Log.e("dataSucess", "Data saved successfully.");
                                }
                            }
                        });
                    }
                }
            }
        };

        buildNotification();
        loginToFirebase();

    }

    private void buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startMyOwnForgroundServiceBelowOreo();
    }

    private void startMyOwnForgroundServiceBelowOreo() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.t_mark_logo_final)
                        .setContentTitle(getString(R.string.updating_your_locations));
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GCheckpoints.NOTIFICATION_CHANNEL_ID, mBuilder.build());
    }

    private void startMyOwnForeground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = getString(R.string.package_name);
            String channelName = getString(R.string.my_bk_service);
            NotificationChannel chan = null;

            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.t_mark_logo_final)
                    .setContentTitle(getString(R.string.updating_your_locations))
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(GCheckpoints.NOTIFICATION_CHANNEL_ID, notification);
        }
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    private void loginToFirebase() {
        String email = getString(R.string.test_email);
        String password = getString(R.string.test_password);
        mAuth.signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful() && task.isComplete()) {
                    requestLocUpdate();
                } else {
                    Log.e("loginFailedFirebase", task.getException().getMessage());
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestLocUpdate() {
        request = new LocationRequest();
        request.setInterval(30000);
        request.setFastestInterval(30000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(this);

        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    public static void removeDatabaseValues() {
        final String path = PrefData.readStringPref(PrefData.company_name) + "*" + PrefData.readStringPref(PrefData.route_id_service);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        DatabaseReference deleteToken = ref.child(PrefData.readStringPref(PrefData.employee_id));
        deleteToken.removeValue(new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e("dataError", "Data could not be deleted" + databaseError.getMessage());
                } else {
                    Log.e("dataSucess", "Data deleted successfully.");
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public static void stopLocationUpdate() {
        if (client != null) {
            client.removeLocationUpdates(locationCallback);
            client = null;
        }
    }

    public static void cancelAlarm(Context con) {
        AlarmManager alarm = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getPendingIntent(con));
    }

    public static void setAlarm(boolean force, Context con) {
        cancelAlarm(con);

        AlarmManager alarm = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

        long delay, when, checkinTime = 0, nextAlarm, remainingMilliToRing = 0;
        int timeInterval = 0;

        checkinTime = PrefData.readLongPref(PrefData.checkin_time);
        timeInterval = Integer.valueOf(PrefData.readStringPref(PrefData.timeInterval));

        if (force) {
            delay = (1000 * 60 * timeInterval);//delay in millisecond
            when = System.currentTimeMillis();//current time in milliseconds

            for (long i = checkinTime; i < when; i = i + delay) {
                remainingMilliToRing = i;
            }
            nextAlarm = remainingMilliToRing + delay;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, nextAlarm, getPendingIntent(con));
            } else {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarm, getPendingIntent(con));
            }
            /* fire the broadcast */
        }
    }

    private static PendingIntent getPendingIntent(Context conn) {
        Intent alarmIntent = new Intent(conn, MyReceiver.class);
        alarmIntent.setAction(CUSTOM_INTENT);
        return PendingIntent.getBroadcast(conn, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            myAlarmService.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, restartPendingIntent);
        } else {
            myAlarmService.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, restartPendingIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            myAlarmService.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, restartPendingIntent);
        } else {
            myAlarmService.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, restartPendingIntent);
        }
    }

    public static class MyReceiver extends BroadcastReceiver {

        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intent1 = new Intent(context, GuardSleepingActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }

}


