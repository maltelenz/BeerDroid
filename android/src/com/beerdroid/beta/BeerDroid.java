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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BeerDroid extends ListActivity {

	private EditText search_field;

	public String TAG = "BeerDroid"; //Application name for logging

	private AlertDialog busy;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		search_field = (EditText) findViewById(R.id.search_field);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Contacting Server...");
		busy = builder.create();
		
		Button search_button = (Button) findViewById(R.id.search_button);
		search_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//fetch the entered search query
				String query = search_field.getText().toString();
				new DoSearch().execute(query);
				busy.show();
			}
		});
	}


	/**
	 * Does a search in the background, calling showResults with the result
	 * Run with 'new DoSearch().execute("search_query");'
	 * @author malte
	 *
	 */
	private class DoSearch extends AsyncTask<String,Object,String>{

		@Override
		protected String doInBackground(String... query) {
			//prepare request
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(Config.base_url + Config.super_search_url + URLEncoder.encode(query[0]));
			Log.d(TAG,"Fetching url: " + get.getURI().toString());
			String search_results = null;
			//call server
			try {
				search_results = client.execute(get, responseHandler);
			} catch (ClientProtocolException e) {
				Log.e(TAG,"Error while searching: " + e.toString());
				return null;
			} catch (IOException e) {
				Log.e(TAG,"Error while searching: " + e.toString());
				return null;
			}

			return search_results;
		}

		@Override
		protected void onPostExecute(String results) {
			Log.d(TAG,"Got result: " + results);
			if (results!=null){
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
		busy.hide();
		ArrayList<Beer> resultList = new ArrayList<Beer>();
		try {
			JSONArray jsonResults = new JSONArray(search_results);
			for(int i=0; i < jsonResults.length(); i++){
				resultList.add(new Beer(jsonResults.getJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e(TAG,"Could not decode results: " + e.toString());
		}
		Log.d(TAG,resultList.toString());
		ResultAdapter resultAdapter = new ResultAdapter(this, R.layout.result_list_item, resultList);
		setListAdapter(resultAdapter);
	}

	private class ResultAdapter extends ArrayAdapter<Beer>{

		private List<Beer> beers;

		public ResultAdapter(Context context, int textViewResourceId, List<Beer> items) {
			super(context, textViewResourceId, items);
			beers = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG,"Showing beer in position: " + position);
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.result_list_item, null);
			}
			Beer b = beers.get(position);
			if (b != null) {
				TextView bt = (TextView) v.findViewById(R.id.beer_name);
				TextView br = (TextView) v.findViewById(R.id.beer_rating);
				TextView bb = (TextView) v.findViewById(R.id.beer_brewery);
				if (bt != null) {
					bt.setText(b.name);
				}
				if(br != null){
					br.setText(b.ba_rating);
				}
				if(bb != null){
					bb.setText(b.brewery_name);
				}
			}
			return v;
		}

	}

}