package com.beerdroid.beta;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class BeerDetails extends Activity {

	private Beer beer;
	private static final String TAG = "BeerDetails";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final Bundle extras = getIntent().getExtras();
		final Long id = extras.getLong("id");
		beer = BeerDroid.resultList.get(id.intValue());
		
		setContentView(R.layout.beer_details);
		
		if (beer.breweryName != null) {
			final TextView nameView = (TextView) findViewById(R.id.beer_details_name);
			nameView.setText(beer.name);
		}

		if (beer.breweryName != null) {
			final TextView breweryView = (TextView) findViewById(R.id.beer_details_brewery);
			breweryView.setText(beer.breweryName);
		}
		
		if (beer.style != null) {
			final TextView styleView = (TextView) findViewById(R.id.beer_details_style);
			styleView.setText(beer.style);
		}

		if (beer.baRating != null) {
			final TextView baRatingView = (TextView) findViewById(R.id.beer_details_ba_rating);
			baRatingView.setText(beer.baRating);
		}
		
		if (beer.systemetSize != null) {
			final TextView systemetSizeView = (TextView) findViewById(R.id.beer_details_systemet_size);
			Log.d(TAG , "beer.systemet_size = " + beer.systemetSize);
			systemetSizeView.setText(beer.systemetSize.toString());
		}
		
		if (beer.systemetPrice != null) {
			final TextView systemetPriceView = (TextView) findViewById(R.id.beer_details_systemet_price);
			systemetPriceView.setText(beer.systemetPrice.toString());
		}

		super.onCreate(savedInstanceState);
	}

	
}
