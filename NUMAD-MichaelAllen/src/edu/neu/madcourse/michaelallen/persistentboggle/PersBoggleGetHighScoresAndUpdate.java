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
	final Context c;
	PersBoggleGetHighScoresAndUpdate(Context c){
		this.c = c;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(Integer... params) {
		if (canAccessServer()){
			int userNewHSint = params[0];
			int opponentNewHSint = params[1];
			Calendar c = Calendar.getInstance();
			Date date = c.getTime();
			String dateString = date.toString();
			PersBoggleHighScore userNewHS = new PersBoggleHighScore(userNewHSint, PersGlobals.getGlobals().getUsername(), dateString);
			PersBoggleHighScore opponentNewHS = new PersBoggleHighScore(opponentNewHSint, PersGlobals.getGlobals().getOpponent(), dateString);
			
			String hsList = KeyValueAPI.get(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
					"pers_highscores");
			
			if (hsList == ""){
				noPriorHS(userNewHS, opponentNewHS);
			}
			else{
				Gson gson = new Gson();
				
				try{
				Type hsType = new TypeToken<ArrayList<PersBoggleHighScore>>(){}.getType();
				ArrayList<PersBoggleHighScore> oldHighScore = new ArrayList<PersBoggleHighScore>();
				oldHighScore = gson.fromJson(hsList, hsType);
				
				ArrayList<PersBoggleHighScore> newhsList = checkOldHighScores(0, oldHighScore, userNewHS);
				if (opponentNewHS.score > 0){
					newhsList = checkOldHighScores(0, newhsList, opponentNewHS);
				}
				
				String jjson = gson.toJson(newhsList);
				KeyValueAPI.put(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
						"pers_highscores", jjson);
				PersGlobals.getGlobals().setHighScoreList(newhsList);
				
				return jjson;
				}
				catch (JsonSyntaxException e){
					
				}
			}
			

			return "";
		}
		else{
			return "";
		}
		
	}
	
	protected void onPostExecute(String hsList) {
		if (c != null && hsList != ""){
			PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
			spref.putString(c, "pers_highscores", hsList);
		}
	}
	
	private void noPriorHS(PersBoggleHighScore newHS, PersBoggleHighScore opponentNewHS){
		ArrayList<PersBoggleHighScore> highScores = new ArrayList<PersBoggleHighScore>();
		if (newHS.score >= 0){
			highScores.add(newHS);
			highScores = checkOldHighScores(0, highScores, opponentNewHS);
			
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
	    	if (PersGlobals.getGlobals().canAccessNetwok(c)){
	    		return KeyValueAPI.isServerAvailable();
	    	}
	    	else{
	    		return false;
	    	}
	}	
	
}