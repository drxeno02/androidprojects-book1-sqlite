/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.tables;

import android.net.Uri;
import android.provider.BaseColumns;

public class ExampleTable {

	public static final String TABLE_NAME = "example";
	public static final String PATH = "example";
	public static final int PATH_TOKEN = 2015;
	public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

	public static class Cols implements BaseColumns {

		// Table column names
		public static final String COLUMN_ID = "Id";
		public static final String COLUMN_ENTRY = "Entry";
	}
}
