package edu.neu.madcourse.michaelallen.persistentboggle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.michaelallen.R;
import edu.neu.madcourse.michaelallen.boggle.Globals;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.graphics.Rect;

public class PersBoggleGame extends Activity implements OnClickListener{
	
	
	String opponent;
	boolean leader;
	String status;
	String username;
	String regId; //regId of user, for async GCMs
	String oppRegId; //regId of opponent
	private final String TAG = "PersBoggleGame";
	int myVersion;
	Handler handler;
	TextView userWords;
	TextView opponentWords;
	int opponentVersion;
	AsyncTask<String, Integer, String> pollingServer;
	boolean quit = false;
	boolean over = false;
	long timeStarted = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(PersGlobals.getGlobals().getUsername() == ""){
			PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
			PersGlobals.getGlobals().setUsername(spref.getString(this, "username"));
		}
		opponentVersion = 0;
		
		opponent = getIntent().getStringExtra("opponent");
		PersGlobals.getGlobals().setOpponent(opponent);
		
		leader = getIntent().getBooleanExtra("leader", false);
		PersGlobals.getGlobals().setLeader(leader);
		
		status = getIntent().getStringExtra("status");
		PersGlobals.getGlobals().setStatus(status);
		
		username = getIntent().getStringExtra("username");
		if (username != null && username != ""){
			PersGlobals.getGlobals().setUsername(username);
		}
		else{
			username = PersGlobals.getGlobals().getUsername();
		}
		
		myVersion = 0;
		
		setContentView(R.layout.pers_boggle_game);
		
		View quitGame = findViewById(R.id.pers_boggle_game_quit);
		quitGame.setOnClickListener(this);
		
		Button pausedResume = (Button) findViewById(R.id.pers_boggle_game_pause);
		pausedResume.setOnClickListener(this);		
		
		
		final String RESUME_GAME = "edu.neu.madcourse.michaelallen.persistentboggle.resume";
		int resumed = getIntent().getIntExtra(RESUME_GAME, 0);		
		
		TextView userText = (TextView) findViewById(R.id.pers_boggle_game_user);
		TextView opponentText = (TextView) findViewById(R.id.pers_boggle_game_opponent);
		
		userText.setText(PersGlobals.getGlobals().getUsername());
		opponentText.setText(opponent);
		
		userWords = (TextView) findViewById(R.id.pers_boggle_game_user_words);
		opponentWords = (TextView) findViewById(R.id.pers_boggle_game_opponent_words);
		
		userWords.setText(username + " found: ");
		opponentWords.setText(opponent + " found: ");
		
		regId = getIntent().getStringExtra("regId");
		oppRegId = getIntent().getStringExtra("oppRegId");
		
		Gson gson = new Gson();
		
		if (status.equals("async")){
			PersGlobals.getGlobals().resetAllVariables();
			int score = getIntent().getIntExtra("score", 0);
			int opponentScore = getIntent().getIntExtra("opponentScore", 0);
			PersGlobals.getGlobals().setScore(score);
			PersGlobals.getGlobals().setOpponentScore(opponentScore);
			setCurrentScore(score);
			setOpponentScore(opponentScore);
			populateBoardWithLetters();
			opponentWords.setEnabled(false);
			opponentWords.setVisibility(View.INVISIBLE);
			
		}
		/*//TODO change this?
		else if (resumed == 1){
			putSharedPreferences();
			if (PersGlobals.getGlobals().getIsPaused()){
				pausedResume.setText(R.string.boggle_resume_text);
			}
			else{
				pausedResume.setText(R.string.boggle_pause_text);
			}
			TextView timerTextView = (TextView) findViewById(R.id.pers_boggle_game_timer);
			int min = (int) (PersGlobals.getGlobals().getTimerVal() / 60);
	    	int sec = (int) (PersGlobals.getGlobals().getTimerVal() % 60);
			timerTextView.setText("" + min + ":" + sec);
		}*/
		else if (leader){ //sync
			//Log.d(TAG, "starting game as leader");
			PersGlobals.getGlobals().resetAllVariables();
			populateBoardWithLetters();
			PersBoggleGameState state = new PersBoggleGameState();
			state.gameStatus = status;
			state.timerVal = PersGlobals.getGlobals().getTimerVal();
			state.boardLetters = PersGlobals.getGlobals().getBoard();
			timeStarted = Calendar.getInstance().getTime().getTime();
			//Log.e(TAG, "time started: " + timeStarted);
			state.timeStarted = timeStarted;
			
			String json = gson.toJson(state);
			PersBogglePutKeyValToServer publishGameToOpponent = new PersBogglePutKeyValToServer();
			publishGameToOpponent.execute(PersGlobals.getGlobals().getUsername() + opponent, json);
			
		}
		else{ //sync not leader
			String state = getIntent().getStringExtra("state");
			PersBoggleGameState gameState = gson.fromJson(state, PersBoggleGameState.class);
			PersGlobals.getGlobals().resetAllVariables();
			
			
			if (gameState.priorChosenWords != null && !gameState.priorChosenWords.isEmpty()){
				PersGlobals.getGlobals().setOpponentPriorWords(gameState.priorChosenWords);
			}
			PersGlobals.getGlobals().setBoard(gameState.boardLetters);
			PersGlobals.getGlobals().setStatus(gameState.gameStatus);
			PersGlobals.getGlobals().setTimerVal(gameState.timerVal);
			//Log.d(TAG, "Starting " + gameState.gameStatus + " game against " + opponent + " with state: " + state);
			timeStarted = gameState.timeStarted;
			Log.e(TAG, "not leader time started: " + timeStarted);
			
		}
		/*
		int[][] test = new int[5][5];
		test[1][1] = 1;
		test[1][2] = 1;
		PersBoggleGameState state = new PersBoggleGameState();
		state.goodSelection = test;
		state.gameVersion = 10;
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String json = gson.toJson(state);
		
		new PersBogglePutKeyValToServer().execute(opponent + PersGlobals.getGlobals().getUsername(), json);
		*/
		
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_game_quit:	
			quit = true;
			finish();
			break;
		case R.id.pers_boggle_game_pause:
			switchPausedOrResumed(true);
		}
		
	}

    @Override
    protected void onResume() {
       super.onResume();
       PersGlobals.getGlobals().initSoundPool(this);
       if(!PersGlobals.getGlobals().getIsPaused()){
    	    if (status.equals("sync")){
	   			//Log.d(TAG, "here in onresume");
	   			if (timeStarted != 0){
	   				syncGame(timeStarted);
	   				timeStarted = 0;
	   			}
	   			else{
	   				packageGameStateAndPublish("", "resume");
	   			}
	   			PersGlobals.getGlobals().setTimer(makeTimer(PersGlobals.getGlobals().getTimerVal()));
	   			startPollingServer(PersGlobals.getGlobals().getOpponent() + PersGlobals.getGlobals().getUsername(), this);
   			}
    	    else{
    	    	PersGlobals.getGlobals().setTimer(makeTimer(PersGlobals.getGlobals().getTimerVal()));
    	    }
       }
       //start polling server looking at the key <opponentUsername>+<myUsername>
       if (opponent != null){
    	   //Log.d("Challenged game", "starting game against " + opponent);
       }
       
    }

    @Override
    protected void onPause() {
       super.onPause();
       PersGlobals.getGlobals().getSP().release();
       if (quit){
    	   PersGlobals.getGlobals().clearTimer();
    	   try{
    		   pollingServer.cancel(true);
    	   }
    	   catch(Exception E){
    		   
    	   }
       }
       else if(!PersGlobals.getGlobals().getIsPaused()){
    	   PersGlobals.getGlobals().clearTimer();
    	   if (status.equals("sync")){
    		   if (!over){
    			   packageGameStateAndPublish("", "pause");
        		   pollingServer.cancel(true);
    		   }
   		}
       }
       
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	
    	//saveSharedPreferences();
    	
    	if (status.equals("sync")){
    		if (!over){
    			packageGameStateAndPublish("", "async");
    		}
    		pollingServer.cancel(true);
    	}    	
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    }

	public void setBoardLetter(int i, int j, String x){
		PersGlobals.getGlobals().setBoardLetters(i, j,  x);
	}
	public String getBoardLetter(int i, int j){
		if(i > PersGlobals.getGlobals().getNumberOfBlocks() - 1){
			i = PersGlobals.getGlobals().getNumberOfBlocks() - 1;
		}
		if (j > PersGlobals.getGlobals().getNumberOfBlocks() - 1){
			j = PersGlobals.getGlobals().getNumberOfBlocks() - 1;
		}
		return PersGlobals.getGlobals().getBoardLetters(i, j);
	}
	public void addToSelectedLetterTextView(String letter){
		TextView SelectedLetters = (TextView) findViewById(R.id.pers_boggle_game_selected);
		String currentText = SelectedLetters.getText().toString();
		currentText = currentText + letter;
		SelectedLetters.setText(currentText);
	}
	public void clearSelectedLetterTextView(){
		TextView SelectedLetters = (TextView) findViewById(R.id.pers_boggle_game_selected);
		SelectedLetters.setText("");
	}
	
	public boolean checkWordAndRewardUser(String selectedLetters){
		if (selectedLetters.length() < 3){
			return false;
		}
		
		String selectedWord = selectedLetters;
		
		selectedWord = selectedWord.toLowerCase();
		if (selectedWordAlreadyChosenPrior(selectedWord)){
			return false;
		}
		
		String loc = getDictionaryLocationForWord(selectedWord);
		boolean match;
		match = matchForWordInGivenFile(selectedWord, loc);

		if (match){
			successfulWord(selectedWord);
			int dink = PersGlobals.getGlobals().getDink();
			PersGlobals.getGlobals().getSP().play(dink, 1, 1, 1, 0, 1);
			return true;
		}
			
		
		return false;
		
		
		
	}
	
	/**
	 * the user has selected a sequence of letters, so package the game state and publish to the server
	 * the opponent will then update their game with the necessary information
	 * and animate their board with the user's selection
	 * @chosenWords: newly selected word
	 * @type: "pause" for a game pause, "resume to resume, "async" to switch to async, else ""
	 */
	private void packageGameStateAndPublish(String chosenWords, String type) {
	
		PersBoggleGameState state = new PersBoggleGameState();
		
		state.score = PersGlobals.getGlobals().getScore();
		state.boardLetters = PersGlobals.getGlobals().getBoard();
		
		if (type.equals("async")){
			state.gameStatus = "async";
			//Log.d(TAG, "putting game with async status to server");
		}
		else if (type.equals("pause")){
			state.isPaused = true;
			state.foundWords = null;
			state.gameStatus = "sync";
		}
		else if (type.equals("resume")){
			state.isPaused = false;
			state.foundWords = null;
			state.gameStatus = "sync";
		}
		else{
			state.priorChosenWords = PersGlobals.getGlobals().getUserPriorWords();
			state.gameStatus = "sync";
			state.foundWords = chosenWords;
			//Log.d(TAG, "putting sync game to server");
		}	
		
		myVersion++;
		state.gameVersion = myVersion;
		publishChangesToServer(state);
	
		
	}
	
	/**
	 * Publishes a new PersBoggleGameState to the server
	 * puts to key "username" + "opponent"
	 */
	private void publishChangesToServer(PersBoggleGameState gameState){
		//Log.d(TAG, "Combined prior words: " + PersGlobals.getGlobals().getCombinedPriorWords());
		new AsyncTask<PersBoggleGameState, Void, Void>(){

			@Override
			protected Void doInBackground(PersBoggleGameState... params) {
				PersBoggleGameState gameState = params[0];
				if(KeyValueAPI.isServerAvailable()){
					Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					String json = gson.toJson(gameState);
					KeyValueAPI.put("allenmic", "allenmic", 
							PersGlobals.getGlobals().getUsername() +
							PersGlobals.getGlobals().getOpponent(), json);
					//Log.d(TAG, "putting new game state to " + PersGlobals.getGlobals().getUsername() +
						//	PersGlobals.getGlobals().getOpponent());
				}
				return null;
			}
			
		}.execute(gameState);
	}
	
	/**
	 * toggle game to paused or to resumed
	 * @param self if true, the user paused. For sync game cancel polling task
	 * 				if false, the opponent paused. For sync keep polling to check for resume
	 */
	private void switchPausedOrResumed(boolean self){
		Button pausedResume = (Button) findViewById(R.id.pers_boggle_game_pause);
		TextView pauseOverlay = (TextView) findViewById(R.id.pers_boggle_game_paused_overlay);
		
		PersGlobals.getGlobals().switchIsPaused();
		
		if (PersGlobals.getGlobals().getIsPaused()){
			pausedResume.setText(R.string.boggle_resume_text);
			PersGlobals.getGlobals().clearTimer();
			pauseOverlay.setVisibility(View.VISIBLE);
			if (status.equals("sync")){
				if (self){
					packageGameStateAndPublish("", "pause");
					pollingServer.cancel(true);
				}
				else{
					pauseOverlay.setText(pauseOverlay.getText() + " By " + PersGlobals.getGlobals().getOpponent());
					pausedResume.setVisibility(View.INVISIBLE);
					pausedResume.setEnabled(false);
				}
				
			}
			else{
				pausedResume.setVisibility(View.VISIBLE);
				pausedResume.setEnabled(true);
			}
		}
		else{
			pausedResume.setText(R.string.boggle_pause_text);
			PersGlobals.getGlobals().setTimer(makeTimer(PersGlobals.getGlobals().getTimerVal()));
			pauseOverlay.setVisibility(View.INVISIBLE);
			if (status.equals("sync")){
				if (self){
					packageGameStateAndPublish("", "resume");
					startPollingServer(PersGlobals.getGlobals().getOpponent() + PersGlobals.getGlobals().getUsername(), this);
				}
				else{
					pauseOverlay.setText(R.string.pers_boggle_game_paused_overlay);
					pausedResume.setVisibility(View.VISIBLE);
					pausedResume.setEnabled(true);
				}
				
			}
			else{
				pausedResume.setVisibility(View.VISIBLE);
				pausedResume.setEnabled(true);
			}
		}
		
		
	}
	
	private void saveSharedPreferences(){
    	SharedPreferences sPref = getSharedPreferences(PersGlobals.getGlobals().getSharedPrefName(), 0);
    	Editor e = sPref.edit();
    	
    	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    	
    	PersSharedPrefHolder sph = new PersSharedPrefHolder();
    	sph.boardLetters = PersGlobals.getGlobals().getBoard();
    	sph.isPaused = PersGlobals.getGlobals().getIsPaused();
    	sph.newGame = PersGlobals.getGlobals().newGameStarted();
    	sph.userPriorChosenWords = PersGlobals.getGlobals().getUserPriorWords();
    	sph.opponentPriorChosenWords = PersGlobals.getGlobals().getOpponentPriorWords();
    	sph.score = PersGlobals.getGlobals().getScore();
    	sph.timerVal = PersGlobals.getGlobals().getTimerVal();
    	
    	String sphJson = gson.toJson(sph);
    	
    	
    	e.putString("persboggle", sphJson);
    	e.commit();
    	
	}
	
	private void setCurrentScore(int n){
		TextView CurrentScore = (TextView) findViewById(R.id.pers_boggle_game_currentscore);
		CurrentScore.setText("" + n);
	}
	
	private void setOpponentScore(int n){
		TextView opponentScore = (TextView) findViewById(R.id.pers_boggle_game_opponentscore);
		opponentScore.setText("" + n);
		PersGlobals.getGlobals().setOpponentScore(n);
	}
	
	private void putSharedPreferences(){
		SharedPreferences sPref = getSharedPreferences("PersBoggleSharedPref", 0);
    	
    	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    	
    	String json = sPref.getString("persboggle", null);
    	if (json != null){
    		PersSharedPrefHolder sph = gson.fromJson(json, PersSharedPrefHolder.class);
    		
    		PersGlobals.getGlobals().setBoard(sph.boardLetters);
    		PersGlobals.getGlobals().setisPaused(sph.isPaused);
    		PersGlobals.getGlobals().setNewGame(sph.newGame);
    		PersGlobals.getGlobals().setUserPriorWords(sph.userPriorChosenWords);
    		PersGlobals.getGlobals().setOpponentPriorWords(sph.opponentPriorChosenWords);
    		PersGlobals.getGlobals().setScore(sph.score);
    		PersGlobals.getGlobals().setTimerVal(sph.timerVal);
    		
    		
    		setCurrentScore(PersGlobals.getGlobals().getScore());
    	}
    	
    	
    	
    	
	}
	
	private CountDownTimer makeTimer(long sec){
		CountDownTimer t =  new CountDownTimer(sec * 1000, 1000) {

			TextView timerTextView = (TextView) findViewById(R.id.pers_boggle_game_timer);
			int beep = PersGlobals.getGlobals().getBeep();
			
		     public void onTick(long millisUntilFinished) {
		    	 int SecondsUntilFinished = (int) millisUntilFinished / 1000;
		    	 int min = SecondsUntilFinished / 60;
		    	 int sec = SecondsUntilFinished % 60;
		    	 
		    	 if (sec < 10) {
		    		 timerTextView.setText(min + ":" + "0" + sec);
		    		 if (min < 1){
		    			 PersGlobals.getGlobals().getSP().play(beep, 1, 1, 1, 0, 1);
		    		 }
		    	 }
		    	 
		    	 else {
		    		 timerTextView.setText(min + ":" + sec);
		    	 }
		    	 PersGlobals.getGlobals().setTimerVal(millisUntilFinished / 1000);
		     }

		     public void onFinish() {
		    	 timerTextView.setText("0:00");
		         timerHitZero();
		     }
		  };
		  return t;
	}
	
	/**
	 * Since my wordlist is split into files starting with "_" 
	 * 		+ the first three letters of the words within the file,
	 * 		this will be the file location to check in the assets folder
	 */
	private String getDictionaryLocationForWord(String selectedWord){
		String loc = "words/" + "_" + selectedWord.substring(0, 3) + ".txt";
		return loc;
	}
	
	private boolean selectedWordAlreadyChosenPrior(String selectedWord){
		
		ArrayList<String> priorWords = PersGlobals.getGlobals().getCombinedPriorWords();
		return priorWords.contains(selectedWord);
	}
	
	/**
	 * Matches a substring of the selectedWord, which has the first three characters removed,
	 *		to the strings wihin the fileLoc dictionary file
	 * Since all strings in fileLoc are in the same format with the first three characters
	 * being contained within the fileLoc name
	 */
	private boolean matchForWordInGivenFile(String selectedWord, String fileLoc) {
		
		InputStream stream;
		try {
			stream = getAssets().open(fileLoc);
			String suffix = selectedWord.substring(3);
			
			InputStreamReader reader = new InputStreamReader(stream);
			BufferedReader buf = new BufferedReader(reader, 8192);
			
			String nextLine = buf.readLine();
			while (nextLine != null){
				if (suffix.equals(nextLine)){
					stream.close();
					return true;
				}
				nextLine = buf.readLine();
			} 
			
			
			stream.close();
			return false;
		
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * increases the points the user has scored for the game
	 * based on the size of word:
	 * 		word length = 3 or 4 ---> 1 point
	 * 		word length > 4 ---> 1 point + length of word over 4 characters
	 * adds the successful word to the priorWords global array
	 * clears the selectedLetters text view's text
	 */
	private void successfulWord(String word){
		int wordSize = word.length();
		int points = 1;
		wordSize -= 4;
		if (wordSize > 0){
			points = points + wordSize;
		}
		
		
		
		PersGlobals.getGlobals().increaseScore(points);
		int totalScore = PersGlobals.getGlobals().getScore();
		setCurrentScore(totalScore);
		
		PersGlobals.getGlobals().addChosenWord(word);
		
		String userWordString = (String) userWords.getText();
		userWordString += word + ", ";
		userWords.setText(userWordString);
		
		if (status.equals("sync")){
			packageGameStateAndPublish(userWordString, "");
		}
		
		clearSelectedLetterTextView();
		
		
	}
	
	private void populateBoardWithLetters(){
		ArrayList<String> pickedLetters = new ArrayList<String>();
		
		for (int i = 0; i < PersGlobals.getGlobals().getNumberOfBlocks(); i++){
			for (int j = 0; j < PersGlobals.getGlobals().getNumberOfBlocks(); j++){
				String rndLetter = getWeightedRandomLetter();
				while (!isLetterOkayToUseOnBoard(pickedLetters, rndLetter)){
					rndLetter = getWeightedRandomLetter();
				}
				
				
				setBoardLetter(i, j, rndLetter);
				pickedLetters.add(rndLetter);
			}
		}
	}
	
	private void timerHitZero(){
		openScoreScreenDialog();
	}
	
	private void openScoreScreenDialog(){
		PersGlobals.getGlobals().setNewGame(false);
		AlertDialog.Builder scoreDialog = new AlertDialog.Builder(this);
		scoreDialog.create();
		scoreDialog.setMessage("Time's up!");
		scoreDialog.setPositiveButton("View Score", new DialogInterface.OnClickListener(){
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				openScoreScreen();
			}
			
		});
		scoreDialog.setIcon(R.drawable.ic_launcher);
		scoreDialog.setInverseBackgroundForced(true);
		scoreDialog.show();
	}
	
	private void openScoreScreen(){
		Intent scoreScreen = new Intent(this, PersBoggleScoreScreen.class);
		if (status.equals("async")){
			scoreScreen.putExtra("regId", regId);
			scoreScreen.putExtra("oppRegId", oppRegId);
			//Log.d(TAG, "going to score screen passing it the oppRegId: " + oppRegId);
		}
		over = true;
		startActivity(scoreScreen);
		finish();
	}
	private boolean isLetterOkayToUseOnBoard(ArrayList<String> letters, String x){
		int count = 0;
		
		for (int i = 0; i < letters.size(); i++){
			if (letters.get(i) == x){
				count++;
			}
		}
		
		if (count < 3){
			return true;
		}
		else {
			return false;
		}
	}
	
	private String getWeightedRandomLetter(){
		String letter = "A";
		
		Random rng = new Random();
		int randInt = rng.nextInt(65);
		
		if (randInt < 3)
			return "A";
		if (2 < randInt && randInt < 5)
			return "B";
		if (4 < randInt && randInt < 7)
			return "C";
		if (6 < randInt && randInt < 9)
			return "D";
		if (8 < randInt && randInt < 12)
			return "E";
		if (11 < randInt && randInt < 14)
			return "F";
		if (13 < randInt && randInt < 16)
			return "G";
		if (15 < randInt && randInt < 18)
			return "H";
		if (17 < randInt && randInt < 21)
			return "I";
		if (20 < randInt && randInt < 22)
			return "J";
		if (21 < randInt && randInt < 23)
			return "K";
		if (22 < randInt && randInt < 25)
			return "L";
		if (24 < randInt && randInt < 27)
			return "M";
		if (26 < randInt && randInt < 29)
			return "N";
		if (28 < randInt && randInt < 34)
			return "O";
		if (33 < randInt && randInt < 34)
			return "P";
		if (33 < randInt && randInt < 35)
			return "Q";
		if (34 < randInt && randInt < 37)
			return "R";
		if (36 < randInt && randInt < 39)
			return "S";
		if (38 < randInt && randInt < 41)
			return "T";
		if (40 < randInt && randInt < 45)
			return "U";
		if (44 < randInt && randInt < 46)
			return "V";
		if (45 < randInt && randInt < 47)
			return "W";
		if (46 < randInt && randInt < 48)
			return "X";
		if (47 < randInt && randInt < 49)
			return "Y";
		if (48 < randInt && randInt < 50)
			return "Z";
		if (49 < randInt && randInt < 54)
			return "E";
		if (53 < randInt && randInt < 57)
			return "I";
		if (56 < randInt && randInt < 60)
			return "A";
		if (59 < randInt && randInt < 63)
			return "U";
		else{
			return letter;
		}
	}
	
	private void setOpponentWords(){
		opponentWords.setText(PersGlobals.getGlobals().getOpponentPriorWordString());
	}
	
	private void showAsyncSwitcherDialog(){
				
		AlertDialog.Builder startingAsyncDialog = new AlertDialog.Builder(this);
		startingAsyncDialog.create();
		startingAsyncDialog.setMessage(PersGlobals.getGlobals().getOpponent() + " has dropped out of the game. "
				+ " Switch to Asynchronous mode or exit?");
		startingAsyncDialog.setPositiveButton("Async", new DialogInterface.OnClickListener(){
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PersGlobals.getGlobals().setStatus("async");
				status = "async";
				switchPausedOrResumed(true);
				leader = true;
				PersGlobals.getGlobals().setLeader(true);
				dialog.cancel();
			}
			
		});
		startingAsyncDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener(){
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				quit = true;
				over = true;
				finish();
			}
			
		});
		startingAsyncDialog.setIcon(R.drawable.ic_launcher);
		startingAsyncDialog.show();
	}
	
	/**
	 * Starts polling and continues until cancelled
	 * checks "opponent" + "username" key
	 * updates certain game state variables based on value gotten,
	 * and animates the board based on new words the opponent has selected
	 * @param key
	 */
	private void startPollingServer(final String key, final PersBoggleGame game){
		
		pollingServer = new AsyncTask<String, Integer, String>(){
			@Override
			protected String doInBackground(String... params) {
				String key = params[0];
				//Log.d("PersBoggleGame", "Starting to poll server for " + key);
				String json;
				PersBoggleGameState opponentGame;
				Gson gson = new Gson();
				//poll server maximum of once every 500ms
				while(true){
					if (isCancelled() == true){
						if(KeyValueAPI.isServerAvailable()){
							//TODO move this somewhere else?
							KeyValueAPI.clearKey("allenmic", "allenmic", key);
						}
						break;
					}
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						
					}
					
					//resetOtherUserBlocks();
					
					if(KeyValueAPI.isServerAvailable()){
						json = KeyValueAPI.get("allenmic", "allenmic", key);
						
						if (json != null && json != ""){
							try{
								opponentGame = gson.fromJson(json, PersBoggleGameState.class);
								if (opponentGame.gameStatus.equals("async")){
									PersGlobals.getGlobals().setStatus("async");
									return "async";
								}
								if(opponentGame.gameVersion > opponentVersion){
									//Log.d(TAG, "got new opponent state: " + json);
									
									if (PersGlobals.getGlobals().getIsPaused() && !opponentGame.isPaused 
										|| !PersGlobals.getGlobals().getIsPaused() && opponentGame.isPaused){
										publishProgress();
									}
									
									if (opponentGame.priorChosenWords != null && !opponentGame.priorChosenWords.isEmpty()){
										PersGlobals.getGlobals().setOpponentPriorWords(opponentGame.priorChosenWords);
									}
									
									if (opponentGame.foundWords != "" && opponentGame.foundWords != null){
										PersGlobals.getGlobals().setOpponentPriorWordString(opponentGame.foundWords);
										//Log.d(TAG, "opponent word string: " + opponentGame.foundWords);
									}
									
									if (opponentGame.score > 0){
										//Log.d(TAG, "opponent score is now " + opponentGame.score);
										publishProgress(opponentGame.score);
										
									}
									
									opponentVersion = opponentGame.gameVersion;
								}
							}
							catch (RuntimeException E){
								Log.e(TAG, "Runtime Exception from polling this: " + json);
							}	
						}
					}
				}
				
				return null;
				
				
			}
			
			
			protected void onProgressUpdate(Integer... progress) {
				if (progress != null && progress.length > 0 && progress[0] != null){
					setOpponentScore(progress[0]);
					setOpponentWords();
				}
				else{
					switchPausedOrResumed(false);
				}
		    }
			
			 protected void onPostExecute(String oppStatus) {
				 if (oppStatus != null && oppStatus.equals("async")){
					 if(!PersGlobals.getGlobals().getIsPaused()){
						 switchPausedOrResumed(true);
					 }
					 showAsyncSwitcherDialog();
				 }
		     }
			
		};
		pollingServer.execute(key);
	}
	
	private void syncGame(long timeStarted){
		Date currentTime = Calendar.getInstance().getTime();
		long timePassed = currentTime.getTime() - timeStarted;
		//Log.d(TAG, "current time: " + currentTime.getTime());
		
		//Log.d(TAG, "time passed: " + timePassed);
		if (timePassed < 5000 && timePassed > 0){
			
			try {
				Thread.sleep(5000 - timePassed);
			} catch (InterruptedException e) {
			}
		}
		//Log.d(TAG, "after thread sleep");
		
	}
	

}
