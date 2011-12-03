package org.trosnoth.serveradmin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class ScreenSelectorActivity extends Activity {
	
	AutomatedTelnetClient telnet;
	
	TextView serverIP;
	ImageButton gameState;
	ImageButton players;
	ImageButton teams;
	ImageButton settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_selector);
		
		serverIP = (TextView) findViewById(R.id.textServer);
		serverIP.setText("Connected to server at " + getIntent().getStringExtra("serverIP"));
		
		gameState = (ImageButton) findViewById(R.id.buttonGameState);
		gameState.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ScreenSelectorActivity.this,
								GameStateActivity.class);
				startActivity(intent);
			}
		});
		
		settings = (ImageButton) findViewById(R.id.buttonSettings);
		settings.setEnabled(false);
		
		players = (ImageButton) findViewById(R.id.buttonPlayers);
		players.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ScreenSelectorActivity.this,
								PlayerActivity.class);
				startActivity(intent);
			}
		});
		
		teams = (ImageButton) findViewById(R.id.buttonTeams);
		teams.setEnabled(false);
		
		// Doing this here means we don't have to do it anywhere else
		telnet = MainMenuActivity.telnet;
		telnet.send("game = getGame()");
		
	}

}
