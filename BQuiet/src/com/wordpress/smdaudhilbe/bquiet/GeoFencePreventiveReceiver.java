package com.wordpress.smdaudhilbe.bquiet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;

public class GeoFencePreventiveReceiver extends BroadcastReceiver {

	MySharedPreferences mPreference;
	private AudioManager audioManager;
	private Context context;
	private NotiId notiId;
	
	@Override
	public void onReceive(Context context, Intent iTent) {
	
		this.context = context;
		
		//	shared preference
		mPreference = new MySharedPreferences(context);	
    	
    	//	audio manager
    	audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	
    	notiId = new NotiId(context);
    	
    	//	check if already geofence is running		
		if(mPreference.getGeoFenceIsInProgress()){
			
			//	disable sms receiver
			int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			ComponentName cName = new ComponentName(context,SmSReceiver.class);    	
			context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("gPreventive : Disabled smsReceiver", "SmsReceiver Disabled");
		
			//	disable ringermodechangereceiver
			flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			cName = new ComponentName(context,EventRingerModeChangeReceiver.class);    	
			context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("gPreventive : Disabled RingerModeChangeReceiver", "RingerModeChangeReceiver Disabled");
			
			//	change ringermode    	
			Log.d("gPreventive : ringermode changed", "ringer_mode_normal");
			
			switch (mPreference.getPreRingerMode()) {
			
			case 1:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);				
				break;
			
			case 2:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);				
				break;
			
			case 3:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);				
				break;

			default:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				break;
			}
    	
			//	send notification
			sendNotification("No internet!","fence disabled!");    	
			Log.d("gPreventive : notification sent", "notification sent");
    	
			//	disable this receiver
			flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			cName = new ComponentName(context,GeoFencePreventiveReceiver.class);    	
			context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("gPreventive : Disabled gPreventive", "gPreventiveReceiver Disabled");
    	
			//	disable inProgress flag
			mPreference.putGeoFenceIsInProgress(false);
			Log.d("mPreference.GeoFenceIsInProgress", "false");
		}
	}

	//	send notification
	private void sendNotification(String Title, String Message) {
		
		// Create an explicit content Intent that starts the main Activity
        Intent notificationIntent = new Intent(context,MainActivity.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =  stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Set the notification contents        
        builder.setSmallIcon(R.drawable.android_happy)
            	.setContentTitle(Title)
            	.setContentText(Message)
            	.setContentIntent(notificationPendingIntent);
        
        //	vibrate before notification
        builder.build().vibrate = new long[] { 100, 250, 100, 500};

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);       
        
        // Issue the notification
        mNotificationManager.notify((int)notiId.notiId(), builder.build());
	}
}