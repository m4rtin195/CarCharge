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
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.network.CloudRestAPI;
import com.martin.carcharge.storage.AppDatabase;
import com.martin.carcharge.storage.Converters;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
        NetworkFlipperPlugin networkFlipperPlugin = null;
        
        if(BuildConfig.DEBUG)
        {
            final FlipperClient client = AndroidFlipperClient.getInstance(this);
            networkFlipperPlugin = new NetworkFlipperPlugin();
            
            client.addPlugin(CrashReporterPlugin.getInstance());
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
            client.addPlugin(new DatabasesFlipperPlugin(this));
            client.addPlugin(new SharedPreferencesFlipperPlugin(this, "com.martin.carcharge_preferences"));
            client.addPlugin(networkFlipperPlugin);
            client.start();
        }
        
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "CarCharge_db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        vm = new MainViewModel.Factory(this).create(MainViewModel.class); //( getApplicationContext()).get(MainViewModel.class);
    
        
        Interceptor interceptor = chain ->
        {
            Request request = chain.request().newBuilder()
                    .addHeader("User-Agent", "com.martin.carharge")
                    .addHeader("x-api-key", BuildConfig.BACKEND_APIKEY)
                    .build();
            return chain.proceed(request);
        };
        
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        //httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        if(BuildConfig.DEBUG)
        {
            builder.networkInterceptors().add(httpLoggingInterceptor);
            builder.addNetworkInterceptor(new FlipperOkhttpInterceptor(networkFlipperPlugin));
        }
        OkHttpClient okHttpClient = builder.build();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://uilqy1jfsf.execute-api.eu-central-1.amazonaws.com/v3/")
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
