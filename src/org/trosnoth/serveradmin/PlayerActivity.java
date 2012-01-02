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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.trosnoth.serveradmin.helpers.AutomatedTelnetClient;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends FragmentActivity {

	private static final String LOGTAG = "Players";

	AutomatedTelnetClient telnet;

	SlidingDrawer playerDrawer;
	ListView playerList;
	TextView listHelper;

	TextView playerName;
	TextView textUsername;
	TextView textTeam;
	TextView textStars;
	TextView textAlive;
	TextView currentUpgrade;

	Button kickPlayer;
	Button giveUpgrade;
	Button removeUpgrade;
	RelativeLayout controlPanel;

	Gallery upgradeGallery;
	TextView upgradeInstruction;
	TextView selectedUpgrade;
	RelativeLayout upgradePane;

	int[] upgrades = { R.string.upgrade_machine_gun, R.string.upgrade_shield,
					R.string.upgrade_minimap_disruption, R.string.upgrade_ninja,
					R.string.upgrade_grenade, R.string.upgrade_ricochet,
					R.string.upgrade_shoxwave, R.string.upgrade_turret,
					R.string.upgrade_phase_shift, R.string.upgrade_respawn_freezer,
					R.string.upgrade_directator };
	String[] upgradeCodes = { "x", "s", "m", "n", "g", "r", "w", "t", "h", "f", "d" };

	HashMap<String, Integer> upgradeMapping = new HashMap<String, Integer>();

	ArrayList<String> players;

	private String currentPlayer = null;
	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.players);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		for (int i = 0; i < upgrades.length; i++) {
			upgradeMapping.put(upgradeCodes[i], upgrades[i]);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey("currentPlayer")) {
			currentPlayer = savedInstanceState.getString("currentPlayer");
		}

		// We keep the drawer behind everything else in the layout editor so it
		// doesn't get in the way
		playerDrawer = (SlidingDrawer) findViewById(R.id.drawerPlayers);
		playerDrawer.bringToFront();

		playerName = (TextView) findViewById(R.id.textPlayerName);
		textUsername = (TextView) findViewById(R.id.textUsername);
		textTeam = (TextView) findViewById(R.id.textTeam);
		textStars = (TextView) findViewById(R.id.textStars);
		textAlive = (TextView) findViewById(R.id.textAlive);
		currentUpgrade = (TextView) findViewById(R.id.textUpgrade);
		selectedUpgrade = (TextView) findViewById(R.id.textSelectedUpgrade);
		upgradeInstruction = (TextView) findViewById(R.id.textUpgradeInstruction);
		listHelper = (TextView) findViewById(R.id.textListHelper);
		listHelper.setVisibility(View.VISIBLE);
		listHelper.setText("Loading...");

		kickPlayer = (Button) findViewById(R.id.buttonKickPlayer);
		kickPlayer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				telnet.send("game.kickPlayer(game.getPlayers()["+currentPlayer()+"].id)");
				update();
			}
		});

		giveUpgrade = (Button) findViewById(R.id.buttonGiveUpgrade);
		giveUpgrade.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int position = upgradeGallery.getSelectedItemPosition();
				telnet.send("getGame().giveUpgrade(getGame().getPlayers()[" + currentPlayer()
								+ "], \"" + upgradeCodes[position] + "\")");
				Context context = getApplicationContext();
				Log.i(LOGTAG, "Giving upgrade (" + context.getString(upgrades[position]) + ") to " + currentPlayer);
				update();
			}
		});

		removeUpgrade = (Button) findViewById(R.id.buttonRemoveUpgrade);
		removeUpgrade.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				telnet.send("getGame().removeUpgrade(getGame().getPlayers()[" + currentPlayer() + "])");
				Log.i(LOGTAG, "Removing upgrade from " + currentPlayer);
				update();
			}
		});

		playerList = (ListView) findViewById(R.id.listPlayers);
		playerList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				currentPlayer = (String) playerList.getItemAtPosition(position);
				Log.i(LOGTAG, "Selecting player: " + currentPlayer);
				update();
				playerDrawer.animateClose();
			}
		});
		playerList.setVisibility(View.INVISIBLE);

		controlPanel = (RelativeLayout) findViewById(R.id.layoutControls);
		upgradePane = (RelativeLayout) findViewById(R.id.layoutUpgrades);

		upgradeGallery = (Gallery) findViewById(R.id.galleryUpgrades);
		upgradeGallery.setAdapter(new ImageAdapter(this));

		upgradeGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedUpgrade.setText(upgrades[position]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		telnet = ConnectionActivity.telnet;

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

	private void hide() {

		upgradePane.setVisibility(View.INVISIBLE);
		controlPanel.setVisibility(View.INVISIBLE);

		playerName.setText(R.string.players_none_selected);
		textUsername.setText(R.string.players_select_player);

		textTeam.setVisibility(View.INVISIBLE);
		textAlive.setVisibility(View.INVISIBLE);
		textStars.setVisibility(View.INVISIBLE);
		currentUpgrade.setVisibility(View.INVISIBLE);

	}

	private void show() {

		upgradePane.setVisibility(View.VISIBLE);
		controlPanel.setVisibility(View.VISIBLE);

		playerName.setText("");
		textUsername.setText("");

		textTeam.setVisibility(View.VISIBLE);
		textAlive.setVisibility(View.VISIBLE);
		textStars.setVisibility(View.VISIBLE);
		currentUpgrade.setVisibility(View.VISIBLE);

	}

	private void update() {

		String result;
		ArrayList<String> players;
		
		players = telnet.parseJSON(telnet.readWrite("print json.dumps(getGame().getPlayers().keys())"));
		Collections.sort(players, String.CASE_INSENSITIVE_ORDER);

		if (players.size() == 0) {
			listHelper.setText(R.string.players_no_players);
			playerList.setVisibility(View.INVISIBLE);
			listHelper.setVisibility(View.VISIBLE);
		} else {
			playerList.setAdapter(new ArrayAdapter<String>(this,
							android.R.layout.simple_list_item_1, players));
			playerList.setVisibility(View.VISIBLE);
			listHelper.setVisibility(View.INVISIBLE);
		}

		if (currentPlayer == null || !players.contains(currentPlayer)) {
			currentPlayer = null;
			hide();
			return;
		} else {
			show();
		}

		Resources res = getResources();

		// This could very easily lead to race conditions
		telnet.send("player = getGame().getPlayers()[" + currentPlayer() + "]");

		playerName.setText(currentPlayer);

		// Username
		result = telnet.readWrite("player.user");
		if (result.length() == 0) {
			Boolean bot = (Boolean) telnet.parse(telnet.readWrite("player.bot"));
			if (bot) {
				textUsername.setText(R.string.players_bot);
			} else {
				textUsername.setText(R.string.players_no_username);
			}
		} else {
			result = (String) telnet.parse(telnet.readWrite("player.user.username"));
			textUsername.setText(result);
		}

		// Alive or dead
		Boolean dead = (Boolean) telnet.parse(telnet.readWrite("player.dead"));
		if (dead) {
			textAlive.setText(R.string.players_dead);
			textAlive.setTextColor(res.getColor(R.color.light_red));
			textStars.setVisibility(View.INVISIBLE);
		} else {
			textAlive.setText(R.string.players_alive);
			textAlive.setTextColor(res.getColor(R.color.light_green));
			textStars.setVisibility(View.VISIBLE);

			// Stars
			int stars = Integer.valueOf(telnet.readWrite("player.stars"));
			if (stars == 1) {
				textStars.setText(R.string.players_1_star);
			} else {
				textStars.setText(getApplicationContext().getString(R.string.players_stars, stars));
			}
		}

		// Team
		result = telnet.readWrite("player.team");
		if (result.length() == 0) {
			textTeam.setText(R.string.teams_none);
			textTeam.setTextColor(res.getColor(R.color.neutral_text));
		} else {
			result = (String) telnet.parse(telnet.readWrite("player.team.id"));
			if (result.equals("A")) {
				textTeam.setText(R.string.teams_blue);
				textTeam.setTextColor(res.getColor(R.color.blue_text));
			} else if (result.equals("B")) {
				textTeam.setText(R.string.teams_red);
				textTeam.setTextColor(res.getColor(R.color.red_text));
			} else {
				textTeam.setText(R.string.unknown);
				textTeam.setTextColor(res.getColor(android.R.color.white));
			}
		}

		// Current upgrade
		result = telnet.readWrite("player.upgrade");
		if (result.length() == 0) {
			currentUpgrade.setText(R.string.players_no_upgrade);
			giveUpgrade.setEnabled(true);
			removeUpgrade.setEnabled(false);
		} else {
			result = (String) telnet.parse(telnet.readWrite("player.upgrade.upgradeType"));
			currentUpgrade.setText(upgradeMapping.get(result));
			giveUpgrade.setEnabled(false);
			removeUpgrade.setEnabled(true);
		}

		// Log.i(LOGTAG, "Update complete.");

	}

	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;

		private Integer[] mImageIds = { R.drawable.upgrade_machinegun, R.drawable.upgrade_shield,
				R.drawable.upgrade_minimap, R.drawable.upgrade_ninja, R.drawable.upgrade_grenade,
				R.drawable.upgrade_ricochet, R.drawable.upgrade_shoxwave,
				R.drawable.upgrade_turret, R.drawable.upgrade_phaseshift,
				R.drawable.upgrade_freezer, R.drawable.upgrade_blank };

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return mImageIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);

			imageView.setImageResource(mImageIds[position]);
			if (position <= 6) {
				imageView.setBackgroundColor(getResources().getColor(android.R.color.white));
			} else {
				imageView.setBackgroundColor(getResources().getColor(R.color.upgrade_special));
			}

			RelativeLayout borderImg = new RelativeLayout(mContext);
			borderImg.setPadding(4, 4, 4, 4);
			borderImg.setBackgroundColor(0xff000000);
			borderImg.addView(imageView);
			return borderImg;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("currentPlayer", currentPlayer);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentPlayer = savedInstanceState.getString("currentPlayer");
	}
	
	private String currentPlayer() {
		String player = currentPlayer;
		player = player.replace("\"", "\\\"");
		return "\"" + player + "\"";
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.players, menu);
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
	            
	        case R.id.players:
	        	playerDrawer.animateToggle();
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
