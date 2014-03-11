package com.wordpress.smdaudhilbe.bquiet.fragments;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.KeyBoardHide;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.misc.MyTimePickerDialog;
import com.wordpress.smdaudhilbe.bquiet.silent.SchedulingReceiver;

public class Create extends Fragment implements OnClickListener, OnCheckedChangeListener,MyTimePickerDialog.TimePickerInterface{
	
	EditText eventName,smsContent,timeToStart,timeMinute,timeAmPm,timeFinishHour,timeFinishMinute,timeFinishAmPm;
	
	Switch smsOnOffSwitch,eventRepeatSwitch;
	
	Button submitButton;
	
	MySharedPreferences mPreference;
	
	int hourOfTheDay,minutes;
	
	String durationOfTheEvent = "No duration Selected",repeatTheEvent = "false",smsTheEvent = "false";
	
	DataBaseConnectivity db;

	int hourToFinish,minutesToFinish;

	private String smsSetting,eventSmsSetting;
	
	InputMethodManager imgr;

	private KeyBoardHide keyboardHide;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.create_fragment, container, false);
		
		//	parent layout
		RelativeLayout rLayout = (RelativeLayout)rootView.findViewById(R.id.createLayout);
		
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		//	reference of shared preference
		mPreference = new MySharedPreferences(getActivity());
			
		//	for database
		db = new DataBaseConnectivity(getActivity());
		
		initViewsAndClickListener(rootView);
		
		keyboardHide = new KeyBoardHide(getActivity());
		
		imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);		
		
		eventName.requestFocus();
		
		KeyBoardHide.editText = eventName;
		KeyBoardHide.editTextAvailable = true;
		
		imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		
		return rootView;
	}

	/**
	 *	initializing all views 
	 **/
	private void initViewsAndClickListener(View rootView) {
		
		eventName = (EditText)rootView.findViewById(R.id.eventName);
		
		timeToStart = (EditText)rootView.findViewById(R.id.Hour);
		timeMinute = (EditText)rootView.findViewById(R.id.Minute);
		timeAmPm = (EditText)rootView.findViewById(R.id.AMPM);
		
		timeFinishHour = (EditText)rootView.findViewById(R.id.finishHour);
		timeFinishMinute = (EditText)rootView.findViewById(R.id.finishMinute);
		timeFinishAmPm = (EditText)rootView.findViewById(R.id.finishAMPM);
		
		smsContent = (EditText)rootView.findViewById(R.id.smsContent);
		smsContent.setLongClickable(false);
		smsContent.setOnClickListener(this);
		
		smsOnOffSwitch = (Switch)rootView.findViewById(R.id.smsOnOffSwitch);
		eventRepeatSwitch = (Switch)rootView.findViewById(R.id.eventRepeatSwitch);
		
		submitButton = (Button)rootView.findViewById(R.id.doneButton);
		
		submitButton.setOnClickListener(this);
		timeToStart.setOnClickListener(this);
		timeMinute.setOnClickListener(this);
		timeAmPm.setOnClickListener(this);
		
		timeFinishHour.setOnClickListener(this);
		timeFinishMinute.setOnClickListener(this);
		timeFinishAmPm.setOnClickListener(this);
		
		smsOnOffSwitch.setOnCheckedChangeListener(this);
		smsOnOffSwitch.setOnClickListener(this);
		eventRepeatSwitch.setOnCheckedChangeListener(this);
		
		//	sms matter
		if(!mPreference.isUserDefinedSmsPermissions()){
			smsSetting = "false";
			eventSmsSetting = "false";
		}
			
		else{
			smsSetting = "true";
			eventSmsSetting = "false";
		}
	}
	
	@Override
	public void onClick(View view) {
		
		//	edittext which holds starting time
		if(view.equals(timeToStart)){
			
			if(!TextUtils.isEmpty(timeFinishHour.getText())){
				timeFinishHour.setText("");
				timeFinishMinute.setText("");
				timeFinishAmPm.setText("");
				
				timeToStart.setText("");
				timeMinute.setText("");
				timeAmPm.setText("");
			}
			
			else if(!TextUtils.isEmpty(timeToStart.getText())){
				timeToStart.setText("");
				timeMinute.setText("");
				timeAmPm.setText("");
			}
			
			showTimePickerDialog("startingTime");
			
			keyboardHide.hideTheKeyBoard();
		}
		
		else if(view.equals(timeMinute)){
			
			if(!TextUtils.isEmpty(timeFinishHour.getText())){
				timeFinishHour.setText("");
				timeFinishMinute.setText("");
				timeFinishAmPm.setText("");
				
				timeToStart.setText("");
				timeMinute.setText("");
				timeAmPm.setText("");
			}
			
			showTimePickerDialog("startingTime");
			keyboardHide.hideTheKeyBoard();
		}
		
		else if(view.equals(timeAmPm)){
			
			if(!TextUtils.isEmpty(timeFinishHour.getText())){
				timeFinishHour.setText("");
				timeFinishMinute.setText("");
				timeFinishAmPm.setText("");
				
				timeToStart.setText("");
				timeMinute.setText("");
				timeAmPm.setText("");
			}
			
			showTimePickerDialog("startingTime");
			
			keyboardHide.hideTheKeyBoard();
		}
		
		else if(view.equals(timeFinishHour)){
			
			keyboardHide.hideTheKeyBoard();
			
			if(!TextUtils.isEmpty(timeToStart.getText()))			
				showTimePickerDialog("finishingTime");
			
			else
				Toast.makeText(getActivity(), "Please fill starting time.", Toast.LENGTH_LONG).show();
		}
		
		else if(view.equals(timeFinishMinute)){
			
			keyboardHide.hideTheKeyBoard();
			
			if(!TextUtils.isEmpty(timeToStart.getText()))			
				showTimePickerDialog("finishingTime");
			
			else
				Toast.makeText(getActivity(), "Set starting time", Toast.LENGTH_LONG).show();		
		}
		
		else if(view.equals(timeFinishAmPm)){
			
			keyboardHide.hideTheKeyBoard();
			
			if(!TextUtils.isEmpty(timeToStart.getText()))			
				showTimePickerDialog("finishingTime");
			
			else
				Toast.makeText(getActivity(), "Please fill starting time.", Toast.LENGTH_LONG).show();
		}
		
		//	atlast submit button
		else if(view.equals(submitButton)){
			
			keyboardHide.hideTheKeyBoard();
			
			HashMap<String,String> temp = new HashMap<String, String>();
			
			//	storing event name
			temp.put("eventName",eventName.getText().toString());
			
			//	storing starting time			
			String hour = timeToStart.getText().toString();
			String minute = timeMinute.getText().toString();
			String tT = timeAmPm.getText().toString();
			
			if(tT.equals("PM"))
				hour = (Integer.parseInt(hour)+12)+"";
			
			temp.put("hourToStart",hour);
			temp.put("minuteToStart",minute);
			
			Log.d("hour to Start",hour);
			Log.d("minute to Start",minute);
			
			//	storing finishing time
			hour = timeFinishHour.getText().toString();
			minute = timeFinishMinute.getText().toString();
			tT = timeFinishAmPm.getText().toString();
			
			if(tT.equals("PM"))
				hour = (Integer.parseInt(hour)+12)+"";
			
			temp.put("hourToFinish", hour);
			temp.put("minuteToFinish", minute);
			
			Log.d("hour to end",hour);
			Log.d("minute to end",minute);
			
			//	repeat or not
			temp.put("repeatTheEvent", repeatTheEvent);
			
			//	store if sms activated				
			temp.put("smsTheEvent",eventSmsSetting);
			temp.put("smsContent",smsContent.getText().toString());			

			
			//	if user has not completely filled the form
			if(TextUtils.isEmpty(eventName.getText()) || TextUtils.isEmpty(timeToStart.getText()) || TextUtils.isEmpty(timeFinishHour.getText()))
				Toast.makeText(getActivity(), "Insufficient data..", Toast.LENGTH_LONG).show();			
			
			else if(smsContent.getText().toString().equals("") && eventSmsSetting.equals("true"))
				Toast.makeText(getActivity(), "Fill SMS content..", Toast.LENGTH_LONG).show();
			
			else if(hourOfTheDay == hourToFinish && minutes == minutesToFinish){
				
				Toast.makeText(getActivity(), "Event should last atleast for 1 minute", Toast.LENGTH_LONG).show();
				timeFinishHour.setText("");
				timeFinishMinute.setText("");
				timeFinishAmPm.setText("");				
			}
			//	storing to database
			else{
				
				long timeToStartInMillis = hourOfTheDay * 60 * 60 * 1000+minutes * 60 * 1000;
				temp.put("timeToStartInMillis",timeToStartInMillis+"");
				
				long timeToFinishInMillis = hourToFinish * 60 * 60 * 1000 + minutesToFinish * 60 * 1000;
				temp.put("timeToFinishInMillis",timeToFinishInMillis+"");
				
				if((timeToFinishInMillis - timeToStartInMillis) < 0)
					Toast.makeText(getActivity(), "Your event extends upto next day", Toast.LENGTH_LONG).show();
				
				//	duration of Event
				temp.put("durationOfTheEvent", (timeToFinishInMillis - timeToStartInMillis)+"");
				
				//	deleting deleted eventName to prevent from conflicts later
				for (int i = 0; i < db.getDeletedAlarmNames().size(); i++) {
					
					if(!db.getDeletedAlarmNames().get(i).equals("no_deleted_alarms")){
						
						//	delete the name inside database
						if(eventName.getText().toString().equals(db.getDeletedAlarmNames().get(i))){

							db.deleteDeletedAlarmNames(eventName.getText().toString());
							break;
						}
					}
				}
				
				//	clear text
				if(db.storeNewEvent(temp) > 0){
										
					eventName.setText("");timeToStart.setText("");timeMinute.setText("");timeAmPm.setText("");
					timeFinishHour.setText("");timeFinishMinute.setText("");timeFinishAmPm.setText("");
					smsContent.setText("");
					
					if(smsOnOffSwitch.isChecked())
						smsOnOffSwitch.setChecked(false);
					
					if(eventRepeatSwitch.isChecked())
						eventRepeatSwitch.setChecked(false);					
					
					//	scheduling events at onBootReceiver
					scheduleEvents();
				}
				
				else{
					if(mPreference.getWhichisDuplicated().equals("eventName")){
						eventName.setText("");
						timeToStart.setText("");timeMinute.setText("");timeAmPm.setText("");
						timeFinishHour.setText("");timeFinishMinute.setText("");timeFinishAmPm.setText("");
					}
					
					else{
						timeToStart.setText("");timeMinute.setText("");timeAmPm.setText("");
						timeFinishHour.setText("");timeFinishMinute.setText("");timeFinishAmPm.setText("");
					}
				}
			}
		}
		
		else if(view.equals(smsContent)){						
			keyboardHide.hideTheKeyBoard();			
			Toast.makeText(getActivity(), "Please turn ON Send SMS ",Toast.LENGTH_LONG).show();					
		}
	}

	//	showing time picker dialog
	private void showTimePickerDialog(String whichTimePicker) {
		
		DialogFragment dFragment = new MyTimePickerDialog();		
		
		//	passing which view called this timepicker
		Bundle bdl = new Bundle();
		bdl.putString("whichTimePicker", whichTimePicker);
				
		//	saying that this is the target destination for dialog fragment
		dFragment.setTargetFragment(this,0);
						
		//	passing name of the view above which TimePicker value to be displayed
		dFragment.setArguments(bdl);
		
		//	dialog fragment will not be cancelled by clicking somewhere (other than) dialog in screen
		dFragment.setCancelable(false);
		
		//	displaying dialog
		dFragment.show(getFragmentManager(), "dummyTimePickerTag");	
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	
		//	repeat this event		
		switch (buttonView.getId()) {
		
		case R.id.eventRepeatSwitch:
			
			if(eventRepeatSwitch.isChecked()){
				Toast.makeText(getActivity(), "This event will repeat daily",Toast.LENGTH_LONG).show();
				repeatTheEvent = "true";
			}
			
			else if(!eventRepeatSwitch.isChecked()){				
				repeatTheEvent = "false";
			}
			
			break;
	
		//	sms on / off switch
		case R.id.smsOnOffSwitch:
			
			if(smsOnOffSwitch.isChecked()){
				
				smsContent.setLongClickable(true);
				
				if(!mPreference.isUserDefinedSmsPermissions())			
					smsEnablingAlert();									
				
				else
					choosingSmsContentAlert();
			}
	
			else if(!smsOnOffSwitch.isChecked()){
				
				keyboardHide.hideTheKeyBoard();
				
				smsContent.setLongClickable(false);
				
				if(!Boolean.parseBoolean(smsSetting)){
					
					smsContent.setText("");
					eventSmsSetting = "false";
				}
				
				else{
					smsContent.setText("");
					smsContent.setHint("SMS text");
					smsContent.setFocusable(false);
					eventSmsSetting = "false";
				}				
			}			
			break;			
		}
	}
	
	//	to enable sms or not
	private void smsEnablingAlert() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle("BQuiet");
		builder.setMessage("Enable SMS settings ? ");
		builder.setIcon(R.drawable.ic_launcher_final);
		builder.setCancelable(false);
		
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mPreference.putSmsPermission(true);
				choosingSmsContentAlert();
			}
		});
		
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				smsContent.setText("");
				eventSmsSetting = "false";
				smsContent.setFocusable(false);
				
				smsOnOffSwitch.setChecked(false);
			}
		});
		
		builder.show();
	}
	
	//	sms choosing alert
	private void choosingSmsContentAlert() {
		
		final CharSequence[] items = {"Use Template", "My Own Message"};
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							
		builder.setTitle("Choose to Compose Message");
		builder.setCancelable(false);
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			
		    public void onClick(DialogInterface dialog, int item) {
		    	
		    	//	for custom message
		    	if(items[item].equals("My Own Message")){
		    		
		    		dialog.dismiss();					    		
					smsContent.setFocusableInTouchMode(true);		
					smsContent.setHint("Type your SMS");
					smsContent.setText("");								
					eventSmsSetting = "true";
					
					smsContent.requestFocus();
					
					KeyBoardHide.editText = smsContent;
					KeyBoardHide.editTextAvailable = true;
				
					imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				}   	
		    	
		    	else if(items[item].equals("Use Template")){
		    		
		    		dialog.dismiss();					    		
					
					final CharSequence[] temps = {"Sorry, I'm Busy Call back later ","I'm Driving","I'm at the cinema","I'm in Class","I'm in a meeting"};
		    		
		    		//	show list of templates
		    		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Choose any Template");
					builder.setCancelable(false);
					builder.setSingleChoiceItems(temps, -1, new DialogInterface.OnClickListener() {
						
					    public void onClick(DialogInterface dialog, int item) {
					    	
					    	dialog.dismiss();
					    	smsContent.setFocusableInTouchMode(true);		
					    	smsContent.setText(temps[item]);
							eventSmsSetting = "true";
					    }								    	
					});
					builder.create().show();
		    	}					        
		    }
		});					
		builder.create().show();		
	}	

	@Override
	public void timePickerDoneClicked(int hourOfDay, int minute, String whichTimePicker) {	
		
		if(whichTimePicker.equals("startingTime")){
			
			hourOfTheDay = hourOfDay;
			minutes = minute;
		}
		else if(whichTimePicker.equals("finishingTime")){			
			
			hourToFinish = hourOfDay;
			minutesToFinish = minute;
			
			if(hourToFinish == hourOfTheDay){
				
				if(minutesToFinish == minutes){
					
					Toast.makeText(getActivity(), "Event should last atleast for 1 minute",Toast.LENGTH_LONG).show();
					
					timeFinishHour.setText("");
					timeFinishMinute.setText("");
					timeFinishAmPm.setText("");
					
					return;
				}
			}
			
			//	event duration calculation
			int startMinutes = hourOfTheDay * 60 + minutes;
			int stopMinutes = hourToFinish * 60 + minutesToFinish;
			int eventDuration = 0;
			
			if(stopMinutes < startMinutes)
				eventDuration = (1440 - startMinutes) + stopMinutes;
			
			else
				eventDuration = stopMinutes - startMinutes;
			
			Toast.makeText(getActivity(), "This event will last for "+eventDuration / 60+" hours and "+eventDuration % 60+" mins",Toast.LENGTH_LONG).show();								
		}		
		
		String hour = "",min = "",mer = "";
		
		if (hourOfDay > 12) {
			
			int temp = hourOfDay -12;			
        	hour = temp+"";
            mer = "PM";            
            
        } else if (hourOfDay == 0) {
            
        	int temp = hourOfDay +12;
        	hour = temp+"";
            mer = "AM";              
        }
        
        else if(hourOfDay < 12){
        	
        	int temp = hourOfDay;			
        	hour = temp+"";
            mer = "AM";    
        	
        }else if (hourOfDay == 12){
        	       	
        	int temp = hourOfDay;			
        	hour = temp+"";
        	mer = "PM";
        }
        else
            mer = "AM";
		        
        if (minute < 10)
            min = "0" + minute;
        else
            min = String.valueOf(minute);
        
		if(whichTimePicker.equals("startingTime")){

			timeToStart.setText(hour);        
			timeMinute.setText(min);
			timeAmPm.setText(mer);
		}
		
		else if(whichTimePicker.equals("finishingTime")){
			
			timeFinishHour.setText(hour);
			timeFinishMinute.setText(min);
			timeFinishAmPm.setText(mer);
		}
	}
	
	//	scheduling events
	private void scheduleEvents() {
		
		// Getting current time and add the seconds in it (I mean by next second the alarm will go off and call broadcast receiver)
		Calendar cal = Calendar.getInstance();		
		
		Intent sIntent = new Intent(getActivity(),SchedulingReceiver.class);
		PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(),0,sIntent,0);
		
		
		AlarmManager aManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);		
		aManager.set(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pIntent);
	}
}
