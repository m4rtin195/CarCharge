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
    @GET("android/actual")
    //@GET("https://run.mocky.io/v3/94e7b516-0c12-4d59-923c-608d4a97ed18")
    @Headers("User-Agent: com.martin.carharge")
    Call<VehicleStatus> getActualStatus(@Query("vehicleId") String vehicleId);
    
    //@POST("android/full")
    //Call<VehicleStatus> getFullStatus();
    
    @POST("android/range")
    @Headers("User-Agent: com.martin.carharge")
    //@Headers("x-api-key: 3N9CgQlJxX5RJOIUjhLpy1q9cxxaTWIo8n0IfYtA")
    @GET("android/range")
    Call<List<VehicleStatus>> getStatuses(@Query("vehicleId") String vehicleId,
                                          @Query("from") Timestamp timestampFrom,
                                          @Query("to") Timestamp timestampTo);
}