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

import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.storage.AppDatabase;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    
    
    public MainViewModel(@NonNull Application context)
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
        Log.i("daco", "initRepositories");
        _loadVehiclesRepo();
        _loadLastVehicle();
    }
    
    
    
    /**********/
    //user
    
    public LiveData<User> user()
    {
        return user;
    }
    
    public User getUser()
    {
        return user.getValue();
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
        if(vehicle.getValue() != null)
            return vehicle.getValue().getId();
        else
            return 0;
    }
    
    public Vehicle getActualVehicle()
    {
        return vehicle.getValue();
    }
    
    public Vehicle getVehicleByStatus(VehicleStatus vs)
    {
        return _getVehicle(vs.getVehicleId());
    }
    
    public List<Vehicle> getAllVehicles()
    {
        return this.vehiclesRepo;
    }
    
    public void addToVehiclesRepo(List<Vehicle> bundle)
    {
        this.vehiclesRepo.addAll(bundle);
    }
    
    public void updateVehicle(Vehicle v)
    {
        db.dao().updateVehicle(v);
        /*pref.edit()
                .putLong(G.PREF_LAST_VEHICLE_ID, v.getId())
                .putString(G.PREF_VEHICLE_NAME, v.getName())
                .putString(G.PREF_VEHICLE_REGPLATE, v.getRegNumber())
                //.putInt(G.PREF_VEHICLE_CAPACITY, v.getBatteryCapacity())
                .putString(G.PREF_VEHICLE_IMAGE, v.getImageFile())
            .apply();*/
        
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
    
    public VehicleStatus getActualVehicleStatus(long vehicleId)
    {
        VehicleStatus vs = db.dao().getLastStatus(vehicleId);
        
        
        if(vs == null) return new VehicleStatus(VehicleStatus.State.Unknown);
        
        //long diff = new Date().getTime() - vs.getTimestamp().getTime();
        //if(diff < pref.getLong(G.PREF_ACTUAL_THRESHOLD, 300)*1000)
        long thresholdVal = pref.getLong(G.PREF_ACTUAL_THRESHOLD, 300);
        Instant timeThreshold = Instant.now().minus(thresholdVal, ChronoUnit.MINUTES);
        if(Instant.ofEpochMilli(vs.getTimestamp().getTime()).isBefore(timeThreshold))
        {
            return vs;
        }
        else
        {
            Log.i(G.tag, "Dont have actual status, returning unknown one");
            return new VehicleStatus(VehicleStatus.State.Unknown);
        }
    }
    
    public List<VehicleStatus> getVehicleStatuses(long vehicleId, Timestamp from, Timestamp to)
    {
        return db.dao().getStatuses(vehicleId, from, to);
    }
    
    public void addVehicleStatuses(List<VehicleStatus> bundle)
    {
        db.dao().insertStatuses(bundle);
    }
    
    
    public void deleteAllVehicleStatuses()
    {
        db.dao().deleteAllStatuses();
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
            Vehicle v = createVehicle("New vehicle");
            lastVehicleId = v.getId();
            pref.edit().putLong(G.PREF_LAST_VEHICLE_ID, lastVehicleId).apply();
        }
        if(_getVehicle(lastVehicleId) == null) //not found
        {
            Log.e(G.tag,"Last used vehicle not found in database. Clearing last flag.");
            pref.edit().remove(G.PREF_LAST_VEHICLE_ID).apply();
            _loadLastVehicle();
            return;
        }
        
        Vehicle v = _getVehicle(lastVehicleId); //valid load
        this.vehicle.setValue(v);
    }
    
    private Vehicle _getVehicle(long id)
    {
        List<Vehicle> list = getAllVehicles();
        for(Vehicle v : list)
            if(v.getId() == id)
                return v;
        
        return null;
    }
    
    private void _loadVehiclesRepo()
    {
        int i = vehiclesRepo.size();
        assert i==0 : "populating not-empty vehiclesRepo!!";
        
        List<Vehicle> list = db.dao().getAllVehicles();
        for(Vehicle v : list)
            v.loadVehicleImage(getApplication().getApplicationContext());
        
        vehiclesRepo.addAll(list);
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
    //viewmodel factory
    
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