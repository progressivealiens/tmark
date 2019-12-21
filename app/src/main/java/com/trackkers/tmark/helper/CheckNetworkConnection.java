package com.trackkers.tmark.helper;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.trackkers.tmark.R;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


/**
 * @author RAM
 * <p/>
 * This class checks device is connected with internet or not.
 */
public class CheckNetworkConnection {


    public static boolean isConnection(Context ctx, @NonNull View view) {
        return isConnection(ctx, view, true);
    }

    public static boolean isConnection1(Context ctx) {
        return isConnection1(ctx, true);
    }

    public static boolean isReachable(Context ctx, @NonNull View view) {
        return isReachable(ctx, view, true);
    }


    public static boolean isConnection(Context ctx, @NonNull View view, boolean showToast) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isAvailable() && ni.isConnected()) {
            return true;
        } else {
            if (showToast)
                //  FunctionHelper.showSnackMessage(view, R.string.text_check_network);

                Toast.makeText(ctx, R.string.text_check_network, Toast.LENGTH_SHORT).show();

            return false;
        }
    }


    public static boolean isConnection1(Context ctx, boolean showToast) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isAvailable() && ni.isConnected()) {
            return true;
        } else {
            if (showToast)
                Toast.makeText(ctx, R.string.text_check_network, Toast.LENGTH_SHORT).show();

            return false;
        }
    }


    private static boolean isReachable(Context ctx, @NonNull View view, boolean showToast) {
        boolean connected = false;
        String instanceURL = "";//AppUrl.PUBLIC_BASE;
        Socket socket;
        try {
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(instanceURL, 80);
            socket.connect(socketAddress, 5000);
            if (socket.isConnected()) {
                connected = true;
                socket.close();
            } else {
                if (showToast)
                    // FunctionHelper.showSnackMessage(view, "Server Not Responding");
                    return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket = null;
        }
        return connected;
    }


}