package com.wordpress.smdaudhilbe.bquiet.map;

import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.wordpress.smdaudhilbe.bquiet.EventRingerModeChangeReceiver;
import com.wordpress.smdaudhilbe.bquiet.GeoFencePreventiveReceiver;
import com.wordpress.smdaudhilbe.bquiet.MainActivity;
import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.RingerModeChangeReceiver;
import com.wordpress.smdaudhilbe.bquiet.SmSReceiver;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.NotiId;

public class ReceiveTransitionsIntentService extends IntentService {

	private AudioManager audioManager;
	
	SimpleGeoFenceStore sStore;
	
	static boolean ring = false;
	static boolean callReceived = false;
	static String callerPhoneNumber = " ";
	
	int transition;
	
	MySharedPreferences mPreference;
	
	NotiId notiId;
	
	String ids;

	private DataBaseConnectivity dbConnectivity;

	private String[] dataFromDB;

	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}
	
	/**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();
        
        notiId = new NotiId(getApplicationContext());

        //	audio Manager
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		//	geofence store
		sStore = new SimpleGeoFenceStore(getApplicationContext());
		
		//	mPreference
		mPreference = new MySharedPreferences(getApplicationContext());
		
		//	database reference
		dbConnectivity = new DataBaseConnectivity(getApplicationContext());

        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeoFenceUtils.CATEGORY_LOCATION_SERVICES);

        // First check for errors
        if (LocationClient.hasError(intent)) {

            // Get the error code
            int errorCode = LocationClient.getErrorCode(intent);

            // Get the error message
            String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);

            // Log the error
            Log.e(GeoFenceUtils.APPTAG,getString(R.string.geofence_transition_error_detail, errorMessage));

            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(GeoFenceUtils.ACTION_GEOFENCE_ERROR).putExtra(GeoFenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        // If there's no error, get the transition type and create a notification
        } else {

            // Get the type of transition (entry or exit)
            transition = LocationClient.getGeofenceTransition(intent);

            // Test that a valid transition was reported
            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {

                // Post a notification
                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                String[] geofenceIds = new String[geofences.size()];
                
                for (int index = 0; index < geofences.size() ; index++)                 	
                    geofenceIds[index] = geofences.get(index).getRequestId();                    
                
                //	geofence id
                ids = TextUtils.join(GeoFenceUtils.GEOFENCE_ID_DELIMITER,geofenceIds);
                String transitionType = getTransitionString(transition);
                
                sStore.whichGeoFenceId(ids);
                
                //	enable sms broadcast receiver
                if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                	
                	//	inProgress flag - any geofence
                	mPreference.putGeoFenceIsInProgress(true);
                	
                	//	first time executed to true / for ringermodechangereceiver to avoid working
                	mPreference.putFirstTimeExecutedOddValue(1);
                	
                	int flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;                	
                	ComponentName cName = new ComponentName(ReceiveTransitionsIntentService.this,SmSReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	//	enable prevention receiver for Internet error or user changes ringer mode
                	flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                	cName = new ComponentName(ReceiveTransitionsIntentService.this,GeoFencePreventiveReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	//	special receiver for ringermodechangereceiver
                	flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;                	
                	cName = new ComponentName(ReceiveTransitionsIntentService.this,RingerModeChangeReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	//	special receiver for ringermodechangereceiver
                	flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;                	
                	cName = new ComponentName(ReceiveTransitionsIntentService.this,EventRingerModeChangeReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	//	put phone to silent
                	
                	// 	to restore previous ringermode
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
        			
                	audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE | AudioManager.RINGER_MODE_SILENT);
                	
                	//	sending broadcast to events about entered inside geofence in order to stop it from executing
                	//	will trigger EventGeoFenceListenerReceiver.class
                	if(!mPreference.getEventAlarmInProgress().equals("no_alarm")){
                		
                		Intent iTent = new Intent();
                		iTent.setAction("com.wordpress.smdaudhilbe.bquiet");
                		sendBroadcast(iTent);                	
                	}
                }
                //	disable sms broadcast receiver
                else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                	
                	int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                	ComponentName cName = new ComponentName(ReceiveTransitionsIntentService.this,SmSReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	//	disable prevention receiver 
                	flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                	cName = new ComponentName(ReceiveTransitionsIntentService.this,GeoFencePreventiveReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	//	disable ringermodechangereceiver
                	flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                	cName = new ComponentName(ReceiveTransitionsIntentService.this,RingerModeChangeReceiver.class);
                	getPackageManager().setComponentEnabledSetting(cName,flag,PackageManager.DONT_KILL_APP);
                	
                	// 	remove inProgress flag
                	mPreference.putGeoFenceIsInProgress(false);
                	
                	//	first time executed to true / for ringermodechangereceiver to avoid working
                	mPreference.putFirstTimeExecutedOddValue(1);
                	
                	//	restoring previous ringer mode
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
                }
        		
                //	send notification
                sendNotification(transitionType, ids);
                
                // Log the transition type and a message
                Log.d(GeoFenceUtils.APPTAG,getString(R.string.geofence_transition_notification_title,transitionType));
                Log.d(GeoFenceUtils.APPTAG,getString(R.string.geofence_transition_notification_text));                
                
            // An invalid transition was reported
            } else {
                // Always log as an error
                Log.e(GeoFenceUtils.APPTAG,getString(R.string.geofence_transition_invalid_type, transition));
            }
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * @param transitionType The type of transition that occurred.
     *
     */
    private void sendNotification(String transitionType, String is) {
    	
    	dataFromDB = dbConnectivity.dataOfGeoFence(sStore.getWhichGeoFenceId());

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
        if(transitionType.equals(getString(R.string.geofence_transition_entered)))        
        	builder.setSmallIcon(R.drawable.ic_launcher_final)
               .setContentTitle(getString(R.string.geofence_transition_notification_title,transitionType))
               .setContentText("You are inside \""+dataFromDB[1]+"\" fence!")
               .setContentIntent(notificationPendingIntent); 
        
        else
        	builder.setSmallIcon(R.drawable.android_happy)
            .setContentTitle(getString(R.string.geofence_transition_notification_title,transitionType))
            .setContentText("You are outside \""+dataFromDB[1]+"\" fence!")
            .setContentIntent(notificationPendingIntent);
        
        //	vibrate before notification
        builder.build().vibrate = new long[] { 100, 250, 100, 500};

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);       
        
        // Issue the notification
        mNotificationManager.notify((int)notiId.notiId(), builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
    	
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
}