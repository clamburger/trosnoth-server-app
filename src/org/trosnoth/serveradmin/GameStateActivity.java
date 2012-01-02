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

import org.trosnoth.serveradmin.helpers.AutomatedTelnetClient;
import org.trosnoth.serveradmin.helpers.InputFilters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameStateActivity extends FragmentActivity {

	private static final String LOGTAG = "GameState";

	AutomatedTelnetClient telnet;

	Button startGame;
	Button endGame;
	Button resetGame;
	Button changeTime;
	CheckBox skipCountdown;
	Spinner winner;

	TextView gameState;
	TextView timeLeft;
	TextView blueZones;
	TextView neutralZones;
	TextView redZones;
	
	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_state);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		startGame = (Button) findViewById(R.id.buttonStartGame);
		endGame = (Button) findViewById(R.id.buttonEndGame);
		resetGame = (Button) findViewById(R.id.buttonResetGame);
		changeTime = (Button) findViewById(R.id.buttonChangeTime);
		skipCountdown = (CheckBox) findViewById(R.id.checkSkipCountdown);
		winner = (Spinner) findViewById(R.id.spinnerWinner);

		startGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.i(LOGTAG, "Starting game.");
				telnet.send("game.startGame()");
				update();
			}
		});

		endGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String[] teams = { "None", "'A'", "'B'" };
				String team = teams[winner.getSelectedItemPosition()];
				Log.i(LOGTAG, "Ending game: " + team);
				telnet.send("game.endGame(" + team + ")");
				update();
			}
		});

		resetGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), R.string.not_yet_implemented, Toast.LENGTH_LONG)
								.show();

			}
		});

		changeTime.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(1001);
			}
		});

		gameState = (TextView) findViewById(R.id.textGameState);
		timeLeft = (TextView) findViewById(R.id.textTimeLeft);
		blueZones = (TextView) findViewById(R.id.textBlueZones);
		neutralZones = (TextView) findViewById(R.id.textNeutralZones);
		redZones = (TextView) findViewById(R.id.textRedZones);

		startGame.setEnabled(false);
		endGame.setEnabled(false);
		resetGame.setEnabled(false);
		changeTime.setEnabled(false);
		skipCountdown.setEnabled(false);
		winner.setEnabled(false);

		telnet = ConnectionActivity.telnet;
		telnet.send("game = getGame()");

		// Update every 5 seconds
		Runnable looper = new Runnable() {
			public void run() {
				update();				
				mHandler.postDelayed(this, ConnectionActivity.UPDATE_FREQ);
			}
		};

		if (ConnectionActivity.automaticUpdate) {
			mHandler.removeCallbacks(looper);
			mHandler.post(looper);
		} else {
			update();
		}
		
	}

	public void update() {

		String result;
		int intResult;

		telnet.send("game = getGame()");
		
		// Get game state
		result = telnet.readWrite("game.getGameState()");
		result = (String) telnet.parse(result);

		Resources res = getResources();

		Boolean start = false;
		Boolean end = false;
		
		if (result.equals("PreGame")) {
			gameState.setText(R.string.game_state_pregame);
			gameState.setTextColor(res.getColor(R.color.light_yellow));
			start = true;
		} else if (result.equals("Lobby")) {
			gameState.setText(R.string.game_state_lobby);
			gameState.setTextColor(res.getColor(R.color.light_yellow));
			start = true;
		} else if (result.equals("Starting")) {
			gameState.setText(R.string.game_state_starting);
			gameState.setTextColor(res.getColor(R.color.light_yellow));
		} else if (result.equals("InProgress")) {
			gameState.setText(R.string.game_state_in_progress);
			gameState.setTextColor(res.getColor(R.color.light_green));
			end = true;
		} else if (result.equals("Ended")) {
			
			result = telnet.readWrite("game.getWinningTeam()");
			if (result.length() == 0) {
				gameState.setText(R.string.game_state_draw);
				gameState.setTextColor(res.getColor(R.color.neutral_text));
			} else {
				result = (String) telnet.parse(result);
				if (result.equals("A")) {
					gameState.setText(R.string.game_state_blue_wins);
					gameState.setTextColor(res.getColor(R.color.blue_text));
				} else {
					gameState.setText(R.string.game_state_red_wins);
					gameState.setTextColor(res.getColor(R.color.red_text));
				}
			}
		} else {
			gameState.setText(R.string.game_state_unknown);
			gameState.setTextColor(res.getColor(R.color.disabled));
		}

		if (start) {
			startGame.setEnabled(true);
			//skipCountdown.setEnabled(true);
		} else {
			startGame.setEnabled(false);
			skipCountdown.setEnabled(false);
		}
		
		if (end) {
			endGame.setEnabled(true);
			winner.setEnabled(true);
			changeTime.setEnabled(true);
		} else {
			endGame.setEnabled(false);
			winner.setEnabled(false);
			changeTime.setEnabled(false);
		}

		resetGame.setEnabled(true);

		// Time remaining
		
		result = telnet.readWrite("game.getTimeRemaining()");
		if (result.length() == 0) {
			timeLeft.setText("--:--");
			timeLeft.setTextColor(res.getColor(R.color.neutral_text));
		} else if (result.equals("False")) {
			timeLeft.setText("\u221E");
			timeLeft.setTextColor(res.getColor(R.color.light_green));
		} else {
			int timeInt = (int) Math.ceil(Double.valueOf(result));
			int minutes = (int) Math.floor(timeInt / 60.0);
			int seconds = timeInt % 60;		
			
			timeLeft.setText(String.format("%02d:%02d", minutes, seconds));
			
			if (timeInt <= 60) {
				timeLeft.setTextColor(res.getColor(R.color.light_red));
			} else if (timeInt <= 180) {
				timeLeft.setTextColor(res.getColor(R.color.light_yellow));
			} else {
				timeLeft.setTextColor(res.getColor(R.color.light_green));
			}
		}

		// Zone counts
		int ownedZones = 0;

		result = telnet.readWrite("game.world.teams[0].numOrbsOwned");
		ownedZones += Integer.valueOf(result);
		blueZones.setText(result);
		blueZones.setTextColor(res.getColor(R.color.blue_text));

		result = telnet.readWrite("game.world.teams[1].numOrbsOwned");
		ownedZones += Integer.valueOf(result);
		redZones.setText(result);
		redZones.setTextColor(res.getColor(R.color.red_text));

		intResult = Integer.valueOf(telnet.readWrite("len(game.world.zones)"));
		neutralZones.setText(String.valueOf(intResult - ownedZones));
		neutralZones.setTextColor(res.getColor(R.color.neutral_text));

		//Log.i(LOGTAG, "Update complete.");
	}
	
	protected Dialog onCreateDialog(int id) {
		if (id == 1001) {
			AlertDialog.Builder builder;
			AlertDialog alertDialog;

			Context mContext = GameStateActivity.this;
			LayoutInflater inflater = (LayoutInflater) mContext
							.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.time_dialog,
							(ViewGroup) findViewById(R.id.layout_root));

			final EditText secs = (EditText) layout.findViewById(R.id.editSeconds);
			secs.setFilters(InputFilters.integerFilter(59));
			
			final EditText mins = (EditText) layout.findViewById(R.id.editMinutes);

			builder = new AlertDialog.Builder(mContext);
			builder.setView(layout);
			alertDialog = builder.create();
			alertDialog.setTitle(R.string.game_state_adjust_time);
			alertDialog.setButton(-1, getApplicationContext().getString(R.string.save),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						
						// Get the values from the EditTexts in the dialog
						String minStr = mins.getText().toString();
						String secStr = secs.getText().toString();
						
						// Convert the minute value
						int minInt;
						if (minStr.length() == 0) {
							minInt = 0;
						} else {
							minInt = Integer.valueOf(minStr);
						}
						
						// Convert the second value
						int secInt;
						if (secStr.length() == 0) {
							secInt = 0;
						} else {
							secInt = Integer.valueOf(secStr);
						}
						
						// Send it through to the server
						int totalSeconds = minInt * 60 + secInt;
						
						Log.i(LOGTAG, "Adjusting time remaining: " + totalSeconds);
						if (totalSeconds > 0) {
							telnet.send("game.setTimeRemaining("+totalSeconds+")");
						} else {
							telnet.send("game.setTimeRemaining(None)");
						}
						update();
					}
				});
			alertDialog.setButton(-2, getApplicationContext().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			return alertDialog;
		}
		return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.refresh_only, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    
	        case android.R.id.home:
	            Intent intent = new Intent(this, DashboardActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	            
	        case R.id.refresh:
	        	update();
	        	Toast.makeText(this, "You feel refreshed.", Toast.LENGTH_SHORT).show();
	        	return true;
	        	
	        default:
	        	Log.i(LOGTAG, "Option selection fell through.");
	            return super.onOptionsItemSelected(item);
	    }
	}

}
