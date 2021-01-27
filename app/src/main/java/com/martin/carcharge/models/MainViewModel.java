package com.martin.carcharge.models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel
{
    //todo mutable vs livedata????
    private MutableLiveData<Vehicle> vehicle;
    private MutableLiveData<VehicleStatus> vehicleStatus;
    
    public MainViewModel()
    {
        Log.i("daco", "Creating new viewmodel, id: " + this.toString());
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>(); //move to dec
        
        //vehicle.observeForever(vehicle -> db.dao().updateVehicle(vehicle)); //todo moze byt?
    }
    
    public MutableLiveData<Vehicle> Vehicle()
    {
        return vehicle;
    }
    public MutableLiveData<VehicleStatus> VehicleStatus()
    {
        return vehicleStatus;
    }
    
    @Deprecated
    public void setVehicleStatus(MutableLiveData<VehicleStatus> vs)
    {
        Log.i("daco", "updating viewmodel, id: " + this.toString());
        vehicleStatus = vs;
    }
}