package com.wordpress.smdaudhilbe.bquiet.model;

public class EventsListItem {

	private String event,startTime,finishTime,sms,repeat;	
	private boolean isSmsVisible,isRepeatVisible;

	public EventsListItem(String event,String startTime,String finishTime){
	
		this.event = event;
		this.startTime = startTime;
		this.finishTime = finishTime; 
	}

	public EventsListItem(String event,String startTime,String finishTime,boolean isSmsVisible,boolean isRepeatVisible,String sms,String repeat) {
		
		this.event = event;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.isSmsVisible = isSmsVisible;
		this.isRepeatVisible = isRepeatVisible;
		this.sms = sms;
		this.repeat = repeat; 
	}	
	
	public EventsListItem(String event,String startTime,String finishTime,boolean isRepeatVisible,String repeat) {
		this.event = event;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.isRepeatVisible = isRepeatVisible;
		this.repeat = repeat; 
	}
	
	public EventsListItem() {
		
	}

	public void setEvent(String event) {
		this.event = event;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
	
	public void setSmsVisible(boolean isSmsVisible) {
		this.isSmsVisible = isSmsVisible;
	}
	
	public void setRepeatVisible(boolean isRepeatVisible) {
		this.isRepeatVisible = isRepeatVisible;
	}
	
	public void setSms(String sms) {
		this.sms = sms;
	}
	
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}
	
	public String getEvent() {
		return this.event;
	}
	
	public String getStartTime() {
		return this.startTime;
	}
	
	public String getFinishTime() {
		return this.finishTime;
	}
	
	public boolean getSmsVisible() {
		return this.isSmsVisible;
	}
	
	public boolean getRepeatVisible() {
		return this.isRepeatVisible;
	}
	
	public String getSms() {
		return this.sms;
	}
	
	public String getRepeat() {
		return this.repeat;
	}
}