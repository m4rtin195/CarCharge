package com.martin.carcharge.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.martin.carcharge.App;
import com.martin.carcharge.G;
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
    ScheduledTask task;
    
    public Downloader(Context context)
    {
        this.context = context;
        
        pref = App.getPreferences();
        api = App.getApi();
        
        executor = Executors.newSingleThreadScheduledExecutor();
    }
    
    public boolean start(long vehicleId, Listener listener)
    {
        long interval = pref.getInt(G.PREF_UPDATE_INTERVAL, 0);
        if(interval > 0)
        {
            task = new ScheduledTask(vehicleId, listener);
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
            start(task.vehicleId, task.listener);
            return true;
        }
        else
            return false;
    }
    
    public void downloadLast(long vehicleId, Listener listener)
    {
        executor.submit(runnableCreator(vehicleId, listener));
    }
    
    public boolean downloadRange(long vehicleId, Timestamp timestampFrom, Timestamp timestampTo, RangeListener listener)
    {
        executor.submit(runnableRangeCreator(vehicleId, timestampFrom, timestampTo, listener));
        return true;
    }
    
    
    
    public Runnable runnableCreator(long vehicleId, Listener listener)
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
    
    public Runnable runnableRangeCreator(long vehicleId, Timestamp timestampFrom, Timestamp timestampTo, RangeListener listener)
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
    
    private class ScheduledTask
    {
        long vehicleId; //todo moze byt protected?
        Listener listener;
        
        Runnable runnable;
        ScheduledFuture<?> taskFuture;
        
        ScheduledTask(long vehicleId, Listener listener)
        {
            this.vehicleId = vehicleId;
            this.listener = listener;
            runnable = runnableCreator(vehicleId, listener);
        }
    }
}