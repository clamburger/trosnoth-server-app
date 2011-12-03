/*******************************************************************************
 * Copyright 2011 Samuel Horn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
