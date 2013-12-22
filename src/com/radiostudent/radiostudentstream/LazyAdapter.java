package com.radiostudent.radiostudentstream;

import java.util.ArrayList;
import java.util.HashMap;
 
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {
	private class ViewHolder {
	    public TextView title;
	    public TextView subtitle;
	}
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        //inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //imageLoader=new ImageLoader(activity.getApplicationContext());
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        /*View vi=convertView;
        if(convertView==null)
            //vi = inflater.inflate(R.layout.list_row, null);
        	vi = inflater.inflate(R.layout.list_row, parent, false);
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView subtitle = (TextView)vi.findViewById(R.id.subtitle); // artist name
        //ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image
 
        HashMap<String, String> post;
        post = data.get(position);
        // Setting all values in listview
        if(title != null) {
		    title.setText("sample text");
		    subtitle.setText("sample text 2");
        }
        //imageLoader.DisplayImage(song.get(CustomizedListView.KEY_THUMB_URL), thumb_image);
        return vi;*/
    	
    	ViewHolder holder = new ViewHolder();
        View vi = convertView;
        if (vi == null) {
            LayoutInflater inflater = ((Activity)activity).getLayoutInflater();
            vi = inflater.inflate(R.layout.list_row, null);
            holder.title = (TextView) vi.findViewById(R.id.title);
            holder.subtitle = (TextView) vi.findViewById(R.id.subtitle);
            vi.setTag(holder);
        } else {

            holder = (ViewHolder) vi.getTag();
        }
        HashMap<String, String> post;
        post = data.get(position);
        holder.title.setText(post.get("title"));
        holder.subtitle.setText(post.get("subtitle"));
        return vi;
    }
}