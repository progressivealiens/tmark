package com.trackkers.tmark.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.trackkers.tmark.services.LiveTrackingForOperations;

public class RestartServiceReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("fromOperationsReceiver", "fromOperationsReceiver");

        context.startService(new Intent(context, LiveTrackingForOperations.class));

    }
}
