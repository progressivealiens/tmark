package com.trackkers.tmark.firebase;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trackkers.tmark.R;
import com.trackkers.tmark.helper.PrefData;
import com.trackkers.tmark.services.LiveTrackingForOperations;
import com.trackkers.tmark.views.activity.LockActivity;
import com.trackkers.tmark.views.activity.ShowNotificationActivity;
import com.trackkers.tmark.views.activity.fieldofficer.FOMarkAttendance;
import com.trackkers.tmark.views.activity.fieldofficer.SiteDetailsActivity;
import com.trackkers.tmark.views.activity.guard.GMainActivity;
import com.trackkers.tmark.views.activity.operations.OperationsMainActivity;

/**
 * Created by ankus on 29/05/2017.
 */

public class FireMsgService extends FirebaseMessagingService {

    public int NOTIFICATION_CHANNEL_ID = 1234;
    Intent intent;
    String title = "", subject = "", body = "", flag = "", notificationType = "0";
    Uri defaultSoundUri;
    PendingIntent pendingIntent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.e("firebaseMessage", "" + remoteMessage.getData());

        if (remoteMessage.getData() != null) {
            receiveData(remoteMessage);
        }
    }

    private void receiveData(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            subject = remoteMessage.getData().get("subject");
            body = remoteMessage.getData().get("body");
            flag = remoteMessage.getData().get("flag");
            notificationType = remoteMessage.getData().get("notificationType");

            if (flag != null && flag.equalsIgnoreCase("working")) {
                if (FOMarkAttendance.isServiceRunning) {
                    startService(new Intent(this, LiveTrackingForOperations.class));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    createNotificationAboveOreo(0);
                else
                    createNotificationBelowOreo(0);

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    createNotificationAboveOreo(1);
                else
                    createNotificationBelowOreo(1);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationAboveOreo(int Flag) {

        try {
            if (Flag == 1) {
                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
                r.play();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (notificationType != null) {
            if (notificationType.equalsIgnoreCase("checkin")) {
                if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("Field Officer")) {
                    intent = new Intent(this, FOMarkAttendance.class);
                } else if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("Guard")) {
                    intent = new Intent(this, GMainActivity.class);
                } else {
                    intent = new Intent(this, OperationsMainActivity.class);
                }
            } else if (notificationType.equalsIgnoreCase("show")) {
                intent = new Intent(this, ShowNotificationActivity.class);
                intent.putExtra("NotificationTitle", title);
                intent.putExtra("NotificationSubject", subject);
                intent.putExtra("NotificationMessage", body);
            } else if (notificationType.equalsIgnoreCase("site")) {
                intent = new Intent(this, SiteDetailsActivity.class);
            }
        } else {
            intent = new Intent(this, LockActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CHANNEL_ID, intent, PendingIntent.FLAG_ONE_SHOT);

        CharSequence name = "Tmark";
        String description = "Notification";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel((String.valueOf(NOTIFICATION_CHANNEL_ID)), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManage = getSystemService(NotificationManager.class);
        notificationManage.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(NOTIFICATION_CHANNEL_ID))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.t_mark_logo_final)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(subject + "\n" + body))
                .setAutoCancel(true)
                .setLights(Color.BLUE, 500, 500)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_CHANNEL_ID, builder.build());


    }

    private void createNotificationBelowOreo(int Flag) {

        try {
            if (Flag == 1) {
                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
                r.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (notificationType != null) {
            if (notificationType.equalsIgnoreCase("checkin")) {
                if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("Field Officer")) {
                    intent = new Intent(this, FOMarkAttendance.class);
                } else if (PrefData.readStringPref(PrefData.employee_type).equalsIgnoreCase("Guard")) {
                    intent = new Intent(this, GMainActivity.class);
                } else {
                    intent = new Intent(this, OperationsMainActivity.class);
                }
            } else if (notificationType.equalsIgnoreCase("show")) {
                intent = new Intent(this, ShowNotificationActivity.class);
                intent.putExtra("NotificationTitle", title);
                intent.putExtra("NotificationSubject", subject);
                intent.putExtra("NotificationMessage", body);
            } else if (notificationType.equalsIgnoreCase("site")) {
                intent = new Intent(this, SiteDetailsActivity.class);
            }
        } else {
            intent = new Intent(this, LockActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CHANNEL_ID, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.ic_notification_icon_tmark)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(subject + "\n" + body))
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, mBuilder.build());
    }

}

