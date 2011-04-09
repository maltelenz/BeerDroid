package com.beerdroid.beta;

import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

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
		
		Preference clearData = findPreference("pref_clear");
		clearData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearLocalDatabase();
				return true;
			}
		});
	}
	
	final void clearLocalDatabase() {
		getContentResolver().delete(Uri.parse("content://" + BeerProvider.AUTHORITY + "/clear_database"), null, null);
		Toast toast = Toast.makeText(getApplicationContext(), "Local databased cleared.", Toast.LENGTH_SHORT);
		toast.show();
	}
}