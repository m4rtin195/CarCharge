package com.martin.carcharge.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.martin.carcharge.models.Vehicle;

@Database(entities = {Vehicle.class}, version = 1)
@TypeConverters({dbConverters.class})
public abstract class AppDatabase extends RoomDatabase
{
    public abstract dbDAO dao();
}
