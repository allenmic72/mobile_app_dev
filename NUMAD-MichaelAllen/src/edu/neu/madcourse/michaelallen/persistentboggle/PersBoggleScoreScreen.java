package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;
import edu.neu.mobileclass.apis.KeyValueAPI;
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

public class PersBoggleScoreScreen extends Activity implements OnClickListener{
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.pers_boggle_score_screen);
		
		TextView scoreText = (TextView) findViewById(R.id.pers_boggle_score_textview);
		String text = "Your score was " + PersGlobals.getGlobals().getScore();
		scoreText.setText(text);
		
		TextView wordsFound = (TextView) findViewById(R.id.pers_boggle_score_screen_words);
		String wordsFoundText = "";
		ArrayList<String> foundWords = PersGlobals.getGlobals().getPriorWords();
		for (int i = 0; i < foundWords.size(); i++){
			wordsFoundText = wordsFoundText + " " + foundWords.get(i);
		}
		wordsFound.setText("You found the following words: " + wordsFoundText);
		
		
		saveScoreIfHigh(PersGlobals.getGlobals().getScore());
		
		View mainScreen = findViewById(R.id.pers_boggle_main_screen_button);
		mainScreen.setOnClickListener(this);
		
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.pers_boggle_main_screen_button:
			finish();
		}
		
	}
	
	public void saveScoreIfHigh(int score){		
		ConnectivityManager connectivityManager 
        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null){
			AsyncTask<Integer, Void, String> updateHighScores = new PersBoggleGetHighScoresAndUpdate();
			updateHighScores.execute(score);
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
	
}
