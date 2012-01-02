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
package org.trosnoth.serveradmin.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.commons.net.telnet.TelnetClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Environment;
import android.util.Log;

public class AutomatedTelnetClient {
	private static final String LOGTAG = "Telnet";
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	private String prompt = ">";

	private String password;

	public AutomatedTelnetClient(String server, int port, String password) throws IOException {
		this.password = password;

		telnet.connect(server, port);
	}

	public boolean initalise() {

		// Get input and output stream references
		in = telnet.getInputStream();
		out = new PrintStream(telnet.getOutputStream());

		// Log the user on
		readUntil("username: ");
		write("trosnoth");
		readUntil("password: ");
		write(password);

		// Advance to a prompt
		readUntil("\r\n");

		int oneChar = 0;

		try {
			oneChar = in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (oneChar == -1) {
			return false;
		}

		readAll();

		return true;
	}

	public ArrayList<String> parseJSON(String jsonString) {
		JSONArray json = null;

		ArrayList<String> result = new ArrayList<String>();
		try {
			json = new JSONArray(jsonString);
			for (int i = 0; i < json.length(); i++) {
				result.add(json.getString(i));
			}
		} catch (JSONException e) {
			Log.e(LOGTAG, "Cannot parse invalid JSON!");
			Log.e(LOGTAG, jsonString);
			return new ArrayList<String>();
		}

		return result;
	}

	public Object parse(String result) {
		if (result.length() == 0) {
			return "";
		} else if (result.equals("True")) {
			return true;
		} else if (result.equals("False")) {
			return false;
		} else if (result.charAt(0) == '\'' || result.charAt(0) == '"') {
			return result.substring(1, result.length() - 1);
		} else if (result.charAt(0) == 'u') {
			return result.substring(2, result.length() - 1);
		} else if (result.charAt(0) == '[') {
			Log.e(LOGTAG, "Cannot parse a list!");
			return null;
		} else if (result.charAt(0) == '(') {
			Log.e(LOGTAG, "Cannot parse a tuple!");
			return null;
		} else if (result.charAt(0) == '<') {
			Log.e(LOGTAG, "Cannot parse an object!");
			return null;
		} else {
			Double.valueOf(result);
		}
		return null;
	}

	public String readWrite(String command)  {
		// Send the command to the server

		// Log.v("Telnet", "Sending: " + command);

		String result = null;
		
		write(command);

		// Read until the line break
		// String result = readUntil("\r\n").trim();
		result = readUntil(">>>");

		result = result.substring(0, result.length() - 3).trim();
		// result = result.trim();

		// Log.v("Telnet", "Received: " + result);

		// Flush any remaining text (including the next prompt)
		readAll();

		// Log.v("Telnet", "Flush successful");

		return result;
	}
	
	public void send(String command) {

		// Log.v("Telnet", "Sending: " + command);

		write(command);

		readAll();
	}

	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (true) {
				sb.append(ch);

				if (sb.length() > 2000) {
					File dir = Environment.getExternalStorageDirectory();
					dir = new File(dir.getAbsolutePath() + "/trosnoth");
					dir.mkdir();
					BufferedWriter out = new BufferedWriter(new FileWriter(dir.getAbsolutePath()
									+ "/fail" + System.currentTimeMillis() + ".log"));
					out.write(sb.toString());
					out.close();
					Log.e(LOGTAG, "Response exceeded 2,000 characters.");
					return null;
				}

				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String readAll() {

		StringBuffer sb = new StringBuffer();
		char ch;

		try {
			Thread.sleep(100);
			while (in.available() > 0) {
				ch = (char) in.read();
				sb.append(ch);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public void write(String value) {
		try {
			out.print(value + "\r\n");
			out.flush();
			telnet.getOutputStream().flush();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) {
		try {
			write(command);
			return readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
