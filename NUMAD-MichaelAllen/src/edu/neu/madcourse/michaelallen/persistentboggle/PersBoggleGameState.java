package edu.neu.madcourse.michaelallen.persistentboggle;


import java.util.ArrayList;
import java.util.Date;

import android.graphics.Rect;

import com.google.gson.annotations.*;

public class PersBoggleGameState{
	
	/**
	 * gameStatus: SYNC, ASYNC, OVER
	 */
	@Expose
	public String gameStatus; 
	
	@Expose
	public int gameVersion;
	
	@Expose
	public boolean isPaused = false;
	
	@Expose
	public int score;
	
	@Expose
	public long timerVal;
	
	@Expose 
	public int[][] blockSelection = new int[5][5];
	
	@Expose
	public ArrayList<String> priorChosenWords;
	
	@Expose 
	public String[][] boardLetters;
	
	@Expose
	public String foundWords = "";
	
	@Expose
	public long timeStarted;
	
	
}