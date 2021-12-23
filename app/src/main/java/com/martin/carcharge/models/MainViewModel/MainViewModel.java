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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainViewModel extends AndroidViewModel
{
    protected final AppDatabase db;
    protected final SharedPreferences pref;
    
    public final Skuska skuska;
    
    private final MutableLiveData<User> user;
    private final List<MutableLiveData<Vehicle>> vehiclesRepo;
    private final MutableLiveData<Vehicle> currentVehicle; //just a copy of reference to vehicle in vehiclesRepo
    private MutableLiveData<VehicleStatus> currentVehicleStatus; //just a reference
    
    
    public MainViewModel(@NonNull Application context)
    {
        super(context);
        skuska = new Skuska(this);
        
        db = App.getDatabase();
        pref = App.getPreferences();
    
        user = new MutableLiveData<>();
        //vehiclesRepo = new MutableLiveData<>();
        //vehiclesRepo.setValue(new ArrayList<>());
        vehiclesRepo = new ArrayList<>();
        currentVehicle = new MutableLiveData<>();
        currentVehicleStatus = null; //it will be only reference
    }
    
    public void init()
    {
        Log.i(G.tag, "repository init");
        _loadVehiclesRepo();
        _loadLastVehicle();
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
    public List<MutableLiveData<Vehicle>> vehiclesRepo()
    {
        return this.vehiclesRepo;
    }
    
    @NonNull
    public List<Vehicle> getAllVehicles()
    {
        List<Vehicle> list = new ArrayList<>();
        for(MutableLiveData<Vehicle> m : vehiclesRepo)
            list.add(Objects.requireNonNull(m.getValue()));
        return list;
    }
    
    @NonNull
    public Vehicle createVehicle(@NonNull String name)
    {
        Vehicle v = new Vehicle();
        v.setName(name);
        v.loadVehicleImage(getApplication().getApplicationContext());
        db.dao().insertVehicle(v);
        this.vehiclesRepo.add(new MutableLiveData<>(v));
        return v;
    }
    
    //update if exists, create if not
    //need to be called to invoke observation of vehicle change and to update it in local db
    public void insertOrUpdateVehicle(@NonNull Vehicle v)
    {
        if(_findVehicleIndex(v) >= 0) //found in local db, have index
        {
            db.dao().updateVehicle(v);
            vehiclesRepo.get(_findVehicleIndex(v)).setValue(v); //invoke observation
            //actually to je potrebne iba pre invoke observe, inak su data vehicle v repo uz zmenene (pass by value-reference)
            //kotlin riesenie: https://stackoverflow.com/a/55537829/14629312
        }
        else
        {
            db.dao().insertVehicle(v);
            vehiclesRepo.add(new MutableLiveData<>(v));
        }
        
        if(v == this.currentVehicle.getValue())
            this.currentVehicle.setValue(v); //to invoke observation
    }
    
    @Nullable
    public Vehicle getVehicleFromRepo(String id)
    {
        for(MutableLiveData<Vehicle> m : vehiclesRepo)
        {
            if(Objects.requireNonNull(m.getValue()).getId().equals(id))
                return m.getValue();
        }
        return null;
    }
    
    private int _findVehicleIndex(Vehicle v)
    {
        int i = 0;
        for(MutableLiveData<Vehicle> m : vehiclesRepo)
        {
            if(Objects.requireNonNull(m.getValue()).equals(v))
                break;
            i++;
        }
        return (i<=(vehiclesRepo.size()-1)) ? i : -1;
    }
    
    
    
    /**********/
    //current vehicle

    @NonNull
    public LiveData<Vehicle> currentVehicle()
    {
        return currentVehicle;
    }
    
    @NonNull
    public Vehicle getCurrentVehicle()
    {
        assert currentVehicle.getValue() != null;
        return currentVehicle.getValue();
    }
    
    public void switchCurrentVehicle(@NonNull Vehicle v)
    {
        this.currentVehicleStatus = v.vehicleStatus(); //zalezi na poradi!!
        this.currentVehicle.setValue(v); //observer v homefragment odregistruje observer na stary vehiclestatus
        this.currentVehicleStatus.postValue(currentVehicleStatus.getValue()); //to invoke observation
        pref.edit().putString(G.PREF_LAST_VEHICLE_ID, v.getId()).apply();
    }
    
    
    
    /**********/
    //status
    
    @NonNull
    public LiveData<VehicleStatus> currentVehicleStatus()
    {
        //assert currentVehicle.getValue() != null;
        //return currentVehicle.getValue().vehicleStatus();
        return currentVehicleStatus;
    }
    
    @Nullable
    public VehicleStatus getCurrentVehicleStatus()
    {
        return currentVehicleStatus.getValue();
    }
    
    //status-db
    
    public void updateVehicleStatus(@NonNull VehicleStatus vs)
    {
        Log.i("daco", "status vid: " + vs.getVehicleId());
        Vehicle v = getVehicleFromRepo(vs.getVehicleId());
        if(v == null)
        {
            Log.e(G.tag, "Received status for vehicle not in database!");
            return;
        }
        v.vehicleStatus().setValue(vs);
        
        //write to db
        if(vs.getState().isNormal())
            _addToStatusesDb(vs);
        
        //put notification if charging is complete
        if(vs.getCurrent_charge() == vs.getTarget_charge()) //todo overit ci pride taka kombinacia
            ((App)getApplication()).postChargeCompleteNotification(v,vs);
    }
    
    @NonNull
    public List<VehicleStatus> getVehicleStatuses(@NonNull Vehicle v, @NonNull Date from, @NonNull Date to)
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
    
    private void _loadVehiclesRepo()
    {
        List<Vehicle> list = db.dao().getAllVehicles();
        for(Vehicle v : list)
        {
            Log.i("daco", "- loading vehicle: " + v.getName() + " (" + v.hashCode() + ")");
            v.loadVehicleImage(getApplication().getApplicationContext());
            v.vehicleStatus().setValue(_reqireVehicleStatus(v));
            vehiclesRepo.add(new MutableLiveData<>(v));
        }
    }
    
    private boolean _loadLastVehicle()
    {
        String lastVehicleId = pref.getString(G.PREF_LAST_VEHICLE_ID, "");
        if(lastVehicleId.isEmpty()) //last id not set  //app first launch
        {
            //cannot create new Vehicle here cause Firestore fetch didnt happen yet
            Log.w(G.tag,"Last used vehicle not set.");
            return false;
        }
        if(getVehicleFromRepo(lastVehicleId) == null) //not found
        {
            Log.e(G.tag,"Last used vehicle not found in database. Clearing last flag.");
            pref.edit().remove(G.PREF_LAST_VEHICLE_ID).apply();
            return _loadLastVehicle(); //to create new one;
        }
        
        Vehicle v = getVehicleFromRepo(lastVehicleId); //valid load
        assert v != null : "Toto by sa nikdy nemalo stat";
        switchCurrentVehicle(v);
        return true;
    }
    
    @NonNull
    private VehicleStatus _reqireVehicleStatus(Vehicle v)
    {
        VehicleStatus oldOne = db.dao().getLastStatus(v.getId());
        if(oldOne == null)
            return new VehicleStatus(v, VehicleStatus.State.Unknown);
    
        int actualityThr = pref.getInt(G.PREF_ACTUALITY_THRESHOLD, 300);
        Instant statusTime = Instant.ofEpochMilli(oldOne.getTimestamp().getTime());
    
        if(statusTime.isBefore(Instant.now().minus(actualityThr, ChronoUnit.MINUTES))) //is too old
        {
            Log.i(G.tag, "Dont have actual status for [" + v.getName() + "], creating unknown one");
            return new VehicleStatus(v, VehicleStatus.State.Unknown);
        }
        else //is actual
            return oldOne;
    }
    
    private void _addToStatusesDb(@NonNull VehicleStatus vs)
    {
        db.dao().insertStatus(vs);
        _manageStatusesDbSize();
    }
    
    private boolean _manageStatusesDbSize()
    {
        final int cacheLimit = 50;
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
    //for crating app-wide viewmodel
    
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