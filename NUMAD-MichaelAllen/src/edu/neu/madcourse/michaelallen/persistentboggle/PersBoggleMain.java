package edu.neu.madcourse.michaelallen.persistentboggle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.google.android.gcm.server.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.michaelallen.R;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class PersBoggleMain extends Activity implements OnClickListener{

	View continueButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.pers_boggle_main);
        
        View exitButton = findViewById(R.id.pers_boggle_exit_button);
        exitButton.setOnClickListener(this);
        
        View acknowledgementsButton = findViewById(R.id.pers_boggle_acknowledgements_button);
        acknowledgementsButton.setOnClickListener(this);
        
        continueButton = findViewById(R.id.pers_boggle_continue_button);
        continueButton.setOnClickListener(this);
        
        View highScoresButton = findViewById(R.id.pers_boggle_high_scores);
        highScoresButton.setOnClickListener(this);
       
        View boggleRulesButton = findViewById(R.id.pers_boggle_rules_button);
        boggleRulesButton.setOnClickListener(this);
        
        View boggleChallengeButton = findViewById(R.id.pers_boggle_challenge_button);
        boggleChallengeButton.setOnClickListener(this);
		
        
        
        

        
    }
    
    @Override
    protected void onResume() {
       super.onResume();
       
       if (canAccessNetwork()){
           AsyncTask<Integer, Void, String> setHSList = new PersBoggleGetHighScoresAndUpdate(this);
           setHSList.execute(-1, -1);
       }
       
       getUsername();
       if (PersGlobals.getGlobals().newGameStarted()){
    	   continueButton.setVisibility(View.VISIBLE);
       }
       else{
    	   continueButton.setVisibility(View.INVISIBLE);
       }
    }

    @Override
    protected void onPause() {
       super.onPause();
       //BoggleMusic.stop(this);
    }
    
    @Override
    protected void onStop() {
       super.onStop();
       PersGlobals.getGlobals().cancelWaitTask();
    }

    

	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		 case R.id.pers_boggle_exit_button:
			 finish();
			 break;
		 case R.id.pers_boggle_acknowledgements_button:
			 Intent boggleack = new Intent(this, PersBoggleAcknowledgements.class);
			 startActivity(boggleack);
			 break;
		 case R.id.pers_boggle_continue_button:
			 if(PersGlobals.getGlobals().newGameStarted()){
				 Intent continueGame = new Intent(this, PersBoggleGame.class);
				 continueGame.putExtra("edu.neu.madcourse.michaelallen.persistentboggle.resume", 1);
				 startActivity(continueGame);
			 }
			 break;
		 case R.id.pers_boggle_high_scores:
			 Intent boggleHS = new Intent(this, PersBoggleViewHighScores.class);
			 startActivity(boggleHS);
			 break;
		 case R.id.pers_boggle_rules_button:
			 Intent boggleRules = new Intent(this, PersBoggleRules.class);
			 startActivity(boggleRules);
			 break;
		 case R.id.pers_boggle_challenge_button:
			 if (checkUserName()){
				 Intent boggleChallenge = new Intent(this, PersBoggleChallengeUser.class);
				 startActivity(boggleChallenge);
			 }
			 break;
		 }
		
	}
	
	private boolean canAccessNetwork() {
	    return PersGlobals.getGlobals().canAccessNetwok(this);
	    
	}
	
	private String getUsernameFromSharedPref(){
		return new PersBoggleSharedPrefAPI().getString(this, "username");
	}
	
	
	private void getUsername(){
		String username = getUsernameFromSharedPref();
		if (username != null){
			PersGlobals.getGlobals().setUsername(username);
			//Log.d("MainActivity getUsername", "username is " + username);
			
			AsyncTask<String, Void, Void> addUsernameToArray = new addToArrayOfUsersOnServer(this);
			addUsernameToArray.execute(username);
		}
		else{
			Log.d("MainActivity getUsername", "no username in shared pref, checking server");
			if (canAccessNetwork()){
				TelephonyManager tm = (TelephonyManager) 
					    getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
				String phoneNumber = tm.getLine1Number();
				
	            AsyncTask<String, Void, String> usernameFromServer = new getUsernameFromServer(this);
	            usernameFromServer.execute(phoneNumber);
	            //TODO 
	        }
		}
	}
	
	/**
	 * No username in sharedPref or in the server for this phone
	 * Ask user for a username they wish to use
	 * Store that username/phone combo on server, save to sharedPref and PersGlobals
	 */
	private void promptUserForUsername(final Context c){
		AlertDialog.Builder userNamePrompt = new AlertDialog.Builder(this);
		userNamePrompt.create();
		userNamePrompt.setTitle("Please enter a User Name");
		
		final EditText input = new EditText(this);
		userNamePrompt.setView(input);
		userNamePrompt.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editable name = input.getText();
				String username = name.toString();

				TelephonyManager tm = (TelephonyManager) 
				    getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
				String phoneNumber = tm.getLine1Number();
				
				AsyncTask<String, Void, Void> putPhoneToName = new PersBogglePutKeyValToServer();
				putPhoneToName.execute(phoneNumber, username);
				PersGlobals.getGlobals().setUsername(username);
				
				AsyncTask<String, Void, Void> putNameToPhone = new PersBogglePutKeyValToServer();
				putNameToPhone.execute(username, phoneNumber);
				
				AsyncTask<String, Void, Void> addUsernameToArray = new addToArrayOfUsersOnServer(c);
				addUsernameToArray.execute(username);
					
				PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
				spref.putString(c, "username", username);
				//Log.d("promptUserForUsername", "saved username to sPref and Globals" + username);
				
				//Start a new game now that we have a username
				Intent challengeUser = new Intent(c, PersBoggleChallengeUser.class);
				startActivity(challengeUser);
					
			}
		});
		userNamePrompt.show();
	}
	
	private boolean checkUserName(){
		if (PersGlobals.getGlobals().getUsername() == null){
			promptUserForUsername(this);
			return false;
		}
		return true;
	}
	
    
}
/**
 * Takes a phone number string as input and gets the username from the server for that number
 * will save this username to the PersGlobals.username if a username was found
 * 
 * @author Mike
 *
 */
class getUsernameFromServer extends AsyncTask<String, Void, String>{
	final Context c;
	getUsernameFromServer(Context c){
		this.c = c;
	}
	
	@Override
	protected String doInBackground(String... params) {
		String phoneNumber = params[0];
		if (canAccessServer() && phoneNumber != null){
			String username = KeyValueAPI.get(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
					phoneNumber);
			if (username != null && username != "") {
				PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
				if (c != null){
					spref.putString(c, "username", username);
				}
				PersGlobals.getGlobals().setUsername(username);
				//Log.d("getUsername Server", "just got username from server: " + username);
			}
			else{
				//Log.d("getUsername Server", "no username saved to server");
			}
		}
		return null;
	}
	
	private boolean canAccessServer() {
    	return PersGlobals.getGlobals().canAccessNetwok(c) && KeyValueAPI.isServerAvailable();
	}
	
	
}

/**
 * adds username to the ArrayList<String> on the server representing usernames
 * @author Mike
 *
 */
class addToArrayOfUsersOnServer extends AsyncTask<String, Void, Void>{
	final Context c;
	addToArrayOfUsersOnServer(Context c){
		this.c = c;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		String username = params[0];
		if(PersGlobals.getGlobals().canAccessNetwok(c) && KeyValueAPI.isServerAvailable()){
			PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
			Gson gson = new Gson();
			Type type = new TypeToken<ArrayList<String>>(){}.getType();
			
			String jsonFromServer = KeyValueAPI.get("allenmic", "allenmic", "usernames");
			Log.d("addToarray", "json array on server is " + jsonFromServer);					
			if (jsonFromServer == null || jsonFromServer == ""){ //first user to be added
			//	Log.d("", "here");
				ArrayList<String> newArray = new ArrayList<String>();
				PersGlobals.getGlobals().setOtherUsers(newArray);
				
				addToArrayAndPutOnServer(newArray, username);
			}
			else{
				ArrayList<String> oldArray = gson.fromJson(jsonFromServer, type);
				
				
				//only add our username if it isn't there already
				if (!oldArray.contains(username)){
					PersGlobals.getGlobals().setOtherUsers(oldArray);
					addToArrayAndPutOnServer(oldArray, username);
				}
				else{//don't want ourselves in the otherUsers array
					//oldArray.remove(username);
					if (c != null){
						String sprefJson = gson.toJson(oldArray);
						spref.putString(c, "usernames", sprefJson);
					}
					PersGlobals.getGlobals().setOtherUsers(oldArray);
				}
				
			}
		}
		
		return null;
	}
	
	private void addToArrayAndPutOnServer(ArrayList<String> oldArray, String username){
		Gson gson = new Gson();
		
		oldArray.add(username);
		
		
		Type type = new TypeToken<ArrayList<String>>(){}.getType();
		String arrayJson = gson.toJson(oldArray, type);
		
		//Log.d("addToArrayAndPutOnServer", "new array is ::: " + arrayJson);
		
		KeyValueAPI.put("allenmic", "allenmic", "usernames", arrayJson);
	}
	
}
