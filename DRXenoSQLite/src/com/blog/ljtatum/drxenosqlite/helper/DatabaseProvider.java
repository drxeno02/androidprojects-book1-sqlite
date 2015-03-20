/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.blog.ljtatum.drxenosqlite.tables.ContentDescriptor;
import com.blog.ljtatum.drxenosqlite.tables.ExampleTable;

@SuppressWarnings("resource")
public class DatabaseProvider extends ContentProvider {

	private static final String UNKNOWN_URI = "unknown_uri";
	private DatabaseHelper dBHelper;

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		dBHelper = new DatabaseHelper(getContext());
		dBHelper.getWritableDatabase();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dBHelper.getReadableDatabase();
		final int token = ContentDescriptor.URI_MATCHER.match(uri);
		Cursor result = null;

		switch (token) {
		case ExampleTable.PATH_TOKEN: {
			result = doQuery(db, uri, ExampleTable.TABLE_NAME, projection, selection, selectionArgs,
				sortOrder);
			break;
		}
		default:
			break;
		}

		return result;
	}

	private Cursor doQuery(SQLiteDatabase db, Uri uri, String tableName, String[] projection,
		String selection, String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(tableName);
		Cursor result = builder.query(db, projection, selection, selectionArgs, sortOrder, null, null);
		result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dBHelper.getWritableDatabase();
		int token = ContentDescriptor.URI_MATCHER.match(uri);
		Uri result = null;

		switch (token) {
		case ExampleTable.PATH_TOKEN: {
			result = doInsert(db, ExampleTable.TABLE_NAME, ExampleTable.CONTENT_URI, uri, values);
			break;
		}
		default:
			break;
		}

		if (result == null) {
			throw new IllegalArgumentException(UNKNOWN_URI + uri);
		}

		return result;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		String table = null;
		int token = ContentDescriptor.URI_MATCHER.match(uri);

		switch (token) {
		case ExampleTable.PATH_TOKEN: {
			table = ExampleTable.TABLE_NAME;
			break;
		}
		default:
			break;
		}

		SQLiteDatabase db = dBHelper.getWritableDatabase();
		db.beginTransaction();

		for (ContentValues cv : values) {
			db.insert(table, null, cv);
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		return values.length;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub

		SQLiteDatabase db = dBHelper.getWritableDatabase();
		int token = ContentDescriptor.URI_MATCHER.match(uri);
		int result = 0;

		switch (token) {
		case ExampleTable.PATH_TOKEN: {
			result = doDelete(db, uri, ExampleTable.TABLE_NAME, selection, selectionArgs);
			break;
		}
		default:
			break;
		}

		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dBHelper.getWritableDatabase();
		int token = ContentDescriptor.URI_MATCHER.match(uri);
		int result = 0;

		switch (token) {
		case ExampleTable.PATH_TOKEN: {
			result = doUpdate(db, uri, ExampleTable.TABLE_NAME, selection, selectionArgs, values);
			break;
		}
		default:
			break;
		}

		return result;
	}

	private int doUpdate(SQLiteDatabase db, Uri uri, String tableName, String selection,
		String[] selectionArgs, ContentValues values) {
		int result = db.update(tableName, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	private int doDelete(SQLiteDatabase db, Uri uri, String tableName, String selection,
		String[] selectionArgs) {
		int result = db.delete(tableName, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	private Uri doInsert(SQLiteDatabase db, String tableName, Uri contentUri, Uri uri, ContentValues values) {
		long id = db.insert(tableName, null, values);
		Uri result = contentUri.buildUpon().appendPath(String.valueOf(id)).build();
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

}
