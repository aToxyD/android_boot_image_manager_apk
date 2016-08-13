package dz.atoxyd.ABM.activities;

//original author: atoxyd 
//modified by: ........

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.Constants;
import dz.atoxyd.ABM.util.ActivityThemeChangeInterface;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;



public class TextEditor extends Activity implements Constants, ActivityThemeChangeInterface{
	
	final Context context=this;
	private boolean mIsLightTheme;
    private SharedPreferences mPreferences;
	
	
    private String fileAbsolutePath;
    private EditText text = null;
	private static ActionBar actionbar;
	private CharSequence title;

	
	private boolean fromSearch = false;
	private String queryString = "";
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		setTheme();
        setContentView(R.layout.texteditor);

		text = (EditText) findViewById(R.id.texteditor);

		Intent intent = getIntent();
        fileAbsolutePath = intent.getStringExtra("fileAbsolutePath");
		
		title = new File(fileAbsolutePath).getName();
		
		actionbar = getActionBar();
		actionbar.setTitle(title);
		
		try {
	
			FileReader f = new FileReader(fileAbsolutePath);
			File file = new File(fileAbsolutePath);

			// if the file has nothing in it there will be an exception here
			// that actually isn't a problem
			if (file.length() != 0)
			{
				StringBuffer result = new StringBuffer();
				char[] buffer;
				buffer = new char[1100];	// made it bigger just in case

				int read = 0;

				do {
					read = f.read(buffer, 0, 1000);

					if (read >= 0)
					{
						result.append(buffer, 0, read);
					}
				} while (read >= 0);

				text.setText(result.toString());
				//fileEditText.setSelection(0, 0);
				//fileEditText.setLinksClickable(true);
				//fileEditText.setAutoLinkMask(Linkify.ALL);
				//fileEditText.setSelection(fileEditText.getSelectionStart(), fileEditText.getSelectionEnd());
				//fileEditText.requestFocus();
				//fileEditText.setTypeface(Typeface.MONOSPACE);
				//fileEditText.setTextSize(20.0f);
				
				/********************************
				 * line wrap */
				//value = sharedPref.getBoolean("linewrap", true);
				//fileEditText.setHorizontallyScrolling(true);
				

				// setup the scroll view correctly
				//ScrollView scroll = (ScrollView) findViewById(R.id.fileeditor_scroll);	
				//scroll.setFillViewport(true);
				//scroll.setHorizontalScrollBarEnabled(true);
			}
		} catch (Exception e) {
		}

		text.addTextChangedListener(new TextWatcher() {

				public void onTextChanged(CharSequence one, int a, int b, int c) {

					// put a little star in the title if the file is changed
					if (!isTextChanged())
					 {
						 CharSequence temp = actionbar.getTitle();
						 actionbar.setTitle("* " + temp);
					 }
				}

				// complete the interface
				public void afterTextChanged(Editable s) { }
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			});
		
		newIntent(intent);
    }
	
	@Override
	public void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		newIntent(intent);
	}

	public void newIntent(Intent intent)
	{
		setIntent(intent);

		// search action
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestions.AUTHORITY, SearchSuggestions.MODE);
			suggestions.saveRecentQuery(query, null);
			fromSearch = true;
			queryString = query;
				
		}	
	}
	
	/****************************************************************
	 * onPause()
	 * 		What happens when you pause the app */
	protected void onPause()
	{
		super.onPause();
	} // end onPause()
	
	@Override
    public void onResume() {
		super.onResume();
		myResume();
	}

	private void myResume()
	{
		try {
			text.requestFocus();

			// search search search
			if (fromSearch)
			{
				int start;
				String t = text.getText().toString().toLowerCase();

				start = t.indexOf(queryString.toLowerCase(), text.getSelectionStart()+1);
				if (start == -1)	// loop search
					start = t.indexOf(queryString.toLowerCase(), 0);

				if (start != -1)
				{
					text.setSelection(start, start + queryString.length());	
				} else {
					Toast.makeText(this, "\"" + queryString + "\" not found", Toast.LENGTH_LONG).show();
				}

				fromSearch = false;
			}
		} catch (Exception e) {
		}
	} // end onResume()
	
	public static boolean isTextChanged()	// checks if the text has been changed
	{
		
		CharSequence temp = actionbar.getTitle();
		
		try {	// was getting error on the developer site, so added this to "catch" it
		
			if (temp.charAt(0) == '*')
			{
				return true;
			}
		} catch (Exception e) {
			return false;
		} 

		return false;
	} // end isTextChanged()
	
	@Override
    public void onBackPressed(){
		if(isTextChanged()){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(getString(R.string.shouldSave))
				.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
						finish();
					}
				})
				.setPositiveButton(getString(R.string.yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						try {
							PrintWriter printWriter = new PrintWriter(fileAbsolutePath);
							printWriter.print(text.getText());
							printWriter.close();
							Toast.makeText(context,getString(R.string.saved_ok), Toast.LENGTH_SHORT).show();
							finish();
						} catch(Exception e){
							
						}
					}
				});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		else finish();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.fileeditor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case R.id.search:
				this.onSearchRequested();
				break;
			case R.id.clear_search_sug:
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context, SearchSuggestions.AUTHORITY, SearchSuggestions.MODE);
				suggestions.clearHistory();
				Toast.makeText(context, R.string.clear_search_sug_ok, Toast.LENGTH_LONG).show();
				break;
            case R.id.save:
                try {
                    PrintWriter printWriter = new PrintWriter(fileAbsolutePath);
                    printWriter.print(text.getText());
                    printWriter.close();
                    Toast.makeText(context,getString(R.string.saved_ok), Toast.LENGTH_SHORT).show();
					if (isTextChanged())
					{
						actionbar.setTitle(title);
					}
                    break;
                } catch(Exception e){
					//Log.e(TAG,"canot read file");
					break;
				}
            case R.id.close:
                finish();
                break;
        }
        return true;
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
