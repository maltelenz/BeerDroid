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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Main activity which is shown when started.
 * @author Malte Lenz
 *
 */
public class BeerDroid extends Activity {

	private static final String TAG = "BeerDroid"; //Application name for logging

	private static final int MENU_PREFERENCES = 0;

	/** Contains the list of beers from the last search. */
	public static ArrayList<Beer> resultList;

	private EditText searchField;

	private ProgressDialog busy;

	private ListView resultView;

	private DatabaseAdapter dBHelper;

	private Resources res;

	/**
	 *  Called when the activity is first created.
	 *  @param savedInstanceState state from last time, unused by us.
	 *  */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		res = getResources();

		//connect to database
		dBHelper = new DatabaseAdapter(this);
        dBHelper.open();

		searchField = (EditText) findViewById(R.id.search_field);

		//prepare a progress dialog
		busy = new ProgressDialog(this);
		busy.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		busy.setMessage("Contacting server...");
		busy.setCancelable(false);

		//make search button clickable
		final Button searchButton = (Button) findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				//fetch the entered search query
				final String query = searchField.getText().toString();
				if (!("".equals(query))) {
					new DoSearch().execute(query);
					busy.show();
				} else {
					Toast.makeText(getBaseContext(), "Please enter a name to search for.", Toast.LENGTH_LONG).show();
				}
			}
		});

		resultView = (ListView) findViewById(R.id.result_list);
		resultView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                showResultDetails(id);
            }
        });
	}

	/**
	 * Creates the menu called by the menu button.
	 * @param menu the menu to modify
	 * @return the menu
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(Menu.NONE, MENU_PREFERENCES, Menu.NONE, "Preferences");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Called when the user has pressed a menu item.
	 * @param item the clicked item
	 * @return if the click was handled
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			//Start the preferences activity
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Show a detailed view with all info on a search result.
	 * @param id which beer to show details for.
	 */
	protected final void showResultDetails(final Long id) {
		Log.d(TAG, "showResultDetails for beer: " + resultList.get(id.intValue()).toString());
		final Intent showDetailIntent = new Intent(this, BeerDetails.class);
		showDetailIntent.putExtra("id", id);
		startActivity(showDetailIntent);
	}


	/**
	 * Does a search in the background, calling showResults with the result.
	 * Run with 'new DoSearch().execute("search_query");'
	 * @author malte
	 *
	 */
	private class DoSearch extends AsyncTask<String, Object, String> {

		@Override
		protected String doInBackground(final String... query) {
			//prepare request
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String county = sp.getString(
					res.getString(R.string.pref_systembolaget_county_key), //the value set in preferences
					res.getString(R.string.pref_systembolaget_county_default) //or default if none is set
					);

			String url = Config.baseUrl + Config.superSearchUrl + URLEncoder.encode(query[0]) + "/" + county;
			final ResponseHandler<String> responseHandler = new BasicResponseHandler();
			final HttpClient client = new DefaultHttpClient();
			final HttpGet get = new HttpGet(url);
			Log.d(TAG, "Fetching url: " + get.getURI().toString());
			String searchResults = null;
			//call server
			try {
				searchResults = client.execute(get, responseHandler);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolException while searching: " + e.toString());
			} catch (IOException e) {
				Log.e(TAG, "IOException while searching: " + e.toString());
			} catch (java.lang.IllegalArgumentException e) {
				Log.e(TAG, "IllegalArgumentException while searching: " + e.toString());
			}
			return searchResults;
		}

		@Override
		protected void onPostExecute(final String results) {
			Log.d(TAG, "Got result: " + results);
			busy.hide();
			if (results != null) {
				showResults(results);
			} else {
				Log.w(TAG, "Something went wrong in server contact.");
				//show message to the user about this
				Toast.makeText(getBaseContext(), "Could not contact server", Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(results);
		}
	}

	/**
	 * Updates the listView of results the user sees.
	 * @param searchResults	a string in json format with search results
	 */
	public final void showResults(final String searchResults) {
		resultList = new ArrayList<Beer>();
		try {
			final JSONArray jsonResults = new JSONArray(searchResults);

			Log.d(TAG, "Number of beers found: " + jsonResults.length());
			//check if we have any hits
			if (jsonResults.length() == 0) {
				Toast.makeText(getBaseContext(), "No beers found", Toast.LENGTH_LONG).show();
			}

			for (int i = 0; i < jsonResults.length(); i = i + 1) {
				resultList.add(new Beer(jsonResults.getJSONObject(i), dBHelper));
			}
		} catch (JSONException e) {
			Log.e(TAG, "Could not decode results: " + e.toString());
		}

		Log.d(TAG, "Final list of search results: " + resultList.toString());

		final ResultAdapter resultAdapter = new ResultAdapter(this, R.layout.result_list_item, resultList);
		resultView.setAdapter(resultAdapter);
	}

	/**
	 * Adapter used for showing the list of results from a search.
	 * @author Malte Lenz
	 *
	 */
	private class ResultAdapter extends ArrayAdapter<Beer> {

		private List<Beer> beers;

		/**
		 * Constructor which saves the list of beers to a local field.
		 * @param context calling context
		 * @param textViewResourceId what text resource (xml file) to use for display
		 * @param items list of beers
		 */
		public ResultAdapter(final Context context, final int textViewResourceId, final List<Beer> items) {
			super(context, textViewResourceId, items);
			beers = items;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			//Log.d(TAG, "Showing beer in position: " + position);
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
					bt.setText(b.getName());
				}
				if (br != null) {
					br.setText(b.getBaRating());
				}
				if (bb != null) {
					bb.setText(b.getBreweryName());
				}
			}
			return v;
		}

	}

	@Override
	protected final void onDestroy() {
		//dismiss dialog
		busy.dismiss();
		//close database connection
		dBHelper.close();

		super.onDestroy();
	}
}
