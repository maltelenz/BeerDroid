package com.beerdroid.beta;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BeerDroid extends Activity {

	private EditText search_field;

	public String TAG = "BeerDroid"; //Application name for logging

	private ProgressDialog busy;

	public static ArrayList<Beer> resultList;

	private ListView resultView;

	/**
	 *  Called when the activity is first created.
	 *  @param savedInstanceState 
	 *  */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		search_field = (EditText) findViewById(R.id.search_field);

		//prepare a progress dialog
		busy = new ProgressDialog(this);
		busy.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		busy.setMessage("Contacting server...");
		busy.setCancelable(false);
		
		//make search button clickable
		final Button search_button = (Button) findViewById(R.id.search_button);
		search_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//fetch the entered search query
				final String query = search_field.getText().toString();
				new DoSearch().execute(query);
				busy.show();
			}
		});
		
		resultView = (ListView) findViewById(R.id.result_list);
		resultView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                showResultDetails(id);
            }
        });
        
	}

	/**
	 * Show a detailed view with all info on a search result
	 * @param id
	 */
	protected void showResultDetails(Long id) {
		Log.d(TAG, "showResultDetails for beer: " + resultList.get(id.intValue()).toString());
		final Intent showDetailIntent = new Intent(this, BeerDetails.class);
		showDetailIntent.putExtra("id", id);
		startActivity(showDetailIntent);
	}


	/**
	 * Does a search in the background, calling showResults with the result
	 * Run with 'new DoSearch().execute("search_query");'
	 * @author malte
	 *
	 */
	private class DoSearch extends AsyncTask<String, Object, String> {

		@Override
		protected String doInBackground(String... query) {
			//prepare request
			final ResponseHandler<String> responseHandler = new BasicResponseHandler();
			final HttpClient client = new DefaultHttpClient();
			final HttpGet get = new HttpGet(Config.base_url + Config.super_search_url + URLEncoder.encode(query[0]));
			Log.d(TAG, "Fetching url: " + get.getURI().toString());
			String search_results = null;
			//call server
			try {
				search_results = client.execute(get, responseHandler);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "Error while searching: " + e.toString());
				return null;
			} catch (IOException e) {
				Log.e(TAG, "Error while searching: " + e.toString());
				return null;
			}

			return search_results;
		}

		@Override
		protected void onPostExecute(String results) {
			Log.d(TAG, "Got result: " + results);
			busy.hide();
			if (results != null) {
				showResults(results);
			}
			super.onPostExecute(results);
		}
	}

	/**
	 * Updates the listView of results the user sees
	 * @param search_results	a string in json format with search results
	 */
	public void showResults(String search_results) {
		resultList = new ArrayList<Beer>();
		try {
			final JSONArray jsonResults = new JSONArray(search_results);
			for (int i = 0; i < jsonResults.length(); i++) {
				resultList.add(new Beer(jsonResults.getJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e(TAG, "Could not decode results: " + e.toString());
		}
		Log.d(TAG, resultList.toString());
		final ResultAdapter resultAdapter = new ResultAdapter(this, R.layout.result_list_item, resultList);
		resultView.setAdapter(resultAdapter);
	}

	private class ResultAdapter extends ArrayAdapter<Beer> {

		private List<Beer> beers;

		public ResultAdapter(Context context, int textViewResourceId, List<Beer> items) {
			super(context, textViewResourceId, items);
			beers = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "Showing beer in position: " + position);
			View v = convertView;
			if (v == null) {
				final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.result_list_item, null);
			}
			final Beer b = beers.get(position);
			if (b != null) {
				final TextView bt = (TextView) v.findViewById(R.id.beer_name);
				final TextView br = (TextView) v.findViewById(R.id.beer_rating);
				final TextView bb = (TextView) v.findViewById(R.id.beer_brewery);
				if (bt != null) {
					bt.setText(b.name);
				}
				if (br != null) {
					br.setText(b.ba_rating);
				}
				if (bb != null) {
					bb.setText(b.brewery_name);
				}
			}
			return v;
		}

	}

	@Override
	protected void onDestroy() {
		busy.dismiss();
		super.onDestroy();
	}

	
}
