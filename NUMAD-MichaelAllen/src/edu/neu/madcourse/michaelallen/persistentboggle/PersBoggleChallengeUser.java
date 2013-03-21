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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    	
    	
    	final Context context = this;

    	final RadioGroup mode = (RadioGroup) findViewById(R.id.pers_boggle_challenge_radiogroup);
    	
    	listv.setOnItemClickListener(new OnItemClickListener() {
    		
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				
				CharSequence toastText = "Please wait a moment...";
		        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
		        
				final int idClicked = (int) id;
				final String opponent = otherUsers.get(idClicked);
				
				AsyncTask<Void, Void, String> sendChallengeOrStartAsyncGame = new AsyncTask<Void, Void, String>(){

					protected String doInBackground(Void... params) {
						if (KeyValueAPI.isServerAvailable()){
							int modeChecked = mode.getCheckedRadioButtonId();
							
							String phoneNum = KeyValueAPI.get("allenmic", "allenmic", opponent);		
							
							if (modeChecked == R.id.pers_boggle_accept_sync_radio){
								
								
								if (phoneNum != null){
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
							       
							        waitUntilUserAccepts = new waitUntilUserAcceptsChallenge(context);
							        waitUntilUserAccepts.execute(opponent);
							        return null;
								}
							
							}
							else{
								String regId = KeyValueAPI.get("allenmic", "allenmic", phoneNum + "regId");
								return regId;
							}
							
						}
						return null;
					}
					
					 protected void onPostExecute(String regId) {
						 if (regId == null){
							 if (context != null){

							        CharSequence toastText = "Sent request to " + opponent
							        		+ ". If they accept, the game will start.";
							        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
						      }
						 }
						 else{
							 showAsyncGameDialog(regId);
						 }
						 
				     }
					 
					 private void showAsyncGameDialog(final String regId){
							AlertDialog.Builder startingAsyncDialog = new AlertDialog.Builder(context);
							startingAsyncDialog.create();
							startingAsyncDialog.setMessage("Starting async game against " + opponent + 
									". You'll play your turn first. Ready?");
							startingAsyncDialog.setPositiveButton("I'm Ready", new DialogInterface.OnClickListener(){
							
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startAsyncGame(regId);
								}
								
							});
							startingAsyncDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
							
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
								
							});
							startingAsyncDialog.setIcon(R.drawable.ic_launcher);
							startingAsyncDialog.show();
							
						}
						
						private void startAsyncGame(String regId){
							String username = PersGlobals.getGlobals().getUsername();
							Intent i = new Intent(context, PersBoggleGame.class);
							i.putExtra("opponent", opponent);
							i.putExtra("username", username);
							i.putExtra("leader", true);
							i.putExtra("status", "async");
							i.putExtra("regId", regId);
							startActivity(i);
							finish();
						}
					 

				};
				sendChallengeOrStartAsyncGame.execute();
				
				
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
