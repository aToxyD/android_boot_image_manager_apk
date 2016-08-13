package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.Helpers;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;



public class CheckSU extends Activity implements Constants, ActivityThemeChangeInterface{
	
	final Context context=this;
    private boolean mIsLightTheme;
    private TextView info;
    private ImageView attn;
    SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		setTheme();
        setContentView(R.layout.check_su);
        info=(TextView) findViewById(R.id.info);
        attn=(ImageView) findViewById(R.id.attn);
		
		new TestSU().execute();
    }

    private class TestSU extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SystemClock.sleep(1000);
            final Boolean canSu = Helpers.checkSu();
            if (canSu) return "ok";
            else return "nok";
        }
        @Override
        protected void onPostExecute(String result) {

            if(result.equals("nok")){
                info.setText(getString(R.string.su_failed));
                attn.setImageResource(R.drawable.ic_attn);
            }
            else{
                Intent returnIntent = new Intent();
                returnIntent.putExtra("r",result);
                setResult(RESULT_OK,returnIntent);
                finish();
            }

        }
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
   
    @Override
    public void onResume() {
        super.onResume();
    }
	
	@Override
    public boolean isThemeChanged() {
        final boolean is_dark_theme = mPreferences.getBoolean(PREF_USE_DARK_THEME, false);
        return is_dark_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_dark_theme = mPreferences.getBoolean(PREF_USE_DARK_THEME, false);
        mIsLightTheme = mPreferences.getBoolean(PREF_USE_DARK_THEME, false);
        setTheme(is_dark_theme ? R.style.Theme_Dark : R.style.Theme_Light);
    }

}
