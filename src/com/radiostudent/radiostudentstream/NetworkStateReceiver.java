package com.radiostudent.radiostudentstream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {
	
	final static String ACTION_NETWORK = "network";
	private static ConnectivityManager connManager;
	
    public void onReceive(Context context, Intent intent) {
    	try {
    		if(intent.getExtras()!=null) {
    			connManager = (ConnectivityManager) context
    		            .getSystemService(Context.CONNECTIVITY_SERVICE);
    		    NetworkInfo ni = connManager.getActiveNetworkInfo();
    	        // NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_EXTRA_INFO);
    	        if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
    	        	Intent int_a = new Intent();
    	        	int_a.setAction(ACTION_NETWORK);
    	        	int_a.putExtra("n_status", "Connected");
    	        	context.sendBroadcast(int_a);
    	        } 
    	        else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
    	        	Intent int_b = new Intent();
    	        	int_b.setAction(ACTION_NETWORK);
    	        	int_b.putExtra("n_status", "Disconnected");
    	        	context.sendBroadcast(int_b);
    	        }
    	        else if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTING){
    	        	Intent int_c = new Intent();
    	        	int_c.setAction(ACTION_NETWORK);
    	        	int_c.putExtra("n_status", "Connecting");
    	        	context.sendBroadcast(int_c);
    	        }
    	        else {
    	        	Intent int_c = new Intent();
    	        	int_c.setAction(ACTION_NETWORK);
    	        	NetworkInfo.State st = ni.getState();
    	        	int_c.putExtra("n_status", st.name());
    	        	context.sendBroadcast(int_c);   	        	
    	        }
            }	
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}