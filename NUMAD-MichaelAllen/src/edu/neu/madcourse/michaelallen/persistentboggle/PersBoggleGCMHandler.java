package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;

import edu.neu.madcourse.michaelallen.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class PersBoggleGCMHandler{
	Intent gcm;
	Bundle extras;
	Context context;
	
	public PersBoggleGCMHandler(Context context, Intent gcm){
		this.context = context;
		this.gcm = gcm;
		if (this.gcm.getExtras() != null){
			this.extras = this.gcm.getExtras();
			processMessage();
		}
		else{
			Log.d("GCMHandler", "Error: invalid GCM. No Extras");
		}
	}
	
	public void processMessage(){
		String type = getExtraString("type");
		
		identifyAndHandleMessageType(type);
	}
	
	private String getExtraString(String key){
		if (extras.getString(key) != null){
			return extras.getString(key);
		}
		else{
			Log.d("GCMHandler", "Error: Expected GCM with extra: " + key);
			return null;
		}
	}
	
	private void identifyAndHandleMessageType(String type){
		if (type.equals("challenge")){
			setChallengedNotification();
		}
		else if (type == "declined"){
			//TODO
		}
		else if (type == "asyncUpdate"){
			//TODO
		}
		else {
			Log.d("GCMHandler", "Error: invalid GCM type recieved: " + type);
		}
	}
	
	private void setChallengedNotification(){
		Gson gson = new Gson();
		String timeSent = getExtraString("time");
		String opponent = getExtraString("opponent");
		
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
        int icon = R.drawable.ic_launcher;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder notification = 
        		new NotificationCompat.Builder(context)
       			.setSmallIcon(icon)
       			.setContentTitle("Challenged!")
       			.setContentText(opponent + " has challenged you!")
       			.setAutoCancel(true);
        

        Intent notificationIntent = new Intent(context, PersBoggleNotificationClicked.class);
        notificationIntent.putExtra("opponent", opponent);
        notificationIntent.putExtra("time", timeSent);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PersBoggleMain.class);
        stackBuilder.addNextIntent(notificationIntent);
        
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        notification.setContentIntent(pendingIntent);
        
        Log.d("", "intent created");
        
        notificationManager.notify(opponent.hashCode(), notification.build());
        
        Log.d("", "intent created 1111");
	}
	
}