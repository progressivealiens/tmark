package com.trackkers.tmark.helper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class PrefData extends Application {

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor editor;

    public static Date noReallyThisIsTheTrueDateAndTime = null;

    private static PrefData mInstance;
    private static String sharedPrefName = "Tmark";

    public static String PREF_LOGINSTATUS = "pref_loginstatus";
    public static String PREF_selected_language = "pref_selected_language";
    public static String isFirstTimeRun = "pref_isFirstTimeRun";
    public static String lockPassword = "pref_lockPassword";

    public static String security_token = "pref_security_token";
    public static String firebase_token = "pref_firebase_token";
    public static String company_logo = "pref_company_logo";
    public static String employee_type = "pref_employee_type";
    public static String employee_name = "pref_employee_name";
    public static String company_name = "pref_company_name";
    public static String company_email = "pref_company_email";
    public static String employee_id = "pref_employee_id";
    public static String employee_code = "pref_employee_code";
    public static String site_code = "pref_site_code";
    public static String route_code = "pref_route_code";
    public static String route_id = "pref_route_id";
    public static String route_id_service = "pref_route_id_service";
    public static String route_name = "pref_route_name";
    public static String route_name_service = "pref_route_name_service";
    public static String route_start_address = "pref_route_start_address";
    public static String route_end_address = "pref_route_end_address";
    public static String site_name = "pref_site_name";
    public static String checkin_time = "pref_checkin_time";
    public static String timeInterval = "pref_time_interval";
    public static String checkpoint_id = "pref_checkpoint_id";
    public static String current_trip = "pref_current_trip";
    public static String checkpoint_id_position = "pref_checkpoint_id_position";
    public static String deviceID = "pref_device_id";
    public static String guard_name = "pref_guard_name";
    public static String switchoff_time = "pref_switchoff_time";
    public static String switchoff_battery_status = "pref_switchoff_batter_status";
    public static String autostartup = "pref_autostartup";
    public static String suid = "pref_suid";
    public static String site_attach_image_position = "pref_site_attach_image_position";
    public static String last_checkin_fo = "pref_last_checkin_fo";
    public static String total_scan_count_fo = "pref_total_scan_count_fo";
    public static String last_checkin_operations = "pref_last_checkin_operations";
    public static String total_scan_count_operations = "pref_total_scan_count_operations";
    public static String e_register_checkin_position = "pref_e_register_checkin_position";

    public PrefData() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;

        setExactTime();
    }

    public static String setExactTime() {

        noReallyThisIsTheTrueDateAndTime = new Date();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TrueTime.build().initialize();

                    if (TrueTime.isInitialized()) {
                        noReallyThisIsTheTrueDateAndTime = TrueTime.now();
                        //Log.e("exactDateIs", noReallyThisIsTheTrueDateAndTime.toString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return noReallyThisIsTheTrueDateAndTime.toString();
    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static synchronized PrefData getInstance() {
        return mInstance;
    }

    public PrefData(Context con) {
        mSharedPreferences = con.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
    }

    public void clear() {
        editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void clearPref() {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void clearKeyPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public static String readStringPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        return mSharedPreferences.getString(key, "");
    }

    public static void writeStringPref(String key, String data) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        editor = mSharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();

    }

    public static boolean readBooleanPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        return mSharedPreferences.getBoolean(key, false);


    }

    public static void writeBooleanPref(String key, boolean data) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        editor = mSharedPreferences.edit();
        editor.putBoolean(key, data);
        editor.apply();

    }

    public static long readLongPref(String key) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        return mSharedPreferences.getLong(key, 0);
    }

    public static void writeLongPref(String key, long data) {
        mSharedPreferences = mInstance.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        editor = mSharedPreferences.edit();
        editor.putLong(key, data);
        editor.apply();

    }

    public SharedPreferences getmSharedPreferences() {
        return mSharedPreferences;
    }

    public void setmSharedPreferences(SharedPreferences mSharedPreferences) {
        PrefData.mSharedPreferences = mSharedPreferences;
    }

    public String getSharedPrefName() {
        return sharedPrefName;
    }

    public void setSharedPrefName(String sharedPrefName) {
        PrefData.sharedPrefName = sharedPrefName;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public void setEditor(SharedPreferences.Editor editor) {
        PrefData.editor = editor;
    }

    public static String getPrefLoginstatus() {
        return PREF_LOGINSTATUS;
    }

    public static void setPrefLoginstatus(String prefLoginstatus) {
        PREF_LOGINSTATUS = prefLoginstatus;
    }

    public static String getSecurity_token() {
        return security_token;
    }

    public static void setSecurity_token(String security_token) {
        PrefData.security_token = security_token;
    }

    public static String getEmployee_type() {
        return employee_type;
    }

    public static void setEmployee_type(String employee_type) {
        PrefData.employee_type = employee_type;
    }

    public static String getCompany_name() {
        return company_name;
    }

    public static void setCompany_name(String company_name) {
        PrefData.company_name = company_name;
    }

    public static String getRoute_id() {
        return route_id;
    }

    public static void setRoute_id(String route_id) {
        PrefData.route_id = route_id;
    }

    public static String getDeviceID() {
        return deviceID;
    }

    public static void setDeviceID(String deviceID) {
        PrefData.deviceID = deviceID;
    }

    public static String getCheckpoint_id() {
        return checkpoint_id;
    }

    public static void setCheckpoint_id(String checkpoint_id) {
        PrefData.checkpoint_id = checkpoint_id;
    }

}
