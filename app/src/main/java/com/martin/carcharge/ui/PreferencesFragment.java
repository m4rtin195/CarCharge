package com.martin.carcharge.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.martin.carcharge.AppActivity;
import com.martin.carcharge.BuildConfig;
import com.martin.carcharge.G;
import com.martin.carcharge.LoginActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.Vehicle;

public class PreferencesFragment extends PreferenceFragmentCompat
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    private FirebaseAuth auth;
    
    Toolbar toolbar;
    Preference preference_language, preference_version, preference_fcmToken, preference_contactDeveloper;
    Preference preference_nickname, preference_logout,
            preference_vehicleName, preference_vehicleRegplate, preference_vehicleCapacity, preference_vehicleImage;
    
    int eggCounter = 0;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        
        db = AppActivity.getDatabase();
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        auth = FirebaseAuth.getInstance();
        
        toolbar = view.findViewById(R.id.toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.background, requireActivity().getTheme()));
            toolbar.setTitle(getString(R.string.app_preferences));
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
            
        preference_language = findPreference("language");
            preference_language.setOnPreferenceChangeListener((preference, newValue) ->
            {
                requireActivity().recreate();
                return true;
            });
            
        preference_version = findPreference("version");
            preference_version.setSummary(BuildConfig.VERSION_NAME);
            preference_version.setOnPreferenceClickListener(easterEgg);
    
        preference_fcmToken = findPreference("fcm_token");
            preference_fcmToken.setSummary(pref.getString("fcm_token", "unloaded"));
            
        preference_contactDeveloper = findPreference("contact_developer");
            preference_contactDeveloper.setOnPreferenceClickListener(contactDeveloper);
            
        preference_nickname = findPreference("nickname");
            preference_nickname.setTitle(pref.getString("nickname", getString(R.string.preferences_nickname))); //todo load odkial
            preference_nickname.setSummary("martin.timko195@gmail.com");
            preference_nickname.setOnPreferenceChangeListener((preference, newValue) ->
            {
                if(newValue.toString().isEmpty())
                {
                    pref.edit().remove("nickname").apply();
                    preference_nickname.setTitle(getString(R.string.preferences_nickname));
                }
                else
                {
                    pref.edit().putString("nickname", newValue.toString()).apply();
                    preference_nickname.setTitle(newValue.toString());
                }
                return true;
            });
            
        preference_logout = findPreference("logout");
            preference_logout.setOnPreferenceClickListener(this::onLogoutClick);
            
        preference_vehicleName = findPreference("vehicle_name");
            preference_vehicleName.setOnPreferenceChangeListener(vehicleChanged);
    
        preference_vehicleRegplate = findPreference("vehicle_regplate");
            preference_vehicleRegplate.setOnPreferenceChangeListener(vehicleChanged);
            
        preference_vehicleCapacity = findPreference("vehicle_capacity");
            preference_vehicleCapacity.setOnPreferenceChangeListener(vehicleChanged);
            
        preference_vehicleImage = findPreference("vehicle_image");
            preference_vehicleImage.setOnPreferenceClickListener(null); //todo implement
        
            
        return view;
    }
    
    private boolean onLogoutClick(Preference preference)
    {
        auth.signOut();
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                //Log.i(G.tag, "user: " + firebaseAuth.getCurrentUser().toString());
                Intent intent = new Intent(PreferencesFragment.this.requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PreferencesFragment.this.startActivity(intent);
    
                try
                {
                    Thread.sleep(100);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.exit(0);
                //Runtime.getRuntime().exit(0);
            }
        });
        
        /*Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish(); // if the activity running has it's own context
        requireContext().finish();*/
        
        return true;
    }
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
    
    Preference.OnPreferenceChangeListener vehicleChanged = (preference, newValue) ->
    {
        Vehicle vehicle = vm.Vehicle().getValue();
        if(preference.equals(preference_vehicleName))
            vehicle.setName(newValue.toString());
        if(preference.equals(preference_vehicleRegplate))
            vehicle.setRegNumber(newValue.toString());
        if(preference.equals(preference_vehicleCapacity))
            vehicle.setBatteryCapacity(Integer.parseInt(newValue.toString())); //todo check format
            
        vm.Vehicle().postValue(vehicle);
        db.dao().updateVehicle(vehicle);
        return true;
    };
    
    Preference.OnPreferenceClickListener contactDeveloper = preference ->
    {
        String body =
                "\n\n-----------------------------" +
                "\nPlease don't remove this information" +
                "\n App Version: " + BuildConfig.VERSION_NAME +
                "\n Device OS version: " + Build.VERSION.RELEASE +
                "\n Device Brand: " + Build.BRAND +
                "\n Device Model: " + Build.MODEL;
        
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"martin.timko195@gmail.com", "timko10@stud.uniza.sk"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Query from CarCharge app");
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.preferences_choose_email_client)));
        
        return true;
    };
    
    Preference.OnPreferenceClickListener easterEgg = preference ->
    {
        eggCounter++;
        if(eggCounter >= 5)
        {
            Toast.makeText(requireContext(), getString(R.string.preferences_easter_egg), Toast.LENGTH_LONG).show();
            eggCounter = Integer.MIN_VALUE;
        }
        return true;
    };
    
}