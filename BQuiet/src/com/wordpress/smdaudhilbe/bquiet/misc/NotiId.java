package com.wordpress.smdaudhilbe.bquiet.misc;

import android.content.Context;

public class NotiId {
	
	private MySharedPreferences mPreference;

	public NotiId(Context context) {
		mPreference = new MySharedPreferences(context);
	}
	
	public long notiId() {
		
		long returnThisValue = mPreference.getNotificationId();
		
		if(returnThisValue == (2^64 - 1)){
			returnThisValue = 0;
			mPreference.putNotificationId(returnThisValue);
		}
		
		else{
			returnThisValue += 1;
			mPreference.putNotificationId(returnThisValue);
		}
		
		return returnThisValue;
	}
}