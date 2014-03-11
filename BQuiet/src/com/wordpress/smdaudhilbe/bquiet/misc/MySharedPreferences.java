package com.wordpress.smdaudhilbe.bquiet.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class MySharedPreferences {
	
	SharedPreferences sPreferences;
	private Editor editor;
	
	public static final String MY_SPREFERENCE = "appPermissionsPreference";
	
	/*
	 * constructor
	 * */
	public MySharedPreferences(Context context) {
		
		sPreferences = context.getSharedPreferences(MY_SPREFERENCE,0);
		editor = sPreferences.edit();
		
		//	it is necessary to call commit() or apply() to save the changes
		editor.commit();
	}

	//	user defined app permissions storing inside shared preferences
	public void storeAppPermissions(HashMap<String, Boolean> appPermission) {
		
		editor.putBoolean("user_permissions",true);
		
		//	retriving keys and values from HashMap
		Set<Entry<String,Boolean>> sE = appPermission.entrySet();
			
		Iterator<Entry<String,Boolean>> iTerator = sE.iterator();
			
		while (iTerator.hasNext()) {				
			
			Map.Entry<String,Boolean> mE = (Map.Entry<String, Boolean>)iTerator.next();
			editor.putBoolean(mE.getKey(),mE.getValue());
		}
		editor.commit();		
	}	
	
	//	when user changes settings inside app
	public void putSmsPermission(boolean value) {
		
		editor.putBoolean("SmS", value);
		editor.commit();
	}
	
	public void putGeoFencePermission(boolean value) {
		
		editor.putBoolean("GeoFencing", value);
		editor.commit();
	}
	
	public void putPassCodePermission(boolean value) {
		
		editor.putBoolean("PassCode", value);
		editor.commit();
	}
	
	//	checking whether user has defined his permissions in app
	public boolean isUserDefinedPermissions() {
		return sPreferences.getBoolean("user_permissions",false);
	}
	
	//	check is SMS activated
	public boolean isUserDefinedSmsPermissions() {
		return sPreferences.getBoolean("SmS",false);
	}
		
	//	check is Geofencing activated
	public boolean isUserDefinedGeofencingPermissions() {
		return sPreferences.getBoolean("GeoFencing",false);
	}
	
	//	check is Passcode activated
	public boolean isUserDefinedPasscodePermissions() {
		return sPreferences.getBoolean("PassCode",false);
	}	

	//	if activated check for its body
	public void storePassCodeContent(String passCodeContent) {
		
		editor.putString("PassCodeBody",passCodeContent);
		editor.putBoolean("ProceedFurther", true);		
				
		editor.commit();		
	}
	
	// 	get sms body
	public String getPassCodeContent() {
		return sPreferences.getString("PassCodeBody",null);
	}
	
	//	making switch off to passcode setting
	public void deletePassCodeContent() {
		
		editor.remove("PassCode");
		editor.remove("PassCodeBody");		
		editor.commit();
	}	
	
	//	for debugging purpose
	public void deleteAll() {
		editor.clear();
		editor.commit();
	}
	
	//	login page
	public void storeProceedFurther(boolean value) {
		
		editor.putBoolean("ProceedFurther", value);
		editor.commit();
	}
	
	//	get back proceed further status
	public boolean getProceedFurther() {
		return sPreferences.getBoolean("ProceedFurther",false);
	}

	//	for internet connection
	public void isActivityOpen(boolean value) {
		editor.putBoolean("isActivityOpen", value);
		editor.commit();
	}
	
	public boolean getIsActivityOpen() {
		return sPreferences.getBoolean("isActivityOpen", false);
	}

	//	Atlast I depend on shared preference for this issue
	public void storeEventNameForSmsReceiver(String eventName) {
		
		editor.putString("whatsTheEvent",eventName);
		editor.commit();
	}
	
	public String getEventNameForSmsReceiver() {
		return sPreferences.getString("whatsTheEvent","no_data");
	}

	//	for EventSmsReceiver
	public void putIsEventSmsReceiverExecutedOnce(boolean state) {
		
		editor.putBoolean("isEventSmsReceiverExecutedOnce",state);
		editor.commit();
	}
	
	public boolean isEventSmsReceiverExecutedOnce() {		
		return sPreferences.getBoolean("isEventSmsReceiverExecutedOnce",false);
	}

	public boolean isSmsReceiverExecutedOnce() {
		return sPreferences.getBoolean("isSmsReceiverExecuted",false);
	}

	public void putIsSmsReceiverExecutedOnce(boolean bool) {
		editor.putBoolean("isSmsReceiverExecuted", bool);
		editor.commit();
	}

	//	putting isFirstTimeGeoFenceExecuted
	public void putIsFirstTimeGeoFenceExecuted(boolean bool) {
		editor.putBoolean("isFirstTimeGeoFenceExecuted", bool);
		editor.commit();
	}
	
	//	isFirstTimeGeoFenceExecuted
	public boolean isFirstTimeGeoFenceExecuted() {
		return sPreferences.getBoolean("isFirstTimeGeoFenceExecuted",false);
	}
	
	//	inProgress geofence
	public void putGeoFenceIsInProgress(boolean bool) {
		editor.putBoolean("isGeoFenceIsInProgress", bool);
		editor.commit();
	}
	
	//	get inprogress flag
	public boolean getGeoFenceIsInProgress() {
		return sPreferences.getBoolean("isGeoFenceIsInProgress", false);
	}

	//	to avoid ringermodechangereceiver to get called
	public void putFirstTimeExecuted(boolean bool) {
		editor.putBoolean("isFirstTimeExecuted", bool);
		editor.commit();
	}	

	public boolean getFirstTimeExecuted() {
		return sPreferences.getBoolean("isFirstTimeExecuted", false);
	}

	//	putting odd value for preventing execution
	public void putFirstTimeExecutedOddValue(int integer) {
		editor.putInt("isFirstTimeExecutedOddValue", integer);
		editor.commit();
	}
	
	public int getFirstTimeExecutedOddValue() {
		return sPreferences.getInt("isFirstTimeExecutedOddValue",0);
	}

	//	eventAlarm is in progress
	public void putEventAlarmInProgress(String whichEventAlarm) {
		editor.putString("isEventAlarmInProgress", whichEventAlarm);
		editor.commit();
	}
	
	public String getEventAlarmInProgress() {
		return sPreferences.getString("isEventAlarmInProgress", "no_alarm");
	}

	//	to avoid execution for first time by ringermodechangereceiver
	public void putEventRingermodeChangeReceiverExecuted(int integer) {
		editor.putInt("isEventRingerModeChangeReceiverExecuted", integer);
		editor.commit();
	}
	
	public int getEventRingermodeChangeReceiverExecuted() {
		return sPreferences.getInt("isEventRingerModeChangeReceiverExecuted", 0);
	}

	//	it is helpful for disabling any de silent alarms
	public void putGlobalAlarmEventInProgress(String eventName) {
		editor.putString("globalAlarmEvent", eventName);
		editor.commit();
	}
	
	public String getGlobalAlarmEventInProgress() {
		return sPreferences.getString("globalAlarmEvent", "no_alarm");
	}

	//	which is duplicated during starting
	public void putWhichisDuplicated(String matter) {
		editor.putString("whichIsDuplicated", matter);
		editor.commit();
	}
	
	public String getWhichisDuplicated() {
		return sPreferences.getString("whichIsDuplicated","");
	}

	//	To delete this event
	public void putDeleteThisEvent(String event) {
		editor.putString("DeleteThisEvent", event);
		editor.commit();
	}
	
	public String getDeleteThisEvent() {
		return sPreferences.getString("DeleteThisEvent","no_event_to_delete");
	}

	//	putting notification id
	public void putNotificationId(long value) {
		editor.putLong("NotiId", value);
		editor.commit();
	}
	
	public long getNotificationId() {
		return sPreferences.getLong("NotiId",0);
	}

	//	to stop de silent service from executing
	public void dontExecuteDeSilentService(boolean value) {
		editor.putBoolean("NoDeSilent", value);
		editor.commit();
	}
	
	public boolean getDontExecuteDeSilentService() {
		return sPreferences.getBoolean("NoDeSilent", false);
	}

	//	dontshowToast
	public void putDontShowToast(boolean bool) {
		editor.putBoolean("bool", bool);
		editor.commit();
	}
	
	public boolean getDontShowToast() {
		return sPreferences.getBoolean("bool", false);
	}

	//	to restore ringer mode
	public void putPreRingerMode(int integer) {
		editor.putInt("preRingerMode", integer);
		editor.commit();
	}
	
	public int getPreRingerMode() {
		
		int preValue = sPreferences.getInt("preRingerMode",22);
		
		editor.putInt("preRingerMode", 22);
		editor.commit();
		
		return preValue;
	}

	//	unique GeoFence Id
	public long getUniqueGeoFenceId() {
		return sPreferences.getLong("uGeoId", 1);
	}
	
	public void putUniqueGeoFenceId() {
		
		long presentVal = sPreferences.getLong("uGeoId", 1);
		
		//	geofence unique id for next geofence
		editor.putLong("uGeoId", presentVal+1);
		editor.commit();
	}
}