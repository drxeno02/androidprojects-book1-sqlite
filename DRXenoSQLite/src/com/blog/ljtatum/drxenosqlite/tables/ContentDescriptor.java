/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.tables;

import android.content.UriMatcher;
import android.net.Uri;

public class ContentDescriptor {
	public static final String AUTHORITY = "com.blog.ljtatum";
	public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	public static final UriMatcher URI_MATCHER = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		// add as many tables as you want below
		matcher.addURI(AUTHORITY, ExampleTable.PATH, ExampleTable.PATH_TOKEN);
		return matcher;
	}
}
