package com.martin.carcharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

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
        String json = intent.getStringExtra("json");
        Type type = new TypeToken<VehicleStatus>() {}.getType();
        VehicleStatus vs = new Gson().fromJson(json, type);
        vs.setState(VehicleStatus.State.Charging); //todo prec
        vm.updateVehicleStatus(vs);
        
        G.debug(context, context.getString(R.string.toast_fcm_update), false);
    }
}
