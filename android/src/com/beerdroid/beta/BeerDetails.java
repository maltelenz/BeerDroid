package com.beerdroid.beta;

import java.util.Hashtable;

import com.beerdroid.provider.BeerProvider;
import com.beerdroid.provider.Beers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Shows details about a given beer.
 * @author Malte Lenz
 *
 */
public class BeerDetails extends Activity {
	private static final String TAG = "BeerDetails";
	private Beer beer;
	
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		//fetch the beer object
		final String beerId = intent.getStringExtra(Beers.BEERADVOCATE_BEER_ID);
		Uri beerUri = Uri.parse(BeerProvider.CONTENT_URI.toString() + "?limit=1");
		Cursor c = managedQuery(beerUri, Beers.ALL_COLUMNS, Beers.BEERADVOCATE_BEER_ID + " = ?", new String[] {beerId}, null);
		if (!c.moveToFirst()) {
			Log.e(TAG, "No data available in cursor");
		} else {
			beer = new Beer(c);
		}
		c.close();
		
		setContentView(R.layout.beer_details);

		//populate all fields
		final TextView nameView = (TextView) findViewById(R.id.beer_details_name);
		nameView.setText(beer.getName());

		final TextView breweryView = (TextView) findViewById(R.id.beer_details_brewery);
		breweryView.setText(beer.getBreweryName());

		final TextView styleView = (TextView) findViewById(R.id.beer_details_style);
		styleView.setText(beer.getStyle());

		final TextView abvView = (TextView) findViewById(R.id.beer_details_abv);
		abvView.setText(beer.getAbv() + " %");

		final TextView baRatingView = (TextView) findViewById(R.id.beer_details_ba_rating);
		baRatingView.setText(beer.getBaRating());

		final TextView systemetSizeView = (TextView) findViewById(R.id.beer_details_systemet_size);
		systemetSizeView.setText(beer.getSystemetSize().toString());

		final TextView systemetPriceView = (TextView) findViewById(R.id.beer_details_systemet_price);
		systemetPriceView.setText(beer.getSystemetPrice());

		if (beer.systemetAvailabilityList != null && beer.systemetAvailabilityList.size() != 0) {
			final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			LinearLayout list = (LinearLayout) findViewById(R.id.beer_details_systemet_available_list);
			if (beer.systemetAvailabilityList.size() != 0) {
				TextView listHeader = (TextView) vi.inflate(R.layout.text_view_header, null);
				listHeader.setText(getResources().getString(R.string.systemet_availability_header) + " " + beer.county + ":");
				list.addView(listHeader);
			}
			for (int i = 0; i < beer.systemetAvailabilityList.size(); i++) {

				final Hashtable<String, String> s = beer.systemetAvailabilityList.get(i);
				View v = vi.inflate(R.layout.systemet_available_list_item, null);

				if (s != null) {
					final TextView sname = (TextView) v.findViewById(R.id.systemet_available_store);
					final TextView snr = (TextView) v.findViewById(R.id.systemet_available_nr);
					if (sname != null) {
						sname.setText(s.get(Beer.SYSTEMET_STORE_KEY));
					}
					if (snr != null) {
						snr.setText(s.get(Beer.SYSTEMET_STORE_NR_KEY));
					}
				}
				list.addView(v);
			}
		}

		//make beeradvocate button clickable if we have their ids
		final Button beerAdvocateButton = (Button) findViewById(R.id.ba_button);
		if (beer.baBrewery != null && beer.baBeer != null) {
			beerAdvocateButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					//create and start a browser intent
					final Intent showBeerAdvocateIntent = new Intent();
					showBeerAdvocateIntent.setAction(Intent.ACTION_VIEW);
					showBeerAdvocateIntent.setData(
							Uri.parse(Config.beerAdvocateBaseUrl + Config.beerAdvocateBeerUrl + beer.baBrewery + "/" + beer.baBeer)
					);
					startActivity(showBeerAdvocateIntent);
				}
			});
		} else {
			beerAdvocateButton.setEnabled(false);
		}
	}
	
	/**
	 * Creates the menu called by the menu button.
	 * @param menu the menu to modify
	 * @return the menu
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.default_menu, menu);
		return true;
	}

	/**
	 * Called when the user has pressed a menu item.
	 * @param item the clicked item
	 * @return if the click was handled
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			//Show the search overlay
			onSearchRequested();
			return true;
		case R.id.preferences:
			//Start the preferences activity
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
		
}
