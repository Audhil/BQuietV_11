package com.wordpress.smdaudhilbe.bquiet.map;

import java.util.ArrayList;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MapListAdapter extends ArrayAdapter<MapPojo> {

	private ArrayList<MapPojo> items;
	private Context context;

	public MapListAdapter(Context context, int resource, ArrayList<MapPojo> items) {
		
		super(context, resource, items);
		
		this.items = items;
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null){
			
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.map_list_items,null);
		}		
		
		TextView eventName = (TextView)convertView.findViewById(R.id.mapEventName);
		TextView geoFenceRadius = (TextView)convertView.findViewById(R.id.mapGeoFenceRadius);
		TextView geoFenceCity = (TextView)convertView.findViewById(R.id.mapGeoFenceCity);
		
		eventName.setText(items.get(position).getGeoFenceEventName());
		geoFenceRadius.setText(items.get(position).getGeoFenceRadius());
		geoFenceCity.setText(items.get(position).getGeoFenceCity());
		
		//	applying custom fonts
		MyFont.applyFonts(convertView, Typeface.createFromAsset(context.getAssets(),"font/Purisa.ttf"));
		
		return convertView;
	}
}