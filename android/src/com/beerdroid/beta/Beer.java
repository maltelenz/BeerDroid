package com.beerdroid.beta;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Beer {

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

	public String name;
	public String style;
	public String baRating;
	public String breweryName;
	public Integer systemetSize;
	public Integer systemetPrice;
	public Integer baBrewery;
	public Integer baBeer;


	public Beer(JSONObject json) {
		try {
			name = json.getString(KEY_NAME);
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
			systemetSize = json.getInt(KEY_SYSTEMET_SIZE);
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

}
