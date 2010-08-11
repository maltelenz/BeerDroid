package com.beerdroid.beta;

import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Class for info about one beer,
 * with helpful functions for access.
 * @author Malte Lenz
 *
 */
public class Beer {

	/**
	 * Hashtable key for nr for systemet availability.
	 */
	public static final String SYSTEMET_STORE_NR_KEY = "nr";

	/**
	 * Hashtable key for name of a store for systemet availability.
	 */
	public static final String SYSTEMET_STORE_KEY = "store";

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
	private static final String KEY_ABV = "abv";
	private static final String KEY_SYSTEMET_AVAILABILITY = "systemet_available";

	private static final String TAG = "Beer";

	/** Name of the beer. */
	public String name;
	/** Style name of the beer. */
	public String style;
	/** Rating on beeradvocate.com in a string. */
	public String baRating;
	/** name of the brewery. */
	public String breweryName;
	/** size of containers from systembolaget, sweden. */
	public Integer systemetSize;
	/** price at systembolaget, sweden. */
	public Integer systemetPrice;
	/** Id of brewery on beeradvocate.com. */
	public Integer baBrewery;
	/** Id of beer on beeradvocate.com. */
	public Integer baBeer;
	/** Alcohol content by volume in percent. */
	public Double abv;
	/** Availability at different stores at systembolaget. */
	public ArrayList<Hashtable<String, String>> systemetAvailabilityList;
	/** County for which the above availability is valid. */
	public String county;


	/**
	 * Create the beer object from received JSON data.
	 * @param json	beer object from server in JSON
	 * @param dBHelper	DatabaseAdapter for database connection
	 * @param newCounty county for which systemet availability is fetched
	 */
	public Beer(final JSONObject json, final DatabaseAdapter dBHelper, final String newCounty) {
		//initialize the availability list
		systemetAvailabilityList = new ArrayList<Hashtable<String, String>>();
		//save the county
		county = newCounty;
		//decode all json info
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
		try {
			abv = json.getDouble(KEY_ABV);
		} catch (JSONException e) {
			Log.d(TAG, "No abv found: " + e.toString());
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

		try {
			JSONArray systemetAvailability = json.getJSONArray(KEY_SYSTEMET_AVAILABILITY);
			for (int i = 0; i < systemetAvailability.length(); i = i + 1) {
				JSONObject systemetStore = systemetAvailability.getJSONObject(i);
				Hashtable<String, String> ht = new Hashtable<String, String>();
				ht.put(SYSTEMET_STORE_NR_KEY, systemetStore.getString(SYSTEMET_STORE_NR_KEY));
				ht.put(SYSTEMET_STORE_KEY, systemetStore.getString(SYSTEMET_STORE_KEY));
				systemetAvailabilityList.add(ht);
			}
		} catch (JSONException e) {
			Log.d(TAG, "No availability found: " + e.toString());
		}

		//beer is fully initialized, save it to database if not already there
		if (!dBHelper.beerExists(this)) {
			//save beer
			dBHelper.createBeer(this);
		}
		// TODO else {
		//	dBHelper.updateBeer(this);
		//}
	}

	/**
	 * Methods for getting and setting all the fields,
	 * with defaults for when fields are null
	 */

	/**
	 * Returns beer name or EMPTY_DISPLAY_STRING if not set.
	 * @return beer name or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getName() {
		if (name != null) {
			return name;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beer name.
	 * @param newName to save
	 */
	public final void setName(final String newName) {
		this.name = newName;
	}

	/**
	 * Returns beer style or EMPTY_DISPLAY_STRING if not set.
	 * @return beer style or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getStyle() {
		if (style != null) {
			return style;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beer style.
	 * @param newStyle of beer to save
	 */
	public final void setStyle(final String newStyle) {
		this.style = newStyle;
	}

	/**
	 * Returns beers rating on beeradvocate.com or EMPTY_DISPLAY_STRING if not set.
	 * @return beers rating on beeradvocate.com or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getBaRating() {
		if (baRating != null) {
			return baRating;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beers rating on beeradvocate.com.
	 * @param newBaRating beers rating on beeradvocate.com as a String
	 */
	public final void setBaRating(final String newBaRating) {
		this.baRating = newBaRating;
	}

	/**
	 * Returns brewery name or EMPTY_DISPLAY_STRING if not set.
	 * @return brewery name or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getBreweryName() {
		if (breweryName != null) {
			return breweryName;
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves brewery name.
	 * @param newBreweryName to save
	 */
	public final void setBreweryName(final String newBreweryName) {
		this.breweryName = newBreweryName;
	}

	/**
	 * Returns the beers price at systembolaget or EMPTY_DISPLAY_STRING if not set.
	 * @return the beers price at systembolaget or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getSystemetPrice() {
		if (systemetPrice != null) {
			return systemetPrice.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves the beers price at systembolaget.
	 * @param newSystemetPrice the beers price at systembolaget
	 */
	public final void setSystemetPrice(final Integer newSystemetPrice) {
		this.systemetPrice = newSystemetPrice;
	}

	/**
	 * Returns brewery id from beeradvocate.com or EMPTY_DISPLAY_STRING if not set.
	 * @return brewery id from beeradvocate.com or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getBaBrewery() {
		if (baBrewery != null) {
			return baBrewery.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves brewery id from beeradvocate.com.
	 * @param newBaBrewery id of the brewery on beeradvocate.com to save
	 */
	public final void setBaBrewery(final Integer newBaBrewery) {
		this.baBrewery = newBaBrewery;
	}

	/**
	 * Returns beer id from beeradvocate.com or EMPTY_DISPLAY_STRING if not set.
	 * @return beer id from beeradvocate.com or EMPTY_DISPLAY_STRING if not set.
	 */
	public final String getBaBeer() {
		if (baBeer != null) {
			return baBeer.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves beer name.
	 * @param newBaBeer name of the beer on beeradvocate.com
	 */
	public final void setBaBeer(final Integer newBaBeer) {
		this.baBeer = newBaBeer;
	}

	/**
	 * Returns size of systembolaget containers or EMPTY_DISPLAY_STRING if not set.
	 * @return size of systembolaget containers or EMPTY_DISPLAY_STRING if not set.
	 */
	public final String getSystemetSize() {
		if (systemetSize != null) {
			return systemetSize.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves abv.
	 * @param newSystemetSize size of containers at systemet to save
	 */
	public final void setSystemetSize(final Integer newSystemetSize) {
		this.systemetSize = newSystemetSize;
	}

	/**
	 * Returns abv or EMPTY_DISPLAY_STRING if not set.
	 * @return abv or EMPTY_DISPLAY_STRING if not set
	 */
	public final String getAbv() {
		if (abv != null) {
			return abv.toString();
		}
		return EMPTY_DISPLAY_STRING;
	}

	/**
	 * Saves size of systemet containers.
	 * @param newAbv Alcohol content to set
	 */
	public final void setAbv(final Double newAbv) {
		this.abv = newAbv;
	}

}
