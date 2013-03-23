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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PersBoggleAcceptChallenge extends Activity implements OnClickListener{
	String opponent;
	String username;
	String regId;
	String oppRegId;
	
	protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.pers_boggle_accept_challenge);
    	
    	View acceptButton = findViewById(R.id.pers_boggle_accept_challenge);
    	acceptButton.setOnClickListener(this);
    	
    	View decline = findViewById(R.id.pers_boggle_decline);
    	decline.setOnClickListener(this);
    	
    	Gson gson = new Gson();
    	Date timeSent = gson.fromJson(getIntent().getStringExtra("time"), Date.class);
    	
    	opponent = getIntent().getStringExtra("opponent");
    	username = getIntent().getStringExtra("username");
    	regId = getIntent().getStringExtra("regId");
    	oppRegId = getIntent().getStringExtra("oppRegId");
    	
    	if (!withinLastFiveMinutes(timeSent)){
    		RadioButton syncRadio = (RadioButton) findViewById(R.id.pers_boggle_accept_sync_radio);
    		syncRadio.setEnabled(false);
    	}
    	
	}
	
	
	private boolean withinLastFiveMinutes(Date timeSent){
		Date current = Calendar.getInstance().getTime();
		long timePassed = current.getTime() - timeSent.getTime();
		return timePassed < 300000; //300000 millis = 5 minutes
	}
	
	private Intent checkRadioAndCreateProperIntent(){
		Intent i = new Intent(this, PersBoggleGame.class);
		i.putExtra("opponent", opponent);
		i.putExtra("username", username);
		i.putExtra("leader", true);
		i.putExtra("regId", regId);
		i.putExtra("oppRegId", oppRegId);
		
		RadioGroup r = (RadioGroup) findViewById(R.id.pers_boggle_accept_radiogroup);
		int id = r.getCheckedRadioButtonId();
		
		if (id == R.id.pers_boggle_accept_sync_radio){
			i.putExtra("status", "sync");
		}
		else if(id == R.id.pers_boggle_accept_async_radio){
			i.putExtra("status", "async");
		}
		else{
			return null;
		}
		return i;
		
		
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_accept_challenge:
			Intent i = checkRadioAndCreateProperIntent();
			if (i != null){
				Log.d("Acceptchallenge", i.getExtras().getString("status"));
				startActivity(i);
				finish();
			}
			else{
				CharSequence toastText = "Please select a game mode";
		        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.pers_boggle_decline:
			//TODO
			finish();
			break;
		}
	}
}
