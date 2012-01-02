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

import org.trosnoth.serveradmin.R;
import org.trosnoth.serveradmin.helpers.AutomatedTelnetClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DashboardActivity extends Activity {

	AutomatedTelnetClient telnet;

	Button gameState;
	Button players;
	Button teams;
	Button settings;
	Button upgrades;
	Button mapLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);

		TrosnothApplication appState = (TrosnothApplication)getApplicationContext();
		String serverIP = appState.getServer();
		if (serverIP != null) {
			setTitle(getString(R.string.dashboard_connected, serverIP));
		}
				
		gameState = (Button) findViewById(R.id.buttonGameState);
		gameState.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, GameStateActivity.class);
				startActivity(intent);
			}
		});

		settings = (Button) findViewById(R.id.buttonServerSettings);
		settings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, ServerSettingsActivity.class);
				startActivity(intent);
			}
		});

		players = (Button) findViewById(R.id.buttonPlayers);
		players.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, PlayerActivity.class);
				startActivity(intent);
			}
		});

		teams = (Button) findViewById(R.id.buttonTeams);
		teams.setEnabled(false);
		
		upgrades = (Button) findViewById(R.id.buttonUpgrades);
		upgrades.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, UpgradeActivity.class);
				startActivity(intent);
			}
		});
		
		mapLayout = (Button) findViewById(R.id.buttonMapLayout);
		mapLayout.setEnabled(false);		

		// Doing this here means we don't have to do it anywhere else
		telnet = ConnectionActivity.telnet;
		telnet.send("import json");

	}

}
