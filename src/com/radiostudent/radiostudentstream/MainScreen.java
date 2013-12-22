package com.radiostudent.radiostudentstream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainScreen extends Activity {

	private boolean isInForeground;
    private Button buttonPlay;
    private ProgressBar progBar;
    private TextView lblTop;
    private TextView lblWeb;
    private boolean playing = false;
    private AudioManager audioManager;
    private SeekBar sBar;
    private int sdkVersion = android.os.Build.VERSION.SDK_INT;
	private final Context c = this;
	private final Activity a = this;
	private JSONArray jsonFeed;
	private Vector<String[]> feedNodes = new Vector<String[]>();
    private final String RS_FEED = "http://radiostudent.si/json-mobile";
	
    private PlayServiceReceiver receiver_playing;
    private NetworkStateListener receiver_network;
    
	private Runnable showProgressBar = new Runnable() {
		@Override
		public void run() {
			progBar.setVisibility(View.VISIBLE);
			buttonPlay.setEnabled(false);
			if(sdkVersion < android.os.Build.VERSION_CODES.JELLY_BEAN) {
	    		buttonPlay.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.logo_dark));
	    	}
	    	else {
	    		buttonPlay.setBackground(c.getResources().getDrawable(R.drawable.logo_dark));
	    	}
		}
	};
		
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	if (isNetworkAvailable())
    	{
        	if(buttonPlay == null) {
        		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        		setContentView(R.layout.activity_main_screen);
        		runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initializeUIElements();
					}
				});
				new AsyncTask<Void, Void, Void>(){
	    			@Override
	    			protected Void doInBackground(Void... params) {
	    				loadFeed();
	    				return null;
	    			}
	            }.execute();
	        	receiver_playing = new PlayServiceReceiver();
	            IntentFilter i_f1 = new IntentFilter();
	            i_f1.addAction(RadioStreamService.ACTION_PLAYING);
	            registerReceiver(receiver_playing, i_f1);
        	}
    	} 
    	else {
    		setContentView(R.layout.no_network);
    	}
    	receiver_network = new NetworkStateListener();
        IntentFilter i_f2 = new IntentFilter();
        i_f2.addAction(com.radiostudent.radiostudentstream.NetworkStateReceiver.ACTION_NETWORK);
        registerReceiver(receiver_network, i_f2);
    	super.onCreate(savedInstanceState);
	}
    
    @Override
    protected void onStart() {
    	isInForeground = true;
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	isInForeground = false;
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	if(receiver_network != null)
    		unregisterReceiver(receiver_network);
		if(receiver_playing != null)
			unregisterReceiver(receiver_playing);
    	super.onDestroy();
    }
    
    private void feedUpdateGUI() {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int arr_size = feedNodes.size();
		    	ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();
		    	for(int i=0; i<arr_size; i++) {
		    		HashMap<String, String> map = new HashMap<String, String>();
		    		String[] node = feedNodes.get(i);
		            map.put("title", node[0]);
		            map.put("subtitle", node[5]);
		            //map.put("image", node[3]);
		            arrList.add(map);
		    	}
				ListView feedLv = (ListView)findViewById(R.id.feedListView);
				LazyAdapter adapter = new LazyAdapter(a, arrList);
				feedLv.setAdapter(adapter);
				lblWeb.setVisibility(View.VISIBLE);
			}
		});
    }

    private void initializeUIElements() {
    	progBar = (ProgressBar) findViewById(R.id.progb1);
        buttonPlay = (Button) findViewById(R.id.button1);
        lblWeb = (TextView)findViewById(R.id.textViewWeb);
        sBar = (SeekBar)findViewById(R.id.volumeControl1);
        sBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)); 
        sBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
        {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) 
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) 
            {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) 
            {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });
        buttonPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(playing)
					stopPlaying();
				else {
					Handler show_progress_h = new Handler();
					show_progress_h.post(showProgressBar);
					new AsyncTask<Void, Void, Void>(){
						@Override
						protected Void doInBackground(Void... arg0) {
							startPlaying();
							return null;
						} 
		            }.execute();					
				}
			}
		});
        lblTop = (TextView)findViewById(R.id.textView2);
        ListView lv = (ListView)findViewById(R.id.feedListView);
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        	  String url = "http://radiostudent.si"+feedNodes.get(position)[4];
        	  Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	  startActivity(browserIntent);
          }
        });
    }
    
    private void loadFeed() {
    	DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
    	HttpPost httppost = new HttpPost(RS_FEED);
    	httppost.setHeader("Content-type", "application/json");
    	InputStream inputStream = null;
    	String result = null;
    	try {
    	    HttpResponse response = httpclient.execute(httppost);           
    	    HttpEntity entity = response.getEntity();
    	    inputStream = entity.getContent();
    	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
    	    StringBuilder sb = new StringBuilder();
    	    String line = null;
    	    while ((line = reader.readLine()) != null)
    	        sb.append(line + "\n");
    	    result = sb.toString();
    	    final String result2 = result;
    	    Looper.prepare();
    	    JSONObject temp_json_obj = new JSONObject(result2);
    	    jsonFeed = temp_json_obj.getJSONArray("nodes");
			for(int i=0; i<jsonFeed.length(); i++) {    	    	
    	    	JSONObject obj = jsonFeed.getJSONObject(i);
    	    	JSONObject sub_obj = obj.getJSONObject("node");
    	    	String title = sub_obj.getString("title");
    	    	String mb_date = sub_obj.getString("mb_date");
    	    	String mb_section = sub_obj.getString("mb_section");
    	    	String mb_image = sub_obj.getString("mb_image");
    	    	String mb_link = sub_obj.getString("mb_link");
    	    	String mb_subtitle = sub_obj.getString("mb_subtitle");
    	    	String[] vals = new String[6];
    	    	vals[0] = title;
    	    	vals[1] = mb_date;
    	    	vals[2] = mb_section;
				vals[3] = mb_image;
				vals[4] = mb_link;
				vals[5] = mb_subtitle;
    	    	feedNodes.add(vals);
    	    }
			feedUpdateGUI();
    	} 
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    	catch (Exception e) { 
    		e.printStackTrace();
    	}
    	finally {
    	    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
    	}
    }
    
    private void startPlaying() {
    	Intent i_srv = new Intent(this, RadioStreamService.class);
    	i_srv.putExtra("action", "play");
    	try {
    		startService(i_srv);
    	}
    	catch(Exception e) {
    		
    	}
    }

    private void stopPlaying() {
    	Intent i_srv = new Intent(this, RadioStreamService.class);
    	i_srv.putExtra("action", "stop");
    	try {
    		startService(i_srv);
    	}
    	catch(Exception e) {
    		
    	}
    	playingStopped();
    }
    
    private void playingStopped() {
    	if(sdkVersion < android.os.Build.VERSION_CODES.JELLY_BEAN) {
    		buttonPlay.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.logo_light));
    	}
    	else {
    		buttonPlay.setBackground(c.getResources().getDrawable(R.drawable.logo_light));
    	}
    	lblTop.setText(R.string.play_text);
        playing = false;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private class NetworkStateListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final Intent i_temp = intent;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(i_temp.hasExtra("n_status")) {
						String status_str = i_temp.getStringExtra("n_status");
						Toast.makeText(c, status_str, Toast.LENGTH_LONG).show();
						if(status_str.equals("Connected") || status_str.equals("Disconnected")) {
							network_reinit_activity();
						}
						else if(status_str.equals("Connecting")) {
							showProgressBar.run();
						}
					}
				}
			});
		}
	}
	
	private void network_reinit_activity() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Activity mainAct = (Activity)c;
				Intent new_i = new Intent(c, MainScreen.class);
				mainAct.finish();
				Intent srv_i = new Intent(c, RadioStreamService.class);
				stopService(srv_i);
				if(isInForeground)
					startActivity(new_i);
			}
		}).run();
	}
	
	private class PlayServiceReceiver extends BroadcastReceiver {		 
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			//if(arg1.hasExtra("event") arg1.getStringExtra("event").equals("playing")) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progBar.setVisibility(View.INVISIBLE);
						buttonPlay.setEnabled(true);
						lblTop.setText(R.string.pause_text);
						playing = true;
					}
				});
			//}
		} 
	}
}
