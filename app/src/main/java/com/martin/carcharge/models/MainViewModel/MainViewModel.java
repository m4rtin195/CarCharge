package com.martin.carcharge.models.MainViewModel;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.storage.AppDatabase;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel
{
    protected final AppDatabase db;
    protected final SharedPreferences pref;
    
    public final Skuska skuska;
    
    private final MutableLiveData<User> user;
    private final List<Vehicle> vehiclesRepo;
    private final MutableLiveData<Vehicle> vehicle;
    private final MutableLiveData<VehicleStatus> vehicleStatus;
    
    
    public MainViewModel(@NonNull android.app.Application context)
    {
        super(context);
        skuska = new Skuska(this);
        
        db = App.getDatabase();
        pref = App.getPreferences();
    
        user = new MutableLiveData<>();
        vehiclesRepo = new ArrayList<>();
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>();
        
        _initRepositories();
    }
    
    private void _initRepositories()
    {
        _loadLastVehicle();
        _loadVehiclesRepo();
    }
    
    
    
    /**********/
    //user
    
    public LiveData<User> user()
    {
        return user;
    }
    
    public void setUser(User u)
    {
        pref.edit() //todo treba to cele??????
                .putString("user_nickname", u.getNickname())
                .putString("user_email", u.getEmail())
                //.putString("user_icon", u.getImageFile())
            .apply();
        
        this.user.setValue(u);
    }
    
    
    
    /**********/
    //vehicle

    public LiveData<Vehicle> vehicle()
    {
        return vehicle;
    }
    
    public long getActualVehicleId()
    {
        return vehicle.getValue().getId();
    }
    
    public List<Vehicle> getAllVehicles()
    {
        return this.vehiclesRepo;
    }
    
    public void fillVehiclesRepo(ArrayList<Vehicle> bundle)
    {
        this.vehiclesRepo.addAll(bundle);
    }
    
    public void updateActualVehicle(Vehicle v)
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
        long id = db.dao().insertVehicle(v);
        v.setId(id);
        
        _addToVehiclesRepo(v);
        return v;
    }
    
    
    
    /**********/
    //statuses
    
    public LiveData<VehicleStatus> vehicleStatus()
    {
        return vehicleStatus;
    }
    
    public void updateVehicleStatus(VehicleStatus vs)
    {
        this.vehicleStatus.setValue(vs);
        if(vs.getState().isValid())
            _addToStatusesRepo(vs);
    }
    
    public VehicleStatus getLastVehicleStatus(long vehicleId)
    {
        return db.dao().getLastStatus(vehicleId);
    }
    
    public List<VehicleStatus> getVehicleStatuses(long vehicleId, Timestamp from, Timestamp to)
    {
        return db.dao().getStatuses(vehicleId, from, to);
    }
    
    public void addVehicleStatuses(ArrayList<VehicleStatus> bundle)
    {
        db.dao().insertStatuses(bundle);
    }
    
    
    
    /**********/
    //internal

    private void _addToVehiclesRepo(Vehicle v)
    {
        this.vehiclesRepo.add(v);
    }
    
    private void _loadLastVehicle()
    {
        long lastVehicleId = pref.getLong(G.PREF_LAST_VEHICLE_ID, 0);
        if(lastVehicleId == 0) //last id not set  //app first launch
        {
            Log.i(G.tag,"Last used vehicle not set. Creating new vehicle.");
            Vehicle v = new Vehicle();
            v.setName("New vehicle");
            lastVehicleId = db.dao().insertVehicle(v);
            pref.edit().putLong(G.PREF_LAST_VEHICLE_ID, lastVehicleId).apply();
        }
        if(db.dao().getVehicle(lastVehicleId) == null) //not exist in database
        {
            Log.e(G.tag,"Last used vehicle not found in database. Clearing last flag.");
            pref.edit().remove(G.PREF_LAST_VEHICLE_ID).apply();
            _loadLastVehicle();
            return;
        }
        
        Vehicle v = db.dao().getVehicle(lastVehicleId); //valid load
        this.vehicle.setValue(v);
    }
    
    private void _loadVehiclesRepo()
    {
        int i = vehiclesRepo.size();
        assert i==0 : "populating not-empty vehiclesRepo!!";
        
        vehiclesRepo.addAll(db.dao().getAllVehicles());
    }
    
    
    private void _addToStatusesRepo(VehicleStatus vs)
    {
        db.dao().insertStatus(vs);
        _manageStatusesRepoSize();
    }
    
    private boolean _manageStatusesRepoSize()
    {
        final int cacheLimit = 10;
        int count = db.dao().countStatuses();
        
        if(count > cacheLimit)
        {
            Log.w(G.tag, "Exceeded cache size!");
            db.dao().deleteStatusesCount(count-cacheLimit);
            return true;
        }
        else
            return false;
    }
    
    
    
    /**********/
    //factory
    
    public static class Factory extends ViewModelProvider.NewInstanceFactory
    {
        private final android.app.Application context;
        
        public Factory(@NonNull android.app.Application application)
        {
            context = application;
        }
        
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass)
        {
            MainViewModel mvm = new MainViewModel(context);
            T t = modelClass.cast(mvm);
            assert t != null;
            return t;
        }
    }
}