package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;

import edu.neu.madcourse.michaelallen.R;
import edu.neu.mobileclass.apis.KeyValueAPI;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class PersBoggleChallengeUser extends Activity implements OnClickListener{
	
	private ListView listv ; 
	
	protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pers_boggle_challenge_user);
    	
    	View challengeQuit = findViewById(R.id.pers_boggle_challenge_back);
    	challengeQuit.setOnClickListener(this);
    	
    	ListView listv = (ListView) findViewById(R.id.pers_boggle_challenge_view);
    	
    	final ArrayList<String> otherUsers = PersGlobals.getGlobals().getOtherUsers();
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.pers_boggle_challenge_textview, otherUsers);
    	
    	listv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				final int idClicked = (int) id;
				
				AsyncTask<Void, Void, Void> lookupPhone = new AsyncTask<Void, Void, Void>(){

					protected Void doInBackground(Void... params) {
						if (KeyValueAPI.isServerAvailable()){
							String username = otherUsers.get(idClicked);
							String phoneNum = KeyValueAPI.get("allenmic", "allenmic", username);
							
							
							if (phoneNum != null){
								Calendar c = Calendar.getInstance();
								Gson gson = new Gson();
								Date date = c.getTime();
								String dateString = gson.toJson(date);
								
								Log.d("challenge this num", "challenging " + phoneNum);
								GCMServlet serv = new GCMServlet();
						        Builder mesBuilder = new Message.Builder();
						        mesBuilder.addData("username", username);
						        mesBuilder.addData("phoneNum", phoneNum);
						        mesBuilder.addData("message", "What up bro");
						        mesBuilder.addData("time", dateString);
						        serv.sendMessage(mesBuilder.build(), phoneNum);
							}
							
						}
						return null;
					}

				};
				lookupPhone.execute();
				
				
			}
    		}); 
    	
    	listv.setAdapter(adapter);
	}
	


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_challenge_back:
			finish();
			break;
		}
		
	}
}