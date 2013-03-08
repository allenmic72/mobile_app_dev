package edu.neu.madcourse.michaelallen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.*;
import edu.neu.madcourse.michaelallen.boggle.BoggleMain;
import edu.neu.madcourse.michaelallen.boggle.Globals;
import edu.neu.madcourse.michaelallen.persistentboggle.PersBoggleMain;
import edu.neu.madcourse.michaelallen.sudoku.Sudoku;
import edu.neu.mobileClass.*;
import edu.neu.mobileclass.apis.KeyValueAPI;

import com.google.android.gcm.GCMRegistrar;

import static edu.neu.madcourse.michaelallen.GCMIntentService.SENDER_ID;


public class MainActivity extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        //TODO: uncomment this
        //PhoneCheckAPI.doAuthorization(this);

        registerGCM();
        
        this.setTitle("Michael Allen");
        
        View teamButton = findViewById(R.id.team_button);
        teamButton.setOnClickListener(this);
        
        View errorButton = findViewById(R.id.create_error_button);
        errorButton.setOnClickListener(this);
        
        View sudokuButton = findViewById(R.id.sudoku_button);
        sudokuButton.setOnClickListener(this);
        
        View mainExitButton = findViewById(R.id.main_exit_button);
        mainExitButton.setOnClickListener(this);
        
        View boggleButton = findViewById(R.id.boggle_main_button);
        boggleButton.setOnClickListener(this);
        
        View persistentBoggleButton = findViewById(R.id.pers_boggle_main_button);
        persistentBoggleButton.setOnClickListener(this);
        
        registerReceiver(mHandleMessageReceiver, new IntentFilter("edu.neu.madcourse.michaelallen.DISPLAY_MESSAGE"));
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onDestroy(){
    	 unregisterReceiver(mHandleMessageReceiver);
         GCMRegistrar.onDestroy(this);
         super.onDestroy();
    }
    
	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		 case R.id.team_button:
			 Intent teamIntent = new Intent(this, Team.class);
			 startActivity(teamIntent);
			 break;
		 case R.id.create_error_button:
			 int error = 5/0;
			 error = error + error;
			 break;
		 case R.id.sudoku_button:
			 Intent sudokuIntent = new Intent(this, Sudoku.class);
			 startActivity(sudokuIntent);
			 break;
		 case R.id.main_exit_button:
			 finish();
			 break;
		 case R.id.boggle_main_button:
			 Intent boggleIntent = new Intent(this, BoggleMain.class);
			 startActivity(boggleIntent);
			 break;
		 case R.id.pers_boggle_main_button:
			 Intent persBoggleIntent = new Intent(this, PersBoggleMain.class);
			 startActivity(persBoggleIntent);
			 break;
		 }
		
	}
	
	/**
	 * register device with GCM if it is not already registered
	 */
	public void registerGCM(){
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
		  GCMRegistrar.register(this, SENDER_ID);
		} else {
		  Log.v("GCM Registering", "Already registered" + regId);
		  
		  //check to see if it is stored on server
		  AsyncTask<String, Void, Void> checkServerForRegId = new AsyncTask<String, Void, Void>(){

			@Override
			protected Void doInBackground(String... params) {
				String regId = params[0];
				if (canAccessNetwork() && KeyValueAPI.isServerAvailable()){
					TelephonyManager tm = (TelephonyManager) 
						    getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
					String phoneNumber = tm.getLine1Number();
					String serverRegId = KeyValueAPI.get("allenmic", "allenmic", phoneNumber + "regId");
					if (serverRegId == null || serverRegId == ""){
						Log.d("registerGCM", "regId on server DNE, putting " + regId);
						KeyValueAPI.put("allenmic", "allenmic", phoneNumber + "regId", regId);
					}
					else{
						Log.d("registerGCM", regId + " is already on the server");
					}
				}
				return null;
			}
			  
		  };
		  checkServerForRegId.execute(regId);
		}
	}
	
	private boolean canAccessNetwork() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if (activeNetworkInfo != null){
	    	return true;
	    }
	    else{
	    	return false;
	    }
	    
	}
	
	  private final BroadcastReceiver mHandleMessageReceiver =
	            new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	
	        }
	    };
	
    
}
