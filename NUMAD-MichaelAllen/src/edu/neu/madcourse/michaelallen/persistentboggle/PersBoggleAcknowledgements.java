package edu.neu.madcourse.michaelallen.persistentboggle;

import edu.neu.madcourse.michaelallen.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PersBoggleAcknowledgements extends Activity implements OnClickListener{
	 @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.pers_boggle_acknowledgements);
	      
	      View quit = findViewById(R.id.pers_boggle_ack_quit);
	      quit.setOnClickListener(this);
	   }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pers_boggle_ack_quit:
			finish();
			break;
		}
	}
}