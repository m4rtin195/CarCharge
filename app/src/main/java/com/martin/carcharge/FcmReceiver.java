package com.martin.carcharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import java.lang.reflect.Type;

public class FcmReceiver extends BroadcastReceiver
{
    private SharedPreferences pref;
    private MainViewModel vm;
    Vibrator v;
    
    FcmReceiver(Context context)
    {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        vm = new ViewModelProvider((ViewModelStoreOwner)context).get(MainViewModel.class);
        v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String json = intent.getStringExtra("json");
        Type type = new TypeToken<VehicleStatus>() {}.getType();
        VehicleStatus vs = new Gson().fromJson(json, type);
        vs.setState(VehicleStatus.State.Charging);
        vm.postVehicleStatus(vs);
        
        G.debug(context, context.getString(R.string.toast_fcm_update));
    }
}
