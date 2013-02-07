package edu.neu.madcourse.michaelallen.boggle;

import java.util.ArrayList;

import com.google.gson.Gson;

import edu.neu.madcourse.michaelallen.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class BoggleHS extends Activity{
	
	private ListView listv ; 
	protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.boggle_hs);
    	
    	ListView listv = (ListView) findViewById(R.id.boggle_hs_view);
    	 
    	 
    	ArrayList<String> hsStrings = new ArrayList<String>();
    	int[] hs = getHighScores();
    	if (hs != null){
        	for (int i = 0; i < hs.length; i++){
        		hsStrings.add( "" + hs[i]);
        	}
    	}
    	else{
    		hsStrings.add("No high scores at this time, play more Boggle!");
    	}
    	
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.boggle_list_textview, hsStrings);
    	
    	listv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				finish();
			}
    		}); 
    	
    	listv.setAdapter(adapter);
    	
	}
	
	private int[] getHighScores(){
		SharedPreferences spr = getSharedPreferences(Globals.getGlobals().getHighScorePrefName(), 0);
		Editor e = spr.edit();
		Gson gson = new Gson();

		String oldHS = spr.getString("highscores", null);
		
		if (oldHS == null){
			return null;
		}
		else{
			BoggleHighScores hs = gson.fromJson(oldHS, BoggleHighScores.class);
			return hs.highscores;
		}
		
	}
}