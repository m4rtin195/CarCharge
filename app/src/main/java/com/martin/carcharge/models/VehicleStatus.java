package com.martin.carcharge.models;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.martin.carcharge.G;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

@Entity(tableName = "vehicle_statuses",
        foreignKeys = @ForeignKey(entity = Vehicle.class, parentColumns = "id",
                                childColumns = "vehicleId", onDelete = ForeignKey.CASCADE))
public class VehicleStatus
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String _id; //todo merge with id
    
    @Ignore
    private String _rev;
    
    @ColumnInfo(index = true)
    private long vehicleId;
    private Timestamp timestamp;
    
    private State state;
    private int current_charge;
    private int target_charge;
    private int current;
    private int elapsed_time;
    private int remain_time;
    private int range;
    private float elec_consumption;
    private float indoor_temperature;
    
    public enum State
    {
        Off("Off"),
        Charging("Charging..."),
        Idle("Idle"),
        Driving("Driving"),
        Initializing("Initializing...");
        
        public final String text;
        State(String label) {this.text = label;}
    }
    
    public VehicleStatus()
    {
        this.state = State.Initializing;
    }
    
    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }
    
    public String get_id()
    {
        return _id;
    }
    
    public void set_id(String _id)
    {
        this._id = _id;
    }
    
    public String get_rev()
    {
        return _rev;
    }
    
    public void set_rev(String _rev)
    {
        this._rev = _rev;
    }
    
    public long getVehicleId()
    {
        return vehicleId;
    }
    
    public void setVehicleId(long vehicleId)
    {
        this.vehicleId = vehicleId;
    }
    
    public Timestamp getTimestamp()
    {
        return timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }
    
    public State getState()
    {
        return state;
    }
    
    public void setState(State state)
    {
        this.state = state;
    }
    
    public int getCurrent_charge()
    {
        return current_charge;
    }
    
    public void setCurrent_charge(int current_charge)
    {
        this.current_charge = current_charge;
    }
    
    public int getTarget_charge()
    {
        return target_charge;
    }
    
    public void setTarget_charge(int target_charge)
    {
        this.target_charge = target_charge;
    }
    
    public int getCurrent()
    {
        return current;
    }
    
    public void setCurrent(int current)
    {
        this.current = current;
    }
    
    public int getElapsed_time()
    {
        return elapsed_time;
    }
    
    public void setElapsed_time(int elapsed_time)
    {
        this.elapsed_time = elapsed_time;
    }
    
    public int getRemain_time()
    {
        return remain_time;
    }
    
    public void setRemain_time(int remain_time)
    {
        this.remain_time = remain_time;
    }
    
    public int getRange()
    {
        return range;
    }
    
    public void setRange(int range)
    {
        this.range = range;
    }
    
    public float getElec_consumption()
    {
        return elec_consumption;
    }
    
    public void setElec_consumption(float elec_consumption)
    {
        this.elec_consumption = elec_consumption;
    }
    
    public float getIndoor_temperature()
    {
        return indoor_temperature;
    }
    
    public void setIndoor_temperature(float indoor_temperature)
    {
        this.indoor_temperature = indoor_temperature;
    }
    
    @NotNull
    @Override
    public String toString()
    {
        return "VehicleStatus{" +
                "id=" + id +
                ", _id='" + _id + '\'' +
                ", _rev='" + _rev + '\'' +
                ", vehicleId=" + vehicleId +
                ", timestamp=" + timestamp +
                ", state=" + state +
                ", current_charge=" + current_charge +
                ", target_charge=" + target_charge +
                ", current=" + current +
                ", elapsed_time=" + elapsed_time +
                ", remain_time=" + remain_time +
                ", range=" + range +
                ", elec_consumption=" + elec_consumption +
                ", indoor_temperature=" + indoor_temperature +
                '}';
    }
}

