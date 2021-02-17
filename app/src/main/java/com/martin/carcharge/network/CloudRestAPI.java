package com.martin.carcharge.network;

import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CloudRestAPI
{
    //@POST("android/actual")
    @GET("https://run.mocky.io/v3/d8ed5987-8166-4939-9eab-46f89687be57")
    @Headers("User-Agent: com.martin.carharge")
    Call<VehicleStatus> getActualStatus(@Query("vehicleId") long vehicleId);
    
    //@POST("android/full")
    Call<VehicleStatus> getFullStatus();
    
    //@POST("android/range")
    @Headers("User-Agent: com.martin.carharge")
    @GET("/")
    Call<List<VehicleStatus>> getStatuses(@Query("vehicleId") long vehicleId,
                                          @Query("from") Timestamp timestampFrom,
                                          @Query("to")Timestamp timestampTo);
}