package com.martin.carcharge.ui.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
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
import com.martin.carcharge.models.User;
import com.martin.carcharge.storage.AppDatabase;
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
            preference_vehicleName, preference_vehicleRegplate, preference_vehicleMaxVoltage, preference_vehicleImage;
    
    User user;
    Vehicle vehicle;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ((MainActivity)requireActivity()).setBottomBarVisible(false, false);
        
        root = super.onCreateView(inflater, container, savedInstanceState);
        assert root != null;
        binding = FragmentPreferencesBinding.bind(root);
        
        pref = App.getPreferences();
        vm = App.getViewModel();
        auth = FirebaseAuth.getInstance();
        
        user = vm.getUser();
        vehicle = vm.getActualVehicle();
        
        toolbar = binding.toolbarPreferences;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
        progressbar = binding.progressPreferences;
        
        configPreferences();
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    @SuppressWarnings("ConstantConditions")
    private void configPreferences()
    {
        /***/
        //General & About
    
        preference_language = findPreference(G.PREF_LANGUAGE);
            preference_language.setOnPreferenceChangeListener((preference, newValue) ->
            {
                requireActivity().recreate(); //todo preco nie
                    /*Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();*/
            
                return true;
            });
        
        preference_fcmEnabled = findPreference(G.PREF_FCM_ENABLED);
            preference_fcmEnabled.setOnPreferenceChangeListener((preference, newValue) ->
            {
                preference_updateInterval.setVisible(!(boolean)newValue);
                return true;
            });
    
        preference_updateInterval = findPreference(G.PREF_UPDATE_INTERVAL);
            preference_updateInterval.setVisible(!pref.getBoolean(G.PREF_FCM_ENABLED, false));
            preference_updateInterval.setOnPreferenceChangeListener((preference, newValue) ->
            {
                ((MainActivity)requireActivity()).getDownloader().restart();
                return true;
            });
    
        preference_invalidateCache = findPreference(G.PREF_INVALIDATE_CACHE);
            preference_invalidateCache.setOnPreferenceClickListener(onInvalidateCache);
    
        preference_version = findPreference(G.PREF_APP_VERSION);
            preference_version.setSummary(BuildConfig.VERSION_NAME);
            preference_version.setOnPreferenceClickListener(easterEgg);
    
        preference_fcmToken = findPreference(G.PREF_FCM_TOKEN);
            preference_fcmToken.setSummary(pref.getString(G.PREF_FCM_TOKEN, getString(R.string.preferences_unloaded)));
    
        preference_contactDeveloper = findPreference(G.PREF_CONTACT_DEVELOPER);
            preference_contactDeveloper.setOnPreferenceClickListener(onContactDeveloper);
    
            
        /***/
        //User & Vehicle
    
        preference_user = findPreference(G.PREF_USER_NICKNAME);
            String nickname = user.getNickname().isEmpty() ?
                    getString(R.string.preferences_nickname) : user.getNickname();
            preference_user.setTitle(nickname);
            preference_user.setSummary(user.getEmail());
            preference_user.setIcon(new BitmapDrawable(getResources(), user.getIcon()));
            preference_user.setOnPreferenceChangeListener(onNicknameChanged);
            ((EditTextPreference)preference_user).setOnBindEditTextListener(TextView::setSingleLine);
    
        preference_logout = findPreference(G.PREF_LOGOUT);
            preference_logout.setOnPreferenceClickListener(onLogout);
    
        preference_vehicleName = findPreference(G.PREF_VEHICLE_NAME);
            //preference_vehicleName.setSummary(vehicle.getName());
            preference_vehicleName.setOnPreferenceChangeListener(onVehicleModified);
            ((EditTextPreference)preference_vehicleName).setOnBindEditTextListener(TextView::setSingleLine);
    
        preference_vehicleRegplate = findPreference(G.PREF_VEHICLE_REGPLATE);
            //preference_vehicleRegplate.setSummary(vehicle.getRegNumber());
            preference_vehicleRegplate.setOnPreferenceChangeListener(onVehicleModified);
            ((EditTextPreference)preference_vehicleRegplate).setOnBindEditTextListener(edit_regplate ->
            {
                edit_regplate.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
                edit_regplate.setSingleLine();
            });
    
        preference_vehicleMaxVoltage = findPreference(G.PREF_VEHICLE_MAX_VOLTAGE);
            //preference_vehicleMaxVoltage.setSummary(vehicle.getMaxVoltage());
            preference_vehicleMaxVoltage.setOnPreferenceChangeListener(onVehicleModified);
            ((EditTextPreference)preference_vehicleMaxVoltage).setOnBindEditTextListener(editText ->
                editText.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL));
    
        preference_vehicleImage = findPreference(G.PREF_VEHICLE_IMAGE);
            String summary = vehicle.getImageFile().isEmpty() ?
                    getString(R.string.preferences_not_set) : getString(R.string.preferences_set);
            preference_vehicleImage.setSummary(summary);
            preference_vehicleImage.setOnPreferenceClickListener(onVehicleImageClick);
    }
    
    OnPreferenceClickListener onVehicleImageClick = preference ->
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, G.RC_FILE_PICKER);
        return true;
    };
    
    //for vehicle image picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(requestCode == G.RC_FILE_PICKER)
        {
            if(resultCode == RESULT_OK)
            {
                Uri uri = intent.getData();
                try(InputStream is = requireActivity().getContentResolver().openInputStream(uri))
                {
                    File path = new File(requireActivity().getFilesDir().toString() + "/media");
                    if(!path.exists())
                        if(!path.mkdir())
                            throw new IOException();
                        
                    File newFile = File.createTempFile("vehicleimage_", ".aaa", path); //todo zistit priponu
                    Files.copy(is, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    
                    File oldFile = new File(requireActivity().getFilesDir().toString() + "/media/" + vehicle.getImageFile());
                    if(oldFile.exists())
                        oldFile.delete();
                    
                    onVehicleModified.onPreferenceChange(preference_vehicleImage, newFile.getName());
                }
                catch(IOException e) {e.printStackTrace();}
            }
        }
    }
    
    OnPreferenceClickListener onLogout = preference ->
    {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(requireContext());
        confirmDialog.setTitle(getString(R.string.preferences_logout_dialog));
        confirmDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> logout());
        confirmDialog.setNegativeButton(getString(R.string.no), null);
        confirmDialog.show();
        
        return true;
    };
    
    private void logout() //todo move all to vm
    {
        //remove user icon and vehicles files
        deleteFiles();

        //remove shared-pref
        pref.edit()
                .remove(G.PREF_LAST_VEHICLE_ID)
                //.remove(G.PREF_USER_NICKNAME).remove(G.PREF_USER_EMAIL).remove(G.PREF_USER_ICON)
                //.remove(G.PREF_VEHICLE_NAME).remove(G.PREF_VEHICLE_REGPLATE).remove(G.PREF_VEHICLE_MAX_VOLTAGE).remove(G.PREF_VEHICLE_IMAGE)
            .apply();
        
        //remove database tables
        AppDatabase db = App.getDatabase();
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
    
        List<Vehicle> vehicles = vm.getAllVehicles();
        for(Vehicle v : vehicles)
        {
            File file = new File(requireActivity().getFilesDir().toString() + "/media/" + v.getImageFile());
            if(file.exists())
                file.delete();
        }
    }
    
    
    OnPreferenceChangeListener onNicknameChanged = (preference, newValue) ->
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
                        Snackbar.make(root, getString(R.string.preferences_toast_nickname_update_success), Snackbar.LENGTH_SHORT).show();
                    }
                    else
                        Snackbar.make(root, getString(R.string.preferences_toast_nickname_update_failed), Snackbar.LENGTH_LONG).show();
                    
                    progressbar.setVisibility(View.INVISIBLE);
                });
        
        return false;
    };
    
    OnPreferenceChangeListener onVehicleModified = (preference, newValue) ->
    {
        if(vehicle != null)
        {
            if(preference.equals(preference_vehicleName))
            {
                vehicle.setName((String) newValue);
                ((EditTextPreference) preference_vehicleName).setText(newValue.toString());
            }
            if(preference.equals(preference_vehicleRegplate))
            {
                vehicle.setRegNumber((String) newValue);
                ((EditTextPreference) preference_vehicleName).setText(newValue.toString());
            }
            if(preference.equals(preference_vehicleMaxVoltage))
            {
                vehicle.setMaxVoltage(Integer.parseInt((String) newValue));
                ((EditTextPreference) preference_vehicleName).setText(newValue.toString());
            }
            if(preference.equals(preference_vehicleImage))
            {
                vehicle.setImageFile((String) newValue);
                preference_vehicleImage.setSummary(getString(R.string.preferences_set));
            }
        }
        vm.updateVehicle(vehicle);
        return true;
    };
    
    OnPreferenceClickListener onInvalidateCache = preference ->
    {
        vm.deleteAllVehicleStatuses();
        Snackbar.make(root, PreferencesFragment.this.getString(R.string.preferences_invalidate_cache_dialog), Snackbar.LENGTH_SHORT).show();
        return true;
    };
    
    OnPreferenceClickListener onContactDeveloper = preference ->
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