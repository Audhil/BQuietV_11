package com.wordpress.smdaudhilbe.bquiet.silent;

import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;

public class SchedulingReceiver extends BroadcastReceiver {
	
	private DataBaseConnectivity dbConnectivity;
	private List<String> activeEvents;
	private String[] eventDetails;
	private Intent sSilentService;
	private AlarmManager aManager;
	private PendingIntent pIntent;
	Calendar cal;
	private MySharedPreferences mPreference;
	private NotiId notiId;

	@SuppressLint("NewApi") 
	@Override
	public void onReceive(Context context, Intent intent) {
		
		dbConnectivity = new DataBaseConnectivity(context);
		activeEvents = dbConnectivity.getAlarmNamesToSchedule();
		sSilentService = new Intent(context,SilentService.class);
		aManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		mPreference = new MySharedPreferences(context);
		
		notiId = new NotiId(context);
		
		//	scheduling alarms
		for(int i = 0; i < activeEvents.size(); i++) {
			
			eventDetails = dbConnectivity.getDetailsOfAnEvent(activeEvents.get(i));
			
			//	if eventAlarm is in progress don't schedule it again
			if(!eventDetails[i].equals(mPreference.getEventAlarmInProgress())){
				
				long iD = notiId.notiId();
				
				//	passing eventName for future reference
				sSilentService.putExtra("alarmEventNameWithIterator", activeEvents.get(i)+":"+iD);
				
				//	alarm pending intents	
				pIntent = PendingIntent.getService(context,(int)iD,sSilentService,0);
			
				//	Calendar
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(eventDetails[4])/3600000);
				cal.getTimeInMillis();	
				cal.set(Calendar.MINUTE,(Integer.parseInt(eventDetails[4]) % 3600000)/60000);
			
				long TriggerMillis = cal.getTimeInMillis();
			
				//	present time
				Calendar present = Calendar.getInstance();
			
				//	Starting
				if(present.getTimeInMillis() < TriggerMillis){
				
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {					
						aManager.set(AlarmManager.RTC_WAKEUP, TriggerMillis, pIntent);			        
					} else {
						aManager.setExact(AlarmManager.RTC_WAKEUP, TriggerMillis, pIntent);
					}
				}
					
				//	if alarm time is before current time - means it is made for next day
				else{
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						aManager.set(AlarmManager.RTC_WAKEUP, TriggerMillis + 24 * 60 * 60 * 1000, pIntent);
					} else {
						aManager.setExact(AlarmManager.RTC_WAKEUP, TriggerMillis + 24 * 60 * 60 * 1000, pIntent);
					}
				}
			}
		}	
	}
}