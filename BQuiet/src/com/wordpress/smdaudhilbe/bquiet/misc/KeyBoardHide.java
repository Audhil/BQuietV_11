package com.wordpress.smdaudhilbe.bquiet.misc;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyBoardHide {

	private Context context;	
	InputMethodManager imgr;
	
	public static EditText editText;
	public static boolean editTextAvailable;

	public KeyBoardHide(Context context) {
		this.context = context;
	}	
	
	public void hideTheKeyBoard() {		
		imgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		if(editTextAvailable)
			imgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);		
	}
}