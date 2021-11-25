package com.martin.carcharge.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class Downloader
{
    private final Context context;
    
    private final SharedPreferences pref;
    private final CloudRestAPI api;
    
    ScheduledExecutorService executor;
    ScheduledDownloaderTask task;
    
    public Downloader(Context context)
    {
        this.context = context;
        
        pref = App.getPreferences();
        api = App.getApi();
        
        executor = Executors.newSingleThreadScheduledExecutor();
    }
    
    //task actions
    public boolean start(Vehicle vehicle, Listener listener)
    {
        
        long interval = pref.getInt(G.PREF_UPDATE_INTERVAL, 0);
        if(interval > 0)
        {
            task = new ScheduledDownloaderTask(vehicle, listener);
            task.taskFuture = executor.scheduleAtFixedRate(task.runnable, 0, interval, TimeUnit.SECONDS);
            return true;
        }
        else
            return false;
    }
    
    public void stop(boolean... keepTask)
    {
        if(task != null)
        {
            task.taskFuture.cancel(false);
        }
        if(keepTask.length==0 || !keepTask[0])
            task = null;
    }
    
    public boolean restart()
    {
        if(task != null)
        {
            stop(true);
            start(task.vehicle, task.listener);
            return true;
        }
        else
            return false;
    }
    
    //one time actions
    public void downloadLast(Vehicle v, Listener listener)
    {
        executor.submit(runnableCreator(v.getId(), listener));
    }
    
    public boolean downloadRange(Vehicle v, Timestamp timestampFrom, Timestamp timestampTo, RangeListener listener)
    {
        executor.submit(runnableRangeCreator(v.getId(), timestampFrom, timestampTo, listener));
        return true;
    }
    
    
    //internal
    private Runnable runnableCreator(String vehicleId, Listener listener)
    {
        return () ->
        {
            Call<VehicleStatus> call = api.getActualStatus(vehicleId);
            try
            {
                Response<VehicleStatus> response = call.execute();
                ((Activity)context).runOnUiThread(() ->
                {
                    if(response.body() != null)
                        listener.onSuccess(response.body());
                    else
                        listener.onFail(response);
                });
            }
            catch(IOException e)
            {
                listener.onFail(null);
                Log.e(G.tag, "Error in Retrofit callback: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
    
    private Runnable runnableRangeCreator(String vehicleId, Timestamp timestampFrom, Timestamp timestampTo, RangeListener listener)
    {
        return () ->
        {
            Call<List<VehicleStatus>> call = api.getStatuses(vehicleId, timestampFrom, timestampTo);
            try
            {
                Response<List<VehicleStatus>> response = call.execute();
                ((Activity)context).runOnUiThread(() ->
                {
                    if(response.body() != null)
                        listener.onSuccess(response.body());
                    else
                        listener.onFail(response);
                });
            }
            catch(IOException e)
            {
                listener.onFail(null);
                Log.e(G.tag, "Error in Retrofit callback: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
    
    
    public interface Listener
    {
        void onSuccess(@NonNull VehicleStatus response);
        void onFail(@Nullable Response<?> response);
    }
    
    public interface RangeListener
    {
        void onSuccess(@NonNull List<VehicleStatus> vs);
        void onFail(@Nullable Response<?> response);
    }
    
    private class ScheduledDownloaderTask
    {
        Vehicle vehicle; //todo moze byt protected alego treba getset?
        Listener listener;
        
        Runnable runnable;
        ScheduledFuture<?> taskFuture;
        
        ScheduledDownloaderTask(Vehicle vehicle, Listener listener)
        {
            this.vehicle = vehicle;
            this.listener = listener;
            runnable = runnableCreator(vehicle.getId(), listener);
        }
    }
}