package com.martin.carcharge;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class Downloader
{
    private final eCar_IoT_Kit_API cloud_api = AppActivity.getApi(); //todo nie v konstruktori?
    private final MainViewModel vm;
    
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    
    Downloader(ViewModelStoreOwner context)
    {
        vm = new ViewModelProvider(context).get(MainViewModel.class);
    }
    
    public void start()
    {
        executor.scheduleAtFixedRate(runnable, 0, 500, TimeUnit.MILLISECONDS);
    }
    public void stop()
    {
        executor.shutdown();
    }
    
    final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            Log.i(G.tag, "zaciatok thready");
            Call<VehicleStatus> call = cloud_api.getActual();
            try
            {
                Response<VehicleStatus> response = call.execute();
                if(response.isSuccessful())
                {
                    Log.i(G.tag, "poziadavka uspesna, HTTP: " + response.code());
                    VehicleStatus vs = response.body();
                    //vs.print();
                    vm.setVehicleStatus(new MutableLiveData<>(vs));
                } else
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