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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.trosnoth.serveradmin.helpers.AutomatedTelnetClient;
import org.trosnoth.serveradmin.helpers.InputFilters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

public class ServerSettingsActivity extends PreferenceActivity implements
				OnSharedPreferenceChangeListener {

	private static final String LOGTAG = "ServerSettings";

	AutomatedTelnetClient telnet;

	ArrayList<String> gameModes;

	private ServerSettings pref;

	public class ServerSettings implements SharedPreferences {

		public void update() {

			String gameSpeed = telnet.readWrite("float(getGame().getSpeed())");

			/* For some mysterious reason, just doing it as above doesn't work. */
			String gameMode = (String) telnet.parse(telnet.readWrite("getGame().getGameMode()"));

			// String playersPerTeam = null;
			// String playersTotal = null;

			values.put("gameSpeed", gameSpeed);
			values.put("gameMode", gameMode);
			// values.put("playersPerTeam" , playersPerTeam);
			// values.put("playersTotal", playersTotal);

		}

		protected Map<String, String> values = new HashMap<String, String>();

		public boolean contains(String key) {
			return values.containsKey(key);
		}

		public Editor edit() {
			return new Editor();
		}

		public Map<String, ?> getAll() {
			return values;
		}

		public boolean getBoolean(String key, boolean defValue) {
			return Boolean.valueOf(this.getString(key, Boolean.toString(defValue)));
		}

		public float getFloat(String key, float defValue) {
			return Float.valueOf(this.getString(key, Float.toString(defValue)));
		}

		public int getInt(String key, int defValue) {
			return Integer.valueOf(this.getString(key, Integer.toString(defValue)));
		}

		public long getLong(String key, long defValue) {
			return Long.valueOf(this.getString(key, Long.toString(defValue)));
		}

		public String getString(String key, String defValue) {
			if (!values.containsKey(key)) {
				return defValue;
			}
			return values.get(key);
		}

		protected List<OnSharedPreferenceChangeListener> listeners = new LinkedList<OnSharedPreferenceChangeListener>();

		public void registerOnSharedPreferenceChangeListener(
						OnSharedPreferenceChangeListener listener) {
			listeners.add(listener);
		}

		public void unregisterOnSharedPreferenceChangeListener(
						OnSharedPreferenceChangeListener listener) {
			listeners.remove(listener);
		}

		public class Editor implements SharedPreferences.Editor {

			public void apply() {
				commit();
			}

			public android.content.SharedPreferences.Editor clear() {
				return this;
			}

			public boolean commit() {
				return true;
			}

			public android.content.SharedPreferences.Editor putBoolean(String key, boolean value) {
				return this.putString(key, Boolean.toString(value));
			}

			public android.content.SharedPreferences.Editor putFloat(String key, float value) {
				return this.putString(key, Float.toString(value));
			}

			public android.content.SharedPreferences.Editor putInt(String key, int value) {
				return this.putString(key, Integer.toString(value));
			}

			public android.content.SharedPreferences.Editor putLong(String key, long value) {
				return this.putString(key, Long.toString(value));
			}

			public android.content.SharedPreferences.Editor putString(String key, String value) {
				Log.i(LOGTAG, "Updating " + key + " with " + value);
				values.put(key, value);

				if (key.equals("gameMode")) {
					telnet.send("getGame().setGameMode(\"" + value + "\")");
				} else if (key.equals("gameSpeed")) {
					telnet.send("getGame().setSpeed(" + value + ")");
				} else if (key.equals("playersPerTeam")) {
					telnet.send("getGame().setPlayerLimits(" + value + ")");
				}

				for (OnSharedPreferenceChangeListener listener : listeners) {
					listener.onSharedPreferenceChanged(ServerSettings.this, key);
				}

				return this;
			}

			public android.content.SharedPreferences.Editor remove(String key) {
				values.remove(key);
				return this;
			}

		}

	}

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		telnet = ConnectionActivity.telnet;
		telnet.send("game = getGame()");

		pref = new ServerSettings();
		pref.registerOnSharedPreferenceChangeListener(this);

		addPreferencesFromResource(R.xml.server_settings);

		gameModes = telnet.parseJSON(telnet
						.readWrite("print json.dumps(getGame().listGameModes())"));
		ListPreference gameModesPref = (ListPreference) this.findPreference("gameMode");

		String[] strArray = new String[gameModes.size()];
		gameModes.toArray(strArray);

		gameModesPref.setEntries(strArray);
		gameModesPref.setEntryValues(strArray);

		EditTextPreference setting;
		EditText settingText;

		setting = (EditTextPreference) findPreference("playersPerTeam");
		settingText = setting.getEditText();
		settingText.setInputType(InputType.TYPE_CLASS_NUMBER);
		settingText.setFilters(InputFilters.integerFilter(255));

		setting = (EditTextPreference) findPreference("playersTotal");
		settingText = setting.getEditText();
		settingText.setInputType(InputType.TYPE_CLASS_NUMBER);
		settingText.setFilters(InputFilters.integerFilter(255));

		setting = (EditTextPreference) findPreference("gameSpeed");
		settingText = setting.getEditText();
		settingText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

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
		pref.update();
		updateSummaries();
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return this.pref;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummaries();
	}

	public void updateSummaries() {
		Preference setting;

		Context context = getApplicationContext();

		setting = findPreference("playersPerTeam");
		// setting.setSummary(context.getString(R.string.settings_limit,
		// pref.getString("playersPerTeam",
		// context.getString(R.string.unknown))));
		setting.setSummary(R.string.settings_not_available);

		setting = findPreference("playersTotal");
		// setting.setSummary(context.getString(R.string.settings_limit,
		// pref.getString("playersTotal",
		// context.getString(R.string.unknown))));
		setting.setSummary(R.string.not_yet_implemented);

		setting = findPreference("gameMode");
		setting.setSummary(pref.getString("gameMode", context.getString(R.string.unknown)));

		setting = findPreference("gameSpeed");
		setting.setSummary(context.getString(R.string.settings_speed,
						pref.getString("gameSpeed", "?")));

	}

}
