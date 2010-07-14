package com.beerdroid.beta;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Beer {

	public String name = null;
	public String style = null;
	public String ba_rating = null;
	public String brewery_name;
	public Integer systemet_size = null;
	public Integer systemet_price = null;
	public Integer ba_brewery = null;
	public Integer ba_beer = null;
	
	
	private String KEY_NAME = "beer_name";
	private String KEY_STYLE = "style";
	private String KEY_BA_RATING = "rating";
	private String KEY_SYSTEMET_PRICE = "systemet_price";
	private String KEY_SYSTEMET_SIZE = "systemet_size";
	private String KEY_BA_ID = "ba_id";
	private String KEY_BA_BREWERY = "brewery";
	private String KEY_BA_BEER = "beer";
	private String KEY_BREWERY_NAME = "brewery_name";
	
	public Beer(JSONObject json) {
		try {
			name = json.getString(KEY_NAME);
		} catch (JSONException e) {
			Log.e("Beer", "Could not create beer: " + e.toString());
			return;
		}
		try {
			style = json.getString(KEY_STYLE);
		} catch (JSONException e) {
			Log.e("Beer", "Could not create beer: " + e.toString());
		}
		try {
			ba_rating = json.getString(KEY_BA_RATING);
		} catch (JSONException e) {
			Log.d("Beer", "No BA rating found: " + e.toString());
		}
		try {
			systemet_price = json.getInt(KEY_SYSTEMET_PRICE);
		} catch (JSONException e) {
			Log.d("Beer", "No systemet_price found: " + e.toString());
		}
		try {
			systemet_size = json.getInt(KEY_SYSTEMET_SIZE);
		} catch (JSONException e) {
			Log.d("Beer", "No systemet_size found: " + e.toString());
		}
		try {
			brewery_name = json.getString(KEY_BREWERY_NAME);
		} catch (JSONException e) {
			Log.d("Beer", "No brewery_name found: " + e.toString());
		}
		JSONObject ba_id;
		try {
			ba_id = json.getJSONObject(KEY_BA_ID);
			try {
				ba_brewery = ba_id.getInt(KEY_BA_BREWERY);
			} catch (JSONException e) {
				Log.d("Beer", "No ba_brewery found: " + e.toString());
			}
			try {
				ba_beer = ba_id.getInt(KEY_BA_BEER);
			} catch (JSONException e) {
				Log.d("Beer", "No ba_beer found: " + e.toString());
			}
		} catch (JSONException e) {
			Log.d("Beer", "No ba_id found: " + e.toString());
		}
		
	
	}

}
