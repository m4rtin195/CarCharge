package com.martin.carcharge.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.List;
import java.util.Locale;

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
    
    @SuppressLint("MissingPermission") //todo ask permission
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        LatLng defaultPoint = new LatLng(36.0, 8.0); //center world
        
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.setPadding(50, 50, 50, 50);
        gMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.googlemap_style_json)));
        gMap.setInfoWindowAdapter(infoWindowAdapter);
        if(askLocationAccess()) gMap.setMyLocationEnabled(true);
        
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(defaultPoint);
        markerOptions.title(vm.vehicle().getValue().getName());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        markerOptions.visible(false);
    
        vehicleMarker = gMap.addMarker(markerOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPoint, 1));
    
        vm.vehicleStatus().observe(getViewLifecycleOwner(), this::updateVehicleLocation); //todo funguje?
        updateVehicleLocation(vm.getCurrentVehicleStatus());
    }
    
    private void updateVehicleLocation(@Nullable VehicleStatus vs)
    {
        if(vs != null && vs.getLocation() != null)
        {
            Location location = vs.getLocation();
            //Location location = new Location("mock"); location.setLatitude(49.20464); location.setLongitude(18.75509); //uniza
            LatLng location2 = new LatLng(location.getLatitude(), location.getLongitude());
            
            vehicleMarker.setPosition(location2);
            vehicleMarker.setVisible(true);
            vehicleMarker.setSnippet(Converters.LocationToAddressOrFormattedString(location, getContext()).replaceFirst(",","\n"));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location2, 15));
        }
        else;
            //if(vehicleMarker != null) vehicleMarker.setVisible(false); //todo aaa
    }
    
    public boolean askLocationAccess()
    {
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
        {
            //((MainActivity)requireActivity()).showSnack(getString(R.string.map_toast_location_access_not_granted), Snackbar.LENGTH_SHORT);
            Toast.makeText(requireContext(), getString(R.string.map_toast_location_access_not_granted), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter()
    {
        @Override
        public View getInfoWindow(@NonNull Marker arg0) {return null;}
    
        @Override
        public View getInfoContents(@NonNull Marker marker)
        {
            Context context = getContext();
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
    
            TextView title = new TextView(context);
            title.setTextSize(18);
            title.setTextColor(Color.BLACK);
            title.setGravity(Gravity.CENTER);
            title.setTypeface(null, Typeface.BOLD);
            title.setText(marker.getTitle());
    
            TextView snippet = new TextView(context);
            snippet.setTextColor(Color.GRAY);
            snippet.setGravity(Gravity.CENTER);
            snippet.setText(marker.getSnippet());
    
            layout.addView(title);
            layout.addView(snippet);
            return layout;
        }
    };
}