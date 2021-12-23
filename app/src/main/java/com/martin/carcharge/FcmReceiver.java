package com.martin.carcharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.storage.Converters;

import java.lang.reflect.Type;

public class FcmReceiver extends BroadcastReceiver
{
    private final MainViewModel vm;
    
    FcmReceiver()
    {
        vm = App.getViewModel();
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Type type = new TypeToken<VehicleStatus>() {}.getType(); //VehicleStatus.class
        String json = intent.getStringExtra(G.EXTRA_JSON);
        VehicleStatus vs = Converters.getGsonConverter().fromJson(json, VehicleStatus.class);
        vm.updateVehicleStatus(vs);
        G.debug(context, context.getString(R.string.toast_fcm_update), false);
    }
}
