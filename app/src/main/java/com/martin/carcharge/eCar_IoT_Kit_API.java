package com.martin.carcharge;

import retrofit2.Call;
import retrofit2.http.POST;

public interface eCar_IoT_Kit_API
{
    @POST("android/actual")
    Call<CarStatus> postActual();
}
