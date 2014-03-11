package com.wordpress.smdaudhilbe.bquiet;

import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
//import android.util.Log;
import android.util.Log;

public class EventSmSReceiver extends BroadcastReceiver {
	
	static boolean ring = false;
	static boolean callReceived = false;
	static String callerPhoneNumber = " ";
	
	DataBaseConnectivity dBConnectivity;
	
	private String smsContentFromDB;
	
	MySharedPreferences mPreference;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		mPreference = new MySharedPreferences(context);
		
		dBConnectivity = new DataBaseConnectivity(context);
		
		smsContentFromDB = dBConnectivity.getEventSmsContent(mPreference.getEventNameForSmsReceiver());                
        	
       	// Get the current Phone State
       	String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        
       	if(state == null)
       		return;

       	// If phone state "Ringing"
       	if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {        	
       	
       		ring = true;
            
       		// Get the Caller's Phone Number
       		Bundle bundle = intent.getExtras();
       		callerPhoneNumber = bundle.getString("incoming_number");           
       	}

        	// If incoming call is received
       	if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
       		
       		ring = false;
       		callReceived = true;
       	}
         
       	// If phone is Idle
       	if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
        	
       		// If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
       		if(ring == true && callReceived == false) {
       			
       			//	there is bug in this segment / so I took this shortcut step
       			if(!smsContentFromDB.equals("no_data")){
       				
       				if(!mPreference.isEventSmsReceiverExecutedOnce()){       					
       					
       					sendSms(callerPhoneNumber,smsContentFromDB);       				
       					mPreference.putIsEventSmsReceiverExecutedOnce(true);
       				}
       				else{      					
       					mPreference.putIsEventSmsReceiverExecutedOnce(false);
       				}
       			}
       		}
       	}
	}

	//	method to send sms
	private void sendSms(String phoneNumber, String message) {
		
		Log.d("eventSms","eventSmsSent");
		
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber,null,message,null,null);
	}
}