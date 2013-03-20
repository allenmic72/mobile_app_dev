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
import android.content.Intent;
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
    	
    	
    	final Context c = this;
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
								waitUntilUserAccepts = new waitUntilUserAcceptsChallenge(c);
								
								Calendar c = Calendar.getInstance();
								Gson gson = new Gson();
								Date date = c.getTime();
								String dateString = gson.toJson(date);
								
								Log.d("challenge this num", "challenging " + phoneNum);
								GCMServlet serv = new GCMServlet();
						        Builder mesBuilder = new Message.Builder();
						        ///NOTE: username and opponent reversed here. These names are from the opponent's perspective
						        mesBuilder.addData("opponent", PersGlobals.getGlobals().getUsername());
						        mesBuilder.addData("username", opponent);
						        mesBuilder.addData("phoneNum", phoneNum);
						        mesBuilder.addData("message", "What up bro");
						        mesBuilder.addData("time", dateString);
						        mesBuilder.addData("type", "challenge");
						        serv.sendMessage(mesBuilder.build(), phoneNum);
						        
						        waitUntilUserAccepts.execute(opponent);
						        
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
	final Context c;
	waitUntilUserAcceptsChallenge(final Context c){
		this.c = c;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		long startTime = Calendar.getInstance().getTimeInMillis();
		long currentTime;
		String opponent = params[0];
		
		//clear the key in case a past game state is still there
		if (KeyValueAPI.isServerAvailable()){
			KeyValueAPI.clearKey("allenmic", "allenmic", opponent + PersGlobals.getGlobals().getUsername());
			KeyValueAPI.clearKey("allenmic", "allenmic", PersGlobals.getGlobals().getUsername() + opponent);
		}
		
		
		while(!this.isCancelled()){
			Log.d("Waituntiluseraccepts", "still waiting, checking " + opponent + PersGlobals.getGlobals().getUsername());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
			currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime - startTime > 300000){
				this.cancel(true);
			}
			if (KeyValueAPI.isServerAvailable()){
				Gson gson = new Gson();
				String gameState = KeyValueAPI.get("allenmic", "allenmic", opponent + PersGlobals.getGlobals().getUsername());
				if (gameState != null && gameState != ""){
					PersBoggleGameState state = gson.fromJson(gameState, PersBoggleGameState.class);
					Log.d("Wait until user accepts", "got game state status : " + state.gameStatus);
					Intent startGame = new Intent(c, PersBoggleGame.class);
					startGame.putExtra("state", gameState);
					startGame.putExtra("leader", false);
					startGame.putExtra("opponent", opponent);
					c.startActivity(startGame);
					this.cancel(true);
				}
			}
		}
		
		return null;
	}
	
}
