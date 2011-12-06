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


import greendroid.app.GDApplication;

public class TrosnothApplication extends GDApplication {

	private String serverIP = null;
	
	public String getServer() {
		return serverIP;
	}
	
	public void setServer(String server) {
		serverIP = server;
	}
	
	@Override
	public Class<?> getHomeActivityClass() {
		return DashboardActivity.class;
	}
}