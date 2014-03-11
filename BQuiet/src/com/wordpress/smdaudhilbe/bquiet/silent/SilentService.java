package com.wordpress.smdaudhilbe.bquiet.silent;

import java.util.Calendar;

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

import com.wordpress.smdaudhilbe.bquiet.EventGeoFenceListenerReceiver;
import com.wordpress.smdaudhilbe.bquiet.EventRingerModeChangeReceiver;
import com.wordpress.smdaudhilbe.bquiet.EventSmSReceiver;
import com.wordpress.smdaudhilbe.bquiet.MainActivity;
import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.RingerModeChangeReceiver;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;

public class SilentService extends Service {

	private DataBaseConnectivity dbConnectivity;
	private String bdl;
	private AlarmManager aManager;
	private String[] afterSplitString;
	private String[] eventDetails;
	private Intent dSilentService;
	private PendingIntent pIntent;
	private Calendar cal;
	private MySharedPreferences mPreference;
	private AudioManager audioManager;
	int flag;
	ComponentName cName;
	private boolean dontProceedFurther = false;
	private Intent sSilentService;
	private NotiId notiId;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@SuppressLint("NewApi") 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		dbConnectivity = new DataBaseConnectivity(getApplicationContext());
		aManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		dSilentService = new Intent(getApplicationContext(),DeSilentService.class);
		mPreference = new MySharedPreferences(getApplicationContext());		
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		notiId = new NotiId(getApplicationContext());
		
		bdl = intent.getExtras().getString("alarmEventNameWithIterator");
		
		//	do all these if and only if geofence is not in progress
		if(!mPreference.getGeoFenceIsInProgress()){
			
			//	to restore previous ringermode
			switch(audioManager.getRingerMode()) {
			
				case AudioManager.RINGER_MODE_SILENT:
					mPreference.putPreRingerMode(1);
					break;
			
				case AudioManager.RINGER_MODE_VIBRATE:
					mPreference.putPreRingerMode(2);		
					break;
					
				case AudioManager.RINGER_MODE_NORMAL:
					mPreference.putPreRingerMode(3);		
					break;
			}
		
			/*
			 * I has a doubt here whether to use Bundle or not
			 */		
			bdl = intent.getExtras().getString("alarmEventNameWithIterator");
			
			//	storing eventName into shared preference as a reference to delete any desilent alarms by ringermodechangereceiver / geofencelistenerreceiver
			mPreference.putGlobalAlarmEventInProgress(bdl);
			
			afterSplitString = bdl.split(":");
			
			//	checking whether alarms deleted or not before proceeding
			for (int i = 0; i < dbConnectivity.getDeletedAlarmNames().size(); i++) {
				
				if(!dbConnectivity.getDeletedAlarmNames().get(i).equals("no_deleted_alarms")){
					
					if(afterSplitString[0].equals(dbConnectivity.getDeletedAlarmNames().get(i))){
						
						//	deleting alarm
						sSilentService = new Intent(getApplicationContext(),SilentService.class);						
						sSilentService.putExtra("alarmEventNameWithIterator", bdl);					
						pIntent = PendingIntent.getService(getApplicationContext(),Integer.parseInt(afterSplitString[1]),sSilentService,0);
						
						aManager.cancel(pIntent);
						
						//	delete the name inside database
						dbConnectivity.deleteDeletedAlarmNames(afterSplitString[0]);
						
						dontProceedFurther = true;
						break;
					}
				}
			}
			
			//	if event is deleted don't do this at all
			if(!dontProceedFurther){
			
				eventDetails = dbConnectivity.getDetailsOfAnEvent(afterSplitString[0]);
				
				/*
				 * 1)	setting event finishing alarm,
				 */		
				dSilentService.putExtra("alarmEventNameWithIterator",bdl);
				
				//	alarm pending intents
				pIntent = PendingIntent.getService(getApplicationContext(),1 + (int)(Math.random() * ((10000 - 1) + 1)),dSilentService,0);
				
				//	Calendar
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(eventDetails[5])/3600000);
				cal.getTimeInMillis();	
				cal.set(Calendar.MINUTE,(Integer.parseInt(eventDetails[5]) % 3600000)/60000);
				
				if(Long.parseLong(eventDetails[5]) < Long.parseLong(eventDetails[4])){
					//	set alarms
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)				
						aManager.set(AlarmManager.RTC_WAKEUP, (24 * 60 * 60 * 1000 + cal.getTimeInMillis()), pIntent);
			        
					else
						aManager.setExact(AlarmManager.RTC_WAKEUP,(24 * 60 * 60 * 1000 + cal.getTimeInMillis()), pIntent);
				}
				
				else{
					//	set alarms
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)				
						aManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pIntent);
			        
					else
						aManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pIntent);
				}
		
				/*
				 * 2)	setting inProgress flag in order to avoid it from getting deleting
				 */
				mPreference.putEventAlarmInProgress(afterSplitString[0]);
				
				//	disable ringermodechangereceiver of geofence without interfering
            	//	special receiver for ringermodechangereceiver
            	flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;                	
            	cName = new ComponentName(this,RingerModeChangeReceiver.class);
            	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
		
				/*
				 * 3)	silent phone
				 */
				audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE | AudioManager.RINGER_MODE_SILENT);
		
				/*
				 * 4)	enable sms receiver if it is activated by user
				 */
				if(Boolean.parseBoolean(eventDetails[2])){			
				
					//	storing event name into shared preference
					mPreference.storeEventNameForSmsReceiver(afterSplitString[0]);
				
					flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;        	
					cName = new ComponentName(SilentService.this,EventSmSReceiver.class);
					getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);			
				}

				/*
				 * 5)	enable ringer mode change receiver
				 */
				flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;        	
				cName = new ComponentName(SilentService.this,EventRingerModeChangeReceiver.class);
				getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
    	
				//	to avoid ringermodechangereceiver to execute for first time
				mPreference.putEventRingermodeChangeReceiverExecuted(1);
    	
				/*
				 * 6)	send notification
				 */
				sendNotification("BQuiet",afterSplitString[0]+" event started!");
    	
				/*
				 * 7)	enable geofence listener if user enters into fence then avoid using events stop immediately 
				 */
				flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;        	
				cName = new ComponentName(SilentService.this,EventGeoFenceListenerReceiver.class);
				getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
    	
				}
			}
		
		//	executes when geofence is in progress
		else{
			
			//	schedule alarm if it is repeated
			afterSplitString = bdl.split(":");			
			eventDetails = dbConnectivity.getDetailsOfAnEvent(afterSplitString[0]);
			sSilentService = new Intent(getApplicationContext(),SilentService.class);
			sSilentService.putExtra("alarmEventNameWithIterator", bdl);
			pIntent = PendingIntent.getService(getApplicationContext(),Integer.parseInt(afterSplitString[1]),sSilentService,0);
			
			//	to avoid from getting deleted
			mPreference.putEventAlarmInProgress(afterSplitString[0]);
			
			//	send notification
			sendNotification("BQuiet",afterSplitString[0]+" event disabled since entered inside fence!");
			
			//	if repeat is active
			if(Boolean.parseBoolean(eventDetails[1])){
				
				//	Calendar
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(eventDetails[4])/3600000);
				cal.getTimeInMillis();	
				cal.set(Calendar.MINUTE,(Integer.parseInt(eventDetails[4]) % 3600000)/60000);
						
				//	set alarms
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)				
					aManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+24 * 60 * 60 * 1000, pIntent);
					        
			    else
			    	aManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+24 * 60 * 60 * 1000, pIntent);
			}
			//	else repeat is inactive
			else{
				aManager.cancel(pIntent);
				
				//	and move the event to bin
				dbConnectivity.removeFromCreateEventsMoveToBinEvents(afterSplitString[0]);
			}
		}
		
		//	stop self if geofence is in progress
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}

	//	to put notification
	private void sendNotification(String Title, String Message) {
		
        // Create an explicit content Intent that starts the main Activity
      Intent notificationIntent = new Intent(getApplicationContext(),MainActivity.class);

      // Construct a task stack
      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

      // Adds the main Activity to the task stack as the parent
      stackBuilder.addParentStack(MainActivity.class);

      // Push the content Intent onto the stack
      stackBuilder.addNextIntent(notificationIntent);

      // Get a PendingIntent containing the entire back stack
      PendingIntent notificationPendingIntent =  stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

      // Get a notification builder that's compatible with platform versions >= 4
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

      // Set the notification contents
      builder.setSmallIcon(R.drawable.ic_launcher_final)
             .setContentTitle(Title)
             .setContentText(Message)
             .setContentIntent(notificationPendingIntent);        
      
      //	vibrate before notification
      builder.build().vibrate = new long[] { 100, 250, 100, 500};
      
      //	to clear itself
      builder.setAutoCancel(true);

      // Get an instance of the Notification manager
      NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);       
      
      // Issue the notification
      mNotificationManager.notify((int)notiId.notiId(), builder.build());
	}
}