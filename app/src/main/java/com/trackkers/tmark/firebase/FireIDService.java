package com.trackkers.tmark.firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FireIDService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("Refreshed token: ", token);
    }

   /* @Override
    public void onTokenRefresh()
    {
        String tkn = FirebaseInstanceId.getInstance().getToken();
        System.out.println("Hello     "+ tkn);
        Log.e("token",tkn);
    }*/
}
