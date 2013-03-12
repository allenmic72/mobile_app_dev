package edu.neu.madcourse.michaelallen;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;
import edu.neu.madcourse.michaelallen.MainActivity;
import edu.neu.madcourse.michaelallen.persistentboggle.PersBoggleGame;
import edu.neu.madcourse.michaelallen.persistentboggle.PersBoggleMain;
import edu.neu.madcourse.michaelallen.persistentboggle.PersGlobals;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;

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
		Bundle extras = intent.getExtras();
		String username = (String) extras.get("username");
		String phoneNum = (String) extras.get("phoneNum");
		String message = (String) extras.get("message");
		String timeJson = (String) extras.get("time");
		
		Log.d(TAG, "Message: " + message + ", from" + username + " at " + phoneNum);
		generateNotification(c, username, timeJson);
		
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
    private static void generateNotification(Context context, String username, String time) {
        Vibrator v = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        v.vibrate(100);
        int icon = R.drawable.ic_launcher;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder notification = 
        		new NotificationCompat.Builder(context)
       			.setSmallIcon(icon)
       			.setContentTitle("Challenged!")
       			.setContentText(username + " has challenged you!")
       			.setAutoCancel(true);
        

        Intent notificationIntent = new Intent(context, PersBoggleGame.class);
        notificationIntent.putExtra("opponent", username);
        notificationIntent.putExtra("time", time);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PersBoggleMain.class);
        stackBuilder.addNextIntent(notificationIntent);
        
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        notification.setContentIntent(pendingIntent);
        
        notificationManager.notify(username.hashCode(), notification.build());
        
    }
	
}