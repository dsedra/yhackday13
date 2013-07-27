/*
 * Reference: http://sunil-android.blogspot.com/2013/02/create-our-android-compass.html
 */

package com.yhackday.foodcompass;

import com.yhackday.foodcompass.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FoodCompass extends Activity 
		implements SensorEventListener{
	
	private static final int AZIMUTH = 0;
	private static final int PITCH = 1;
	private static final int ROLL = 2;

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
			
			this.compass.updateDirection(this.matrixValues[AZIMUTH]);
		}
	}
}
