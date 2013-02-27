package edu.neu.madcourse.michaelallen.persistentboggle;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PersBoggleSharedPrefAPI{
	
	
	public String getString (Context c, String key){
		SharedPreferences sPref = c.getSharedPreferences(PersGlobals.getGlobals().getSharedPrefName(), 0);
		
		return sPref.getString(key, null);
	}
	
	public void putString (Context c, String key, String val){
		SharedPreferences sPref = c.getSharedPreferences(PersGlobals.getGlobals().getSharedPrefName(), 0);
		Editor e = sPref.edit();
		
		e.putString(key, val);
		e.commit();
	}
}