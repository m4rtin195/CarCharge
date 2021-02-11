package com.martin.carcharge.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.martin.carcharge.App;
import com.martin.carcharge.G;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.R;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

public class HomeFragment extends Fragment
{
    private SharedPreferences pref;
    private MainViewModel vm;
    
    TextView text_vehicleName, text_regNumber, text_state, text_charge;
    ImageView image_vehicle;
    ProgressBar progress_charge;
    TextView text_voltage, text_tVoltage, text_current, text_maxCurrent,
            text_chargingTime, text_remainTime,
            text_approach, text_fuel, text_location,
            text_indoorTemp, text_outdoorTemp, text_desiredTemp;
    
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(true);
        
        View root = inflater.inflate(R.layout.fragment_home, container, false);
    
        pref = App.getPreferences();
        vm = App.getViewModel();
        
        findViews(root);
    
        vm.vehicle().observe(getViewLifecycleOwner(), this::updateVehicleFields);
        vm.vehicleStatus().observeForever(this::updateStatusFields); //todo forever?
    
        return root;
    }
    
    void updateVehicleFields(Vehicle vehicle)
    {
        Log.i(G.tag, "observed vehicle change.");
        text_vehicleName.setText(vehicle.getName());
        text_regNumber.setText(vehicle.getRegNumber());
        if(vehicle.getImageFile().isEmpty())
            image_vehicle.setImageResource(R.drawable.vehicle_placeholder);
        else
        {
            Uri uri = Uri.parse(requireActivity().getFilesDir() + "/media/" + vehicle.getImageFile());
            image_vehicle.setImageURI(uri); //todo
        }
    }
    
    @SuppressLint("DefaultLocale") //todo prec
    void updateStatusFields(VehicleStatus vs)
    {
        Log.i(G.tag, "observed status change.");
        
        int maxVoltage = 851; //todo load odniekial
        
        if(vs.getState() == VehicleStatus.State.Loading)
        {
            loading();
            return;
        }
        
        if(vs.getState() != null) text_state.setText(stateToString(vs.getState()));
        text_charge.setText(String.format("%d%%", vs.getCurrent_charge()));
        progress_charge.setIndeterminate(false);
        progress_charge.setProgress(vs.getCurrent_charge(),true);
        progress_charge.setSecondaryProgress(vs.getTarget_charge());
        
        text_voltage.setText(String.format("%.1fV", (vs.getCurrent_charge()/100f)*maxVoltage));
        text_tVoltage.setText(String.format("%.0fV", (vs.getTarget_charge()/100f)*maxVoltage));
        text_current.setText(String.format("%+dA", vs.getCurrent())); //todo a minus?
        //text_maxCurrent.setText(vs.g);
        text_chargingTime.setText(minsToTime(vs.getElapsed_time()));
        text_remainTime.setText(minsToTime(vs.getRemain_time()));
        text_approach.setText(String.format("%dkm", vs.getRange()));
        //text_location.setText("49°12’32”N  18°45’36”E");
        text_outdoorTemp.setText(String.format("%.1f°C", vs.getOutdoor_temperature()));
        text_indoorTemp.setText(String.format("%.1f°C", vs.getIndoor_temperature()));
        //text_desiredTemp.setText("20.0°C");
    }
    
    private String stateToString(VehicleStatus.State st)
    {
        String s = new String();
        
        if(st == VehicleStatus.State.Unknown) s = getString(R.string.home_state_unknown);
        if(st == VehicleStatus.State.Loading) s = getString(R.string.home_state_loading);
        if(st == VehicleStatus.State.Off) s = getString(R.string.home_state_off);
        if(st == VehicleStatus.State.Charging) s = getString(R.string.home_state_charging);
        if(st == VehicleStatus.State.Idle) s = getString(R.string.home_state_idle);
        if(st == VehicleStatus.State.Driving) s = getString(R.string.home_state_driving);
    
        return s;
    }
    
    public void loading()
    {
        text_state.setText(getString(R.string.home_state_loading));
        progress_charge.setIndeterminate(true);
    }
    
    private void findViews(View root)
    {
        text_vehicleName = root.findViewById(R.id.text_vehicleName);
        text_regNumber = root.findViewById(R.id.text_regNumber);
        image_vehicle = root.findViewById(R.id.image_vehicle);
        text_state = root.findViewById(R.id.text_state);
        text_charge = root.findViewById(R.id.text_charge);
        progress_charge = root.findViewById(R.id.progress_charge);
        text_voltage = root.findViewById(R.id.text_voltage);
        text_tVoltage = root.findViewById(R.id.text_tVoltage);
        text_current = root.findViewById(R.id.text_current);
        text_maxCurrent = root.findViewById(R.id.text_maxCurrent);
        text_chargingTime = root.findViewById(R.id.text_chargingTime);
        text_remainTime = root.findViewById(R.id.text_remainTime);
        text_approach = root.findViewById(R.id.text_approach);
        text_fuel = root.findViewById(R.id.text_fuel);
        text_location = root.findViewById(R.id.text_location);
        text_indoorTemp = root.findViewById(R.id.text_indoorTemp);
        text_outdoorTemp = root.findViewById(R.id.text_outdoorTemp);
        text_desiredTemp = root.findViewById(R.id.text_desiredTemp);
    }
    
    private String minsToTime(int mins)
    {
        int days = 0, hours = 0, minutes = 0;
        days = mins/1440; mins -= days*1440;
        hours = mins/60; mins -= hours*60;
        minutes = mins;
        
        return (days>0 ? (days + "d ") : "") + (hours>0 ? (hours + "h ") : "") + minutes + "m";
    }
}