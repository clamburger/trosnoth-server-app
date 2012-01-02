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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.trosnoth.serveradmin.helpers.AutomatedTelnetClient;
import org.trosnoth.serveradmin.helpers.Upgrade;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class UpgradeActivity extends FragmentActivity {
	
	private static final String LOGTAG = "Upgrades";

	AutomatedTelnetClient telnet;

	ArrayList<Upgrade> upgrades = new ArrayList<Upgrade>();
	UpgradeAdapter adapter;
	JSONObject upgradeInfo;
	Handler mHandler = new Handler();
	
	ListView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upgrades);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		telnet = ConnectionActivity.telnet;
		adapter = new UpgradeAdapter(this, R.layout.upgrades_list, upgrades);
		
		list = (ListView) findViewById(R.id.list); 
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Toast.makeText(getApplicationContext(), "Not yet implemented.", Toast.LENGTH_SHORT).show();
			}
		});
		
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
		
		telnet.send("game = getGame()");
		
		String jsonString = telnet.readWrite("print json.dumps(getGame().getUpgrades())");
		
		upgrades.clear();
		
		try {
			
			JSONArray json = new JSONArray(jsonString);
			for (int i = 0; i < json.length(); i++) {
				upgrades.add(new Upgrade(json.getJSONObject(i)));				
			}
			
		} catch (JSONException e) {
			Log.e(LOGTAG, "JSONException! Cannot parse upgrade string!");
			Log.e(LOGTAG, jsonString);
			return;
		}
		
	}

	private class UpgradeAdapter extends ArrayAdapter<Upgrade> {

		private ArrayList<Upgrade> items;

		public UpgradeAdapter(Context context, int textViewResourceId, ArrayList<Upgrade> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.upgrades_list, null);
			}
			Upgrade upgrade = items.get(position);
			if (upgrade != null) {				
				
				ImageView icon = (ImageView) v.findViewById(R.id.icon);
				String drawableString = "drawable/" + upgrade.icon;
				int drawableId = getResources().getIdentifier(drawableString, null, getPackageName());
				Drawable image = getResources().getDrawable(drawableId);
				icon.setImageDrawable(image);
				
				if (upgrade.special) {
					icon.setBackgroundColor(getResources().getColor(R.color.upgrade_special));
				} else {
					icon.setBackgroundColor(getResources().getColor(android.R.color.white));
				}
				
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				tt.setText(upgrade.name);
				
				TextView textStars = (TextView) v.findViewById(R.id.textStars);
				textStars.setText(Integer.toString(upgrade.starCost));
				
				TextView textTime = (TextView) v.findViewById(R.id.textTime);
				textTime.setText(Integer.toString(upgrade.timeLimit));
			}
			return v;
		}
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
