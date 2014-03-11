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
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

public class Profile extends Fragment implements OnClickListener{
	
	MyFont myFont = new MyFont();
	
	EditText eText1,eText2,eText3,eText4;
	
	MySharedPreferences mPreference;
	
	String userEnteredPassCode;
	
	View rootView;	
	
	private String oldPassCodeContent = "IamnotEmpty";
	
	Vibrator vB;

	private ScrollView sCrollView;

	private TextView alertingView;
	
	private Button tB1,tB2,tB3,tB4,tB5,tB6,tB7,tB8,tB9,tB10,tBDel;

	private RelativeLayout rLayout;

	private boolean newPassCodeChange = false;
	
	static int edExecuted = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		mPreference = new MySharedPreferences(getActivity());
		
		vB = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
				
		rootView = inflater.inflate(R.layout.profile_fragment, container, false);

		sCrollView = (ScrollView)rootView.findViewById(R.id.passCodeMatter);
		
		MyFont.applyFonts(sCrollView, Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		initViewsAndClickListeners(rootView);
		
		//	if user has not enabled passcode
		if(!mPreference.isUserDefinedPasscodePermissions()){
			
			alertingView.setVisibility(View.VISIBLE);			
			rLayout.setVisibility(View.INVISIBLE);
			enablePermissionAlert();
		}
		
		else{
			rLayout.setVisibility(View.VISIBLE);
			alertingView.setVisibility(View.INVISIBLE);			
			
			newPassCodeChange  = true;
		}
		
		return rootView;
	}

	//	enabling passcode from new passcode page
	private void enablePermissionAlert() {
		
		vB.vibrate(100);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle("BQuiet");
		builder.setMessage("Enable passcode?");
		builder.setIcon(R.drawable.ic_launcher_final);
		builder.setCancelable(false);
		
		builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				
				rLayout.setVisibility(View.VISIBLE);
				alertingView.setVisibility(View.INVISIBLE);
				
				Toast.makeText(getActivity(),"Enter passcode",Toast.LENGTH_LONG).show();
			}			
		});
		
		builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				//	do nothing
			}
		});
		
		final AlertDialog alert = builder.create();
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
	
	//	views initializations
	private void initViewsAndClickListeners(View rootView) {
		
		eText1 = (EditText)rootView.findViewById(R.id.firstPassCodeNumBox);
		eText2 = (EditText)rootView.findViewById(R.id.secondPassCodeNumBox);
		eText3 = (EditText)rootView.findViewById(R.id.thirdPassCodeNumBox);
		eText4 = (EditText)rootView.findViewById(R.id.fourthPassCodeNumBox);
		
		alertingView = (TextView)rootView.findViewById(R.id.enablePasscodeMessage);
		
		rLayout = (RelativeLayout)rootView.findViewById(R.id.passCodeForm);
		
		tB1 = (Button)rootView.findViewById(R.id.TabBtnn1);tB1.setOnClickListener(this);		
		tB2 = (Button)rootView.findViewById(R.id.TabBtnn2);tB2.setOnClickListener(this);
		tB3 = (Button)rootView.findViewById(R.id.TabBtnn3);tB3.setOnClickListener(this);		
		tB4 = (Button)rootView.findViewById(R.id.TabBtnn4);tB4.setOnClickListener(this);
		tB5 = (Button)rootView.findViewById(R.id.TabBtnn5);tB5.setOnClickListener(this);
		tB6 = (Button)rootView.findViewById(R.id.TabBtnn6);tB6.setOnClickListener(this);
		tB7 = (Button)rootView.findViewById(R.id.TabBtnn7);tB7.setOnClickListener(this);
		tB8 = (Button)rootView.findViewById(R.id.TabBtnn8);tB8.setOnClickListener(this);
		tB9 = (Button)rootView.findViewById(R.id.TabBtnn9);tB9.setOnClickListener(this);
		tB10 = (Button)rootView.findViewById(R.id.TabBtnn10);tB10.setOnClickListener(this);
		tBDel = (Button)rootView.findViewById(R.id.TabBtnnDel);tBDel.setOnClickListener(this);				
	}

	@Override
	public void onClick(View view) {
		
		if(!newPassCodeChange){
			
			switch (view.getId()) {
		
			case R.id.TabBtnn1:			
				writeAndExecute("1");			
				break;
		
			case R.id.TabBtnn2:			
				writeAndExecute("2");			
				break;
		
			case R.id.TabBtnn3:			
				writeAndExecute("3");			
				break;
		
			case R.id.TabBtnn4:			
				writeAndExecute("4");			
				break;
		
			case R.id.TabBtnn5:			
				writeAndExecute("5");			
				break;
		
			case R.id.TabBtnn6:			
				writeAndExecute("6");			
				break;
		
			case R.id.TabBtnn7:			
				writeAndExecute("7");			
				break;
		
			case R.id.TabBtnn8:			
				writeAndExecute("8");			
				break;
		
			case R.id.TabBtnn9:			
				writeAndExecute("9");			
				break;
		
			case R.id.TabBtnn10:			
				writeAndExecute("0");				
				break;
		
			case R.id.TabBtnnDel:			
				writeAndExecute("Del");			
				break;

			default:
				break;
			}
		}
		
		else			
			gettingOldPasscodeDialog("BQuiet", "Enter passcode");		
	}
	
	//	method to check and execute
	private void writeAndExecute(String value) {
		
		//	if delete pressed
		if(value.equals("Del")){
			
			switch (edExecuted) {
			
			case 1:
				eText1.setText("");				
				edExecuted--;				
				break;
				
			case 2:
				eText2.setText("");				
				edExecuted--;				
				break;
				
			case 3:
				eText3.setText("");				
				edExecuted--;				
				break;
			}
		}
		
		else{
		
			if(TextUtils.isEmpty(eText1.getText())){
			
				eText1.setText(value);
				edExecuted = 1;
			}
		
			else if(TextUtils.isEmpty(eText2.getText())){
				
				eText2.setText(value);
				edExecuted = 2;
			}
		
			else if(TextUtils.isEmpty(eText3.getText())){
		
				eText3.setText(value);
				edExecuted = 3;
			}
		
			else if(TextUtils.isEmpty(eText4.getText())){
		
				eText4.setText(value);
				
				if(!mPreference.isUserDefinedPasscodePermissions())
					Toast.makeText(getActivity(), "New passcode updated!",Toast.LENGTH_LONG).show();
				
				else
					Toast.makeText(getActivity(), "Passcode updated!",Toast.LENGTH_LONG).show();
				
				mPreference.storePassCodeContent(eText1.getText().toString()+eText2.getText().toString()+eText3.getText().toString()+eText4.getText().toString());
				mPreference.putPassCodePermission(true);
					
				eText1.setText("");eText2.setText("");eText3.setText("");eText4.setText("");
				
				newPassCodeChange = true;				
			}
		}
	}

	//	dialog to get old passcode
	private void gettingOldPasscodeDialog(String Title, String Message) {
		
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
		
		builder.setNegativeButton("Cancel", null);
		
		//	preventing dialog from closing if user has entered any invalid passcode such as characters below length 4
		final AlertDialog alert = builder.create();
		
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
							oldPassCodeContent = passCode.getText().toString();							
							
							//	if user entered correct passcode
							if(mPreference.getPassCodeContent().equals(oldPassCodeContent)){
								
								Toast.makeText(getActivity(), "Type new passcode",Toast.LENGTH_LONG).show();
								newPassCodeChange = false;			
							}
							
							else
								Toast.makeText(getActivity(), "Wrong passcode",Toast.LENGTH_LONG).show();
							
							alert.cancel();							
						}
					}
				});
				
				Button cancelClicked = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
				
				cancelClicked.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {												
						alert.cancel();
					}
				});
			}
		});
		
		alert.show();
		
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
}