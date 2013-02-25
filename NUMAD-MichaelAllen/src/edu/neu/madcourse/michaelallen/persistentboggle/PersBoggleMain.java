package edu.neu.madcourse.michaelallen.persistentboggle;

import edu.neu.madcourse.michaelallen.R;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
        
        if (canAccessServer()){
            AsyncTask<Integer, Void, String> setHSList = new PersBoggleGetHighScoresAndUpdate();
            setHSList.execute(-1);
        }
        
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
			 Intent boggleGame = new Intent(this, PersBoggleGame.class);
			 startActivity(boggleGame);
			 PersGlobals.getGlobals().setNewGame(true);
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
			 Intent boggleHS = new Intent(this, PersBoggleHS.class);
			 startActivity(boggleHS);
			 break;
		 case R.id.pers_boggle_rules_button:
			 Intent boggleRules = new Intent(this, PersBoggleRules.class);
			 startActivity(boggleRules);
			 break;
		 }
		
	}
	
	private boolean canAccessServer() {
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