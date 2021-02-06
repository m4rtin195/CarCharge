package com.martin.carcharge;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.preference.PreferenceManager;

import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class Downloader
{
    //todo db
    private final Context context;
    private final SharedPreferences pref;
    private final MainViewModel vm;
    private final KitCloudAPI cloud_api;
    
    ScheduledExecutorService executor;
    ScheduledFuture<?> task;
    
    Downloader(Context context)
    {
        this.context = context;
        
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        vm = new ViewModelProvider((ViewModelStoreOwner) context).get(MainViewModel.class);
        cloud_api = AppActivity.getApi();
        
        executor = Executors.newSingleThreadScheduledExecutor();
    }
    
    public boolean start()
    {
        long interval = pref.getInt("update_interval", 0);
        if(interval > 0)
        {
            task = executor.scheduleAtFixedRate(runnable, 0, interval, TimeUnit.SECONDS); //todo change to minutes
            return true;
        } else
            return false;
    }
    
    public void stop()
    {
        if(task != null) task.cancel(false);
    }
    
    public void restart()
    {
        stop();
        start();
    }
    
    public void download()
    {
        executor.execute(runnable);
    }
    
    final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            Call<VehicleStatus> call = cloud_api.getActual();
            try
            {
                Response<VehicleStatus> response = call.execute();
                if(response.isSuccessful())
                {
                    Log.i(G.tag, "HTTP poziadavka uspesna");
    
                    VehicleStatus vs = response.body();
                    vm.postVehicleStatus(vs);
                    
                    ((Activity)context).runOnUiThread(() ->
                            G.debug(context, context.getString(R.string.toast_refreshed), false));
                }
                else
                {
                    Log.i(G.tag, "poziadavka zlyhala, HTTP: " + response.code());
                }
            }
            catch(IOException e)
            {
                Log.i(G.tag, "error in Retrofit callback: " + e.getMessage());
                e.printStackTrace();
            }
        }
    };
}