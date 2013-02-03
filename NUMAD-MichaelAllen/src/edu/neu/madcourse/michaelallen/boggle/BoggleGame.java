package edu.neu.madcourse.michaelallen.boggle;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import edu.neu.madcourse.michaelallen.R;

public class BoggleGame extends Activity implements OnClickListener{
	
	public String boardLetters[][];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.boggle_game);
		
		View quitGame = findViewById(R.id.boggle_game_quit);
		quitGame.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.boggle_game_quit:
			finish();
			break;
		}
		
	}
}