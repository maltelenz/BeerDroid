package com.beerdroid.beta;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class BeerDetails extends Activity {

	private Beer beer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final Bundle extras = getIntent().getExtras();
		final Long id = extras.getLong("id");
		beer = BeerDroid.resultList.get(id.intValue());

		setContentView(R.layout.beer_details);

		final TextView nameView = (TextView) findViewById(R.id.beer_details_name);
		nameView.setText(beer.getName());

		final TextView breweryView = (TextView) findViewById(R.id.beer_details_brewery);
		breweryView.setText(beer.getBreweryName());

		final TextView styleView = (TextView) findViewById(R.id.beer_details_style);
		styleView.setText(beer.getStyle());

		final TextView baRatingView = (TextView) findViewById(R.id.beer_details_ba_rating);
		baRatingView.setText(beer.getBaRating());

		final TextView systemetSizeView = (TextView) findViewById(R.id.beer_details_systemet_size);
		systemetSizeView.setText(beer.getSystemetSize().toString());

		final TextView systemetPriceView = (TextView) findViewById(R.id.beer_details_systemet_price);
		systemetPriceView.setText(beer.getSystemetPrice());

		super.onCreate(savedInstanceState);
	}


}
