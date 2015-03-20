/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.activity;

import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.blog.ljtatum.drxenosqlite.R;
import com.blog.ljtatum.drxenosqlite.adapter.DatabaseAdapter;
import com.blog.ljtatum.drxenosqlite.helper.DatabaseHelper;
import com.blog.ljtatum.drxenosqlite.tables.ExampleTable;
import com.blog.ljtatum.drxenosqlite.utils.ValidationUtils;

public class MainActivity extends Activity implements OnClickListener {

	private Context mContext;
	private DatabaseAdapter mDbAdapter;
	private static DatabaseHelper db;
	private Button btnAdd, btnDelete, btnAdvance;
	private EditText etAdd, etDelete;
	private ListView lv;

	/*
	 * Note that entries are iterated in arbitrary order if using HashMap. For specific order,
	 * LinkedHashMap was used
	 */
	private static LinkedHashMap<String, String> map;
	private ValidationUtils validationUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getIds();
	}

	private void getIds() {
		mContext = MainActivity.this;
		map = new LinkedHashMap<String, String>();
		db = new DatabaseHelper(mContext);
		btnAdd = (Button) findViewById(R.id.btn_add);
		btnDelete = (Button) findViewById(R.id.btn_delete);
		btnAdvance = (Button) findViewById(R.id.btn_advance);
		etAdd = (EditText) findViewById(R.id.edt_add);
		etDelete = (EditText) findViewById(R.id.edt_delete);
		lv = (ListView) findViewById(R.id.lv);

		btnAdd.setOnClickListener(this);
		btnDelete.setOnClickListener(this);
		btnAdvance.setOnClickListener(this);

		validationUtils = new ValidationUtils(mContext, new EditText[] {etAdd, etDelete});
		mDbAdapter = new DatabaseAdapter(mContext, map);
		lv.setAdapter(mDbAdapter);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_add:
			etDelete.setError(null);
			if (validationUtils.isValidData(etAdd.getText().toString().trim(),
				etDelete.getText().toString().trim(), false)) {
				db.addData(mContext, etAdd.getText().toString().trim());
				etAdd.setText("");
				updateList();
			}
			break;
		case R.id.btn_delete:
			etAdd.setError(null);
			if (validationUtils.isValidData(etAdd.getText().toString().trim(),
				etDelete.getText().toString().trim(), true)) {

				if (db.checkDatabase()) {
					db.deleteRecord(ExampleTable.TABLE_NAME, ExampleTable.Cols.COLUMN_ID +
						" = " + Integer.parseInt(etDelete.getText().toString()));
					db.close();
				}
				etDelete.setText("");
				updateList();
			}
			break;
		case R.id.btn_advance:
			Intent dbmanager = new Intent(MainActivity.this, AndroidDatabaseManager.class);
			startActivity(dbmanager);
			break;
		default:
			break;
		}
	}

	private void updateList() {
		validationUtils.clearError();

		if (!map.isEmpty()) {
			map.clear();
		}

		Cursor cursor = db.getEntry(mContext, "");
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				if (cursor.moveToFirst()) {
					do {
						map.put(cursor.getString(cursor.getColumnIndex(ExampleTable.Cols.COLUMN_ID)),
							cursor.getString(cursor.getColumnIndex(ExampleTable.Cols.COLUMN_ENTRY)));
					} while (cursor.moveToNext());
				}
			}
			cursor.close();
		}

		mDbAdapter.notifyDataSetChanged();
	}
}
