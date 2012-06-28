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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
     // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              // Called when a new location is found by the network location provider.
              //makeUseOfNewLocation(location);
              double plats_long = location.getLongitude();
              double plats_lat = location.getLatitude();
              String longitude_deg = location.convert(plats_long,Location.FORMAT_MINUTES);
              String latitude_deg = location.convert(plats_lat,Location.FORMAT_MINUTES);
              TextView debug_info = (TextView) findViewById(R.id.debug_info);
              debug_info.setText("Longitude: " + longitude_deg + "\nLatitude: " + latitude_deg);

              //setContentView(debug_info);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };
       

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
    }
    
    	
    
    /** Called when the user selects the Send button */
    /*public void sendMessage(View view) {
        // Do something in response to button
    	// GIT test
    	// GIT test2
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }*/
    
}
