package com.abr.testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import java.util.Locale;


public class Location_testActivity extends Activity implements OnInitListener{
    /** Called when the activity is first created. */
	public final static String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";
	public static final String PREFS_NAME = "abrLapTimer";
    
    //Global variables
    public LocationManager locationManager;
    public LocationListener locationListener;
    public boolean startRace = false; //Shall be set to true by a button
    public boolean raceStarted = false; //Start race clock and set this to true
    public boolean debugOn; //Shall be set to true/false by a button and it's state is now saved
    public boolean usingBearing = true; //Counting laps using the bearing to start location
    public float bearingSetting = 45; // Difference between lastLocation bearing to and current bearing to must exceed
    public boolean usingDistance = false; //Counting laps using distance to start location
    public float distanceSetting = 20; // The radius in which a location must be before distance exceeds again
    public int totalLaps = 0; //Number of laps
    public long raceStartTime = 0; //The utc time when the race was started
    public long lapStartTime = 0; //The utc time when the lap started
    public long currentLapTime = 0; //The current running utc time minus lapStartTime
    public long latestLapTime = 0; // The time of the last completed lap
    public Location startLocation = null; //The start location for reference long and lat
    public Location lastLocation = null; //The last location visited to compare with current location (passed the starting line)
    public boolean leftStartLine = false; // Will be set to true when the car has left the start/finish line
    public float startLineCriteria = 40; // Meters from starting point to be possible to register new lap
    public boolean newLap = true; // Will be set to true when new lap is completed and set to false when leftStartLine is set to true
    public Driver currentDriver = new Driver(); // The driver that currently is driving (either 1, 2, 3 or 4)
    public Driver driver1 = new Driver(), driver2 = new Driver(), driver3 = new Driver(), driver4 = new Driver(); //Four drivers
    public float speed = 0; //Not all location updates hold speed information, save last know speed here
    private int MY_DATA_CHECK_CODE = 0; //For TTS
    private TextToSpeech myTTS;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        debugOn = settings.getBoolean("debugMode", false);
        //Toggle debug on/off
        ToggleButton debugButton = (ToggleButton) findViewById(R.id.debugOn);
        debugButton.setChecked(debugOn);
        
        //Set the name for the four drivers
        driver1.setName("Fredrik");
        driver2.setName("Magnus");
        driver3.setName("Emil");
        driver4.setName("Guest");
        TextView driver1Text = (TextView) findViewById(R.id.driver1);
        TextView driver2Text = (TextView) findViewById(R.id.driver2);
        TextView driver3Text = (TextView) findViewById(R.id.driver3);
        TextView driver4Text = (TextView) findViewById(R.id.driver4);
        driver1Text.setText(driver1.getName());
        driver2Text.setText(driver2.getName());
        driver3Text.setText(driver3.getName());
        driver4Text.setText(driver4.getName());
        
        //For the TTS functionality
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //Keep screen lit
        this.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStart(){
    	super.onStart();
    	
    	//Test to see if scrolling is implemented correctly.
    	//Remove until stop remove
    	//ScrollView scroll = (ScrollView) findViewById(R.id.scrollView1);
    	//scroll.fullScroll(View.FOCUS_DOWN);
    	/*TextView textViewallLaps = (TextView) findViewById(R.id.allLaps);
		String allLapsString = "\n apa\n bepa\n apa\n bepa\n apa\n bepa\n apa\n bepa\n apa\n bepa\n apa\n bepa" ;
    	textViewallLaps.append(allLapsString);
    	textViewallLaps.append(allLapsString);
    	textViewallLaps.append(allLapsString);
    	textViewallLaps.append(allLapsString);
    	textViewallLaps.append(allLapsString);
    	textViewallLaps.append(allLapsString);textViewallLaps.append(allLapsString);
    	textViewallLaps.append(allLapsString);textViewallLaps.append(allLapsString);*/
    	//Stop removing :)
    	
    	
    	
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
            	//Set the clock
            	TextView clock = (TextView) findViewById(R.id.raceClock);
    	    	clock.setText(getTime(location.getTime(),"HH:mm:ss"));
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
    	// We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("debugMode", debugOn);
        // Commit the edits!
        editor.commit();
        
        //Save all lap times to file
        //but only if lastLocation has been updated
        if(lastLocation != null){
        	String FILENAME = getTime(lastLocation.getTime(),"yyyy-MM-dd_HH_mm_ss_SS") + ".txt";
            TextView laps = (TextView) findViewById(R.id.allLaps);
            CharSequence chars = laps.getText();
            String string = chars.toString();
            saveToFile(string,FILENAME,true);

        }
    	
    	// Remove the listener you previously added
    	locationManager.removeUpdates(locationListener);
    }
    
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (myTTS != null) {
            myTTS.stop();
            myTTS.shutdown();
        }
        super.onDestroy();
    }
    
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
        	//Perhaps use  Locale.getDefault() later on
            myTTS.setLanguage(Locale.US);
        }
    }
    
    public void checkIfNewLap(Location location){
    	
    	if(!raceStarted){
    		raceStarted = true;
    		raceStartTime = location.getTime();
    		lapStartTime = raceStartTime;
    		startLocation = location;
    		lastLocation = startLocation;
    	}
    	else{
    		currentLapTime = location.getTime() - lapStartTime;
    		TextView textViewCurrentLapTime = (TextView) findViewById(R.id.currentLapTime);
	    	textViewCurrentLapTime.setText(getTime(currentLapTime,"mm:ss.SS"));
	    	
	    	//Save speed if available
	    	if(location.hasSpeed()){
	    		speed = location.getSpeed();
	    	}
	    	
	    	if(newLap){
	    		float distanceFromStart = location.distanceTo(startLocation);
	    		if(distanceFromStart > startLineCriteria && distanceFromStart > location.getAccuracy()){
	    			leftStartLine = true;
	    			newLap = false;
	    		}
	    	}
    		
    		if(usingBearing && leftStartLine){
    			float lastBearingTo = lastLocation.bearingTo(startLocation);
    			float currentBearingTo = location.bearingTo(startLocation);
    			float bearingDiff = Math.abs(lastBearingTo-currentBearingTo);
    			if(bearingDiff > bearingSetting && location.distanceTo(startLocation) < startLineCriteria){
    				totalLaps = totalLaps + 1;
    				//Let's get of the grid :)
    				newLap = true;
    				leftStartLine = false;
    				TextView laps = (TextView) findViewById(R.id.laps);
        	    	String lapString = totalLaps + " laps";
        	    	laps.setText(lapString);
    				//Increase total laps and stint laps for current driver
        	    	currentDriver.increaseLaps();
        	    	currentDriver.increaseStintLaps();
        	    	//And update stint laps and total laps
        	        TextView lapsText = (TextView) findViewById(R.id.driverLaps);
        	    	lapsText.setText(currentDriver.getStintLaps() + " (" + currentDriver.getLaps() + ") laps");
    				//Use speed and distance to compensate for when passing the start/finish line
    				compensateLocation(location);
    			}    			
    		}
    		else if(usingDistance && leftStartLine){
    			
    		}
    		else{
    			//Just for testing the function
    	    	/*TextView laps = (TextView) findViewById(R.id.laps);
    	    	String lapString = totalLaps + " laps";
    	    	laps.setText(lapString);*/
    		}
    		lastLocation = location;
    	}
    }
    public void printDebug(Location location){
    	
    	String debugString;

    	double longitude = location.getLongitude();
    	double latitude = location.getLatitude();
    	String longitudeMin = Location.convert(longitude,Location.FORMAT_MINUTES);
    	String latitudeMin = Location.convert(latitude,Location.FORMAT_MINUTES);
    	long time = location.getTime();
    	String timeFormatted = getTime(time,"yyyy-MM-dd HH:mm:ss.SS");
    	
    	debugString = "Time: " + timeFormatted + "\nLongitude: " + longitudeMin + "\nLatitude: " + latitudeMin;
    	
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
    	
    	if(raceStarted){
    		double startLongitude = startLocation.getLongitude();
        	double startLatitude = startLocation.getLatitude();
        	String startLongitudeMin = Location.convert(startLongitude,Location.FORMAT_MINUTES);
        	String startLatitudeMin = Location.convert(startLatitude,Location.FORMAT_MINUTES);
        	float bearingToStart = location.bearingTo(startLocation);
        	String start = "Bearing to start: " + bearingToStart + "\nStart longitude: " + startLongitudeMin + "\nStart latitude: " + startLatitudeMin;
        	debugString = start + "\n" + debugString;
        	
        	//Save some debug info to file, what more should be saved?
        	//Since bearingToStart is key for new lap I'll save that as well as currentLapTime
        	String saveDebug = bearingToStart + " - " + getTime(currentLapTime,"mm:ss") + " - " + totalLaps + "\n";
        	saveToFile(saveDebug, "debug_abr_laptimer.txt", true);
    	}
    	//Check the newLap and leftStartLine booleans
    	debugString = debugString + "\nNew lap: " + newLap + "\nLeft start/finish: " + leftStartLine;
    	
    	TextView debugInfo = (TextView) findViewById(R.id.debugInfo);
    	debugInfo.setText(debugString);
    }
    public static String getTime(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date. 
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(milliSeconds);
         return formatter.format(calendar.getTime());
    }
    public void compensateLocation(Location location){
    	//Recalculate when passing finish line
		if(speed > 0){
			float lastToStart = lastLocation.distanceTo(startLocation);
			float currentToStart = location.distanceTo(startLocation);
			float travelledDistance = location.distanceTo(lastLocation);
			float weightedDistance = ( currentToStart / (currentToStart + lastToStart) ) * travelledDistance;
			long timeCompensate = (long) ( (weightedDistance / location.getSpeed()) * 1000 ); //get it in milliseconds
			// Reset location to finish line
			location.setTime(location.getTime()-timeCompensate);
			location.setLatitude(startLocation.getLatitude());
			location.setLongitude(startLocation.getLongitude());
		}
		latestLapTime = location.getTime() - lapStartTime;
		lapStartTime = location.getTime();
		//Add lap to textview all Laps.
		TextView textViewallLaps = (TextView) findViewById(R.id.allLaps);
		String allLapsString = "\n" + totalLaps + " " + getTime(latestLapTime,"mm:ss.SS") + " " + currentDriver;
    	textViewallLaps.append(allLapsString);
		
		//Add check if laptime is the driver's best one.
		checkCurrentDriverBestTime(latestLapTime);
		
		//Add latest laptime to textfield
		TextView textViewlastLapTime = (TextView) findViewById(R.id.latestLaptime);
    	textViewlastLapTime.setText(getTime(latestLapTime,"mm:ss.SS"));
    }
    public void toggleStartRace(View view) {
        // Is the start race toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
        	startRace = true;
        } else {
        	startRace = false;
        	//Also set race as not started
        	raceStarted=false;
        }
    }
    public void toggleDebug(View view) {
        // Is the start race toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
        	debugOn = true;
        } else {
        	debugOn = false;
        	// Clear the debugInfo textview
        	TextView debugInfo = (TextView) findViewById(R.id.debugInfo);
        	debugInfo.setText("");
        }
    }
    public void changeDriver(View view) {
        // Which driver button was pushed
        CharSequence driverNameChar = ((TextView) view).getText();
        String driverName = driverNameChar.toString();

    	// Change Current driver     
        if(driverName == driver1.getName()){
    		currentDriver = driver1;
    	}
    	else if(driverName == driver2.getName()){
    		currentDriver = driver2;
    	}
    	else if(driverName == driver3.getName()){
    		currentDriver = driver3;
    	}
    	else{
    		currentDriver = driver4;
    	}
        //Reset drivers stint laps
        currentDriver.resetStintLaps();
        //And update stint laps and total laps
        TextView laps = (TextView) findViewById(R.id.driverLaps);
    	laps.setText(currentDriver.getStintLaps() + " (" + currentDriver.getLaps() + ") laps");
        //Change the currentDriver textview with the name of current driver
    	TextView current = (TextView) findViewById(R.id.currentDriver);
    	current.setText(currentDriver.getName());
    	
    	//Test the text-to-speech
    	speakWords("New driver is " + currentDriver.getName());
    }
    public void checkCurrentDriverBestTime(long lapTime){
    	//Get currentDrivers best lap time
    	long time = currentDriver.getTime();
    	//Check if new lap time is better??    	
    	if(currentDriver.getName() == driver1.getName()){
    		time = driver1.getTime();
    		if(time == 0 || lapTime < time ){
    			driver1.setTime(lapTime);
    			TextView bestTime = (TextView) findViewById(R.id.driver1BestTime);
    			bestTime.setText(getTime(lapTime,"mm:ss.SS"));
    		}
    		
    	}
    	else if(currentDriver.getName() == driver2.getName()){
    		time = driver2.getTime();
    		if(time == 0 || lapTime < time ){
    			driver2.setTime(lapTime);
    			TextView bestTime = (TextView) findViewById(R.id.driver2BestTime);
    			bestTime.setText(getTime(lapTime,"mm:ss.SS"));
    		}
    	}
    	else if(currentDriver.getName() == driver3.getName()){
    		time = driver3.getTime();
    		if(time == 0 || lapTime < time ){
    			driver3.setTime(lapTime);
    			TextView bestTime = (TextView) findViewById(R.id.driver3BestTime);
    			bestTime.setText(getTime(lapTime,"mm:ss.SS"));
    		}
    	}
    	else{
    		time = driver4.getTime();
    		if(time == 0 || lapTime < time ){
    			driver4.setTime(lapTime);
    			TextView bestTime = (TextView) findViewById(R.id.driver4BestTime);
    			bestTime.setText(getTime(lapTime,"mm:ss.SS"));
    		}
    	}

    }
    public void saveToFile(String text, String fileName, boolean append){
    	
    	String state = Environment.getExternalStorageState();
    	
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
        	File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);
            
            try {
                // Make sure the directory exists.
                path.mkdirs();

                // Copy the string to the file.
                OutputStream os = new FileOutputStream(file,append);
                os.write(text.getBytes());
                os.close();

            } catch (IOException e) {
            	//Hmm should have some exceptions handling here

            }
        }
    }
    private void speakWords(String speech) {
    	 
        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
        
        //Queue up
        //myTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {      
                myTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
            }
    }
    
}
