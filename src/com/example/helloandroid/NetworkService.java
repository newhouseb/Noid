package com.example.helloandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NetworkService extends Service {
	private NetworkThread _networkthread;
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i("Service", "Bound!");
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("Service", "Created!");
	}

	@Override
	public void onStart(Intent intent, int startid) {
		super.onStart(intent, startid);
		Log.i("Service", "Started!");
		
		SharedPreferences settings = getSharedPreferences("prefsFile", 0);
		
		_networkthread = new NetworkThread("notifyio");
		_networkthread.owner = this;
		_networkthread.stream = settings.getString("stream", "");
		_networkthread.settings = settings;
		_networkthread.mp = MediaPlayer.create(getBaseContext(), R.raw.noid);
		_networkthread.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if(_networkthread.stream != "" && _networkthread != null)
			_networkthread.start();
	}
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(_networkthread != null) {
			Thread temp = _networkthread;
			_networkthread = null;
			temp.interrupt();
			Log.i("Service", "Thread Stopped!");
		}
		
		Log.i("Service", "Destructed!");
	}	
	
}
