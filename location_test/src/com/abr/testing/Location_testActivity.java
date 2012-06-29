package com.abr.testing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Location_testActivity extends Activity {
    /** Called when the activity is first created. */
	public final static String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";
    
    //Global variables
    public LocationManager locationManager;
    public LocationListener locationListener;
    public boolean startRace = true; //Shall be set to true by a button
    public boolean raceStarted = false; //Start race clock and set this to true
    public boolean debugOn = true; //Shall be set to true by a button
    public int totalLaps = 0;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        
    }
    
    @Override
    public void onStart(){
    	super.onStart();
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            	// Called when a new location is found by the network location provider.
            	if(startRace){
            		checkIfNewLap(location);
            	}
            	if(debugOn){
            		printDebug(location);
            	}
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };
       

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	// Remove the listener you previously added
    	locationManager.removeUpdates(locationListener);
    }
    
    public void checkIfNewLap(Location location){
    	
    	String lapString;
    	
    	TextView laps = (TextView) findViewById(R.id.laps);
    	lapString = "Varv: " + totalLaps;
    	laps.setText(lapString);
    	
    }
    
    public void printDebug(Location location){
    	
    	String debugString;

    	double longitude = location.getLongitude();
    	double latitude = location.getLatitude();
    	String longitude_deg = location.convert(longitude,Location.FORMAT_MINUTES);
    	String latitude_deg = location.convert(latitude,Location.FORMAT_MINUTES);
    	
    	debugString = "Longitude: " + longitude_deg + "\nLatitude: " + latitude_deg;
    	
    	if(location.hasAltitude()){
    		double altitude = location.getAltitude();
    		debugString = debugString + "\nAltitude: " + altitude;
    	}
    	if(location.hasAccuracy()){
    		float accuracy = location.getAccuracy();
    		debugString = debugString + "\nAccuracy (m): " + accuracy;
    	}
    	if(location.hasSpeed()){
    		float speed = location.getSpeed();
    		debugString = debugString + "\nSpeed: " + speed;
    	}
    	if(location.hasBearing()){
    		float bearing = location.getBearing();
    		debugString = debugString + "\nBearing: " + bearing;
    	} 	
    	
    	TextView debugInfo = (TextView) findViewById(R.id.debugInfo);
    	debugInfo.setText(debugString);
    	
    	
    }
    
    
}
