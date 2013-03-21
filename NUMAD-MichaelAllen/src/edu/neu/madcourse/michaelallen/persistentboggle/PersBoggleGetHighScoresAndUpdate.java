package edu.neu.madcourse.michaelallen.persistentboggle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.michaelallen.boggle.Globals;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

/**
 * uses int of -1 on execution to signal that there is no high score to add
 * @author Mike
 *
 */
public class PersBoggleGetHighScoresAndUpdate extends AsyncTask<Integer, Void, String>{

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(Integer... params) {
		if (!canAccessServer()){
			//TODO no access to server, defer the high scoring?
		}
		int newHSint = params[0];
		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		String dateString = date.toString();
		PersBoggleHighScore newHS = new PersBoggleHighScore(newHSint, PersGlobals.getGlobals().getUsername(), dateString);
		
		String hsList = KeyValueAPI.get(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
				"pers_highscores");
		
		if (hsList == ""){
			noPriorHS(newHS);
		}
		else{
			Gson gson = new Gson();
			
			try{
			Type hsType = new TypeToken<ArrayList<PersBoggleHighScore>>(){}.getType();
			ArrayList<PersBoggleHighScore> oldHighScore = new ArrayList<PersBoggleHighScore>();
			oldHighScore = gson.fromJson(hsList, hsType);
			
			ArrayList<PersBoggleHighScore> newhsList = checkOldHighScores(0, oldHighScore, newHS);
			String jjson = gson.toJson(newhsList);
			KeyValueAPI.put(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
					"pers_highscores", jjson);
			PersGlobals.getGlobals().setHighScoreList(newhsList);
			}
			catch (JsonSyntaxException e){
				
			}
		}
		

		return hsList;
	}
	
	protected void onPostExecute(String hsList) {
	}
	
	private void noPriorHS(PersBoggleHighScore newHS){
		ArrayList<PersBoggleHighScore> highScores = new ArrayList<PersBoggleHighScore>();
		if (newHS.score >= 0){
			highScores.add(newHS);
			
			Gson gson = new Gson();
			String jjson = gson.toJson(highScores);
			KeyValueAPI.put(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
					"pers_highscores", jjson);
			PersGlobals.getGlobals().setHighScoreList(highScores);
		}
		else{
			PersGlobals.getGlobals().setHighScoreList(highScores);
		}
	}
	
	private ArrayList<PersBoggleHighScore> checkOldHighScores(int i, ArrayList<PersBoggleHighScore> hsList, PersBoggleHighScore newHS){
		while (i < 5){
			if (i < hsList.size()){
				if (hsList.get(i).score < newHS.score){				
					PersBoggleHighScore tmp = hsList.get(i);
					
					hsList.set(i, newHS);
					return checkOldHighScores(i+1, hsList, tmp); //shift old score
				}
				i++;
			}
			else{
				//at the end of the list, but less than 5 entries. Add new HS to end of list
				if (newHS.score >= 0){
					hsList.add(newHS);
				}
				break;
			}
		}
		return hsList;
	}
	
	private boolean canAccessServer() {
	    	return KeyValueAPI.isServerAvailable();
	}	
	
}