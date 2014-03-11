package com.wordpress.smdaudhilbe.bquiet.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.adapter.EventListAdapter;
import com.wordpress.smdaudhilbe.bquiet.database.DataBaseConnectivity;
import com.wordpress.smdaudhilbe.bquiet.misc.EventRestoreDeleteDialog;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.model.EventsListItem;

/*
 * This is the fragment which is selected default by system after launching
 * 
 */
public class Events extends Fragment implements EventRestoreDeleteDialog.eventRestoreDeleteInterface,OnItemClickListener{

	MyFont myFont = new MyFont();
	
	MySharedPreferences sPreferences;
	
	ListView lV;
	
	ArrayList<EventsListItem> eventListItems;
	
	List<EventsListItem> dataFromDatabase;
	
	EventListAdapter eAdapter;
	
	DataBaseConnectivity db;
	
	private int itemPositon;
	
	Vibrator vB;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.events_fragment, container, false);
		
		RelativeLayout rLayout = (RelativeLayout)rootView.findViewById(R.id.eventsLayout);
		
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		//	making shared preference reference
		sPreferences = new MySharedPreferences(getActivity());
		
		initViews(rootView);
		
		vB = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		
		//	here I initialise eventListItems
		eventListItems = new ArrayList<EventsListItem>();
		
		//	database object
		db = new DataBaseConnectivity(getActivity());
		
		dataFromDatabase = db.getDataFromDatabase();
		
		//	adding event list items
		for(EventsListItem eListItem : dataFromDatabase)
			eventListItems.add(eListItem);
		
		eAdapter = new EventListAdapter(getActivity(),0,eventListItems);
		
		lV.setAdapter(eAdapter);
		lV.setOnItemLongClickListener(new myLongClick());		
		
		lV.setOnItemClickListener(this);
		
		//	if no items inside listview
		if(eAdapter.isEmpty()){
			View emptyView = rootView.findViewById(R.id.eventsEmptyTextView);
			lV.setEmptyView(emptyView);
		}
		
		return rootView;
	}
	
	//	long click event of list view
	private class myLongClick implements OnItemLongClickListener{		

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
			
			itemPositon = position;
			
			if(eventListItems.get(position).getEvent().equals(sPreferences.getEventAlarmInProgress())){
				Toast.makeText(getActivity(), "Event is in progress.", Toast.LENGTH_LONG).show();
			}
			
			else{
				vB.vibrate(50);
				showAlertDialog("Delete Event ?","Choose any of your choice");
			}
			
			return true;
		}	
	}

	private void initViews(View rootView) {
		lV = (ListView)rootView.findViewById(R.id.eventsListView);		
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
	
	@Override
	public void positiveClicked() {
		
		//	putting a reference inside database
		db.storeDeletedAlarmNames(eventListItems.get(itemPositon).getEvent());
		
		db.removeFromCreateEventsMoveToBinEvents(eventListItems.get(itemPositon).getEvent());
		eventListItems.remove(eventListItems.get(itemPositon));
		eAdapter.notifyDataSetChanged();
	}

	@Override
	public void negativeClicked() {
		//	do nothing
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
		showDetailInAlertDialog(eventListItems.get(position).getEvent());
	}

	//	event Details
	private void showDetailInAlertDialog(String eventName) {

		String[] eventDetails = db.getDetailsOfEventToCancelAlarmOfThisEvent(eventName);

		String dataRepeat,dataSMS,dataSMSContent;

		if(Boolean.parseBoolean(eventDetails[3]))
			dataRepeat = "Yes";
		else
			dataRepeat = "No";		

		if(Boolean.parseBoolean(eventDetails[1])){			
			dataSMS = "Yes";
			dataSMSContent = eventDetails[2];
		}
		else{
			dataSMS = "No";
			dataSMSContent = "nil";
		}

		String eventDetail = "";

		if(dataSMSContent.equals("nil")){

			eventDetail = "Repeatable	: "+dataRepeat+"\n"
							+"SMS active	: "+dataSMS;
		}

		else{
			eventDetail = "Repeatable	: "+dataRepeat+"\n"
							+"SMS active	: "+dataSMS+"\n"
							+"SMS content	: "+dataSMSContent;
		}

		vB.vibrate(100);

		new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.ic_launcher_final)
			.setTitle("BQuiet")
			.setMessage(eventDetail)
			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//	do nothing
				}
			})
			.show();		
	}
}