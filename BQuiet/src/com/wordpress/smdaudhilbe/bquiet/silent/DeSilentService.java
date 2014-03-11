package com.wordpress.smdaudhilbe.bquiet.silent;

import java.util.Calendar;

import com.wordpress.smdaudhilbe.bquiet.EventGeoFenceListenerReceiver;
import com.wordpress.smdaudhilbe.bquiet.EventRingerModeChangeReceiver;
import com.wordpress.smdaudhilbe.bquiet.EventSmSReceiver;
import com.wordpress.smdaudhilbe.bquiet.MainActivity;
import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class DeSilentService extends Service {

	private String bdl;
	private MySharedPreferences mPreference;
	private AudioManager audioManager;
	private AlarmManager alarmManager;
	private String[] afterSplitString;
	private DataBaseConnectivity dbConnectivity;
	private String[] eventDetails;
	private Intent sSilentService;
	private PendingIntent pIntent;
	private Calendar cal;
	private NotiId notiId;

	@Override
	public IBinder onBind(Intent intent) {	
		return null;
	}
	
	@SuppressLint("NewApi") 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		bdl = intent.getExtras().getString("alarmEventNameWithIterator");
		mPreference = new MySharedPreferences(getApplicationContext());
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

		//	database connectivity
		dbConnectivity = new DataBaseConnectivity(getApplicationContext());
		
		notiId = new NotiId(getApplicationContext());
		
		if(!mPreference.getDontExecuteDeSilentService()){
			
			//	put event is not in progress
			mPreference.putEventAlarmInProgress("no_alarm");
	
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
			
			//	disable sms receiver
			int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			ComponentName cName = new ComponentName(getApplicationContext(),EventSmSReceiver.class);    	
			getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("DeSilentService : Disabled EventSmSReceiver", "EventSmSReceiver Disabled");
						
			//	disable entered geofence receiver
			flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			cName = new ComponentName(getApplicationContext(),EventGeoFenceListenerReceiver.class);    	
			getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("DeSilentService : Disabled EventGeoFenceListenerReceiver", "EventGeoFenceListenerReceiver Disabled");		
						
			//	disable this receiver
			flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;    	
			cName = new ComponentName(getApplicationContext(),EventRingerModeChangeReceiver.class);    	
			getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
			Log.d("DeSilentService : Disabled EventRingerModeChangeReceiver", "EventRingerModeChangeReceiver Disabled");
						
			//	schedule alarm if it is repeated
			afterSplitString = bdl.split(":");			
			eventDetails = dbConnectivity.getDetailsOfAnEvent(afterSplitString[0]);
			sSilentService = new Intent(getApplicationContext(),SilentService.class);
			sSilentService.putExtra("alarmEventNameWithIterator", bdl);					
			pIntent = PendingIntent.getService(getApplicationContext(),Integer.parseInt(afterSplitString[1]),sSilentService,0);
		
			//	send notification
			sendNotification("BQuiet",afterSplitString[0]+" event ended!");
				
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
			}
			//	else repeat is inactive
			else{
				alarmManager.cancel(pIntent);
			
				//	and move the event to bin
				dbConnectivity.removeFromCreateEventsMoveToBinEvents(afterSplitString[0]);
			}
		}
		else
			mPreference.dontExecuteDeSilentService(false);		
		
		//	stop service
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}

		//	notification
	private void sendNotification(String Title, String Message) {
		
		// Create an explicit content Intent that starts the main Activity
        Intent notificationIntent = new Intent(getApplicationContext(),MainActivity.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =  stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        // Set the notification contents        
        builder.setSmallIcon(R.drawable.android_happy)
            	.setContentTitle(Title)
            	.setContentText(Message)
            	.setContentIntent(notificationPendingIntent);
        
        //	vibrate before notification
        builder.build().vibrate = new long[] { 100, 250, 100, 500};

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);       
        
        // Issue the notification with random id
        // Issue the notification        
        mNotificationManager.notify((int)notiId.notiId(), builder.build());
	}
}