package com.radiostudent.radiostudentstream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.IBinder;

public class RadioStreamService extends Service implements
OnCompletionListener, OnPreparedListener {

    private MediaPlayer mediaPlayer;
    private boolean DATA_SET = false;
    private int sdkVersion = android.os.Build.VERSION.SDK_INT;
    private final String RS_STREAM_URL = "http://kruljo.radiostudent.si:8000/hiq.m3u";
    //private final String RS_STREAM_URL = "http://kruljo.radiostudent.si:8000/loq.m3u";
	final static String ACTION_PLAYING = "playing";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
	}
	
    @Override
	public void onDestroy() {
        //Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        //Log.d(TAG, "onDestroy");
    	if(mediaPlayer != null && mediaPlayer.isPlaying()) {
    		mediaPlayer.stop();
    		mediaPlayer.release();
        }
    	mediaPlayer=null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	String action = "";
        if(intent != null && intent.hasExtra("action")) {
            action = intent.getStringExtra("action");
            if(action.equals("play")) {
            	if(DATA_SET) {
            		mediaPlayer.start();
            		doSendBroadcast();
            	}
            	else {
            		if(sdkVersion < 9)
            			parseM3uUrlAndPrepare(RS_STREAM_URL);
            		else
            			parseM3uUrlAndPrepare_new(RS_STREAM_URL);
            	}
			}
            else if(action.equals("stop")) {
                if(mediaPlayer != null)
                 if(mediaPlayer.isPlaying())
                	 mediaPlayer.pause();
            }
        }        
	    return START_STICKY;
	}    

    private void parseM3uUrlAndPrepare(final String url) {
    	AsyncTask<String, Integer, String> asyn = new  AsyncTask<String, Integer, String>(){
    		HttpClient httpClient;
	        HttpGet getRequest;
	        HttpResponse httpResponse = null;
	        String filePath = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                httpClient = new DefaultHttpClient();
                getRequest = new HttpGet(url);
            }

	        @Override
	        protected String doInBackground(String... params) {
                try {
                    httpResponse = httpClient.execute(getRequest);
                } 
                catch (ClientProtocolException e) {
                    e.printStackTrace();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
                if(httpResponse != null) {
	                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	                    // ERROR MESSAGE
	                } 
	                else {
	                    InputStream inputStream = null;
	                    try {
	                        inputStream = httpResponse.getEntity().getContent();
	                    }
	                    catch (IllegalStateException e1) {
	                        e1.printStackTrace();
	                    } 
	                    catch (IOException e1) {
	                        e1.printStackTrace();
	                    } 
	                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	                    String line;
	                    try {
	                        while ((line = bufferedReader.readLine()) != null) {
	                            //Log.v("PLAYLISTLINE", "ORIG: " + line);
	                            if (line.startsWith("#")) { // Metadata
	
	                            } else if (line.length() > 0) {
	                                filePath = "";
	
	                                if (line.startsWith("http://")) { // Assume it's a full URL
	                                    filePath = line;
	                                } 
	                                else { // Assume it's relative
	                                    try{
	                                    	filePath = getRequest.getURI().resolve(line).toString();
	                                    }
	                                    catch(IllegalArgumentException e){
	
	                                    }
	                                    catch(Exception e){
	
	                                    }
	                                }
	                            }
	                        }
	                    } 
	                    catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                    try {
	                        inputStream.close();
	                    } 
	                    catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                }
                }
	            return filePath;
	        }

	        @Override
	        protected void onPostExecute(String filePath) {
	            try {
		            mediaPlayer.setDataSource(filePath);
		            DATA_SET = true;
		            mediaPlayer.prepareAsync(); //this will prepare file a.k.a buffering
		
		        } catch (IllegalArgumentException e) {
		            e.printStackTrace();
		        } catch (IllegalStateException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}
		};
	    asyn.execute("");
	}
 
    private void parseM3uUrlAndPrepare_new(final String url) {
    	AsyncTask<String, Integer, String> asyn = new  AsyncTask<String, Integer, String>(){
    		URL the_url;
	        HttpURLConnection conn;
	        String filePath = "";
	        InputStream inputStream;
	        HttpGet getRequest;
	        
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
					the_url = new URL(url);
					conn = (HttpURLConnection) the_url.openConnection(Proxy.NO_PROXY);
					getRequest = new HttpGet(url);
				}
                catch (MalformedURLException e) {
					e.printStackTrace();
				} 
                catch (IOException e) {
					e.printStackTrace();
				}
            }

	        @Override
	        protected String doInBackground(String... params) {
                if(conn != null) {
                    try {
                    	inputStream = new BufferedInputStream(conn.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (line.startsWith("#")) { 

                            } 
                            else if (line.length() > 0) {
                                filePath = "";
                                if (line.startsWith("http://")) { // Assume it's a full URL
                                    filePath = line;
                                } 
                                else { // Assume it's relative
                                    try{
                                    	filePath = getRequest.getURI().resolve(line).toString();
                                    }
                                    catch(IllegalArgumentException e){
                                    	e.printStackTrace();
                                    }
                                    catch(Exception e){
                                    	e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } 
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        inputStream.close();
                    } 
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
	            return filePath;
	        }

	        @Override
	        protected void onPostExecute(String filePath) {
	            try {
		            mediaPlayer.setDataSource(filePath);
		            DATA_SET = true;
		            mediaPlayer.prepareAsync(); //this will prepare file a.k.a buffering
		
		        } 
	            catch (IllegalArgumentException e) {
		            e.printStackTrace();
		        }
	            catch (IllegalStateException e) {
		            e.printStackTrace();
		        }
	            catch (IOException e) {
		            e.printStackTrace();
		        }
			}
		};
	    asyn.execute("");
	}
    
    @Override
    public void onPrepared(MediaPlayer mp) {
	   // TODO Auto-generated method stub
       if(mediaPlayer != null)
	   mediaPlayer.start();
	   doSendBroadcast();
	}
	
    private void doSendBroadcast() {
        Intent r1_intent = new Intent();
        r1_intent.setAction(ACTION_PLAYING);
        r1_intent.putExtra(ACTION_PLAYING, ACTION_PLAYING);                 
        sendBroadcast(r1_intent);
    }
    
	@Override
	public void onCompletion(MediaPlayer mp) {
		if(mediaPlayer != null)
			mediaPlayer.stop();
	}
}
