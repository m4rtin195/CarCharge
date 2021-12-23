package com.martin.carcharge;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

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
    
        //todo hide nav buttons nizsie je?
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
        //Context configContext;
        super.attachBaseContext(newBase);
        String languageStr = PreferenceManager.getDefaultSharedPreferences(this).getString(G.PREF_LANGUAGE, "system");
        if(!languageStr.equals("system"))
        {
            Configuration config = new Configuration(Resources.getSystem().getConfiguration()); //get default system config
            //noinspection ConstantConditions
            config = updateConfigurationLanguage(config, languageStr);
            applyOverrideConfiguration(config);
            //configContext = newBase.createConfigurationContext(config); //create new context with modified system language
        }
        
        //super.attachBaseContext(configContext); //attach modified config
        // this was proposed to override system language, to get DateUtils.getRelativeTimeSpanString work with set locale
        // but it is not working idk why, and also crashes becase getDefaultSharedPreferences cannot be called without context
        // set first to obtain user-set language, and vice versa we cannot change language if we dont know to which one.
        // see: https://stackoverflow.com/a/68139358/14629312 for Kotlin solution (doesnt solve problem with sharedprefs).
    }
    
    private Configuration updateConfigurationLanguage(@NonNull Configuration config, String language)
    {
        Locale newLocale = stringToLocale(language);
        config.setLocale(newLocale);
        return config;
    }
    
    private Locale stringToLocale(String s)
    {
        //Locale.forLanguageTag(); //todo ???
        StringTokenizer st = new StringTokenizer(s,"_");
        String language = new String();
        String country = new String();
        if(st.hasMoreTokens())
            language = (String) st.nextElement();
        if(st.hasMoreTokens())
            country = (String) st.nextElement();
        return new Locale(language, country);
    }
    
    public Locale getCurrentLocale()
    {
        return getResources().getConfiguration().getLocales().get(0);
    }
    
    public int getResColor(int resoruceId)
    {
        return getResources().getColor(resoruceId, getTheme());
    }
}