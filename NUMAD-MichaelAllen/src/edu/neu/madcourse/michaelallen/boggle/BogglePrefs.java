package edu.neu.madcourse.michaelallen.boggle;

import edu.neu.madcourse.michaelallen.R;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class BogglePrefs extends PreferenceActivity {
	
	   private static final String OPT_MUSIC = "music";
	   private static final boolean OPT_MUSIC_DEF = true;
	   
	@SuppressWarnings("deprecation")
	@Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      addPreferencesFromResource(R.xml.boggle_settings);
	      //TODO: get options working
	   }
	
	   public static boolean getMusic(Context context) {
	      return PreferenceManager.getDefaultSharedPreferences(context)
	            .getBoolean(OPT_MUSIC, OPT_MUSIC_DEF);
	   }
	   

}