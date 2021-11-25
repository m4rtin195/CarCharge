package com.martin.carcharge.storage;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

@Database(entities = {Vehicle.class, VehicleStatus.class}, version = 12)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase
{
    public abstract dbDAO dao();
}
