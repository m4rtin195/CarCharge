package com.martin.carcharge.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

@Entity(tableName = "vehicle_statuses",
        foreignKeys = @ForeignKey(entity = Vehicle.class, parentColumns = "id",
                                childColumns = "vehicleId", onDelete = ForeignKey.CASCADE))
public class VehicleStatus
{
    @PrimaryKey
    @SerializedName("_id")
    @NonNull
    private String id;
    
    @ColumnInfo(index = true)
    private long vehicleId;
    
    @Expose
    private Timestamp timestamp;
    
    @Expose
    private Connectivity connectivity;
    
    private State state;
    private int current_charge;
    private int target_charge;
    private int current;
    private int elapsed_time;
    private int remain_time;
    private int range;
    private float outdoor_temperature;
    private float indoor_temperature;
    
    public enum Connectivity
    {
        Unknown(Integer.MIN_VALUE),
        @SerializedName("0") NotConnected(0),
        @SerializedName("1") Sigfox(1),
        @SerializedName("2") WiFi(2);
        
        public final int value;
        Connectivity(int value)
        {
            this.value = value;
        }
        public static Connectivity getConnectivity(int value)
        {
            for(Connectivity c : values())
                if(c.value == value) return c;
            return null;
        }
    }
    
    public enum State
    {
        Unknown(Integer.MIN_VALUE),
        Loading(Integer.MIN_VALUE+1),
        @SerializedName("0") Off(0),
        @SerializedName("1") Charging(1),
        @SerializedName("2") Idle(2),
        @SerializedName("3") Driving(3);
        
        public final int value;
        State(int value)
        {
            this.value = value;
        }
        public static State getState(int value)
        {
            for(State state : values())
                if(state.value == value) return state;
            return null;
        }
        public boolean isValid()
        {
            return (value >= 0);
        }
    }
    
    public VehicleStatus()
    {
        id = UUID.randomUUID().toString().replace("-","");
        state = State.Unknown;
        connectivity = Connectivity.Unknown;
        timestamp = new Timestamp(0);
        
        vehicleId = 1; //todo prec
    }
    
    @Ignore
    public VehicleStatus(State state)
    {
        this();
        this.state = state;
    }
    
    @NotNull
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
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
    
    public Connectivity getConnectivity()
    {
        return connectivity;
    }
    public void setConnectivity(Connectivity connectivity)
    {
        this.connectivity = connectivity;
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
    
    public float getOutdoor_temperature()
    {
        return outdoor_temperature;
    }
    public void setOutdoor_temperature(float outdoor_temperature)
    {
        this.outdoor_temperature = outdoor_temperature;
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
                ", _id='" + id + '\'' +
                ", vehicleId=" + vehicleId +
                ", timestamp=" + timestamp +
                ", state=" + state +
                ", current_charge=" + current_charge +
                ", target_charge=" + target_charge +
                ", current=" + current +
                ", elapsed_time=" + elapsed_time +
                ", remain_time=" + remain_time +
                ", range=" + range +
                ", elec_consumption=" + outdoor_temperature +
                ", indoor_temperature=" + indoor_temperature +
                '}';
    }
}

