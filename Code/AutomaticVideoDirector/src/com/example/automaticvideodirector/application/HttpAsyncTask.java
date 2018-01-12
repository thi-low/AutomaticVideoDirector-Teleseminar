package com.example.automaticvideodirector.application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.automaticvideodirector.database.MetaData;
import com.example.automaticvideodirector.ui.CameraActivity;

import android.os.AsyncTask;
import android.util.Log;

/**
*
* @author thilo
*/

class Wrapper {
	public String result;
	public int code;
	public Wrapper(){
		this.result = "";
		this.code = -1;
	}
	public Wrapper(String result, int code){
		this.result = result;
		this.code = code;
	}
}

public class HttpAsyncTask extends AsyncTask<String, Void, Wrapper> {
	
	public static final int HTTP_POST   = 1;
	public static final int HTTP_GET    = 2;
	public static final int HTTP_UPLOAD = 3;
	
	public static final int SERVER_UNREACHABLE = 0;
	
	private static final String DEBUG_TAG = "HTTP-POST-AutomaticVideoDirector";
	
	private int request;
	private String url;
	private MetaData data;
	private Callback callback;
	
	public interface Callback {
		public void run(String result, int code);
	}
	
	public HttpAsyncTask(int request, String url, MetaData data, Callback callback){
		this.request=request;
		this.data = data;
		this.url = url;
		this.callback = callback;
	}
	
	@Override
	protected Wrapper doInBackground(String... urls) {
		Log.d(DEBUG_TAG, "doInBackground first line");
		Wrapper w = null;
		try {
			if (request == HTTP_GET) {	
				w = httpGet(url); 
			} 
			if (request == HTTP_POST) {
				w = httpPost(url, data);
			} 
			if (request == HTTP_UPLOAD) {
				Log.d(DEBUG_TAG, "attempting HTTP_UPLOAD");
				w = httpFileUpload(url, data);
			}
		} catch (IOException e) {
			Log.d(DEBUG_TAG, e.getMessage());
			w =  null;
		}
		return w;
	}
	
	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(Wrapper w) {
		if (w == null) {
			callback.run("Server unreachable", SERVER_UNREACHABLE);
			return;
		}
			
		if (w.result == null) {
			Log.d(DEBUG_TAG, "Error");
		} else {
			Log.d(DEBUG_TAG, "Result: "+ w.result);
		}
		if (callback != null) {
			callback.run(w.result, w.code);
		}
	}
	
	/*
	 * HTTP requests
	 */
	public Wrapper httpPost(String myurl, MetaData data) throws IOException {

		Wrapper response = new Wrapper();

		URL url = new URL(myurl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		try {
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setChunkedStreamingMode(0);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.connect();

			//Creation - JSONObject
			JSONObject jsonParam = new JSONObject();
			try {
				jsonParam.put("name", data.getVideoFile());
				jsonParam.put("finish_time", data.getTimeStamp());
				jsonParam.put("duration", data.getDuration());
				jsonParam.put("width", data.getWidth());
				jsonParam.put("height", data.getHeight());
				jsonParam.put("shaking", data.getShaking());
				jsonParam.put("tilt", data.getTilt());
				Log.d(DEBUG_TAG, jsonParam.toString());
			} catch (JSONException e) {
				Log.d(DEBUG_TAG, "JSON wrong");
				e.printStackTrace();
			}

			//POST REQUEST
			OutputStream output = new BufferedOutputStream(urlConnection.getOutputStream());
			output.write(jsonParam.toString().getBytes());
			output.flush();

			//POST RESPONSE
			StringBuilder sb = new StringBuilder();
			response.code = urlConnection.getResponseCode();
			Log.d(DEBUG_TAG, "httpResult: " + response.code);
			if (response.code == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream(), "utf-8")
				);
				while ((response.result = br.readLine()) != null) {
					sb.append(response.result + "\n");
				}
				br.close();
				Log.d(DEBUG_TAG, sb.toString());
				response.result = sb.toString();
			} else {
				Log.d(DEBUG_TAG, urlConnection.getResponseMessage());
			}
		} finally {
			urlConnection.disconnect();
		}
		return response;
	}
	
	
	public Wrapper httpGet(String myurl) throws IOException {
		URL url = new URL(myurl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setDoInput(true);
		urlConnection.connect();
		Wrapper response = new Wrapper("", urlConnection.getResponseCode());
		Log.d(DEBUG_TAG, "The response is: " + response.code);

		InputStream in = null;
		List<String> responseBody = new ArrayList<String>();
		try {
			if (response.code == HttpURLConnection.HTTP_OK) {
				in = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = null;
				while ((line = reader.readLine()) != null) {
					responseBody.add(line);
				}
				reader.close();
				response.result = responseBody.toString();
			} else {
				Log.d(DEBUG_TAG, urlConnection.getResponseMessage());
			}
		} finally {
			if (in != null) {
				in.close();
			}
			urlConnection.disconnect();
		}
		return response;
	}
	
	public Wrapper httpFileUpload(String myurl, MetaData data) throws IOException {

		URL url = new URL(myurl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		try {
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
			urlConnection.setRequestMethod("PUT");
			urlConnection.setChunkedStreamingMode(0);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Cache-Control", "no-cache");
			urlConnection.setRequestProperty("Content-Type", "video/mpeg");
			urlConnection.connect();
			Log.d(DEBUG_TAG, "Url Connection setup complete");
			
			DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
            
            FileInputStream fstrm = new FileInputStream(CameraActivity.getVideoDir() 
            		+ "/" + data.getVideoFile());
            // create a buffer of maximum size
            int bytesAvailable = fstrm.available();
                
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[ ] buffer = new byte[bufferSize];

            // read file and write it into stream...
            int bytesRead = fstrm.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fstrm.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fstrm.read(buffer, 0,bufferSize);
            }

            // close streams
            fstrm.close();
                
            dos.flush();
            
			Wrapper response = new Wrapper("", urlConnection.getResponseCode());
            Log.d(DEBUG_TAG, Integer.toString(response.code));
            Log.d(DEBUG_TAG, "Reading response");
            
            if (response.code != HttpURLConnection.HTTP_CREATED) {
            	Log.d(DEBUG_TAG, urlConnection.getResponseMessage());
            	return response;
            }

            // retrieve the response from server
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			List<String> responseBody = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				responseBody.add(line);
			}
			reader.close();
			dos.close();
			
			response.result = responseBody.toString();
            Log.d(DEBUG_TAG, response.result);
            return response;
		} finally {
			urlConnection.disconnect();
		}
	}
}
