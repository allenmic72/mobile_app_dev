package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;

import edu.neu.madcourse.michaelallen.R;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class PersBoggleChallengeUser extends Activity implements OnClickListener{
	
	AsyncTask<String, Void, Void> waitUntilUserAccepts;
	CountDownTimer goForFiveMinutes;
	
	protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pers_boggle_challenge_user);
    	
    	View challengeQuit = findViewById(R.id.pers_boggle_challenge_back);
    	challengeQuit.setOnClickListener(this);
    	
    	ListView listv = (ListView) findViewById(R.id.pers_boggle_challenge_view);
    	
    	final ArrayList<String> otherUsers = PersGlobals.getGlobals().getOtherUsers();
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.pers_boggle_challenge_textview, otherUsers);
    	
    	
    	
    	listv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				final int idClicked = (int) id;
				
				AsyncTask<Void, Void, Void> lookupPhone = new AsyncTask<Void, Void, Void>(){

					protected Void doInBackground(Void... params) {
						if (KeyValueAPI.isServerAvailable()){
							String opponent = otherUsers.get(idClicked);
							String phoneNum = KeyValueAPI.get("allenmic", "allenmic", opponent);
							
							
							
							if (phoneNum != null){
								waitUntilUserAccepts = new waitUntilUserAcceptsChallenge();
								
								Calendar c = Calendar.getInstance();
								Gson gson = new Gson();
								Date date = c.getTime();
								String dateString = gson.toJson(date);
								
								Log.d("challenge this num", "challenging " + phoneNum);
								GCMServlet serv = new GCMServlet();
						        Builder mesBuilder = new Message.Builder();
						        mesBuilder.addData("opponent", opponent);
						        mesBuilder.addData("phoneNum", phoneNum);
						        mesBuilder.addData("message", "What up bro");
						        mesBuilder.addData("time", dateString);
						        mesBuilder.addData("type", "challenge");
						        serv.sendMessage(mesBuilder.build(), phoneNum);
						        
						        
						        waitUntilUserAccepts.execute(opponent);
						        goForFiveMinutes = new CountDownTimer(300000, 300000){

									@Override
									public void onFinish() {
										waitUntilUserAccepts.cancel(true);
										
									}

									@Override
									public void onTick(long millisUntilFinished) {
										
									}
						        	
						        }.start();
							}
							
						}
						return null;
					}

				};
				lookupPhone.execute();
				
				
			}
    		}); 
    	
    	listv.setAdapter(adapter);
	}
	


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_challenge_back:
			finish();
			break;
		}
		
	}
	
	private boolean canAccessNetwork() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if (activeNetworkInfo != null){
	    	return true;
	    }
	    else{
	    	return false;
	    }
	    
	}
}

class waitUntilUserAcceptsChallenge extends AsyncTask<String, Void, Void>{
	
	@Override
	protected Void doInBackground(String... params) {
		String opponent = params[0];
		while(!this.isCancelled()){

			if (KeyValueAPI.isServerAvailable()){
				Gson gson = new Gson();
				String gameState = KeyValueAPI.get("allenmic", "allenmic", opponent + PersGlobals.getGlobals().getUsername());
				if (gameState != null && gameState != ""){
					gson.fromJson(gameState, PersBoggleGameState.class);
					
				}
			}
		}
		
		return null;
	}
	
}
