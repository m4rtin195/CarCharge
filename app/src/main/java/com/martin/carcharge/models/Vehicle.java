package com.martin.carcharge.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class Vehicle
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String regNumber;
    private int batteryMaxVoltage;
    private String imageFile;
    
    public Vehicle()
    {
        name = "";
        regNumber = "";
        batteryMaxVoltage = 0;
        imageFile = "";
    }
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getRegNumber() {return regNumber;}
    public void setRegNumber(String regNumber) {this.regNumber = regNumber;}
    
    public int getBatteryMaxVoltage() {return batteryMaxVoltage;}
    public void setBatteryMaxVoltage(int batteryMaxVoltage) {this.batteryMaxVoltage = batteryMaxVoltage;}
    
    public String getImageFile() {return imageFile;}
    public void setImageFile(String imageFile) {this.imageFile = imageFile;}
}
