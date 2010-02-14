package com.example.helloandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;
import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class NetworkThread extends Thread {
	
    private static int HELLO_ID = 1;	
    private NotificationManager _notificationManager;

    public NetworkService owner;
    public String stream;
    public SharedPreferences settings;
    public Vibrator vibrator;
    public MediaPlayer mp;
    
    public NetworkThread(String str) {
    	super(str);
    }
    
	public void run() {
		while(true) {
			
			// Connect
			InputStream source = null;
			BufferedReader br = null;
			Log.i("Service", "connecting...");
			try {
				URLConnection connection = new URL(stream).openConnection();
				connection.setConnectTimeout(0);
				source = connection.getInputStream();
				br = new BufferedReader( new InputStreamReader( source ) );
			} catch(Exception e) {
				Log.i("Service", e.toString());
				continue;
			}
		    
			// Read a line (TODO: reuse connections)
			String line = "";
		    try {
		    	line = br.readLine();
		    	Log.i("Service", "Read" + line);
		    } catch(Exception e) {
		    	Log.i("Service", "Error reading URL");
		    	continue;
		    }
		    
		    // Suck out all the juicy bits from JSON
		    String author = "";
		    String title = "";
		    String text = "";
		    String url = "";
		    String appicon = "";
		    try {
		    	JSONObject message = new JSONObject(line);
		    	text = message.getString("text");
		    	title = message.has("title") ? message.getString("title") : "";
		    	author = message.getString("source");
		    	url = message.has("link") ? message.getString("link") : "";
		    	appicon = message.has("icon") ? message.getString("icon") : "";
		    } catch(JSONException e) {
		    	Log.i("Service", "JSON decoding error");
		    	continue;
		    }
		    
		    // Get the system notifier
		    Log.i("Service", "Done reading, now notifying " + url);
	        _notificationManager = (NotificationManager) owner.getSystemService(Context.NOTIFICATION_SERVICE);
	        
	        // Create our notification
	        int icon = owner.getResources().getIdentifier("statusicon", "drawable", owner.getPackageName());//R.drawable.favicon;
	        CharSequence tickerText = author; 
	        long when = System.currentTimeMillis();
	        Notification notification = new Notification(icon, tickerText, when);
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        
	        Context context = owner.getApplicationContext();
	        CharSequence contentTitle = title + " - " + author;
	        CharSequence contentText = text; 
	        
	        // Add in URL links if they exist
	        Intent notificationIntent;
	        // TODO: Don't launch an activity from a service... it is bad
	        if(url != "") {
	        	notificationIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
	        } else {
	        	notificationIntent = null;
	        }
	        PendingIntent contentIntent = PendingIntent.getActivity(owner, 0, notificationIntent, 0);
	        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	        
	        // Reach in and change the icon to the requested icon
	        try {
	        	int iconContainerId = (Integer) Class.forName("com.android.internal.R$id").getField("icon").get(null);
	        	notification.contentView.setImageViewResource(iconContainerId, R.drawable.favicon);
	        	
	        	if(appicon != "") {
		        	Bitmap bm = null; 
		            try { 
		            	InputStream favicon = new URL(appicon).openStream();
		                BufferedInputStream bis = new BufferedInputStream(favicon); 
		                bm = BitmapFactory.decodeStream(bis); 
		                bis.close(); 
		           } catch (IOException e) { 
		               Log.e("Service", "Error getting bitmap", e); 
		           } 
		           Log.i("Service", "bitmap?" + bm.toString());
		           notification.contentView.setImageViewBitmap(iconContainerId, bm);
	        	}
	        } catch (Exception e) { 
	        	Log.i("Service", "reflection failed" + e); 
	        	continue; 
	        }
	        
	        // Submit and continue
	        
	        if(settings.getBoolean("notification",false)) {
	        	_notificationManager.notify(HELLO_ID++, notification);
	        	Log.i("Service", "Notification enabled");
	        } else {
	        	Log.i("Service", "Notification disabled");
	        }
	        
	        if(settings.getBoolean("vibration", false)) {
	        	long[] pattern = {0L,500L};
	        	vibrator.vibrate(pattern,-1);
	        }
	        
	        if(settings.getBoolean("sound", false)) {
	        	mp.start();
	        }
	        
        	Log.i("Service", "Done notifying " + appicon);
		}
	}
	
}
