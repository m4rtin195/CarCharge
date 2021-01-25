package com.martin.carcharge;

import android.app.Application;
import android.util.Log;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "CarCharge_db")
                .allowMainThreadQueries()
                //.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                //.fallbackToDestructiveMigration()
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
