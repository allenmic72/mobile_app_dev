package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;

import edu.neu.madcourse.michaelallen.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class PersBoggleNotificationClicked extends Activity implements OnClickListener{
	
	protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
    	
    	Log.d("TATATA", "test");
    	
    	setContentView(R.layout.pers_boggle_accept_challenge);
    	
    	View acceptSync = findViewById(R.id.pers_boggle_accept_sync);
    	acceptSync.setOnClickListener(this);
    	
    	View acceptAsync = findViewById(R.id.pers_boggle_accept_async);
    	acceptAsync.setOnClickListener(this);
    	
    	View decline = findViewById(R.id.pers_boggle_decline);
    	decline.setOnClickListener(this);
    	
    	Gson gson = new Gson();
    	Date timeSent = gson.fromJson(getIntent().getStringExtra("time"), Date.class);
    	
    	if (withinLastFiveMinutes(timeSent)){
    		acceptSync.setVisibility(View.VISIBLE);
    	}
    	
	}
	
	
	private boolean withinLastFiveMinutes(Date timeSent){
		Date current = Calendar.getInstance().getTime();
		long timePassed = current.getTime() - timeSent.getTime();
		return timePassed < 300000; //300000 millis = 5 minutes
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_accept_sync:
			Intent sync = new Intent(this, PersBoggleGame.class);
			sync.putExtra("status", "sync");
			startActivity(sync);
			finish();
			break;
		case R.id.pers_boggle_accept_async:
			Intent async = new Intent(this, PersBoggleGame.class);
			async.putExtra("status", "async");
			startActivity(async);
			finish();
			break;
		case R.id.pers_boggle_decline:
			//TODO decline chalenge
			break;
		}
	}
}
