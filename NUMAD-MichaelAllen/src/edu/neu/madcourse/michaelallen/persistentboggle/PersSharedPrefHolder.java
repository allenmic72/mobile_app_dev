package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;

import com.google.gson.*;
import com.google.gson.annotations.*;

public class PersSharedPrefHolder{
	@Expose
	public String[][] boardLetters;
	
	@Expose
	public int score;
	
	@Expose
	public ArrayList<String> priorChosenWords;
	
	@Expose
	public boolean newGame;
	
	@Expose
	public long timerVal;
	
	@Expose
	public boolean isPaused;
}