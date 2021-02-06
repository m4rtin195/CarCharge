package com.martin.carcharge;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class G
{
    public static final String tag = "daco";
    public static final String ACTION_BROAD_UPDATE = "com.martin.carcharge.FcmReceiver.update";
    public static final String BOTTOM_DRAWER_TAG = "com.martin.carcharge.BottomVehiclesDrawer.tag";
    public static final int RC_SIGN_IN = 9001;
    public static final int RC_FILE_PICKER = 9002;
    
    
    public static void debug(Context context, String string, Boolean... vibrate)
    {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug", false))
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
