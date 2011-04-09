package com.beerdroid.beta;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BeerProvider extends ContentProvider {
	
    public static final String AUTHORITY = "com.beerdroid.beta.beerprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/beer");
		
	private static final String TAG = "BeerProvider";
	private static final String DATABASE_NAME = "data";
	// Version of the database. Increase whenever database schema is changed.
	private static final int DATABASE_VERSION = 1;
	private static final String BEER_TABLE_NAME = "beer";
	
	// Mime types
	public static final String BEER_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.beerdroid.beta.beer";
	
	// Table columns
	public static final String _ID = "_id";
	public static final String NAME = "beer_name";
	public static final String BREWERY = "brewery_name";
	public static final String STYLE = "style";
	public static final String ABV = "abv";
	public static final String BEERADVOCATE_RATING = "ba_rating";
	public static final String BEERADVOCATE_BEER_ID = "ba_beer_id";
	public static final String BEERADVOCATE_BREWERY_ID = "ba_brewery_id";
	public static final String SYSTEMBOLAGET_SIZE = "systemet_size";
	public static final String SYSTEMBOLAGET_PRICE = "systemet_price";
	public static final String[] ALL_BEER_COLUMNS = {_ID, NAME, BREWERY, STYLE, ABV, BEERADVOCATE_RATING,
		BEERADVOCATE_BEER_ID, BEERADVOCATE_BREWERY_ID, SYSTEMBOLAGET_SIZE, SYSTEMBOLAGET_PRICE};
	
	// Used selections
	private static final String QUERY_ID =  _ID + " = ?";
	private static final String QUERY_BA_ID = BEERADVOCATE_BEER_ID + " = ?";
	private static final String QUERY_SEARCH_SUGGEST = NAME + " LIKE ? OR " + NAME + " LIKE ?";
	
	//query for creating the beer table
	private static final String CREATE_TABLE_BEER =
		"CREATE TABLE " + BEER_TABLE_NAME + "("
		+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ NAME + " TEXT NOT NULL,"
		+ STYLE + " TEXT,"
		+ BEERADVOCATE_RATING + " TEXT,"
		+ BREWERY + " TEXT,"
		+ SYSTEMBOLAGET_SIZE + " INT,"
		+ SYSTEMBOLAGET_PRICE + " FLOAT,"
		+ BEERADVOCATE_BREWERY_ID + " INT,"
		+ BEERADVOCATE_BEER_ID + " INT,"
		+ ABV + " FLOAT"
		+ ");";
	
	/**
	 * Internal helper class for creating, and updating the database.
	 * @author Malte Lenz
	 *
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * Default constructor.
		 * @param context calling context
		 */
		DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			Log.d(TAG, "Creating table \"" + BEER_TABLE_NAME + "\"");
			db.execSQL(CREATE_TABLE_BEER);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			Log.w(TAG , "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + BEER_TABLE_NAME);
			onCreate(db);
		}
	}
	private DatabaseHelper mDbHelper;
	
	private static final HashMap<String, String> sQSBProjectionMap = new HashMap<String, String>();
	private static final HashMap<String, String> sBeerProjectionMap = new HashMap<String, String>();
	
	private static final int BEER = 1;
    private static final int COUNT = 2;
    private static final int SEARCH_SUGGEST = 3;
    private static final int CLEAR_DATABASE = 4;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
    	sUriMatcher.addURI(AUTHORITY, "beer", BEER);
    	sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    	sUriMatcher.addURI(AUTHORITY, "clear_database", CLEAR_DATABASE);
    	sQSBProjectionMap.put(_ID, _ID + " AS " + BaseColumns._ID);
    	sQSBProjectionMap.put(NAME, NAME + " AS " +	SearchManager.SUGGEST_COLUMN_TEXT_1);
    	sQSBProjectionMap.put(BREWERY, BREWERY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
    	sQSBProjectionMap.put(BEERADVOCATE_BEER_ID, BEERADVOCATE_BEER_ID +  " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);    	
    	sBeerProjectionMap.put(_ID, _ID);
    	sBeerProjectionMap.put(NAME, NAME);
    	sBeerProjectionMap.put(BREWERY, BREWERY); 
    	sBeerProjectionMap.put(STYLE, STYLE); 
    	sBeerProjectionMap.put(ABV, ABV); 
    	sBeerProjectionMap.put(BEERADVOCATE_RATING, BEERADVOCATE_RATING); 
    	sBeerProjectionMap.put(BEERADVOCATE_BEER_ID, BEERADVOCATE_BEER_ID); 
    	sBeerProjectionMap.put(BEERADVOCATE_BREWERY_ID, BEERADVOCATE_BREWERY_ID);
    	sBeerProjectionMap.put(SYSTEMBOLAGET_SIZE, SYSTEMBOLAGET_SIZE);
    	sBeerProjectionMap.put(SYSTEMBOLAGET_PRICE, SYSTEMBOLAGET_PRICE);
    }
	
	@Override
	public final boolean onCreate() {
		mDbHelper = new DatabaseHelper(getContext());
		return true;	
	}

	@Override	
	public final int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		switch (sUriMatcher.match(uri)) {
		case BEER:
			break;
		case CLEAR_DATABASE:
			selection = "1";
			selectionArgs = null;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " +  uri);
		}
		return db.delete(BEER_TABLE_NAME, selection, selectionArgs);
	}

	@Override
	public final String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case BEER:
			return BEER_MIME_TYPE;
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			return null;
		}	
	}
	
	@Override
	public final int bulkInsert(Uri uri, final ContentValues[] values) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int numInserted = 0;
		for (int i = 0; i < values.length; ++i) {
			if (!validateContentValues(values[i])) {
				continue;
			}
			// Check if the ID already exists, if so, update else insert
			try {
				long id = getBeerId(QUERY_BA_ID, new String[] {values[i].getAsString(BEERADVOCATE_BEER_ID)});
				if (id > 0) {
					db.update(BEER_TABLE_NAME, values[i], _ID + "= ?", new String[] {String.valueOf(id)});
					Log.d(TAG, "Beer with id " + id + " updated.");
					++numInserted;
				} else if (id == -1) {					
					id = db.insert(BEER_TABLE_NAME, null, values[i]);
					Log.d(TAG, "Beer with id " + id + " inserted.");
					++numInserted;
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception caught when inserting beer: " + e.getMessage());
			}
		}
		return numInserted;
	}

	@Override
	public final Uri insert(Uri uri, ContentValues values) throws IllegalArgumentException {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// Verify that all required values exist
		if (!validateContentValues(values)) {
			throw new IllegalArgumentException("Incorrect ContentValues format");
		}
		long id = getBeerId(QUERY_BA_ID, new String[] {values.getAsString(BEERADVOCATE_BEER_ID)});
		try {
			id = db.insert(BEER_TABLE_NAME, null, values);
			Log.d(TAG, "Beer with id " + id + " inserted.");
			return Uri.withAppendedPath(uri, Long.toString(id));
		} catch (Exception e) {
			Log.e(TAG, "Exception caught when inserting beer: " + e.getMessage());
		}
		return null;
	}
	
	@Override
	public final Cursor query(final Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		 SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		 qb.setTables(BEER_TABLE_NAME);
		switch (sUriMatcher.match(uri)) {
		case BEER:
			if (selectionArgs == null) {
				String id = uri.getLastPathSegment();
				selection = QUERY_ID;
				selectionArgs = new String[] {id};
			}
			qb.setProjectionMap(sBeerProjectionMap);
			break;
		case COUNT:
			return null;
		case SEARCH_SUGGEST:
			qb.setProjectionMap(sQSBProjectionMap);
			projection = new String[] {_ID, NAME, BREWERY, BEERADVOCATE_BEER_ID};
			selection = QUERY_SEARCH_SUGGEST;
			selectionArgs = new String[] {selectionArgs[0] + "%", "% " + selectionArgs[0] + "%"};
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " +  uri);
		}
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
	    Cursor c = qb.query(db,
	    		projection,
	    		selection,
	    		selectionArgs,
	    		null,
	    		null,
	    		null,
	    		uri.getQueryParameter("limit"));

	    //c.setNotificationUri(getContext().getContentResolver(), uri);
	    return c;
	}

	@Override
	public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Querys for the database for the id of the matches
	 * @param selection Selection string
	 * @param selectionArgs[] Selection arguments  
	 * @return id if unique match found, -1 if not found, -2 if non-unique match 
	 */
	private final long getBeerId(String selection, String selectionArgs[]) {
		long id = 0;
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Cursor c = db.query(BEER_TABLE_NAME, 
				new String[] {_ID}, 
				selection, 
				selectionArgs, 
				null, null, null, "2");
		if (!c.moveToFirst()) {
			id = -1;
		} else if (c.getCount() > 1) {
			id = -2;
		} else {
			id = c.getLong(0);
		}
		c.close();
		return id;
	}
	
	private final boolean validateContentValues(ContentValues values) {
		if (values.size() != 9) {
			Log.d(TAG, "Incorrect ContentValues format for BeerProvider");
		} else if (!values.containsKey(NAME)) {
			Log.d(TAG, "BeerProvider.NAME key is missing.");
		} else if (!values.containsKey(STYLE)) {
			Log.d(TAG, "BeerProvider.STYLE key is missing.");
		} else if (!values.containsKey(BEERADVOCATE_RATING)) {
			Log.d(TAG, "BeerProvider.BA_RATING key is missing.");
		} else if (!values.containsKey(BREWERY)) {
			Log.d(TAG, "BeerProvider.BREWERY key is missing.");
		} else if (!values.containsKey(SYSTEMBOLAGET_SIZE)) {
			Log.d(TAG, "BeerProvider.SYSTEMBOLAGET_SIZE key is missing.");
		} else if (!values.containsKey(SYSTEMBOLAGET_PRICE)) {
			Log.d(TAG, "BeerProvider.SYSTEMBOLAGET_PRICE key is missing.");
		} else if (!values.containsKey(BEERADVOCATE_BREWERY_ID)) {
			Log.d(TAG, "BeerProvider.BA_BREWERY_ID key is missing.");
		} else if (!values.containsKey(BEERADVOCATE_BEER_ID)) {
			Log.d(TAG, "BeerProvider.BA_BEER_ID key is missing.");
		} else if (!values.containsKey(ABV)) {
			Log.d(TAG, "BeerProvider.ABV key is missing.");
		} else {
			return true;
		}
		return false;
	}
}
