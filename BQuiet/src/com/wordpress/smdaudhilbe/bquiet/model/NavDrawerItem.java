package com.wordpress.smdaudhilbe.bquiet.model;

public class NavDrawerItem {
	
	private String title;
	private int icon;
	
	public NavDrawerItem(String title,int icon) {		
		this.title = title;
		this.icon = icon;
	}
	
	//	title	
	public String getTitle() {		
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	//	icon
	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}
}