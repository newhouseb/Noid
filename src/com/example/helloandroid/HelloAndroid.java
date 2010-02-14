package com.example.helloandroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class HelloAndroid extends Activity {
	public Intent svc;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i("app", "starting...");     
        
        setContentView(R.layout.main); 
        
        /* Start the background service */
        svc = new Intent();
        svc.setAction("com.example.helloandroid.NetworkService.MY_SERVICE");
        startService(svc);
        
        // Load preferences
        SharedPreferences settings = getSharedPreferences("prefsFile", 0);
        if(settings.getString("stream", "") != "") {
        	((TextView) findViewById(R.id.TextView01)).setText("Configured Successfully.  All systems go!");
        }       
        ((CheckBox) findViewById(R.id.CheckBox01)).setChecked(settings.getBoolean("notification", true));
        ((CheckBox) findViewById(R.id.CheckBox02)).setChecked(settings.getBoolean("sound", false));
        ((CheckBox) findViewById(R.id.CheckBox03)).setChecked(settings.getBoolean("vibration", false));
        
        final Intent intent = getIntent();
        if((intent != null) && (intent.getAction() != null)) {
        	String data = intent.getDataString();
        	Log.i("app", "got intent" + intent.getAction());
        	String stream = "http://api.notify.io/v1/listen/" + data.substring(29,69);
				
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("stream", stream);
			Log.i("Service", "data: " + data + ", stream: " + stream);
			editor.commit(); 
        	
        	/*final CheckBox one = (CheckBox) findViewById(R.id.CheckBox01);
        	one.setChecked(true);
        	one.setText(data);*/
        }
        
        /* The button to open to the webpage where you can get your URLs */
        Button configure = (Button) findViewById(R.id.Button01);
        configure.setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://notify.io")));
        		}
        	});
        
        /* Set up on click handlers for checkboxes */
        ((CheckBox) findViewById(R.id.CheckBox01)).setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			SharedPreferences settings = getSharedPreferences("prefsFile", 0);
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putBoolean("notification", ((CheckBox) v).isChecked());
    			editor.commit();

    		}
    	});
        
        ((CheckBox) findViewById(R.id.CheckBox02)).setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			SharedPreferences settings = getSharedPreferences("prefsFile", 0);
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putBoolean("sound", ((CheckBox) v).isChecked());
    			editor.commit(); 
    		}
    	});
        ((CheckBox) findViewById(R.id.CheckBox03)).setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			SharedPreferences settings = getSharedPreferences("prefsFile", 0);
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putBoolean("vibration", ((CheckBox) v).isChecked());
    			editor.commit();  			
    		}
    	});            
        
        Log.i("app", "started!");
    }
}