package com.example.automaticvideodirector.ui;

import android.content.Context;
import android.util.AttributeSet;

// Credits:
//http://enzam.wordpress.com/2013/09/29/android-preference-show-current-value-in-summary/
public class EditTextPreferenceWithSummary extends android.preference.EditTextPreference {
 
    public EditTextPreferenceWithSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
     
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
         
        setSummary(getSummary());
    }
 
    @Override
    public CharSequence getSummary() {
        return this.getText();
    }
}
