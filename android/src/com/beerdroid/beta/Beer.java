package com.beerdroid.beta;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Beer {

	private static final String EMPTY_DISPLAY_STRING = "-";
	
	private static final String KEY_NAME = "beer_name";
	private static final String KEY_STYLE = "style";
	private static final String KEY_BA_RATING = "rating";
	private static final String KEY_SYSTEMET_PRICE = "systemet_price";
	private static final String KEY_SYSTEMET_SIZE = "systemet_size";
	private static final String KEY_BA_ID = "ba_id";
	private static final String KEY_BA_BREWERY = "brewery";
	private static final String KEY_BA_BEER = "beer";
	private static final String KEY_BREWERY_NAME = "brewery_name";

	private static final String TAG = "Beer";

	private String name;
	private String style;
	private String baRating;
	private String breweryName;
	private Integer systemetSize;
	private Integer systemetPrice;
	private Integer baBrewery;
	private Integer baBeer;


	public Beer(JSONObject json) {
		try {
			setName(json.getString(KEY_NAME));
		} catch (JSONException e) {
			Log.e(TAG, "Could not decode name: " + e.toString());
			return;
		}
		try {
			style = json.getString(KEY_STYLE);
		} catch (JSONException e) {
			Log.e(TAG, "Could not decode style: " + e.toString());
		}
		try {
			baRating = json.getString(KEY_BA_RATING);
		} catch (JSONException e) {
			Log.d(TAG, "No BA rating found: " + e.toString());
		}
		try {
			systemetPrice = json.getInt(KEY_SYSTEMET_PRICE);
		} catch (JSONException e) {
			Log.d(TAG, "No systemet_price found: " + e.toString());
		}
		try {
			setSystemetSize(json.getInt(KEY_SYSTEMET_SIZE));
		} catch (JSONException e) {
			Log.d(TAG, "No systemet_size found: " + e.toString());
		}
		try {
			breweryName = json.getString(KEY_BREWERY_NAME);
		} catch (JSONException e) {
			Log.d(TAG, "No brewery_name found: " + e.toString());
		}
		JSONObject baId;
		try {
			baId = json.getJSONObject(KEY_BA_ID);
			try {
				baBrewery = baId.getInt(KEY_BA_BREWERY);
			} catch (JSONException e) {
				Log.d(TAG, "No ba_brewery found: " + e.toString());
			}
			try {
				baBeer = baId.getInt(KEY_BA_BEER);
			} catch (JSONException e) {
				Log.d(TAG, "No ba_beer found: " + e.toString());
			}
		} catch (JSONException e) {
			Log.d(TAG, "No ba_id found: " + e.toString());
		}


	}

	
	/**
	 * Methods for getting and setting all the fields,
	 * with defaults for when fields are null
	 */
	
	/**
	 * Returns beer name or "-" if not set
	 * @return
	 */
	public String getName() {
		if (name != null) {
			return name;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beer name
	 * @param name
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	
	/**
	 * Returns beer style or "-" if not set
	 * @return
	 */
	public String getStyle() {
		if (style != null) {
			return style;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beer style
	 * @param name
	 */
	public void setStyle(String newStyle) {
		this.style = newStyle;
	}

	/**
	 * Returns beers rating on beeradvocate.com or "-" if not set
	 * @return
	 */
	public String getBaRating() {
		if (baRating != null) {
			return baRating;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beers rating on beeradvocate.com
	 * @param name
	 */
	public void setBaRating(String newBaRating) {
		this.baRating = newBaRating;
	}

	/**
	 * Returns brewery name or "-" if not set
	 * @return
	 */
	public String getBreweryName() {
		if (breweryName != null) {
			return breweryName;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves brewery name
	 * @param name
	 */
	public void setBreweryName(String newBreweryName) {
		this.breweryName = newBreweryName;
	}

	/**
	 * Returns the beers price at systembolaget or "-" if not set
	 * @return
	 */
	public String getSystemetPrice() {
		if (systemetPrice != null) {
			return systemetPrice.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves the beers price at systembolaget
	 * @param name
	 */
	public void setSystemetPrice(Integer newSystemetPrice) {
		this.systemetPrice = newSystemetPrice;
	}

	/**
	 * Returns brewery id from beeradvocate.com or "-" if not set
	 * @return
	 */
	public String getBaBrewery() {
		if (baBrewery != null) {
			return baBrewery.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves brewery id from beeradvocate.com
	 * @param name
	 */
	public void setBaBrewery(Integer newBaBrewery) {
		this.baBrewery = newBaBrewery;
	}

	/**
	 * Returns beer id from beeradvocate.com or "-" if not set
	 * @return
	 */
	public String getBaBeer() {
		if (baBeer != null) {
			return baBeer.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beer name
	 * @param name
	 */
	public void setBaBeer(Integer newBaBeer) {
		this.baBeer = newBaBeer;
	}

	/**
	 * Returns size of systembolaget containers or "-" if not set.
	 * @return
	 */
	public String getSystemetSize() {
		if (systemetSize != null) {
			return systemetSize.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves size of systemet containers.
	 * @param newSystemetSize
	 */
	public void setSystemetSize(Integer newSystemetSize) {
		this.systemetSize = newSystemetSize;
	}

}
