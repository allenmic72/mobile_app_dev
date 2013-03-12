package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;

public class PersBoggleNotificationClicked extends Activity{
	
	
	private boolean withinLastFiveMinutes(Date timeSent){
		Date current = Calendar.getInstance().getTime();
		long timePassed = current.getTime() - timeSent.getTime();
		return timePassed < 300000; //300000 millis = 5 minutes
	}
}
