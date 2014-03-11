package com.wordpress.smdaudhilbe.bquiet.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.map.GeoFenceUtils.REMOVE_TYPE;
import com.wordpress.smdaudhilbe.bquiet.map.GeoFenceUtils.REQUEST_TYPE;
import com.wordpress.smdaudhilbe.bquiet.map.MapFragmentActivity.ErrorDialogFragment;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

public class MapActivity extends Activity implements OnItemClickListener,OnItemLongClickListener,OnClickListener{
	
	private ListView myFencesList;
	
	MySharedPreferences mPreference;
	
	static int UniqueGeoFenceId = 1;
	
	SimpleGeoFenceStore sGeoFenceStore;
	
	DataBaseConnectivity dBConnectivity;
	
	//	map pojo
	private List<MapPojo> freshList;
	
	private ArrayList<MapPojo> mapListItems;
	
	private MapListAdapter mapListAdapter;
	
	private GeoFenceRemover mGeofenceRemover;
	private GeoFenceRequester mGeofenceRequester;
	private REMOVE_TYPE mRemoveType;
	private List<String> mGeofenceIdsToRemove;
	private REQUEST_TYPE mRequestType;
    
    //	geofenceIdremoved
    String GeoFenceIdRemoved;
	
	//	Google Play Service available or not request code
	final int GOOGLE_PLAY_SERVICE_REQUEST_CODE = 99;
	private SpannableStringBuilder sBuilder;
	
	Vibrator vB;
	
	RelativeLayout rLayout;

	private Button tB1,tB2,tB3,tB4,tB5,tB6,tB7,tB8,tB9,tB10,tBDel;

	private EditText eText1,eText2,eText3,eText4;

	private ScrollView fencePasscode;

	private RelativeLayout fenceListView;

	private boolean askPasscode = true;

	private static int edExecuted = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.map);
		
		vB = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
		sBuilder = new SpannableStringBuilder("Fences");
		sBuilder.setSpan(new TypefaceSpan(this,"Purisa.ttf",getApplicationContext()),0,sBuilder.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getActionBar().setTitle(sBuilder);
				
		mPreference = new MySharedPreferences(getApplicationContext());
		
        // Instantiate a Geofence remover
        mGeofenceRemover = new GeoFenceRemover(this);
		
		//	initViews
		initViewsAndClickListeners();
		
		dBConnectivity = new DataBaseConnectivity(getApplicationContext());
		
		//	settings error
		if(!mPreference.isUserDefinedGeofencingPermissions())
			enablePermissionAlert();

		//	do this if and only if listview is visible
		//	reference to geofence preference
		else
			sGeoFenceStore = new SimpleGeoFenceStore(getApplicationContext());
		
		//	get boolean from MainActivity
		askPasscode = getIntent().getExtras().getBoolean("askPasscode");
	}	
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//	passcode verification if it is checked
		if(mPreference.isUserDefinedPasscodePermissions() && askPasscode){
			getActionBar().hide();
			fencePasscode.setVisibility(View.VISIBLE);
		}
		else{
			getActionBar().setDisplayHomeAsUpEnabled(true);
			fenceListView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {	
		super.onResume();
		
		freshList = dBConnectivity.getGeoFenceDetails();
		
		mapListItems = new ArrayList<MapPojo>();
	
		//	adding map list items
		for(MapPojo mPojo : freshList)
			mapListItems.add(mPojo);
		
		mapListAdapter = new MapListAdapter(getApplicationContext(),0,mapListItems);
	
		myFencesList.setAdapter(mapListAdapter);
		
		if(mapListAdapter.isEmpty()){
			View emptyView = (View) findViewById(R.id.fencesEmptyTextView);
			myFencesList.setEmptyView(emptyView);
		}
	}
	
	//	alert dialog
	private void enablePermissionAlert() {
		
		vB.vibrate(100);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
		
		builder.setTitle("BQuiet");
		builder.setMessage("Enable Location services? ");
		builder.setIcon(R.drawable.ic_launcher_final);
		builder.setCancelable(false);
		
		builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				mPreference.putGeoFencePermission(true);				
			}
		});
		
		builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		builder.show();
	}

	//	internet status checking method
	private boolean isPhoneHasInternet() {
		
		ConnectivityManager cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(cManager != null){
			
			NetworkInfo[] info = cManager.getAllNetworkInfo();
			
			if(info != null){
				
				for (int i = 0; i < info.length; i++)
					if(info[i].getState() == NetworkInfo.State.CONNECTED)
						return true;			
			}
		}		
		return false;
	}

	private void initViewsAndClickListeners() {
		
		rLayout = (RelativeLayout)findViewById(R.id.fencemapListLayout);		
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getAssets(),"font/Purisa.ttf"));
		
		fencePasscode = (ScrollView)findViewById(R.id.fencePassCodeLayout);
		fenceListView = (RelativeLayout)findViewById(R.id.fenceMapLayout);
		
		tB1 = (Button)findViewById(R.id.fenceTabBtn1);tB1.setOnClickListener(this);		
		tB2 = (Button)findViewById(R.id.fenceTabBtn2);tB2.setOnClickListener(this);
		tB3 = (Button)findViewById(R.id.fenceTabBtn3);tB3.setOnClickListener(this);		
		tB4 = (Button)findViewById(R.id.fenceTabBtn4);tB4.setOnClickListener(this);
		tB5 = (Button)findViewById(R.id.fenceTabBtn5);tB5.setOnClickListener(this);
		tB6 = (Button)findViewById(R.id.fenceTabBtn6);tB6.setOnClickListener(this);
		tB7 = (Button)findViewById(R.id.fenceTabBtn7);tB7.setOnClickListener(this);
		tB8 = (Button)findViewById(R.id.fenceTabBtn8);tB8.setOnClickListener(this);
		tB9 = (Button)findViewById(R.id.fenceTabBtn9);tB9.setOnClickListener(this);
		tB10 = (Button)findViewById(R.id.fenceTabBtn10);tB10.setOnClickListener(this);
		tBDel = (Button)findViewById(R.id.fenceTabBtnDel);tBDel.setOnClickListener(this);		
		
		eText1 = (EditText)findViewById(R.id.fencefirstPassCodeNum);		
		eText2 = (EditText)findViewById(R.id.fencesecondPassCodeNum);
		eText3 = (EditText)findViewById(R.id.fencethirdPassCodeNum);
		eText4 = (EditText)findViewById(R.id.fencefourthPassCodeNum);
		
		myFencesList = (ListView)findViewById(R.id.myFencesList);
		
		myFencesList.setOnItemClickListener(this);
		myFencesList.setOnItemLongClickListener(this);
	}

	//	listview item click
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		mPreference.putDontShowToast(true);
		
		Intent iTent = new Intent(this,MapFragmentActivity.class);
		
		iTent.putExtra("GeoFenceId",mapListAdapter.getItem(position).getGeoFenceId());
		
		iTent.putExtra("toDo", "get");		
		iTent.putExtra("askPasscode", false);
		
		startActivityForResult(iTent,11);
	}	

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,long id) {	
		
		if(isPhoneHasInternet()){
			
			//	vibrating
			vB.vibrate(100);
			
			GeoFenceRemover.GeoIDToRemove = mapListAdapter.getItem(position).getGeoFenceId();
		
			AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
		
			alert.setTitle("Delete Event?");
		
			alert.setIcon(R.drawable.alert);
		
			alert.setCancelable(false);
		
			alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {			

				private MapPojo item;

				@Override
				public void onClick(DialogInterface dialog, int which) {
				
					//	request type to remove
					mRequestType = GeoFenceUtils.REQUEST_TYPE.REMOVE;
				
					//	remove as list
					mRemoveType = GeoFenceUtils.REMOVE_TYPE.LIST;
				
					if(!googlePlayServiceConnected())
						return;
					
					//	geo fence id to remove				
					mGeofenceIdsToRemove = Collections.singletonList(mapListAdapter.getItem(position).getGeoFenceId());
				
					// Try to remove the geofence
					try {
						
						//	removing geofence
						mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

						item = (MapPojo) myFencesList.getAdapter().getItem(position);
						mapListAdapter.remove(item);
						mapListAdapter.notifyDataSetChanged();		            
		            
						// Catch errors with the provided geofence IDs
		        		} catch (IllegalArgumentException e) {
		            	e.printStackTrace();
		        		} catch (UnsupportedOperationException e) {
		        		// Notify user that previous request hasn't finished.
		        			Toast.makeText(getApplicationContext(), "Can\'t remove geofences, previous request hasn\'t finished.",Toast.LENGTH_LONG).show();
		        		}
					}
				});
		
			alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {				
					dialog.cancel();
				}
			});		
			alert.show();
		}
		else
    		Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_LONG).show();
	
		return false;
	}
	
	//	checking for Google Play Service installation in device
	public boolean googlePlayServiceConnected() {
		
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) 
            return true;        
        
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
                        if (GeoFenceUtils.REQUEST_TYPE.REMOVE == mRequestType){

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeoFenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

                            // If the removal was by a List of geofence IDs
                            } else {
                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                    break;
                }
                
            case 9999:
            	
            	if(resultCode == RESULT_OK)            		
            		askPasscode = intent.getExtras().getBoolean("askPasscode");

            	break;
            	
            case 11:
            	
            	if(resultCode == RESULT_OK)            		
            		askPasscode = intent.getExtras().getBoolean("askPasscode");

            	break;
        }
    }
    
    //	custom font to action bar title
    public static class TypefaceSpan extends MetricAffectingSpan {
    	
        /** An <code>LruCache</code> for previously loaded typefaces. */
      private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);
   
      private Typeface mTypeface;
   
      /**
       * Load the {@link Typeface} and apply to a {@link Spannable}.
     * @param context2 
       */
      public TypefaceSpan(Context context, String typefaceName, Context contextt) {
          mTypeface = sTypefaceCache.get(typefaceName);
   
          if (mTypeface == null) {
              mTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(),"font/Purisa.ttf");
   
              // Cache the loaded Typeface
              sTypefaceCache.put(typefaceName, mTypeface);
          }
      }
   
      public TypefaceSpan(ActionBarDrawerToggle actionBarDrawerToggle,String typefaceName, Context context) {
		
          mTypeface = sTypefaceCache.get(typefaceName);
          
          if (mTypeface == null) {
              mTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(),"font/Purisa.ttf");
   
              // Cache the loaded Typeface
              sTypefaceCache.put(typefaceName, mTypeface);
          }
	  }

	@Override
      public void updateMeasureState(TextPaint p) {
          p.setTypeface(mTypeface);
          
          // Note: This flag is required for proper typeface rendering
          p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
      }
   
      @Override
      public void updateDrawState(TextPaint tp) {
          tp.setTypeface(mTypeface);
          
          // Note: This flag is required for proper typeface rendering
          tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
      }
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {    	
    	getMenuInflater().inflate(R.menu.map, menu);    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    	
		case R.id.map_geofence_plus:
			
			if(mPreference.isUserDefinedGeofencingPermissions()){
		    	
	    		if(isPhoneHasInternet()){
	    			
	    			if(userEnabledLocationServices()){
	    				
	    				mPreference.putDontShowToast(false);
	    	
	    				Intent iTent = new Intent(this,MapFragmentActivity.class);
	    				
	    				long geoFenceId = mPreference.getUniqueGeoFenceId();	    				
			
	    				//	unique id for geofences
	    				iTent.putExtra("GeoFenceId",geoFenceId+"");
	    				iTent.putExtra("toDo", "add");
	    				iTent.putExtra("askPasscode", false);
			
	    				startActivityForResult(iTent, 9999);
	    			}
	    			//	to show alert dialog
	    			else	    				
	    				showAlertDialog();    			
	    		}
	    	
	    	else
	    		Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_LONG).show();
	    	}	    	
	    	else
	    		Toast.makeText(getApplicationContext(), "Enable location services and try again!", Toast.LENGTH_LONG).show();
			
			break;
			
			//	when home button is pressed
			case android.R.id.home:			
				Intent itent = new Intent();
				itent.putExtra("askPasscode",false);			
				setResult(22,itent);
				
				finish();
				break;
		}
    	    	
    	return true;
    }    
    
    private boolean userEnabledLocationServices() {
    	
    	LocationManager lManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		//	getting GPS status
		boolean isGPSEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		//	getting network status
		boolean isNetworkEnabled = lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);			
		
		if(!isGPSEnabled && !isNetworkEnabled){
			return false;
		}

		return true;
	}

	public void showAlertDialog() {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
		
		alert.setTitle("Enable GPS / Network Settings");
		
		alert.setMessage("Do you want to go settings?");
		
		alert.setPositiveButton("Settings",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {			
				
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		
		alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {							
				dialog.cancel();
			}
		});		
		alert.show();
	}
    
	@Override
	protected void onPause() {	
		super.onPause();
		
		fenceListView.setVisibility(View.INVISIBLE);
		askPasscode = true;		
	}

	@Override
	public void onClick(View view) {
		
		/*
		 * first launch Activity / Passcode needed page view click events
		 */
		switch (view.getId()) {
			
			//	passcode needed page	
			case R.id.fenceTabBtn1:			
				writeAndExecute("1");			
				break;
		
			case R.id.fenceTabBtn2:			
				writeAndExecute("2");			
				break;
		
			case R.id.fenceTabBtn3:			
				writeAndExecute("3");			
				break;
		
			case R.id.fenceTabBtn4:			
				writeAndExecute("4");			
				break;
		
			case R.id.fenceTabBtn5:			
				writeAndExecute("5");			
				break;
		
			case R.id.fenceTabBtn6:			
				writeAndExecute("6");			
				break;
		
			case R.id.fenceTabBtn7:			
				writeAndExecute("7");			
				break;
		
			case R.id.fenceTabBtn8:			
				writeAndExecute("8");			
				break;
		
			case R.id.fenceTabBtn9:			
				writeAndExecute("9");			
				break;
		
			case R.id.fenceTabBtn10:			
				writeAndExecute("0");				
				break;
		
			case R.id.fenceTabBtnDel:			
				writeAndExecute("Del");			
				break;
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
			
			//	changing visibility			
			//	showing actionbar for navigation drawer
			getActionBar().show();
			
			fencePasscode.setVisibility(View.INVISIBLE);
			fenceListView.setVisibility(View.VISIBLE);
		}
		
		//	for wrong passcode
		else{
			vB.vibrate(100);			
			Toast.makeText(getApplicationContext(),"Wrong passcode!",Toast.LENGTH_LONG).show();			
			eText1.setText("");eText2.setText("");eText3.setText("");eText4.setText("");
			edExecuted = 0;
		}			
	}
	
	//	go back MainActivity
	@Override
	public void onBackPressed() {
		
		Intent itent = new Intent();
		itent.putExtra("askPasscode",false);			
		setResult(22,itent);
		finish();
	}
}