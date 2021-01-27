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
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "regNumber")
    private String regNumber;
    
    @ColumnInfo(name = "batteryCapacity")
    private int batteryCapacity;
    
    @ColumnInfo(name = "imageUri")
    private Uri imageUri;
    
    public Vehicle()
    {
        name = "";
        regNumber = "";
        batteryCapacity = 0;
        imageUri = null;
    }
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getRegNumber() {return regNumber;}
    public void setRegNumber(String regNumber) {this.regNumber = regNumber;}
    
    public int getBatteryCapacity() {return batteryCapacity;}
    public void setBatteryCapacity(int batteryCapacity) {this.batteryCapacity = batteryCapacity;}
    
    public Uri getImageUri() {return imageUri;}
    public void setImageUri(Uri imageUri) {this.imageUri = imageUri;}
}
