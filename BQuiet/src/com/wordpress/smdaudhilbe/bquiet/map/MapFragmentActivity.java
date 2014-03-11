package com.wordpress.smdaudhilbe.bquiet.map;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.map.GeoFenceUtils.REQUEST_TYPE;
import com.wordpress.smdaudhilbe.bquiet.misc.KeyBoardHide;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

@SuppressLint({ "InlinedApi", "DefaultLocale" }) 
public class MapFragmentActivity extends FragmentActivity implements OnClickListener,OnCheckedChangeListener,OnMapClickListener,OnCameraChangeListener{
	
	//	Google Map
	GoogleMap gMap;
	
	//	Google Play Service available or not request code
	final int GOOGLE_PLAY_SERVICE_REQUEST_CODE = 99;

	private EditText myEventName,mySmsContent,myFenceRadius;
	
	LinearLayout l1,l2;

	private Switch smsOnOff;

	private Button saveButton,cancelButton,changeMapButton;

	private MySharedPreferences mPreference;

	protected String smsActivated = "false";

	private GpsNetworkLocationProviders gPSnETworkLocationProviders;
	
	//	Geo fence expiration time - I am setting it to NEVER_EXPIRE
	private static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;	
	
	//	Geo fence object
	SimpleGeoFence sGeoFence;

	//	Reference to SharedPreference
	SimpleGeoFenceStore sGeoFenceStorePref;
	
	//	list of all GeoFences
	List<Geofence> sGeoFenceList;
	
    // decimal formats for latitude, longitude, and radius
    private DecimalFormat mLatLngFormat;
    private DecimalFormat mRadiusFormat;
    
    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;
    
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    //	geoFence requester
	private GeoFenceRequester mGeofenceRequester;

	private REQUEST_TYPE mRequestType;
	
	//	geofence Id
	String GeoFenceId;
	
	double finalLat = 0,finalLng = 0;

	private float presentZoomLevel;

	private double myRadius;

	private String smsSetting;

	private String eventSmsSetting;

	//	database connectivity
	private DataBaseConnectivity dBConnectivity;

	private String toDo;

	private KeyBoardHide keyboardHide;

	private InputMethodManager imgr;

	private Geocoder geoCoder;

	private List<Address> listAddress;

	private String City;

	private String thisTheEvent;
	
	private Button tB1,tB2,tB3,tB4,tB5,tB6,tB7,tB8,tB9,tB10,tBDel;

	private EditText eText1,eText2,eText3,eText4;

	private Vibrator vB;

	private ScrollView fenceFragPassCode;

	private RelativeLayout rLayout;

	private boolean askPasscode = true;	
	
	private static int edExecuted = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.map_fragment);
		
		//	vibrator
		vB = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
		//	keypad hide
		keyboardHide = new KeyBoardHide(getApplicationContext());
		
		//	softkeypad hide
		imgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		//	geocoder to get location names
		geoCoder = new Geocoder(getApplicationContext());
		
		//	applying my font
		RelativeLayout rLayout = (RelativeLayout)findViewById(R.id.map_Fragment_Layout);
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getAssets(),"font/Purisa.ttf"));	
		
		mPreference = new MySharedPreferences(getApplicationContext());
		
		gPSnETworkLocationProviders = new GpsNetworkLocationProviders(getApplicationContext());
		
		//	getting geo fence Id
		GeoFenceId = getIntent().getExtras().getString("GeoFenceId");
		
		toDo = getIntent().getExtras().getString("toDo");
		
		initViewsAndListeners();
		
		//	database connectivity
		dBConnectivity = new DataBaseConnectivity(getApplicationContext());
		
		//	initializing map
		if(googlePlayServiceConnected()){			
			initializeMap();				
			geoFenceInit();
		}
		
		//	get boolean from MapActivity
		askPasscode = getIntent().getExtras().getBoolean("askPasscode");
	}

	//	geofence init
	private void geoFenceInit() {
		
		//	shared preference
		sGeoFenceStorePref = new SimpleGeoFenceStore(MapFragmentActivity.this);
		
		//	active list of geofences
		sGeoFenceList = new ArrayList<Geofence>();
		
		// Set the pattern for the latitude and longitude format
        String latLngPattern = getString(R.string.lat_lng_pattern);

        // Set the format for latitude and longitude
        mLatLngFormat = new DecimalFormat(latLngPattern);

        // Localize the format
        mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());

        // Set the pattern for the radius format
        String radiusPattern = getString(R.string.radius_pattern);

        // Set the format for the radius
        mRadiusFormat = new DecimalFormat(radiusPattern);

        // Localize the pattern
        mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());
        
        //	geofence transition receiver
        mBroadcastReceiver = new GeofenceSampleReceiver();        
        
        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeoFenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeoFenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeoFenceUtils.CATEGORY_LOCATION_SERVICES);
        
        // Instantiate a Geofence requester
        mGeofenceRequester = new GeoFenceRequester(this);
	}

	//	init Views and listeners
	private void initViewsAndListeners() {
		
		myEventName = (EditText)findViewById(R.id.mapEventNameEText);
		mySmsContent = (EditText)findViewById(R.id.mapSmsContentEText);
		
		//	hint color to white
		myEventName.setHintTextColor(Color.DKGRAY);
		mySmsContent.setHintTextColor(Color.DKGRAY);
		
		smsOnOff = (Switch)findViewById(R.id.mapSmsSwitch);
		myFenceRadius = (EditText)findViewById(R.id.mapRadiusEText);		
		
		//	hint color to white
		myFenceRadius.setHintTextColor(Color.DKGRAY);
		
		changeMapButton = (Button)findViewById(R.id.changeMapView);
		saveButton = (Button)findViewById(R.id.mapSaveButton);
		cancelButton = (Button)findViewById(R.id.mapCancelButton);
		
		l1 = (LinearLayout)findViewById(R.id.linear1);
		l2 = (LinearLayout)findViewById(R.id.linear2);
		
		myFenceRadius.setOnClickListener(this);
		mySmsContent.setOnClickListener(this);
		
		smsOnOff.setOnCheckedChangeListener(this);		
		
		//	sms matter
		if(!mPreference.isUserDefinedSmsPermissions()){
			smsSetting = "false";
			eventSmsSetting = "false";
		}
		
		else{
			smsSetting = "true";
			eventSmsSetting = "false";
		}
		
		tB1 = (Button)findViewById(R.id.fenceFragTabBtn1);tB1.setOnClickListener(this);		
		tB2 = (Button)findViewById(R.id.fenceFragTabBtn2);tB2.setOnClickListener(this);
		tB3 = (Button)findViewById(R.id.fenceFragTabBtn3);tB3.setOnClickListener(this);		
		tB4 = (Button)findViewById(R.id.fenceFragTabBtn4);tB4.setOnClickListener(this);
		tB5 = (Button)findViewById(R.id.fenceFragTabBtn5);tB5.setOnClickListener(this);
		tB6 = (Button)findViewById(R.id.fenceFragTabBtn6);tB6.setOnClickListener(this);
		tB7 = (Button)findViewById(R.id.fenceFragTabBtn7);tB7.setOnClickListener(this);
		tB8 = (Button)findViewById(R.id.fenceFragTabBtn8);tB8.setOnClickListener(this);
		tB9 = (Button)findViewById(R.id.fenceFragTabBtn9);tB9.setOnClickListener(this);
		tB10 = (Button)findViewById(R.id.fenceFragTabBtn10);tB10.setOnClickListener(this);
		tBDel = (Button)findViewById(R.id.fenceFragTabBtnDel);tBDel.setOnClickListener(this);		
		
		eText1 = (EditText)findViewById(R.id.fenceFragfirstPassCodeNum);		
		eText2 = (EditText)findViewById(R.id.fenceFragsecondPassCodeNum);
		eText3 = (EditText)findViewById(R.id.fenceFragthirdPassCodeNum);
		eText4 = (EditText)findViewById(R.id.fenceFragfourthPassCodeNum);
		
		fenceFragPassCode = (ScrollView)findViewById(R.id.fenceFragPassCodeLayout);
		rLayout = (RelativeLayout)findViewById(R.id.actualMapFrag);
	} 

	//	create map
	private void initializeMap() {
		
		if (gMap == null) {
            gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.MyMap)).getMap();
 
            // check if map is created successfully or not
            if (gMap == null) 
                Toast.makeText(getApplicationContext(),"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            
            //	disabling following icons from viewing
    		gMap.getUiSettings().setCompassEnabled(false);
    		gMap.getUiSettings().setZoomControlsEnabled(false);
    		gMap.getUiSettings().setMyLocationButtonEnabled(false);            
    		
            //	place a pointer to my location
    		//	hide if opened in offline - using "get" parameter
    		if(!toDo.equals("get"))
    			gMap.setMyLocationEnabled(true);
            
    		//	map click event
    		//	disable if opened in offline - using "get" parameter
    		if(!toDo.equals("get"))
    			gMap.setOnMapClickListener(this);
    		
    		//	zoom
    		gMap.setOnCameraChangeListener(this);
            
            
            //	getting location of user and displaying at map on startup either getting values from GPS or Network provider
            if(gPSnETworkLocationProviders.canGetLocation()){
            	
            	double latitude = gPSnETworkLocationProviders.getLatitude();
            	double longitude = gPSnETworkLocationProviders.getLongitude();
            	
            	//	showing it on map            	
            	CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude,longitude)).zoom(15).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                
                //	stop to get location updates
                gPSnETworkLocationProviders.stopUsingGpsNetworkLocationProviders();
            }
        	//	alert user to enable gps / network provider for any error
            else
            	gPSnETworkLocationProviders.showAlertDialog();                     
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//	passcode verification if it is checked
		if(mPreference.isUserDefinedPasscodePermissions() && askPasscode){
			getActionBar().hide();
			fenceFragPassCode.setVisibility(View.VISIBLE);
		}
		else
			rLayout.setVisibility(View.VISIBLE);		
	}
	
	
	
	@Override
	protected void onResume() {	
		super.onResume();		
	
		ActionBar actionBar = getActionBar();
		actionBar.hide();		
		
		if(toDo.equals("get")){
			
			mySmsContent.setVisibility(View.INVISIBLE);
			myEventName.setVisibility(View.INVISIBLE);
			smsOnOff.setVisibility(View.INVISIBLE);
			myFenceRadius.setVisibility(View.INVISIBLE);
			saveButton.setVisibility(View.INVISIBLE);
			cancelButton.setVisibility(View.INVISIBLE);
			
			changeMapButton.setVisibility(View.INVISIBLE);
			l1.setVisibility(View.INVISIBLE);
			l2.setVisibility(View.INVISIBLE);
			
		
			if(googlePlayServiceConnected()){
				initializeMap();
			
	        // Register the broadcast receiver to receive status updates
	        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
	        
	        /*
	         * Get existing geofences from the latitude, longitude, and
	         * radius values stored in SharedPreferences. If no values
	         * exist, null is returned.
	         */
	        sGeoFence = sGeoFenceStorePref.getGeoFence(GeoFenceId);
	        
	        //	there is geofences
	        if(sGeoFence != null){
	        	
	        	double lat,lng;
	        	float radius;
	        	
	        	lat = sGeoFence.getLatitude();
	        	lng = sGeoFence.getLongitude();
	        	radius = sGeoFence.getRadius();
	        	
	            LatLng sfLatLng = new LatLng(lat,lng);
	            
            	//	show geofence on map
	        	gMap.addMarker(new MarkerOptions().position(sfLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
	        	
	        	gMap.addCircle(new CircleOptions().center(sfLatLng).radius(radius).fillColor(Color.parseColor("#80B2A9F6")));
	        	
            	CameraPosition cameraPosition = new CameraPosition.Builder().target(sfLatLng).zoom(15).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));                
	        	}
			}
		}
	}		
	
	//	checking for Google Play Service installation in device
	public boolean googlePlayServiceConnected() {
		
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        }
        
        // If Google Play services is not available
        else if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){       

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, GOOGLE_PLAY_SERVICE_REQUEST_CODE);            
            
            if (dialog != null) {
            	
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                
                errorFragment.show(getFragmentManager(), GeoFenceUtils.APPTAG);
                
                return false;
            }            
        }
        return false;
	}
	
	//	error dialog fragment to show google play service is unavailable
	public static class ErrorDialogFragment extends DialogFragment{
		
		private Dialog eDialog;
		
		public ErrorDialogFragment() {
			super();
			eDialog = null;
		}

		public void setDialog(Dialog dialog) {
			eDialog = dialog;
		}		
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			//	cannot be cancelable
			eDialog.setCancelable(false);			
			return eDialog;
		}
	}
	
    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeoFenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeoFenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(sGeoFenceList);
                        }
                        
                    break;
                }
        }
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
				checkPassCode(eText1.getText().toString()+eText2.getText().toString()+eText3.getText().toString()+eText4.getText().toString());
			}
		}
	}
	
//	checking done here
	private void checkPassCode(String userEnteredPassCode) {
		
		//	for correct passcode
		if(mPreference.getPassCodeContent().equals(userEnteredPassCode)){
			
			//	clearing editText views
			eText1.setText("");eText2.setText("");eText3.setText("");eText4.setText("");
			
			fenceFragPassCode.setVisibility(View.INVISIBLE);
			rLayout.setVisibility(View.VISIBLE);
		}
		
		//	for wrong passcode
		else{
			vB.vibrate(100);			
			Toast.makeText(getApplicationContext(),"Wrong passcode!",Toast.LENGTH_LONG).show();			
			eText1.setText("");eText2.setText("");eText3.setText("");eText4.setText("");
			edExecuted = 0;
		}			
	}

	//	choosing radius of Geo Fence
	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.mapSmsContentEText:
			
			if(!smsOnOff.isChecked())
				Toast.makeText(getApplicationContext(),"Turn ON switch", Toast.LENGTH_LONG).show();
			
			keyboardHide.hideTheKeyBoard();			
			break;
			
		case R.id.mapRadiusEText:
				
			final CharSequence[] temps = {"50 mtrs","100 mtrs","200 mtrs","500 mtrs","1000 mtrs","2000 mtrs","3000 mtrs","4000 mtrs"};
		
			//	show list of templates
			final AlertDialog.Builder builder = new AlertDialog.Builder(MapFragmentActivity.this);
		
			builder.setTitle("Radius of the Fence");
			builder.setCancelable(false);
			builder.setSingleChoiceItems(temps,-1, new DialogInterface.OnClickListener() {
			
				public void onClick(DialogInterface dialog, int item) {
		    	
					dialog.cancel();		
					myFenceRadius.setText(temps[item]);
		    	
					String[] temp = myFenceRadius.getText().toString().split(" ");
					myRadius = Double.parseDouble(temp[0]);
				}								    	
			});
			
			builder.show();
			
			keyboardHide.hideTheKeyBoard();	
			break;
		
		
		//	passcode needed page	
		case R.id.fenceFragTabBtn1:			
			writeAndExecute("1");			
			break;
			
		case R.id.fenceFragTabBtn2:			
			writeAndExecute("2");			
			break;
			
		case R.id.fenceFragTabBtn3:			
			writeAndExecute("3");			
			break;
			
		case R.id.fenceFragTabBtn4:			
			writeAndExecute("4");			
			break;
			
		case R.id.fenceFragTabBtn5:			
			writeAndExecute("5");			
			break;
			
		case R.id.fenceFragTabBtn6:			
			writeAndExecute("6");			
			break;
			
		case R.id.fenceFragTabBtn7:			
			writeAndExecute("7");			
			break;
			
		case R.id.fenceFragTabBtn8:			
			writeAndExecute("8");			
			break;
			
		case R.id.fenceFragTabBtn9:			
			writeAndExecute("9");			
			break;
			
		case R.id.fenceFragTabBtn10:			
			writeAndExecute("0");				
			break;
		
		case R.id.fenceFragTabBtnDel:			
			writeAndExecute("Del");			
			break;
		}
	}
	
	//	to enable sms or not
	private void smsEnablingAlert() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MapFragmentActivity.this);
		
		builder.setTitle("BQuiet");
		builder.setMessage("Enable SMS? ");
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
				
				mySmsContent.setText("");
				eventSmsSetting = "false";
				mySmsContent.setFocusable(false);
				
				smsOnOff.setChecked(false);
			}
		});
		
		builder.show();
	}
	
	//	sms choosing alert
	private void choosingSmsContentAlert() {
		
		final CharSequence[] items = {"Use Template", "My Own Message"};
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(MapFragmentActivity.this);
							
		builder.setTitle("Choose to Compose Message");
		builder.setCancelable(false);
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			
		    public void onClick(DialogInterface dialog, int item) {
		    	
		    	//	for custom message
		    	if(items[item].equals("My Own Message")){
		    		
		    		dialog.dismiss();					    		
		    		mySmsContent.setFocusableInTouchMode(true);		
		    		mySmsContent.setHint("Type your SMS");
		    		mySmsContent.setText("");								
					eventSmsSetting = "true";
					
					mySmsContent.requestFocus();
					
					KeyBoardHide.editText = mySmsContent;
					KeyBoardHide.editTextAvailable = true;
				
					imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				}   	
		    	
		    	else if(items[item].equals("Use Template")){
		    		
		    		dialog.dismiss();					    		
					
					final CharSequence[] temps = {"Sorry, I am Busy Call back later ","I'm Driving","I'm at the cinema","I'm in Class","I'm in a meeting"};
		    		
		    		//	show list of templates
		    		final AlertDialog.Builder builder = new AlertDialog.Builder(MapFragmentActivity.this);
					builder.setTitle("Choose any Template");
					builder.setCancelable(false);
					builder.setSingleChoiceItems(temps, -1, new DialogInterface.OnClickListener() {
						
					    public void onClick(DialogInterface dialog, int item) {
					    	
					    	dialog.dismiss();
					    	mySmsContent.setFocusableInTouchMode(true);		
					    	mySmsContent.setText(temps[item]);
							eventSmsSetting = "true";
					    }								    	
					});
					builder.create().show();
		    	}					        
		    }
		});					
		builder.create().show();
		
		keyboardHide.hideTheKeyBoard();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		keyboardHide.hideTheKeyBoard();
		
		if(smsOnOff.isChecked()){
			
			mySmsContent.setHint("Type your SMS");
			
			mySmsContent.setLongClickable(true);
			
			if(!mPreference.isUserDefinedSmsPermissions())	
				smsEnablingAlert();									
			
			else
				choosingSmsContentAlert();
		}

		else if(!smsOnOff.isChecked()){
			
			keyboardHide.hideTheKeyBoard();
			
			mySmsContent.setHint("SMS text");			
			
			mySmsContent.setFocusable(false);
			
			mySmsContent.setLongClickable(false);
			
			if(!Boolean.parseBoolean(smsSetting)){
				
				mySmsContent.setText("");
				eventSmsSetting = "false";
			}
			
			else{
				mySmsContent.setText("");
				mySmsContent.setHint("SMS text");
				mySmsContent.setFocusable(false);
				eventSmsSetting = "false";
			}				
		}						
	}
	
	@Override
	public void onBackPressed() {
	
		Intent iTent = new Intent();
		iTent.putExtra("askPasscode",false);
		setResult(RESULT_OK,iTent);	
		
		finish();
	}
	
	//	handler of Cancel button - finish activity
	public void CancelClicked(View view) {
		
		Intent iTent = new Intent();
		iTent.putExtra("askPasscode",false);
		setResult(RESULT_OK,iTent);	
		
		finish();
	}
	
	//	handler of Save button to save Geofencing
	public void SaveClicked(View view) {
		
		mRequestType = GeoFenceUtils.REQUEST_TYPE.ADD;
		
		if(!googlePlayServiceConnected())
			return;
		
		if(!validateUserInputs())
			return;
		
        /*
         * Create a version of geofence that is "flattened" into individual fields. This
         * allows it to be stored in SharedPreferences.
         */
		
		//	radius
		String[] temp = myFenceRadius.getText().toString().split(" ");
		float radius = Float.parseFloat(temp[0]);
		
		String EventName = myEventName.getText().toString();
		String EventSmSContent = mySmsContent.getText().toString();
		String EventSmSActive = eventSmsSetting;
		String EventGeoFenceRadius = temp[0];
		
		//	creating geofence
		sGeoFence = new SimpleGeoFence(GeoFenceId,finalLat,finalLng,radius,GEOFENCE_EXPIRATION_TIME,Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
		sGeoFenceStorePref.setGeoFence(GeoFenceId, sGeoFence);
		sGeoFenceList.add(sGeoFence.toGeofence());
		
		//	storing references in database
		dBConnectivity.storeGeoFenceDetails(new String[]{GeoFenceId,EventName,EventSmSContent,EventSmSActive,EventGeoFenceRadius,City,finalLat+":"+finalLng});
		
		try {			
			//	adding geofence
			mGeofenceRequester.addGeofences(sGeoFenceList);
			
			Toast.makeText(getApplicationContext(),"Fence created!",Toast.LENGTH_LONG).show();
			
			//	convey success message to previous activity
			Intent iTent = new Intent();
			iTent.putExtra("askPasscode",false);
			setResult(RESULT_OK,iTent);			
									
			clearFieldsAndMarker();
			
		} catch (UnsupportedOperationException e) {
			
            Toast.makeText(getApplicationContext(),"Can't add geofences, previous request hasn't finished.",Toast.LENGTH_LONG).show();
		}			
	}

	//	clear
	private void clearFieldsAndMarker() {
		
		myEventName.setText("");
		mySmsContent.setText("");
		myFenceRadius.setText("");
		
		smsOnOff.setChecked(false);
		
		gMap.clear();
	}

	//	validating user inputs
    private boolean validateUserInputs() {

    	boolean inputOK = true;
    	
    	//	if phone loses internet connection when submitting geofence
    	if(!isPhoneHasInternet()){
    		Toast.makeText(getApplicationContext(), "No Internet connection",Toast.LENGTH_LONG).show();   			
   			inputOK = false;
   			return inputOK;
    	}
    	
   		//	unfilled fields
   		if(TextUtils.isEmpty(myEventName.getText()) || TextUtils.isEmpty(myFenceRadius.getText())){   		
   			Toast.makeText(getApplicationContext(), "Insufficient data",Toast.LENGTH_LONG).show();   			
   			inputOK = false;
   			return inputOK;
   		}
    		
   		if(finalLat > GeoFenceUtils.MAX_LATITUDE || finalLat < GeoFenceUtils.MIN_LATITUDE || finalLat == 0){
   			Toast.makeText(getApplicationContext(), "Invalid Latitude",Toast.LENGTH_LONG).show();
   			inputOK = false;
   			return inputOK;
   		}
    		
   		if(finalLng > GeoFenceUtils.MAX_LONGITUDE || finalLng < GeoFenceUtils.MIN_LONGITUDE || finalLng == 0){
   			Toast.makeText(getApplicationContext(), "Invalid Longitude",Toast.LENGTH_LONG).show();
   			inputOK = false;
   			return inputOK;
   		}   		
		return inputOK;
	}
    
    //	internet status checking method
	private boolean isPhoneHasInternet() {
		
		ConnectivityManager cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(cManager != null){
			
			NetworkInfo[] info = cManager.getAllNetworkInfo();
			
			if(info != null){
				for (int i = 0; i < info.length; i++) {
					if(info[i].getState() == NetworkInfo.State.CONNECTED)
						return true;
				}
			}
		}		
		return false;
	}

	/**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
    	
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
    	
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeoFenceUtils.ACTION_GEOFENCE_ERROR)) {
                handleGeofenceError(context, intent);
                
            // Intent contains information about successful addition or removal of geofences
            } else if (TextUtils.equals(action, GeoFenceUtils.ACTION_GEOFENCES_ADDED)
            			||TextUtils.equals(action, GeoFenceUtils.ACTION_GEOFENCES_REMOVED)) {
                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeoFenceUtils.ACTION_GEOFENCE_TRANSITION)) {
                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else 
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();            
        }

		private void handleGeofenceTransition(Context context, Intent intent) {
			
		}

		private void handleGeofenceStatus(Context context, Intent intent) {
			
		}

		private void handleGeofenceError(Context context, Intent intent) {
			
            String msg = intent.getStringExtra(GeoFenceUtils.EXTRA_GEOFENCE_STATUS);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
    }

    //	placing marker
	@SuppressWarnings("unused")
	@Override
	public void onMapClick(LatLng point) {
		
		keyboardHide.hideTheKeyBoard();
		
		int finalRadius = 0;
		
		finalLat = point.latitude;
		finalLng = point.longitude;		
		
		//	restricting to add marker on maximum zoom - 21.0
		if(presentZoomLevel < 20.0){
			Toast.makeText(getApplicationContext(), "Indefinite region. Zoom in",Toast.LENGTH_LONG).show();
		}
		
		else if(TextUtils.isEmpty(myFenceRadius.getText())){
			Toast.makeText(getApplicationContext(), "Select radius of your fence",Toast.LENGTH_LONG).show();
		}
		
		else if(checkPoints(point,myRadius)){
			//	do nothing
		}
				
		else{			
			gMap.clear();
			
			gMap.addMarker(new MarkerOptions().position(point).draggable(true));
    		gMap.addCircle(new CircleOptions().center(point).radius(myRadius).fillColor(Color.parseColor("#80B2A9F6")));		
    		
    		Address address;
    		
    		String[] arrayOfRdSt = {"rd","road","st","street","highway","town","village"};
    		
    		//	getting location names
    		try {    			
    			   			
				listAddress = geoCoder.getFromLocation(finalLat, finalLng,1);				
				
				address = listAddress.get(0);
				
				if(listAddress != null && listAddress.size() > 0){					
					
					String localAddress = address.getAddressLine(0);
					
					if(!localAddress.equals(null) && localAddress.length() > 0){
						
						if(localAddress.contains(",")){
							
							String[] afterSplit = localAddress.split(",");				
							
							City = afterSplit[afterSplit.length - 1].trim();
							
							try {								
								Long isLongAvailable = Long.parseLong(City);
								
								if(afterSplit.length > 1)
								{
									City = afterSplit[afterSplit.length - 2].trim();
								}
								
								else
									City = "Undefined Name";				
								
							} catch (NumberFormatException e) {
								City = afterSplit[afterSplit.length - 1].trim();
							}
							
							//	converting to lowercase and matching
							if(Arrays.asList(arrayOfRdSt).contains(City.toLowerCase()))
								if(afterSplit.length > 1)
									City = afterSplit[afterSplit.length - 2].trim()+" "+City;
						}
						
						else if(localAddress.contains(" ")){

							String[] afterSplit = localAddress.split(" ");				
							
							City = afterSplit[afterSplit.length - 1].trim();
							
							try {								
								Long isLongAvailable = Long.parseLong(City);
								
								if(afterSplit.length > 1)
								{
									City = afterSplit[afterSplit.length - 2].trim();
								}
								
								else
									City = "Undefined Name";				
								
							} catch (NumberFormatException e) {
								City = afterSplit[afterSplit.length - 1].trim();
							}
							
//							converting to lowercase and matching
							if(Arrays.asList(arrayOfRdSt).contains(City.toLowerCase()))
							{
								if(afterSplit.length > 1)
								{
									City = afterSplit[afterSplit.length - 2].trim()+" "+City;
								}
							}
						}
						//	fail
						else{

							if(address.getLocality() != null)
							{
								City = address.getLocality();
							}
							else if(address.getCountryName() != null)
							{
								City = address.getCountryName();								
							}
							else
								City = "Undefined Name";
						}

					}
					//	getAddress is null
					else
						City = "Undefined Name";
				}
				//	list null
				else
					City = "Undefined Name";
				
			} catch (Exception e) {				
				
				City = "Undefined Name";				
			}    		
		}	
	}

	//	checkPoints
	private boolean checkPoints(LatLng point,Double RadiusOfPresentFence) {
		
		String[] radiusNLatLng = dBConnectivity.getLatLngNRadiusValues(),dataLatLng;
		String lat = "0",lng = "0",radius = "0";
		float distance;

		Location locationA = new Location("point A");
		Location locationB = new Location("point B");		
		
		//	for first time execution
		if(!mPreference.isFirstTimeGeoFenceExecuted())			
			mPreference.putIsFirstTimeGeoFenceExecuted(true);			
		
		//	for second time execution
		else{				
			
			for (int i = 0; i < radiusNLatLng.length; i++) {				
				
				if((i % 2) == 0)
					radius = radiusNLatLng[i];
				
				else{
					dataLatLng = radiusNLatLng[i].split(":");
					
					lat = dataLatLng[0];
					lng = dataLatLng[1];
					
					//	getting event name too
					thisTheEvent = dBConnectivity.getGeoFenceEventNameToUseAtToast(radiusNLatLng[i]);
				}
				
				//	location A is old geofence
				locationA.setLatitude(Double.parseDouble(lat));
				locationA.setLongitude(Double.parseDouble(lng));
				
				//	location B is present geofence
				locationB.setLatitude(point.latitude);
				locationB.setLongitude(point.longitude);
				
				//	calculating distance
				distance = locationA.distanceTo(locationB);
				
				//	if the second point lies inside or at border of existing fence
				if(distance <= Float.parseFloat(radius)){
					
					Toast.makeText(getApplicationContext(), "Fence overlaps \""+thisTheEvent+"\" fence",Toast.LENGTH_LONG).show();
					
					return true;
				}
				
				else if(distance < (Float.parseFloat(radius)+RadiusOfPresentFence)){
					
					Toast.makeText(getApplicationContext(), "Fence overlaps \""+thisTheEvent+"\" fence",Toast.LENGTH_LONG).show();

					return true;
				}				
			}			
		}		
		return false;				
	}

	//	to get zoom value
	@Override
	public void onCameraChange(CameraPosition position) {		
		presentZoomLevel = position.zoom;
	}
	
	//	changing map view
	public void changeMapViewClicked(View view){
		
		if(changeMapButton.getText().toString().equals("H")){
			changeMapButton.setText("N");
			gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}
		
		else{
			changeMapButton.setText("H");
			gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		
		keyboardHide.hideTheKeyBoard();
	}
	
	@Override
	protected void onPause() {	
		super.onPause();
		
		rLayout.setVisibility(View.INVISIBLE);
		askPasscode = true;		
	}
}