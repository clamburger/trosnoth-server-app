package org.trosnoth.serveradmin.helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class Upgrade {
    
    public String id;
    public String name;
    public int starCost;
    public int timeLimit;
    public String icon;
    public Boolean special;
    
    public Upgrade(JSONObject json) throws JSONException {
    	id = json.getString("id");
    	name = json.getString("name");
    	starCost = json.getInt("starCost");
    	timeLimit = json.getInt("timeLimit");
    	icon = json.getString("icon");
    	special = json.getBoolean("special");
    }
    
}