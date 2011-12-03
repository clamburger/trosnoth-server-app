package org.trosnoth.serveradmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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

public class PlayerActivity extends Activity {

	private static final String LOGTAG = "Trosnoth Players";

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

	String[] upgrades = { "Machine Gun", "Shield", "Minimap Disruption", "Ninja", "Grenade",
			"Ricochet", "Shoxwave", "Turret", "Phase Shift", "Respawn Freezer", "Directator" };
	String[] upgradeCodes = { "x", "s", "m", "n", "g", "r", "w", "t", "h", "f", "d" };

	HashMap<String, String> upgradeMapping = new HashMap<String, String>();

	ArrayList<String> players;

	private String currentPlayer = null;
	private Handler mHandler = new Handler();
	
	public void notYetImplemented() {
		Toast.makeText(getApplicationContext(), "Not yet implemented.", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.players);

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
				notYetImplemented();
				// telnet.send("game.kickPlayer(game.getPlayers()[\""+currentPlayer+"\"].id)");
				// update();
			}
		});

		giveUpgrade = (Button) findViewById(R.id.buttonGiveUpgrade);
		giveUpgrade.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int position = upgradeGallery.getSelectedItemPosition();
				telnet.send("getGame().giveUpgrade(getGame().getPlayers()[\"" + currentPlayer
								+ "\"], \"" + upgradeCodes[position] + "\")");
				Log.i(LOGTAG, "Giving upgrade (" + upgrades[position] + ") to " + currentPlayer);
				update();
			}
		});

		removeUpgrade = (Button) findViewById(R.id.buttonRemoveUpgrade);
		removeUpgrade.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				notYetImplemented();
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

		telnet = MainMenuActivity.telnet;

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

	private void hide() {

		upgradePane.setVisibility(View.INVISIBLE);
		controlPanel.setVisibility(View.INVISIBLE);

		playerName.setText("No player selected");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			textUsername.setText("Drag the bar from the right to select a player");
		} else {
			textUsername.setText("Drag the bar from the bottom to select a player");
		}

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

		// Get a list of players
		result = (String) telnet.parse(telnet
						.readWrite("'\\n'.join(getGame().getPlayers().keys())"));

		String[] playerArray = result.split("\\\\n");
		players = new ArrayList<String>(Arrays.asList(playerArray));
		Collections.sort(players, String.CASE_INSENSITIVE_ORDER);

		if (players.size() == 0) {
			listHelper.setText("There are currently no players on this server.");
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
		telnet.send("player = getGame().getPlayers()[\"" + currentPlayer + "\"]");

		playerName.setText(currentPlayer);

		// Username
		result = telnet.readWrite("player.user");
		if (result.length() == 0) {
			Boolean bot = (Boolean) telnet.parse(telnet.readWrite("player.bot"));
			if (bot) {
				textUsername.setText("Bot");
			} else {
				textUsername.setText("Not authenticated");
			}
		} else {
			result = (String) telnet.parse(telnet.readWrite("player.user.username"));
			textUsername.setText(result);
		}

		// Alive or dead
		Boolean dead = (Boolean) telnet.parse(telnet.readWrite("player.dead"));
		if (dead) {
			textAlive.setText("Dead");
			textAlive.setTextColor(res.getColor(R.color.light_red));
			textStars.setVisibility(View.INVISIBLE);
		} else {
			textAlive.setText("Alive");
			textAlive.setTextColor(res.getColor(R.color.light_green));
			textStars.setVisibility(View.VISIBLE);

			// Stars
			int stars = Integer.valueOf(telnet.readWrite("player.stars"));
			if (stars == 1) {
				textStars.setText("1 star");
			} else {
				textStars.setText(stars + " stars");
			}
		}

		// Team
		result = (String) telnet.parse(telnet.readWrite("player.team"));
		if (result.length() == 0) {
			textTeam.setText("Rogue");
			textTeam.setTextColor(res.getColor(R.color.neutral_text));
		} else {
			textTeam.setText("Unknown [" + result + "]");
			textTeam.setTextColor(res.getColor(android.R.color.white));
		}

		// Current upgrade
		result = telnet.readWrite("player.upgrade");
		if (result.length() == 0) {
			currentUpgrade.setText("No upgrade");
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
				R.drawable.upgrade_respawnfreeze, R.drawable.upgrade_blank };

		public ImageAdapter(Context c) {
			mContext = c;
			TypedArray attr = mContext.obtainStyledAttributes(R.styleable.upgrade_gallery);
			mGalleryItemBackground = attr.getResourceId(
							R.styleable.upgrade_gallery_android_galleryItemBackground, 0);
			attr.recycle();
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
			// imageView.setLayoutParams(new Gallery.LayoutParams(-2, -2));
			// imageView.setScaleType(ImageView.ScaleType.CENTER);
			// imageView.setBackgroundResource(mGalleryItemBackground);
			if (position <= 6) {
				imageView.setBackgroundColor(getResources().getColor(android.R.color.white));
			} else {
				imageView.setBackgroundColor(0xffd7e33c);
			}

			RelativeLayout borderImg = new RelativeLayout(mContext);
			borderImg.setPadding(4, 4, 4, 4);
			borderImg.setBackgroundColor(0xff000000);
			borderImg.addView(imageView);
			return borderImg;

			// return imageView;
		}
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
			startActivity(new Intent(this, MainMenuActivity.class).setFlags(
							Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(
							Intent.FLAG_ACTIVITY_SINGLE_TOP));
			return true;
		default:
			return super.onOptionsItemSelected(item);
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

}