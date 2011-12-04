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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

	private Button buttonConnect;
	private EditText editServerIP;
	private EditText editPassword;

	private static final String LOGTAG = "Trosnoth Main";
	private static final String PREFS_NAME = "trosnoth";

	private SharedPreferences settings;

	public static AutomatedTelnetClient telnet;

	public static String serverIP;
	public static Boolean automaticUpdate;
	public static int UPDATE_FREQ = 5000;

	private String errorMessage = "An error occurred while displaying this error.";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);

		buttonConnect = (Button) findViewById(R.id.buttonConnect);
		editServerIP = (EditText) findViewById(R.id.editServerIP);
		editPassword = (EditText) findViewById(R.id.editPassword);

		buttonConnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				connect(editServerIP.getText().toString(), 6799, editPassword.getText().toString());
			}
		});

		// Input filter for the IP address EditText.
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
							int dstart, int dend) {
				if (end > start) {
					String destTxt = dest.toString();
					String resultingTxt = destTxt.substring(0, dstart)
									+ source.subSequence(start, end) + destTxt.substring(dend);
					if (!resultingTxt
									.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
						return "";
					} else {
						String[] splits = resultingTxt.split("\\.");
						for (int i = 0; i < splits.length; i++) {
							if (Integer.valueOf(splits[i]) > 255) {
								return "";
							}
						}
					}
				}
				return null;
			}

		};
		editServerIP.setFilters(filters);

		settings = getSharedPreferences(PREFS_NAME, 0);
		editServerIP.setText(settings.getString("serverIP", ""));
		editPassword.setText(settings.getString("password", ""));
		automaticUpdate = settings.getBoolean("autoUpdate", true);
	}

	public void readLine(BufferedReader r) throws IOException {
		String output;
		output = r.readLine();
		Log.i(LOGTAG, output);
	}

	public void errorMessage(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// private ProgressDialog dialog;

	public void connect(String ip, int port, String password) {

		serverIP = ip;
		Log.i(LOGTAG, "Connecting to " + ip + ":" + port + "...");

		// Doesn't work; too much effort for too little reward at the moment
		// dialog = ProgressDialog.show(this, "", "Connecting...", true, true);

		try {
			telnet = new AutomatedTelnetClient(ip, port, password);
			// dialog.hide();
		} catch (ConnectException e) {
			errorMessage = "Connection refused. Make sure you have network access and have entered the correct IP.\n"
							+ e.getClass().getCanonicalName();
		} catch (SocketTimeoutException e) {
			errorMessage = "The connection timed out. There may not be a Trosnoth server running at the specified IP.\n"
							+ e.getClass().getCanonicalName();
		} catch (IOException e) {
			errorMessage = "The server at " + ip + " could not be contacted.\n"
							+ "Ensure you entered the IP correctly.\n"
							+ e.getClass().getCanonicalName();
		}

		if (telnet == null) {
			Log.e(LOGTAG, "Connection error!");
			showDialog(666);
			return;
		}

		if (!telnet.initalise()) {
			errorMessage = "Incorrect password.";
			showDialog(666);
			return;
		}

		Log.i(LOGTAG, "Connection established.");

		int gameInt = Integer.parseInt(telnet.readWrite("len(authfactory.servers)"));

		if (gameInt == 0) {
			showDialog(1337);
		} else {
			nextMenu();
		}

	}

	public void makeToast(String message) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.show();
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (id == 1337) {
			builder.setMessage(
							"There are currently no games running on this server.\n"
											+ "Would you like to create one?")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
									telnet.send("authfactory.createGame()");
									nextMenu();
								}
							}).setNegativeButton("No", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
		} else if (id == 666) {
			builder.setMessage(errorMessage).setPositiveButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
		}
		dialog = builder.create();
		return dialog;
	}

	private void nextMenu() {
		Intent intent = new Intent(MainMenuActivity.this, ScreenSelectorActivity.class);
		intent.putExtra("serverIP", serverIP);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences.Editor editor = settings.edit();

		switch (item.getItemId()) {
		case R.id.saveIP:
			editor.putString("serverIP", editServerIP.getText().toString());
			editor.commit();
			return true;
		case R.id.savePassword:
			editor.putString("password", editPassword.getText().toString());
			editor.commit();
			return true;
		case R.id.toggleUpdates:
			automaticUpdate = !automaticUpdate;
			editor.putBoolean("autoUpdate", automaticUpdate);
			makeToast("Automatic updates: " + Boolean.toString(automaticUpdate));
			editor.commit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
