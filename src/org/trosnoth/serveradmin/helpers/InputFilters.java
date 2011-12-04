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
