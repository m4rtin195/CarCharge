package com.martin.carcharge;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.google.firebase.messaging.FirebaseMessaging;
import com.martin.carcharge.database.AppDatabase;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppActivity extends Application
{
    static AppDatabase db;
    static eCar_IoT_Kit_API api;
    
    /*static final Migration MIGRATION_1_2 = new Migration(1, 2)
    {
        @Override
        public void migrate(SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE Vehicle ADD COLUMN aaa TEXT");
        }
    };*/
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
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
                //.addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build();
    
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("https://fe6ea208.eu-gb.apigw.appdomain.cloud/ecar-iot-kit-api/v1/")
                .baseUrl("https://run.mocky.io/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    
        api = retrofit.create(eCar_IoT_Kit_API.class);
    }
    
    public static AppDatabase getDatabase() {return db;}
    public static eCar_IoT_Kit_API getApi() {return api;}
}
