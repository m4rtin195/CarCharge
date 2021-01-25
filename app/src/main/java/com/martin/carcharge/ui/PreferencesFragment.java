package com.martin.carcharge.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.martin.carcharge.R;

public class PreferencesFragment extends PreferenceFragmentCompat
{
    Preference preference_language;
    Preference preference_version;
    Preference preference_contactDeveloper;
    //NumberPickerPreference preference_refreshrate;
    
    String appVersion;
    private int counter = 0;
    
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        try {appVersion = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName;}
        catch(PackageManager.NameNotFoundException e) {e.printStackTrace();}
        
        preference_language = findPreference("language");
       /* preference_language.setOnPreferenceChangeListener((preference, newValue) ->
        {
            requireActivity().recreate();
            return true;
        });*/
        
        //preference_refreshrate = findPreference("refreshrate");
        
        preference_version = findPreference("version");
        preference_version.setSummary(appVersion);
        preference_version.setOnPreferenceClickListener((preference) ->
        {
            counter++;
            if (counter >= 5)
            {
                Toast.makeText(requireContext(), getString(R.string.preferences_easter_egg), Toast.LENGTH_LONG).show();
                counter = Integer.MIN_VALUE;
            }
            return true;
        });
    
        preference_contactDeveloper = findPreference("contact_developer");
        preference_contactDeveloper.setOnPreferenceClickListener(preference ->
        {
            onContactDeveloper();
            return true;
        });
        
        return view;
    }
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
    
    
    
    public void onContactDeveloper()
    {
        String body =
                "\n\n-----------------------------" +
                "\nPlease don't remove this information" +
                "\n Device OS version: " + Build.VERSION.RELEASE +
                "\n App Version: " + appVersion +
                "\n Device Brand: " + Build.BRAND +
                "\n Device Model: " + Build.MODEL +
                "\n Device Manufacturer: " + Build.MANUFACTURER;
    
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"martin.timko@centrum.sk", "timko10@stud.uniza.sk"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Query from CarCharge app");
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.preferences_choose_email_client)));
    }
}