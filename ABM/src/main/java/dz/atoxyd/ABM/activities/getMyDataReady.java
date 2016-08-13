package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;



import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.Helpers;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;




public class getMyDataReady extends Activity implements Constants, ActivityThemeChangeInterface{
	
	final Context context=this;
	private boolean mIsLightTheme;
    private SharedPreferences mPreferences;
	private static ProgressDialog progressDialog;
    private static boolean isdialog=false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		setTheme();
        setContentView(R.layout.get_data_ready);
       
		new getDataReadyOperation().execute();
	
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
	
	private class getDataReadyOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //SystemClock.sleep(1000);
            final Boolean isDataReady = getMyDataReady(context);
            if (isDataReady) return "ok";
            else return "nok";
			
        }
        @Override
        protected void onPostExecute(String result) {
            isdialog=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if(result.equals("ok")){
                Toast.makeText(context,getString(R.string.get_data_ready_ok),Toast.LENGTH_SHORT).show();
				Intent returnIntent = new Intent();
				returnIntent.putExtra("s",result);
				setResult(RESULT_OK,returnIntent);
				finish();
            }
            else{
                //if the arch is not suported
            }
        }
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, getString(R.string.first_run_message));
            isdialog=true;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
	
	private void get_assetFile(String file, Context c, boolean fileInDir) {
		String arch="arm";//Helpers.getArch();
		final AssetManager am = c.getAssets();
		InputStream inputStream=null;
		try{
			if(fileInDir){
				inputStream = am.open(arch+"/"+file);
			}else{
				inputStream = am.open(file);
			}

			OutputStream outputStream = c.openFileOutput(file, c.MODE_PRIVATE);
			byte buffer[] = new byte[1024];
			int length = 0;

			while((length=inputStream.read(buffer)) > 0) {
				outputStream.write(buffer,0,length);
			}

			outputStream.close();
			inputStream.close();

		}catch (IOException e) {
			Log.d(TAG, "Unknown arch: " + arch);
		}
	}

	private Boolean getMyDataReady(Context c){

		try{
			get_assetFile("mkboot", c, false);
			new File(c.getFilesDir()+"/mkboot").setExecutable(true,false);
			get_assetFile("wrapper", c, false);
			new File(c.getFilesDir()+"/wrapper").setExecutable(true,false);
			get_assetFile("magic.mgc", c, false);
			new File(c.getFilesDir()+"/magic.mgc").setReadable(true,false);
			get_assetFile("busybox", c, true);
			new File(c.getFilesDir()+"/busybox").setExecutable(true,false);
			get_assetFile("cpio", c, true);
			new File(c.getFilesDir()+"/cpio").setExecutable(true,false);
			get_assetFile("file", c, true);
			new File(c.getFilesDir()+"/file").setExecutable(true,false);
			get_assetFile("grep", c, true);
			new File(c.getFilesDir()+"/grep").setExecutable(true,false);
			get_assetFile("gzip", c, true);
			new File(c.getFilesDir()+"/gzip").setExecutable(true,false);
			get_assetFile("lz4", c, true);
			new File(c.getFilesDir()+"/lz4").setExecutable(true,false);
			get_assetFile("lzma", c, true);
			new File(c.getFilesDir()+"/lzma").setExecutable(true,false);
			get_assetFile("lzop", c, true);
			new File(c.getFilesDir()+"/lzop").setExecutable(true,false);
			get_assetFile("mkbootfs", c, true);
			new File(c.getFilesDir()+"/mkbootfs").setExecutable(true,false);
			get_assetFile("mkbootimg", c, true);
			new File(c.getFilesDir()+"/mkbootimg").setExecutable(true,false);
			get_assetFile("od", c, true);
			new File(c.getFilesDir()+"/od").setExecutable(true,false);
			get_assetFile("xz", c, true);
			new File(c.getFilesDir()+"/xz").setExecutable(true,false);
			return true;
		}catch (Exception e) {
		Log.d(TAG, "Canot copy data files ");
		}
		return false;
	}
}
