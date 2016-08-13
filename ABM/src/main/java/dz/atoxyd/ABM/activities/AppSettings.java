package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceScreen;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;



public class AppSettings extends PreferenceActivity implements Constants, ActivityThemeChangeInterface {

	final Context context=this;
    SharedPreferences mPreferences;
    private CheckBoxPreference mLightThemePref;
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.app_settings);
        setTheme();

        mLightThemePref = (CheckBoxPreference) findPreference("use_dark_theme");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals("use_dark_theme")) {
            mPreferences.edit().putBoolean("theme_changed",true).commit();
            finish();
            return true;
        } 
        return false;
    }

    @Override
    public boolean isThemeChanged() {
        final boolean is_dark_theme = mPreferences.getBoolean(PREF_USE_DARK_THEME, false);
        return is_dark_theme != mLightThemePref.isChecked();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setTheme() {
        final boolean is_dark_theme = mPreferences.getBoolean(PREF_USE_DARK_THEME, false);
        setTheme(is_dark_theme ? R.style.Theme_Dark : R.style.Theme_Light);
        getListView().setBackgroundDrawable(getResources().getDrawable(is_dark_theme ? R.drawable.background_holo_dark : R.drawable.background_holo_light));
    }
}
