package com.martin.carcharge.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.R;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

public class HomeFragment extends Fragment
{
    
    private MainViewModel vm;
    SharedPreferences settings;
    
    TextView text_vehicleName, text_regNumber, text_state, text_charge;
    ImageView image_vehicle;
    ProgressBar progress_charge;
    TextView text_voltage, text_tVoltage, text_current, text_maxCurrent,
            text_chargingTime, text_remainTime,
            text_approach, text_fuel, text_location,
            text_indoorTemp, text_outdoorTemp, text_desiredTemp;
    FloatingActionButton fab_action;
    
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        findViews(root);
        fab_action.setOnClickListener(actionOnClickListener);
    
        vm.getVehicle().observe(getViewLifecycleOwner(), this::updateVehicleFields);
        vm.getVehicleStatus().observeForever(new Observer<VehicleStatus>()
        {
            @Override
            public void onChanged(VehicleStatus vehicleStatus)
            {
                Log.i("daco", "changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        });
        Log.i("daco", "registering observer to vm id: " + vm.toString());
    
        return root;
    }
    
    void updateVehicleFields(Vehicle vehicle)
    {
        Log.i("daco", "observed vehicle.");
        text_vehicleName.setText(vehicle.getName());
        text_regNumber.setText(vehicle.getRegNumber());
        image_vehicle.setVisibility(vehicle.getImageUri() == null ? View.VISIBLE : View.GONE);
        image_vehicle.setImageURI(vehicle.getImageUri());
    }
    
    @SuppressLint("DefaultLocale") //todo prec
    void updateStatusFields(VehicleStatus vs)
    {
        Log.i("daco", "observed status.");
        //text_state.setText(vs.getState());
        text_charge.setText(String.format("%d%%", vs.getCurrent_charge()));
        progress_charge.setProgress(vs.getCurrent_charge(),true);
        progress_charge.setSecondaryProgress(vs.getTarget_charge());
        
        //text_voltage.setText("648.2V");
        //text_tVoltage.setText("800V");
        text_current.setText(vs.getCurrent());
        //text_maxCurrent.setText(vs.g);
        text_chargingTime.setText(minsToTime(vs.getElapsed_time()));
        text_remainTime.setText(minsToTime(vs.getRemain_time()));
        text_approach.setText(vs.getRange());
        //text_location.setText("49°12’32”N  18°45’36”E");
        //text_outdoorTemp.setText("13.2°C");
        text_indoorTemp.setText(String.format("%.1f%%", vs.getIndoor_temperature()));
        //text_desiredTemp.setText("20.0°C");
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
        fab_action = requireActivity().findViewById(R.id.fab_action);
    }
    
    private String minsToTime(int mins)
    {
        int days = 0, hours = 0, minutes = 0;
        days = mins/1440; mins -= days*1440;
        hours = mins/60; mins -= hours*60;
        minutes = mins;
        
        return (days>0 ? (days + " days, ") : "") + (hours>0 ? (hours + " hours, ") : "") + minutes + " mins";
    }
    
    void updateImage()
    {
        //image_vehicle.setImageURI(mediaURI);
    }
    
    View.OnClickListener actionOnClickListener = view ->
    {
        /*Vehicle vehicle = vm.getVehicle().getValue();
        assert vehicle != null : "Vehicle in viewmodel is null";
        
        text_vehicleName.setText(vehicle.getName()); //todo osetrenie ci vehicle existuje
        text_regNumber.setText(vehicle.getRegNumber());
        
        text_state.setText(getString(R.string.home_state_loading));
        progress_charge.setIndeterminate(true);*/
    
        VehicleStatus vs = new VehicleStatus();
        vs.current = 322;
        //vs.print();
        vm.setVehicleStatus(new MutableLiveData<>(vs));
    
        Vehicle vh = new Vehicle();
        vh.setName("CCCC");
        vh.setRegNumber("dd");
        vm.setVehicle(new MutableLiveData<>(vh));
        Log.i("daco", "click");
    };
}