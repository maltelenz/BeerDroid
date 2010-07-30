package com.beerdroid.beta;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Provides an interface to the preferences in preferences.xml.
 * @author Malte Lenz
 *
 */
public class Preferences extends PreferenceActivity {

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
}