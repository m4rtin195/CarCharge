package com.martin.carcharge;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.math.MathUtils;
import com.google.android.material.snackbar.Snackbar;
import com.martin.carcharge.databinding.ActivityMainBinding;
import com.martin.carcharge.databinding.DialogEditBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.ui.home.HomeFragment;

import java.util.Objects;

public class MainActivity extends BaseActivity
{
    private SharedPreferences pref;
    private MainViewModel vm;
    
    NavController navController;
    
    ActivityMainBinding binding;
    View root;
    View scrim;
    FloatingActionButton fab_action;
    BottomAppBar bottombar;
    
    BottomSheetBehavior<FragmentContainerView> bottomSheetBehavior;
    LocalBroadcastManager lbm;
    
    Downloader downloader;
    BroadcastReceiver fcmReceiver;
    
    
    @SuppressLint("ClickableViewAccessibility")
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
            fab_action.setOnClickListener(onActionClickListener_flash);
    
        bottombar = binding.bottombar;
            setSupportActionBar(bottombar);
        
        bottomSheetBehavior = BottomSheetBehavior.from(binding.fragmentBottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        
        scrim = binding.scrim;
            scrim.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
        
        //todo ten normalny sposob?
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_navHost))).getNavController();
    
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        
        downloader = new Downloader(this);
        fcmReceiver = new FcmReceiver();
    
        //todo tu alebo v onStart?
        User user = (User) getIntent().getExtras().get(G.EXTRA_USER);
        vm.setUser(user);
        if(getIntent().getExtras().getBoolean(G.EXTRA_ISNEW, false))
            Snackbar.make(root, getString(R.string.welcome_back) + ", " + user.getNickname() + "!",
                    Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).show();
    
        vm.loadLastVehicle();
    } //onCreate
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navHost);
        assert navHostFragment != null;
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        assert fragment instanceof HomeFragment;
        ((HomeFragment) fragment).initState();
        
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
        if(item.getItemId() == android.R.id.home) //navigation icon
        {
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN)
            {
                setBottomSheetExpanded(true);
                setFabInternal(G.FAB_PLUS);
            }
            else
            {
                setBottomSheetExpanded(false);
                setFabInternal(G.FAB_FLASH);
            }
        }
        
        if(item.getItemId() == R.id.menu_history)
        {
            setBottomSheetExpanded(false);
            navController.navigate(R.id.navigation_action_home_to_history);
        }
        
        if(item.getItemId() == R.id.menu_refresh)
        {
            boolean b = downloader.download();
            if(b)
                showSnack(getString(R.string.toast_refreshed), Snackbar.LENGTH_SHORT);
            else
                showSnack(getString(R.string.toast_refresh_fail), Snackbar.LENGTH_SHORT);
        }
    
        return true;
    }
    
    /**********/
    
    
    public void updateStatus()
    {
        Log.i(G.tag, "updateStatus()");
        //Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), "Updating...", Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_action).setAction("Znova!!", null).show();
    }
    
    
    View.OnClickListener onActionClickListener_flash = view ->
    {
        Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
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
    
    void newVehicle()
    {
        View content = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.pairing_new_vehicle));
        builder.setView(content);
        builder.setPositiveButton(getString(R.string.pairing_pair), (dialog2, which) -> {});
        
        {
            AlertDialog newVehicleDialog = builder.create();
            newVehicleDialog.show();
            newVehicleDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            
            EditText edit_name = DialogEditBinding.bind(content).editName;
            Button dialogPButton = newVehicleDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            
            edit_name.addTextChangedListener(new TextWatcher()
            {
                @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override public void afterTextChanged(Editable editable) {dialogPButton.setEnabled(!editable.toString().isEmpty()); }
            });
            
            dialogPButton.setEnabled(false);
            dialogPButton.setOnClickListener(view ->
            {
                newVehicleDialog.dismiss();
                Vehicle newVehicle = vm.createVehicle(edit_name.getText().toString());
                //vehiclesAdapter.add(newVehicle); //todo
            });
        }
    }
    
    public void restartDownloader()
    {
        downloader.restart();
    }
    
    public void setBottomSheetExpanded(boolean b)
    {
        if(b)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        else
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    
    private void setFabInternal(int state)
    {
        if(state == G.FAB_FLASH)
        {
            bottombar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            fab_action.setImageResource(R.drawable.ic_flash);
            fab_action.setOnClickListener(onActionClickListener_flash);
        }
        if(state == G.FAB_PLUS)
        {
            bottombar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            fab_action.setImageResource(R.drawable.ic_plus);
            fab_action.setOnClickListener(v -> newVehicle());
        }
    }
    
    //todo merge?
    public void setFabVisible(boolean v)
    {
        fab_action.setVisibility(v ? View.VISIBLE : View.GONE);
    }
    public void setBottomBarVisible(boolean v)
    {
        bottombar.setVisibility(v ? View.VISIBLE : View.GONE);
    }
    
    public View getRootLayout()
    {
        return root;
    }
    
    public FloatingActionButton getFab()
    {
        return fab_action;
    }
    
    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback()
    {
        int previousState = BottomSheetBehavior.STATE_HIDDEN;
        
        @Override public void onStateChanged(@NonNull View bottomSheet, int newState)
        {
            if(newState == BottomSheetBehavior.STATE_SETTLING && previousState == BottomSheetBehavior.STATE_EXPANDED)
                setFabInternal(G.FAB_FLASH);
            if(newState == BottomSheetBehavior.STATE_SETTLING)
                scrim.setVisibility(View.VISIBLE);
            if(newState == BottomSheetBehavior.STATE_HIDDEN)
                scrim.setVisibility(View.INVISIBLE);
            
            if(newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_HIDDEN)
                previousState = newState;
        }
        @Override public void onSlide(@NonNull View bottomSheet, float slideOffset)
        {
            float baseAlpha = ResourcesCompat.getFloat(getResources(), R.dimen.material_emphasis_high_type);
            float offset = (slideOffset - (-1f)) / (1f - (-1f)) * (1f - 0f);
            int alpha = Math.round(MathUtils.lerp(0f, 255f, offset * baseAlpha));
            int color = Color.argb(alpha, 0, 0, 0);
            scrim.setBackgroundColor(color);
            MainActivity.super.setStatusBarColor(color);
        }
    };
    
    public void showSnack(String text, int duration)
    {
        Snackbar.make(root, text, duration).setAnchorView(R.id.fab_action).show();
    }
    
    @Override
    public void onBackPressed()
    {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
        {
            setBottomSheetExpanded(false);
            return;
        }
        super.onBackPressed();
    }
}

//todo room livedata