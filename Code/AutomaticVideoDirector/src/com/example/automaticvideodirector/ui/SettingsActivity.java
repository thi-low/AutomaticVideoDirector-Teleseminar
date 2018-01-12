package com.example.automaticvideodirector.ui;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	public static final String SERVER_ADDRESS = "server_address";
	public static final String SERVER_PORT = "server_port";
	public static final String EVENT = "event";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
