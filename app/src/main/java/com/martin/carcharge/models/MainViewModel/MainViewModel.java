package com.martin.carcharge.models.MainViewModel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.martin.carcharge.AppActivity;
import com.martin.carcharge.G;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import org.jetbrains.annotations.NotNull;

public class MainViewModel extends AndroidViewModel
{
    protected final AppDatabase db;
    protected final SharedPreferences pref;
    
    public final Skuska skuska;
    
    private final MutableLiveData<User> user;
    private final MutableLiveData<Vehicle> vehicle;
    private final MutableLiveData<VehicleStatus> vehicleStatus;
    
    public MainViewModel(@NonNull Application context)
    {
        super(context);
        Log.i("daco","creating new viewmodel, id:" + this.toString());
        skuska = new Skuska(this);
        db = AppActivity.getDatabase();
        pref = AppActivity.getPreferences();
        
        user = new MutableLiveData<>();
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>();
    }
    
    public LiveData<User> user()
    {
        return user;
    }
    
    public LiveData<Vehicle> vehicle()
    {
        return vehicle;
    }
    
    public void postVehicle(Vehicle v)
    {
        db.dao().updateVehicle(v);
        pref.edit()
                .putLong("last_vehicle_id", v.getId())
                .putString("vehicle_name", v.getName())
                .putString("vehicle_regplate", v.getRegNumber())
                //.putInt("vehicle_capacity", v.getBatteryCapacity())
                .putString("vehicle_image", v.getImageFile())
                .apply();
        
        this.vehicle.postValue(v);
    }
    
    public Vehicle createVehicle(String name)
    {
        Vehicle v = new Vehicle();
        v.setName(name);
        long id = db.dao().insert(v);
        v.setId(id);
        return v;
    }
    
    public void loadLastVehicle()
    {
        long lastVehicleId = pref.getLong("last_vehicle_id", 0);
        if(lastVehicleId == 0) //app first launch
        {
            Log.i(G.tag,"Last used vehicle not set. Creating new vehicle.");
            Vehicle temp = new Vehicle();
            temp.setName("New vehicle");
            lastVehicleId = db.dao().insert(temp);
            pref.edit().putLong("last_vehicle_id", lastVehicleId).apply();
        }
        if(db.dao().getVehicle(lastVehicleId) == null) //not exist in database
        {
            Log.e(G.tag,"Last used vehicle not found in database. Clearing last flag.");
            pref.edit().remove("last_vehicle_id").apply();
            loadLastVehicle();
            return;
        }
        
        Vehicle v = db.dao().getVehicle(lastVehicleId); //valid load
        this.vehicle.postValue(v);
    }
    
    
    
    public LiveData<VehicleStatus> vehicleStatus()
    {
        return vehicleStatus;
    }
    
    public void postVehicleStatus(VehicleStatus vs)
    {
        this.vehicleStatus.postValue(vs);
    }
    
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory
    {
        private final Application context;
        
        public Factory(@NonNull Application application)
        {
            context = application;
        }
        
        @NotNull
        @Override
        public <T extends ViewModel> T create(@NotNull Class<T> modelClass)
        {
            //noinspection unchecked
            return (T) new MainViewModel(context);
        }
    }
}