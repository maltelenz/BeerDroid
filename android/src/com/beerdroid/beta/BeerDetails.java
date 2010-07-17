package com.beerdroid.beta;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BeerDetails extends Activity {

	private Beer beer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//fetch the beer object
		final Bundle extras = getIntent().getExtras();
		final Long id = extras.getLong("id");
		beer = BeerDroid.resultList.get(id.intValue());

		setContentView(R.layout.beer_details);
		
		//populate all fields
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

		//make beeradvocate button clickable if we have their ids
		final Button beerAdvocateButton = (Button) findViewById(R.id.ba_button);
		beerAdvocateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//create and start a browser intent
				final Intent showBeerAdvocateIntent = new Intent();
				showBeerAdvocateIntent.setAction(Intent.ACTION_VIEW);
				showBeerAdvocateIntent.setData(
						Uri.parse(Config.beerAdvocateBaseUrl + Config.beerAdvocateBeerUrl + beer.baBrewery + "/" + beer.baBeer)
					);
				startActivity(showBeerAdvocateIntent);
			}
		});

		
		super.onCreate(savedInstanceState);
	}


}
