package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;
import edu.neu.mobileclass.apis.KeyValueAPI;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;

import edu.neu.madcourse.michaelallen.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class PersBoggleScoreScreen extends Activity implements OnClickListener{
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.pers_boggle_score_screen);
		
		TextView scoreText = (TextView) findViewById(R.id.pers_boggle_score_textview);
		String text = "Your score was " + PersGlobals.getGlobals().getScore();
		scoreText.setText(text);
		
		TextView wordsFound = (TextView) findViewById(R.id.pers_boggle_score_screen_words);
		TextView opponentWordsFound = (TextView) findViewById(R.id.pers_boggle_score_screen_opponent_words);
		
		String wordsFoundText = "";
		String opponentWordsFoundText = "";
		
		ArrayList<String> foundWords = PersGlobals.getGlobals().getUserPriorWords();
		for (int i = 0; i < foundWords.size(); i++){
			wordsFoundText = wordsFoundText + " " + foundWords.get(i);
		}
		wordsFound.setText("You found the following words: " + wordsFoundText);
		if (PersGlobals.getGlobals().getOpponentPriorWordString() != ""){
			opponentWordsFound.setText(PersGlobals.getGlobals().getOpponentPriorWordString());
		}
		
		if (PersGlobals.getGlobals().getLeader()){
			saveScoreIfHigh(PersGlobals.getGlobals().getScore(), PersGlobals.getGlobals().getOpponentScore());
		}
		else{
			saveScoreIfHigh(-1, -1); 
		}
		
		View mainScreen = findViewById(R.id.pers_boggle_main_screen_button);
		mainScreen.setOnClickListener(this);
		
		if (PersGlobals.getGlobals().getStatus().equals("async")){
			sendGCMToASyncOpponent();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.pers_boggle_main_screen_button:
			finish();
		}
		
	}
	
	public void saveScoreIfHigh(int score, int oppScore){		
		ConnectivityManager connectivityManager 
        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null){
			AsyncTask<Integer, Void, String> updateHighScores = new PersBoggleGetHighScoresAndUpdate(this);
			if (PersGlobals.getGlobals().getStatus().equals("sync")){
				updateHighScores.execute(score, oppScore);
			}
			else{
				updateHighScores.execute(score, -1);
			}
			
		}
		else{
			//TODO what to do if no internet connection when saving score?
		}
		
		
	}
	
	private void showDialogToUser(int i){
		AlertDialog.Builder IdDialogBuilder = new AlertDialog.Builder(this);
		IdDialogBuilder.create();
		i++;
		IdDialogBuilder.setMessage("You got the #" + i + " position on the High Score list!");
		IdDialogBuilder.setPositiveButton("Cool", new DialogInterface.OnClickListener(){
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences spr = getSharedPreferences(PersGlobals.getGlobals().getHighScorePrefName(), 0);
				Gson gson = new Gson();

				String oldHS = spr.getString("pers_highscores", null);
				Log.d("tst", oldHS + " ");
			}
			
		});
		IdDialogBuilder.show();
	}
	
	private void sendGCMToASyncOpponent() {
		Gson gson = new Gson();
		String regId = getIntent().getStringExtra("regId");
		String oppRegId = getIntent().getStringExtra("oppRegId");
		int score = PersGlobals.getGlobals().getScore();
		int opponentScore = PersGlobals.getGlobals().getOpponentScore();
		String scoreJson = gson.toJson(score);
		String opponentScoreJson = gson.toJson(opponentScore);
		
		GCMServlet serv = new GCMServlet();
        Builder mesBuilder = new Message.Builder();
        mesBuilder.addData("regId", oppRegId);
        mesBuilder.addData("oppRegId", regId);
        mesBuilder.addData("opponent", PersGlobals.getGlobals().getUsername());
        mesBuilder.addData("username", PersGlobals.getGlobals().getOpponent());
        mesBuilder.addData("opponentScore", scoreJson);
        mesBuilder.addData("score", opponentScoreJson);
        mesBuilder.addData("type", "asyncUpdate");
        
        CharSequence toastText = "";
        if (isNetworkAvailable()){
    		toastText = "Notifying " + PersGlobals.getGlobals().getOpponent()
    				+ " that it is their turn...";
        }
        else{
        	toastText = "Can't access network. Will attempt to notify "
        			+ PersGlobals.getGlobals().getOpponent() + " later.";
        }
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        
        serv.sendAsyncMessage(mesBuilder.build(), oppRegId);
	}
	
	private boolean isNetworkAvailable(){
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
