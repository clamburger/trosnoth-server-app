package org.trosnoth.serveradmin;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class GameSelectorActivity extends Activity {
	
	protected static final String LOGTAG = "Trosnoth";

	AutomatedTelnetClient telnet;
	
	public static int selectedGame;
	
	private ListView list;
	private Button createGame;
	
	boolean currentState = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_selector);
        
        list = (ListView) findViewById(R.id.listGames);
        createGame = (Button) findViewById(R.id.buttonCreate);
        telnet = MainMenuActivity.telnet;
        
        createGame.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Log.i(LOGTAG, "Creating a new game...");
        		String result = telnet.readWrite("authfactory.createGame()");
        		if (result.startsWith("Traceback")) {
        			String[] stacktrace = result.split("\n");
        			Log.e(LOGTAG, "Python exception!");
        			Log.e(LOGTAG, stacktrace[stacktrace.length-1]);
        			makeToast("Game could not be created!");
        		} else {
        			Log.i(LOGTAG, "Game created.");
        			makeToast("Game successfully created.");
        		}
        		updateList();
        	}
        });
        
        updateList();
        
	}
	
	public void updateList() {
        
        int gameInt = Integer.parseInt(telnet.readWrite("len(authfactory.servers)"));
        
        if (gameInt == 0) {
        	String[] noGames = {"No games are currently running."};
        	list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, noGames));
        	list.setClickable(false);
        } else {
        	ArrayList<String> games = new ArrayList<String>();
        	for (int i = 0; i < gameInt; i++) {
        		games.add("Game " + i);
        	}
        	list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, games));
        	list.setOnItemClickListener(new OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        			selectedGame = position;
        			Log.i(LOGTAG, "Selected game: " + position);
        		}
			});
        }
        
	}
	
	public void makeToast(String message) {
    	Context context = getApplicationContext();
    	Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
    	toast.show();
    }

}
