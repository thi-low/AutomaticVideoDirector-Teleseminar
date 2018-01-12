package com.example.automaticvideodirector.ui;

import java.net.HttpURLConnection;

import com.example.automaticvideodirector.R;
import com.example.automaticvideodirector.application.HttpAsyncTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayMessageActivity extends Activity {
	private TextView mResponseTextView;
	
	private void show_toast (String s){
    	Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        toast.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);
		
	    mResponseTextView = (TextView)findViewById(R.id.response_body);
		
		Intent intent = getIntent();
		String requestURL = intent.getStringExtra(MainActivity.REQUEST_URL);
    	
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo == null || !networkInfo.isConnected()) {
	    	show_toast("No network");
	    	return;
	    }
	    
		new HttpAsyncTask(HttpAsyncTask.HTTP_GET, requestURL, null,
			new HttpAsyncTask.Callback() {
				@Override
				public void run(String result, int code) {
					if (result != null && code == HttpURLConnection.HTTP_OK) {
	    				mResponseTextView.setText(result);
	    			} else {
	    				mResponseTextView.setText("HTTP_GET failed: " + code + " - " + result);
	    			}
				}
			}
		).execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings: {
	            launchSettings(getCurrentFocus());
	        }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
    public void launchSettings(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		this.startActivity(intent);
    }
}
