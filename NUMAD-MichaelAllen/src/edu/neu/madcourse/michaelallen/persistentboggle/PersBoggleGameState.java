package edu.neu.madcourse.michaelallen.persistentboggle;


import java.util.ArrayList;

import android.graphics.Rect;

import com.google.gson.annotations.*;

public class PersBoggleGameState{
	//gameStatus: SYNCH, ASYNCH, OVER
	@Expose
	public String gameStatus; 
	
	@Expose
	public int gameVersion;
	
	@Expose
	public boolean isPaused;
	
	@Expose
	public int score;
	
	@Expose
	public long timerVal;
	
	@Expose 
	public int[][] blockSelection = new int[5][5];
	
	@Expose
	public ArrayList<String> newChosenWords = new ArrayList<String>();
	
	
}