package com.martin.carcharge.database;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
    public static Uri StringToUri(String value)
    {
        return Uri.parse(value);
    }
    
    @TypeConverter
    public static String UriToString(Uri value)
    {
        if(value == null) return new String();
        else return value.toString();
    }
}