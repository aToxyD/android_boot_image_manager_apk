package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.Helpers;
import dz.atoxyd.ABM.util.Item;
import dz.atoxyd.ABM.util.FileArrayAdapter;
import dz.atoxyd.ABM.util.Browser;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;

import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends Activity implements Constants, ActivityThemeChangeInterface{
	
	final Context context=this;
	private PreferenceChangeListener mPreferenceListener;
	private SharedPreferences mPreferences;
	private boolean mIsLightTheme;
	public static Boolean thide=false;
    public static boolean is_restored=false;
	private boolean pref_changed=false;
	
	private FileArrayAdapter adapter;
	ListView myListView;
	ScrollView myScrollView;
	private File currentDir;
	private int nbk=1;
	private boolean needRefresh = false;
	
	private FloatingActionMenu menu;
	private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
	private FloatingActionButton fab4;
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		setTheme();
        setContentView(R.layout.main_activity);
		
		if(savedInstanceState==null) {
            checkForSu();
        }
		
		mPreferenceListener = new PreferenceChangeListener();
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);

		final String dn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG;
		if(!new File(dn).exists()){
			new File(dn).mkdir();
		}

		final String fn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/devices.xml";
		if(!new File(fn).exists()){
			Helpers.get_rawFile(context);
		}
		
		myScrollView = (ScrollView) findViewById(R.id.no_project_text_scroll);
		myListView = (ListView) findViewById(R.id.projectListView);
		
		final String b = mPreferences.getString("my_data_ready", "");
		if (b.equals("true")) {
			if(!new File(UnpackHomeDir).exists()){
				creatUnpackHomeDir();
			}
			currentDir= new File(UnpackHomeDir);
			if (currentDir.list().length > 0){
				adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, false, false));
				myScrollView.setVisibility(View.GONE);
				myListView.setAdapter(adapter);
				myListView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> list, View v, int position, long id){
							Item o = adapter.getItem(position);
							currentDir = new File(o.getPath());
							if(o.getImage().equalsIgnoreCase("dir")){
								if(!currentDir.getName().equalsIgnoreCase("")){
									if(currentDir.getAbsolutePath().equalsIgnoreCase(UnpackHomeDir)){
										adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, false, false));
										myListView.setAdapter(adapter);
									}
									else{
										adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, true, false));
										myListView.setAdapter(adapter);
									}
								}
							}else{
								try
								{
									if (isTextFile(currentDir.getAbsolutePath()))
									{
										try
										{
											Intent texteditor = new Intent(context, TextEditor.class);
											texteditor.putExtra("fileAbsolutePath", o.getPath());
											startActivity(texteditor);
										}
										catch (Exception e)
										{
											//Log.e(TAG,"Error launching TextEditor activity");
										}
									}
									else Toast.makeText(context,"Sorry you can only edite text file", Toast.LENGTH_LONG).show();
								}
								catch (Exception e)
								{}

							}
						}
					});

				myListView.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> list, View v, int position, long id){
							Item o = adapter.getItem(position);
							currentDir = new File(o.getPath());
							if(o.getImage().equalsIgnoreCase("dir")){
								if(!currentDir.getName().equalsIgnoreCase("")){
									if(currentDir.getParentFile().getAbsolutePath().equalsIgnoreCase(UnpackHomeDir)){
										AlertDialog.Builder builder = new AlertDialog.Builder(context);
										builder.setMessage(getString(R.string.del_project_dir, currentDir.getName()))
											.setNegativeButton(getString(R.string.cancel),
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,int id) {
													dialog.cancel();
												}
											})
											.setPositiveButton(getString(R.string.yes),
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int id) {
													final StringBuilder sb = new StringBuilder();
													sb.append(context.getFilesDir()+"/busybox rm -rf ")
														.append(currentDir)
														.append(";\n");
													Helpers.RunExecCmd(sb,true);
													Toast.makeText(context,getString(R.string.del_project_dir_ok, currentDir.getName()), Toast.LENGTH_LONG).show();
													needRefresh = true;
													onResume();
												}
											});
										AlertDialog alertDialog = builder.create();
										alertDialog.show();
									}
								}
							}
							return true;
						}
					});
			}
			else{
				myListView.setVisibility(View.GONE);
			}
		}
		
        menu = (FloatingActionMenu) findViewById(R.id.menu);

        menu.showMenuButton(true);
		
		fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
		fab4 = (FloatingActionButton) findViewById(R.id.fab4);

        fab1.setOnClickListener(clickListener);
        fab2.setOnClickListener(clickListener);
        fab3.setOnClickListener(clickListener);
		fab4.setOnClickListener(clickListener);
	}
	
	private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab1:
                    try{
						Intent flash = new Intent(context, FlasherActivity.class);
						flash.putExtra("mod","kernel");
						startActivity(flash);
					}
					catch(Exception e){
						Log.e(TAG,"Error launching flasher activity");
					}
					break;
                case R.id.fab2:
                    try{
						Intent flash = new Intent(context, FlasherActivity.class);
						flash.putExtra("mod","recovery");
						startActivity(flash);
					}
					catch(Exception e){
						Log.e(TAG,"Error launching flasher activity");
					}
					break;
                case R.id.fab3:
					try{
						Intent repack = new Intent(context, Operations.class);
						repack.putExtra("mod","repackramdisk");
						startActivity(repack);
					}
					catch(Exception e){
						Log.e(TAG,"Error launching FileChooser activity");
					}
					break;
				case R.id.fab4:
					try{
						Intent unpack = new Intent(context, Operations.class);
						unpack.putExtra("mod","unpackramdisk");
						startActivityForResult(unpack, 3);
					}
					catch(Exception e){
						Log.e(TAG,"Error launching FileChooser activity");
					}
					break;
            }
        }
    };
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
	
	@Override
    public void onBackPressed(){
		if(!currentDir.getName().equalsIgnoreCase("")){
			if(currentDir.getAbsolutePath().equalsIgnoreCase(UnpackHomeDir)){
				adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, false, false));
				myListView.setAdapter(adapter);
				if(nbk==2){
					finish();
				}
				else{
					nbk++;
					Toast.makeText(context,getString(R.string.bkexit), Toast.LENGTH_LONG).show();
				}
			}
			else if(currentDir.getParentFile().getAbsolutePath().equalsIgnoreCase(UnpackHomeDir)){
				currentDir = currentDir.getParentFile();
				adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, false, false));
				myListView.setAdapter(adapter);
			}
			else{
				currentDir = currentDir.getParentFile();
				adapter = new FileArrayAdapter(context, R.layout.file_item, Browser.Fill(currentDir, context, true, false));
				myListView.setAdapter(adapter);
				nbk=1;
			}
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
			case R.id.app_about:
				LayoutInflater factory = LayoutInflater.from(context);
				final View AboutDialog = factory.inflate(R.layout.about_dialog,
														 (ViewGroup) findViewById(R.id.tabhost));
                TabHost tabs = (TabHost) AboutDialog.findViewById(R.id.tabhost);
                tabs.setup();
                TabSpec tspec1 = tabs.newTabSpec("about");
                tspec1.setIndicator(getString(R.string.version));
                tspec1.setContent(R.id.text_about_scroll);
                TextView text = ((TextView) AboutDialog.findViewById(R.id.text_version));
                text.setMovementMethod(LinkMovementMethod.getInstance());
                text.append("\n" + getString(R.string.about_version) + "\n" + getString(R.string.about_build_date));
                tabs.addTab(tspec1);
                TabSpec tspec2 = tabs.newTabSpec("credits");
                tspec2.setIndicator(getString(R.string.credits));
                tspec2.setContent(R.id.text_credits_scroll);
                ((TextView) AboutDialog.findViewById(R.id.text_credits))
					.setMovementMethod(LinkMovementMethod.getInstance());
                tabs.addTab(tspec2);
                TabSpec tspec3 = tabs.newTabSpec("thanks");
                tspec3.setIndicator(getString(R.string.thanks));
                tspec3.setContent(R.id.text_thanks_scroll);
                ((TextView) AboutDialog.findViewById(R.id.text_thanks))
					.setMovementMethod(LinkMovementMethod.getInstance());
                tabs.addTab(tspec3);

				AlertDialog.Builder builder=new AlertDialog.Builder(context)
					.setTitle(getString(R.string.about) + " " + getString(R.string.app_name))
					.setView(AboutDialog)
					.setIcon(R.drawable.ic_launcher)
					.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
                break;
            case R.id.app_settings:
                Intent intent = new Intent(context, AppSettings.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mPreferences.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isThemeChanged() || thide || is_restored) {
            if(thide) thide=false;
            if(is_restored) is_restored=false;
            Helpers.restartApp(this);
        }
		if(needRefresh){
			this.onCreate(null);
		}
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == 1)&&(resultCode == RESULT_OK)) {
            String r= data.getStringExtra("r");
            if(r!=null && r.equals("ok")){
                mPreferences.edit().putString("root_access", "true").commit();
				checkDataReady();
				return;
			}
		}
		if ((requestCode == 2)&&(resultCode == RESULT_OK)) {
			String s= data.getStringExtra("s");
            if(s!=null && s.equals("ok")){
				mPreferences.edit().putString("my_data_ready","true").commit();
				if(!new File(UnpackHomeDir).exists()){
					creatUnpackHomeDir();
				}
				return;
			}
		}
		if (requestCode == 3) {
			this.onCreate(null);
			return;
		}
        finish();
    }

    private void checkForSu() {
        final String b=mPreferences.getString("root_access","");
		if(b.equals("true")) {
			if(!Helpers.checkSu()) {
				Log.d(TAG, "check for su");
				Intent intent = new Intent(context, CheckSU.class);
				startActivityForResult(intent, 1);
			}
		}
		else{
			Log.d(TAG, "check for su");
			Intent intent = new Intent(context, CheckSU.class);
			startActivityForResult(intent, 1);
		}    
    }

	private void checkDataReady() {
       final String b = mPreferences.getString("my_data_ready", "");
	   if (b.equals("true")) {
			//
		}
		else{
			Intent intent = new Intent(context, getMyDataReady.class);
			startActivityForResult(intent, 2);
		}  
    }
	
	private void creatUnpackHomeDir(){
		final StringBuilder sb = new StringBuilder();
		//new File(UnpackHomeDir).mkdir();
		sb.append(context.getFilesDir()+"/busybox mkdir ")
			.append(UnpackHomeDir)
			.append(";\n");
		sb.append(context.getFilesDir()+"/busybox chmod 777 ")
			.append(UnpackHomeDir)
			.append(";\n");
		Helpers.RunExecCmd(sb,true);
		currentDir= new File(UnpackHomeDir);
	}
	
	private boolean isTextFile(String filePath) throws Exception {
		File f = new File(filePath);
		if(!f.exists())
			return false;
		FileInputStream in = new FileInputStream(f);
		int size = in.available();
		if(size > 1000)
			size = 1000;
		byte[] data = new byte[size];
		in.read(data);
		in.close();
		String s = new String(data, "ISO-8859-1");
		String s2 = s.replaceAll(
            "[a-zA-Z0-9ßöäü\\.\\*!\"§\\$\\%&/()=\\?@~'#:,;\\"+
            "+><\\|\\[\\]\\{\\}\\^°²³\\\\ \\n\\r\\t_\\-`´âêîô"+
            "ÂÊÔÎáéíóàèìòÁÉÍÓÀÈÌÒ©‰¢£¥€±¿»«¼½¾™ª]", "");
		// will delete all text signs

		double d = (double)(s.length() - s2.length()) / (double)(s.length());
		// percentage of text signs in the text
		return d > 0.95;
	}

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            pref_changed=true;
        }
    }
}
