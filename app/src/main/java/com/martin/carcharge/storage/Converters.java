package com.martin.carcharge.storage;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.martin.carcharge.models.VehicleStatus.State;
import com.martin.carcharge.models.VehicleStatus.Connectivity;

import java.lang.reflect.Type;
import java.sql.Timestamp;

public class Converters
{
    @TypeConverter
    public static int StateToInt(State value)
    {
        return value.value;
    }
    @TypeConverter
    public static State IntToState(int value)
    {
        return State.getState(value);
    }
    
    @TypeConverter
    public static int ConnectivityToInt(Connectivity value)
    {
        return value.value;
    }
    @TypeConverter
    public static Connectivity IntToConnectivity(int value)
    {
        return Connectivity.getConnectivity(value);
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
    
    
    public static Gson getGsonForRetrofit()
    {
        return new GsonBuilder()
                .registerTypeAdapter(Timestamp.class, new TimestampAdapter())
                .create();
    }
    
    static class TimestampAdapter implements JsonDeserializer<Timestamp>
    {
        @Override
        public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return new Timestamp(json.getAsJsonPrimitive().getAsLong());
        }
    }
    
    /*static class TimestampAdapter extends TypeAdapter<Timestamp>
    {
        @Override
        public Timestamp read(JsonReader in) throws IOException
        {
            return new Timestamp(in.nextLong() * 1000);
        }
        
        @Override
        public void write(JsonWriter out, Timestamp value) throws IOException
        {
            out.value(value.getTime() / 1000);
        }
    }*/
}