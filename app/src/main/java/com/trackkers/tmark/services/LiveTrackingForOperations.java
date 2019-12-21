package com.trackkers.tmark.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
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
import com.trackkers.tmark.model.operations.LastUpdatedModel;
import com.trackkers.tmark.model.operations.LocationModelOperations;
import com.trackkers.tmark.receiver.RestartServiceReceiver;
import com.trackkers.tmark.views.activity.fieldofficer.FOMainActivity;

public class LiveTrackingForOperations extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    SettingsClient mSettingsClient;
    LocationSettingsRequest mLocationSettingsRequest;
    static FusedLocationProviderClient fusedLocationProviderClient;
    LocationSettingsRequest.Builder builder;
    static LocationCallback locationCallback;
    String currentTimeStamp, currentDateTime;
    boolean isFirstTimeRun=false;

    public LiveTrackingForOperations() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (mGoogleApiClient != null && fusedLocationProviderClient != null) {
            requestLocation();
        } else {
            buildGoogleApiClient();
        }

        buildNotification();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initialization();
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        buildGoogleApiClient();
        buildNotification();
        loginToFirebase();

    }

    private void initialization() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };

    }

    private void buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startMyOwnForgroundServiceBelowOreo();
    }

    private void startMyOwnForgroundServiceBelowOreo() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, String.valueOf(FOMainActivity.NOTIFICATION_CHANNEL_ID));
        Notification notification = mBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.t_mark_logo_final)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(getString(R.string.updating_your_location))
                .setOngoing(true)
                .build();
        startForeground(FOMainActivity.NOTIFICATION_CHANNEL_ID, notification);
    }

    private void startMyOwnForeground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = getString(R.string.my_bk_service);

            NotificationChannel chan = new NotificationChannel(String.valueOf(FOMainActivity.NOTIFICATION_CHANNEL_ID), channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, String.valueOf(FOMainActivity.NOTIFICATION_CHANNEL_ID));
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.t_mark_logo_final)
                    .setContentTitle(getString(R.string.updating_your_location))
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .build();
            startForeground(FOMainActivity.NOTIFICATION_CHANNEL_ID, notification);
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

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.e("firebase","user created successfully");

                mAuth.signInWithEmailAndPassword(
                        email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful() && task.isComplete()) {
                            requestLocation();
                            Log.e("loginSucessfullFirebase", "loginSucessfullFirebase");
                        } else {
                            Log.e("loginFailedFirebase", task.getException().getMessage());
                        }
                    }
                });
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        fusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                locationCallback,
                Looper.myLooper());
    }

    public static void removeDatabaseValues(Context context) {
        String path = PrefData.readStringPref(PrefData.company_name) + "*" + PrefData.readStringPref(PrefData.employee_type);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path).child(PrefData.readStringPref(PrefData.employee_id));
        ref.removeValue(new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                try{
                    if (databaseError != null) {
                        Log.e("dataError", "Data could not be deleted" + databaseError.getMessage());
                        Toast.makeText(context, "Error Deleting Values : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("dataSucess", "Data deleted successfully.");
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("firebaseFail",e.getMessage());
                    Toast.makeText(context, "Error Deleting Values", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public static void stopLocationUpdate() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null && location.hasAccuracy()) {
            if (location.getAccuracy() <= 40) {

                currentTimeStamp = Utils.currentTimeStamp();
                currentDateTime = Utils.selectedDateAndTime(Long.valueOf(currentTimeStamp));

                if (!isFirstTimeRun){
                    lastUpdatedonFirestore(location.getLatitude(),location.getLongitude());
                    isFirstTimeRun=true;
                }

                LocationModelOperations locationModel = new LocationModelOperations(
                        location.getLatitude(),
                        location.getLongitude(),
                        PrefData.readStringPref(PrefData.employee_id),
                        PrefData.readStringPref(PrefData.employee_name),
                        currentDateTime);

                String path = PrefData.readStringPref(PrefData.company_name) + "*" + PrefData.readStringPref(PrefData.employee_type);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path).child(PrefData.readStringPref(PrefData.employee_id));
                ref.setValue(locationModel, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
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


    private void lastUpdatedonFirestore(double lati,double longi) {

        currentTimeStamp = Utils.currentTimeStamp();
        currentDateTime = Utils.selectedDateAndTime(Long.valueOf(currentTimeStamp));
        String luUpdated=lati+","+longi+",0,"+currentDateTime;

        LastUpdatedModel lastUpdatedModel=new LastUpdatedModel(PrefData.readStringPref(PrefData.firebase_token),luUpdated);

        final String path = "lu" + "*" + PrefData.readStringPref(PrefData.company_name) + "*" + PrefData.readStringPref(PrefData.employee_type);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path).child(PrefData.readStringPref(PrefData.employee_id));
        ref.setValue(lastUpdatedModel, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e("dataError", "Data could not be saved" + databaseError.getMessage());
                } else {
                    Log.e("dataSucessLU", "Data saved successfully.LU");
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

   /* public static void cancelAlarmForLocationUpdate(Context con) {
        AlarmManager alarm = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            alarm.cancel(getPendingIntent(con));
        }
    }

    public static void setAlarmForLocationUpdate(boolean force, Context con) {
        cancelAlarmForLocationUpdate(con);

        AlarmManager alarm = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);

        if (alarm != null) {
            if (force) {

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    alarm.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, getPendingIntent(con));
                } else {
                    AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 60 * 1000, getPendingIntent(con));
                    alarm.setAlarmClock(alarmClockInfo,getPendingIntent(con));

                   // alarm.setAlarmClock(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, getPendingIntent(con));
                }
            }
        }
    }

    private static PendingIntent getPendingIntent(Context conn) {
        Intent alarmIntent = new Intent(conn, MyLiveTrackingReceiver.class);
        alarmIntent.setAction(CUSTOM_INTENT);
        return PendingIntent.getBroadcast(conn, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static class MyLiveTrackingReceiver extends BroadcastReceiver {

        public MyLiveTrackingReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("fromTrackingReceiver", "fromLiveTrackingReceiver");
            setAlarmForLocationUpdate(true, context);

            Intent startService=new Intent(context, LiveTrackingForOperations.class);
            context.startService(startService);

        }
    }*/


    @Override
    public void onDestroy() {
        initAlarm();
        super.onDestroy();
        Log.e("onDestroyService", "onDestroyService");

        Intent broadIntent = new Intent(this, RestartServiceReceiver.class);
        sendBroadcast(broadIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        initAlarm();
        super.onTaskRemoved(rootIntent);
        Log.e("onTaskRemoved", "onTaskRemoved");

        Intent broadIntent = new Intent(this, RestartServiceReceiver.class);
        sendBroadcast(broadIntent);

    }

    private void initAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, RestartServiceReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0,intent, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, alarmIntent);
        } else {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, alarmIntent);
        }
    }
}
