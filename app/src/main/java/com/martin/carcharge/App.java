package com.martin.carcharge;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.martin.carcharge.storage.AppDatabase;
import com.martin.carcharge.storage.Converters;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.network.CloudRestAPI;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends android.app.Application
{
    static AppDatabase db;
    static SharedPreferences pref;
    static MainViewModel vm;
    static CloudRestAPI api;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // todo tu?????
        
        SoLoader.init(this, false);
        if(BuildConfig.DEBUG)
        {
            final FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(CrashReporterPlugin.getInstance());
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
            client.addPlugin(new DatabasesFlipperPlugin(this));
            client.addPlugin(new SharedPreferencesFlipperPlugin(this, "com.martin.carcharge_preferences"));
            client.start();
        }
        
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "CarCharge_db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        vm = new MainViewModel.Factory(this).create(MainViewModel.class); //( getApplicationContext()).get(MainViewModel.class);
    
        
        Interceptor interceptor = new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                //Log.i("daco", "interceptor run");
                //String userSecret = vm.user().getValue().getUserSecret();
                
                //Request request = chain.request();
                Request request = chain.request().newBuilder().addHeader("x-api-key", "3N9CgQlJxX5RJOIUjhLpy1q9cxxaTWIo8n0IfYtA").build(); //todo extern
                //request.newBuilder().addHeader("User-Secret", userSecret).build();
                return chain.proceed(request);
            }
        };
        
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        builder.networkInterceptors().add(httpLoggingInterceptor);
        OkHttpClient okHttpClient = builder.build();
        
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("https://fe6ea208.eu-gb.apigw.appdomain.cloud/ecar-iot-kit-api/v1/")
                .baseUrl("https://uilqy1jfsf.execute-api.eu-central-1.amazonaws.com/v2/")
                .addConverterFactory(GsonConverterFactory.create(Converters.getGsonConverter()))
                .client(okHttpClient)
                .build();
        
        api = retrofit.create(CloudRestAPI.class);
    }
    
    public static AppDatabase getDatabase() {return db;}
    public static SharedPreferences getPreferences() {return pref;}
    public static MainViewModel getViewModel() {return vm;}
    public static CloudRestAPI getApi() {return api;}
}
