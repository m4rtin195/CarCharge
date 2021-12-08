package com.martin.carcharge.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.models.FcmRegistration;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class ApiClient
{
    private final CloudRestAPI rawAPI;
    
    private final ExecutorService executor;
    private final String FCM_token;
    private String userId;
    
    public ApiClient(CloudRestAPI rawAPI)
    {
        this.rawAPI = rawAPI;
        executor = Executors.newCachedThreadPool();
        
        FCM_token = App.getPreferences().getString(G.PREF_FCM_TOKEN, "");
        userId = FirebaseAuth.getInstance().getUid();
    }
    
    public void resetAuthUid()
    {
        userId = FirebaseAuth.getInstance().getUid();
    }
    
    public CloudRestAPI getRawAPI()
    {
        return rawAPI;
    }
    
    public CompletableFuture<VehicleStatus> getLastStatus(String userId, String vehicleId)
    {
        CompletableFuture<VehicleStatus> future = new CompletableFuture<>();
        
        executor.submit(() ->
        {
            Call<VehicleStatus> call = rawAPI.getLastStatus(userId, vehicleId);
            try
            {
                Response<VehicleStatus> response = call.execute(); //blocking
                if(response.isSuccessful())
                    future.complete(response.body());
                else
                {
                    //todo
                    //Log.w(G.tag, "Error registering fcm token for vehicle: " + vehicleId + "\n" + response.errorBody());
                    future.complete(null);
                    future.completeExceptionally(new Throwable("aaa"));
                }
            }
            catch(IOException e)
            {
                Log.e(G.tag, "Error in Retrofit callback: " + e.getMessage());
                e.printStackTrace();
                future.complete(null);
            }
        });
        
        return future;
    }
    
    public CompletableFuture<List<VehicleStatus>> getRangeStatuses(String userId, String vehicleId, Date timestampFrom, Date timestampTo)
    {
        CompletableFuture<List<VehicleStatus>> future = new CompletableFuture<>();
        
        //todo ...
        
        return future;
    }
    
    public CompletableFuture<Boolean> registerFcm(String vehicleId)
    {
        return _registerFcm(vehicleId, FcmRegistration.Method.REGISTER);
    }
    
    public CompletableFuture<Void> registerFcm(List<Vehicle> vehicles)
    {
        return _registerFcm(vehicles, FcmRegistration.Method.REGISTER);
    }
    
    public CompletableFuture<Boolean> unregisterFcm(String vehicleId)
    {
        return _registerFcm(vehicleId, FcmRegistration.Method.UNREGISTER);
    }
    
    public CompletableFuture<Void> unregisterFcm(List<Vehicle> vehicles)
    {
        return _registerFcm(vehicles, FcmRegistration.Method.UNREGISTER);
    }
    
    //one vehicle
    private CompletableFuture<Boolean> _registerFcm(String vehicleId, FcmRegistration.Method method)
    {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FcmRegistration request = new FcmRegistration(vehicleId, FCM_token, method);
        
        executor.submit(() ->
        {
            Call<Void> call = rawAPI.fcmRegister(userId, vehicleId, request);
            try
            {
                Response<Void> response = call.execute(); //blocking
                if(response.isSuccessful())
                    future.complete(true);
                else
                {
                    //todo log to user
                    Context context = null;
                    //((Activity)context).requireViewById() //how to get root layout
                    Log.w(G.tag, "Error registering fcm token for vehicle: " + vehicleId + "\n" + response.errorBody());
                    future.complete(false);
                }
            }
            catch(IOException e)
            {
                Log.e(G.tag, "Error in Retrofit callback: " + e.getMessage());
                e.printStackTrace();
                future.complete(false);
            }
        });
        
        return future;
    }
    
    //array
    private CompletableFuture<Void> _registerFcm(List<Vehicle> vehicles, FcmRegistration.Method method)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            List<CompletableFuture<?>> operations = new ArrayList<>();
            for(Vehicle v : vehicles)
            {
                CompletableFuture<?> f = _registerFcm(v.getId(), method);
                operations.add(f);
            }
            if(operations.size() > 0)
                CompletableFuture.allOf(operations.toArray(new CompletableFuture[0])).join();
            
            return null;
        });
    }
}
