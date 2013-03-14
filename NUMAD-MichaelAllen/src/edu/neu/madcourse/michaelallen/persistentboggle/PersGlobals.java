package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;

import edu.neu.madcourse.michaelallen.R;
import edu.neu.madcourse.michaelallen.sudoku.Game;

import android.content.Context;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.CountDownTimer;

public class PersGlobals{
	private PersGlobals(){}
	
	private int numberOfBlocks = 5; //default 5
	
	private String[][] boardLetters = new String[numberOfBlocks][numberOfBlocks];
	
	private int score = 0;
	
	private ArrayList<String> userPriorChosenWords = new ArrayList<String>();
	private ArrayList<String> opponentPriorChosenWords = new ArrayList<String>();
	private ArrayList<String> combinedPriorChosenWords = new ArrayList<String>();
	
	private boolean newGame = false;
	
	private long timerVal = 120;
	
	private CountDownTimer gameTimer;
	
	private boolean isPaused = false;
	
	private SoundPool sp;
	private int dink;
	private int typewriter;
	private int beep;
	
	private final String SHARED_PREF_NAME = "PersBoggleSharedPref";
	private final String HIGH_SCORE_PREF = "HighScore";
	
	private ArrayList<PersBoggleHighScore> hsList = null;
	
	private String username = null;
	private String currentOpponent = null;
	
	private String teamname = "allenmic";
	private String password = "allenmic";
	
	private ArrayList<String> otherUsers = new ArrayList<String>();
	private ArrayList<Rect> otherUserBlocks = new ArrayList<Rect>();
	
	private AsyncTask<String, Void, Void> pollingServer;
	
	private boolean leader = false;
	
	private static class GlobalHolder{
		private static final PersGlobals INSTANCE = new PersGlobals();
	}
	
	public void setTimer(CountDownTimer t){
		gameTimer = t;
		gameTimer.start();
	}
	
	public void clearTimer(){
		gameTimer.cancel();
	}
	
	public void initSoundPool(Context c){
		sp = new SoundPool(3, 3, 0);
		dink = sp.load(c, R.raw.dink, 1);
		typewriter = sp.load(c, R.raw.typewriter, 3);
		beep = sp.load(c, R.raw.bleep, 2);
		
	}
	
	public int getDink(){
		return dink;
	}
	
	public int getTypewriter(){
		return typewriter;
	}
	
	public int getBeep(){
		return beep;
	}
	
	public SoundPool getSP(){
		return sp;
	}
	
	public String getSharedPrefName(){
		return SHARED_PREF_NAME;
	}
	
	public String getHighScorePrefName(){
		return HIGH_SCORE_PREF;
	}
	
	public boolean getIsPaused(){
		return isPaused;
	}
	
	public void switchIsPaused(){
		isPaused = !isPaused;
	}
	
	public void setisPaused(boolean b){
		isPaused = b;
	}
	
	public long getTimerVal(){
		return timerVal;
	}
	
	public void setTimerVal(long val){
		timerVal = val;
	}
	
	public boolean newGameStarted(){
		return newGame;
	}
	
	public void setNewGame(boolean b){
		newGame = b;
	}
	
	public String getBoardLetters(int i, int j){
		return boardLetters[i][j];
	}
	
	public void setBoardLetters(int i, int j, String letter){
		boardLetters[i][j] = letter;
	}
	
	public String[][] getBoard(){
		final String[][] board = boardLetters;
		return board;
	}

	public void setBoard(String[][] board){
		boardLetters = board;
	}
	
	public int getScore(){
		return score;
	}
	
	public void increaseScore(int n){
		score += n;
	}
	
	public void setScore(int n){
		score = n;
	}
	
	public ArrayList<String> getUserPriorWords(){
		final ArrayList<String> words = userPriorChosenWords;
		return words;
	}
	
	public ArrayList<String> getCombinedPriorWords(){
		final ArrayList<String> words = combinedPriorChosenWords;
		return words;
	}
	
	public void setUserPriorWords(ArrayList<String> priors){
		userPriorChosenWords = priors;
		combinedPriorChosenWords = userPriorChosenWords;
		combinedPriorChosenWords.addAll(opponentPriorChosenWords);
		
	}
	
	public void addChosenWord(String word){
		userPriorChosenWords.add(word);
		combinedPriorChosenWords.add(word);
	}
	
	public void addAllChosenWords(ArrayList<String> chosenWords){
		userPriorChosenWords.addAll(chosenWords);
		combinedPriorChosenWords.addAll(chosenWords);
	}
	
	public ArrayList<String> getOpponentPriorWords(){
		final ArrayList<String> words = opponentPriorChosenWords;
		return words;
	}
	
	public void setOpponentPriorWords(ArrayList<String> priors){
		opponentPriorChosenWords = priors;
		combinedPriorChosenWords = opponentPriorChosenWords;
		combinedPriorChosenWords.addAll(userPriorChosenWords);
		
	}
	
	
	
	public void setNumberOfBlocks(int n){
		numberOfBlocks = n;
	}
	
	public int getNumberOfBlocks(){
		return numberOfBlocks;
	}
	
	public void resetAllVariables(){
		boardLetters = new String[numberOfBlocks][numberOfBlocks];
		score = 0;
		userPriorChosenWords = new ArrayList<String>();
		timerVal = 120; 
		gameTimer = null;
		isPaused = false;
	}
	
	public void setHighScoreList(ArrayList<PersBoggleHighScore> hs){
		hsList = hs;
	}
	
	public ArrayList<PersBoggleHighScore> getHighScoreList(){
		return hsList;
	}
	
	public void setUsername(String user){
		username = user;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getTeamName(){
		return teamname;
	}
	
	public String getPassword(){
		return password;
	}
	
	public ArrayList<String> getOtherUsers(){
		return otherUsers;
	}
	
	public void setOtherUsers(ArrayList<String> users){
		otherUsers = users;
	}
	
	public void addToOtherUserBlocks(Rect block){
		otherUserBlocks.add(block);
	}
	
	public ArrayList<Rect> getOtherUserBlocks(){
		return otherUserBlocks;
	}
	
	public void clearOtherUserBlocks(){
		otherUserBlocks.clear();
	}
	
	public void setOpponent(String o){
		currentOpponent = o;
	}
	
	public String getOpponent(){
		return currentOpponent;
	}
	
	public void setPollingTask(AsyncTask<String, Void, Void> task){
		pollingServer = task;
	}
	
	public void cancelPollingTask(){
		if (pollingServer != null){
			pollingServer.cancel(true);
		}
	}
	
	public void setLeader(boolean l){
		leader = l;
	}
	
	public boolean returnLeader(){
		return leader;
	}
	
	public static PersGlobals getGlobals(){
		return GlobalHolder.INSTANCE;
	}
	
	
}