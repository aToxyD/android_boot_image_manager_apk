package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.CMDProcessor;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.Helpers;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class FlasherActivity extends Activity implements Constants, ActivityThemeChangeInterface{
	final Context context=this;
	LinearLayout suportinfo;
	LinearLayout insuportinfo;
	TextView deviceName;
    TextView deviceModel;
    TextView deviceBoard;
	TextView flasherInfo;
	ImageView attn;
    TextView insuportInfo ;
    Button chooseBtn,backupBtn;
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    private String partionInfo,tip,model,backupname;
    private static ProgressDialog progressDialog;
    private static boolean isdialog=false;
    private String dn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		setTheme();
        setContentView(R.layout.flasher);

		model=Build.MODEL;
		Intent intent1=getIntent();
        tip=intent1.getStringExtra("mod");
		partionInfo=getPartionInfo(model);

        dn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/backup/";

		model=Build.MODEL;
		suportinfo=(LinearLayout)findViewById(R.id.suportinfo);
		insuportinfo=(LinearLayout)findViewById(R.id.insuportinfo);
		deviceName=(TextView)findViewById(R.id.name);
        deviceModel=(TextView)findViewById(R.id.model);
        deviceBoard=(TextView)findViewById(R.id.board);
		flasherInfo=(TextView)findViewById(R.id.flashinfo);
		insuportInfo=(TextView)findViewById(R.id.insuportedinfo);
        chooseBtn=(Button) findViewById(R.id.chooseBtn);
        backupBtn=(Button) findViewById(R.id.backupBtn);
        
		if(VerifiedModel(model)){
			deviceModel.setText(model);
			deviceBoard.setText(Build.MANUFACTURER);
			deviceName.setText(Build.DEVICE);//Build.PRODUCT
			if(tip.equalsIgnoreCase("kernel")){
				flasherInfo.setText("boot.img "+getString(R.string.flash_info,partionInfo)+" "+tip.toUpperCase());
				chooseBtn.setText(getString(R.string.choose,"boot.img"));
			}
			else{
				flasherInfo.setText("recovery.img "+getString(R.string.flash_info,partionInfo)+" "+tip.toUpperCase());
				chooseBtn.setText(getString(R.string.choose,"recovery.img"));
			}
			insuportinfo.setVisibility(View.GONE);
		}
		else{
			attn=(ImageView) findViewById(R.id.attn);
			attn.setImageResource(R.drawable.ic_attn);
			insuportInfo.setText(model + ", " + getString(R.string.unsupported_info));
			suportinfo.setVisibility(View.GONE);
			chooseBtn.setVisibility(View.GONE);
			backupBtn.setVisibility(View.GONE);
		}
	
		chooseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    try{
                        Intent intent2 = new Intent(context, Operations.class);
                        intent2.putExtra("mod",tip);
                        intent2.putExtra("part",partionInfo);
                        startActivity(intent2);
                        //finish();
                    }
                    catch(Exception e){
                        Log.e(TAG,"Error launching filechooser activity");
                    }
                }
            });
		backupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //------backup------
                    LayoutInflater factory = LayoutInflater.from(context);
                    final View editDialog = factory.inflate(R.layout.backup_dialog, null);
                    final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
                    final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);

                    tn.setText(getString(R.string.backup_name));
                    backupname=makeBkName(tip);
                    tv.setText(backupname);
                    tv.addTextChangedListener(new TextWatcher() {
							public void afterTextChanged(Editable s) {
								backupname=tv.getText().toString();
							}

							public void beforeTextChanged(CharSequence s, int start, int count, int after) {
							}

							public void onTextChanged(CharSequence s, int start, int before, int count) {
							}
						});
                    AlertDialog.Builder builder=new AlertDialog.Builder(context)
						.setTitle(getString(R.string.backup)+" "+tip.toUpperCase())
						.setView(editDialog)
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.setPositiveButton(getString(R.string.backup), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (theButton != null) {
                        theButton.setOnClickListener(new CustomListener(alertDialog));
                    }
                }
            });
    }
	
	@Override
    public void onResume() {
        if(isdialog) progressDialog = ProgressDialog.show(context, null, getString(R.string.wait));
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
	
    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            if ((backupname != null) && (backupname.length() > 0)) {
                if (backupname.endsWith("/")) { backupname = backupname.substring(0, backupname.length() - 1);}
                if(!backupname.startsWith("/")) { backupname="/"+backupname; }
                if ( new File(dn+tip+backupname).exists() ){
                    Toast.makeText(context,getString(R.string.exist_file,dn+tip+backupname),Toast.LENGTH_LONG).show();
                }
                else{
                    dialog.dismiss();
                    if(!new File(dn+tip+backupname).mkdirs()){
                        Toast.makeText(context,getString(R.string.err_file,dn+tip+backupname),Toast.LENGTH_LONG).show();
                    }
                    else {
                        new backupOperation().execute();
                    }
                }
            }

        }
    }

    private static String getValue(String tag, org.w3c.dom.Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodes.item(0);
        return node.getNodeValue();
    }

    private String getPartionInfo(String this_model){
        Boolean model_detected=false;
        InputStream f;

        final String fn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/devices.xml";
        try {
            if (new File(fn).exists()){
                f = new BufferedInputStream(new FileInputStream(fn));
                Log.i(TAG,"external /"+TAG+"/devices.xml in use");
            }
            else{
                f = getResources().openRawResource(R.raw.devices);
            }
            DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc=builder.parse(f, null);
            doc.getDocumentElement().normalize();
            NodeList nList=doc.getElementsByTagName("device");
            for (int k = 0; k < nList.getLength(); k++) {
                Node node = nList.item(k);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    final String models[]=getValue("model", element).split(",");
                    for (String mi : models) {
                        if(mi.equalsIgnoreCase(this_model)){
                            partionInfo=getValue(tip, element);
							model_detected=true;
                        }
                    }
                    if(model_detected) {
                        Log.d(TAG,tip+" partition detected: "+partionInfo);
                        break;
                    }
                }
            }
            f.close();
        }
        catch (Exception e) {
            Log.e(TAG,"Error reading devices.xml");
            model_detected=false;
            e.printStackTrace();
        }
        return partionInfo;
    }

	private Boolean VerifiedModel(String thismodel){
        Boolean modelexist=false;
        InputStream f;

        final String fn=mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath())+"/"+TAG+"/devices.xml";
        try {
            if (new File(fn).exists()){
                f = new BufferedInputStream(new FileInputStream(fn));
                Log.i(TAG,"external /"+TAG+"/devices.xml in use");
            }
            else{
                f = getResources().openRawResource(R.raw.devices);
            }
            DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc=builder.parse(f, null);
            doc.getDocumentElement().normalize();
            NodeList nList=doc.getElementsByTagName("device");
            for (int k = 0; k < nList.getLength(); k++) {
                Node node = nList.item(k);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    final String models[]=getValue("model", element).split(",");
                    for (String mi : models) {
                        if(mi.equalsIgnoreCase(thismodel)){
                            modelexist=true;
                        }
                    }
                    if(modelexist) {
                        break;
                    }
                }
            }
            f.close();
        }
        catch (Exception e) {
            Log.e(TAG,"Error reading devices.xml");
            modelexist=false;
            e.printStackTrace();
        }
        return modelexist;
    }
   
    private class backupOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            final StringBuilder sb = new StringBuilder();
            if(tip.equalsIgnoreCase("kernel")){
                final File destDir = new File("/system/lib/modules");
                final File[]dirs = destDir.listFiles();
                if((dirs!=null)&&(dirs.length>0)){
                    sb.append(context.getFilesDir()+"/busybox cp -r /system/lib/modules/*.ko")
					.append(" ")
					.append(dn)
					.append(tip)
					.append(backupname)
					.append(";\n");
                }
                sb.append(context.getFilesDir()+"/busybox dd if=")
				  .append(partionInfo)
				  .append(" of=\"")
				  .append(dn)
				  .append(tip)
				  .append(backupname)
				  .append("/boot.img\";\n");
            }
            else{
                sb.append(context.getFilesDir()+"/busybox dd if=").append(partionInfo).append(" of=\"").append(dn).append(tip).append(backupname).append("/recovery.img\";\n");
            }

            Helpers.RunExecCmd(sb, true);
			return null;
        }
		
        @Override
        protected void onPostExecute(String result) {
            isdialog=false;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if((result==null)||!result.equals("nok")){
                Toast.makeText(context,getString(R.string.backup_ok),Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context,getString(R.string.backup_nok),Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, getString(R.string.wait));
            isdialog=true;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private String makeBkName(String tip){
        if(tip.equalsIgnoreCase("kernel")) {
            CMDProcessor.CommandResult cr = new CMDProcessor().sh.runWaitFor(context.getFilesDir()+"/busybox uname -r");
            if (cr.success() && cr.stdout != null && cr.stdout.length() > 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
                Date now = new Date();
                return tip + "_" + cr.stdout + "_" + formatter.format(now);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
                Date now = new Date();
                return tip + "_" + formatter.format(now);
            }
        }
        else{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
            Date now = new Date();
            return tip + "_" + formatter.format(now);
        }
    }
}
