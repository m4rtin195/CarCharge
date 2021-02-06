package com.martin.carcharge.models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.martin.carcharge.G;

public class MainViewModel extends ViewModel
{
    private final MutableLiveData<User> user;
    private final MutableLiveData<Vehicle> vehicle;
    private final MutableLiveData<VehicleStatus> vehicleStatus;
    
    public MainViewModel()
    {
        user = new MutableLiveData<>();
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>();
        
        //vehicle.observeForever(vehicle -> db.dao().updateVehicle(vehicle)); //todo moze byt?
    }
    
    public MutableLiveData<User> user()
    {
        return user;
    }
    
    public MutableLiveData<Vehicle> Vehicle() //todo lowercase
    {
        return vehicle;
    }
    
    public MutableLiveData<VehicleStatus> VehicleStatus()
    {
        return vehicleStatus;
    }
}