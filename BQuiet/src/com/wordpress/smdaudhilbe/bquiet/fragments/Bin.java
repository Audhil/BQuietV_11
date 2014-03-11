package com.wordpress.smdaudhilbe.bquiet.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.adapter.BinListAdapter;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.EventRestoreDeleteDialog;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.model.EventsListItem;
import com.wordpress.smdaudhilbe.bquiet.silent.SchedulingReceiver;

public class Bin extends Fragment implements OnItemLongClickListener,EventRestoreDeleteDialog.eventRestoreDeleteInterface {

	MyFont myFont = new MyFont();	

	private ArrayList<EventsListItem> binListItems;

	private DataBaseConnectivity db;

	private List<EventsListItem> dataFromDatabase;

	private BinListAdapter eAdapter;

	private ListView lV;

	private int itemPositon;
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.bin_fragment, container, false);
		
		RelativeLayout rLayout = (RelativeLayout)rootView.findViewById(R.id.binLayout);
		
		MyFont.applyFonts(rLayout, Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		initViewsAndClickListener(rootView);		
		
		//	here I initialise binListItems
		binListItems = new ArrayList<EventsListItem>();
		
		//	database object
		db = new DataBaseConnectivity(getActivity());
		
		dataFromDatabase = db.getDataFromDatabaseBinTable();
		
		//	adding event list items
		for(EventsListItem eListItem : dataFromDatabase)
			binListItems.add(eListItem);
		
		eAdapter = new BinListAdapter(getActivity(),0,binListItems);
		
		lV.setAdapter(eAdapter);

		lV.setOnItemLongClickListener(this);
		
		//		if no items inside listview
		if(eAdapter.isEmpty()){
			View emptyView = rootView.findViewById(R.id.binEmptyTextView);
			lV.setEmptyView(emptyView);
		}
		
		return rootView;
	}

	//	init views
	private void initViewsAndClickListener(View rootView) {
		lV = (ListView)rootView.findViewById(R.id.binListView);		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,long id) {
		
		itemPositon = position;
		
		showAlertDialog("Event Restore / Delete ?","Choose any of your choice");		
		
		return true;
	}

	//	to show alert Dialog
	private void showAlertDialog(String Title, String Message) {
		
		Bundle bdl = new Bundle();
		bdl.putString("Title", Title);
		bdl.putString("Message", Message);
		
		DialogFragment dialogFragment = new EventRestoreDeleteDialog();		

		//	saying that this is the target destination for dialog fragment
		dialogFragment.setTargetFragment(this,0);
		
		//	passing data to be displayed at dialog fragment
		dialogFragment.setArguments(bdl);
				
		//	displaying dialog
		dialogFragment.show(getFragmentManager(), "dummyTag");
	}

	//	response to alertDialog
	@Override
	public void negativeClicked() {	
		
		//	item to be removed from listview and also from database		
		db.removeEventCompletely(binListItems.get(itemPositon).getEvent());
		binListItems.remove(binListItems.get(itemPositon));				
		eAdapter.notifyDataSetChanged();
	}	

	@Override
	public void positiveClicked() {
		
		//	remove item if and only if restoration done
		if(db.tryRestoringItemsTocreateEventTable(binListItems.get(itemPositon).getEvent())){
			binListItems.remove(binListItems.get(itemPositon));				
			eAdapter.notifyDataSetChanged();
			
			//	re scheduling events appropriately
			scheduleEvents();
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