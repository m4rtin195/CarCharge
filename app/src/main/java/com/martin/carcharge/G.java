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
    public static final String NOTIFICATION_CHANNELID = "com.martin.carcharge";
    public static final String ACTION_BROADCAST_UPDATE = "com.martin.carcharge.FcmReceiver.update";
    public static final String EXTRA_USER = "com.martin.carcharge.LoginActivity.user";
    public static final String EXTRA_USER_JUST_LOGGED = "com.martin.carcharge.LoginActivity.isNew";
    public static final String EXTRA_JSON = "json";
    public static final int RC_FROM_NOTIF = 9000;
    public static final int RC_SIGN_IN = 9001;
    public static final int RC_FILE_PICKER = 9002;
    public static final int FAB_FLASH = 5001;
    public static final int FAB_PLUS = 5002;
    public static final int FAB_REFRESH = 5003;
    
    //not binded with xml!!
    public static final String PREF_DEBUG_ENABLED = "debug";
    public static final String PREF_FCM_ENABLED = "fcm_enabled";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_LAST_VEHICLE_ID = "last_vehicle_id";
    public static final String PREF_UPDATE_INTERVAL = "update_interval";
    public static final String PREF_USER_NICKNAME = "user_nickname";
    public static final String PREF_USER_ICON = "user_icon";
    public static final String PREF_VEHICLE_NAME = "vehicle_name";
    public static final String PREF_VEHICLE_REGPLATE = "vehicle_regplate";
    public static final String PREF_VEHICLE_MAX_VOLTAGE = "vehicle_max_voltage";
    public static final String PREF_VEHICLE_IMAGE = "vehicle_image";
    public static final String PREF_ACTUALITY_THRESHOLD = "actuality_threshold";
    public static final String PREF_INVALIDATE_CACHE = "invalidate_cache";
    public static final String PREF_APP_VERSION = "app_version";
    public static final String PREF_CONTACT_DEVELOPER = "contact_developer";
    public static final String PREF_LOGOUT = "logout";
    
    public static final String FIRESTORE_LAST_LOGGED_IN = "last_logged_in";
    public static final String FIRESTORE_DEBUG_ENABLED = PREF_DEBUG_ENABLED;
    public static final String FIRESTORE_FCM_ENABLED = PREF_FCM_ENABLED;
    public static final String FIRESTORE_LANGUAGE = PREF_LANGUAGE;
    public static final String FIRESTORE_UPDATE_INTERVAL = PREF_UPDATE_INTERVAL;
    public static final String FIRESTORE_ACTUALITY_THRESHOLD = PREF_ACTUALITY_THRESHOLD;
    public static final String FIRESTORE_VEHICLE_NAME = "name";
    public static final String FIRESTORE_VEHICLE_REGPLATE = "regNumber";
    public static final String FIRESTORE_VEHICLE_MAX_VOLTAGE = "maxVoltage";
    public static final String FIRESTORE_VEHICLE_IMAGE = "imageFilename";
    
    
    public static void debug(Context context, String string, Boolean... vibrate)
    {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(G.PREF_DEBUG_ENABLED, false))
        {
            if((vibrate.length>0 && !vibrate[0]))
            {
                ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE))
                        .vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
    
            Date now = new Date();
            SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Toast.makeText(context, string + " (" + date.format(now) + ")", Toast.LENGTH_SHORT).show();
        }
    }
}
