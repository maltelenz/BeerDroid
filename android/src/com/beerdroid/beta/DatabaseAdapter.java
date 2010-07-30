package com.beerdroid.beta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A utility class for running database queries
 * on the local sqlite database.
 * @author Malte Lenz
 *
 */
public class DatabaseAdapter {

	//Columns in the table
	/** Row id name. */
	public static final String KEY_ROW_ID = "_id";
	/** beer column name. */
	public static final String KEY_BEER_NAME = "beer_name";
	/** style column name. */
	public static final String KEY_STYLE = "style";
	/** beeradvocate.com rating column name. */
	public static final String KEY_BA_RATING = "ba_rating";
	/** brewery name column name. */
	public static final String KEY_BREWERY_NAME = "brewery_name";
	/** size at systembolaget column name. */
	public static final String KEY_SYSTEMET_SIZE = "systemet_size";
	/** price at systembolaget column name. */
	public static final String KEY_SYSTEMET_PRICE = "systemet_price";
	/** brewery id at beeradvocate.com column name. */
	public static final String KEY_BA_BREWERY_ID = "ba_brewery_id";
	/** beer id at beeradvocate.com column name. */
	public static final String KEY_BA_BEER_ID = "ba_beer_id";
	/** beer abv column name. */
	public static final String KEY_ABV = "abv";

	//name of the database
	private static final String DATABASE_NAME = "data";

	//name of the table
	private static final String BEER_TABLE_NAME = "beer";

	//version of the database. Increase whenever database schema is changed.
	private static final int DATABASE_VERSION = 1;

	//string used for logging
	private static final String TAG = "BeerDroid DatabaseAdapter";

	//query for creating the beer table
	private static final String CREATE_TABLE_BEER =
		"create table " + BEER_TABLE_NAME + "("
		+ KEY_ROW_ID + " integer primary key autoincrement,"
		+ KEY_BEER_NAME + " text not null,"
		+ KEY_STYLE + " text,"
		+ KEY_BA_RATING + " text,"
		+ KEY_BREWERY_NAME + " text,"
		+ KEY_SYSTEMET_SIZE + " int,"
		+ KEY_SYSTEMET_PRICE + " float,"
		+ KEY_BA_BREWERY_ID + " int,"
		+ KEY_BA_BEER_ID + " int,"
		+ KEY_ABV + " float"
		+ ");";

	private DatabaseHelper mDbHelper;

	private SQLiteDatabase mDb;

	//Context from calling activity
	private final Context mCtx;

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

	/**
	 * save the context for later use.
	 * @param ctx context from calling activity
	 */
	public DatabaseAdapter(final Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Fetch a writable database.
	 * @return this
	 */
	public final DatabaseAdapter open() {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close the database.
	 */
	public final void close() {
		mDbHelper.close();
	}

	/**
	 * Saves a new beer to database. Does not check for duplicates!
	 * @param beer a beer object
	 * @return KEY_ROW_ID of saved beer
	 */
	public final long createBeer(final Beer beer) {
		final ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_BEER_NAME, beer.name);
		initialValues.put(KEY_STYLE, beer.style);
		initialValues.put(KEY_BA_RATING, beer.baRating);
		initialValues.put(KEY_BREWERY_NAME, beer.breweryName);
		initialValues.put(KEY_SYSTEMET_SIZE, beer.systemetSize);
		initialValues.put(KEY_SYSTEMET_PRICE, beer.systemetPrice);
		initialValues.put(KEY_BA_BREWERY_ID, beer.baBrewery);
		initialValues.put(KEY_BA_BEER_ID, beer.baBeer);
		initialValues.put(KEY_ABV, beer.abv);
		return mDb.insert(BEER_TABLE_NAME, null, initialValues);
	}

	/**
	 * Returns true if a beer already exists in the database.
	 * For now only uses the name.
	 * @param beer the beer object to check
	 * @return if given beer exists
	 */
	public final boolean beerExists(final Beer beer) {
		final Cursor beerCursor = mDb.query(true, //is distinct
				BEER_TABLE_NAME, //name of table
				new String[] {KEY_ROW_ID}, //fields to fetch
				KEY_BEER_NAME + " LIKE " + DatabaseUtils.sqlEscapeString(beer.name), //restrictions (in sql WHERE clause form)
				null, //selection arguments
				null, //group by
				null, //having
				null, //order by
				null //limit number of results
		);
		boolean doesExist = false;
		if (beerCursor.getCount() > 0) {
			doesExist = true;
		}
		beerCursor.close();
		return doesExist;
	}
}
