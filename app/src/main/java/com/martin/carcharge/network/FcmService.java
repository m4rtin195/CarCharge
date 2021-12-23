package com.martin.carcharge.network;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

public class FcmService extends FirebaseMessagingService
{
    private MainViewModel vm;
    LocalBroadcastManager lbm;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        vm = App.getViewModel();
        Log.i("daco", "onCreate() FcmService, vm is: " + (vm == null));
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }
    
    @Override
    public void onNewToken(@NonNull String token)
    {
        Log.d(G.tag, "Refreshed FCM token: " + token);
    
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
            //JSONObject object = new JSONObject(params);
            JsonElement json = JsonParser.parseString(Objects.requireNonNull(params.get("serialized")));
            
            Intent intent = new Intent(G.ACTION_BROADCAST_FCMUPDATE);
            intent.putExtra(G.EXTRA_JSON, json.toString());
            
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
