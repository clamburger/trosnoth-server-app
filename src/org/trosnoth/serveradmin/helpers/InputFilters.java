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
package org.trosnoth.serveradmin.helpers;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilters {

	public static InputFilter[] integerFilter(final int maxValue) {
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
							int dstart, int dend) {
				if (end > start) {
					String destTxt = dest.toString();
					String resultingTxt = destTxt.substring(0, dstart)
									+ source.subSequence(start, end) + destTxt.substring(dend);
					if (!resultingTxt.matches("^\\d*")) {
						return "";
					}
					if (maxValue > 0 && Integer.valueOf(resultingTxt) > maxValue) {
						return "";
					}
				}
				return null;
			}

		};
		
		return filters;
	}

}
