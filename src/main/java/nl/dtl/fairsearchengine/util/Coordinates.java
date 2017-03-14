package nl.dtl.fairsearchengine.util;

public class Coordinates {
	double latitude = 45;
	double longitude = 45;
	
	
	public Coordinates(double latitude,double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
}
