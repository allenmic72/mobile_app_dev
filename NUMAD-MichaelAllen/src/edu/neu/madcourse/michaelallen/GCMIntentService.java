package edu.neu.madcourse.michaelallen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import edu.neu.madcourse.michaelallen.MainActivity;
import edu.neu.madcourse.michaelallen.persistentboggle.PersGlobals;

import com.google.android.gcm.GCMBaseIntentService;

import edu.neu.mobileclass.apis.KeyValueAPI;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	public static final String SENDER_ID = "395918605463";
	private static final String TAG = "GCMIntentService";
	
	public GCMIntentService() {
        super(SENDER_ID); //Google API sender Id
    }
	
	@Override
	protected void onError(Context c, String errorId) {
		//make sure regid is not set so we can get a new one
	}

	@Override
	protected void onMessage(Context c, Intent intent) {
		Log.d(TAG, "Recieved a GCM Message" + intent);
		generateNotification(c, "Hey boy you got a message, better read it");
		
	}

	@Override
	protected void onRegistered(Context c, String regId) {
		//send regId to server and save in sharedPref and PersGlobals
		if (canAccessNetwork()){
			AsyncTask<String, Void, Void> registerIdToServer = 
					new AsyncTask<String, Void, Void>(){

						@Override
						protected Void doInBackground(String... id) {
							String regId = id[0];
							if (regId != null && regId != ""){
								if(KeyValueAPI.isServerAvailable()){
									TelephonyManager tm = (TelephonyManager) 
										    getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
									String phoneNumber = tm.getLine1Number();
									KeyValueAPI.put("allenmic", "allenmic", phoneNumber + "regId", regId);
									PersGlobals.getGlobals().getSharedPrefName();
									Log.d(TAG, "registered and put id: " + regId + " on server");
								}
							}
							return null;
						}
				
			};
			registerIdToServer.execute(regId);
		}
		else{
			//TODO can't access network so queue for later? 
		}
		
	}

	@Override
	protected void onUnregistered(Context c, String regId) {
		//make sure to remove references to regId in sharedPref and Globals
		
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
	
	 /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
	
}