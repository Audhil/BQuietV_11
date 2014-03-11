package com.wordpress.smdaudhilbe.bquiet;

import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;

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

public class RingerModeChangeReceiver extends BroadcastReceiver {

	private AudioManager audioManager;
	private MySharedPreferences mPreference;
	private Context context;
	private NotiId notiId;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		this.context = context;
		
		//	audio manager
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		//	sharedpreference
		mPreference = new MySharedPreferences(context);
		
		notiId = new NotiId(context);
		
		//	this condition is true for only for odd values
		if((mPreference.getFirstTimeExecutedOddValue() % 2) == 1)
			mPreference.putFirstTimeExecutedOddValue(2);		
		
		//	not odd value
		else{
			
			//	send notification
			if(mPreference.getGeoFenceIsInProgress())
				sendNotification("BQuiet","fence disabled!");
			
			//	disable inProgress flag
			mPreference.putGeoFenceIsInProgress(false);
			
			//	changing ringermode
			switch (audioManager.getRingerMode()) {
		
			case AudioManager.RINGER_MODE_SILENT:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				mPreference.putFirstTimeExecutedOddValue(1);	
				break;
			
			case AudioManager.RINGER_MODE_VIBRATE:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);		
				mPreference.putFirstTimeExecutedOddValue(1);
				break;

			default:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				mPreference.putFirstTimeExecutedOddValue(1);
				break;
			}
			
			//	disable sms receiver
			int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			ComponentName cName = new ComponentName(context,SmSReceiver.class);    	
			context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("ringerMode : Disabled smsReceiver", "SmsReceiver Disabled");
			
			//	disable no internet receiver
			flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			cName = new ComponentName(context,GeoFencePreventiveReceiver.class);    	
			context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("ringerMode : Disabled gPreventive", "gPreventiveReceiver Disabled");		
			
			//	disable this receiver
			flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			cName = new ComponentName(context,RingerModeChangeReceiver.class);    	
			context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("ringerMode : Disabled RingerModeChangeReceiver", "RingerModeChangeReceiver Disabled");
		}
       	
       	Log.d("ringermodechangereceiver", "executed");
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