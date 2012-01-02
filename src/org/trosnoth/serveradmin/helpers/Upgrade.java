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
