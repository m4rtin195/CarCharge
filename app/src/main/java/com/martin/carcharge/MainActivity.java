package com.martin.carcharge;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.databinding.ActivityMainBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.ui.BottomSheetFragment;
import com.martin.carcharge.ui.home.HomeFragment;

import java.util.Objects;

public class MainActivity extends BaseActivity
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    private FirebaseAuth auth;
    
    NavController navController;
    
    ActivityMainBinding binding;
    View root;
    FloatingActionButton fab_action;
    BottomAppBar bottomAppBar;
    
    Downloader downloader;
    BroadcastReceiver fcmReceiver;
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setupUI();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        root = binding.getRoot();
        setContentView(root);
        
        db = AppActivity.getDatabase(); //todo to appactivity ako static
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        vm = new ViewModelProvider(this).get(MainViewModel.class);
        auth = FirebaseAuth.getInstance();
        
        fab_action = binding.fabAction;
            fab_action.setOnClickListener(onActionClickListener);
        bottomAppBar = binding.bottomAppBar;
        setSupportActionBar(bottomAppBar);
        
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_nav_host))).getNavController();
        
        pref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        
        downloader = new Downloader(this);
        fcmReceiver = new FcmReceiver(this); //todo isto netreba pretypovat???
        
    } //onCreate
    
    @Override
    public void onStart() //todo presun sem veci
    {
        super.onStart();
        
        vm.loadLastVehicle();
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_host);
        assert navHostFragment != null;
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        assert fragment instanceof HomeFragment;
        ((HomeFragment)fragment).initState();
        
        try {Thread.sleep(1000);} catch(InterruptedException e) {e.printStackTrace();}
        //todo load last status
        
        if(!pref.getBoolean("fcm_enabled", false))
            downloader.start();
    
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(fcmReceiver, new IntentFilter(G.ACTION_BROAD_UPDATE));
    }
    
    /*@Override
    protected void onPause()
    {
        super.onPause();
        //lbm.unregisterReceiver(fcmReceiver); //todo treba?? dokedy zostane zaregistrovany?
    }*/
    
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_appbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            bottomSheetFragment.show(getSupportFragmentManager(), G.BOTTOM_DRAWER_TAG);
        }
        if(item.getItemId() == R.id.menu_history)
            navController.navigate(R.id.navigation_action_home_to_history);
        if(item.getItemId() == R.id.menu_refresh)
            downloader.download();
            
        return true;
    }
    
    /**********/
    
    
    public void updateStatus()
    {
        Log.i(G.tag, "updateStatus()");
        //Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), "Updating...", Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).setAction("Znova!!", null).show();
    }
    
    
    View.OnClickListener onActionClickListener = view ->
    {
        downloader.download();
    };
    
    OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (sharedPreferences, key) ->
    {
        Log.i("daco","onSharedPreferenceChangeListener()");
        if(key.equals("update_interval"))
            downloader.restart();
    };
    
    public void restartDownloader()
    {
        downloader.restart();
    }
    
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
        return root;
    }
}

//todo room livedata
//todo pref tagy do G