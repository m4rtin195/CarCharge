package com.martin.carcharge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

public class FcmService extends FirebaseMessagingService
{
    @Override
    public void onNewToken(@NotNull String token)
    {
        Log.d(G.tag, "Refreshed token: " + token);
    
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref.edit().putString("fcm_token", token).apply();
        
        //sendRegistrationToServer(token);
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Log.d(G.tag, "From: " + remoteMessage.getFrom());
        
        if(remoteMessage.getData().size() > 0)
        {
            Log.d(G.tag, "Message data payload: " + remoteMessage.getData());
            Map<String, String> params = remoteMessage.getData();
            Type type = new TypeToken<VehicleStatus>() {}.getType();
            JSONObject object = new JSONObject(params);
            
            //VehicleStatus vs = new Gson().fromJson(object.toString(), type);
    
            Intent myIntent = new Intent("custom-update");
            myIntent.putExtra("json", object.toString());
            this.sendBroadcast(myIntent);
        }
    }
    
    @Override
    public void onDeletedMessages()
    {
        Log.d(G.tag, "onDeletedMessages()");
    }
    
    
    void handle(Map<String, String> data)
    {
        
        //return new Gson().fromJson(aa, listType);
    }
}
