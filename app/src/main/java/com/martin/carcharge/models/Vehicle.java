package com.martin.carcharge.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.martin.carcharge.R;

import org.jetbrains.annotations.NotNull;

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
        id = "386625"; //UUID.randomUUID().toString().replace("-","").substring(0, 10); //todo mock
        name = "";
        regNumber = "";
        maxVoltage = 0;
        imageFilename = "";
        
        vehicleStatus = new MutableLiveData<>();
    }
    
    @NonNull
    public String getId() {return id;}
    public void setId(@NotNull String id) {this.id = id;}
    
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
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.bm_vehicle_placeholder);
            return false;
        }
        else
        {
            image = BitmapFactory.decodeFile(context.getFilesDir().toString() + "/media/" + imageFilename);
            return true;
        }
    }
}
