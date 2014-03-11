package com.wordpress.smdaudhilbe.bquiet.misc;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.widget.TimePicker;

public class MyTimePickerDialog extends DialogFragment implements OnTimeSetListener{

	TimePickerInterface tInterface;
	private String whichTimePicker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		

		//	getting arguments
		whichTimePicker = getArguments().getString("whichTimePicker");

		try {			
			tInterface = (TimePickerInterface)getTargetFragment();			
		} catch (Exception e) {			
			throw new ClassCastException("Calling fragment must implement MyTimePickerDialog.TimePickerInterface");
		}		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	
		
		// Use the current time as the default values for the picker
        Calendar c = Calendar.getInstance();
        
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
		
		return new TimePickerDialog(getActivity(),this,hour,minute,false);
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		tInterface.timePickerDoneClicked(hourOfDay,minute,whichTimePicker);
	}
	
	//	to prevent dialog from disappearing from screen during configuration changes
	@Override
	public void onDestroyView() {
		
		if(getDialog() != null && getRetainInstance())
			getDialog().setOnDismissListener(null);
		
		super.onDestroyView();
	}
	
	public interface TimePickerInterface{
		public void timePickerDoneClicked(int hourOfDay, int minute,String whichTimePicker);
	}
}