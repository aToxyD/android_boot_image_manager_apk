package dz.atoxyd.ABM.util;

//original author: atoxyd 
//modified by: ........

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import dz.atoxyd.ABM.R;
import dz.atoxyd.ABM.util.Helpers;
import dz.atoxyd.ABM.util.FileArrayAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;



public class Browser {
    

    public static List<Item> Fill(File f, Context context, Boolean with_parent_dir, Boolean zip_img_only){
		
        File[]dirs = f.listFiles();
        List<Item>dir = new ArrayList<Item>();
        List<Item>fls = new ArrayList<Item>();
        try{
            assert dirs != null;
            for(File ff: dirs){
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if(ff.isDirectory()){
                    dir.add(new Item(ff.getName(),context.getString(R.string.dir),date_modify,ff.getAbsolutePath(),"dir"));
                }
                else{
					if (zip_img_only){
						if(ff.getName().toLowerCase().endsWith(".img") || ff.getName().toLowerCase().endsWith(".zip"))
							fls.add(new Item(ff.getName(),Helpers.ReadableByteCount(ff.length()), date_modify, ff.getAbsolutePath(),"file"));
					}
					else fls.add(new Item(ff.getName(),Helpers.ReadableByteCount(ff.length()), date_modify, ff.getAbsolutePath(),"file"));
				}
            }
        }
        catch(Exception e){
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
		
        if(!f.getName().equalsIgnoreCase("")){
			if(with_parent_dir){
				if(!f.getParentFile().getAbsolutePath().equalsIgnoreCase("/")){
					dir.add(0,new Item("..",context.getString(R.string.dir_parent),"",f.getParent(),"dir"));
				}
			}
		}
        return dir;
    }
}
    
