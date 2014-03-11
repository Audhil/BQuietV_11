package com.wordpress.smdaudhilbe.bquiet.map;

import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class GpsNetworkLocationProviders extends Service implements LocationListener {

	private Context context;
	private LocationManager lManager;
	private Location location;
	private boolean isGPSEnabled,isNetworkEnabled,canGetLocation;
	private double latitude,longitude;
	private MySharedPreferences mPreference;
	
	//	minimum distance in meter at which it can update location
	public static final long MIN_DISTANCE_TO_CHANGE_UPDATES = 10;
	
	//	minimum time between each updates in millisecond
	public static final long MIN_TIME_TO_CHANGE_LOCATION = 1000 * 60 * 1;

	public GpsNetworkLocationProviders(Context context) {
		
		mPreference = new MySharedPreferences(context);
		this.context = context;
		getLocationOfUser();
	}
	
	//	get location of user
	private void getLocationOfUser() {
		
		try {
			
			lManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			
			//	getting GPS status
			isGPSEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
			//	getting network status
			isNetworkEnabled = lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);			
			
			if(!isGPSEnabled && !isNetworkEnabled){
				//	note both of them is not enabled hence do nothing
			}else{
				
				this.canGetLocation = true;
				
				//	first get location from GPS services
				if(isGPSEnabled){
					
					lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_TO_CHANGE_LOCATION,MIN_DISTANCE_TO_CHANGE_UPDATES,this);
					
					if(lManager != null){
						location = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						
						if(location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
							
						if(!mPreference.getDontShowToast())
							Toast.makeText(context, "Geo location with GPS",Toast.LENGTH_LONG).show();
						}
					}
				}
				
				//	second get location from network provider
				if(isNetworkEnabled){
					
					lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_TO_CHANGE_LOCATION,MIN_DISTANCE_TO_CHANGE_UPDATES,this);
					
					if(lManager != null){
						location = lManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						
						if(location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						
						if(!mPreference.getDontShowToast())
							Toast.makeText(context, "Geo location with Network provider",Toast.LENGTH_LONG).show();
						}
					}
				}				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//	returning latitude
	public double getLatitude() {
		
		if(location != null)
			latitude = location.getLatitude();
		
		return latitude;		
	}
	
	//	returning longitude
	public double getLongitude() {
		
		if(location != null)
			longitude = location.getLongitude();
		
		return longitude;
	}
	
	//	function to check if best network provider
	public boolean canGetLocation(){
		return this.canGetLocation;
	}
	
	//	to show alert dialog if GPS is not enabled
	public void showAlertDialog() {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		
		alert.setTitle("Enable GPS / Network Settings");
		
		alert.setMessage("Do you want to go settings?");
		
		alert.setPositiveButton("Settings",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {			
				
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent);
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
	
	//	stop using GpsNetworkLocationProviders
	public void stopUsingGpsNetworkLocationProviders() {
		
		if(lManager != null){			
			lManager.removeUpdates(GpsNetworkLocationProviders.this);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {	
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
}