package com.martin.carcharge.ui.home;

import android.os.Bundle;
import android.text.format.DateUtils;
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
import androidx.lifecycle.Observer;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

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
            text_range, text_fuel, text_location,
            text_indoorTemp, text_outdoorTemp, text_desiredTemp;
    
    Vehicle currentVehicle;
    Observer<VehicleStatus> currentVehicleStatusObserver = this::updateStatusFields;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(true, true);
    
        vm = App.getViewModel();
        
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        
        bindViews();
        binding.layoutLocation.setOnClickListener(onLocationTileClickListener);
        image_connectivity.setOnClickListener(onImageConnectivityClickListener);
    
        currentVehicle = vm.getCurrentVehicle();
        vm.currentVehicle().observe(getViewLifecycleOwner(), vehicleSwitchedObserver);
        vm.currentVehicleStatus().observe(getViewLifecycleOwner(), currentVehicleStatusObserver);
    
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    
    Observer<Vehicle> vehicleSwitchedObserver = new Observer<Vehicle>()
    {
        @Override
        public void onChanged(Vehicle vehicle)
        {
            updateVehicleFields(vehicle);
            currentVehicle.vehicleStatus().removeObserver(currentVehicleStatusObserver);
            vm.currentVehicleStatus().observe(getViewLifecycleOwner(), currentVehicleStatusObserver);
        }
    };
    
    void updateVehicleFields(Vehicle vehicle)
    {
        if(vehicle == null) return;
        Log.d(G.tag, "[observed vehicle change]");
        
        text_vehicleName.setText(vehicle.getName());
        text_regNumber.setText(vehicle.getRegNumber());
        image_vehicle.setImageBitmap(vehicle.getImage());
    }
    
    private void updateStatusFields(VehicleStatus vs)
    {
        if(vs == null) return;
        Log.d(G.tag, "[observed status change: " + vs.getState() + "]");
        
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
        //text_voltage.setText(String.format(l, "%.1fV", ((vs.getCurrent_charge()/100f)*(maxVoltage-600)+600)));
        text_voltage.setText("449.2V");
        text_tVoltage.setText(String.format(l, "%.0fV", ((vs.getTarget_charge()/100f)*(maxVoltage-600)+600)));
        text_current.setText(String.format(l, "%+dA", vs.getCurrent()));
        text_maxCurrent.setText(vs.getMax_current() != Integer.MIN_VALUE ?
                String.format(l, "%dA", vs.getMax_current()) : "-");
        text_chargingTime.setText(minsToTime(vs.getElapsed_time()));
        text_remainTime.setText(minsToTime(vs.getRemain_time()));
        text_range.setText(String.format(l, "%dkm", vs.getRange()));
        text_location.setText(Converters.LocationToAddressOrFormattedString(vs.getLocation(), requireContext()).split(",")[0]);
        text_outdoorTemp.setText(String.format(l, "%.1f°C", vs.getOutdoor_temperature()));
        text_indoorTemp.setText(String.format(l, "%.1f°C", vs.getIndoor_temperature()));
        text_desiredTemp.setText(vs.getDesired_temperature() != Float.MIN_VALUE ?
                String.format(l, "%.1f°C", vs.getDesired_temperature()) : "-");
        
        image_connectivity.setVisibility(View.VISIBLE);
        switch(vs.getConnectivity())
        {
            case Unknown: image_connectivity.setImageDrawable(null); break;
            case NotConnected: image_connectivity.setImageResource(R.drawable.ic_offline2); break;
            case Sigfox: image_connectivity.setImageResource(R.drawable.ic_iot); break;
            case WLAN: image_connectivity.setImageResource(R.drawable.ic_wlan); break;
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
        text_range.setText(dash);
        text_location.setText(dash);
        text_outdoorTemp.setText(dash);
        text_indoorTemp.setText(dash);
        text_desiredTemp.setText(dash);
    }
    
    private void bindViews()
    {
        text_vehicleName = binding.textVehicleName;
        text_regNumber = binding.textRegNumber;
        image_vehicle = binding.imageVehicle;
        image_connectivity = binding.imageConnectivity;
        text_state = binding.textState;
        text_charge = binding.textCharge;
        progress_charge = binding.progressbarCharge;
        text_voltage = binding.textVoltage;
        text_tVoltage = binding.textTVoltage;
        text_current = binding.textCurrent;
        text_maxCurrent = binding.textMaxCurrent;
        text_chargingTime = binding.textChargingTime;
        text_remainTime = binding.textRemainTime;
        text_range = binding.textRange;
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
            if(vm.getCurrentVehicleStatus() != null && vm.getCurrentVehicleStatus().getLocation() != null)
            {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_navHost);
                navController.navigate(R.id.navigation_action_to_map);
            }
            else
                G.debug(requireContext(), "Location is null");
        }
    };
    
    View.OnClickListener onImageConnectivityClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            TextView textView = new TextView(requireContext());
            textView.setPadding(32,32,32,32);
            textView.setGravity(Gravity.END);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            
            VehicleStatus vs = vm.getCurrentVehicleStatus();
            assert vs != null;
            String str = requireContext().getString(R.string.home_connectivity_connected_via) + " " + vs.getConnectivity().toString() + "\n" +
                    requireContext().getString(R.string.home_connectivity_updated) + " " + DateUtils.formatSameDayTime(vs.getTimestamp().getTime(), new Date().getTime(), DateFormat.SHORT, DateFormat.MEDIUM);
                    /*new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(vs.getTimestamp())*/ //TODO format dna?
            
            textView.setText(str);
            
            PopupWindow popupWindow = new PopupWindow(textView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.home_tile, requireActivity().getTheme()));
            popupWindow.showAsDropDown(binding.imageConnectivity, -350, 50, Gravity.LEFT);
        }
    };
}