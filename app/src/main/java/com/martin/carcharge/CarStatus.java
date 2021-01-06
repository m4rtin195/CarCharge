package com.martin.carcharge;

public class CarStatus
{
    private String _id;
    private String _rev;

    private int state;              //enum?
    private int current_charge;
    private int target_charge;
    private int current;
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
}

