package com.wordpress.smdaudhilbe.bquiet;

import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class SplashScreen extends Activity {

	private RelativeLayout rLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		 //Remove title bar
	    requestWindowFeature(Window.FEATURE_NO_TITLE);

	    //Remove notification bar
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		rLayout = (RelativeLayout)findViewById(R.id.splashRoot);
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getAssets(),"font/Purisa.ttf"));	
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				// This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainActivity.class);                
                startActivity(i);
 
                // close this activity
                finish();
			}
		}, 3000);
	}
}