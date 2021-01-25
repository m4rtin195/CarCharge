package com.martin.carcharge.models;

import android.util.Log;

public class VehicleStatus
{
    private String _id;
    private String _rev;

    private int state;              //enum?
    private int current_charge;
    private int target_charge;
    public int current; //todo change
    private int elapsed_time;
    private int remain_time;
    private int range;
    private float elec_consumption;
    private float indoor_temperature;

    private boolean parsed;


    public String get_id()
    {
        return _id;
    }

    public String get_rev()
    {
        return _rev;
    }

    public int getState()
    {
        return state;
    }

    public int getCurrent_charge()
    {
        return current_charge;
    }

    public int getTarget_charge()
    {
        return target_charge;
    }

    public int getCurrent()
    {
        return current;
    }

    public int getElapsed_time()
    {
        return elapsed_time;
    }

    public int getRemain_time()
    {
        return remain_time;
    }

    public int getRange()
    {
        return range;
    }

    public float getElec_consumption()
    {
        return elec_consumption;
    }

    public float getIndoor_temperature()
    {
        return indoor_temperature;
    }

    public boolean isParsed()
    {
        return parsed;
    }
    
    public void print()
    {
        Log.i("daco", "_id: " + _id);
        Log.i("daco", "_rev: " + _rev);
        Log.i("daco", "state: " + state);
        Log.i("daco", "current_charge: " + current_charge);
        Log.i("daco", "target_charge: " + target_charge);
        Log.i("daco", "current: " + current);
        Log.i("daco", "elapsed_time: " + elapsed_time);
        Log.i("daco", "remain_time: " + remain_time);
        Log.i("daco", "range: " + range);
        Log.i("daco", "elec_consumption: " + elec_consumption);
        Log.i("daco", "indoor_temperature: " + indoor_temperature);
    }
}

