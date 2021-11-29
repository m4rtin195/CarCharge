package com.martin.carcharge.models;

import androidx.annotation.NonNull;

public class FcmRegistration
{
    private String vehicleId;
    private String fcmToken;
    private boolean register;
    
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
