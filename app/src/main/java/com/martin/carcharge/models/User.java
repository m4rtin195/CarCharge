package com.martin.carcharge.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseUser;

public class User implements Parcelable
{
    private String nickname;
    private String email;
    private String imageFile;
    private Drawable image;
    
    public User()
    {}
    
    public User(Parcel parcel)
    {
        nickname = parcel.readString();
        email = parcel.readString();
        imageFile = parcel.readString();
        //todo Bitmap
    }
    
    public String getNickname()
    {
        return nickname;
    }
    
    public void setNickname(String nickname)
    {
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
    
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeString(nickname);
        parcel.writeString(email);
        parcel.writeString(imageFile);
        //bitmap
    }
    
    public static final Creator<User> CREATOR = new Creator<User>()
    {
        @Override
        public User[] newArray(int size)
        {
            return new User[size];
        }
        
        @Override
        public User createFromParcel(Parcel incoming)
        {
            return new User(incoming);
        }
    };
}
