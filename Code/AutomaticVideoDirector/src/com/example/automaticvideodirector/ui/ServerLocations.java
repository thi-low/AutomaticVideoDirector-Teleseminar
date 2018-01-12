package com.example.automaticvideodirector.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class ServerLocations {
	static final String event_new = "/event/new"; //GET
	static final String event_descr = "/event/"; //event_id, GET
	static final String video_metadata_upload = "/event/"; //event_id, POST
	static final String video_upload = "/video/"; //video_id, PUT
	static final String video_download = "/video/"; //video_id, GET
	static final String video_selected = "/selected"; //GET
	
	static String getServerAndPort(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return "http://"
        		+ sharedPref.getString(SettingsActivity.SERVER_ADDRESS, "") + ":"
        		+ sharedPref.getString(SettingsActivity.SERVER_PORT, "");
	}

	
	public static String getVideoUploadUrl(Context context, int id) {
        return getServerAndPort(context) + video_upload + Integer.toString(id);
	}
	
	static String getVideoMetadataUploadUrl(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String event = sharedPref.getString(SettingsActivity.EVENT, "");
	    return getServerAndPort(context) + video_metadata_upload + event;
	}
	
	static String getEventDescriptionUrl(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String event = sharedPref.getString(SettingsActivity.EVENT, "");
	    return getServerAndPort(context) + event_descr  + event;
	}
	
	public static String getSelectedListUrl(Context context) {
        return getServerAndPort(context) + video_selected;
	}
	
}
