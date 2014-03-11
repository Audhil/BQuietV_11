package com.wordpress.smdaudhilbe.bquiet.misc;


import com.wordpress.smdaudhilbe.bquiet.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class EventRestoreDeleteDialog extends DialogFragment {

	String Title,Message;
	eventRestoreDeleteInterface callBack;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Title = getArguments().getString("Title");
		Message = getArguments().getString("Message");		

		try {
			
			callBack = (eventRestoreDeleteInterface)getTargetFragment();
			
		} catch (ClassCastException e) {
			
			throw new ClassCastException("Calling fragment must implement EventRestoreDeleteDialog.eventRestoreDeleteInterface");
		}		
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(Title);
//		builder.setMessage(Message);
		builder.setIcon(R.drawable.alert);
		
		String positiveText = "",negativeText = "";
		
		if(Title.equals("Delete Event ?")){
			positiveText = "Delete";
			negativeText = "Cancel";
		}
		else if(Title.equals("Event Restore / Delete ?")){
			positiveText = "Restore";
			negativeText = "Delete";
		}
		
		builder.setPositiveButton(positiveText,new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {				
				callBack.positiveClicked();
			}			
		});
		
		builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callBack.negativeClicked();
			}
		});
		
		return builder.create();
	}
	
	public interface eventRestoreDeleteInterface{
		public void negativeClicked();
		public void positiveClicked();
	}
	
	//	to prevent dialog from disappearing from screen during configuration changes
	@Override
	public void onDestroyView() {
	
		if(getDialog() != null && getRetainInstance())
			getDialog().setOnDismissListener(null);
		
		super.onDestroyView();
	}
}