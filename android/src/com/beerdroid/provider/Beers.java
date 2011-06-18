package com.beerdroid.provider;

import android.provider.BaseColumns;

public final class Beers implements BaseColumns {
	public static final String TABLE_NAME = "beer";
	
	/** Table columns */
	public static final String _ID = "_id";
	public static final String NAME = "beer_name";
	public static final String BREWERY = "brewery_name";
	public static final String BREWERY_ID = "brewery_id";
	public static final String STYLE = "style";
	public static final String ABV = "abv";
	public static final String BEERADVOCATE_RATING = "ba_rating";
	public static final String BEERADVOCATE_BEER_ID = "ba_beer_id";
	public static final String BEERADVOCATE_BREWERY_ID = "ba_brewery_id";
	public static final String SYSTEMBOLAGET_SIZE = "systemet_size";
	public static final String SYSTEMBOLAGET_PRICE = "systemet_price";

	public static final String[] ALL_COLUMNS = {_ID, NAME, BREWERY, STYLE, ABV, BEERADVOCATE_RATING,
		BEERADVOCATE_BEER_ID, BEERADVOCATE_BREWERY_ID, SYSTEMBOLAGET_SIZE, SYSTEMBOLAGET_PRICE};
}
