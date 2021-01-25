package com.martin.carcharge.models;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel
{
    private MutableLiveData<Vehicle> vehicle;
    private MutableLiveData<VehicleStatus> vehicleStatus;
    
    public MainViewModel()
    {
        Log.i("daco", "Creating new viewmodel, id: " + this.toString());
        vehicle = new MutableLiveData<>();
        vehicleStatus = new MutableLiveData<>();
    }
    
    public LiveData<Vehicle> getVehicle()
    {
        return vehicle;
    }
    
    public void setVehicle(MutableLiveData<Vehicle> v)
    {
        vehicle = v;
    }
    
    public LiveData<VehicleStatus> getVehicleStatus()
    {
        return vehicleStatus;
    }
    
    public void setVehicleStatus(MutableLiveData<VehicleStatus> vs)
    {
        Log.i("daco", "updating viewmodel, id: " + this.toString());
        vehicleStatus = vs;
    }
}