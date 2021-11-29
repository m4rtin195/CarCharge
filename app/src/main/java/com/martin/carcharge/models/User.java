package com.martin.carcharge.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable
{
    private String uid;
    private String nickname;
    private String email;
    private String imageFilename;
    private Bitmap icon;
    
    public User() {}
    
    public String getUid()
    {
        return uid;
    }
    public void setUid(String uid)
    {
        this.uid = uid;
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
    
    public String getImageFilename()
    {
        return imageFilename;
    }
    public void setImageFilename(String imageFilename)
    {
        this.imageFilename = imageFilename;
    }
    
    public void setIcon(Bitmap icon)
    {
        this.icon = icon;
    }
    public Bitmap getIcon()
    {
        return icon;
    }
    
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeString(uid);
        parcel.writeString(nickname);
        parcel.writeString(email);
        parcel.writeString(imageFilename);
        parcel.writeParcelable(icon, flags);
    }
    
    public User(Parcel parcel)
    {
        uid = parcel.readString();
        nickname = parcel.readString();
        email = parcel.readString();
        imageFilename = parcel.readString();
        icon = parcel.readParcelable(getClass().getClassLoader());
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
