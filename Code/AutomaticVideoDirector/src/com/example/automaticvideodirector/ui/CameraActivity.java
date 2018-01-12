package com.example.automaticvideodirector.ui;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.automaticvideodirector.R;
import com.example.automaticvideodirector.application.HttpAsyncTask;
import com.example.automaticvideodirector.database.MetaData;
import com.example.automaticvideodirector.database.MetaDataSource;
import com.example.automaticvideodirector.domain.ShakeDetection;
import com.example.automaticvideodirector.domain.TiltDetection;
/**
 * 
 * This activity is responsible to record a video (of max. 20sec???). 
 * After creating the mediafile, a connection to the database has to be established to insert all the necessary meta-information.-->MetaDatSource.insertMetaData();
 * Additionally a HTTPURLCONNECTION has to be created to POST the metadata via JSON to the server--> new HttpAsnycTask("POST",MetaData metaData);
 * Last but not least information to user, if successful or not.
 * 
 */

public class CameraActivity extends Activity implements Observer {
	
	/**
	 * VARIABLES
	 */
	private static final String DEBUG_TAG = "CameraActivity";
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private boolean isRecording = false;
	
	//CAMERA & MEDIA RECORDER
	private Camera cameraInstance;
	private CameraPreview cameraPreview;
	private MediaRecorder mediaRecorder;
	
	//FIEL STORAGE & DB
	private MetaDataSource datasource;
	private MetaData metaData;
	private File handlerFile;
		
	//SENSOR OBSERVER
	private ShakeDetection shakeDetector;
	private SensorManager sensorManager;
	private TiltDetection tiltDetector;
	private int counterShake;
	private int counterTilt;
	
	
	private Button captureButton;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		captureButton = (Button) findViewById(R.id.button_capture);
   
	}
	
	/*
	 * http://stackoverflow.com/questions/7754263/android-record-video-with-continuous-auto-focus
	 */	
	boolean startContinuousAutoFocus() {
		Camera.Parameters params = cameraInstance.getParameters();

		List<String> focusModes = params.getSupportedFocusModes();

		String CAF_PICTURE = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
		String CAF_VIDEO = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
		String supportedMode = focusModes.contains(CAF_PICTURE) ? CAF_PICTURE
				: focusModes.contains(CAF_VIDEO) ? CAF_VIDEO : null;
		
		if (supportedMode != null) {
			System.out.println("Autofocus enabled");
			params.setFocusMode(supportedMode);
			cameraInstance.setParameters(params);
			return true;
		}

		return false;
	}
	
	protected void onResume() {
		super.onResume();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		 
		Log.d(DEBUG_TAG,"Camara Activity resumed");
		//CAMERA PREVIEW
		cameraInstance = getCameraInstance();
		cameraPreview = new CameraPreview(this, cameraInstance);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        startContinuousAutoFocus();
        
        //ESTABLISH CONNECTION TO DATABASE 
        datasource = new MetaDataSource(this);
        datasource.open();
        
        //SENSOR MANAGER
        counterShake=0;
        counterTilt=0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        shakeDetector = new ShakeDetection(sensorManager);
        shakeDetector.addObserver(CameraActivity.this);
        tiltDetector = new TiltDetection(sensorManager);
        tiltDetector.addObserver(CameraActivity.this);
	}
	
	
	@Override
    protected void onPause() {
		Log.d(DEBUG_TAG,"Camera Activity paused");
        datasource.close();
        releaseMediaRecorder();
        releaseCamera();
        counterShake=0;
        counterTilt=0;
        shakeDetector.deleteObservers();
        tiltDetector.deleteObservers();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
	}
	
//	@Override
//    protected void onStop() {
//		Log.d(DEBUG_TAG,"Camera Activity stopped");
//        datasource.close();
//        releaseMediaRecorder();
//        releaseCamera();
//        counter=0;
//        shakeDetector.deleteObservers();
//        super.onStop();
//    }
//
//	@Override    
//	protected void onDestroy() {
//		Log.d(DEBUG_TAG,"Camera Activity destroyed");
//		datasource.close();
//		releaseMediaRecorder();
//		releaseCamera();
//		counter=0;
//		shakeDetector.deleteObservers();
//		super.onDestroy();
//	}

	/**IS CALLED WHEN THERE ARE CHANGES IN THE ACCELEROMETER OBSERVABLE */
	@Override
	public void update(Observable observable, Object data) {
		if(observable==shakeDetector){
			counterShake++;
			if(counterShake%10==0){
				show_toast("Reduce Shaking");
			}
		}
		else if(observable==tiltDetector){
			counterTilt++;
			if(counterTilt%40==0){
				show_toast("Hold straight");
			}
			
		}
	}

	/**REALEASE MEDIA RECORDER*/
    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void releaseCamera(){
        if (cameraInstance != null){
        	cameraInstance.release();
        	cameraInstance = null;
        }
    }

    /** GET AN INSTANCE OF THE CAMERA OBJECT */
	public static Camera getCameraInstance(){
	    Camera camera = null;
	    try {
	        camera = Camera.open();
	    }
	    catch (Exception e){
	    	Log.d(DEBUG_TAG,"Camera.open() failed");
	    }
	    return camera;
	}

	/** PREPARE MEDIA RECORDER FOR RECORDING */
	private boolean prepareVideoRecorder(){
		Log.d(DEBUG_TAG,"prepareVideoRecorder()");
	    mediaRecorder = new MediaRecorder();

	    //Unlock and set camera to MediaRecorder
	    cameraInstance.unlock();
	    mediaRecorder.setCamera(cameraInstance);
	    // Set Video and Audio sources
	    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    //SETS VIDEO QUALITY:HIGH
	    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
	    //SET OUTOUT FILE
	    handlerFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
	    mediaRecorder.setOutputFile(handlerFile.toString());
	    //Set the preview output to our preview surfaceholder
	    mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
	    // Prepare configured MediaRecorder
	    try {
	    	mediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.d(DEBUG_TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.d(DEBUG_TAG, "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	    Log.i(DEBUG_TAG,"Media recorder successfully prepared");
	    return true;
	}
	
		
	static public String getVideoDir() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) 
				+ "/Automatic Video Director";
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		Log.d("CameraActivity","in getOutputMediaFile1");
		File mediaStorageDir = new File(getVideoDir());
	    Log.d("CameraActivity",mediaStorageDir.getPath());
	    if (! mediaStorageDir.exists()){
	    	Log.d("CameraActivity", "Directory does not exists");
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("CameraActivity", "failed to create directory");
	            return null;
	        }
	    }
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }
	    return mediaFile;
	}

	/**
	 * Listener 
	 */
	public void recordListener(View v) {
        if (isRecording) {
            // inform the user that recording has stopped
            Log.d(DEBUG_TAG,"Media recorder was already recording");
            // stop recording and release camera
            mediaRecorder.stop();   // stop the recording
            Log.i(DEBUG_TAG,"Media recorder stopped");
            releaseMediaRecorder(); // release the MediaRecorder object
            Log.i(DEBUG_TAG,"Media recorder released");
            cameraInstance.lock();  // take camera access back from MediaRecorder
            Log.d(DEBUG_TAG,"Media recorder stopped, camera locked");
            
            
            captureButton.setBackgroundColor(getResources().getColor(R.color.green));
            isRecording = false;
            
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Log.d(DEBUG_TAG,Uri.fromFile(handlerFile).toString());
            retriever.setDataSource(CameraActivity.this, Uri.fromFile(handlerFile));
            
            metaData=getMetaDataFromFile(retriever);
            Date lastModDate = new Date(handlerFile.lastModified());
            Timestamp timestamp = new Timestamp(lastModDate.getTime());
            metaData.setTimeStamp(timestamp.toString());
            long insertId = datasource.insertMetaData(metaData);

            Log.d(DEBUG_TAG,"New row in table: ID=" +insertId +" CounterShake: "+counterShake +", CounterTilt: "+counterTilt);
            counterShake=0;
            counterTilt=0;
        	new HttpAsyncTask(HttpAsyncTask.HTTP_POST,
        			ServerLocations.getVideoMetadataUploadUrl(CameraActivity.this), 
        			metaData, 
	        		new HttpAsyncTask.Callback() {
						@Override
						public void run(String result, int code) {
							if (result != null && code == HttpURLConnection.HTTP_OK) {
								try {   
									//UPDATE DATABASE WITH SERVERID...
									System.out.println(result);
									String no_escape = result
											.replace("\\\"", "\"")
											.replace("\"{", "{")
											.replace("}\"", "}");
									JSONObject json = new JSONObject(no_escape);
									int serverID = json.getInt("id");
									String name = json.getString("name");
									System.out.println(name + " --> " + serverID);
									datasource.updateServerId(serverID, name);
									show_toast("Metadata was succefully sent to the server. "
											+name +" has new server-ID: "+ serverID);	
								} catch (Exception e) {
									System.out.println(e.getMessage());
									show_toast("Upload of MetaData has failed");
								}
							} else {
								show_toast("Server returned an error: " + code + " - " + result);
							}
						}
			}).execute();
            //Scans the external directory to add the media file immediately after creating
        	MediaScannerConnection.scanFile(CameraActivity.this,
        	          new String[] { handlerFile.toString() }, null,
        	          new MediaScannerConnection.OnScanCompletedListener() {
        	      public void onScanCompleted(String path, Uri uri) {
        	          Log.i("ExternalStorage", "Scanned " + path + ":");
        	          Log.i("ExternalStorage", "-> uri=" + uri);
        	      }
        	});
            
            
        } else {
        	
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
            	Log.d("CameraActivity","Media recorder will be started in the next line");
                mediaRecorder.start();
                Log.d("CameraActivity","Media recorder started");
                counterShake=0;
                counterTilt=0;
                // inform the user that recording has started
                
                //TODO Hangs if you do this 
//                captureButton.setText(getString(R.string.button_stop));
                captureButton.setBackgroundColor(getResources().getColor(R.color.red));
                
                isRecording = true;
                Log.i("CameraActivity","Media recorder started");
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
            
        
	}

	public MetaData getMetaDataFromFile(MediaMetadataRetriever retriever){
		Log.i(DEBUG_TAG, "Retrieve meta data from mediafile");
		MetaData data = new MetaData();
		data.setVideoFile(handlerFile.getName());
//		data.setTimeStamp(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
		data.setTimeStamp(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
		data.setDuration(Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
		data.setShaking(counterShake);
		data.setTilt(counterTilt);
		data.setWidth(Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
		data.setHeight(Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
		return data;
		
	}
	
	private void show_toast (String s) {
    	Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        toast.show();
	}

	
}
