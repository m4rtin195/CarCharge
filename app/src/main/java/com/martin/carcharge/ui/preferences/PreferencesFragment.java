package com.martin.carcharge.ui.preferences;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.martin.carcharge.databinding.FragmentPreferencesBinding;
import com.martin.carcharge.models.MainViewModel.MainViewModel;
import com.martin.carcharge.models.User;
import com.martin.carcharge.models.Vehicle;
import com.martin.carcharge.storage.AppDatabase;
import com.martin.carcharge.storage.CloudStorage;
import com.martin.carcharge.storage.FileStorage;
import com.martin.carcharge.storage.FirestoreDb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class PreferencesFragment extends PreferenceFragmentCompat
{
    private SharedPreferences pref;
    private MainViewModel vm;
    private FirestoreDb fdb;
    private FirebaseAuth auth;
    private CloudStorage cstrg;
    
    private FragmentPreferencesBinding binding;
    private View root;
    private Toolbar toolbar;
    private ProgressBar progressbar;
    
    private Preference
                preference_language, preference_fcmEnabled, preference_updateInterval, preference_invalidateCache, preference_debugEnabled,  //app-prefs
                preference_version, preference_fcmToken, preference_contactDeveloper;  //info
    private Preference
                preference_user, preference_logout,  //user-related
                preference_vehicleName, preference_vehicleRegplate, preference_vehicleMaxVoltage, preference_vehicleImage;  //vehicle-related
    
    private User user;
    private Vehicle vehicle;
    
    private ActivityResultLauncher<Intent> vehicleImagePickerLauncher;
    
    /*
        global settings are synced with firebase, but on fail, there's no rollback
        vehicle settings changes are sent to firestore, and are updated locally only if server update succeed
    */
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
        vehicleImagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), vehicleImagePickerCallback);
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
        fdb = App.getFirestoreDb();
        auth = FirebaseAuth.getInstance();
        cstrg = App.getCloudStorage();
        
        user = vm.getUser();
        vehicle = vm.getCurrentVehicle();
        
        toolbar = binding.toolbarPreferences;
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
        progressbar = binding.progressbarPreferences;
        
        _configPreferences();
        return root;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
    
    @SuppressWarnings("ConstantConditions")
    private void _configPreferences()
    {
        /***/
        //General & About
    
        preference_language = findPreference(G.PREF_LANGUAGE);
            preference_language.setOnPreferenceChangeListener((preference, newValue) ->
            {
                requireActivity().recreate(); //activity is recreated, but not whole application
                fdb.updateUserData(G.FIRESTORE_LANGUAGE, newValue);
                return true;
            });
        
        preference_fcmEnabled = findPreference(G.PREF_FCM_ENABLED);
            preference_fcmEnabled.setOnPreferenceChangeListener((preference, newValue) ->
            {
                preference_updateInterval.setVisible(!(boolean)newValue);
                fdb.updateUserData(G.FIRESTORE_FCM_ENABLED, newValue);
                return true;
            });
    
        preference_updateInterval = findPreference(G.PREF_UPDATE_INTERVAL);
            preference_updateInterval.setVisible(!pref.getBoolean(G.PREF_FCM_ENABLED, false));
            preference_updateInterval.setOnPreferenceChangeListener((preference, newValue) ->
            {
                ((MainActivity)requireActivity()).getDownloader().restart();
                fdb.updateUserData(G.FIRESTORE_UPDATE_INTERVAL, newValue);
                return true;
            });
    
        preference_invalidateCache = findPreference(G.PREF_INVALIDATE_CACHE);
            preference_invalidateCache.setOnPreferenceClickListener(onInvalidateCache);
            
        preference_debugEnabled = findPreference(G.PREF_DEBUG_ENABLED);
            preference_debugEnabled.setOnPreferenceChangeListener((preference, newValue) ->
            {
                fdb.updateUserData(G.FIRESTORE_DEBUG_ENABLED, newValue);
                return true;
            });
    
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
            String nickname = user.getNickname().isEmpty() ? getString(R.string.preferences_nickname) : user.getNickname();
            preference_user.setTitle(nickname);
            preference_user.setSummary(user.getEmail());
            preference_user.setIcon(new BitmapDrawable(getResources(), user.getIcon()));
            preference_user.setOnPreferenceChangeListener(onNicknameChanged);
            ((EditTextPreference)preference_user).setText(nickname);
            ((EditTextPreference)preference_user).setOnBindEditTextListener(TextView::setSingleLine);
    
        preference_logout = findPreference(G.PREF_LOGOUT);
            preference_logout.setOnPreferenceClickListener(onLogout);
    
        preference_vehicleName = findPreference(G.PREF_VEHICLE_NAME);
            preference_vehicleName.setSummaryProvider(vehiclePrefsSummaryProvider);
            preference_vehicleName.setOnPreferenceChangeListener(onVehicleModified);
            ((EditTextPreference)preference_vehicleName).setText(vehicle.getName());
            ((EditTextPreference)preference_vehicleName).setOnBindEditTextListener(TextView::setSingleLine);
    
        preference_vehicleRegplate = findPreference(G.PREF_VEHICLE_REGPLATE);
            preference_vehicleRegplate.setSummaryProvider(vehiclePrefsSummaryProvider);
            preference_vehicleRegplate.setOnPreferenceChangeListener(onVehicleModified);
            ((EditTextPreference)preference_vehicleRegplate).setText(vehicle.getRegNumber());
            ((EditTextPreference)preference_vehicleRegplate).setOnBindEditTextListener(edit_regplate ->
            {
                edit_regplate.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
                edit_regplate.setSingleLine();
            });
    
        preference_vehicleMaxVoltage = findPreference(G.PREF_VEHICLE_MAX_VOLTAGE);
            preference_vehicleMaxVoltage.setSummaryProvider(vehiclePrefsSummaryProvider);
            preference_vehicleMaxVoltage.setOnPreferenceChangeListener(onVehicleModified);
            ((EditTextPreference)preference_vehicleMaxVoltage).setText(String.valueOf(vehicle.getMaxVoltage()));
            ((EditTextPreference)preference_vehicleMaxVoltage).setOnBindEditTextListener(editText ->
                editText.setInputType(InputType.TYPE_CLASS_NUMBER));
    
        preference_vehicleImage = findPreference(G.PREF_VEHICLE_IMAGE);
            //String summary = vehicle.getImageFilename().isEmpty() ? getString(R.string.preferences_not_set) : getString(R.string.preferences_set);
            //preference_vehicleImage.setSummary(summary);
            preference_vehicleImage.setSummaryProvider(vehiclePrefsSummaryProvider);
            preference_vehicleImage.setOnPreferenceClickListener(onVehicleImageClick);
        
    } //configPreferences
    
    /**********/
    
    private final OnPreferenceChangeListener onNicknameChanged = (preference, newValue) ->
    {
        progressbar.setVisibility(View.VISIBLE);
        FirebaseUser fUser = Objects.requireNonNull(auth.getCurrentUser());
        fUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newValue.toString()).build())
                .addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        String title = (newValue.toString().isEmpty() ?
                                getString(R.string.preferences_nickname) : newValue.toString());
                        preference_user.setTitle(title);
                        ((EditTextPreference) preference_user).setText(newValue.toString()); //save to shared
                        Snackbar.make(root, getString(R.string.preferences_toast_nickname_update_success), Snackbar.LENGTH_SHORT).show();
                        this.user.setNickname((String)newValue);
                        vm.setUser(this.user);
                    }
                    else
                    {
                        Log.e(G.tag, "Updating nickaneme failed: " + task.getException());
                        Snackbar.make(root, getString(R.string.preferences_toast_nickname_update_failed), Snackbar.LENGTH_LONG).show();
                    }
                    
                    progressbar.setVisibility(View.INVISIBLE);
                });
        
        return false;
    };
    
    
    //spolocny resolver pre summary pre vsetky vehicle-preferences
    //volany pri kazdej zmene (.setText())
    //alternativne sa da volat .setSummary rucne pri kazdej zmene preference
    //must be on top (forward reference)
    private final Preference.SummaryProvider<Preference> vehiclePrefsSummaryProvider = new Preference.SummaryProvider<Preference>()
    {
        @Override
        public CharSequence provideSummary(Preference preference)
        {
            if(preference.equals(preference_vehicleName))
                return (vehicle.getName().isEmpty() ? getString(R.string.preferences_not_set) : vehicle.getName());
            if(preference.equals(preference_vehicleRegplate))
                return (vehicle.getRegNumber().isEmpty() ? getString(R.string.preferences_not_set) : vehicle.getRegNumber());
            if(preference.equals(preference_vehicleMaxVoltage))
                return (vehicle.getMaxVoltage()==0 ? getString(R.string.preferences_not_set) : vehicle.getMaxVoltage() + " " + getString(R.string.preferences_volts));
            if(preference.equals(preference_vehicleImage))
                return (vehicle.getImageFilename().isEmpty() ? getString(R.string.preferences_not_set) : getString(R.string.preferences_set));
            
            return new String();
        }
    };
    
    
    private final OnPreferenceClickListener onVehicleImageClick = preference ->
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //startActivityForResult(intent, G.RC_FILE_PICKER);
        vehicleImagePickerLauncher.launch(intent);
        return true;
    };
    
    private final ActivityResultCallback<ActivityResult> vehicleImagePickerCallback = new ActivityResultCallback<ActivityResult>()
    {
        @Override
        public void onActivityResult(ActivityResult result)
        {
            if(result.getResultCode() == RESULT_OK)
            {
                Intent intent = result.getData();
                Uri uri = Objects.requireNonNull(intent).getData();
                String extension = uri.getPath().substring(uri.getPath().lastIndexOf("."));

                try(InputStream is = requireActivity().getContentResolver().openInputStream(uri))
                {
                    File path = new File(requireActivity().getFilesDir().toString() + "/media");
                    if(!path.exists())
                        if(!path.mkdir())
                            throw new IOException();

                    File newFile = File.createTempFile("vehicleimage_", extension, path);
                    Files.copy(is, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    onVehicleModified.onPreferenceChange(preference_vehicleImage, newFile.getName());
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    };
    
    
    //listener for common tasks for all preferences
    //show snack, update in viewmodel, hide progressbar
    //must be before onVehicleModified (forward reference)
    private final OnCompleteListener<Void> finishVehicleUpdate = task ->
    {
        if(task.isSuccessful())
        {
            Snackbar.make(root, getString(R.string.preferences_toast_vehicle_update_success), Snackbar.LENGTH_SHORT).show();
            vm.insertOrUpdateVehicle(vehicle); //to save to local db
        }
        else
        {
            Log.e(G.tag, "Updating in Firestore failed: " + task.getException());
            Snackbar.make(root, getString(R.string.preferences_toast_vehicle_update_failed), Snackbar.LENGTH_LONG).show();
        }
        
        progressbar.setVisibility(View.INVISIBLE);
    };
    
    
    private final OnPreferenceChangeListener onVehicleModified = (preference, newValue) ->
    {
        progressbar.setVisibility(View.VISIBLE);
    
        if(preference.equals(preference_vehicleName))
        {
            fdb.updateVehicleData(this.vehicle, G.FIRESTORE_VEHICLE_NAME, newValue)
                .addOnCompleteListener(task ->
                    {
                        if(task.isSuccessful())
                        {
                            this.vehicle.setName((String) newValue);
                            ((EditTextPreference)preference_vehicleName).setText(newValue.toString());
                        }
                    })
                .addOnCompleteListener(finishVehicleUpdate);
        }
        
        if(preference.equals(preference_vehicleRegplate))
        {
            fdb.updateVehicleData(this.vehicle, G.FIRESTORE_VEHICLE_REGPLATE, newValue)
                .addOnCompleteListener(task ->
                    {
                        if(task.isSuccessful())
                        {
                            this.vehicle.setRegNumber((String) newValue);
                            ((EditTextPreference)preference_vehicleRegplate).setText(newValue.toString());
                        }
                    })
                .addOnCompleteListener(finishVehicleUpdate);
        }
        
        if(preference.equals(preference_vehicleMaxVoltage))
        {
            fdb.updateVehicleData(this.vehicle, G.FIRESTORE_VEHICLE_MAX_VOLTAGE, Integer.parseInt((String)newValue))
                .addOnCompleteListener(task ->
                    {
                        if(task.isSuccessful())
                        {
                            this.vehicle.setMaxVoltage(Integer.parseInt((String)newValue));
                            ((EditTextPreference)preference_vehicleMaxVoltage).setText(newValue.toString());
                        }
                    })
                .addOnCompleteListener(finishVehicleUpdate);
        }
        
        if(preference.equals(preference_vehicleImage))
        {
            //upload image to CloudStorage
            File file = new File(requireContext().getFilesDir().toString() + "/media/", (String)newValue);
            cstrg.uploadVehicleImage(file)
                .thenAccept((success) ->
                {
                    if(success)
                    {
                        //update filename in Firestore
                        fdb.updateVehicleData(this.vehicle, G.FIRESTORE_VEHICLE_IMAGE, newValue)
                                .addOnCompleteListener(task ->
                                {
                                    if(task.isSuccessful())
                                    {
                                        //delete old file
                                        FileStorage.deleteFile(requireContext(), "/media/" + vehicle.getImageFilename());
                                        cstrg.deleteVehicleImage(this.vehicle.getImageFilename());
            
                                        this.vehicle.setImageFilename((String) newValue);
                                        this.vehicle.loadVehicleImage(requireContext());
                                    }
                                })
                                .addOnCompleteListener(finishVehicleUpdate);
                    }
                    else //cloudstorage upload failed
                    {
                        Snackbar.make(root, getString(R.string.preferences_toast_vehicle_update_failed), Snackbar.LENGTH_LONG).show();
                        progressbar.setVisibility(View.INVISIBLE);
                    }
                }); //cloudstorage update
        } //preference_vehicleImage
        
        return false;
    }; //onVehicleModified listener
    
    
    private final OnPreferenceClickListener onLogout = preference ->
    {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(requireContext());
        confirmDialog.setTitle(getString(R.string.preferences_logout_dialog));
        confirmDialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> _logout());
        confirmDialog.setNegativeButton(getString(R.string.no), null);
        confirmDialog.show();
        
        return true;
    };
    
    private void _logout()
    {
        progressbar.setVisibility(View.VISIBLE);
        //remove user icon and vehicles files
        FileStorage.deleteAllFiles(requireContext());
        
        //unregister vehicles from fcm token
        App.getApiClient().unregisterFcm(vm.getAllVehicles()); //blocking
        
        //remove shared-prefs
        pref.edit().remove(G.PREF_LAST_VEHICLE_ID).remove(G.PREF_USER_ICON).apply();
        
        //remove database tables
        AppDatabase db = App.getDatabase();
        db.dao().deleteAllVehicles();
        db.dao().deleteAllStatuses();
        
        //viewmodel is dropped because process will be recreated
        
        //sign-out from google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignIn.getClient(requireContext(), gso).signOut().addOnCompleteListener(task ->
        {
            //sign-out from firebase auth
            auth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
            //pockat na dokoncenie I/O na persistent storage
            try {Thread.sleep(100);} catch(InterruptedException e) {e.printStackTrace();}
            Runtime.getRuntime().exit(0);
        });
    }
    
    
    private final OnPreferenceClickListener onInvalidateCache = preference ->
    {
        vm.deleteAllVehicleStatuses();
        Snackbar.make(root, PreferencesFragment.this.getString(R.string.preferences_invalidate_cache_dialog), Snackbar.LENGTH_SHORT).show();
        return true;
    };
    
    
    private final OnPreferenceClickListener onContactDeveloper = preference ->
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
    
    
    private int eggCounter = 0;
    private final OnPreferenceClickListener easterEgg = preference ->
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