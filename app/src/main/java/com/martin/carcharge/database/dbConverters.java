package com.martin.carcharge.database;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.models.VehicleStatus.State;

import java.sql.Timestamp;

public class dbConverters
{
    /*//String to ArrayList
    @TypeConverter
    public static ArrayList<String> fromString(String value)
    {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    //ArrayList to String
    @TypeConverter
    public static String fromArrayList(ArrayList<String> list)
    {
        Gson gson = new Gson();
        return gson.toJson(list);
    }*/
    
    @TypeConverter
    public static String UriToString(Uri value)
    {
        if(value == null) return new String();
        else return value.toString();
    }
    @TypeConverter
    public static Uri StringToUri(String value)
    {
        return Uri.parse(value);
    }
    
    
    @TypeConverter
    public static long TimestampToLong(Timestamp value)
    {
        return value.getTime();
    }
    @TypeConverter
    public static Timestamp LongToTimestamp(long value)
    {
        return new Timestamp(value);
    }
    
    
    @TypeConverter
    public static int fromStatus(State value)
    {
        return value.ordinal();
    }
    @TypeConverter
    public static State toState(int value)
    {
        return State.values()[value];
    }
}