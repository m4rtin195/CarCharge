package com.martin.carcharge.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

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
import com.martin.carcharge.G;
import com.martin.carcharge.models.VehicleStatus.State;
import com.martin.carcharge.models.VehicleStatus.Connectivity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Converters
{
    /** database **/
    
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
    public static long DateToLong(Date value)
    {
        return value.getTime();
    }
    
    @TypeConverter
    public static Date LongToDate(long value)
    {
        return new Date(value);
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
    
    
    /** retrofit **/
    
    public static Gson getGsonConverter()
    {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateAdapter())
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .create();
    }
    
    static class DateAdapter implements JsonDeserializer<Date>
    {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return new Date(json.getAsJsonPrimitive().getAsLong());
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
    
    
    /** others **/
    
    public static String LocationToAddressOrFormattedString(Location l)
    {
        return LocationToAddressOrFormattedString(l, null);
    }
    
    //must provide context for geocoding
    @SuppressLint("DefaultLocale")
    public static String LocationToAddressOrFormattedString(Location l, Context context)
    {
        if(l != null)
        {
            if(context != null)
            {
                String address = null;
                try{
                    address = Geocoder(l, context);
                } catch(IOException e) {
                    Log.w(G.tag, e.toString());}
                if(address != null) return address;
            }
            
            String[] lat = Location.convert(l.getLatitude(), Location.FORMAT_SECONDS).split(":");
            String[] lng = Location.convert(l.getLongitude(), Location.FORMAT_SECONDS).split(":");
            //return "49°12'16\"N  18°45'18\"E";
            return String.format("%d°%02d'%02.0f\"N  %d°%02d'%02.0f\"E",
                    Integer.parseInt(lat[0]), Integer.parseInt(lat[1]), Float.parseFloat(lat[2]),
                    Integer.parseInt(lng[0]), Integer.parseInt(lng[1]), Float.parseFloat(lng[2]));
        }
        else // location is null
            return "-";
    }
    
    private static String Geocoder(Location l, Context context) throws IOException
    {
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault()); //todo locale
        addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
        if(!addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        else return null;
    }
}