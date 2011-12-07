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

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;

import org.trosnoth.serveradmin.R;
import org.trosnoth.serveradmin.helpers.AutomatedTelnetClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DashboardActivity extends GDActivity {

	AutomatedTelnetClient telnet;

	Button gameState;
	Button players;
	Button teams;
	Button settings;

	public DashboardActivity() {
		// Removes the home button
		super(ActionBar.Type.Empty);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.dashboard);

		TrosnothApplication appState = (TrosnothApplication)getApplicationContext();
		String serverIP = appState.getServer();
		if (serverIP != null) {
			setTitle(getString(R.string.dashboard_connected, serverIP));
		}
		
		getActionBar().addItem(ActionBarItem.Type.Info);
		
		ActionBarItem item = getActionBar().newActionBarItem(NormalActionBarItem.class);
		item.setDrawable(R.drawable.action_bar_disconnect).setContentDescription(R.string.dashboard_disconnect);
		addActionBarItem(item);
		
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
				Intent intent = new Intent(DashboardActivity.this, ServerSettingsContainer.class);
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

		// Doing this here means we don't have to do it anywhere else
		telnet = ConnectionActivity.telnet;
		telnet.send("import json");

	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (position) {
		case 0:
			Intent intent = new Intent(DashboardActivity.this, InformationActivity.class);
			startActivity(intent);
			return true;
		case 1:
			finish();
			return true;
		default:
			return false;
		}
	}

}
