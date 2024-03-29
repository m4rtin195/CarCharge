package com.martin.carcharge.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.martin.carcharge.G;
import com.martin.carcharge.R;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "vehicles")
public class Vehicle
{
    @NonNull
    @PrimaryKey
    private String id;
    private String name;
    private String regNumber;
    private int maxVoltage;
    private String imageFilename;
    
    @Ignore
    private Bitmap image;
    
    @Ignore
    private final MutableLiveData<VehicleStatus> vehicleStatus;
  
    
    public Vehicle()
    {
        id = UUID.randomUUID().toString().replace("-","").substring(0, 10);
        name = "";
        regNumber = "";
        maxVoltage = 0;
        imageFilename = "";
        
        vehicleStatus = new MutableLiveData<>();
    }
    
    @SuppressWarnings("ConstantConditions")
    public Vehicle(HashMap<String,Object> map)
    {
        this();
        boolean corrupted = false;
        if(map.get("id") != null) id = (String)map.get("id"); else corrupted = true;
        if(map.get("name") != null) name = (String)map.get("name"); else corrupted = true;
        if(map.get("regNumber") != null) regNumber = (String)map.get("regNumber"); else corrupted = true;
        if(map.get("maxVoltage") != null) maxVoltage = ((Number)map.get("maxVoltage")).intValue(); else corrupted = true;
        if(map.get("imageFilename") != null) imageFilename = (String)map.get("imageFilename"); else corrupted = true;
        
        if(corrupted)
            Log.w(G.tag, this.name + ": Vehicle_from_HashMap constructor - incomplete data! \n" + map.toString());
    }
    
    @NonNull
    public String getId() {return id;}
    public void setId(@NonNull String id) {this.id = id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getRegNumber() {return regNumber;}
    public void setRegNumber(String regPlate) {this.regNumber = regPlate;}
    
    public int getMaxVoltage() {return maxVoltage;}
    public void setMaxVoltage(int maxVoltage) {this.maxVoltage = maxVoltage;}
    
    public String getImageFilename() {return imageFilename;}
    public void setImageFilename(String imageFilename) {this.imageFilename = imageFilename;}
    
    public Bitmap getImage() {return image;}
    public void setImage(Bitmap image) {this.image = image;}
    
    public MutableLiveData<VehicleStatus> vehicleStatus()
    {
        return vehicleStatus;
    }
    
    public boolean loadVehicleImage(Context context)
    {
        if(imageFilename.isEmpty())
        {
            Log.w(G.tag, this.name + ": dont have image file. loading placeholder");
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.bm_vehicle_placeholder);
            return false;
        }
        else
        {
            image = BitmapFactory.decodeFile(context.getFilesDir().toString() + "/media/" + imageFilename);
            if(image == null)
            {
                Log.w(G.tag, this.name + ": have, but cannot load image file. loading placeholder");
                image = BitmapFactory.decodeResource(context.getResources(), R.drawable.bm_vehicle_placeholder);
                return false;
            }
            else
                return true;
        }
    }
    
    @SuppressWarnings("ConstantConditions")
    public void update(HashMap<String,Object> map)
    {
        if(map.get("id") != null && !Objects.equals(map.get("id"), this.id))
            this.id = (String)map.get("id");
        if(map.get("name") != null && !Objects.equals(map.get("name"), this.name))
            this.name = (String)map.get("name");
        if(map.get("regNumber") != null && !Objects.equals(map.get("regNumber"), this.regNumber))
            this.regNumber = (String)map.get("regNumber");
        if(map.get("maxVoltage") != null && !Objects.equals(map.get("maxVoltage"), this.maxVoltage))
            this.maxVoltage = ((Number)map.get("maxVoltage")).intValue();
        if(map.get("imageFilename") != null && !Objects.equals(map.get("imageFilename"), this.imageFilename))
            this.imageFilename = (String)map.get("imageFilename");
    }
    
    @NonNull
    @Override
    public String toString()
    {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", regNumber='" + regNumber + '\'' +
                ", maxVoltage=" + maxVoltage +
                ", imageFilename='" + imageFilename + '\'' +
                ", image=" + image +
                ", vehicleStatus=" + vehicleStatus +
                '}';
    }
    
    public HashMap<String,Object> toHashMap()
    {
         return new HashMap<String,Object>()
             {{
                 put("id", id);
                 put("name", name);
                 put("regNumber", regNumber);
                 put("maxVoltage", maxVoltage);
                 put("imageFilename", imageFilename);
             }};
    }
}
