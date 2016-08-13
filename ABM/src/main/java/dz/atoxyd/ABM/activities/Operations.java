package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.CMDProcessor;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.FileArrayAdapter;
import dz.atoxyd.ABM.util.Helpers;
import dz.atoxyd.ABM.util.Item;
import dz.atoxyd.ABM.util.Browser;
import dz.atoxyd.ABM.util.UnzipUtility;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;



public class Operations extends Activity implements Constants, ActivityThemeChangeInterface{
    final Context context=this;
	private boolean mIsLightTheme;
    private File currentDir;
    SharedPreferences mPreferences;
    private FileArrayAdapter adapter;
	ListView myListView;
    private ProgressDialog progressDialog;
    private String tip;
    private String part;
    private String nFile;
    private int nbk=1;
    private boolean iszip=false;
    private String dtitlu;
	private String processmsg = "";
	private String OldOrNewRamdisk = "";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		setTheme();
		setContentView(R.layout.operations);
        Intent intent1=getIntent();
        tip=intent1.getStringExtra("mod");
        part=intent1.getStringExtra("part");
        if(tip.equalsIgnoreCase("kernel"))
			{
				dtitlu=getString(R.string.kernel);
			}
        else if(tip.equalsIgnoreCase("recovery"))
			{
				dtitlu=getString(R.string.recovery);
			}
		else if(tip.equalsIgnoreCase("unpackramdisk"))
		{
			dtitlu=getString(R.string.unpack);
		}
		else if(tip.equalsIgnoreCase("repackramdisk"))
		{
			dtitlu=getString(R.string.repack);
		}
		
		if(tip.equalsIgnoreCase("repackramdisk")){
			currentDir = new File(UnpackHomeDir);
		}
		else{
			currentDir = new File(mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath()));
		}
		
		myListView = (ListView) findViewById(R.id.OperationsListView);
		if(tip.equalsIgnoreCase("repackramdisk")) {
			adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, false,true));
		}
		else{
			adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, true, true));
		}
		myListView.setAdapter(adapter);

		myListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> list, View v, int position, long id)
				{
					Item o = adapter.getItem(position);
					if(o.getImage().equalsIgnoreCase("dir")){
						if(tip.equalsIgnoreCase("repackramdisk")){
							nFile = o.getPath();
							Repackdialog();
						}
						else{
							currentDir = new File(o.getPath());
							adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, true, true));
							myListView.setAdapter(adapter);

						}

					}
					else{
						nFile=currentDir+"/"+o.getName();
						iszip=o.getName().toLowerCase().endsWith(".zip");
						if(iszip){
							if(tip.equalsIgnoreCase("kernel")||tip.equalsIgnoreCase("recovery")){
								new TestZipOperation().execute();
							}
							else if(tip.equalsIgnoreCase("unpackramdisk")){
								if(!isDirUnpackedExist()){
									new TestZipOperation().execute();
								}
								else{
									delUnpackeDirdialog();
								}
							}

						}
						else if(tip.equalsIgnoreCase("kernel")||tip.equalsIgnoreCase("recovery")){
							Flasherdialog();
						}
						else if(tip.equalsIgnoreCase("unpackramdisk")){
							if(!isDirUnpackedExist()){
								Unpackdialog();
							}
							else{
								delUnpackeDirdialog();
							}
						}
						else if(tip.equalsIgnoreCase("repackramdisk")){
							Repackdialog();
						}
					}
					if(!processmsg.equalsIgnoreCase("")){
						processmsg="";
					}
				}
			});
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close) {
            finish();
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, true, true));
		myListView.setAdapter(adapter);
		
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

    @Override
    public void onBackPressed(){
		if(tip.equalsIgnoreCase("repackramdisk")){
			finish();
		}
		else if(!currentDir.getParentFile().getAbsolutePath().equalsIgnoreCase("/")){
			currentDir=currentDir.getParentFile();
			adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, true, true));
			myListView.setAdapter(adapter);
			nbk=1;
		}
		else{
			if(nbk==2){
				finish();
			}
			else{
				nbk++;
				Toast.makeText(context,getString(R.string.bkexit), Toast.LENGTH_SHORT).show();
			}
		}
	}


/***************
TestZipOperation
****************/
    private class TestZipOperation extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            final UnzipUtility unzipper = new UnzipUtility();
            try{
                return unzipper.testZip(nFile,tip);
            }
            catch (Exception e) {
                Log.d(TAG,"ZIP error: "+nFile);
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if(!result){
               Toast.makeText(context, getString(R.string.bad_zip), Toast.LENGTH_LONG).show();
                return;
            }
			if(tip.equalsIgnoreCase("unpackramdisk")){
				Unpackdialog();
			}
			if(tip.equalsIgnoreCase("kernel") || tip.equalsIgnoreCase("recovery")){
				Flasherdialog();
			}
        }
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, getString(R.string.verify));
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

/**************
FlashOperation
*********†*****/
    private class FlashOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            final StringBuilder sb = new StringBuilder();
            final String dn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/tmp";

            if(tip.equalsIgnoreCase("kernel")){
                sb.append(context.getFilesDir()+"/busybox rm -rf /data/dalvik-cache/*;\n");
                sb.append(context.getFilesDir()+"/busybox rm -rf /cache/*;\n");
                if(iszip){
                    try{
                        new UnzipUtility().unzipfile(nFile,dn, new String[]{"boot.img",".ko"});
                    }
                    catch (Exception e) {
                        Log.d(TAG,"unzip error: "+nFile);
                        e.printStackTrace();
                        return "";
                    }
                    nFile=dn+"/boot.img";
                    File destDir = new File(dn+"/system/lib/modules");
                    File[]dirs = destDir.listFiles(
                            new FileFilter() {
                                @Override
                                public boolean accept(File file) {
                                    return file.getName().toLowerCase().endsWith(".ko");
                                }
                            }
                    );
                    if((dirs!=null)&&(dirs.length>0)){
                        sb.append(context.getFilesDir()+"/busybox mount -o rw,remount /system;\n");
                        sb.append(context.getFilesDir()+"/busybox rm -rf /system/lib/modules/*.ko;\n");
                        for(File ff: dirs){
                            if(ff.getName().toLowerCase().endsWith(".ko")){
                                sb.append(context.getFilesDir()+"/busybox cp ")
								  .append(dn)
								  .append("/system/lib/modules/")
								  .append(ff.getName())
								  .append(" /system/lib/modules/")
								  .append(ff.getName())
								  .append(";\n");
                                sb.append(context.getFilesDir()+"/busybox chmod 644 ")
								  .append("/system/lib/modules/")
								  .append(ff.getName())
								  .append(";\n");
                            }
                        }
                        sb.append(context.getFilesDir()+"/busybox mount -o ro,remount /system;\n");
                    }
                    sb.append(context.getFilesDir()+"/busybox dd if=")
					  .append(nFile).append(" of=")
					  .append(part).append("\n");
                    sb.append(context.getFilesDir()+"/busybox rm -rf ")
					  .append(dn)
					  .append("/*;\n");
                }
                else{
                    if(currentDir.getAbsolutePath().contains(TAG)){
                        File[]dirs = currentDir.listFiles(
                                new FileFilter() {
                                    @Override
                                    public boolean accept(File file) {
                                        return file.getName().toLowerCase().endsWith(".ko");
                                    }
                                }
                        );
                        if((dirs!=null)&&(dirs.length>0)){
                            sb.append(context.getFilesDir()+"/busybox mount -o rw,remount /system;\n");
                            sb.append(context.getFilesDir()+"/busybox rm -rf /system/lib/modules/*.ko;\n");
                            for(File ff: dirs){
                                if(ff.getName().toLowerCase().endsWith(".ko")){
                                    sb.append(context.getFilesDir()+"/busybox cp \"")
									  .append(currentDir.getAbsolutePath())
									  .append("/")
									  .append(ff.getName())
									  .append("\" /system/lib/modules/")
									  .append(ff.getName())
									  .append(";\n");
                                    sb.append(context.getFilesDir()+"/busybox chmod 644 ")
									  .append("/system/lib/modules/")
									  .append(ff.getName())
									  .append(";\n");
                                }
                            }
                            sb.append(context.getFilesDir()+"/busybox mount -o ro,remount /system;\n");
                        }
                    }
                    sb.append(context.getFilesDir()+"/busybox dd if=\"")
					  .append(nFile)
					  .append("\" of=")
					  .append(part)
					  .append(";\n");
                }

            }
            else{
                if(iszip){
                    try{
                        new UnzipUtility().unzipfile(nFile,dn, new String[]{"recovery.img"});
                    }
                    catch (Exception e) {
                        Log.d(TAG,"unzip error: "+nFile);
                        e.printStackTrace();
                        return "";
                    }
                    nFile=dn+"/recovery.img";
                    sb.append(context.getFilesDir()+"/busybox dd if=")
					  .append(nFile)
					  .append(" of=")
					  .append(part)
					  .append(";\n");
                    sb.append(context.getFilesDir()+"/busybox rm -rf ")
					  .append(dn)
					  .append("/*;\n");
                }
                else{
                    sb.append(context.getFilesDir()+"/busybox dd if=")
					  .append(nFile)
					  .append(" of=")
					  .append(part)
					  .append(";\n");
                }
            }
            Helpers.RunExecCmd(sb,true);
            return tip;
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.equalsIgnoreCase("kernel")){
                mPreferences.edit().putBoolean("booting",true).commit();
                new CMDProcessor().su.runWaitFor("reboot");
            }
            else if(result.equalsIgnoreCase("recovery")){
                mPreferences.edit().putBoolean("booting",true).commit();
                new CMDProcessor().su.runWaitFor("reboot recovery");
            }
            else{
                if (progressDialog != null) progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, getString(R.string.wait));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
	
/*******************
UnpackRepackOperation
*********†**********/
	private class UnpackRepackOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params){
			final File tmp = new File(nFile);
			String oldnFile = nFile;
            final StringBuilder sb = new StringBuilder();
			
            final String dn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG;
			if(!new File(dn).exists()){
				new File(dn).mkdir();
			}
			
			final String newRMd=dn+"/newImage";
			if(!new File(newRMd).exists() ){
				new File(newRMd).mkdir();
			}
				
            if(tip.equalsIgnoreCase("unpackramdisk")){
				if(iszip){
					if(!isDirUnpackedExist()){
						try{
							new UnzipUtility().unzipfile(nFile,dn, new String[]{"boot.img"});
						}
                        catch (Exception e) {
                        Log.d(TAG,"unzip error: "+nFile);
                        e.printStackTrace();
                        return "";
						}
                        nFile=dn+"/boot.img";
                        sb.append("cd ")
						  .append(context.getFilesDir())
						  .append(";\n");
						sb.append("./mkboot ")
						  .append(nFile)
						  .append(" ")
						  .append(UnpackHomeDir)
						  .append("/")
						  .append(tmp.getName().replace(".zip","_unpacked"))
						  .append(";\n");
						sb.append(context.getFilesDir()+"/busybox chmod 777 ")
						  .append(UnpackHomeDir)
						  .append("/")
						  .append(tmp.getName().replace(".zip","_unpacked"))
						  .append(";\n");
					    sb.append(context.getFilesDir()+"/busybox rm -rf ").append(dn).append("/*;\n");
						nFile=oldnFile;
					}
					else{
						sb.append(context.getFilesDir()+"/busybox rm -rf ")
							.append(UnpackHomeDir)
							.append("/")
							.append(tmp.getName().replace(".zip","_unpacked"))
							.append(";\n");
					}
						
				}
				else{ /* Not a Zip File */
					if(!isDirUnpackedExist()){
						sb.append("cd ")
						    .append(context.getFilesDir())
							.append(";\n");
						sb.append("./mkboot ")
							.append(nFile)
							.append(" ")
							.append(UnpackHomeDir)
							.append("/")
							.append(tmp.getName().replace(".img","_unpacked"))
							.append(";\n");
						sb.append(context.getFilesDir()+"/busybox chmod 777 ")
							.append(UnpackHomeDir)
							.append("/")
							.append(tmp.getName().replace(".img","_unpacked"))
							.append(";\n");
					}
					else{
						sb.append(context.getFilesDir()+"/busybox rm -rf ")
							.append(UnpackHomeDir)
							.append("/")
							.append(tmp.getName().replace(".img","_unpacked"))
							.append(";\n");
					}
				}
            }
			else if(tip.equalsIgnoreCase("repackramdisk")){
				sb.append("cd ")
					.append(context.getFilesDir())
					.append(";\n");
				sb.append("./mkboot ")
					.append(nFile)
					.append(" ")
					.append(newRMd)
					.append("/")
					.append("new_")
					.append(tmp.getName().replace("_unpacked",""))
					.append(".img;\n");
			}
			processmsg=Helpers.RunExecCmd(sb,false);
            return tip;
        }

        @Override
        protected void onPostExecute(String result) {
			if (progressDialog != null) {
                progressDialog.dismiss();
            }
			if((result==null)||!result.equals("nok")){
				if(tip.equalsIgnoreCase("unpackramdisk")){
					if(isDirUnpackedExist()){
						MyOutMsgdialog(processmsg);
					}
					else{
						Toast.makeText(context, getString(R.string.del_unpacked_dir_ok), Toast.LENGTH_LONG).show();
					}
				}		
				else if(tip.equalsIgnoreCase("repackramdisk")){
					MyOutMsgdialog(processmsg);
				}
			}
		}

        @Override
        protected void onPreExecute() {
			progressDialog = ProgressDialog.show(context, null, getString(R.string.wait));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
	
/*********************
dialog OnClickListener
**************†*******/
    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            dialog.cancel();
			if(tip.equalsIgnoreCase("unpackramdisk")||tip.equalsIgnoreCase("repackramdisk")){
				if(processmsg.equalsIgnoreCase("")){
					new UnpackRepackOperation().execute();
				}
				else{
					Helpers.cpTextToclipoard(context, processmsg);
					Toast.makeText(context, getString(R.string.copy_to_clipboard_ok), Toast.LENGTH_LONG).show();
				}
				
			}
			else {
				new FlashOperation().execute();
			}
        }
    }

    private void Flasherdialog(){
		Mydialog(new File(nFile).getName()+"\n"+getString(R.string.flash_info,part)+" "+tip.toUpperCase()+"\n\n"+getString(R.string.system_will_reboot));
    }
	
	private void Unpackdialog(){
		Mydialog(getString(R.string.unpacking, new File(nFile).getName()));
    }
	
	private void Repackdialog(){
		Mydialog(getString(R.string.repacking, new File(nFile).getName()));
	}
	
	private void delUnpackeDirdialog(){
		if(iszip){
			Mydialog(getString(R.string.del_unpacked_dir, new File(nFile).getName().replace(".zip", "_unpacked")));
		}
		else{
			Mydialog(getString(R.string.del_unpacked_dir, new File(nFile).getName().replace(".img", "_unpacked")));
		}
	}
	
	private void Mydialog(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dtitlu)
			.setMessage(message)
			.setNegativeButton(getString(R.string.cancel),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			})
			.setPositiveButton(getString(R.string.yes),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (theButton != null) {
            theButton.setOnClickListener(new CustomListener(alertDialog));
        }	
    }
	
	private void MyOutMsgdialog(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dtitlu)
			.setMessage(message)
			.setNegativeButton(getString(R.string.cancel),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			})
			.setPositiveButton(getString(R.string.copy_to_clipboard),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (theButton != null) {
            theButton.setOnClickListener(new CustomListener(alertDialog));
        }	
    }
	
	private boolean isDirUnpackedExist(){
		final File tmp = new File(nFile);
		File[]dirs = new File(UnpackHomeDir).listFiles();
		if(iszip){
			try{
				assert dirs != null;
				for(File ff: dirs){
					if(ff.isDirectory()){
						if (ff.getName().equalsIgnoreCase(tmp.getName().replace(".zip","_unpacked"))){
							return true;
						}
					}
				}
			}
			catch(Exception e){
			}
		}
		else{
			try{
				assert dirs != null;
				for(File ff: dirs){
					if(ff.isDirectory()){
						if (ff.getName().equalsIgnoreCase(tmp.getName().replace(".img","_unpacked"))){
							return true;
						}
					}
				}
			}
			catch(Exception e){
			}
		}
		return false;
	}
}
    
