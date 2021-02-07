package com.martin.carcharge.ui.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.martin.carcharge.App;
import com.martin.carcharge.BuildConfig;
import com.martin.carcharge.G;
import com.martin.carcharge.LoginActivity;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.databinding.FragmentPreferencesBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.Vehicle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class PreferencesFragment extends PreferenceFragmentCompat
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    private FirebaseAuth auth;
    
    FragmentPreferencesBinding binding;
    View root;
    Toolbar toolbar;
    ProgressBar progressbar;
    
    Preference preference_language, preference_fcmEnabled, preference_updateInterval, preference_invalidateCache,
            preference_version, preference_fcmToken, preference_contactDeveloper;
    Preference preference_user, preference_logout,
            preference_vehicleName, preference_vehicleRegplate, preference_vehicleCapacity, preference_vehicleImage;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentPreferencesBinding.bind(root);
        
        db = App.getDatabase();
        pref = App.getPreferences();
        vm = App.getViewModel();
        auth = FirebaseAuth.getInstance();
        
        toolbar = binding.toolbarPreferences;
            toolbar.setBackgroundColor(Color.TRANSPARENT);
            toolbar.setTitle(getString(R.string.app_preferences));
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
    
        progressbar = binding.progressPreferences;
    
        //((MainActivity)requireActivity()).setFabVisible(false);
        //((MainActivity)requireActivity()).setBottomBarVisible(false);
    
    
        /***/
        
        preference_language = findPreference(G.PREF_LANGUAGE);
            preference_language.setOnPreferenceChangeListener((preference, newValue) ->
            {
                requireActivity().recreate();
                return true;
            });
    
        preference_fcmEnabled = findPreference(G.PREF_FCM_ENABLED);
            preference_fcmEnabled.setOnPreferenceChangeListener((preference, newValue) ->
            {
                preference_updateInterval.setVisible(!(boolean)newValue);
                return true;
            });
    
        preference_updateInterval = findPreference(G.PREF_FCM_ENABLED);
            preference_updateInterval.setVisible(!pref.getBoolean(G.PREF_FCM_ENABLED, false));
            preference_updateInterval.setOnPreferenceChangeListener((preference, newValue) ->
            {
                ((MainActivity)requireActivity()).restartDownloader();
                return true;
            });
        
        preference_invalidateCache = findPreference("invalidate_cache");
            preference_invalidateCache.setOnPreferenceClickListener(invalidateCache);
            
        preference_version = findPreference("version");
            preference_version.setSummary(BuildConfig.VERSION_NAME);
            preference_version.setOnPreferenceClickListener(easterEgg);
    
        preference_fcmToken = findPreference(G.PREF_FCM_TOKEN);
            preference_fcmToken.setSummary(pref.getString(G.PREF_FCM_TOKEN, getString(R.string.preferences_unloaded)));
            
        preference_contactDeveloper = findPreference("contact_developer");
            preference_contactDeveloper.setOnPreferenceClickListener(contactDeveloper);
    
        /***/
        
        preference_user = findPreference(G.PREF_USER_NICKNAME);
            String title = (pref.getString(G.PREF_USER_NICKNAME, "").isEmpty() ?
                    getString(R.string.preferences_nickname) : pref.getString(G.PREF_USER_NICKNAME, ""));
            preference_user.setTitle(title);
            preference_user.setSummary(pref.getString(G.PREF_USER_EMAIL, ""));
            preference_user.setIcon(((MainActivity)requireActivity()).getUserIcon(pref.getString(G.PREF_USER_ICON, "")));
            preference_user.setOnPreferenceChangeListener(nicknameChanged);
            ((EditTextPreference)preference_user).setOnBindEditTextListener(TextView::setSingleLine);
            
        preference_logout = findPreference("logout");
            preference_logout.setOnPreferenceClickListener(this::onLogoutClick);
            
        preference_vehicleName = findPreference(G.PREF_VEHICLE_NAME);
            preference_vehicleName.setOnPreferenceChangeListener(vehicleModified);
            ((EditTextPreference)preference_vehicleName).setOnBindEditTextListener(TextView::setSingleLine);
    
        preference_vehicleRegplate = findPreference(G.PREF_VEHICLE_REGPLATE);
            preference_vehicleRegplate.setOnPreferenceChangeListener(vehicleModified);
            ((EditTextPreference)preference_vehicleRegplate).setOnBindEditTextListener(edit_regplate ->
            {
                edit_regplate.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
                edit_regplate.setSingleLine();
            });
    
        preference_vehicleCapacity = findPreference(G.PREF_VEHICLE_CAPACITY);
            preference_vehicleCapacity.setOnPreferenceChangeListener(vehicleModified);
            
        preference_vehicleImage = findPreference(G.PREF_VEHICLE_IMAGE);
            String summary = pref.getString(G.PREF_VEHICLE_IMAGE, "").isEmpty() ?
                    getString(R.string.preferences_not_set) : getString(R.string.preferences_set);
            preference_vehicleImage.setSummary(summary);
            preference_vehicleImage.setOnPreferenceClickListener(this::onVehicleImageClick);
        
        /***/
        
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    public boolean onVehicleImageClick(Preference preference)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, G.RC_FILE_PICKER);
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(requestCode == G.RC_FILE_PICKER)
        {
            if(resultCode == RESULT_OK)
            {
                Uri uri = intent.getData();
                try(InputStream is = requireActivity().getContentResolver().openInputStream(uri);)
                {
                    File path = new File(requireActivity().getFilesDir().toString() + "/media");
                    if(!path.exists())
                        if(!path.mkdir())
                            throw new IOException();
                        
                    File file = File.createTempFile("vehicleimage_", ".aaa", path);
                    Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    pref.edit().putString(G.PREF_VEHICLE_IMAGE, file.getName()).apply();
                }
                catch(IOException e) {e.printStackTrace();}
            }
    
            vehicleModified.onPreferenceChange(preference_vehicleImage, pref.getString(G.PREF_VEHICLE_IMAGE, ""));
            preference_vehicleImage.setSummary(getString(R.string.preferences_set));
        }
    }
    
    private boolean onLogoutClick(Preference preference)
    {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(requireContext());
        confirmDialog.setTitle(getString(R.string.preferences_logout_dialog));
        confirmDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> logout());
        confirmDialog.setNegativeButton(getString(R.string.no), null);
        confirmDialog.show();
        
        return true;
    }
    
    private void logout() //todo move all to vm
    {
        //remove user icon and vehicles files
        deleteFiles();

        //remove shared-pref
        pref.edit()
                .remove(G.PREF_LAST_VEHICLE_ID)
                .remove(G.PREF_USER_NICKNAME).remove(G.PREF_USER_EMAIL).remove(G.PREF_USER_ICON)
                .remove(G.PREF_VEHICLE_NAME).remove(G.PREF_VEHICLE_REGPLATE).remove(G.PREF_VEHICLE_CAPACITY).remove(G.PREF_VEHICLE_IMAGE)
            .apply();
        
        //remove database tables
        db.dao().deleteAllVehicles();
        db.dao().deleteAllStatuses();
        
        //sign-out from google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignIn.getClient(requireContext(), gso).signOut().addOnCompleteListener(task ->
        {
            //sign-out from firebase
            auth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
            //pockat na dokoncenie IO na persistent
            try {Thread.sleep(100);} catch(InterruptedException e) {e.printStackTrace();}
            Runtime.getRuntime().exit(0);
        });
    }
    
    private void deleteFiles()
    {
        if(!pref.getString(G.PREF_USER_ICON, "").isEmpty())
        {
            File file = new File(requireActivity().getFilesDir().toString() + "/media/" + pref.getString(G.PREF_USER_ICON, ""));
            if(file.exists())
                file.delete();
        }
    
        List<Vehicle> vehicles = db.dao().getAllVehicles(); //todo arraylist?
        for(Vehicle vehicle : vehicles)
        {
            File file = new File(requireActivity().getFilesDir().toString() + "/media/" + vehicle.getImageFile());
            if(file.exists())
                file.delete();
        }
    }
    
    OnPreferenceChangeListener nicknameChanged = (preference, newValue) ->
    {
        progressbar.setVisibility(View.VISIBLE);
        FirebaseUser user = Objects.requireNonNull(auth.getCurrentUser());
        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newValue.toString()).build())
                .addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        String title = (newValue.toString().isEmpty() ?
                                getString(R.string.preferences_nickname) : newValue.toString());
                        preference_user.setTitle(title);
                        ((EditTextPreference) preference_user).setText(newValue.toString()); //interne ulozi aj do shared
                        Snackbar.make(root, getString(R.string.preferences_nickname_update_success), Snackbar.LENGTH_SHORT).show();
                    }
                    else
                        Snackbar.make(root, getString(R.string.preferences_nickname_update_failed), Snackbar.LENGTH_LONG).show();
                    
                    progressbar.setVisibility(View.INVISIBLE);
                });
        
        return false;
    };
    
    OnPreferenceChangeListener vehicleModified = (preference, newValue) ->
    {
        Vehicle vehicle = vm.vehicle().getValue();
        if(vehicle != null)
        {
            if(preference.equals(preference_vehicleName))
                vehicle.setName((String)newValue);
            if(preference.equals(preference_vehicleRegplate))
                vehicle.setRegNumber((String)newValue);
            if(preference.equals(preference_vehicleCapacity))
                vehicle.setBatteryCapacity(Integer.parseInt((String)newValue)); //todo check format}
            if(preference.equals(preference_vehicleImage))
                vehicle.setImageFile((String)newValue);
        }
        
        vm.postVehicle(vehicle);
        return true;
    };
    
    OnPreferenceClickListener invalidateCache = preference ->
    {
        db.dao().deleteAllStatuses(); //todo to viewmodel
        Snackbar.make(root, getString(R.string.preferences_invalidate_cache_dialog), Snackbar.LENGTH_SHORT).show();
        return true;
    };
    
    OnPreferenceClickListener contactDeveloper = preference ->
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
    
    int eggCounter = 0;
    OnPreferenceClickListener easterEgg = preference ->
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