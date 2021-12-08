package com.martin.carcharge;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.network.ApiClient;
import com.martin.carcharge.network.CloudRestAPI;
import com.martin.carcharge.storage.AppDatabase;
import com.martin.carcharge.storage.CloudStorage;
import com.martin.carcharge.storage.Converters;
import com.martin.carcharge.storage.FileStorage;
import com.martin.carcharge.storage.FirestoreDb;

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
    static ApiClient api;
    @SuppressLint("StaticFieldLeak") //its application context
    static FirestoreDb fdb;
    @SuppressLint("StaticFieldLeak") //its application context
    static CloudStorage cstrg;
    
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
        
        createNotificationChannel();
        
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
        
        api = new ApiClient(retrofit.create(CloudRestAPI.class));
        cstrg = new CloudStorage(getApplicationContext()); //poradie!! fdb v konstruktore taha ref na cstrg
        fdb = new FirestoreDb(getApplicationContext());
        
        assert FileStorage.checkMediaFolder(getApplicationContext()) : "Cannot access /media folder";
    }
    
    //Instances getters
    public static AppDatabase getDatabase() {return db;}
    public static SharedPreferences getPreferences() {return pref;}
    public static MainViewModel getViewModel() {return vm;}
    public static ApiClient getApiClient() {return api;}
    public static FirestoreDb getFirestoreDb() {return fdb;}
    public static CloudStorage getCloudStorage() {return cstrg;}
    
    
    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel()
    {
        //must be checkeck for older apis, if minSdkVersion<26
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String chname = "All CarCharge notifications";
            NotificationChannel channel = new NotificationChannel(G.NOTIFICATION_CHANNELID, chname, NotificationManager.IMPORTANCE_DEFAULT);
            // not possible to change the importance or other notification behaviors after this
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
    
    public void postChargeCompleteNotification(Vehicle v, VehicleStatus vs)
    {
        Intent openAppIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), G.RC_FROM_NOTIF, openAppIntent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder notif = new NotificationCompat.Builder(getApplicationContext(), G.NOTIFICATION_CHANNELID)
            .setSmallIcon(R.drawable.bm_appicon)
            .setContentTitle(getApplicationContext().getString(R.string.notification_charge_completed))
            .setContentText(getApplicationContext().getString(R.string.notification_charge_completed_cont, v.getName(), vs.getTarget_charge()))
            .setContentIntent(pi)
            .addAction(R.drawable.ic_flash, getApplicationContext().getString(R.string.notification_charge_to_100),
                    PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0))
            .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, notif.build());
    }
}
