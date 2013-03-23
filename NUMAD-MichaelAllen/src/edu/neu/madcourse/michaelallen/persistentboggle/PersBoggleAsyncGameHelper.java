package edu.neu.madcourse.michaelallen.persistentboggle;

import android.content.Context;
import android.telephony.TelephonyManager;
import edu.neu.mobileclass.apis.KeyValueAPI;

/**
 * Contains web calls so only use within an async task
 * @author Mike
 *
 */
public class PersBoggleAsyncGameHelper{
	String oppPhoneNum;
	Context context;
	
	PersBoggleAsyncGameHelper(Context context, String oppPhoneNum){
		this.oppPhoneNum = oppPhoneNum;
		this.context = context;
	}
	
	/**
	 * 
	 * @return arg0 for user regId, arg1 for opponent
	 */
	public String[] getRegIds(){
		TelephonyManager mTelephonyMgr = (TelephonyManager)
		        context.getSystemService(Context.TELEPHONY_SERVICE);

		String myPhoneNum = mTelephonyMgr.getLine1Number();
		    
		String regId = KeyValueAPI.get("allenmic", "allenmic", myPhoneNum + "regId");
		String oppRegId = KeyValueAPI.get("allenmic", "allenmic", oppPhoneNum + "regId");
		String[] result = new String[2];
		result[0] = regId;
		result[1] = oppRegId;
		return result;
	}
}