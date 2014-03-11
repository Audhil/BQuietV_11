package com.wordpress.smdaudhilbe.bquiet.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;

public class Help extends Fragment {
	
	ScrollView sView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.info, container, false);
		
		sView = (ScrollView)rootView.findViewById(R.id.INFO);
		
		MyFont.applyFonts(sView, Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		return rootView;
	}
}