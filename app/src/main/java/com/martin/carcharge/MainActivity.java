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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martin.carcharge.databinding.ActivityMainBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.ui.BottomSheetFragment;
import com.martin.carcharge.ui.home.HomeFragment;

import java.util.Objects;

public class MainActivity extends BaseActivity
{
    private SharedPreferences pref;
    private MainViewModel vm;
    
    NavController navController;
    
    ActivityMainBinding binding;
    View root;
    FloatingActionButton fab_action;
    BottomAppBar bottomAppBar;
    
    LocalBroadcastManager lbm;
    
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
        
        pref = App.getPreferences();
        vm = App.getViewModel();
        
        fab_action = binding.fabAction;
            fab_action.setOnClickListener(onActionClickListener);
            
        bottomAppBar = binding.bottomAppBar;
            setSupportActionBar(bottomAppBar);
        
        //todo ten normalny sposob?
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_nav_host))).getNavController();
    
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        
        downloader = new Downloader(this);
        fcmReceiver = new FcmReceiver();
    
        //todo tu alebo v onStart?
        User user = (User) getIntent().getExtras().get(G.EXTRA_USER);
        vm.postUser(user);
        if(getIntent().getExtras().getBoolean(G.EXTRA_ISNEW, false))
            Snackbar.make(root, getString(R.string.toast_welcome_back) + ", " + user.getNickname() + "!",
                    Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).show();
    
        vm.loadLastVehicle();
        
    } //onCreate
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_host);
        assert navHostFragment != null;
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        assert fragment instanceof HomeFragment;
        ((HomeFragment)fragment).initState();
        
        //try {Thread.sleep(1000);} catch(InterruptedException e) {e.printStackTrace();}
        //load last status
        
        if(!pref.getBoolean(G.PREF_FCM_ENABLED, false))
            downloader.start();
        
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
        if(key.equals(G.PREF_FCM_ENABLED))
            downloader.restart();
        
        if(key.equals(G.PREF_FCM_ENABLED))
        {
            Toast.makeText(this, "daco", Toast.LENGTH_SHORT).show(); //todo prec
            if(pref.getBoolean(G.PREF_FCM_ENABLED, false))
                lbm.registerReceiver(fcmReceiver, new IntentFilter(G.ACTION_BROAD_UPDATE));
            else
                lbm.unregisterReceiver(fcmReceiver);
        }
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