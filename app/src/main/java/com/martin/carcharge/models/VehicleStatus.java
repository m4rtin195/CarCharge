package com.martin.carcharge.models;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.martin.carcharge.R;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "vehicle_statuses",
        foreignKeys = @ForeignKey(entity = Vehicle.class, parentColumns = "id",
                                childColumns = "vehicleId", onDelete = ForeignKey.CASCADE))
public class VehicleStatus
{
    @PrimaryKey
    @SerializedName("id")
    @NonNull
    private String id;
    
    @ColumnInfo(index = true)
    private String vehicleId;
    
    @Expose
    private Date timestamp;
    
    @Expose
    private Connectivity connectivity;
    
    @NonNull
    private State state;
    private int current_charge;
    private int target_charge;
    private int current;
    private int elapsed_time;
    private int remain_time;
    private int range;
    private float outdoor_temperature;
    private float indoor_temperature;
    
    @Expose
    private Location location;
    private int max_current;
    private float desired_temperature;
    
    public enum Connectivity
    {
        Unknown(-1),
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
        Unknown(-1),
        Loading(-2),
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
        public boolean isNormal()
        {
            return (value >= 0);
        }
        
        public String asString(Context context, boolean asProgress)
        {
            String str = new String();
            if(value == Unknown.value) str = context.getString(R.string.home_state_unknown);
            if(value == Loading.value) str = context.getString(R.string.home_state_loading)  + (asProgress ? "..." : "");
            if(value == Off.value) str = context.getString(R.string.home_state_off);
            if(value == Charging.value) str = context.getString(R.string.home_state_charging) + (asProgress ? "..." : "");
            if(value == Idle.value) str = context.getString(R.string.home_state_idle);
            if(value == Driving.value) str = context.getString(R.string.home_state_driving);
            return str;
        }
    }
    
    public VehicleStatus()
    {
        id = "0"; //UUID.randomUUID().toString().replace("-","").substring(0, 10);;
        state = State.Unknown;
        connectivity = Connectivity.Unknown;
        timestamp = new Timestamp(0); //normalna init atributu
        
        location = null;
        max_current = Integer.MIN_VALUE;
        desired_temperature = Float.MIN_VALUE;
        //vehicleId = "1"; //todo prec ked bude api
    }
    
    @Ignore
    public VehicleStatus(@NonNull Vehicle v, @NonNull State s)
    {
        this();
        this.vehicleId = v.getId();
        this.state = s;
    }

    @NonNull
    public String getId()
    {
        return id;
    }
    public void setId(@NonNull String id)
    {
        this.id = id;
    }
    
    public String getVehicleId()
    {
        return vehicleId;
    }
    public void setVehicleId(String vehicleId)
    {
        this.vehicleId = vehicleId;
    }
    
    public Date getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(Date timestamp)
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
    
    @NonNull
    public State getState()
    {
        return state;
    }
    public void setState(@NonNull State state)
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
    
    public Location getLocation() {return location;}
    public void setLocation(Location location) {this.location = location;}
    
    public int getMax_current() {return max_current;}
    public void setMax_current(int max_current) {this.max_current = max_current;}
    
    public float getDesired_temperature() {return desired_temperature;}
    public void setDesired_temperature(float desired_temperature) {this.desired_temperature = desired_temperature;}
    
    @NonNull
    @Override
    public String toString()
    {
        return "VehicleStatus{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", timestamp=" + timestamp.toString() +
                ", connectivity=" + connectivity +
                ", state=" + state +
                ", current_charge=" + current_charge +
                ", target_charge=" + target_charge +
                ", current=" + current +
                ", elapsed_time=" + elapsed_time +
                ", remain_time=" + remain_time +
                ", range=" + range +
                ", outdoor_temperature=" + outdoor_temperature +
                ", indoor_temperature=" + indoor_temperature +
                ", desired_temperature=" + desired_temperature +
                ", location=" + location.toString() +
                ", max_current=" + max_current +
                '}';
    }
}

