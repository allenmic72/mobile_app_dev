package edu.neu.madcourse.michaelallen;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.*;
import edu.neu.mobileClass.*;


public class MainActivity extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        PhoneCheckAPI.doAuthorization(this);

        this.setTitle("Michael Allen");
        
        View teamButton = findViewById(R.id.team_button);
        teamButton.setOnClickListener(this);
        
        View errorButton = findViewById(R.id.create_error_button);
        errorButton.setOnClickListener(this);
        
        View sudokuButton = findViewById(R.id.sudoku_button);
        sudokuButton.setOnClickListener(this);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		 case R.id.team_button:
			 Intent i = new Intent(this, Team.class);
			 startActivity(i);
			 break;
		 case R.id.create_error_button:
			 int error = 5/0;
			 error = error + error;
			 break;
		 case R.id.sudoku_button:
			 Intent s = new Intent(this, Sudoku.class);
			 startActivity(s);
			 break;
		 }
		
	}
    
}
