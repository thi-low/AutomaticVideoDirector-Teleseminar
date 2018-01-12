package com.example.automaticvideodirector.domain;

import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * This class is based on the approach of the "Android Cook Book"
 * url = http://androidcookbook.com/Recipe.seam?recipeId=529
 */

public class ShakeDetection extends Observable implements SensorEventListener {
	
	
	/**
	 * VARIABLES
	 */
	private static final String DEBUG_TAG = "ShakeDetetction";
	
	private SensorManager sensorManager;
	
	/* Current values of acceleration, one for each axis */
	private float xCurrent;
	private float yCurrent;
	private float zCurrent;

	/* Previous values of acceleration */
	private float xPrevious;
	private float yPrevious;
	private float zPrevious;

	private boolean updated = true;

	/*Threshold is based on experimenting */
	private final float shakeThreshold = 0.7f;
	
	/* Has a shaking motion been started*/
	private boolean shakeInitiated = false;
	
	
	/**
	 * Constructor
	 */
	public ShakeDetection(SensorManager sensorManager){
		this.sensorManager = sensorManager;
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),	SensorManager.SENSOR_DELAY_NORMAL);
	}
	
		
	/**
	 * CALLED WHEN SENSOR EVENT LISTENER DETECTS CHANGES IN SENSOR DATA
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		updateParameters(event.values[0], event.values[1], event.values[2]);
        if ((!shakeInitiated) && hasAccChanged()) {   
		    shakeInitiated = true; 
	    } else if ((shakeInitiated) && hasAccChanged()) {
		    executeShakeAction();
	    } else if ((shakeInitiated) && (!hasAccChanged())) {
		    shakeInitiated = false;
        }
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	
	/** Acceleraton parameters are updated on change  */
	private void updateParameters(float xNew, float yNew, float zNew) {
        /* we have to suppress the first change of acceleration, it results from first values being initialized with 0 */
		if (updated) {  
			xPrevious = xNew;
			yPrevious = yNew;
			zPrevious = zNew;
			updated = false;
		} else {
			xPrevious = xCurrent;
			yPrevious = yCurrent;
			zPrevious = zCurrent;
		}
		xCurrent = xNew;
		yCurrent = yNew;
		zCurrent = zNew;
	}
	
	/** defines when a movement is interpreted as a shaking motion */
	private boolean hasAccChanged() {
		float deltaX = Math.abs(xPrevious - xCurrent);
		float deltaY = Math.abs(yPrevious - yCurrent);
		float deltaZ = Math.abs(zPrevious - zCurrent);
		return (deltaX > shakeThreshold && deltaY > shakeThreshold)
				|| (deltaX > shakeThreshold && deltaZ > shakeThreshold)
				|| (deltaY > shakeThreshold && deltaZ > shakeThreshold);
	}
	
	/** Notify Observers */
	private void executeShakeAction(){
		setChanged();
		notifyObservers();
		
	}
	
	
	

}
