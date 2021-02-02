package com.martin.carcharge.ui.preferences;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.martin.carcharge.AppActivity;
import com.martin.carcharge.BuildConfig;
import com.martin.carcharge.LoginActivity;
import com.martin.carcharge.MainActivity;
import com.martin.carcharge.R;
import com.martin.carcharge.database.AppDatabase;
import com.martin.carcharge.models.MainViewModel;
import com.martin.carcharge.models.Vehicle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class PreferencesFragment extends PreferenceFragmentCompat
{
    private AppDatabase db;
    private SharedPreferences pref;
    private MainViewModel vm;
    private FirebaseAuth auth;
    
    View root;
    Toolbar toolbar;
    ProgressBar progressbar;
    Preference preference_language, preference_fcmEnabled, preference_updateFrequency, preference_invalidateCache,
            preference_version, preference_fcmToken, preference_contactDeveloper;
    Preference preference_user, preference_logout,
            preference_vehicleName, preference_vehicleRegplate, preference_vehicleCapacity, preference_vehicleImage;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = super.onCreateView(inflater, container, savedInstanceState);
        
        db = AppActivity.getDatabase();
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        auth = FirebaseAuth.getInstance();
        
        toolbar = root.findViewById(R.id.toolbar_preferences);
            toolbar.setBackgroundColor(Color.TRANSPARENT);
            toolbar.setTitle(getString(R.string.app_preferences));
            toolbar.setNavigationOnClickListener(view1 -> requireActivity().onBackPressed());
    
        ((MainActivity)requireActivity()).setFabVisible(false);
        ((MainActivity)requireActivity()).setBottomBarVisible(false);
    
        progressbar = root.findViewById(R.id.progress_preferences);
    
        /***/
    
        preference_language = findPreference("language");
            preference_language.setOnPreferenceChangeListener((preference, newValue) ->
            {
                requireActivity().recreate();
                return true;
            });
    
        preference_fcmEnabled = findPreference("fcm_enabled");
            preference_fcmEnabled.setOnPreferenceChangeListener((preference, newValue) ->
            {
                preference_updateFrequency.setVisible(!(boolean)newValue);
                return true;
            });
    
        preference_updateFrequency = findPreference("update_frequency");
        
        preference_invalidateCache = findPreference("invalidate_cache");
            preference_invalidateCache.setOnPreferenceClickListener(invalidateCache);
            
        preference_version = findPreference("version");
            preference_version.setSummary(BuildConfig.VERSION_NAME);
            preference_version.setOnPreferenceClickListener(easterEgg);
    
        preference_fcmToken = findPreference("fcm_token");
            preference_fcmToken.setSummary(pref.getString("fcm_token", "unloaded"));
            
        preference_contactDeveloper = findPreference("contact_developer");
            preference_contactDeveloper.setOnPreferenceClickListener(contactDeveloper);
    
        /***/
        
        preference_user = findPreference("user_nickname");
            String title = (pref.getString("user_nickname", "").isEmpty() ?
                    getString(R.string.preferences_nickname) : pref.getString("user_nickname", ""));
            preference_user.setTitle(title);
            preference_user.setSummary(pref.getString("user_email", ""));
            preference_user.setIcon(getUserIcon(pref.getString("user_icon", "")));
            preference_user.setOnPreferenceChangeListener(nicknameChanged);
            ((EditTextPreference)preference_user).setOnBindEditTextListener(TextView::setSingleLine);
            
        preference_logout = findPreference("logout");
            preference_logout.setOnPreferenceClickListener(this::onLogoutClick);
            
        preference_vehicleName = findPreference("vehicle_name");
            preference_vehicleName.setOnPreferenceChangeListener(vehicleModified);
    
        preference_vehicleRegplate = findPreference("vehicle_regplate");
            preference_vehicleRegplate.setOnPreferenceChangeListener(vehicleModified);
            
        preference_vehicleCapacity = findPreference("vehicle_capacity");
            preference_vehicleCapacity.setOnPreferenceChangeListener(vehicleModified);
            
        preference_vehicleImage = findPreference("vehicle_image");
            preference_vehicleImage.setOnPreferenceClickListener(null); //todo implement
    
        /***/
        
        return root;
    }
    
    private boolean onLogoutClick(Preference preference)
    {
        DialogInterface.OnClickListener onPositive = (dialog, which) ->
        {
            pref.edit()
                    .remove("user_nickname")
                    .remove("user_email")
                    .remove("user_icon")
                    .apply();
            
            File file = new File();
            file.delete();
            
            db.dao().deleteAllStatuses();
            
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignIn.getClient(requireContext(), gso).signOut().addOnCompleteListener(task ->
            {
                auth.signOut();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
                //pockat na dokoncenie IO na persistent
                 try {Thread.sleep(100);} catch(InterruptedException e) {e.printStackTrace();}
                Runtime.getRuntime().exit(0);
            });
        };
        
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(requireContext());
        confirmDialog.setTitle(getString(R.string.preferences_logout_dialog));
        confirmDialog.setPositiveButton(getString(R.string.yes), onPositive);
        confirmDialog.setNegativeButton(getString(R.string.no), null);
        confirmDialog.show();
        
        return true;
    }
    
    Preference.OnPreferenceChangeListener nicknameChanged = (preference, newValue) ->
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
    
    Preference.OnPreferenceChangeListener vehicleModified = (preference, newValue) ->
    {
        Vehicle vehicle = vm.Vehicle().getValue();
        if(vehicle != null)
        {
            if(preference.equals(preference_vehicleName))
                vehicle.setName(newValue.toString());
            if(preference.equals(preference_vehicleRegplate))
                vehicle.setRegNumber(newValue.toString());
            if(preference.equals(preference_vehicleCapacity))
                vehicle.setBatteryCapacity(Integer.parseInt(newValue.toString())); //todo check format}
        }
        
        vm.Vehicle().postValue(vehicle);
        db.dao().updateVehicle(vehicle);
        return true;
    };
    
    Preference.OnPreferenceClickListener invalidateCache = preference ->
    {
        db.dao().deleteAllStatuses();
        Snackbar.make(root, getString(R.string.preferences_invalidate_cache_dialog), Snackbar.LENGTH_SHORT).show();
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
    
    int eggCounter = 0;
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
    
    Drawable getUserIcon(String uri)
    {
        Drawable icon = null;
        
        if(uri.isEmpty())
            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_user, requireActivity().getTheme());
        else
        {
            try
            {
                InputStream ims = requireActivity().getContentResolver().openInputStream(Uri.parse(uri));
                Bitmap raw = BitmapFactory.decodeStream(ims);
                raw = getclip(raw);
                
                
                icon = new BitmapDrawable(getResources(), raw);
            }
            catch(FileNotFoundException e) {e.printStackTrace();}
        }
        
        return icon;
    }
    
    public Bitmap getclip(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f, bitmap.getWidth() / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    
}