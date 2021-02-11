package com.martin.carcharge.network;

import com.martin.carcharge.models.VehicleStatus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;

public interface CloudRestAPI
{
    //@POST("android/actual")
    @POST("d8ed5987-8166-4939-9eab-46f89687be57")
    Call<VehicleStatus> getActualStatus();
    
    //@POST("android/full")
    Call<VehicleStatus> getFullStatus();
    
    //@POST("android/range")
    @POST("/")
    Call<List<VehicleStatus>> getStatuses();
}