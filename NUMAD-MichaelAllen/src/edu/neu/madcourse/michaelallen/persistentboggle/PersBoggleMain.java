package edu.neu.madcourse.michaelallen.persistentboggle;

import com.google.android.gcm.server.Message;
import com.google.gson.Gson;

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
        View newGameButton = findViewById(R.id.pers_boggle_new_game_button);
        newGameButton.setOnClickListener(this);
        
        View acknowledgementsButton = findViewById(R.id.pers_boggle_acknowledgements_button);
        acknowledgementsButton.setOnClickListener(this);
        
        continueButton = findViewById(R.id.pers_boggle_continue_button);
        continueButton.setOnClickListener(this);
        
        View highScoresButton = findViewById(R.id.pers_boggle_high_scores);
        highScoresButton.setOnClickListener(this);
       
        View boggleRulesButton = findViewById(R.id.pers_boggle_rules_button);
        boggleRulesButton.setOnClickListener(this);
        
        if (canAccessNetwork()){
            AsyncTask<Integer, Void, String> setHSList = new PersBoggleGetHighScoresAndUpdate();
            setHSList.execute(-1);
        }
        
        getUsername();
        
        GCMServlet serv = new GCMServlet();
        Message mes = new Message.Builder().build();
        serv.sendMessage(mes, "9788883064");

    }
    
    @Override
    protected void onResume() {
       super.onResume();
       
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
	public void onClick(View v) {
		 switch (v.getId()) {
		 case R.id.pers_boggle_exit_button:
			 finish();
			 break;
		 case R.id.pers_boggle_new_game_button:
			 if (checkUserName()){
				 Intent boggleGame = new Intent(this, PersBoggleGame.class);
				 startActivity(boggleGame);
				 PersGlobals.getGlobals().setNewGame(true);
				 Log.d("username", "starting game with saved name: " + PersGlobals.getGlobals().getUsername());
			 }
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
	
	private String getUsernameFromSharedPref(){
		return new PersBoggleSharedPrefAPI().getString(this, "username");
	}
	
	
	private void getUsername(){
		String username = getUsernameFromSharedPref();
		if (username != null){
			PersGlobals.getGlobals().setUsername(username);
			Log.d("MainActivity getUsername", "username is " + username);
		}
		else{
			Log.d("MainActivity getUsername", "no username in shared pref, checking server");
			if (canAccessNetwork()){
				TelephonyManager tm = (TelephonyManager) 
					    getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
				String phoneNumber = tm.getLine1Number();
				
	            AsyncTask<String, Void, String> usernameFromServer = new getUsernameFromServer();
	            usernameFromServer.execute(phoneNumber);
	            //TODO 
	        }
		}
	}
	
	/**
	 * No username in sharedPref or in the server for this phone
	 * Ask user for a username they wish to use
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
				
				AsyncTask<String, Void, Void> putName = new PersBogglePutKeyValToServer();
				putName.execute(phoneNumber, username);
				PersGlobals.getGlobals().setUsername(username);
				
					
				PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
				spref.putString(c, "username", username);
				Log.d("promptUserForUsername", "saved username to sPref and Globals" + username);
				
				//Start a new game now that we have a username
				Intent boggleGame = new Intent(c, PersBoggleGame.class);
				startActivity(boggleGame);
				PersGlobals.getGlobals().setNewGame(true);
					
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

	@Override
	protected String doInBackground(String... params) {
		String phoneNumber = params[0];
		
		if (canAccessServer() && phoneNumber != null){
			String username = KeyValueAPI.get(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
					phoneNumber);
			if (username != null && username != "") {
				PersGlobals.getGlobals().setUsername(username);
				Log.d("getUsername Server", "just got username from server: " + username);
			}
			else{
				Log.d("getUsername Server", "no username saved to server");
			}
		}
		return null;
	}
	
	private boolean canAccessServer() {
    	return KeyValueAPI.isServerAvailable();
	}
	
	
}
