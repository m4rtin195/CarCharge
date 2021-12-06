package com.martin.carcharge.models;

import androidx.annotation.NonNull;

public class FcmRegistration
{
    private final String vehicleId;
    private final String fcmToken;
    private final boolean register;
    
    public enum Method
    {
        REGISTER, UNREGISTER
    }
    
    public FcmRegistration(String vehicleId, String fcmToken, Method operation)
    {
        this.vehicleId = vehicleId;
        this.fcmToken = fcmToken;
        this.register = (operation == Method.REGISTER);
    }
    
    @NonNull
    public String toString()
    {
        return "FcmRegistration{" +
                "vehicleId=" + vehicleId +
                ", fcmToken=" + fcmToken +
                ", operation=" + register +
                "}";
    }
    
}
