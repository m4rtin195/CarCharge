package com.martin.carcharge.ui.home;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.martin.carcharge.App;
import com.martin.carcharge.BaseActivity;
import com.martin.carcharge.G;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.databinding.FragmentHomeBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.storage.Converters;

import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment
{
    private MainViewModel vm;
    
    FragmentHomeBinding binding;
    View root;
    TextView text_vehicleName, text_regNumber, text_state, text_charge;
    ImageView image_vehicle, image_connectivity;
    ProgressBar progress_charge;
    TextView text_voltage, text_tVoltage, text_current, text_maxCurrent,
            text_chargingTime, text_remainTime,
            text_approach, text_fuel, text_location,
            text_indoorTemp, text_outdoorTemp, text_desiredTemp;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(true, true);
    
        vm = App.getViewModel();
        
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        
        findViews();
        binding.layoutLocation.setOnClickListener(onLocationTileClickListener);
        
        image_connectivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i("daco", "clicked");
                TextView aa = new TextView(requireContext());
                aa.setPadding(32,32,32,32);
                aa.setGravity(Gravity.END);
                aa.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END); //todo co z toho?
                aa.setText("Connected via Sigfox \nUpdated: 14:32:05");
                PopupWindow popupWindow = new PopupWindow(aa, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    
                popupWindow.setTouchable(true);
                //popupWindow.
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.home_tile, requireActivity().getTheme()));
                popupWindow.showAsDropDown(binding.imageConnectivity, -350, 50, Gravity.LEFT);
            }
        });
    
        vm.vehicle().observe(getViewLifecycleOwner(), this::updateVehicleFields);
        vm.vehicleStatus().observe(getViewLifecycleOwner(), this::updateStatusFields);
    
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    private void updateVehicleFields(Vehicle vehicle)
    {
        Log.d(G.tag, "observed vehicle change.");
        
        text_vehicleName.setText(vehicle.getName());
        text_regNumber.setText(vehicle.getRegNumber());
        image_vehicle.setImageBitmap(vehicle.getImage());
    }
    
    private void updateStatusFields(VehicleStatus vs)
    {
        Log.d(G.tag, "observed status change. " + vs.getState());
        
        if(vs.getState() == VehicleStatus.State.Unknown)
        {
            clearStatusFields();
            text_state.setText(getString(R.string.home_state_unknown));
            return;
        }
        if(vs.getState() == VehicleStatus.State.Loading)
        {
            clearStatusFields();
            text_state.setText(getString(R.string.home_state_loading));
            progress_charge.setIndeterminate(true);
            return;
        }
        
        Locale l = ((BaseActivity)requireActivity()).getCurrentLocale();
        int maxVoltage = 700; //Objects.requireNonNull(vm.getVehicleByStatus(vs)).getMaxVoltage();
        
        //topscreen
        text_state.setText(vs.getState().asString(requireContext(), true));
        text_charge.setVisibility(View.VISIBLE);
        text_charge.setText(String.format(l, "%d%%", vs.getCurrent_charge()));
        progress_charge.setIndeterminate(false);
        progress_charge.setProgress(vs.getCurrent_charge(),true);
        progress_charge.setSecondaryProgress(vs.getTarget_charge());
        
        //tiles
        text_voltage.setText(String.format(l, "%.1fV", ((vs.getCurrent_charge()/100f)*(maxVoltage-600)+600)));
        text_tVoltage.setText(String.format(l, "%.0fV", ((vs.getTarget_charge()/100f)*(maxVoltage-600)+600)));
        text_current.setText(String.format(l, "%+dA", vs.getCurrent()));
        text_maxCurrent.setText(vs.getMax_current() != Integer.MIN_VALUE ?
                String.format(l, "%dA", vs.getMax_current()) : "-");
        text_chargingTime.setText(minsToTime(vs.getElapsed_time()));
        text_remainTime.setText(minsToTime(vs.getRemain_time()));
        text_approach.setText(String.format(l, "%dkm", vs.getRange()));
        //text_location.setText(Converters.LocationToFormattedString(vs.getLocation()));
        text_outdoorTemp.setText(String.format(l, "%.1f°C", vs.getOutdoor_temperature()));
        //text_indoorTemp.setText(String.format(l, "%.1f°C", vs.getIndoor_temperature()));
        text_indoorTemp.setText(String.format(l, "26.4°C", vs.getIndoor_temperature()));
        text_desiredTemp.setText(vs.getDesired_temperature() != Float.MIN_VALUE ?
                String.format(l, "%.1f°C", vs.getDesired_temperature()) : "-");
        
        image_connectivity.setVisibility(View.VISIBLE);
        switch(vs.getConnectivity())
        {
            case Unknown:  /*image_connectivity.setImageResource(R.drawable.ic_offline3);*/ break;
            case NotConnected: image_connectivity.setImageResource(R.drawable.ic_offline2); break;
            case Sigfox: /*image_connectivity.setImageResource(R.drawable.ic_iot);*/ break;
            case WiFi: image_connectivity.setImageResource(R.drawable.ic_wifi); break;
        }
    }
    
    private void clearStatusFields()
    {
        String dash = "-";
        
        text_state.setText(dash);
        text_charge.setVisibility(View.INVISIBLE);
        text_charge.setText(dash);
        progress_charge.setIndeterminate(false);
        progress_charge.setProgress(0);
        progress_charge.setSecondaryProgress(0);
        image_connectivity.setVisibility(View.INVISIBLE);
        
        text_voltage.setText(dash);
        text_tVoltage.setText(dash);
        text_current.setText(dash);
        text_maxCurrent.setText(dash);
        text_chargingTime.setText(dash);
        text_remainTime.setText(dash);
        text_approach.setText(dash);
        text_location.setText(dash);
        text_outdoorTemp.setText(dash);
        text_indoorTemp.setText(dash);
        text_desiredTemp.setText(dash);
    }
    
    private void findViews()
    {
        text_vehicleName = binding.textVehicleName;
        text_regNumber = binding.textRegNumber;
        image_vehicle = binding.imageVehicle;
        image_connectivity = binding.imageConnectivity;
        text_state = binding.textState;
        text_charge = binding.textCharge;
        progress_charge = binding.progressCharge;
        text_voltage = binding.textVoltage;
        text_tVoltage = binding.textTVoltage;
        text_current = binding.textCurrent;
        text_maxCurrent = binding.textMaxCurrent;
        text_chargingTime = binding.textChargingTime;
        text_remainTime = binding.textRemainTime;
        text_approach = binding.textApproach;
        text_fuel = binding.textFuel;
        text_location = binding.textLocation;
        text_indoorTemp = binding.textIndoorTemp;
        text_outdoorTemp = binding.textOutdoorTemp;
        text_desiredTemp = binding.textDesiredTemp;
    }
    
    private String minsToTime(int mins)
    {
        int days = 0, hours = 0, minutes = 0;
        days = mins/1440; mins -= days*1440;
        hours = mins/60; mins -= hours*60;
        minutes = mins;
        
        return (days>0 ? (days + "d ") : "") + (hours>0 ? (hours + "h ") : "") + minutes + "m";
    }
    
    View.OnClickListener onLocationTileClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(true /*vm.getCurrentVehicleStatus().getLocation() != null*/)
            {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_navHost);
                navController.navigate(R.id.navigation_action_home_to_map);
            }
            else
                G.debug(requireContext(), "Location is null");
        }
    };
}