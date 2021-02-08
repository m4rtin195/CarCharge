package com.martin.carcharge.models.MainViewModel;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.martin.carcharge.App;
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
    
    public MainViewModel(@NonNull android.app.Application context)
    {
        super(context);
        skuska = new Skuska(this);
        
        db = App.getDatabase();
        pref = App.getPreferences();
    
        user = new MutableLiveData<>();
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>();
    }
    
    
    
    public LiveData<User> user()
    {
        return user;
    }
    
    public void setUser(User u)
    {
        pref.edit()
                .putString("user_nickname", u.getNickname())
                .putString("user_email", u.getEmail())
                .putString("user_icon", u.getImageFile())
            .apply();
        
        this.user.setValue(u);
    }
    
    
    
    
    public LiveData<Vehicle> vehicle()
    {
        return vehicle;
    }
    
    public void setVehicle(Vehicle v)
    {
        db.dao().updateVehicle(v);
        pref.edit()
                .putLong(G.PREF_LAST_VEHICLE_ID, v.getId())
                .putString(G.PREF_VEHICLE_NAME, v.getName())
                .putString(G.PREF_VEHICLE_REGPLATE, v.getRegNumber())
                //.putInt(G.PREF_VEHICLE_CAPACITY, v.getBatteryCapacity())
                .putString(G.PREF_VEHICLE_IMAGE, v.getImageFile())
            .apply();
        
        this.vehicle.setValue(v);
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
        long lastVehicleId = pref.getLong(G.PREF_LAST_VEHICLE_ID, 0);
        if(lastVehicleId == 0) //app first launch
        {
            Log.i(G.tag,"Last used vehicle not set. Creating new vehicle.");
            Vehicle temp = new Vehicle();
            temp.setName("New vehicle");
            lastVehicleId = db.dao().insert(temp);
            pref.edit().putLong(G.PREF_LAST_VEHICLE_ID, lastVehicleId).apply();
        }
        if(db.dao().getVehicle(lastVehicleId) == null) //not exist in database
        {
            Log.e(G.tag,"Last used vehicle not found in database. Clearing last flag.");
            pref.edit().remove(G.PREF_LAST_VEHICLE_ID).apply();
            loadLastVehicle();
            return;
        }
        
        Vehicle v = db.dao().getVehicle(lastVehicleId); //valid load
        this.vehicle.setValue(v);
    }
    
    
    
    
    
    public LiveData<VehicleStatus> vehicleStatus()
    {
        return vehicleStatus;
    }
    
    public void setVehicleStatus(VehicleStatus vs)
    {
        this.vehicleStatus.setValue(vs);
    }
    
    
    
    
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory
    {
        private final android.app.Application context;
        
        public Factory(@NonNull android.app.Application application)
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