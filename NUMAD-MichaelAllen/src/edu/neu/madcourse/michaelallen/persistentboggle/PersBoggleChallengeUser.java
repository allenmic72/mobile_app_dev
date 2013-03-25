package edu.neu.madcourse.michaelallen.persistentboggle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioButton;
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
    	
    	ArrayList<String> otherUsersBefore = PersGlobals.getGlobals().getOtherUsers();
    	if (otherUsersBefore == null){
    		try{
    			PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
        		String json = spref.getString(this, "usernames");
        		Gson gson = new Gson();
        		Type type = new TypeToken<ArrayList<String>>(){}.getType();
        		otherUsersBefore = gson.fromJson(json, type);
        		if (otherUsersBefore.size() > 0){
        			CharSequence text = "Trouble connecting to server. Using cached opponents";
            		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        		}
    		}
    		catch (RuntimeException E){
    			otherUsersBefore = new ArrayList<String>();
    		}
    	}
    	final ArrayList<String> otherUsers = otherUsersBefore;
    	
    	if (otherUsers.size() < 1){
    		CharSequence text = "You're the first to play the game. Find a friend to play against!";
    		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    	}
    	
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
				
				AsyncTask<Void, Void, String[]> sendChallengeOrStartAsyncGame = new AsyncTask<Void, Void, String[]>(){

					PersBoggleAsyncGameHelper asyncHelper;
					protected String[] doInBackground(Void... params) {
						if (PersGlobals.getGlobals().canAccessNetwok(context) && KeyValueAPI.isServerAvailable()){
							int modeChecked = mode.getCheckedRadioButtonId();
							Log.d("Challenge", "button checked: " + modeChecked);
							String oppPhoneNum = KeyValueAPI.get("allenmic", "allenmic", opponent);		
							
							PersBoggleAsyncGameHelper asyncHelper = new PersBoggleAsyncGameHelper(context, oppPhoneNum);
							String [] regids = asyncHelper.getRegIds();
							
							if (modeChecked == R.id.pers_boggle_sync_radio){
								
								
								if (oppPhoneNum != null){
									Calendar c = Calendar.getInstance();
									Gson gson = new Gson();
									Date date = c.getTime();
									String dateString = gson.toJson(date);
									
									
									Log.d("challenge this num", "challenging " + oppPhoneNum);
									GCMServlet serv = new GCMServlet();
							        Builder mesBuilder = new Message.Builder();
							        ///NOTE: username and opponent reversed here. These names are from the opponent's perspective
							        mesBuilder.addData("opponent", PersGlobals.getGlobals().getUsername());
							        mesBuilder.addData("username", opponent);
							        mesBuilder.addData("phoneNum", oppPhoneNum);
							        mesBuilder.addData("message", "What up bro");
							        mesBuilder.addData("time", dateString);
							        mesBuilder.addData("type", "challenge");
							        if (regids != null){
							        	//add regids reversed
										mesBuilder.addData("oppRegId", regids[0]);
										mesBuilder.addData("regId", regids[1]);
									}
							        serv.sendMessage(mesBuilder.build(), oppPhoneNum);
							       
							        waitUntilUserAccepts = new waitUntilUserAcceptsChallenge(context);
							        waitUntilUserAccepts.execute(opponent);
							        return null;
								}
							
							}
							else{
								return regids;
							}
							
						}
						CharSequence toastText = "Please connect to the Internet before starting a game";
				        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
						this.cancel(true);
						return null;
					}
					
					 protected void onPostExecute(String[] regIds) {
						 if (regIds == null){
							 if (context != null){

							        CharSequence toastText = "Sent request to " + opponent
							        		+ ". If they accept, the game will start.";
							        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
						      }
						 }
						 else{
							 showAsyncGameDialog(regIds);
						 }
						 
				     }
					 
					 private void showAsyncGameDialog(final String[] regIds){
							AlertDialog.Builder startingAsyncDialog = new AlertDialog.Builder(context);
							startingAsyncDialog.create();
							startingAsyncDialog.setMessage("Starting async game against " + opponent + 
									". You'll play your turn first. Ready?");
							startingAsyncDialog.setPositiveButton("I'm Ready", new DialogInterface.OnClickListener(){
							
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startAsyncGame(regIds);
								}
								
							});
							startingAsyncDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
							
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
								
							});
							startingAsyncDialog.setIcon(R.drawable.ic_launcher);
							startingAsyncDialog.show();
							
						}
						
						private void startAsyncGame(String[] regIds){
							String username = PersGlobals.getGlobals().getUsername();
							Intent i = new Intent(context, PersBoggleGame.class);
							i.putExtra("opponent", opponent);
							i.putExtra("username", username);
							i.putExtra("leader", true);
							i.putExtra("status", "async");
							i.putExtra("regId", regIds[0]);
							i.putExtra("oppRegId", regIds[1]);
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
		String regId = "";
		String oppRegId = "";
		String oppPhoneNum = "";
		
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
				if (oppPhoneNum.equals("")){
					oppPhoneNum = KeyValueAPI.get("allenmic", "allenmic", opponent);
					PersBoggleAsyncGameHelper asyncHelper = new PersBoggleAsyncGameHelper(c, oppPhoneNum);
					String[] regids = asyncHelper.getRegIds();
					regId = regids[0];
					oppRegId = regids[1];
				}
				Gson gson = new Gson();
				String gameState = KeyValueAPI.get("allenmic", "allenmic", opponent + PersGlobals.getGlobals().getUsername());
				if (gameState != null && gameState != ""){
					PersBoggleGameState state = gson.fromJson(gameState, PersBoggleGameState.class);
					Log.d("Wait until user accepts", "got game state status : " + state.gameStatus);
					Intent startGame = new Intent(c, PersBoggleGame.class);
					startGame.putExtra("state", gameState);
					startGame.putExtra("leader", false);
					startGame.putExtra("opponent", opponent);
					startGame.putExtra("status", state.gameStatus);
					startGame.putExtra("regId", regId);
					startGame.putExtra("oppRegId", oppRegId);
					c.startActivity(startGame);
					this.cancel(true);
				}
			}
		}
		
		return null;
	}
	
}
