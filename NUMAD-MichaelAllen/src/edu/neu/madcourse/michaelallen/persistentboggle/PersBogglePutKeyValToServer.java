package edu.neu.madcourse.michaelallen.persistentboggle;

import edu.neu.mobileclass.apis.KeyValueAPI;
import android.os.AsyncTask;
import android.util.Log;

/**
 * puts param[1] val to the server for param[0] key
 * @author Mike
 *
 */
public class PersBogglePutKeyValToServer extends AsyncTask <String, Void, Void>{

	@Override
	protected Void doInBackground(String... params) {
		String key = params[0];
		String val = params[1];
		
		if (canAccessServer()){
			KeyValueAPI.put(PersGlobals.getGlobals().getTeamName(), PersGlobals.getGlobals().getPassword(),
					key, val);
			//Log.d("putkeyval", "putting val " + val + " for key " + key);
		}
		else{
			//TODO no server access what to do
		}
		
		return null;
	}
	
	private boolean canAccessServer() {
    	return KeyValueAPI.isServerAvailable();
    }	

}