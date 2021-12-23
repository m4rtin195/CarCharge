package com.martin.carcharge;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.martin.carcharge.databinding.DialogEdittextBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.models.VehicleStatus;
import com.martin.carcharge.network.Downloader;

import java.util.Objects;

import retrofit2.Response;

public class MainActivity extends BaseActivity
{
    private SharedPreferences pref;
    private MainViewModel vm;
    
    NavController navController;
    
    ActivityMainBinding binding;
    View root;
    View scrim;
    FloatingActionButton fab_button;
    BottomAppBar bottomBar;
    
    BottomSheetBehavior<FragmentContainerView> bottomSheetBehavior;
    LocalBroadcastManager lbm;
    
    Downloader downloader;
    BroadcastReceiver fcmReceiver;
    
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(G.tag, "MainActivity onCreate()");
        super.onCreate(savedInstanceState);
        this.setupUI();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        root = binding.getRoot();
        setContentView(root);
        
        pref = App.getPreferences();
        vm = App.getViewModel();
        
        fab_button = binding.fabButton;
            fab_button.setOnClickListener(onActionClickListener_flash);
    
        bottomBar = binding.bottombar;
            setSupportActionBar(bottomBar);
        
        bottomSheetBehavior = BottomSheetBehavior.from(binding.fragmentBottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        
        scrim = binding.scrim;
            scrim.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
        
        //bug v Navigation 2.3.x - musi sa obchadzat cez supportFragmentManager - pravdepodobne pernament
        //https://stackoverflow.com/questions/58703451/fragmentcontainerview-as-navhostfragment
        //navController = Navigation.findNavController(root);
        navController = ((NavHostFragment)Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_navHost))).getNavController();
        
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        
        downloader = new Downloader(this);
        fcmReceiver = new FcmReceiver();
        
        
        // resolve intent extras
        if(getIntent().hasExtra(G.EXTRA_USER))
        {
            User user = (User)getIntent().getExtras().get(G.EXTRA_USER);
            vm.setUser(user);
            if(getIntent().getExtras().getBoolean(G.EXTRA_USER_JUST_LOGGEDIN, false)) // user just logged-in
            {
                Log.i(G.tag, "(new user)");
                Snackbar.make(root, getString(R.string.welcome_back) + ", " + user.getNickname() + "!", Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_button).show();
                App.getApiClient().registerFcm(vm.getAllVehicles())
                        .thenRun(() -> Log.i(G.tag, "FCM-vehicle registrations completed.")); //register all vehicles to fcm token
            }
        }
        if(getIntent().hasExtra(G.EXTRA_VEHICLEID))
        {
            Vehicle v = vm.getVehicleFromRepo(getIntent().getStringExtra(G.EXTRA_VEHICLEID));
            if(v != null)
                vm.switchCurrentVehicle(v);
        }
        
        
        // current vehicle not set
        if(vm.currentVehicle().getValue() == null)
        {
            if(!vm.getAllVehicles().isEmpty())
                vm.switchCurrentVehicle(vm.getAllVehicles().get(0));
            else
                vm.switchCurrentVehicle(vm.createVehicle("New vehicle"));
        }
        
        
        //FOR DEMO ONLY
        //loading po spusteni app, onStart je volane aj po prepnuti do popredia???
        //vm.updateVehicleStatus(new VehicleStatus(vm.getCurrentVehicle(), VehicleStatus.State.Loading));
        
        //delay this by 1s
        new Handler(Looper.getMainLooper()).postDelayed(() ->
        {
            //vm.updateVehicleStatus(vm.requireActualVehicleStatus(vm.getCurrentVehicle()));      //load last status //todo prec
            
            /*if(!pref.getBoolean(G.PREF_FCM_ENABLED, false))                       // FCM disabled, start downloader
            {
                if(!downloader.start(vm.getCurrentVehicle(), autoNewDataListener))           // if downloader not started (interval 0)
                    downloader.downloadLast(vm.getCurrentVehicle(), manualNewDataListener);  // download once
            }
            else
                downloader.downloadLast(vm.getCurrentVehicle(), manualNewDataListener);      // FCM is enabled, download once*/
        }, 1000);
        
    } //onCreate
    
    @Override
    public void onStart()
    {
        Log.i("daco", "MainActivity onStart()");
        super.onStart();
        lbm.registerReceiver(fcmReceiver, new IntentFilter(G.ACTION_BROADCAST_FCMUPDATE));
    }
    
    @Override
    public void onResume()
    {
        Log.i("daco", "MainActivity onResume()");
        super.onResume();
    }
    
    @Override
    protected void onPause()
    {
        Log.i("daco", "MainActivity onPause()");
        super.onPause();
        //lbm.unregisterReceiver(fcmReceiver); //todo overit ci treba
    }
    
    @Override
    protected void onStop()
    {
        Log.i("daco", "MainActivity onStop()");
        super.onStop();
    }
    
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
                _setFabFunction(G.FAB_PLUS);
            }
            else
            {
                setBottomSheetExpanded(false);
                _setFabFunction(G.FAB_FLASH);
            }
        }
        
        if(item.getItemId() == R.id.menu_history)
        {
            setBottomSheetExpanded(false);
            navController.navigate(R.id.navigation_action_to_history);
        }
        
        if(item.getItemId() == R.id.menu_refresh)
        {
            downloader.downloadLast(vm.getCurrentVehicle(), manualNewDataListener);
        }
    
        return true;
    }
    
    /**********/
    
    public Downloader getDownloader()
    {
        return downloader;
    }
    
    public void setBottomSheetExpanded(boolean b)
    {
        if(b)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        else
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    
    public void setBottomBarVisible(boolean bar, boolean fab)
    {
        if(bar) bottomBar.performShow();
        else bottomBar.performHide();
        bottomBar.setHideOnScroll(bar);
        fab_button.setVisibility(fab ? View.VISIBLE : View.GONE);
    }
    
    /*public View getRootLayout()
    {
        return root;
    }*/
    
    public FloatingActionButton getFab()
    {
        return fab_button;
    }
    
    /*public void showSnack(String text, int duration)
    {
        Snackbar.make(root, text, duration).setAnchorView(R.id.fab_button).show();
    }*/
    
    @Override
    public void onBackPressed()
    {
        //just hide bottomSheet
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
        {
            setBottomSheetExpanded(false);
            return;
        }
        super.onBackPressed();
    }
    
    private void _setFabFunction(int mode)
    {
        if(mode == G.FAB_FLASH)
        {
            bottomBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            fab_button.setImageResource(R.drawable.ic_flash);
            fab_button.setOnClickListener(onActionClickListener_flash);
        }
        if(mode == G.FAB_PLUS)
        {
            bottomBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            fab_button.setImageResource(R.drawable.ic_plus);
            fab_button.setOnClickListener(onNewVehicleClickListener);
        }
    }
    
    /**********/
    
        //Download listener for automatic refresh requests  //quiet one
    public Downloader.Listener autoNewDataListener = new Downloader.Listener()
    {
        @Override
        public void onSuccess(@NonNull VehicleStatus vs)
        {
            if(vs.getVehicleId().equals(vm.getCurrentVehicle().getId()))
                vm.updateVehicleStatus(vs);
            else
            {
                Log.w(G.tag, "Received status is not for current selected vehicle!");
                onFail(null);
            }
        }
        @Override
        public void onFail(Response<?> response)
        {
            G.debug(getApplication(), getString(R.string.toast_refresh_fail), true);
        }
    };
    
    //Download listener for manual refresh requests  //talky one
    public Downloader.Listener manualNewDataListener = new Downloader.Listener()
    {
        @Override
        public void onSuccess(@NonNull VehicleStatus vs)
        {
            if(vs.getVehicleId().equals(vm.getCurrentVehicle().getId()))
            {
                vm.updateVehicleStatus(vs);
                Snackbar.make(root, getString(R.string.toast_refreshed), Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_button).show();
            }
            else
            {
                Log.w(G.tag, "Received status is not for currently selected vehicle!");
                onFail(null);
            }
        }
        @Override
        public void onFail(Response<?> response)
        {
            //prvotne nacitavanie pri spusteni aplikacie
            if(vm.getCurrentVehicleStatus() != null && vm.getCurrentVehicleStatus().getState() == VehicleStatus.State.Loading)
                vm.updateVehicleStatus(new VehicleStatus(vm.getCurrentVehicle(), VehicleStatus.State.Unknown));
            
            Snackbar.make(root, getString(R.string.toast_refresh_fail), Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab_button).show();
        }
    };
    
    View.OnClickListener onActionClickListener_flash = (View view) ->
    {
        Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
    };
    
    OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (sharedPreferences, key) ->
    {
        if(key.equals(G.PREF_UPDATE_INTERVAL))
            downloader.restart();
        
        if(key.equals(G.PREF_FCM_ENABLED))
        {
            Toast.makeText(this, "fcm enabled change", Toast.LENGTH_SHORT).show(); //todo prec
            if(pref.getBoolean(G.PREF_FCM_ENABLED, false))
                lbm.registerReceiver(fcmReceiver, new IntentFilter(G.ACTION_BROADCAST_FCMUPDATE));
            else
                lbm.unregisterReceiver(fcmReceiver);
        }
    };
    
    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback()
    {
        int previousState = BottomSheetBehavior.STATE_HIDDEN;
        
        @Override public void onStateChanged(@NonNull View bottomSheet, int newState)
        {
            if(newState == BottomSheetBehavior.STATE_SETTLING && previousState == BottomSheetBehavior.STATE_EXPANDED)
                _setFabFunction(G.FAB_FLASH);
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
            float offset = (slideOffset - (-1f)) / (1f - (-1f));
            int alpha = Math.round(MathUtils.lerp(0f, 255f, offset * baseAlpha));
            int color = Color.argb(alpha, 0, 0, 0);
            scrim.setBackgroundColor(color);
            MainActivity.super.setStatusBarColor(color);
        }
    };
    
    View.OnClickListener onNewVehicleClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Toast.makeText(getApplicationContext(), "This is NOT implemented!", Toast.LENGTH_LONG).show();
            View content = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.pairing_new_vehicle));
            builder.setView(content);
            builder.setPositiveButton(getString(R.string.pairing_pair), (dialog2, which) -> {});
            
            {
                AlertDialog newVehicleDialog = builder.create();
                newVehicleDialog.show();
                newVehicleDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                
                EditText edit_name = DialogEdittextBinding.bind(content).editName;
                Button dialogPButton = newVehicleDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                
                edit_name.addTextChangedListener(new TextWatcher()
                {
                    @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override public void afterTextChanged(Editable editable) {dialogPButton.setEnabled(!editable.toString().isEmpty()); }
                });
                
                dialogPButton.setEnabled(false);
                dialogPButton.setOnClickListener(view1 ->
                {
                    newVehicleDialog.dismiss();
                    Vehicle newVehicle = vm.createVehicle(edit_name.getText().toString());
                    //vehiclesAdapter.add(newVehicle); //todo
                });
            }
        }
    };
}

//todo room livedata