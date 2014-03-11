package com.wordpress.smdaudhilbe.bquiet.fragments;


import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

public class Settings extends Fragment implements OnCheckedChangeListener{
	
	Switch geoFencingSwitch,smsSwitch,passCodeSwitch;
	
	MySharedPreferences mPreference;
	
	private String newPassCodeContent = "IamEmpty";

	protected boolean showNewPassCodeDialog = true;
	
	Vibrator vB;
	
	AlertDialog alert;
	
	private boolean dontShowDialog = false;
	private boolean cancelButtonClicked = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.setting_fragment, container, false);
		
		//	vibrator
		vB = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		
		//	parent layout
		RelativeLayout rLayout = (RelativeLayout)rootView.findViewById(R.id.settingLayout);
		
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		//	init views
		initViews(rootView);
				
		//	shared preference
		mPreference = new MySharedPreferences(getActivity());
		
		//	if user has defined permissions then showing switch states when again user comes to screen next time			
			//	for geo fencing
		if(mPreference.isUserDefinedGeofencingPermissions())
			geoFencingSwitch.setChecked(true);		
			
		//	for passcode
		if(mPreference.isUserDefinedPasscodePermissions())
			passCodeSwitch.setChecked(true);		
			
		//	for sms
		if(mPreference.isUserDefinedSmsPermissions())
			smsSwitch.setChecked(true);		
		
		
		//	click and change listeners
		geoFencingSwitch.setOnCheckedChangeListener(this);
		smsSwitch.setOnCheckedChangeListener(this);
		passCodeSwitch.setOnCheckedChangeListener(this);
		
		return rootView;
	}

	/**
	 * 
	 * init Views 
	 * 
	 * */
	public void initViews(View rootView) {
		geoFencingSwitch = (Switch)rootView.findViewById(R.id.geoFencingPermissionSwitch);
		smsSwitch = (Switch)rootView.findViewById(R.id.smsPermissionSwitch);
		passCodeSwitch = (Switch)rootView.findViewById(R.id.passCodePermissionSwitch);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {	
		
		switch (buttonView.getId()) {
		
		case R.id.geoFencingPermissionSwitch:
			
			if(geoFencingSwitch.isChecked())
				mPreference.putGeoFencePermission(true);
			
			else
				disableConfirmationAlert(R.id.geoFencingPermissionSwitch);
						
			break;
			
		case R.id.smsPermissionSwitch:
			
			if(smsSwitch.isChecked())
				mPreference.putSmsPermission(true);
			
			else
				disableConfirmationAlert(R.id.smsPermissionSwitch);
			
			break;	

		case R.id.passCodePermissionSwitch:
			
			if(!cancelButtonClicked)
			if(passCodeSwitch.isChecked()){
				if(!dontShowDialog)
					if(showNewPassCodeDialog)
						gettingNewOldPasscodeDialog("BQuiet", "Enter new passcode");
			}			
			
			else{
				if(!cancelButtonClicked)
					gettingNewOldPasscodeDialog("BQuiet", "Enter old passcode");
			}
			
			break;
			
		default:			
			break;
		}		
	}
	
    //	getting new passcode
    private void gettingNewOldPasscodeDialog(String Title,final String Message) {
    	
    	vB.vibrate(100);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(Title);
		builder.setMessage(Message);
		builder.setIcon(R.drawable.ic_launcher_final);
		builder.setCancelable(false);
		
		
		final EditText passCode = new EditText(getActivity());		
		passCode.setInputType(InputType.TYPE_CLASS_NUMBER);
		passCode.setTransformationMethod(PasswordTransformationMethod.getInstance());
		passCode.setHint("....");
		passCode.setBackgroundColor(Color.WHITE);		
		
		
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(4);
		passCode.setFilters(filterArray);
		
		builder.setView(passCode);
		
		builder.setPositiveButton("OK",null);
		
		builder.setNegativeButton("Cancel",null);
		
		//	preventing dialog from closing if user has entered any invalid passcode such as characters below length 4
		alert = builder.create();
		
		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				
				Button okButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				
				okButton.setOnClickListener(new OnClickListener() {					

					@Override
					public void onClick(View v) {
						
						if(passCode.getText().toString().trim().length() < 4)
							Toast.makeText(getActivity(), "Insufficient passcode",Toast.LENGTH_LONG).show();						

						else{
							//	case for new passcode
							if(!Message.equals("Enter old passcode")){
								
								newPassCodeContent = passCode.getText().toString();							
								Toast.makeText(getActivity(), "Your passcode : "+newPassCodeContent,Toast.LENGTH_LONG).show();	
							
								//	storing passcode content inside shared preference
								mPreference.storePassCodeContent(newPassCodeContent);
								mPreference.putPassCodePermission(true);
								passCodeSwitch.setChecked(true);
							
								alert.cancel();							
							}
							//	case for old passcode
							else{
																
								String oldPassCode = passCode.getText().toString();
								
								if(oldPassCode.equals(mPreference.getPassCodeContent())){
									mPreference.deletePassCodeContent();
									mPreference.putPassCodePermission(false);
									mPreference.storeProceedFurther(true);
								}
								
								else{
									Toast.makeText(getActivity(), "Wrong passcode",Toast.LENGTH_LONG).show();
									dontShowDialog = true;
									passCodeSwitch.setChecked(true);
									dontShowDialog = false;
								}
								
								alert.cancel();
							}
						}
					}
				});
				
				Button cancelClicked = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
				
				cancelClicked.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						
						//	new passcode
						if(!Message.equals("Enter old passcode")){
							
							cancelButtonClicked = true;
							passCodeSwitch.setChecked(false);
							cancelButtonClicked = false;
							
							alert.cancel();							
						}
						//	old passcode
						else{
							
							cancelButtonClicked = true;
							passCodeSwitch.setChecked(true);
							cancelButtonClicked = false;
							
							alert.cancel();
						}
					}
				});
			}
		});		
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				alert.cancel();
				timer.cancel();
			}
		}, 6000);
		
		//	to show keypad
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		
		alert.show();	
	}
    
    //	disable confirmation alert
    public void disableConfirmationAlert(final int whichViewCalledMe) {
    	
    	vB.vibrate(100);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle("BQuiet");
		builder.setMessage("Do you want to disable it ? ");
		builder.setIcon(R.drawable.alert);
		builder.setCancelable(false);
		
		builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch (whichViewCalledMe) {
				
				case R.id.geoFencingPermissionSwitch:
					
					mPreference.putGeoFencePermission(false);
					
					break;
					
				case R.id.smsPermissionSwitch:
					
					mPreference.putSmsPermission(false);
					
					break;

				case R.id.passCodePermissionSwitch:
					
					mPreference.putGeoFencePermission(false);
					mPreference.deletePassCodeContent();
					
					break;					
					
				default:
					break;
				}				
			}
		});
		
		builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch (whichViewCalledMe) {
				
				case R.id.geoFencingPermissionSwitch:
					
					geoFencingSwitch.setChecked(true);
					
					break;
					
				case R.id.smsPermissionSwitch:
					
					smsSwitch.setChecked(true);
					
					break;

				case R.id.passCodePermissionSwitch:
					
					showNewPassCodeDialog  = false;
					passCodeSwitch.setChecked(true);					
					
					break;					
					
				default:
					break;
				}
			}
		});
		alert = builder.create();
		alert.show();
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {				
				alert.cancel();
				timer.cancel();	
			}
		}, 6000);
	}
}