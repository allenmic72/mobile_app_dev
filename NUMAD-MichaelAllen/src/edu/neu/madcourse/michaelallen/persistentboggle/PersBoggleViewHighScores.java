package edu.neu.madcourse.michaelallen.persistentboggle;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.neu.madcourse.michaelallen.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;


public class PersBoggleViewHighScores extends Activity implements OnClickListener{
	
	private ListView listv ; 
	protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pers_boggle_hs);
    	
    	View hsQuit = findViewById(R.id.pers_boggle_hs_quit);
    	hsQuit.setOnClickListener(this);
    	
    	ListView listv = (ListView) findViewById(R.id.pers_boggle_hs_view);
    	 
    	 
    	ArrayList<String> hsStrings = new ArrayList<String>();
    	ArrayList<PersBoggleHighScore> hs = PersGlobals.getGlobals().getHighScoreList();
    	if (hs == null){ //check spref for a copy of the hsList
    		try{
    			Gson gson = new Gson();
    			PersBoggleSharedPrefAPI spref = new PersBoggleSharedPrefAPI();
        		String hsList = spref.getString(this, "pers_highscores");
        		if (hsList != null && hsList != ""){
        			Type hsType = new TypeToken<ArrayList<PersBoggleHighScore>>(){}.getType();
        			hs = gson.fromJson(hsList, hsType);
        			CharSequence text = "Trouble connecting to server. Using cached high scores";
            		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        		}
    		}
    		catch (RuntimeException E){
    		}
    		
    	}
    	if (hs != null){
    		if (hs.size() == 0){
    			hsStrings.add("There are no High Scores at this time. Play some Persistent Boggle!");
    		}
    		else{
	        	for (int i = 0; i < hs.size(); i++){
	        		PersBoggleHighScore temp = hs.get(i);
	        		hsStrings.add("" + temp.score + " by " + temp.name + " on " + temp.date);
	        	}
    		}
    	}
    	else{
    		hsStrings.add("There was an error getting the High Scores list from the server. " +
    				"It could be a problem with your internet connection");
    	}
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.pers_boggle_list_textview, hsStrings);
    	
    	listv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				finish();
			}
    		}); 
    	
    	listv.setAdapter(adapter);
    	
	}
	
		
	

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_hs_quit:
			finish();
			break;
		}
		
	}
}