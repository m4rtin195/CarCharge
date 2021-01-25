package com.martin.carcharge;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.Vehicle;

import java.util.Objects;


public class MainActivity extends AppCompatActivity
{
    private AppDatabase db = AppActivity.getDatabase();
    private SharedPreferences settings;
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
        getWindow().setNavigationBarColor(getResources().getColor(R.color.google_background, getTheme()));
        getWindow().setNavigationBarDividerColor(getResources().getColor(R.color.tile_gray, getTheme()));
        setContentView(R.layout.activity_main);
    
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        //decorView.setSystemUiVisibility(uiOptions);
        getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars());
    
        settings = PreferenceManager.getDefaultSharedPreferences(this);
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
        
        updateVehicle();
        //updateStatus();
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
    
        updateStatus();
        return true;
    }
    
    void updateVehicle()
    {
        long lastVehicleId = settings.getLong("last_vehicle_id", 0);
        if(lastVehicleId == 0)
        {
            Vehicle temp = new Vehicle();
            lastVehicleId = db.dao().insert(temp);
            settings.edit().putLong("last_vehicle_id", lastVehicleId).apply();
        }
    
        Vehicle vh = new Vehicle();
        vh.setName("Volkswagen ID.4");
        vh.setRegNumber("PO-111AA");
//        vh.setName("Tesla Model 3");
//        vh.setRegNumber("ZA-987MT");
        //vm.setVehicle(new MutableLiveData<>(vh));
        //vm.setVehicle(new MutableLiveData<>(db.dao().getVehicle(lastVehicleId)));
//        VehicleStatus vs = new VehicleStatus();
//        vs.current = 322;
//        //vs.print();
//        vm.setVehicleStatus(new MutableLiveData<>(vs));
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