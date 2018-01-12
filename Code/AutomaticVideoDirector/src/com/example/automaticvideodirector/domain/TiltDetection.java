package com.example.automaticvideodirector.domain;

import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TiltDetection extends Observable implements SensorEventListener {
	
	
	/**
	 * VARIABLES
	 */
	private static final String DEBUG_TAG = "TiltDetetction";
	private SensorManager sensorManager;
	
	
	/**
	 * Constructor
	 */
	public TiltDetection(SensorManager sensorManager){
		this.sensorManager = sensorManager;
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),	SensorManager.SENSOR_DELAY_NORMAL);
	}

	
	/**CALLED WHEN SENSOR EVENT LISTENER DETECTS CHANGES IN SENSOR DATA*/
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
	        	// assign axis
				float x=event.values[0];
				float y=event.values[1];
				float z=event.values[2];
				
				if(x>=10.5 || x<=6.0 || y>=2.0 || y<=-2.0 || z>=4.0 || z<=-4.0){
					executeTiltAction();
				}			
    }
		
		
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	
	/** Notify Observers */
	private void executeTiltAction(){
		setChanged();
		notifyObservers();
		
	}
	
}
