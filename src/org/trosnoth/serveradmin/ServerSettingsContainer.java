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

import greendroid.widget.ActionBarItem;
import greendroid.widget.LoaderActionBarItem;

import org.trosnoth.serveradmin.helpers.GDSingleTabActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ServerSettingsContainer extends GDSingleTabActivity {

	private Intent intent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().addItem(ActionBarItem.Type.Refresh);

		intent = new Intent(this, ServerSettingsActivity.class);
		showActivity(intent);

	}

	/**
	 * A consequence of our ugly hack is that we need another ugly hack to
	 * communicate with the activity
	 */
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (position) {
		case 0:
			Activity currentActivity = getLocalActivityManager().getActivity(
							getTabHost().getCurrentTabTag());
			((ServerSettingsActivity) currentActivity).update();
			((LoaderActionBarItem) item).setLoading(false);
			return true;
		default:
			return false;
		}
	}

}
