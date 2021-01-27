package com.martin.carcharge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.Vehicle;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    
    NavController navController;
    
    CoordinatorLayout layout_root;
    FloatingActionButton fab_action;
    BottomAppBar bottomAppBar;
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CarCharge);
        getWindow().setStatusBarColor(getResources().getColor(R.color.google_background, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.google_background, getTheme()));
        getWindow().setNavigationBarDividerColor(getResources().getColor(R.color.tile_gray, getTheme()));
        setContentView(R.layout.activity_main);
    
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        //decorView.setSystemUiVisibility(uiOptions);
        getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars());
    
        db = AppActivity.getDatabase();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        vm = new ViewModelProvider(this).get(MainViewModel.class);
        
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
    
        updateStatus();
        return true;
    }
    
    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(newBase);
        pref = PreferenceManager.getDefaultSharedPreferences(this); //bezi pred onCreate
        if(!pref.getString("language", "system").equals("system"))
            applyOverrideConfiguration(updateConfigurationLanguage(new Configuration()));
    }
    
    private Configuration updateConfigurationLanguage(@NotNull Configuration config)
    {
        if(!config.getLocales().isEmpty()) return config;
        
        String languageStr = pref.getString("language", "system");
        Locale newLocale = stringToLocale(languageStr);
        if(newLocale != null)
            config.setLocale(newLocale);
        return config;
    }
    
    private Locale stringToLocale(String s)
    {
        StringTokenizer tempStringTokenizer = new StringTokenizer(s,"_");
        String language = new String();
        String country = new String();
        if(tempStringTokenizer.hasMoreTokens())
            language = (String) tempStringTokenizer.nextElement();
        if(tempStringTokenizer.hasMoreTokens())
            country = (String) tempStringTokenizer.nextElement();
        return new Locale(language, country);
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
            Log.i("daco","dlt");
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
    
    void updateStatus()
    {
        //Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), "Updating...", Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).setAction("Znova!!", null).show();
    }
    
    public void setFabVisible(boolean v)
    {
        fab_action.setVisibility(v ? View.VISIBLE : View.GONE);
    }
    
    public View getRootLayout()
    {
        return layout_root;
    }
}

//todo room livedata