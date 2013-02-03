package edu.neu.madcourse.michaelallen.boggle;

import edu.neu.madcourse.michaelallen.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class BoggleMain extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.boggle_main);
        
        View exitButton = findViewById(R.id.boggle_exit_button);
        exitButton.setOnClickListener(this);
        View newGameButton = findViewById(R.id.boggle_new_game_button);
        newGameButton.setOnClickListener(this);
        
        View optionsButton = findViewById(R.id.boggle_options_button);
        optionsButton.setOnClickListener(this);
        
        
    }
    
    @Override
    protected void onResume() {
       super.onResume();
       BoggleMusic.play(this, R.raw.main);
       //TODO: Change the music
    }

    @Override
    protected void onPause() {
       super.onPause();
       BoggleMusic.stop(this);
    }

    

	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		 case R.id.boggle_exit_button:
			 finish();
			 break;
		 case R.id.boggle_new_game_button:
			 Intent boggleGame = new Intent(this, BoggleGame.class);
			 startActivity(boggleGame);
			 break;
		 case R.id.boggle_options_button:
			 Intent boggleOptions = new Intent(this, BogglePrefs.class);
			 startActivity(boggleOptions);
		 }
		
	}
    
}