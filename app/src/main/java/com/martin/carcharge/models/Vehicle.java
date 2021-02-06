package com.martin.carcharge.models;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class Vehicle
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String regNumber;
    private int batteryCapacity;
    private String imageFile;
    
    public Vehicle()
    {
        name = "";
        regNumber = "";
        batteryCapacity = 0;
        imageFile = "";
    }
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getRegNumber() {return regNumber;}
    public void setRegNumber(String regNumber) {this.regNumber = regNumber;}
    
    public int getBatteryCapacity() {return batteryCapacity;}
    public void setBatteryCapacity(int batteryCapacity) {this.batteryCapacity = batteryCapacity;}
    
    public String getImageFile() {return imageFile;}
    public void setImageFile(String imageFile) {this.imageFile = imageFile;}
}
