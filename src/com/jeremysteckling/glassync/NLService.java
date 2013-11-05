package com.jeremysteckling.glassync;

import com.jeremysteckling.glassync.Glassinator.Callback;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

@SuppressLint("NewApi")
public class NLService extends NotificationListenerService {

   String TAG = this.getClass().getSimpleName();
    
    private static Handler handler;
    private static AuthPreferences authPreferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
        this.handler = new Handler();
        this.authPreferences = new AuthPreferences(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

    	if(sbn.isClearable())
    	{
	        Notification notification = sbn.getNotification();
	        
	        PackageManager pm = getApplicationContext().getPackageManager();
	        ApplicationInfo ai;
	        try {
	            ai = pm.getApplicationInfo( sbn.getPackageName(), 0);
	        } catch (final NameNotFoundException e) {
	            ai = null;
	        }
	        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
	        
	        String extra_text;
	        String extra_title;
	        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
	        {
	        	extra_text = notification.extras.getString(Notification.EXTRA_TEXT);
	        	extra_title = notification.extras.getString(Notification.EXTRA_TITLE);
	        }
	        else
	        {
	        	try {
		        	RemoteViews remoteView = notification.contentView;
		        	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		            ViewGroup localView = (ViewGroup) inflater.inflate(remoteView.getLayoutId(), null);
		            remoteView.reapply(getApplicationContext(), localView);
		            TextView tvTitle = (TextView) localView.findViewById(android.R.id.title);
		            extra_title = tvTitle.getText().toString();
	        	} catch(Exception e) {
	        		extra_title = applicationName;
	        	}
	        	
	        	extra_text = notification.tickerText.toString();
	        }
	        
	        Log.d(TAG, "Pulled a notification.");
	    	
	        if (authPreferences.getUser() != null
					&& authPreferences.getToken() != null) {
	        
	        	Glassinator glassinator = new Glassinator(authPreferences.getToken(),
	        			handler);
	        	
	        	Log.d(TAG, "Token: "+this.authPreferences.getToken());
	        	
	        	glassinator.postTimelineCard(extra_title, extra_text, applicationName);
	    	
	        } else Log.d(TAG, "No auth.");
	        
    	}
    	else
    		Log.d(TAG, "Notification was not cancelable, not displaying.");

    }
    
    private Callback statusCallback = new Callback() {

		@Override
		public void run() {
			Log.d("",status);
		}
	};

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

}
