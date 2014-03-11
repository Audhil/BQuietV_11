package com.wordpress.smdaudhilbe.bquiet;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.util.LruCache;
import android.support.v4.widget.DrawerLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.style.MetricAffectingSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.wordpress.smdaudhilbe.bquiet.adapter.NavDrawerListAdapter;
import com.wordpress.smdaudhilbe.bquiet.fragments.Bin;
import com.wordpress.smdaudhilbe.bquiet.fragments.Create;
import com.wordpress.smdaudhilbe.bquiet.fragments.Events;
import com.wordpress.smdaudhilbe.bquiet.fragments.Help;
import com.wordpress.smdaudhilbe.bquiet.fragments.MapFragDummy;
import com.wordpress.smdaudhilbe.bquiet.fragments.Profile;
import com.wordpress.smdaudhilbe.bquiet.fragments.Settings;
import com.wordpress.smdaudhilbe.bquiet.map.MapActivity;
import com.wordpress.smdaudhilbe.bquiet.misc.AppRater;
import com.wordpress.smdaudhilbe.bquiet.misc.KeyBoardHide;
import com.wordpress.smdaudhilbe.bquiet.misc.MyFont;
import com.wordpress.smdaudhilbe.bquiet.misc.MySharedPreferences;
import com.wordpress.smdaudhilbe.bquiet.model.NavDrawerItem;

public class MainActivity extends Activity implements OnCheckedChangeListener,OnClickListener{

	private MySharedPreferences mPreference;
	private Vibrator vB;
	
	private RelativeLayout rLayout;
	private DrawerLayout drawerLayout;
	private ScrollView scrollPassCodeLayout,scrollFirstLaunch;
	
	//	Views for first launch page / passcode needed page
	private CheckBox passCode,sms,location;
	private Button doneBtn,tB1,tB2,tB3,tB4,tB5,tB6,tB7,tB8,tB9,tB10,tBDel;
	private EditText eText1,eText2,eText3,eText4;
	
	private HashMap<String, Boolean> appPermission;
	String newPassCodeContent;
	
	//	for passcode needed page
	static int edExecuted = 0;
	
	//	for navigation drawer	
	//	used to store app title
    private CharSequence mTitle;    
    //	nav drawer title    
	private CharSequence mDrawerTitle;
	
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private ListView mDrawerList;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	private ActionBarDrawerToggle mDrawerToggle;
	private SpannableStringBuilder sBuilder;
	private Fragment fragment;
	private Runnable myPendingRunnable;
	private KeyBoardHide keyBoardHide;
	private boolean askPasscode = true;	
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initViewsApplyFonts();
		
		//	for first time app launch
		if(!mPreference.isUserDefinedPermissions()){			
			getActionBar().hide();			
			scrollFirstLaunch.setVisibility(View.VISIBLE);
		}
		
		//	for navigation drawer
        //	setting title
        mTitle = mDrawerTitle = getTitle();        
        
        //	load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        
        //	load slide menu icons too
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        
		navDrawerItems = new ArrayList<NavDrawerItem>();
        
        //	Events
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        //	Create
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        //	Map
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        //	Profile
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        //	Bin
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));        
        //	Settings
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        //	Help
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        
        //	recycling typed array
        navMenuIcons.recycle();
        
        //	setting adaper
        adapter = new NavDrawerListAdapter(getApplicationContext(),navDrawerItems);
        mDrawerList.setAdapter(adapter);
        
        //	and click listener for listview
        mDrawerList.setOnItemClickListener(new MyListener());
        
        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);	//	to enable icon at home button
        getActionBar().setHomeButtonEnabled(true);	//	to enable clicking effect at home button
        
        mDrawerToggle = new ActionBarDrawerToggle(
        						this, 
        						drawerLayout,
        						R.drawable.ic_drawer, //nav menu toggle icon
        						R.string.app_name, // nav drawer open - description for accessibility
        						R.string.app_name // nav drawer close - description for accessibility
        						){
        	
        	View myView = new View(MainActivity.this);

			public void onDrawerClosed(View view) {
            
            	//	applying purisa font to action bar
            	sBuilder = new SpannableStringBuilder(mTitle);
            	sBuilder.setSpan(new TypefaceSpan(this, "Purisa.ttf",getApplicationContext()), 0, sBuilder.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            	getActionBar().setTitle(sBuilder);
            
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
                
                //	to avoid strucking
                if(myPendingRunnable != null){
                	myView.post(myPendingRunnable);
                	myPendingRunnable = null;
                }
            }
 
            public void onDrawerOpened(View drawerView) {
            	
            	//	applying purisa font to action bar            	
            	sBuilder = new SpannableStringBuilder(mDrawerTitle);
                sBuilder.setSpan(new TypefaceSpan(this, "Purisa.ttf",getApplicationContext()), 0, sBuilder.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                getActionBar().setTitle(sBuilder);
                
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
                
                //	hiding keypad
                keyBoardHide.hideTheKeyBoard();
            }
        };        
        drawerLayout.setDrawerListener(mDrawerToggle);
        
        // on first time display view for first nav item	         
        displayView(0);
	}
	
	/*
	 * Item click listener
	 * */
	private class MyListener implements OnItemClickListener{

		//	display fragments
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {			
			displayView(position);
		}		
	}
	
	//	displaying respective fragments
	private void displayView(int position) {
		
		// update the main content by replacing fragments
        switch (position) {
        
        	case 0:
        		fragment = new Events();
        		break;
            
        	case 1:
        		fragment = new Create();
        		break;
            
        	case 2:
        		fragment = new MapFragDummy();
        		Intent intent = new Intent(this,MapActivity.class);
        		startActivityForResult(intent.putExtra("askPasscode",false), 22);
        		break;
            
        	case 3:
        		fragment = new Profile();
        		break;
            
        	case 4:
        		fragment = new Bin();
        		break;
        
        	case 5:
        		fragment = new Settings();
        		break;
           
        	case 6:
        		fragment = new Help();
        		break;
        }
        
        //	replacing fragments
        if(fragment != null){
        	
        	myPendingRunnable = new Runnable() {
				
				@Override
				public void run() {
					FragmentManager fManager = getFragmentManager();
					fManager.beginTransaction().replace(R.id.frame_container,fragment).commit();
				}
			};
        }
		
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position,true);
        mDrawerList.setSelection(position);
        setTitle(navMenuTitles[position]);
        drawerLayout.closeDrawer(mDrawerList);
	}

	//	in order to get passcode after background navigation
	@Override
	protected void onStart() {	
		super.onStart();
		
		//	passcode verification if it is checked
		if(mPreference.isUserDefinedPasscodePermissions() && askPasscode){
			getActionBar().hide();
			scrollPassCodeLayout.setVisibility(View.VISIBLE);
		}
		else{
			drawerLayout.setVisibility(View.VISIBLE);
			drawerLayout.openDrawer(Gravity.LEFT);
		}
	}

	private void initViewsApplyFonts() {
		
		rLayout = (RelativeLayout)findViewById(R.id.base_Of_All);
		
		MyFont.applyFonts(rLayout,Typeface.createFromAsset(getAssets(),"font/Purisa.ttf"));
		
		scrollFirstLaunch = (ScrollView)findViewById(R.id.app_first_launch);
		scrollPassCodeLayout = (ScrollView)findViewById(R.id.passCodeMatter);
		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		
		/*
		 * first launch page
		 */
		passCode = (CheckBox)findViewById(R.id.app_passcode);
		sms = (CheckBox)findViewById(R.id.app_sms);
		location = (CheckBox)findViewById(R.id.app_location);
		
		doneBtn = (Button)findViewById(R.id.app_submit);
		
		appPermission = new HashMap<String, Boolean>();
		
		passCode.setOnCheckedChangeListener(this);
		doneBtn.setOnClickListener(this);
		
		/*
		 * Passcode page
		 */
		tB1 = (Button)findViewById(R.id.TabBtn1);tB1.setOnClickListener(this);		
		tB2 = (Button)findViewById(R.id.TabBtn2);tB2.setOnClickListener(this);
		tB3 = (Button)findViewById(R.id.TabBtn3);tB3.setOnClickListener(this);		
		tB4 = (Button)findViewById(R.id.TabBtn4);tB4.setOnClickListener(this);
		tB5 = (Button)findViewById(R.id.TabBtn5);tB5.setOnClickListener(this);
		tB6 = (Button)findViewById(R.id.TabBtn6);tB6.setOnClickListener(this);
		tB7 = (Button)findViewById(R.id.TabBtn7);tB7.setOnClickListener(this);
		tB8 = (Button)findViewById(R.id.TabBtn8);tB8.setOnClickListener(this);
		tB9 = (Button)findViewById(R.id.TabBtn9);tB9.setOnClickListener(this);
		tB10 = (Button)findViewById(R.id.TabBtn10);tB10.setOnClickListener(this);
		tBDel = (Button)findViewById(R.id.TabBtnDel);tBDel.setOnClickListener(this);		
		
		eText1 = (EditText)findViewById(R.id.firstPassCodeNum);		
		eText2 = (EditText)findViewById(R.id.secondPassCodeNum);
		eText3 = (EditText)findViewById(R.id.thirdPassCodeNum);
		eText4 = (EditText)findViewById(R.id.fourthPassCodeNum);
		
		/*
		 * Navigation drawer's listview
		 */
		mDrawerList = (ListView)findViewById(R.id.list_slidermenu);		
		
		mPreference = new MySharedPreferences(getApplicationContext());
		
		vB = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
		keyBoardHide = new KeyBoardHide(getApplicationContext());
		
		AppRater.app_launched(MainActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.main, menu);
		
		//	menu item
		MenuItem shareItem = (MenuItem) menu.findItem(R.id.action_share);
			
		ShareActionProvider mShare = (ShareActionProvider)shareItem.getActionProvider();
			
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
			
		shareIntent.setAction(Intent.ACTION_SEND);			
		shareIntent.setType("text/plain");		
		shareIntent.putExtra(Intent.EXTRA_TEXT,"I started using BQuiet App! Get it for you too! at\n\nhttps://play.google.com/store/apps/details?id=com.wordpress.smdaudhilbe.bquiet");
			
		mShare.setShareIntent(shareIntent);
		
		return true;
	}
	
    @Override
    public void setTitle(CharSequence title) {
    	
    	mTitle = title;
    
    	//	applying purisa font to action bar
    	sBuilder = new SpannableStringBuilder(mTitle);
    	sBuilder.setSpan(new TypefaceSpan(this, "Purisa.ttf"), 0, sBuilder.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);    	
    	getActionBar().setTitle(sBuilder);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item))        	
            return true;
        
    	return super.onOptionsItemSelected(item);
    }
    
    //	to replace back or up icon in actionbar to drawer icon
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {    
    	super.onPostCreate(savedInstanceState);
    	
    	//	Sync the toggle state after onRestoreInstanceState has occured
 		mDrawerToggle.syncState();
    }
	
	@Override
    public void onBackPressed() {   	
    	
    	vB.vibrate(100);
    	
    	new AlertDialog.Builder(this)
        	 .setIcon(android.R.drawable.ic_dialog_alert)
        	 .setIcon(R.drawable.ic_launcher_final)
        	 .setTitle("BQuiet")
        	 .setMessage("Are you sure you want to exit?")
        	 .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
        		 
        		 @Override
        		 public void onClick(DialogInterface dialog, int which) {        					
        			 finish();    
        		 }
        	 })
        	 .setNegativeButton("No", null)
        	 .show();    	
    }

	/*
	 * First Launch Page
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//	show passcode needed dialog
		if(isChecked)
			gettingNewPasscodeDialog("BQuiet", "Enter passcode");						
	}
	
	private void gettingNewPasscodeDialog(String Title,String Message) {
    	
    	vB.vibrate(100);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		
		builder.setTitle(Title);
		builder.setMessage(Message);
		builder.setIcon(R.drawable.ic_launcher_final);
		builder.setCancelable(false);
		
		final EditText passCodee = new EditText(getApplicationContext());		
		passCodee.setInputType(InputType.TYPE_CLASS_NUMBER);
		passCodee.setTransformationMethod(PasswordTransformationMethod.getInstance());
		passCodee.setHint("....");		
		passCodee.setTextColor(Color.BLACK);
		passCodee.setBackgroundColor(Color.WHITE);
		passCodee.requestFocus();
		
		
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(4);
		passCodee.setFilters(filterArray);
		
		builder.setView(passCodee);
		
		builder.setPositiveButton("OK",null);
		
		builder.setNegativeButton("Cancel",null);
		
		//	preventing dialog from closing if user has entered any invalid passcode such as characters below length 4
		final AlertDialog alert = builder.create();
		
		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				
				Button okButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				
				okButton.setOnClickListener(new OnClickListener() {					

					@Override
					public void onClick(View v) {
						
						if(passCodee.getText().toString().trim().length() < 4)
							Toast.makeText(getApplicationContext(), "Insufficient passcode",Toast.LENGTH_LONG).show();						

						else{
							newPassCodeContent = passCodee.getText().toString();														
							alert.cancel();							
						}
					}
				});
				
				Button cancelClicked = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
				
				cancelClicked.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						
						passCode.setChecked(false);						
						alert.cancel();
					}
				});
			}			
		});		
		//	to show keypad
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);				
		alert.show();
	}

	@Override
	public void onClick(View view) {
		
		/*
		 * first launch Activity / Passcode needed page view click events
		 */
		switch (view.getId()) {
		
			//	for first launch activity
			case R.id.app_submit:
				
				if(passCode.isChecked()){						
					appPermission.put("PassCode",true);
					mPreference.storePassCodeContent(newPassCodeContent);
				}
				else{
					appPermission.put("PassCode",false);
					mPreference.deletePassCodeContent();
				}

				if(sms.isChecked())
					appPermission.put("SmS",true);		
				else
					appPermission.put("SmS",false);
				
				if(location.isChecked())
					appPermission.put("GeoFencing",true);		
				else
					appPermission.put("GeoFencing",false);
				
				mPreference.storeAppPermissions(appPermission);
				
				//	changing visibility
				scrollFirstLaunch.setVisibility(View.INVISIBLE);
				
				if(mPreference.isUserDefinedPasscodePermissions())	
					scrollPassCodeLayout.setVisibility(View.VISIBLE);
				
				else{
					getActionBar().show();
					drawerLayout.setVisibility(View.VISIBLE);
				}
							
				break;
			
			//	passcode needed page	
			case R.id.TabBtn1:			
				writeAndExecute("1");			
				break;
		
			case R.id.TabBtn2:			
				writeAndExecute("2");			
				break;
		
			case R.id.TabBtn3:			
				writeAndExecute("3");			
				break;
		
			case R.id.TabBtn4:			
				writeAndExecute("4");			
				break;
		
			case R.id.TabBtn5:			
				writeAndExecute("5");			
				break;
		
			case R.id.TabBtn6:			
				writeAndExecute("6");			
				break;
		
			case R.id.TabBtn7:			
				writeAndExecute("7");			
				break;
		
			case R.id.TabBtn8:			
				writeAndExecute("8");			
				break;
		
			case R.id.TabBtn9:			
				writeAndExecute("9");			
				break;
		
			case R.id.TabBtn10:			
				writeAndExecute("0");				
				break;
		
			case R.id.TabBtnDel:			
				writeAndExecute("Del");			
				break;
		}
	}

	//	method to check and execute
	private void writeAndExecute(String value) {
		
		//	if delete pressed
		if(value.equals("Del")){
			
			switch (edExecuted) {
			
			case 1:
				eText1.setText("");				
				edExecuted--;				
				break;
				
			case 2:
				eText2.setText("");				
				edExecuted--;				
				break;
				
			case 3:
				eText3.setText("");				
				edExecuted--;				
				break;
			}
		}
		
		else{
		
			if(TextUtils.isEmpty(eText1.getText())){			
				eText1.setText(value);
				edExecuted = 1;
			}
		
			else if(TextUtils.isEmpty(eText2.getText())){				
				eText2.setText(value);
				edExecuted = 2;
			}
		
			else if(TextUtils.isEmpty(eText3.getText())){		
				eText3.setText(value);
				edExecuted = 3;
			}
		
			else if(TextUtils.isEmpty(eText4.getText())){		
				eText4.setText(value);				
				checkPassCode(eText1.getText().toString()+eText2.getText().toString()+eText3.getText().toString()+eText4.getText().toString());
			}
		}
	}
	
	//	checking done here
	private void checkPassCode(String userEnteredPassCode) {
		
		//	for correct passcode
		if(mPreference.getPassCodeContent().equals(userEnteredPassCode)){
			
			//	clearing editText views
			eText1.setText("");eText2.setText("");eText3.setText("");eText4.setText("");
			
			//	changing visibility			
			//	showing actionbar for navigation drawer
			getActionBar().show();
			
			scrollPassCodeLayout.setVisibility(View.INVISIBLE);			
			drawerLayout.setVisibility(View.VISIBLE);
			
			drawerLayout.openDrawer(Gravity.LEFT);
		}
		
		//	for wrong passcode
		else{
			vB.vibrate(100);			
			Toast.makeText(getApplicationContext(),"Wrong passcode!",Toast.LENGTH_LONG).show();			
			eText1.setText("");eText2.setText("");eText3.setText("");eText4.setText("");
			edExecuted = 0;
		}			
	}
	
	@Override
	protected void onPause() {	
		super.onPause();
		
		//	hiding keypad
		keyBoardHide.hideTheKeyBoard();
		
		//	to ask passcode after returning from background
		askPasscode = true;
		
		//	to avoid colliding with passcode page
		drawerLayout.setVisibility(View.INVISIBLE);
	}
	
    //	custom font to action bar title
    public static class TypefaceSpan extends MetricAffectingSpan {
        /** An <code>LruCache</code> for previously loaded typefaces. */
      private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);
   
      private Typeface mTypeface;
   
      /**
       * Load the {@link Typeface} and apply to a {@link Spannable}.
       */
      public TypefaceSpan(Context context, String typefaceName) {
          mTypeface = sTypefaceCache.get(typefaceName);
   
          if (mTypeface == null) {
              mTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(),"font/Purisa.ttf");
   
              // Cache the loaded Typeface
              sTypefaceCache.put(typefaceName, mTypeface);
          }
      }
   
      public TypefaceSpan(ActionBarDrawerToggle actionBarDrawerToggle,String typefaceName, Context context) {
		
          mTypeface = sTypefaceCache.get(typefaceName);
          
          if (mTypeface == null) {
              mTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(),"font/Purisa.ttf");
   
              // Cache the loaded Typeface
              sTypefaceCache.put(typefaceName, mTypeface);
          }
	  }

	@Override
      public void updateMeasureState(TextPaint p) {
          p.setTypeface(mTypeface);
          
          // Note: This flag is required for proper typeface rendering
          p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
      }
   
      @Override
      public void updateDrawState(TextPaint tp) {
          tp.setTypeface(mTypeface);
          
          // Note: This flag is required for proper typeface rendering
          tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
      }
   }
    
    //	Not to ask passcode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {    
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode == 22){
    		askPasscode = data.getExtras().getBoolean("askPasscode");
    		
    		//	to pull navigation drawer
    		drawerLayout.openDrawer(Gravity.LEFT);
    	}
    }
}