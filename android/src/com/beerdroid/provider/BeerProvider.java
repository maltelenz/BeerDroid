package com.beerdroid.provider;

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
	
    public static final String AUTHORITY = "com.beerdroid.provider.beerprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/beer");
		
	private static final String TAG = "BeerProvider";
	private static final String DATABASE_NAME = "data";
	// Version of the database. Increase whenever database schema is changed.
	private static final int DATABASE_VERSION = 1;
	
	// Mime types
	public static final String BEER_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.beerdroid.beta.beer";
	
	
	// Used selections
	private static final String QUERY_ID =  Beers._ID + " = ?";
	private static final String QUERY_BA_ID = Beers.BEERADVOCATE_BEER_ID + " = ?";
	private static final String QUERY_SEARCH_SUGGEST = Beers.NAME + " LIKE ? OR " + Beers.NAME + " LIKE ?";
	
	//query for creating the beer table
	private static final String CREATE_TABLE_BEER =
		"CREATE TABLE " + Beers.TABLE_NAME + "("
		+ Beers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ Beers.NAME + " TEXT NOT NULL,"
		+ Beers.STYLE + " TEXT,"
		+ Beers.BEERADVOCATE_RATING + " TEXT,"
		+ Beers.BREWERY_ID + " TEXT,"
		+ Beers.SYSTEMBOLAGET_SIZE + " INT,"
		+ Beers.SYSTEMBOLAGET_PRICE + " FLOAT,"
		+ Beers.BEERADVOCATE_BEER_ID + " INT,"
		+ Beers.ABV + " FLOAT"
		+ ");";
	
//	private static final String CREATE_TABLE_BREWERY =
//		"CREATE TABLE " + BREWERY_TABLE_NAME + "("
//		+ _ID + "INTEGER PRIMARY KEY AUTOINCREMENT,"
//		+ BREWERY + " TEXT,"
//		+ BEERADVOCATE_BREWERY_ID + " INT"
//		+ ");";
	
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
			Log.d(TAG, "Creating table \"" + Beers.TABLE_NAME + "\"");
			db.execSQL(CREATE_TABLE_BEER);
//			db.execSQL(CREATE_TABLE_BREWERY);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			Log.w(TAG , "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + Beers.TABLE_NAME);
//			db.execSQL("DROP TABLE IF EXISTS " + BREWERY_TABLE_NAME);
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
    private static final int BREWERY = 5; 
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
    	sUriMatcher.addURI(AUTHORITY, "beer", BEER);
    	sUriMatcher.addURI(AUTHORITY, "beer/#", BEER);
    	sUriMatcher.addURI(AUTHORITY, "brewery", BREWERY);
    	sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    	sUriMatcher.addURI(AUTHORITY, "clear_database", CLEAR_DATABASE);
    	sQSBProjectionMap.put(Beers._ID, Beers._ID + " AS " + BaseColumns._ID);
    	sQSBProjectionMap.put(Beers.NAME, Beers.NAME + " AS " +	SearchManager.SUGGEST_COLUMN_TEXT_1);
    	sQSBProjectionMap.put(Beers.BREWERY, Beers.BREWERY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
    	sQSBProjectionMap.put(Beers.BEERADVOCATE_BEER_ID, Beers.BEERADVOCATE_BEER_ID +  " AS " + SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);    	
    	sBeerProjectionMap.put(Beers._ID, Beers._ID);
    	sBeerProjectionMap.put(Beers.NAME, Beers.NAME);
    	sBeerProjectionMap.put(Beers.BREWERY, Beers.BREWERY); 
    	sBeerProjectionMap.put(Beers.STYLE, Beers.STYLE); 
    	sBeerProjectionMap.put(Beers.ABV, Beers.ABV); 
    	sBeerProjectionMap.put(Beers.BEERADVOCATE_RATING, Beers.BEERADVOCATE_RATING); 
    	sBeerProjectionMap.put(Beers.BEERADVOCATE_BEER_ID, Beers.BEERADVOCATE_BEER_ID); 
    	sBeerProjectionMap.put(Beers.BEERADVOCATE_BREWERY_ID, Beers.BEERADVOCATE_BREWERY_ID);
    	sBeerProjectionMap.put(Beers.SYSTEMBOLAGET_SIZE, Beers.SYSTEMBOLAGET_SIZE);
    	sBeerProjectionMap.put(Beers.SYSTEMBOLAGET_PRICE, Beers.SYSTEMBOLAGET_PRICE);
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
		return db.delete(Beers.TABLE_NAME, selection, selectionArgs);
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
				long id = getBeerId(QUERY_BA_ID, new String[] {values[i].getAsString(Beers.BEERADVOCATE_BEER_ID)});
				if (id > 0) {
					db.update(Beers.TABLE_NAME, values[i], Beers._ID + "= ?", new String[] {String.valueOf(id)});
					Log.d(TAG, "Beer with id " + id + " updated.");
					++numInserted;
				} else if (id == -1) {					
					id = db.insert(Beers.TABLE_NAME, null, values[i]);
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
		long id = getBeerId(QUERY_BA_ID, new String[] {values.getAsString(Beers.BEERADVOCATE_BEER_ID)});
		try {
			id = db.insert(Beers.TABLE_NAME, null, values);
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
		 qb.setTables(Beers.TABLE_NAME);
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
			projection = new String[] {Beers._ID, Beers.NAME, Beers.BREWERY, Beers.BEERADVOCATE_BEER_ID};
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
		Cursor c = db.query(Beers.TABLE_NAME, 
				new String[] {Beers._ID}, 
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
		} else if (!values.containsKey(Beers.NAME)) {
			Log.d(TAG, "BeerProvider.NAME key is missing.");
		} else if (!values.containsKey(Beers.STYLE)) {
			Log.d(TAG, "BeerProvider.STYLE key is missing.");
		} else if (!values.containsKey(Beers.BEERADVOCATE_RATING)) {
			Log.d(TAG, "BeerProvider.BA_RATING key is missing.");
		} else if (!values.containsKey(Beers.BREWERY)) {
			Log.d(TAG, "BeerProvider.BREWERY key is missing.");
		} else if (!values.containsKey(Beers.SYSTEMBOLAGET_SIZE)) {
			Log.d(TAG, "BeerProvider.SYSTEMBOLAGET_SIZE key is missing.");
		} else if (!values.containsKey(Beers.SYSTEMBOLAGET_PRICE)) {
			Log.d(TAG, "BeerProvider.SYSTEMBOLAGET_PRICE key is missing.");
		} else if (!values.containsKey(Beers.BEERADVOCATE_BREWERY_ID)) {
			Log.d(TAG, "BeerProvider.BA_BREWERY_ID key is missing.");
		} else if (!values.containsKey(Beers.BEERADVOCATE_BEER_ID)) {
			Log.d(TAG, "BeerProvider.BA_BEER_ID key is missing.");
		} else if (!values.containsKey(Beers.ABV)) {
			Log.d(TAG, "BeerProvider.ABV key is missing.");
		} else {
			return true;
		}
		return false;
	}
}
