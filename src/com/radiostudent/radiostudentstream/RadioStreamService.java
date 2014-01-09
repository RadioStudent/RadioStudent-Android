package com.radiostudent.radiostudentstream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

public class RadioStreamService extends Service {

	private int sdkVersion = android.os.Build.VERSION.SDK_INT;
    private MediaPlayer player;
    private final String RS_STREAM_URL = "http://kruljo.radiostudent.si:8000/hiq.m3u";
    //private final String RS_STREAM_URL = "http://kruljo.radiostudent.si:8000/loq.m3u";
	private final Context c = this;
	final static String ACTION_PLAYING = "playing";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
    public void onCreate() {
	    //Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
	}
	
    @Override
	public void onDestroy() {
        //Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        //Log.d(TAG, "onDestroy");
    	if(player != null && player.isPlaying()) {
    		player.stop();
    		player.release();
        }
        player=null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	String action = "";
    	if(intent != null && intent.hasExtra("action")) {
    		action = intent.getStringExtra("action");
        	if(action.equals("play")) {
            	//Toast.makeText(this, "Loading", Toast.LENGTH_LONG).show();
    	    	try {
    	        	new AsyncTask<Void, Void, Void>(){
    	    			@Override
    	    			protected Void doInBackground(Void... params) {
    	    	        	if(player == null) 
    	    	        		initializeMediaPlayer();
    	    	        	else {
    	    	        		player.start();
    		    	        	Intent r1_intent = new Intent();
    		    	        	r1_intent.setAction(ACTION_PLAYING);
    		    	        	r1_intent.putExtra(ACTION_PLAYING, ACTION_PLAYING);	    	           
    		    	            sendBroadcast(r1_intent);
    	    	        	}
    	    				return null;
    	    			}
    	            }.execute();
    	        }
    	        catch (Exception e) {
    	        	e.printStackTrace();
    	        }
            }
            else if(action.equals("stop")) {
            	//Toast.makeText(this, "Stoped", Toast.LENGTH_LONG).show();
            	if(player != null)
    	        	if(player.isPlaying()) 
    	        		player.pause();
            }
    	}
    	return super.onStartCommand(intent, flags, startId);
    }
    
    private void initializeMediaPlayer() {
	    try {    	
	    	Uri u = Uri.parse(ParserM3UToURL.parse(RS_STREAM_URL, sdkVersion, c));
	        player = MediaPlayer.create(c, u);
	        /*player.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.start();
		        	Intent r1_intent = new Intent();
		        	r1_intent.setAction(ACTION_PLAYING);
		        	r1_intent.putExtra(ACTION_PLAYING, ACTION_PLAYING);	    	           
		            sendBroadcast(r1_intent);
				}
			})*/;
			player.start();
        	Intent r1_intent = new Intent();
        	r1_intent.setAction(ACTION_PLAYING);
        	r1_intent.putExtra("event", ACTION_PLAYING);
            sendBroadcast(r1_intent);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
    }
}
