package dz.atoxyd.ABM.util;

//original author: atoxyd 
//modified by: ........

import android.app.Activity;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import dz.atoxyd.ABM.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;



public class Helpers implements Constants {

    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists() && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, " su does not exist!!!");
            return false; // tell caller to bail...
        }
        try {
            if ((new CMDProcessor().su.runWaitFor("ls /data/app-private")).success()) {
                Log.i(TAG, " SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, " SU exists but we dont have permission");
                return false;
            }
        }
        catch (final NullPointerException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
    
    public static void restartApp(final Activity activity) {
        if (activity == null) return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }

    public static String ReadableByteCount(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = String.valueOf("KMGTPE".charAt(exp-1));
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

	public static String  inputStream2String(InputStream in, String encoding) throws  Exception {   
        StringBuffer out = new StringBuffer();   
        InputStreamReader inread = new InputStreamReader(in, encoding);   
           
        char[]b = new char[4096];   
        try
		{
			for(int n; (n = inread.read(b)) != -1;)
			{   
                out.append(new  String(b,  0,  n));   
			}
		}
		catch (IOException e){
		}
		return out.toString();   
    }
	
	public static void cpTextToclipoard(Context c, String text){
		ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(text);
	}
	
	public static String RunExecCmd(StringBuilder sb,Boolean su) {
		
		String shell;
		if(su){
            shell = "su";
			}
        else{
            shell = "sh";
		}

		Process process = null;

		DataOutputStream processOutput = null;
		InputStream processInput = null;
		String outmsg = new String();
		
		
		try {

			process = Runtime.getRuntime().exec(shell);
			processOutput = new DataOutputStream(process.getOutputStream());
			processOutput.writeBytes(sb.toString() + "\n");
			processOutput.writeBytes("exit\n");
			processOutput.flush();
			processInput = process.getInputStream();
			outmsg = inputStream2String(processInput, "UTF-8");
			process.waitFor();

		} catch (Exception e) {
			//Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
			//return;
		}

		finally {
			try {
				if (processOutput != null) {
					processOutput.close();
				}
				process.destroy();
			} catch (Exception e) {
			}

		}
		return outmsg;
	}
	
	public static String getArch() {
        String string2 = System.getProperty("os.arch").substring(0, 3).toUpperCase();
		
        if (string2.equals("ARM") || string2.equals("AAR")) {
            return "arm";
        }
		if (string2.equals("ARM64") || string2.equals("AAR64")) {
            return "arm64";
        }
        if (string2.equals("MIP")) {
            return "mips";
        }
		if (string2.equals("MIP64")) {
            return "mips64";
        }
        if (string2.equals("I68") || string2.equals("X86")) {
            return "x86";
        }
		if (string2.equals("I68_64") || string2.equals("X86_64")) {
            return "x86_64";
        }
		Log.d(TAG, "Unknown arch: " + string2);
        return string2;
    }
		
	public static void get_rawFile(Context c) {

		SharedPreferences mPreferences;

		mPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		final String fn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/devices.xml";

		try{
			InputStream inputStream = c.getResources().openRawResource(R.raw.devices);

			OutputStream outputStream = new FileOutputStream(new File(fn));
			byte buffer[] = new byte[1024];
			int length = 0;

			while((length=inputStream.read(buffer)) > 0) {
				outputStream.write(buffer,0,length);
			}

			outputStream.close();
			inputStream.close();

		}catch (Exception e) {

		}
	}
	
}

