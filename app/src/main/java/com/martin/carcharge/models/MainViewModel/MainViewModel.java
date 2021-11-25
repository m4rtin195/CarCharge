package com.martin.carcharge.models.MainViewModel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private final MutableLiveData<List<Vehicle>> vehiclesRepo;
    private final MutableLiveData<Vehicle> vehicle;
    private final MutableLiveData<VehicleStatus> vehicleStatus;
    
    
    public MainViewModel(@NonNull Application context)
    {
        super(context);
        skuska = new Skuska(this);
        
        db = App.getDatabase();
        pref = App.getPreferences();
    
        user = new MutableLiveData<>();
        vehiclesRepo = new MutableLiveData<>();
        vehiclesRepo.setValue(new ArrayList<>());
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>();
        
        _initRepositories();
    }
    
    private void _initRepositories()
    {
        _loadVehiclesRepo();
        _loadLastVehicle();
        _loadLastVehicleStatus();
    }
    
    
    
    /**********/
    //user

    @NonNull
    public LiveData<User> user()
    {
        return user;
    }
    
    @NonNull
    public User getUser()
    {
        assert user.getValue() != null;
        return user.getValue();
    }
    
    public void setUser(User u)
    {
        this.user.setValue(u);
    }
    
    
    
    /**********/
    //vehicles_repo

    @NonNull
    public LiveData<List<Vehicle>> vehiclesRepo()
    {
        return vehiclesRepo;
    }
    
    @NonNull
    public List<Vehicle> getAllVehicles()
    {
        assert vehiclesRepo.getValue() != null;
        return this.vehiclesRepo.getValue();
    }
    
    public void addToVehiclesRepo(@NonNull List<Vehicle> bundle)
    {
        assert vehiclesRepo.getValue() != null;
        this.vehiclesRepo.getValue().addAll(bundle);
    }
    
    @Nullable
    public Vehicle getVehicleByStatus(@NonNull VehicleStatus vs)
    {
        return _getVehicle(vs.getVehicleId());
    }
    
    public void updateVehicle(@NonNull Vehicle v)
    {
        db.dao().updateVehicle(v);
        _loadVehiclesRepo();
        this.vehicle.setValue(v);
    }
    
    @NonNull
    public Vehicle createVehicle(@NonNull String name)
    {
        Vehicle v = new Vehicle();
        v.setName(name);
        //v.setId(id); //todo
        db.dao().insertVehicle(v);
        _addToVehiclesRepo(v);
        return v;
    }
    
    
    
    /**********/
    //vehicle

    @NonNull
    public LiveData<Vehicle> vehicle()
    {
        return vehicle;
    }
    
    @NonNull
    public Vehicle getCurrentVehicle()
    {
        assert vehicle.getValue() != null;
        return vehicle.getValue();
    }
    
    public void changeVehicle(@NonNull Vehicle v)
    {
        this.vehicle.setValue(v);
        this.vehicleStatus.setValue(_reqireVehicleStatus(v));
    }
    
    
    
    /**********/
    //status
    
    @NonNull
    public LiveData<VehicleStatus> vehicleStatus()
    {
        return vehicleStatus;
    }
    
    @NonNull
    public VehicleStatus getCurrentVehicleStatus()
    {
        Log.i("daco",db.dao().getLastStatus("386625").getId());
        return db.dao().getLastStatus("386625");
//        assert vehicleStatus.getValue() != null;
//        return vehicleStatus.getValue();
    }
    
    //status-db
    
    public void updateVehicleStatus(@NonNull VehicleStatus vs)
    {
        this.vehicleStatus.setValue(vs);
        if(vs.getState().isValid())
            _addToStatusesRepo(vs);
    }
    
    @NonNull //todo ale isto????
    public List<VehicleStatus> getVehicleStatuses(@NonNull Vehicle v, @NonNull Timestamp from, @NonNull Timestamp to)
    {
        return db.dao().getStatuses(v.getId(), from, to);
    }
    
    public void addVehicleStatuses(@NonNull List<VehicleStatus> bundle)
    {
        db.dao().insertStatuses(bundle);
    }
    
    public void deleteAllVehicleStatuses()
    {
        db.dao().deleteAllStatuses();
    }
    
    
    
    /**********/
    //internal

    private void _addToVehiclesRepo(@NonNull Vehicle v)
    {
        assert vehiclesRepo.getValue() != null;
        this.vehiclesRepo.getValue().add(v);
    }
    
    private void _loadLastVehicle()
    {
        String lastVehicleId = pref.getString(G.PREF_LAST_VEHICLE_ID, "");
        if(lastVehicleId.isEmpty()) //last id not set  //app first launch
        {
            Log.i(G.tag,"Last used vehicle not set. Creating new vehicle.");
            Vehicle v = createVehicle("New vehicle");
            lastVehicleId = v.getId();
            pref.edit().putString(G.PREF_LAST_VEHICLE_ID, lastVehicleId).apply();
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
    
    private Vehicle _getVehicle(String id)
    {
        List<Vehicle> list = getAllVehicles();
        for(Vehicle v : list)
            if(v.getId().equals(id))
                return v;
        
        return null;
    }
    
    private void _loadVehiclesRepo()
    {
        assert vehiclesRepo.getValue() != null;
        vehiclesRepo.getValue().clear();
       
        List<Vehicle> list = db.dao().getAllVehicles();
        for(Vehicle v : list)
            v.loadVehicleImage(getApplication().getApplicationContext());
        
        vehiclesRepo.getValue().addAll(list);
    }
    
    private void _loadLastVehicleStatus()
    {
        _reqireVehicleStatus(getCurrentVehicle());
    }
    
    @NonNull
    private VehicleStatus _reqireVehicleStatus(Vehicle v)
    {
        VehicleStatus oldOne = db.dao().getLastStatus(v.getId());
        if(oldOne == null)
            return new VehicleStatus(v, VehicleStatus.State.Unknown);
    
        long actualityThr = pref.getLong(G.PREF_ACTUALITY_THRESHOLD, 300);
        Instant statusTime = Instant.ofEpochMilli(oldOne.getTimestamp().getTime());
    
        if(statusTime.isBefore(Instant.now().minus(actualityThr, ChronoUnit.MINUTES))) //is too old
        {
            Log.i(G.tag, "Dont have actual status, creating unknown one");
            return new VehicleStatus(v, VehicleStatus.State.Unknown);
        }
        else //is actual
            return oldOne;
    }
    
    private void _addToStatusesRepo(@NonNull VehicleStatus vs)
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