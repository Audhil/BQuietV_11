package com.wordpress.smdaudhilbe.bquiet.adapter;

import java.util.ArrayList;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.model.EventsListItem;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BinListAdapter extends ArrayAdapter<EventsListItem> {
	
	private ArrayList<EventsListItem> item;
	private Context context;
	
	DataBaseConnectivity db;

	public BinListAdapter(Context context, int resource,ArrayList<EventsListItem> eventListItems) {
		super(context, resource, eventListItems);
		
		this.item = eventListItems;		
		this.context = context;
		db = new DataBaseConnectivity(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
						
		if(convertView == null){
			
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.events_list_items,null);
		}		
		
		TextView eventTitle = (TextView)convertView.findViewById(R.id.eventTitle);
		TextView eventTimeToStart = (TextView)convertView.findViewById(R.id.eventTimeToStart);
		TextView eventTimeToFinish = (TextView)convertView.findViewById(R.id.eventTimeToStop);
		
		eventTitle.setText(item.get(position).getEvent());	
		
		
		int startTime = Integer.parseInt(item.get(position).getStartTime());
		int stopTime = Integer.parseInt(item.get(position).getFinishTime());
		
		int sStartHours = startTime/3600000;
		int sStartMins = (startTime % 3600000)/60000;
		
		int sStopHours = stopTime/3600000;
		int sStopMins = (stopTime % 3600000)/60000;
							
		eventTimeToStart.setText(formatStrings(sStartHours,sStartMins)+"  - ");
		eventTimeToFinish.setText(formatStrings(sStopHours,sStopMins));
		
		//	applying custom fonts
		MyFont.applyFonts(convertView, Typeface.createFromAsset(context.getAssets(),"font/Purisa.ttf"));
		
		return convertView;
	}
	
private String formatStrings(int hours, int mins) {
		
		String hour = "",min = "",mer = "";
				
		if (hours > 12) {
			
			int temp = hours -12;			
			
			if(temp < 10)					
				hour = "0"+temp+"";
			
			else
				hour = temp+"";
			
            mer = "PM";            
            
        } else if (hours == 0) {
            
        	int temp = hours +12;
        	hour = temp+"";
            mer = "AM";              
        }
        
        else if(hours < 12){
        	
        	int temp = hours;
        	
        	if(temp < 10)					
				hour = "0"+temp+"";
			
			else
				hour = temp+"";
        	
            mer = "AM";    
        	
        }else if (hours == 12){
        	       	
        	int temp = hours;			
        	hour = temp+"";
        	mer = "PM";
        }
        else
            mer = "AM";
		        
        if (mins < 10)
            min = "0" + mins;
        else
            min = String.valueOf(mins);
		
		return hour+":"+min+" "+mer;
	}
}