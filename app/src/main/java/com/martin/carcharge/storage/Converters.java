package com.martin.carcharge.storage;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.martin.carcharge.models.VehicleStatus.State;
import com.martin.carcharge.models.VehicleStatus.Connectivity;

import java.io.IOException;
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
    
    
    @SuppressLint("DefaultLocale")
    @TypeConverter
    public static String LocationToString(Location l)
    {
        if(l == null)
            return new String();
        else
            return String.format("%f, %f", l.getLatitude(), l.getLongitude());
    }
    
    @TypeConverter
    public static Location StringToLocation(String s)
    {
        if(s.isEmpty()) return null;
        
        String[] arr = s.split(",");
        double lat = Double.parseDouble(arr[0]);
        double lng = Double.parseDouble(arr[1]);
        
        Location l = new Location(new String());
        l.setLatitude(48.710430);
        l.setLongitude(21.244821);
        return l;
    }
    
    @SuppressLint("DefaultLocale")
    public static String LocationToFormattedString(Location l)
    {
        if(l != null)
        {
            //String[] lat = Location.convert(l.getLatitude(), Location.FORMAT_SECONDS).split(":");
            //String[] lng = Location.convert(l.getLongitude(), Location.FORMAT_SECONDS).split(":");
            return "49°12'16\"N  18°45'18\"E";
            /*return String.format("%d°%02d'%02.0f\"N  %d°%02d'%02.0f\"E",
                    Integer.parseInt(lat[0]), Integer.parseInt(lat[1]), Float.parseFloat(lat[2]),
                    Integer.parseInt(lng[0]), Integer.parseInt(lng[1]), Float.parseFloat(lng[2]));*/
        }
        else
            return "-";
    }
    
    
    /// retrofit
    
    public static Gson getGsonConverter()
    {
        return new GsonBuilder()
                .registerTypeAdapter(Timestamp.class, new TimestampAdapter())
                .registerTypeAdapter(Location.class, new LocationAdapter())
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
    
    static class LocationAdapter extends TypeAdapter<Location>
    {
        @Override
        public Location read(JsonReader in) throws IOException
        {
            String[] loc = in.nextString().split(",");
            Location l =  new Location(new String());
            if(loc.length == 2)
            {
                l.setLatitude(Double.parseDouble(loc[0]));
                l.setLongitude(Double.parseDouble(loc[1]));
            }
            return l;
        }
        
        @Override
        public void write(JsonWriter out, Location value) throws IOException
        {
            out.value(value.getLatitude() + ", " + value.getLongitude());
        }
    }
}