/*
 * Reference: http://sunil-android.blogspot.com/2013/02/create-our-android-compass.html
 */

package com.yhackday.foodcompass;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.yhackday.foodcompass.util.SystemUiHider;

import android.app.Activity;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FoodCompass extends Activity 
		implements SensorEventListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
	
	private static final int AZIMUTH = 0;
	private static final int PITCH = 1;
	private static final int ROLL = 2;
	
	private static final float NORTH_LATITUDE = 90f;
	private static final float NORTH_LONGITUDE = 0f;
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = 1000 * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = 1000 * FASTEST_INTERVAL_IN_SECONDS;

	private SystemUiHider mSystemUiHider;
	
	private SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorMagneticField;
	
	private float[] valuesAccelerometer;
	private float[] valuesMagneticField;
	
	private float[] matrixR;
	private float[] matrixI;
	private float[] matrixValues;
	
	private Compass compass;
	
	private LocationClient locationClient;
	private LocationRequest locationRequest;
	private Location currentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		this.compass = (Compass) findViewById(R.id.compass_view);
		
		this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		this.sensorAccelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		this.sensorMagneticField = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		this.valuesAccelerometer = new float[3];
		this.valuesMagneticField = new float[3];
		this.matrixR = new float[9];
		this.matrixI = new float[9];
		this.matrixValues = new float[3];
		
		this.locationClient = new LocationClient(this, this, this);
		this.locationRequest = LocationRequest.create();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		this.locationRequest.setInterval(UPDATE_INTERVAL);
		this.locationRequest.setFastestInterval(FASTEST_INTERVAL);
	}
	
	@Override
	protected void onStart() {
		this.locationClient.connect();
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		this.sensorManager.registerListener(
				this,
				this.sensorAccelerometer, 
				SensorManager.SENSOR_DELAY_NORMAL);
		this.sensorManager.registerListener(
				this, 
				this.sensorMagneticField,
				SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
		
	}
	
	@Override
	protected void onPause() {
		this.sensorManager.unregisterListener(
				this,
				this.sensorAccelerometer);
		this.sensorManager.unregisterListener(
				this,
				this.sensorMagneticField);
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		this.locationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch(event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			for(int i=0; i<3; i++) {
				this.valuesAccelerometer[i] = event.values[i];
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			for(int i=0; i<3; i++) {
				this.valuesMagneticField[i] = event.values[i];
			}
			break;
		}
		
		boolean success = SensorManager.getRotationMatrix(
				this.matrixR, 
				this.matrixI, 
				this.valuesAccelerometer, 
				this.valuesMagneticField);
		
		if(success) {
			SensorManager.getOrientation(
					this.matrixR, 
					this.matrixValues);
			
			if (this.currentLocation != null) {
				Location northPole = new Location("");
				northPole.setLatitude(NORTH_LATITUDE);
				northPole.setLongitude(NORTH_LONGITUDE);
				
				float bearing = this.currentLocation.bearingTo(northPole);
				//this.compass.updateDirection(bearing);
				this.compass.updateDirection(this.matrixValues[AZIMUTH]);
				//this.compass.updateDirection(10);
				
				Log.d("BEARING", ""+bearing);
				Log.d("AZIMUTH", ""+this.matrixValues[AZIMUTH]);
			}
		}
		
		if (this.locationClient.isConnected()) {
			this.currentLocation = this.locationClient.getLastLocation();
			if (this.currentLocation != null) {
				//Log.d("LOCATION",this.currentLocation.toString());
			}
			else {
				Log.d("LOCATION", "NULL");
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
            try {
                result.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
            }
        } else {
        	Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
            Log.e("FoodCompass", ""+result.getErrorCode());
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		this.locationClient.requestLocationUpdates(this.locationRequest, this);
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			Log.d("LOCATION","Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
			this.currentLocation = location;
		}
		else {
			Log.d("LOCATION", "NULL");
		}
	}
}
