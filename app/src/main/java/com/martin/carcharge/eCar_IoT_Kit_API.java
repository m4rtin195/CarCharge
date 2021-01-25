package com.martin.carcharge;

import com.martin.carcharge.models.VehicleStatus;

import retrofit2.Call;
import retrofit2.http.POST;

public interface eCar_IoT_Kit_API
{
    //@POST("android/actual")
    @POST("97579ae3-97c7-48ab-a836-4d073bab4865")
    Call<VehicleStatus> getActual();
    //Call<ResponseBody> getActual();
    
}
