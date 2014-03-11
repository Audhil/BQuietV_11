package com.wordpress.smdaudhilbe.bquiet.database;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wordpress.smdaudhilbe.bquiet.map.MapPojo;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.model.EventsListItem;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class DataBaseConnectivity extends SQLiteOpenHelper {

	private static final String DB_NAME = "BQuietDataBase";
	
	private static String DB_PATH = "/data/data/com.wordpress.smdaudhilbe.bquiet/databases/";
	
	private Context context;

	//	details that come from user
	private HashMap<String, String> details;

	private MySharedPreferences myPreference;

	public DataBaseConnectivity(Context context) {
		super(context, DB_NAME,null,1);	
		this.context = context;
		myPreference = new MySharedPreferences(context);
	}
	
	//	creating a empty database
	public void createDB() throws IOException{
		
		boolean dbExist = checkDB();
		
		if(dbExist)
			;//	do nothing
		
		else
		{
			//	creating an empty database into the default system path / so that we can replace it with our own
			this.getReadableDatabase();
			
			copyDB();
		}
	}
	
	//	checking for database
	private boolean checkDB() {
		
		SQLiteDatabase checkdb = null;
		
		try {
			String myPath = DB_PATH+DB_NAME;
			
			checkdb = SQLiteDatabase.openDatabase(myPath, null,SQLiteDatabase.OPEN_READONLY);
			
		} catch (SQLException e) {
			//	nothing to do
		}
		
		//	if database exists the close it
		if(checkdb != null)
			checkdb.close();
		
		return checkdb != null ? true : false;		
	}

	//	copying database
	private void copyDB() {
		
		//	opening the local db at asset/ in inputStream
		InputStream in = null;
		try {
			in = context.getAssets().open(DB_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//	path to just created empty DB
		String outPath = DB_PATH+DB_NAME;
		
		//	open the empty db at output stream
		OutputStream out = null;
		try {
			out = new FileOutputStream(outPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//	buffer (byte rates) from input to output
		byte[] buffer = new byte[1024];
		
		int length;
		
		//	copying from local db to system empty db
		try {
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer,0,length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//	closing
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	@Override
	public void onCreate(SQLiteDatabase db) {

		/*	creating table to hold events	*/
		db.execSQL("create table createEvents (eventName text primary key not null," +
												"hourToStart text not null," +
												"minuteToStart text not null," +
												"durationOfTheEvent text not null," +
												"repeatTheEvent text not null," +
												"smsTheEvent text not null," +
												"smsContent text not null," +
												"hourToFinish text not null," +
												"minuteToFinish text not null,"+
												"timeToStartInMillis text not null," +
												"timeToFinishInMillis text not null);");
		
		db.execSQL("create table binEvents (eventName text not null," +
												"hourToStart text not null," +
												"minuteToStart text not null," +
												"durationOfTheEvent text not null," +
												"repeatTheEvent text not null," +
												"smsTheEvent text not null," +
												"smsContent text not null," +
												"hourToFinish text not null," +
												"minuteToFinish text not null,"+
												"timeToStartInMillis text not null," +
												"timeToFinishInMillis text not null);");
		
		db.execSQL("create table mapIds (geoFenceId text not null," +
										"geoFenceEventName text not null," +
										"geoFenceSmsContent text not null," +
										"geoFenceSmsActivated text not null," +
										"geoFenceRadius text not null," +
										"geoFenceCity text not null," +
										"geoFenceLatLng text not null);");
		
		db.execSQL("create table deletedAlarms(DeletedAlarmNames text not null);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("drop table if exists createEvents;");
		db.execSQL("drop table if exists binEvents;");		
		db.execSQL("drop table if exists mapIds;");
		db.execSQL("drop table if exists deletedAlarms;");
		
		onCreate(db);
	}
	
	/*
	 * 
	 * retrieve data from database
	 * 
	 */
	public List<EventsListItem> getDataFromDatabase() {
		
		List<EventsListItem> list = new ArrayList<EventsListItem>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query("createEvents", new String[] {"eventName", 
															   "repeatTheEvent",
															   "smsTheEvent", 
															   "smsContent", 
															   "timeToStartInMillis", 
															   "timeToFinishInMillis"}, null, null,null,null,"timeToStartInMillis"+" DESC");
		
		if(cursor.moveToFirst()){
		
			do{
				EventsListItem eVlistItem = new EventsListItem();
			
				//	eventName
				eVlistItem.setEvent(cursor.getString(0).toString());
			
				//	repeatevent
				eVlistItem.setRepeatVisible(Boolean.parseBoolean(cursor.getString(1)));
			
				//	smsevent
				eVlistItem.setSmsVisible(Boolean.parseBoolean(cursor.getString(2)));				
			
				//	smscontent
				eVlistItem.setSms(cursor.getString(3).toString());
			
				//	eventStartTimeinMillis
				eVlistItem.setStartTime(cursor.getString(4).toString());				
			
				//	eventFinishTimeinMillis
				eVlistItem.setFinishTime(cursor.getString(5).toString());				
			
				list.add(eVlistItem);
			
			}while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		return list;
	}
	
	/**
	 * 
	 * Storing events inside database
	 * 
	 * */
	public int storeNewEvent(HashMap<String,String> vals) {
		
		details = vals;
		
		//	before entering check for duplicates inside database
		//	get a key named eventName from HashMap
		if(isDuplicated() && isTimeDuplicated()){
		
			SQLiteDatabase db = this.getWritableDatabase();
		
			ContentValues cValues = new ContentValues();
		
			//	retriving keys and values from HashMap
			Set<Entry<String,String>> sE = vals.entrySet();
			
			Iterator<Entry<String,String>> iTerator = sE.iterator();
			
			while (iTerator.hasNext()) {					
				Map.Entry<String,String> mE = (Map.Entry<String, String>)iTerator.next();
			
				cValues.put(mE.getKey().toString(),mE.getValue().toString());
			}
			
			db.insert("createEvents", null, cValues);
			db.close();
		
			Toast.makeText(context, "Done.",Toast.LENGTH_LONG).show();
			return 1;
		}
		
		else{
			if(!isDuplicated())
				myPreference.putWhichisDuplicated("eventName");
			
			else if(!isTimeDuplicated())
				myPreference.putWhichisDuplicated("eventTime");
		}
		return 0;
	}

	/**
	 * 
	 * 	checking for duplicates
	 * 
	 */
	private boolean isDuplicated() {
		
		//	first checking with eventName since it is primary key in my case
		String eventName = details.get("eventName");
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from createEvents where eventName='"+eventName+"';", null);
		
		//	checks whether cursor is empty
		//	moveToFirst() - This method will return false if the cursor is empty
		if(cursor.moveToFirst()){
			
			Toast.makeText(context,"You already registered \""+eventName+"\" event", Toast.LENGTH_LONG).show();
			
			cursor.close();
			db.close();
			
			return false;
		}
		//	cursor is not empty - data is not available there so allow to get data from user 
		else{
			
			cursor.close();
			db.close();
			
			return true;
		}	
	}
	
	/* *
	 * time duplicated
	 * */
	public boolean isTimeDuplicated() {
		
		Long timeToStartInMillis = Long.parseLong(details.get("timeToStartInMillis"));
		
		Long timeToFinishInMillis = Long.parseLong(details.get("timeToFinishInMillis"));	
		
		//	check first is there any other event to get start at same time
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from createEvents;", null);
		
		if(cursor.moveToFirst()){
			
			do {
				long start = Long.parseLong(cursor.getString(9));
				long stop = Long.parseLong(cursor.getString(10));
				boolean stopTime = false,startTime = false;
				
				//	event eating
				for(long i = timeToStartInMillis;i <= timeToFinishInMillis;i++){
				
					if(start > timeToStartInMillis && stop < timeToFinishInMillis){
						
						if(!(timeToFinishInMillis < start)){
						
							Toast.makeText(context, "This event will eat Event : \""+cursor.getString(0)+"\"",Toast.LENGTH_LONG).show();
							cursor.close();
							db.close();
							return false;
						}
					}
				}				
				
				//	event clashing
				for (long i = start; i <= stop; i++) {
					
					//	checking if myevent time lies in between start and stop time of previous events
					if(timeToStartInMillis == i){
						startTime = true;
						break;
					}
					
					else if(timeToFinishInMillis == i){
						stopTime = true;
						break;
					}										
				}				
				
				int hourOfDay = Integer.parseInt(cursor.getString(1));						
				int minute = Integer.parseInt(cursor.getString(2));
				
				int finishHour = Integer.parseInt(cursor.getString(7));
				int finishMinute = Integer.parseInt(cursor.getString(8));
				
				String hour = "",min = "",mer = "",fhour = "",fmin = "",fmer = ""; 
				
				//	starting time
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
		        
		        
		        //	finishing time
		        if (finishHour > 12) {
					
					int temp = finishHour -12;			
		        	fhour = temp+"";
		            fmer = "PM";            
		            
		        } else if (finishHour == 0) {
		            
		        	int temp = finishHour +12;
		        	fhour = temp+"";
		            fmer = "AM";              
		        }
		        
		        else if(finishHour < 12){
		        	
		        	int temp = finishHour;			
		        	fhour = temp+"";
		            fmer = "AM";    
		        	
		        }else if (finishHour == 12){
		        	       	
		        	int temp = finishHour;			
		        	fhour = temp+"";
		        	fmer = "PM";
		        }
		        else
		            fmer = "AM";
				        
		        if (finishMinute < 10)
		            fmin = "0" + finishMinute;
		        else
		            fmin = String.valueOf(finishMinute);
			
				
				if(stopTime){				
						
					Toast.makeText(context, "This event's period clashes with \""+cursor.getString(0)+"\""+" ( "+hour+":"+min+" "+mer+" - "+fhour+":"+fmin+" "+fmer+" )",Toast.LENGTH_LONG).show();
					
					cursor.close();
					db.close();
					return false;
				}
				else if(startTime){
					Toast.makeText(context, "This event's period clashes with \""+cursor.getString(0)+"\""+" ( "+hour+":"+min+" "+mer+" - "+fhour+":"+fmin+" "+fmer+" )",Toast.LENGTH_LONG).show();
					
					cursor.close();
					db.close();
					return false;
				}				
			} while (cursor.moveToNext());	
			
			cursor.close();
			db.close();
			return true;
		}
		//	there is no data available at database or myevent does not coincide with other events
		else{
			cursor.close();
			db.close();
			return true;						
		}
	}
	
	/**
	 * remove this line from createEvents table and insert the same into binTable
	 * */	
	public void removeFromCreateEventsMoveToBinEvents(String event) {	
		
		String[] data = new String[11];

		//	reading
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from createEvents where eventName='"+event+"';", null);
		
		//	copying content inside string array
		if(cursor.moveToFirst()){
			for(int i = 0;i < 11;i++)
				data[i] = cursor.getString(i);
			
			//	deleting the particular row in createEvents table
			db.delete("createEvents", "eventName=?",new String[]{event});
			
			cursor.close();
			db.close();
		}
		
		cursor.close();
		db.close();
		
		//	writing
		db = this.getWritableDatabase();
		
		ContentValues cValues = new ContentValues();
				
		String[] keys = new String[]{"eventName","hourToStart","minuteToStart","durationOfTheEvent","repeatTheEvent","smsTheEvent","smsContent",
										"hourToFinish","minuteToFinish","timeToStartInMillis","timeToFinishInMillis"};

		//	inserting the data inside binEvents table
		for(int i = 0;i < keys.length;i++)
			cValues.put(keys[i],data[i]);	
	
		db.insert("binEvents", null, cValues);		
		db.close();

//		Toast.makeText(context, "Data Saved inside database binEvents",Toast.LENGTH_LONG).show();
	}

	/**
	 * to populate binTable
	 * 
	 * */	
	public List<EventsListItem> getDataFromDatabaseBinTable() {
		
		List<EventsListItem> list = new ArrayList<EventsListItem>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query("binEvents", new String[] {"eventName", 
															   "timeToStartInMillis", 
															   "timeToFinishInMillis"}, null, null,null,null,"timeToStartInMillis"+" DESC");
		
		if(cursor.moveToFirst()){
		
			do{
				EventsListItem eVlistItem = new EventsListItem();
			
				//	eventName
				eVlistItem.setEvent(cursor.getString(0));
			
				//	eventStartTimeinMillis
				eVlistItem.setStartTime(cursor.getString(1));				
			
				//	eventFinishTimeinMillis
				eVlistItem.setFinishTime(cursor.getString(2));				
			
				list.add(eVlistItem);
			
			}while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		return list;
	}

	/**
	 * 
	 * removing any event completely from app
	 * removing from table binEvents
	 * 
	 * @param eventName
	 */
	public void removeEventCompletely(String eventName) {
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		//	deleting the particular row in createEvents table
		db.delete("binEvents", "eventName=?",new String[]{eventName});		
		
		db.close();
	}
	
	/**
	 * restoring method
	 * @param eventToBeRestored 
	 * 
	 */
	public boolean tryRestoringItemsTocreateEventTable(String eventToBeRestored) {
		
		HashMap<String,String> vals = new HashMap<String, String>();
		
		String[] keys = new String[]{"eventName","hourToStart","minuteToStart","durationOfTheEvent","repeatTheEvent","smsTheEvent","smsContent",
				"hourToFinish","minuteToFinish","timeToStartInMillis","timeToFinishInMillis"};
		
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("select * from binEvents where eventName='"+eventToBeRestored+"';", null);
		
		//	copying content inside string array
		if(cursor.moveToFirst()){
			
			for(int i = 0;i < 11;i++)
				vals.put(keys[i], cursor.getString(i));
			
			cursor.close();
			db.close();
		}
		
		//	inserting inside createEvents table
		if(storeNewEvent(vals) > 0){
			
			SQLiteDatabase dbb = this.getReadableDatabase();
			
			dbb.delete("binEvents", "eventName=?",new String[]{eventToBeRestored});
			
			dbb.close();
			return true;
		}
		
		//	try to store inside createEvents table		
		return false;
	}	
	
	//	storing geofenceId in mapId
	public void storeGeoFenceDetails(String[] details) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cValues = new ContentValues();
		
		String[] keys = new String[]{"geoFenceId","geoFenceEventName","geoFenceSmsContent","geoFenceSmsActivated","geoFenceRadius","geoFenceCity","geoFenceLatLng"};

		//	inserting the data inside binEvents table
		for(int i = 0;i < keys.length;i++){
			cValues.put(keys[i],details[i]);
		}

		db.insert("mapIds",null,cValues);		
		db.close();
	}
	
	//	getting geofence details
	public List<MapPojo> getGeoFenceDetails() {
		
		List<MapPojo> list = new ArrayList<MapPojo>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from mapIds;",null);
		
		if(cursor.moveToFirst()){
			
			do{				
				MapPojo mPojo = new MapPojo();
				
				mPojo.setGeoFenceId(cursor.getString(0).toString());
				
				mPojo.setGeoFenceEventName(cursor.getString(1).toString());
				
				mPojo.setGeoFenceRadius(cursor.getString(4).toString()+" mtrs");
				
				mPojo.setGeoFenceCity(cursor.getString(5).toString());		
				
				mPojo.setGeoFenceLatLng(cursor.getString(6).toString());
				
				list.add(mPojo);				
				
			}while(cursor.moveToNext());
		}		
		
		cursor.close();
		db.close();
		
		return list;
	}
	
	//	delete geofence details
	public void deleteGeoFenceDetails(String geoFenceId) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.delete("mapIds","geoFenceId=?",new String[]{geoFenceId});
		
		db.close();
	}
	
	public String[] dataOfGeoFence(String FenceId) {
		
		String[] data = new String[7];
		
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("select * from mapIds where geoFenceId='"+FenceId+"';", null);
		
		//	copying content inside string array
		if(cursor.moveToFirst()){
			
			for(int i = 0;i < 7;i++)
				data[i] = cursor.getString(i);
			
			cursor.close();
			db.close();
		}
		
		cursor.close();
		db.close();
		
		return data;
	}
	
	//	Atlast I needed this for EventSmsReceiver
	public String getEventSmsContent(String eventName) {
		
		String smsContent = "no_data";
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from createEvents where eventName='"+eventName+"';", null);
		
		if(cursor.moveToFirst()){
			
			smsContent = cursor.getString(6);
			
			cursor.close();
			db.close();
		}	
		
		cursor.close();
		db.close();
		
		return smsContent;
	}
	
	//	Atlast again I need this
	public String[] getDetailsOfEventToCancelAlarmOfThisEvent(String eventName) {
		
		String[] requiredData = new String[4];
		
		SQLiteDatabase db = this.getReadableDatabase();
			
		Cursor cursor = db.rawQuery("select * from createEvents where eventName='"+eventName+"';", null);
			
		//	checks whether cursor is empty
		//	moveToFirst() - This method will return false if the cursor is empty
		if(cursor.moveToFirst()){
			
			//	eventName
			requiredData[0] = cursor.getString(0);			
				
			//	smsActive
			requiredData[1] = cursor.getString(5);
			
			//	smsContent
			requiredData[2] = cursor.getString(6);
			
			//	repeatTheEvent
			requiredData[3] = cursor.getString(4);
						
			cursor.close();
			db.close();
		}
		
		cursor.close();
		db.close();
		
		return requiredData;
	}
	
	/**
	 * logic change
	 * */
//	Atlast again I need this
	public String[] getDetailsOfAnEvent(String eventName) {
		
		String[] requiredData = new String[6];
		
		SQLiteDatabase db = this.getReadableDatabase();
			
		Cursor cursor = db.rawQuery("select * from createEvents where eventName='"+eventName+"';", null);
			
		//	checks whether cursor is empty
		//	moveToFirst() - This method will return false if the cursor is empty
		if(cursor.moveToFirst()){
			
			//	eventName
			requiredData[0] = cursor.getString(0);
			
			//	repeatTheEvent
			requiredData[1] = cursor.getString(4);
				
			//	smsActive
			requiredData[2] = cursor.getString(5);
			
			//	smsContent
			requiredData[3] = cursor.getString(6);			
			
			//	startTimeInMillis
			requiredData[4] = cursor.getString(9);
					
			//	finishTimeInMillis
			requiredData[5] = cursor.getString(10);
						
			cursor.close();
			db.close();
		}		
		
		cursor.close();
		db.close();
		
		return requiredData;
	}
	
	//	gettingEventName
	public String getGeoFenceEventNameToUseAtToast(String latLng) {
		
		String fenceName = "no_fence";
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from mapIds where geoFenceLatLng='"+latLng+"';", null);
		
		if(cursor.moveToFirst()){
			fenceName = cursor.getString(1);			
		}
		
		cursor.close();
		db.close();
		
		return fenceName;
	}
	
	//	getting latlng to compare
	public String[] getLatLngNRadiusValues() {
				
		int i = 0,length = 0;
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from mapIds;", null);
		
		//	determining length
		if(cursor.moveToFirst()){
			
			do {
				length++;				
			} while(cursor.moveToNext());		
		}		
		
		String[] radiusNLatLng = new String[length * 2];
		
		if(cursor.moveToFirst()){
			
			do{					
				//	radius
				radiusNLatLng[i++] = cursor.getString(4);
				
				//	latlng
				radiusNLatLng[i++] = cursor.getString(6);
				
			} while (cursor.moveToNext());
			
			cursor.close();
			db.close();
		}
		
		cursor.close();
		db.close();
		
		return radiusNLatLng;
	}
	
	//	final try
	public List<String> getAlarmNamesToSchedule() {
		
		List<String> listOfAlarms = new ArrayList<String>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from createEvents;",null);
		
		if(cursor.moveToFirst()){			
			do {				
				listOfAlarms.add(cursor.getString(0));				
			} while (cursor.moveToNext());			
			cursor.close();
			db.close();
		}
		
		cursor.close();
		db.close();
		
		return listOfAlarms;
	}
	
	//	cancelAlarm names
	public void storeDeletedAlarmNames(String deletedAlarm) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cValues = new ContentValues();
		
		cValues.put("DeletedAlarmNames",deletedAlarm);		

		db.insert("deletedAlarms",null,cValues);	
		
		db.close();
	}
	
	public List<String> getDeletedAlarmNames() {		
		
		List<String> deletedAlarmNames = new ArrayList<String>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from deletedAlarms;",null);
		
		if(cursor.moveToFirst()){
			
			do {
				deletedAlarmNames.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		
		else
			deletedAlarmNames.add("no_deleted_alarms");
		
		cursor.close();
		db.close();
		
		return deletedAlarmNames;
	}
	
	public void deleteDeletedAlarmNames(String deleteEventName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.delete("deletedAlarms","DeletedAlarmNames=?",new String[]{deleteEventName});
		
		db.close();
	}
}