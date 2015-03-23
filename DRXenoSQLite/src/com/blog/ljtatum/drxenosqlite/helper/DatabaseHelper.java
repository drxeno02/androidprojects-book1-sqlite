/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.blog.ljtatum.drxenosqlite.tables.ExampleTable;

/**
 * SQLite database helper
 */
@SuppressWarnings("static-method")
public class DatabaseHelper extends SQLiteOpenHelper {
	public static String TAG = DatabaseHelper.class.getSimpleName();

	public static final String KEY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS {0} ({1})";
	public static final String KEY_DROP_TABLE = "DROP TABLE IF EXISTS {0}";

	// Constants
	private static Context mContext;
	private SQLiteDatabase mDatabase;

	private static final String DATABASE_NAME = "database.db";
	private static final String DATABASE_PATH = "data/sqlite/example/";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createEntryTable(db);
	}

	private void createEntryTable(SQLiteDatabase db) {
		StringBuilder entryTableFields = new StringBuilder();
		entryTableFields.append(ExampleTable.Cols.COLUMN_ID)
			.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
			.append(ExampleTable.Cols.COLUMN_ENTRY).append(" TEXT");
		createTable(db, ExampleTable.TABLE_NAME, entryTableFields.toString());
	}

	public void createTable(SQLiteDatabase db, String name, String fields) {
		String query = MessageFormat.format(DatabaseHelper.KEY_CREATE_TABLE, name, fields);
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		System.out.println("Upgrading db version (v" + oldVersion +
			") to (v" + newVersion + ")");
		db.execSQL("DROP TABLE IF EXISTS " + ExampleTable.TABLE_NAME);
		onCreate(db);

	}

	/**
	 * Method is used to create an empty database that will be created into the default system path
	 * of the application. The former database will be overwritten
	 *
	 * @throws IOException
	 */
	public void createDatabase() throws IOException {
		boolean dBExist = checkDatabase();
		if (!dBExist) {
			getReadableDatabase();
			try {
				copyDatabase();
			} catch (Exception e) {
				Log.e(TAG, "error creating db: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method is used to copy database from local assets folder to the created database in the
	 * system folder. The copying is done via transferring bytestream
	 *
	 * @throws IOException
	 */
	private void copyDatabase() throws IOException {
		// open local db as input stream
		InputStream is = mContext.getAssets().open(DATABASE_NAME);

		// path to the new created empty db
		String outFileName = DATABASE_PATH + DATABASE_NAME;

		// open the empty db as the output stream
		OutputStream os = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte buffer[] = new byte[1024];

		int length;
		while ((length = is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}

		// close streams
		os.flush();
		os.close();
		is.close();
	}

	/**
	 * Method is used to check if the database already exists to avoid re-copying the file each time
	 * you open the application
	 *
	 * @return true if it exists, otherwise false
	 */
	public boolean checkDatabase() {
		boolean checkDb = false;
		try {
			String mPath = mContext.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator + DATABASE_NAME;
			File dbFile = new File(mPath);
			checkDb = dbFile.exists();
		} catch (Exception e) {
			Log.e(TAG, "database does not exist: " + e.toString());
			e.printStackTrace();
		}

		return checkDb;
	}

	/**
	 * Method is used to check if the database already exists to avoid re-copying the file each time
	 * you open the application
	 *
	 * @return true if it exists, otherwise false
	 * @deprecated since 4.2 update (with implementation of multi-user support), if you are testing
	 *             with a non-admin user you cannot access /data/data path. It is safer to use my
	 *             updated checkDatabase() method
	 */
	@Deprecated
	public boolean checkDatabaseOld() {
		SQLiteDatabase checkDb = null;
		try {
			String mPath = DATABASE_PATH + DATABASE_NAME;
			checkDb = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (Exception e) {
			Log.e(TAG, "error checking existing db: " + e.toString());
			e.printStackTrace();
		}

		if (checkDb != null) {
			checkDb.close();
		}
		return checkDb != null ? true : false;
	}

	/**
	 * Method is used to open the database
	 *
	 * @throws SQLException
	 */
	public void openDatabase() throws SQLException {
		String mPath = DATABASE_PATH + DATABASE_NAME;
		mDatabase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
	}

	/**
	 * Method is used to add data to database
	 *
	 * @param table
	 * @param key
	 * @param value
	 * @return
	 */
	public long addData(String table, String[] key, String[] value) {
		mDatabase = getWritableDatabase();
		if (mDatabase.isOpen()) {
			ContentValues cv = new ContentValues();
			for (int i = 0; i < key.length; i++) {
				cv.put(key[i], value[i]);
			}
			return mDatabase.insert(table, null, cv);
		}
		return 0;
	}

	/**
	 * Method is used to add data to database
	 *
	 * @param context
	 * @param entry
	 */
	public void addData(Context context, String value) {
		mDatabase = getWritableDatabase();
		if (mDatabase.isOpen()) {
			try {
				ContentValues cv = getContentValuesEntryTable(value);
				ContentResolver resolver = context.getContentResolver();
				Cursor cursor = resolver.query(ExampleTable.CONTENT_URI, null, null, null, null);
				resolver.insert(ExampleTable.CONTENT_URI, cv);
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private ContentValues getContentValuesEntryTable(String value) {
		ContentValues cv = new ContentValues();
		cv.put(ExampleTable.Cols.COLUMN_ENTRY, value);
		return cv;
	}

	/**
	 * Method is used to update data on database
	 *
	 * @param table
	 * @param key
	 * @param value
	 * @param whereClause
	 * @return
	 */
	public long upgradeData(String table, String[] key, String[] value, String whereClause) {
		mDatabase = getWritableDatabase();
		if (mDatabase.isOpen()) {
			ContentValues cv = new ContentValues();
			for (int i = 0; i < key.length; i++) {
				cv.put(key[i], value[i]);
			}
			return mDatabase.update(table, cv, whereClause, null);
		}
		return 0;
	}

	/**
	 * Method is used to retrieve stored data
	 *
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public Cursor getAllData(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		Cursor cursor = null;
		if (mDatabase != null) {
			if (!mDatabase.isOpen()) {
				mDatabase = getWritableDatabase();
			}

			if (mDatabase.isOpen()) {
				cursor = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
			}
		}
		return cursor;
	}

	/**
	 * Method is used to retrieve stored data
	 *
	 * @param context
	 * @param entry
	 * @return
	 */
	public Cursor getEntry(Context context, String entry) {
		Cursor cursor = null;
		String sorting = null;

		if (mDatabase != null) {
			if (!mDatabase.isOpen()) {
				mDatabase = getWritableDatabase();
			}

			if (mDatabase.isOpen()) {
				if (TextUtils.isEmpty(entry)) {
					cursor = context.getContentResolver().query(ExampleTable.CONTENT_URI, null, null, null, sorting);
				} else {
					cursor = context.getContentResolver().query(ExampleTable.CONTENT_URI, null, ExampleTable.Cols.COLUMN_ENTRY + " ='" + entry + "'", null, sorting);
				}

				if (cursor != null) {
					cursor.moveToFirst();
				}
			}
		}
		return cursor;
	}

	/**
	 * Method is used to delete a record from a given table
	 *
	 * @param table
	 * @param condition
	 * @return
	 */
	public long deleteRecord(String table, String condition) {
		mDatabase = getWritableDatabase();
		if (mDatabase.isOpen()) {
			return mDatabase.delete(table, condition, null);
		}
		return 0;
	}

	@Override
	public void close() {
		if (mDatabase != null) {
			mDatabase.close();
		}
		super.close();
	}

	/**
	 * Method is used to check the existence of any given table
	 *
	 * @param tableName
	 * @param openDb
	 * @return
	 */
	public boolean isTableExists(String tableName, boolean openDb) {
		if (openDb) {
			if (mDatabase == null || !mDatabase.isOpen()) {
				mDatabase = getReadableDatabase();
			}

			if (!mDatabase.isReadOnly()) {
				mDatabase.close();
				mDatabase = getReadableDatabase();
			}
		}

		Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
	}

	/**
	 * Method is used to query data for advance mode
	 *
	 * @param Query
	 * @return
	 */
	public ArrayList<Cursor> getData(String Query) {
		mDatabase = getWritableDatabase();
		String[] strArry = new String[] {"mesage"};

		/*
		 * arrayList of two cursors, one has results from the query and the other stores error
		 * messages if any errors are triggered
		 */
		ArrayList<Cursor> alCursor = new ArrayList<Cursor>(2);
		MatrixCursor cursor = new MatrixCursor(strArry);
		alCursor.add(null);
		alCursor.add(null);

		try {
			String maxQuery = Query;
			Cursor c = mDatabase.rawQuery(maxQuery, null);

			// add value (message) to cursor
			cursor.addRow(new Object[] {"Success"});

			alCursor.set(1, cursor);
			if (null != c && c.getCount() > 0) {
				alCursor.set(0, c);
				c.moveToFirst();
				return alCursor;
			}
			return alCursor;
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
			// save the error message to cursor and return arrayList
			cursor.addRow(new Object[] {"" + e.getMessage()});
			alCursor.set(1, cursor);
			return alCursor;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			// save the error message to cursor and return arrayList
			cursor.addRow(new Object[] {"" + e.getMessage()});
			alCursor.set(1, cursor);
			return alCursor;
		}
	}
}
