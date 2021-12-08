package com.martin.carcharge.network;

import com.martin.carcharge.models.FcmRegistration;
import com.martin.carcharge.models.VehicleStatus;

import java.sql.Timestamp;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CloudRestAPI
{
    @GET("android/last")
    //@GET("https://run.mocky.io/v3/94e7b516-0c12-4d59-923c-608d4a97ed18")
    Call<VehicleStatus> getLastStatus(@Header("userId") String userId,
                                      @Query("vehicleId") String vehicleId);
    
    @GET("android/range")
    Call<List<VehicleStatus>> getStatuses(@Header("userId") String userId,
                                          @Query("vehicleId") String vehicleId,
                                          @Query("timeFrom") String timestampFrom,
                                          @Query("timeTo") String timestampTo);
    
    @POST("android/fcm-register")
    Call<Void> fcmRegister(@Header("userId") String userId,
                           @Query("vehicleId") String vehicleId,
                           @Body FcmRegistration body);
    
    @POST("android/fcm-register") //same endpoint
    Call<Void> fcmUnregister(@Header("userId") String userId,
                             @Query("vehicleId") String vehicleId,
                             @Body FcmRegistration body);
    
}