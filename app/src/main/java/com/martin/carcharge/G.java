package com.martin.carcharge;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class G
{
    public static final String tag = "daco";
    public static final String ACTION_BROAD_UPDATE = "com.martin.carcharge.FcmReceiver.update";
    public static final String BOTTOM_DRAWER_TAG = "com.martin.carcharge.BottomVehiclesDrawer.tag";
    public static final String EXTRA_USER = "com.martin.carcharge.LoginActivity.user";
    public static final String EXTRA_ISNEW = "com.martin.carcharge.LoginActivity.isNew";
    public static final int RC_SIGN_IN = 9001;
    public static final int RC_FILE_PICKER = 9002;
    public static final int FAB_FLASH = 5001;
    public static final int FAB_PLUS = 5002;
    public static final int FAB_REFRESH = 5003;
    
    //not bind with xml!!
    public static final String PREF_DEBUG = "debug";
    public static final String PREF_FCM_ENABLED = "fcm_enabled";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_LAST_VEHICLE_ID = "last_vehicle_id";
    public static final String PREF_UPDATE_INTERVAL = "update_interval";
    public static final String PREF_USER_NICKNAME = "user_nickname";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_ICON = "user_icon";
    public static final String PREF_VEHICLE_NAME = "vehicle_name";
    public static final String PREF_VEHICLE_REGPLATE = "vehicle_regplate";
    public static final String PREF_VEHICLE_CAPACITY = "vehicle_capacity";
    public static final String PREF_VEHICLE_IMAGE = "vehicle_image";
    
    
    public static void debug(Context context, String string, Boolean... vibrate)
    {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(G.PREF_DEBUG, false))
        {
            if((vibrate.length>0 && vibrate[0]) || vibrate.length==0)
            {
                ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE))
                        .vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
    
            Date now = new Date();
            SimpleDateFormat date = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            Toast.makeText(context, string + " (" + date.format(now) + ")", Toast.LENGTH_SHORT).show();
        }
    }
}
