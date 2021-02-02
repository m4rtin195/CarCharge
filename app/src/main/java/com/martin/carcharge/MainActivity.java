package com.martin.carcharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Objects;


public class MainActivity extends BaseActivity
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    private FirebaseAuth auth;
    
    NavController navController;
    
    CoordinatorLayout layout_root;
    FloatingActionButton fab_action;
    BottomAppBar bottomAppBar;
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setupUI();
        setContentView(R.layout.activity_main);
        
        db = AppActivity.getDatabase();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        vm = new ViewModelProvider(this).get(MainViewModel.class);
        auth = FirebaseAuth.getInstance();
    
        layout_root = findViewById(R.id.layout_root);
        fab_action = findViewById(R.id.fab_action);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);
    
        BottomNavigationView navbar = findViewById(R.id.navbar);
        
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();
        NavigationUI.setupWithNavController(navbar, navController);
    
        Downloader downloader = new Downloader(this);
        //downloader.start();
        
        loadVehicle();
        //updateStatus();
        
    } //onCreate
    
    @Override
    protected void onResume() //todo presun
    {
        super.onResume();
        registerReceiver(myReceiver, new IntentFilter("custom-update"));
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(myReceiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu)
    {
        getMenuInflater().inflate(R.menu.bottom_appbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.menu_history)
            navController.navigate(R.id.navigation_action_home_to_history);
        if(item.getItemId() == R.id.menu_refresh)
            navController.navigate(R.id.navigation_action_home_to_preferences);
        
        return true;
    }
    
    /**********/
    
    void loadVehicle()
    {
        long lastVehicleId = pref.getLong("last_vehicle_id", 0);
        if(lastVehicleId == 0)
        {
            Vehicle temp = new Vehicle();
            temp.setName("New vehicle");
            lastVehicleId = db.dao().insert(temp);
            pref.edit().putLong("last_vehicle_id", lastVehicleId).apply();
        }
        if(db.dao().getVehicle(lastVehicleId) == null)
        {
            Log.i(G.tag,"dlt");
            pref.edit().remove("last_vehicle_id").apply();
            loadVehicle();
            return;
        }
        
        Vehicle vehicle = db.dao().getVehicle(lastVehicleId);
        vm.Vehicle().postValue(vehicle);
        pref.edit()
                .putString("vehicle_name", vehicle.getName())
                .putString("vehicle_regplate", vehicle.getRegNumber())
                .putString("vehicle_capacity", vehicle.getBatteryCapacity() > 0 ?
                        String.valueOf(vehicle.getBatteryCapacity()) : "")
                .apply();
    }
    
    public void updateStatus()
    {
        Log.i(G.tag, "updateStatus()");
        //Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), "Updating...", Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).setAction("Znova!!", null).show();
    }
    
    public BroadcastReceiver myReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String json = intent.getStringExtra("json");
            Type type = new TypeToken<VehicleStatus>() {}.getType();
            VehicleStatus vs = new Gson().fromJson(json, type);
            vs.setState(VehicleStatus.State.Charging);
            vm.VehicleStatus().postValue(vs);
            Snackbar.make(getWindow().getDecorView().getRootView(), "Received FCM update. " +
                    Calendar.getInstance().getTime().getHours() + ":" +
                    Calendar.getInstance().getTime().getMinutes() + ":" +
                    Calendar.getInstance().getTime().getSeconds(), Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).setAction("Znova!!", null).show();
        }
    };
    
    
    
    public void setFabVisible(boolean v)
    {
        fab_action.setVisibility(v ? View.VISIBLE : View.GONE);
    }
    public void setBottomBarVisible(boolean v)
    {
        bottomAppBar.setVisibility(v ? View.VISIBLE : View.GONE);
    }
    
    public View getRootLayout()
    {
        return layout_root;
    }
}

//todo room livedata