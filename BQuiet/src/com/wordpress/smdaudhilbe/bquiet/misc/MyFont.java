package com.wordpress.smdaudhilbe.bquiet.misc;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyFont {

	public static void applyFonts(View v,Typeface fontToSet) {
		
		try {
			
			if(v instanceof ViewGroup){
				
				ViewGroup vG = (ViewGroup)v;
				
				for(int i = 0; i < vG.getChildCount();i++){
					
					View child = vG.getChildAt(i);					
					applyFonts(child, fontToSet);
					
				}
				
			}else if (v instanceof TextView)
				((TextView)v).setTypeface(fontToSet);			
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}