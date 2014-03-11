package com.wordpress.smdaudhilbe.bquiet.fragments;

import com.wordpress.smdaudhilbe.bquiet.R;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MapFragDummy extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.map_frag_dummy, container, false);
		
		RelativeLayout rLayout = (RelativeLayout)rootView.findViewById(R.id.map_frag_dummy_layout);
		
		MyFont.applyFonts(rLayout, Typeface.createFromAsset(getActivity().getAssets(),"font/Purisa.ttf"));
		
		return rootView;
	}
}