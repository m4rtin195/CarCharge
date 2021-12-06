package com.martin.carcharge.network;

import com.google.android.gms.tasks.Task;
import com.martin.carcharge.App;

public class ApiClient
{
    private CloudRestAPI api;
    
    public ApiClient()
    {
        api = App.getApi();
    }
    
    
    public Task<?> registerFcm()
    {
        //api.fcmRegister();
        return null;
    }
}
