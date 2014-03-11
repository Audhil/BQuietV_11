package com.wordpress.smdaudhilbe.bquiet.map;

public class MapPojo {
	
	private String GeoFenceId,EventName,EventSmSContent,EventSmSActive,EventRadius,EventCity,EventLatLng;
	
	public MapPojo() {
	
	}
	
	public void setGeoFenceId(String geoId) {
		this.GeoFenceId = geoId;
	}
	
	public void setGeoFenceEventName(String eventName) {
		this.EventName = eventName;	
	}
	
	public void setGeoFenceSmSContent(String smsContent) {
		this.EventSmSContent = smsContent;
	}
	
	public void setGeoFenceSmSActive(String smsActive) {
		this.EventSmSActive = smsActive;
	}
	
	public void setGeoFenceRadius(String geoFenceRadius) {
		this.EventRadius = geoFenceRadius;
	}
	
	public void setGeoFenceCity(String geoFenceCity) {
		this.EventCity = geoFenceCity;
	}	
	
	public void setGeoFenceLatLng(String LatLng) {
		this.EventLatLng = LatLng;
	}
	
	public String getGeoFenceLatLng() {
		return this.EventLatLng;
	}
	
	public String getGeoFenceCity() {
		return this.EventCity;
	}
	
	public String getGeoFenceRadius() {
		return this.EventRadius;
	}
	
	public String getGeoFenceId() {
		return this.GeoFenceId;
	}
	
	public String getGeoFenceEventName() {
		return this.EventName;
	}
	
	public String getGeoFenceSmSContent() {
		return this.EventSmSContent;
	}
	
	public String getGeoFenceSmSActive() {
		return this.EventSmSActive;
	}
}