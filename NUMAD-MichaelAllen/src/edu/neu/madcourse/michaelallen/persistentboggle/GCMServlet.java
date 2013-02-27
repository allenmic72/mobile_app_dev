package edu.neu.madcourse.michaelallen.persistentboggle;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.server.*;

import edu.neu.mobileclass.apis.KeyValueAPI;

public class GCMServlet {
	private final String myApiKey = "AIzaSyDvZHrRf7WYck1xs4k0gNcXa7MTVo3BrMY";
	
	private AsyncTask<Message, Void, Result> sendMessageAsync;
	
	
	/**
	 * sends a message to the device with the phoneNumber arg
	 * @param message
	 * @param phoneNumber
	 */
	public void sendMessage(Message message, String phoneNumber){
		if (KeyValueAPI.isServerAvailable()){
			final String regId = KeyValueAPI.get("allenmic", "allenmic", phoneNumber + "regId");
			sendMessageAsync = new AsyncTask<Message, Void, Result>(){
				
				@Override
				protected Result doInBackground(Message... params) {
					Message mes = params[0];
					Sender sender = new Sender(myApiKey);
					if (mes != null){
						try {
							Result result = sender.send(mes, regId, 5);
							Log.d("GCMServlet", "Sending message to " + regId);
							return result;
						} catch (IOException e) {
							
						}
					}
					return null;
				}
				
				
			};
			sendMessageAsync.execute(message);
		}
	}
}