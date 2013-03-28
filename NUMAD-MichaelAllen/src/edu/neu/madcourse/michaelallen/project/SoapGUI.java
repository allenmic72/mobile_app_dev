package edu.neu.madcourse.michaelallen.project;

import edu.neu.madcourse.michaelallen.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SoapGUI extends Activity implements OnClickListener{
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.soap_gui);
		
		View startServiceButton = findViewById(R.id.soap_gui_start_service);
		startServiceButton.setOnClickListener(this);
		
		View killServiceButton = findViewById(R.id.soap_gui_kill_service);
		killServiceButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.soap_gui_start_service:
			Intent startService = new Intent(this, AccelerometerListenerService.class);
			startService(startService); 
			break;
		case R.id.soap_gui_kill_service:
			Intent stopService = new Intent(this, AccelerometerListenerService.class);
			stopService(stopService); 
			break;
		}
		
	}
	
	
}