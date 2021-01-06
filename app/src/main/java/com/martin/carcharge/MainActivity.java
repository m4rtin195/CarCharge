package com.martin.carcharge;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
{
    eCar_IoT_Kit_API cloud_api;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        BottomNavigationView navbar = findViewById(R.id.navbar);
        
        NavController navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();
        NavigationUI.setupWithNavController(navbar, navController);
    
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fe6ea208.eu-gb.apigw.appdomain.cloud/ecar-iot-kit-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        cloud_api = retrofit.create(eCar_IoT_Kit_API.class);
    }
}