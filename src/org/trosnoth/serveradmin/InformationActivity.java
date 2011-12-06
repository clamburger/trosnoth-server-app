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

import greendroid.app.GDActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class InformationActivity extends GDActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.information);
	}
	
    public void onAppUrlClicked(View v) {
        final Uri appUri = Uri.parse(getString(R.string.information_url));
        startActivity(new Intent(Intent.ACTION_VIEW, appUri));
    }
    
    public void onGreenDroidUrlClicked(View v) {
        final Uri appUri = Uri.parse(getString(R.string.information_greendroid_url));
        startActivity(new Intent(Intent.ACTION_VIEW, appUri));
    }
    

}
