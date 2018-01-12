package com.example.automaticvideodirector.ui;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	/**
	 * VARIABLES
	 */
	private SurfaceHolder mHolder;
    private Camera mCamera;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.d("CameraPreview","Surface Created!");
        } catch (IOException e) {
            Log.d("CameraPreview", "Error setting camera preview: " + e.getMessage());
        }
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {

        if (mHolder.getSurface() == null){
          return;
        }
        
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        	Log.d("CameraPreview", "Error stopping camera preview: " + e.getMessage());
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
        }
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//Fixed crash 
		this.getHolder().removeCallback(this);
	    mCamera.release();
		
	}
	
}
