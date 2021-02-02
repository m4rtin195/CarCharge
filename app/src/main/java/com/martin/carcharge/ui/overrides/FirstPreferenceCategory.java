package com.martin.carcharge.ui.overrides;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

public class FirstPreferenceCategory extends PreferenceCategory
{
    public FirstPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public FirstPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
    public FirstPreferenceCategory(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public FirstPreferenceCategory(Context context)
    {
        super(context);
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);
        
        TextView textView = (TextView) holder.findViewById(android.R.id.title);
        LinearLayout root = (LinearLayout) textView.getRootView().getRootView();
        
        if(root != null)
        {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = 0;
            root.setLayoutParams(params);
        }
    }
}