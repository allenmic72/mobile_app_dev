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
							//Log.d("GCMServlet", "Sending message to " + regId);
							return result;
						} catch (IOException e) {
							//Log.d("GCMServlet", "IOException when sending message: " + e);
						}
					}
					return null;
				}
				
				 protected void onPostExecute(Result result) {
			         //Log.d("GCMServlet", "" + result);
			     }
				
			};
			sendMessageAsync.execute(message);
		}
	}
	
	/**
	 * sends a message to the given GCM regid
	 * Will retry many times using GCM's exponential backoff retry
	 */
	public void sendAsyncMessage(Message message, final String regId){
		sendMessageAsync = new AsyncTask<Message, Void, Result>(){
			
			@Override
			protected Result doInBackground(Message... params) {
				Message mes = params[0];
				Sender sender = new Sender(myApiKey);
				if (mes != null){
					try {
						Result result = sender.send(mes, regId, 50);
						//Log.d("GCMServlet", "Sending message to " + regId);
						return result;
					} catch (IOException e) {
						//Log.e("GCMServlet", "IOException when sending message: " + e);
					}
				}
				return null;
			}
			
			 protected void onPostExecute(Result result) {
		        // Log.d("GCMServlet", "GCM send result:::: " + result);
		     }
			
		};
		sendMessageAsync.execute(message);
	}
}