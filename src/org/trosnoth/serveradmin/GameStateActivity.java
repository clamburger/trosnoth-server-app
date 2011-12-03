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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameStateActivity extends Activity {

	private static final String LOGTAG = "Trosnoth GameState";

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
				String[] teams = { "draw", "blue", "red" };
				String team = teams[winner.getSelectedItemPosition()];
				Log.i(LOGTAG, "Ending game: " + team);
				telnet.send("game.endGame(\"" + team + "\")");
				update();
			}
		});

		resetGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Not yet implemented.", Toast.LENGTH_LONG)
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

		telnet = MainMenuActivity.telnet;
		telnet.send("game = getGame()");

		// Update every 5 seconds
		Runnable looper = new Runnable() {
			public void run() {
				update();				
				mHandler.postDelayed(this, 3000);
			}
		};

		mHandler.removeCallbacks(looper);
		mHandler.post(looper);
	}

	public void update() {

		String result;
		int intResult;

		// Get game state
		result = telnet.readWrite("game.getGameState()");
		result = (String) telnet.parse(result);

		Resources res = getResources();

		if (result.equals("P")) {
			gameState.setText("Pre-game");
			gameState.setTextColor(res.getColor(R.color.light_yellow));
			startGame.setEnabled(true);
			// skipCountdown.setEnabled(true);
			endGame.setEnabled(false);
			winner.setEnabled(false);
		} else if (result.equals("I")) {
			gameState.setText("Active");
			gameState.setTextColor(res.getColor(R.color.light_green));
			startGame.setEnabled(false);
			skipCountdown.setEnabled(false);
			endGame.setEnabled(true);
			winner.setEnabled(true);
			changeTime.setEnabled(true);
		} else if (result.equals("B")) {
			gameState.setText("Blue wins!");
			gameState.setTextColor(res.getColor(R.color.blue_text));
		} else if (result.equals("R")) {
			gameState.setText("Red wins!");
			gameState.setTextColor(res.getColor(R.color.red_text));
		} else if (result.equals("D")) {
			gameState.setText("It's a draw!");
			gameState.setTextColor(res.getColor(R.color.neutral_text));
		} else {
			gameState.setText("Unknown [" + result + "]");
			gameState.setTextColor(res.getColor(R.color.disabled));
		}

		if (!result.equals("P") && !result.equals("I")) {
			startGame.setEnabled(false);
			skipCountdown.setEnabled(false);
			endGame.setEnabled(false);
			winner.setEnabled(false);
		}
		
		if (!result.equals("I")) {
			changeTime.setEnabled(false);
		}

		resetGame.setEnabled(true);

		// Time remaining
		
		//result = telnet.readWrite("game.world.getTimeLeft()");
		result = "";
		if (result.length() == 0) {
			timeLeft.setText("--:--");
			timeLeft.setTextColor(res.getColor(R.color.neutral_text));
		} else if (result.startsWith("Traceback")) {
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

			InputFilter[] filters = new InputFilter[1];
			filters[0] = new InputFilter() {
				public CharSequence filter(CharSequence source, int start, int end,
								Spanned dest, int dstart, int dend) {
					if (end > start) {
						String destTxt = dest.toString();
						String resultingTxt = destTxt.substring(0, dstart)
										+ source.subSequence(start, end)
										+ destTxt.substring(dend);
						if (!resultingTxt.matches("^\\d{1,2}?")) {
							return "";
						}
						if (Integer.valueOf(resultingTxt) > 59) {
							return "";
						}
					}
					return null;
				}

			};
			final EditText secs = (EditText) layout.findViewById(R.id.editSeconds);
			secs.setFilters(filters);
			
			final EditText mins = (EditText) layout.findViewById(R.id.editMinutes);

			builder = new AlertDialog.Builder(mContext);
			builder.setView(layout);
			alertDialog = builder.create();
			alertDialog.setTitle("Enter the new time remaining");
			alertDialog.setButton(-1, "Save",
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
			alertDialog.setButton(-2, "Cancel",
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
	    inflater.inflate(R.menu.standard, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {
		  case R.id.refresh:
		    update();
		    return true;
		  case R.id.disconnect:
			  startActivity(new Intent(this, MainMenuActivity.class)
			  .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			  .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
		    return true;
		  default:
		    return super.onOptionsItemSelected(item);
	  }
	}
}
