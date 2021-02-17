package com.martin.carcharge.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.martin.carcharge.R;

@Entity(tableName = "vehicles")
public class Vehicle
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String regNumber;
    private int maxVoltage;
    private String imageFile;
    
    @Ignore
    private Bitmap image;
    
    public Vehicle()
    {
        name = "";
        regNumber = "";
        maxVoltage = 0;
        imageFile = "";
    }
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getRegNumber() {return regNumber;}
    public void setRegNumber(String regPlate) {this.regNumber = regPlate;}
    
    public int getMaxVoltage() {return maxVoltage;}
    public void setMaxVoltage(int maxVoltage) {this.maxVoltage = maxVoltage;}
    
    public String getImageFile() {return imageFile;}
    public void setImageFile(String imageFile) {this.imageFile = imageFile;}
    
    public Bitmap getImage() {return image;}
    public void setImage(Bitmap image) {this.image = image;}
    
    
    public boolean loadVehicleImage(Context context)
    {
        if(imageFile.isEmpty())
        {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.bm_vehicle_placeholder);
            return false;
        }
        else
        {
            image = BitmapFactory.decodeFile(context.getFilesDir().toString() + "/media/" + imageFile);
            return true;
        }
    }
}
