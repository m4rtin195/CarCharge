package com.martin.carcharge;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.StringTokenizer;

public class BaseActivity extends AppCompatActivity
{
    public void setupUI()
    {
        setTheme(R.style.Theme_CarCharge);
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.background, getTheme()));
            window.setNavigationBarColor(getResources().getColor(R.color.google_background, getTheme()));
            window.setNavigationBarDividerColor(getResources().getColor(R.color.tile_gray, getTheme()));
        }
    
        //else
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        //decorView.setSystemUiVisibility(uiOptions);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void hideNav(boolean b)
    {
        Window window = getWindow();
        window.getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        if(b)
            window.getInsetsController().hide(WindowInsets.Type.navigationBars());
        else
            window.getInsetsController().show(WindowInsets.Type.navigationBars());
    }
    
    public void setStatusBarColor(int color)
    {
        getWindow().setStatusBarColor(color);
    }
    
    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(newBase);
        String languageStr = PreferenceManager.getDefaultSharedPreferences(this).getString(G.PREF_LANGUAGE, "system");
        if(!languageStr.equals("system"))
            applyOverrideConfiguration(updateConfigurationLanguage(new Configuration(), languageStr));
    }
    
    private Configuration updateConfigurationLanguage(@NotNull Configuration config, String language)
    {
        if(!config.getLocales().isEmpty()) return config;
        
        Locale newLocale = stringToLocale(language);
        config.setLocale(newLocale);
        return config;
    }
    
    private Locale stringToLocale(String s)
    {
        //Locale.forLanguageTag(); //todo ???
        StringTokenizer tempStringTokenizer = new StringTokenizer(s,"_");
        String language = new String();
        String country = new String();
        if(tempStringTokenizer.hasMoreTokens())
            language = (String) tempStringTokenizer.nextElement();
        if(tempStringTokenizer.hasMoreTokens())
            country = (String) tempStringTokenizer.nextElement();
        return new Locale(language, country);
    }
}