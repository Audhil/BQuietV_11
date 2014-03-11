package com.wordpress.smdaudhilbe.bquiet;

import java.util.Calendar;

import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;
import com.wordpress.smdaudhilbe.bquiet.silent.SilentService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class EventRingerModeChangeReceiver extends BroadcastReceiver {

	private Context context;
	private AudioManager audioManager;
	private MySharedPreferences mPreference;
	AlarmManager alarmManager;
	private String[] afterSplitString;
	private DataBaseConnectivity dbConnectivity;
	private String[] eventDetails;
	private Intent sSilentService;
	private Calendar cal;
	private NotiId notiId;

	@SuppressLint("NewApi") 
	@Override
	public void onReceive(Context context, Intent intent) {

		this.context = context;
		
		//	audio manager
		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		//	sharedpreference
		mPreference = new MySharedPreferences(context);
		
		//	alarm manager to delete de silent service
		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		//	database connectivity
		dbConnectivity = new DataBaseConnectivity(context);
		
		//	notification id
		notiId = new NotiId(context);
		
		//	this condition is true for only for odd values
		if((mPreference.getEventRingermodeChangeReceiverExecuted() % 2) == 1)
			mPreference.putEventRingermodeChangeReceiverExecuted(2);
		
		//	this condition is true for even values
		else{	

			//	if it is triggered at Event period
			if(!mPreference.getEventAlarmInProgress().equals("no_alarm")){
				
				//	put event is not in progress
				mPreference.putEventAlarmInProgress("no_alarm");

				//	disable sms receiver
				int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
				ComponentName cName = new ComponentName(context,EventSmSReceiver.class);    	
				context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
				Log.d("EventRingerModeChangeReceiver : Disabled EventSmSReceiver", "EventSmSReceiver Disabled");
					
				//	disable entered geofence receiver
				flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
				cName = new ComponentName(context,EventGeoFenceListenerReceiver.class);    	
				context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
				Log.d("EventRingerModeChangeReceiver : Disabled EventGeoFenceListenerReceiver", "EventGeoFenceListenerReceiver Disabled");		
					
				//	disable this receiver
				flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
				cName = new ComponentName(context,EventRingerModeChangeReceiver.class);    	
				context.getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
				Log.d("EventRingerModeChangeReceiver : Disabled EventRingerModeChangeReceiver", "EventRingerModeChangeReceiver Disabled");
		
				//	cancelling de silent alarm scheduled
				String alarmEventNameWithIterator = mPreference.getGlobalAlarmEventInProgress();
				mPreference.dontExecuteDeSilentService(true);
				
				Log.d("EventRingerModeChangeReceiver : Disabled De Silenting Service", "De Silenting Service Disabled");
			
				//	schedule alarm if it is repeated
				afterSplitString = alarmEventNameWithIterator.split(":");			
				eventDetails = dbConnectivity.getDetailsOfAnEvent(afterSplitString[0]);
				sSilentService = new Intent(context,SilentService.class);
				sSilentService.putExtra("alarmEventNameWithIterator", alarmEventNameWithIterator);					
				PendingIntent pIntent = PendingIntent.getService(context,Integer.parseInt(afterSplitString[1]),sSilentService,0);
			
				//	if repeat is active
				if(Boolean.parseBoolean(eventDetails[1])){				
					//	Calendar
					cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(eventDetails[4])/3600000);
					cal.getTimeInMillis();	
					cal.set(Calendar.MINUTE,(Integer.parseInt(eventDetails[4]) % 3600000)/60000);
					
					//	set alarms
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)				
						alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+24 * 60 * 60 * 1000, pIntent);
			        
					else
						alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+24 * 60 * 60 * 1000, pIntent);
				
					Log.d("EventRingerModeChangeReceiver :","scheduled alarm for repeating");
				}
				//	else repeat is inactive
				else{
					alarmManager.cancel(pIntent);
				
					//	and move the event to bin
					dbConnectivity.removeFromCreateEventsMoveToBinEvents(afterSplitString[0]);
				
					Log.d("EventRingerModeChangeReceiver :","alarm cancelled for not to repeat");
				}
				
				//	changing ringermode
				switch (audioManager.getRingerMode()) {
			
				case AudioManager.RINGER_MODE_SILENT:
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					mPreference.putEventRingermodeChangeReceiverExecuted(1);
					break;
				
				case AudioManager.RINGER_MODE_VIBRATE:
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					mPreference.putEventRingermodeChangeReceiverExecuted(1);
					break;

				default:
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					mPreference.putEventRingermodeChangeReceiverExecuted(1);
					break;
				}
				
				//	send notification
				sendNotification("BQuiet",afterSplitString[0]+" disabled!");
				mPreference.putEventRingermodeChangeReceiverExecuted(1);
			}
		}
       	Log.d("EventRingerModeChangeReceiver", "executed");
	}

	//	notification
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
        
        mNotificationManager.notify((int)notiId.notiId(), builder.build());
	}
}