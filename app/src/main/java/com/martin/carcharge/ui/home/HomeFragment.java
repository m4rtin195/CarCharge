package com.martin.carcharge.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.martin.carcharge.R;

public class HomeFragment extends Fragment
{
    
    private HomeViewModel homeViewModel;
    SharedPreferences settings;
    
    TextView text_vehicleName, text_regNumber, text_state, text_charge;
    ImageView image_vehicle;
    ProgressBar progress_charge;
    
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        //homeViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        
        text_vehicleName
        
        return view;
    }
}