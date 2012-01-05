/*******************************************************************************
 * Copyright 2012 Samuel Horn
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DashboardActivity extends FragmentActivity {

	private static final String LOGTAG = "Dashboard";

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
		teams.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show();
			}
		});
		
		upgrades = (Button) findViewById(R.id.buttonUpgrades);
		upgrades.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, UpgradeActivity.class);
				startActivity(intent);
			}
		});
		
		mapLayout = (Button) findViewById(R.id.buttonMapLayout);
		mapLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show();
			}
		});		

		// Doing this here means we don't have to do it anywhere else
		telnet = ConnectionActivity.telnet;
		telnet.send("import json");

	}
	
	public void promptForMessage() {
		final EditText input = new EditText(this);
		
		new AlertDialog.Builder(this)
	    .setTitle("Send a server message")
	    .setView(input)
	    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            String message = input.getText().toString();
	            if (message.length() == 0) {
	            	return;
	            }
	            
	            message = message.replaceAll("\"", "\\\"");
	            telnet.send("getGame().sendServerMessage(\""+message+"\")");
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dashboard, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    
	        case R.id.sendMessage:
	        	promptForMessage();
	        	return true;
	        	
	        default:
	        	Log.i(LOGTAG, "Option selection fell through.");
	            return super.onOptionsItemSelected(item);
	    }
	}

}
