package com.martin.carcharge.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.martin.carcharge.App;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.databinding.FragmentMapBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.storage.Converters;

public class MapFragment extends Fragment implements OnMapReadyCallback
{
    private MainViewModel vm;
    
    FragmentMapBinding binding;
    View root;
    Toolbar toolbar;
    FragmentContainerView mapFragment;
    GoogleMap gMap;
    
    Marker vehicleMarker;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity) requireActivity()).setBottomBarVisible(false, true);
        vm = App.getViewModel();
        
        binding = FragmentMapBinding.inflate(inflater, container, false);
        root = binding.getRoot();
    
        toolbar = binding.toolbarPreferences;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
            
        mapFragment = binding.fragmentMap;
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    @SuppressLint("MissingPermission")
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        LatLng defaultPoint = new LatLng(36.0, 8.0);
        
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.setPadding(50, 50, 50, 50);
        gMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        if(askLocationAccess()) gMap.setMyLocationEnabled(true);
        
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(defaultPoint);
        markerOptions.title(vm.vehicle().getValue().getName());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        markerOptions.visible(false);
    
        vehicleMarker = gMap.addMarker(markerOptions);
        assert vehicleMarker != null : "dpc preco";
        Log.i("daco", "aaaa");
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPoint, 1));
    
        vm.vehicleStatus().observe(getViewLifecycleOwner(), this::updateVehicleLocation);
        updateVehicleLocation(vm.getCurrentVehicleStatus());
    }
    
    private void updateVehicleLocation(VehicleStatus vs)
    {
        if(true /*vs.getLocation() != null*/)
        {
            Log.i("daco", "bbbbb");
    
            //Location location = vs.getLocation();
            Location location = new Location("mock"); location.setLatitude(49.20464); location.setLongitude(18.75509); //todo aaaaa
            LatLng location2 = new LatLng(location.getLatitude(), location.getLongitude());
            
            vehicleMarker.setVisible(true);
            vehicleMarker.setPosition(location2);
            vehicleMarker.setSnippet(Converters.LocationToFormattedString(location));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location2, 15));
        }
        else
            if(vehicleMarker != null) vehicleMarker.setVisible(false);
    }
    
    public boolean askLocationAccess()
    {
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
        {
            ((MainActivity)requireActivity()).showSnack(getString(R.string.map_toast_location_access_not_granted), Snackbar.LENGTH_SHORT);
            //Toast.makeText(requireContext(), getString(R.string.toast_location_access_not_granted), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}