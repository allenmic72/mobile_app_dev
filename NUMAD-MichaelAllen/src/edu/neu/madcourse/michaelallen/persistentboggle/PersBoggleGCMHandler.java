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
		else if (type.equals("asyncUpdate")){
			setAsyncTurnNotification();
		}
		else if (type.equals("declined")){
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
		String username = getExtraString("username");
		String regId = getExtraString("regId");
		String oppRegId = getExtraString("oppRegId");
		
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
        int icon = R.drawable.ic_launcher;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder notification = 
        		new NotificationCompat.Builder(context)
       			.setSmallIcon(icon)
       			.setContentTitle("Challenged!")
       			.setContentText(opponent + " has challenged you!")
       			.setAutoCancel(true);
        

        Intent notificationIntent = new Intent(context, PersBoggleAcceptChallenge.class);
        notificationIntent.putExtra("username", username);
        notificationIntent.putExtra("opponent", opponent);
        notificationIntent.putExtra("time", timeSent);
        notificationIntent.putExtra("regId", regId);
        notificationIntent.putExtra("oppRegId", oppRegId);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PersBoggleMain.class);
        stackBuilder.addNextIntent(notificationIntent);
        
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        notification.setContentIntent(pendingIntent);        
        notificationManager.notify(opponent.hashCode(), notification.build());
        
	}
	

	private void setAsyncTurnNotification() {
		Gson gson = new Gson();
		String opponent = getExtraString("opponent");
		String username = getExtraString("username");
		String regId = getExtraString("regId");
		String oppRegId = getExtraString("oppRegId");
		String scoreJson = getExtraString("score");
		String opponentScoreJson = getExtraString("opponentScore");
		
		int score = gson.fromJson(scoreJson, Integer.class);
		int opponentScore = gson.fromJson(opponentScoreJson, Integer.class);
		
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
        int icon = R.drawable.ic_launcher;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder notification = 
        		new NotificationCompat.Builder(context)
       			.setSmallIcon(icon)
       			.setContentTitle("Asynchronous Boggle")
       			.setContentText("Play your turn against " + opponent + " !")
       			.setAutoCancel(true);
        

        Intent notificationIntent = new Intent(context, PersBoggleGame.class);
        notificationIntent.putExtra("username", username);
        notificationIntent.putExtra("opponent", opponent);
        notificationIntent.putExtra("regId", regId);
        notificationIntent.putExtra("oppRegId", oppRegId);
        notificationIntent.putExtra("score", score);
        notificationIntent.putExtra("opponentScore", opponentScore);
        notificationIntent.putExtra("status", "async");
        notificationIntent.putExtra("leader", true);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PersBoggleMain.class);
        stackBuilder.addNextIntent(notificationIntent);
        
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        notification.setContentIntent(pendingIntent);
        notificationManager.notify(opponent.hashCode(), notification.build());
        
	}
	
}