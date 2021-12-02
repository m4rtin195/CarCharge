package com.martin.carcharge.network;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.G;
import com.martin.carcharge.models.VehicleStatus;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

public class FcmService extends FirebaseMessagingService
{
    LocalBroadcastManager lbm;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }
    
    @Override
    public void onNewToken(@NonNull String token)
    {
        Log.d(G.tag, "Refreshed token: " + token);
    
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref.edit().putString(G.PREF_FCM_TOKEN, token).apply();
        
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
            
            Intent intent = new Intent(G.ACTION_BROADCAST_UPDATE);
            intent.putExtra(G.EXTRA_JSON, object.toString());
            lbm.sendBroadcast(intent);
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
