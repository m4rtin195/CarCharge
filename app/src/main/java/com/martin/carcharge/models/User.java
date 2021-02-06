package com.martin.carcharge.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import androidx.preference.PreferenceManager;

public class User
{
    private SharedPreferences pref;
    
    private String nickname;
    private String email;
    private String imageFile;
    private Drawable image;
    
    public User(Context context)
    {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public String getNickname()
    {
        return nickname;
    }
    
    public void setNickname(String nickname)
    {
        pref.edit().putString("nickname", nickname).apply();
        this.nickname = nickname;
    }
    
    public String getEmail()
    {
        return email;
    }
    
    public void setEmail(String email)
    {
        this.email = email;
    }
    
    public String getImageFile()
    {
        return imageFile;
    }
    
    public void setImageFile(String imageFile)
    {
        this.imageFile = imageFile;
    }
    
    public Drawable getImage()
    {
        return image;
    }
    
    public void setImage(Drawable image)
    {
        this.image = image;
    }
}
