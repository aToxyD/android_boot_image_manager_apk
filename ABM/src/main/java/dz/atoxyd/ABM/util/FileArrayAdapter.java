package dz.atoxyd.ABM.util;

//original author: atoxyd 
//modified by: ........

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import dz.atoxyd.ABM.R;

import java.io.File;
import java.util.List;



public class FileArrayAdapter extends ArrayAdapter<Item>{

    private File currentDir;
	private Context c;
    private int id;
    private List<Item>items;
	
	private final static int ICON_FOLDER = R.drawable.ic_folder;
    private final static int ICON_FILE = R.drawable.ic_file;
	

    public FileArrayAdapter(Context context, int textViewResourceId,List<Item> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }
	
    public Item getItem(int i){
        return items.get(i);
    }
	
    public void setItem(Item item,String date){
        item.setDate(date);
        notifyDataSetChanged();
    }
	
    @Override
    public View getView(int position, View row, ViewGroup parent) {

        if (row == null) {
            LayoutInflater mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = mInflater.inflate(id, parent,false);
        }

        final Item item = items.get(position);
        if (item != null) {
			ImageView image1 = (ImageView) row.findViewById(R.id.item_Image);
            TextView text1 = (TextView) row.findViewById(R.id.item_Name);
            TextView text2 = (TextView) row.findViewById(R.id.item_Date);
            TextView text3 = (TextView) row.findViewById(R.id.item_Size);

			if(image1!=null){
				if(item.getPath()==null){
                    image1.setVisibility(View.GONE);
                }
                else{
                    currentDir = new File(item.getPath());
				    int icon = currentDir.isDirectory() ? ICON_FOLDER : ICON_FILE;
				    image1.setImageResource(icon);
				}
			}
            if(text1!=null){
                if(item.getImage().equalsIgnoreCase("dir")){
					text1.setTypeface(null, Typeface.BOLD);
				}
                else{
					text1.setTypeface(null, Typeface.NORMAL);}
                    text1.setText(item.getName());
            }
			if(text2!=null){
                if(item.getDate()==null){
                    text2.setVisibility(View.GONE);
                }
				else if(item.getName().equalsIgnoreCase("..")){
					text2.setText(item.getData());
                    text2.setVisibility(View.VISIBLE);
				}
                else{
                    text2.setText(item.getDate());
                    text2.setVisibility(View.VISIBLE);
                }
            }
            if(text3!=null){
                if(item.getImage().equalsIgnoreCase("dir")){
					text3.setVisibility(View.GONE);
                }
                else{
                    text3.setText(item.getData());
                    text3.setVisibility(View.VISIBLE);
                }
            } 
		}
        return row;
    }
}
