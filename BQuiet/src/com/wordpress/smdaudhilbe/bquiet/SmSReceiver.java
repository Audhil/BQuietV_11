package com.wordpress.smdaudhilbe.bquiet;

import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.map.SimpleGeoFenceStore;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SmSReceiver extends BroadcastReceiver {
	
	static boolean ring = false;
	static boolean callReceived = false;
	static String callerPhoneNumber = " ";
	
	DataBaseConnectivity dBConnectivity;
	
	private String[] dataFromDB;
	
	SimpleGeoFenceStore sStore;
	
	private MySharedPreferences mPreference;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		sStore = new SimpleGeoFenceStore(context);
		
		dBConnectivity = new DataBaseConnectivity(context);
		
        dataFromDB = dBConnectivity.dataOfGeoFence(sStore.getWhichGeoFenceId());
        
        mPreference = new MySharedPreferences(context);
                
        if(Boolean.parseBoolean(dataFromDB[3])){
        	
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
        			
        			if(!mPreference.isSmsReceiverExecutedOnce()){       					
        				
        				sendSms(callerPhoneNumber,dataFromDB[2]);       				
        				mPreference.putIsSmsReceiverExecutedOnce(true);
        			}
        			else{      					
        				mPreference.putIsSmsReceiverExecutedOnce(false);
        			}        			
        		}
        	}
        }
	}

	//	method to send sms
	private void sendSms(String phoneNumber, String message) {
		
		Log.d("Sms","SmsSent");
		
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber,null,message,null,null);
	}
}