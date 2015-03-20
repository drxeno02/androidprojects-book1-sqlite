package com.blog.ljtatum.drxenosqlite.activity;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.blog.ljtatum.drxenosqlite.helper.DatabaseHelper;

public class AndroidDatabaseManager extends Activity implements OnItemClickListener {
	private static final String TAG = AndroidDatabaseManager.class.getSimpleName();

	private DatabaseHelper dbm;
	private TableLayout tableLayout;
	private TableRow.LayoutParams tableRowParams;
	private HorizontalScrollView hSv;
	private ScrollView svMain;
	private LinearLayout llMain;
	private TextView tv1, tvMsg;
	private Button btnPrev, btnNext;
	private Spinner spinnerMainTable;

	indexInfo info = new indexInfo();

	/**
	 * Embedded static class to save cursor, table values, ect which is used by functions to share
	 * data
	 */
	public static class indexInfo {
		public static int index = 10;
		public static int numOfPgs = 0;
		public static int currPg = 0;
		public static String tableName = "";
		public static Cursor mainCursor;
		public static int cursorPos = 0;
		public static ArrayList<String> alValue;
		public static ArrayList<String> alHeaderNames;
		public static ArrayList<String> alEmptyTableColNames;
		public static boolean isEmpty;
		public static boolean isCustomQuery;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		getIds();
	}

	@SuppressWarnings("hiding")
	private void getIds() {
		dbm = new DatabaseHelper(AndroidDatabaseManager.this);
		svMain = new ScrollView(AndroidDatabaseManager.this);

		/*
		 * the main linear layout that contains all the widgets, tables, ect each of which is
		 * created and added dynamically to void using xml
		 */
		llMain = new LinearLayout(AndroidDatabaseManager.this);
		llMain.setOrientation(LinearLayout.VERTICAL);
		llMain.setBackgroundColor(Color.WHITE);
		llMain.setScrollContainer(true);
		svMain.addView(llMain);

		// required layouts are created dynamically and added to the main scrollview
		setContentView(svMain);

		// first row of layout which has a text view and spinner
		final LinearLayout llFirstRow = new LinearLayout(AndroidDatabaseManager.this);
		llFirstRow.setPadding(0, 10, 0, 20);
		LinearLayout.LayoutParams paramsFirstRow = new LinearLayout.LayoutParams(0, 150);
		paramsFirstRow.weight = 1;

		TextView tvMain = new TextView(AndroidDatabaseManager.this);
		tvMain.setText("Select Table");
		tvMain.setTextSize(22);
		tvMain.setLayoutParams(paramsFirstRow);
		spinnerMainTable = new Spinner(AndroidDatabaseManager.this);
		spinnerMainTable.setLayoutParams(paramsFirstRow);

		llFirstRow.addView(tvMain);
		llFirstRow.addView(spinnerMainTable);
		llMain.addView(llFirstRow);

		ArrayList<Cursor> alCur1;

		// horizontal scroll view for table if the table content does not fit into screen
		hSv = new HorizontalScrollView(AndroidDatabaseManager.this);

		/*
		 * main table layout where the content of the sql tables will be displayed when user selects
		 * a table
		 */
		tableLayout = new TableLayout(AndroidDatabaseManager.this);
		tableLayout.setHorizontalScrollBarEnabled(true);
		hSv.addView(tableLayout);

		// second row of the layout which shows number of records in the table selected by user
		final LinearLayout llSecondRow = new LinearLayout(AndroidDatabaseManager.this);
		llSecondRow.setPadding(0, 20, 0, 10);
		LinearLayout.LayoutParams paramsSecondRow = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
			android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		paramsSecondRow.weight = 1;
		TextView tvSecondRow = new TextView(AndroidDatabaseManager.this);
		tvSecondRow.setText("Num Of Records: ");
		tvSecondRow.setTextSize(20);
		tvSecondRow.setLayoutParams(paramsSecondRow);
		tv1 = new TextView(AndroidDatabaseManager.this);
		tv1.setTextSize(20);
		tv1.setLayoutParams(paramsSecondRow);
		llSecondRow.addView(tvSecondRow);
		llSecondRow.addView(tv1);
		llMain.addView(llSecondRow);

		// custom query editText field
		final EditText etCustomQuery = new EditText(this);
		etCustomQuery.setVisibility(View.GONE);
		etCustomQuery.setHint("Enter your query and press submit. Results will be displayed below");
		llMain.addView(etCustomQuery);

		final Button btnSubmitQuery = new Button(AndroidDatabaseManager.this);
		btnSubmitQuery.setVisibility(View.GONE);
		btnSubmitQuery.setText("Submit Query");

		btnSubmitQuery.setBackgroundColor(Color.parseColor("#BAE7F6"));
		llMain.addView(btnSubmitQuery);

		final TextView tvHelp = new TextView(AndroidDatabaseManager.this);
		tvHelp.setText("Click on any row below to update or delete values or even remove the table");
		tvHelp.setPadding(0, 7, 0, 7);

		// spinner with options add new row, drop or delete table
		final Spinner spinnerTable = new Spinner(AndroidDatabaseManager.this);
		llMain.addView(spinnerTable);
		llMain.addView(tvHelp);
		hSv.setPadding(0, 10, 0, 10);
		hSv.setScrollbarFadingEnabled(false);
		hSv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
		llMain.addView(hSv);

		// third layout which has buttons for the pagination of content from database
		final LinearLayout llThirdRow = new LinearLayout(AndroidDatabaseManager.this);
		btnPrev = new Button(AndroidDatabaseManager.this);
		btnPrev.setText("Previous");

		btnPrev.setBackgroundColor(Color.parseColor("#BAE7F6"));
		btnPrev.setLayoutParams(paramsSecondRow);
		btnNext = new Button(AndroidDatabaseManager.this);
		btnNext.setText("Next");
		btnNext.setBackgroundColor(Color.parseColor("#BAE7F6"));
		btnNext.setLayoutParams(paramsSecondRow);
		TextView tvBlank = new TextView(this);
		tvBlank.setLayoutParams(paramsSecondRow);
		llThirdRow.setPadding(0, 10, 0, 10);
		llThirdRow.addView(btnPrev);
		llThirdRow.addView(tvBlank);
		llThirdRow.addView(btnNext);
		llMain.addView(llThirdRow);

		// textView at the bottom of the screen that displays error or success messages
		tvMsg = new TextView(AndroidDatabaseManager.this);

		tvMsg.setText("Error Messages Displayed Below");
		String Query = "SELECT name _id FROM sqlite_master WHERE type ='table'";
		tvMsg.setTextSize(18);
		llMain.addView(tvMsg);

		final Button customQuery = new Button(AndroidDatabaseManager.this);
		customQuery.setText("Custom Query");
		customQuery.setBackgroundColor(Color.parseColor("#BAE7F6"));
		llMain.addView(customQuery);
		customQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// set drop down to custom Query
				indexInfo.isCustomQuery = true;
				llSecondRow.setVisibility(View.GONE);
				spinnerTable.setVisibility(View.GONE);
				tvHelp.setVisibility(View.GONE);
				etCustomQuery.setVisibility(View.VISIBLE);
				btnSubmitQuery.setVisibility(View.VISIBLE);
				spinnerMainTable.setSelection(0);
				customQuery.setVisibility(View.GONE);
			}
		});

		// display custom query results in table layout
		btnSubmitQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tableLayout.removeAllViews();
				customQuery.setVisibility(View.GONE);

				ArrayList<Cursor> alCur2;
				String strQuery1 = etCustomQuery.getText().toString();
				Log.i(TAG, "query string: " + strQuery1);

				// pass the custom query string and get the results
				alCur2 = dbm.getData(strQuery1);
				final Cursor cursorQuery = alCur2.get(0);
				@SuppressWarnings("resource")
				Cursor cursorMsg = alCur2.get(1);
				cursorMsg.moveToLast();

				// if the custom query returns results display the results in table layout
				if (cursorMsg.getString(0).equalsIgnoreCase("Success")) {
					tvMsg.setBackgroundColor(Color.parseColor("#2ecc71"));
					if (cursorQuery != null) {
						etCustomQuery.setText("");

						tvMsg.setText("Query executed successfully! Number of rows returned: " + cursorQuery.getCount());
						if (cursorQuery.getCount() > 0) {
							indexInfo.mainCursor = cursorQuery;
							refreshTable(1);
						}
					} else {
						tvMsg.setText("Query executed successfully");
						refreshTable(1);
					}
				} else {
					// display error message if conflict with custom query
					tvMsg.setBackgroundColor(Color.parseColor("#e74c3c"));
					tvMsg.setText("Error: " + cursorMsg.getString(0));
					etCustomQuery.setText("");
					// hide virtual keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(
						Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				}
			}
		});

		// layout parameters for each row in the table
		tableRowParams = new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
			android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(0, 0, 2, 0);

		/*
		 * query that return a cursor with the list of tables in the database. This cursor is used
		 * to populate the spinner in the first row
		 */
		alCur1 = dbm.getData(Query);

		// the first cursor has results of the custom query
		@SuppressWarnings("resource")
		final Cursor cursorResult = alCur1.get(0);

		// the second cursor has error messages
		@SuppressWarnings("resource")
		Cursor cursorErrorMsg = alCur1.get(1);

		cursorErrorMsg.moveToLast();
		String msg = cursorErrorMsg.getString(0);
		Log.i(TAG, "Message from sql = " + msg);

		ArrayList<String> alTableNames = new ArrayList<String>();

		if (cursorResult != null) {
			cursorResult.moveToFirst();
			// first item on main spinner; msg prompt
			alTableNames.add("<< click here >>");
			do {
				// add names of the table to arrayList
				alTableNames.add(cursorResult.getString(0));
			} while (cursorResult.moveToNext());
		}

		// array adapter with above created arrayList
		ArrayAdapter<String> mainAdapter = new ArrayAdapter<String>(AndroidDatabaseManager.this,
			android.R.layout.simple_spinner_item, alTableNames) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);

				v.setBackgroundColor(Color.WHITE);
				TextView tv = (TextView) v;
				tv.setTextSize(20);
				return tv;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);

				v.setBackgroundColor(Color.WHITE);
				return v;
			}
		};

		mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		if (mainAdapter != null) {
			// set the adapter to select table spinner (main)
			spinnerMainTable.setAdapter(mainAdapter);
		}

		// display the table contents
		spinnerMainTable.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
				if (pos == 0 && !indexInfo.isCustomQuery) {
					llSecondRow.setVisibility(View.GONE);
					hSv.setVisibility(View.GONE);
					llThirdRow.setVisibility(View.GONE);
					spinnerTable.setVisibility(View.GONE);
					tvHelp.setVisibility(View.GONE);
					tvMsg.setVisibility(View.GONE);
					etCustomQuery.setVisibility(View.GONE);
					btnSubmitQuery.setVisibility(View.GONE);
					customQuery.setVisibility(View.GONE);
				}
				if (pos != 0) {
					llSecondRow.setVisibility(View.VISIBLE);
					spinnerTable.setVisibility(View.VISIBLE);
					tvHelp.setVisibility(View.VISIBLE);
					etCustomQuery.setVisibility(View.GONE);
					btnSubmitQuery.setVisibility(View.GONE);
					customQuery.setVisibility(View.VISIBLE);
					hSv.setVisibility(View.VISIBLE);
					tvMsg.setVisibility(View.VISIBLE);
					llThirdRow.setVisibility(View.VISIBLE);
					cursorResult.moveToPosition(pos - 1);
					indexInfo.cursorPos = pos - 1;

					// display content of the table
					Log.d(TAG, "selected table name is " + cursorResult.getString(0));
					indexInfo.tableName = cursorResult.getString(0);
					tvMsg.setText("Error Messages Displayed Below");
					tvMsg.setBackgroundColor(Color.WHITE);

					// removes any data if present in the table layout
					tableLayout.removeAllViews();
					ArrayList<String> spinnerTableValues = new ArrayList<String>();
					spinnerTableValues.add("Click here to change this table");
					spinnerTableValues.add("Add row to this table");
					spinnerTableValues.add("Delete this table");
					spinnerTableValues.add("Drop this table");
					ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
						android.R.layout.simple_spinner_dropdown_item, spinnerTableValues);
					spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

					// array adapter that add values to the spinner for the user to make changes to
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(AndroidDatabaseManager.this,
						android.R.layout.simple_spinner_item, spinnerTableValues) {

						@Override
						public View getView(int position, View convertView, ViewGroup parent) {
							View v = super.getView(position, convertView, parent);
							v.setBackgroundColor(Color.WHITE);
							TextView tv = (TextView) v;
							tv.setTextSize(20);
							return tv;
						}

						@Override
						public View getDropDownView(int position, View convertView, ViewGroup parent) {
							View v = super.getDropDownView(position, convertView, parent);
							v.setBackgroundColor(Color.WHITE);
							return v;
						}
					};

					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinnerTable.setAdapter(adapter);
					String strQuery2 = "select * from " + cursorResult.getString(0);

					ArrayList<Cursor> alCur3 = dbm.getData(strQuery2);
					final Cursor c2 = alCur3.get(0);
					// save cursor to static indexInfo class to be reused later
					indexInfo.mainCursor = c2;

					// if cursor returned from database is not null display the data in table layout
					if (c2 != null) {
						int counts = c2.getCount();
						indexInfo.isEmpty = false;
						tv1.setText("" + counts);

						spinnerTable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

								((TextView) parentView.getChildAt(0)).setTextColor(Color.rgb(0, 0, 0));
								// drop table selected
								if (spinnerTable.getSelectedItem().toString().equals("Drop this table")) {
									// an alert dialog to confirm user selection
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (!isFinishing()) {
												new AlertDialog.Builder(AndroidDatabaseManager.this)
												.setTitle("Are You Sure ?")
												.setMessage("Pressing yes will remove " + indexInfo.tableName + " table from database")
												.setPositiveButton("Yes",
													new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														String strQueryDropTable = "Drop table " + indexInfo.tableName;
														Log.d(TAG, "drop table query" + strQueryDropTable);
														ArrayList<Cursor> alDropTable = dbm.getData(strQueryDropTable);
														Cursor tempCur = alDropTable.get(1);
														tempCur.moveToLast();
														Log.i(TAG, "Drop table message: " + tempCur.getString(0));
														if (tempCur.getString(0).equalsIgnoreCase("Success")) {
															tvMsg.setBackgroundColor(Color.parseColor("#2ecc71"));
															tvMsg.setText(indexInfo.tableName + "dropped successfully!");
															refreshActivity();
														} else {
															tvMsg.setBackgroundColor(Color.parseColor("#e74c3c"));
															tvMsg.setText("Error:" + tempCur.getString(0));
															spinnerTable.setSelection(0);
														}
													}
												})
												.setNegativeButton("No",
													new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														spinnerTable.setSelection(0);
													}
												})
												.create().show();
											}
										}
									});
								}

								// delete table selected
								if (spinnerTable.getSelectedItem().toString().equals("Delete this table")) {
									// an alert dialog to confirm user selection
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (!isFinishing()) {

												new AlertDialog.Builder(AndroidDatabaseManager.this)
												.setTitle("Are You Sure?")
												.setMessage("Clicking on yes will delete all the contents of " + indexInfo.tableName + " table from database")
												.setPositiveButton("Yes",
													new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														String strQueryDeleteTable = "Delete from " + indexInfo.tableName;
														Log.d(TAG, "delete table query" + strQueryDeleteTable);
														ArrayList<Cursor> alDeleteTable = dbm.getData(strQueryDeleteTable);
														Cursor tempCur = alDeleteTable.get(1);
														tempCur.moveToLast();
														Log.i("Delete table message", tempCur.getString(0));
														if (tempCur.getString(0).equalsIgnoreCase("Success")) {
															tvMsg.setBackgroundColor(Color.parseColor("#2ecc71"));
															tvMsg.setText(indexInfo.tableName + " table content deleted successfully");
															indexInfo.isEmpty = true;
															refreshTable(0);
														} else {
															tvMsg.setBackgroundColor(Color.parseColor("#e74c3c"));
															tvMsg.setText("Error:" + tempCur.getString(0));
															spinnerTable.setSelection(0);
														}
													}
												})
												.setNegativeButton("No",
													new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														spinnerTable.setSelection(0);
													}
												})
												.create().show();
											}
										}
									});

								}

								// add row to table selected
								if (spinnerTable.getSelectedItem().toString().equals("Add row to this table")) {
									final LinkedList<TextView> listAddNewRowNames = new LinkedList<TextView>();
									final LinkedList<EditText> listAddNewRowValues = new LinkedList<EditText>();
									final ScrollView svAddRow = new ScrollView(AndroidDatabaseManager.this);
									Cursor c4 = indexInfo.mainCursor;
									if (indexInfo.isEmpty) {
										getColumnNames();
										for (int i = 0; i < indexInfo.alEmptyTableColNames.size(); i++) {
											String strName = indexInfo.alEmptyTableColNames.get(i);
											TextView tv = new TextView(getApplicationContext());
											tv.setText(strName);
											listAddNewRowNames.add(tv);
										}
										for (int i = 0; i < listAddNewRowNames.size(); i++) {
											EditText et = new EditText(getApplicationContext());
											listAddNewRowValues.add(et);
										}
									} else {
										for (int i = 0; i < c4.getColumnCount(); i++) {
											String strName = c4.getColumnName(i);
											TextView tv = new TextView(getApplicationContext());
											tv1.setText(strName);
											listAddNewRowNames.add(tv);
										}
										for (int i = 0; i < listAddNewRowNames.size(); i++) {
											EditText et = new EditText(getApplicationContext());
											listAddNewRowValues.add(et);
										}
									}

									final RelativeLayout rl = new RelativeLayout(AndroidDatabaseManager.this);
									RelativeLayout.LayoutParams rlParams1 = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
									rlParams1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
									for (int i = 0; i < listAddNewRowNames.size(); i++) {
										TextView tv = listAddNewRowNames.get(i);
										EditText et = listAddNewRowValues.get(i);
										int t = i + 400;
										int k = i + 500;
										int lid = i + 600;

										tv.setId(t);
										tv.setTextColor(Color.parseColor("#000000"));
										et.setBackgroundColor(Color.parseColor("#F2F2F2"));
										et.setTextColor(Color.parseColor("#000000"));
										et.setId(k);
										final LinearLayout ll2 = new LinearLayout(AndroidDatabaseManager.this);
										LinearLayout.LayoutParams tvl = new LinearLayout.LayoutParams(0, 100);
										tvl.weight = 1;
										ll2.addView(tv, tvl);
										ll2.addView(et, tvl);
										ll2.setId(lid);

										Log.i(TAG, "EditText value: " + et.getText().toString());

										RelativeLayout.LayoutParams rlParams2 = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
										rlParams2.addRule(RelativeLayout.BELOW, ll2.getId() - 1);
										rlParams2.setMargins(0, 20, 0, 0);
										rl.addView(ll2, rlParams2);

									}

									rl.setBackgroundColor(Color.WHITE);
									svAddRow.addView(rl);

									// display alert dialog for adding onto table
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (!isFinishing()) {
												new AlertDialog.Builder(AndroidDatabaseManager.this)
												.setTitle("Values")
												.setCancelable(false)
												.setView(svAddRow)
												.setPositiveButton("Add",
													new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														indexInfo.index = 10;
														String strAddValue = "Insert into " + indexInfo.tableName + " (";
														for (int i = 0; i < listAddNewRowNames.size(); i++) {
															TextView tv = listAddNewRowNames.get(i);
															tv.getText().toString();
															if (i == listAddNewRowNames.size() - 1) {
																strAddValue = strAddValue + tv.getText().toString();
															} else {
																strAddValue = strAddValue + tv.getText().toString() + ", ";
															}
														}
														strAddValue = strAddValue + " ) VALUES ( ";
														for (int i = 0; i < listAddNewRowNames.size(); i++) {
															EditText et = listAddNewRowValues.get(i);
															et.getText().toString();
															if (i == listAddNewRowNames.size() - 1) {
																strAddValue = strAddValue + "'" + et.getText().toString() + "' ) ";
															} else {
																strAddValue = strAddValue + "'" + et.getText().toString() + "' , ";
															}
														}

														// insert query
														ArrayList<Cursor> alAddValue = dbm.getData(strAddValue);
														Cursor tempCur = alAddValue.get(1);
														tempCur.moveToLast();
														Log.d(TAG, "Adding new row: " + tempCur.getString(0));
														if (tempCur.getString(0).equalsIgnoreCase("Success")) {
															tvMsg.setBackgroundColor(Color.parseColor("#2ecc71"));
															tvMsg.setText("New row added succesfully to " + indexInfo.tableName);
															refreshTable(0);
														} else {
															tvMsg.setBackgroundColor(Color.parseColor("#e74c3c"));
															tvMsg.setText("Error: " + tempCur.getString(0));
															spinnerTable.setSelection(0);
														}

													}
												})
												.setNegativeButton("Close",
													new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														spinnerTable.setSelection(0);
													}
												})
												.create().show();
											}
										}
									});
								}
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {}
						});

						// display first row of the table with column names of the table selected by
						// user
						TableRow tableHeader = new TableRow(getApplicationContext());

						tableHeader.setBackgroundColor(Color.BLACK);
						tableHeader.setPadding(0, 2, 0, 2);
						for (int k = 0; k < c2.getColumnCount(); k++) {
							LinearLayout cell = new LinearLayout(AndroidDatabaseManager.this);
							cell.setBackgroundColor(Color.WHITE);
							cell.setLayoutParams(tableRowParams);
							final TextView tableHeaderColumns = new TextView(getApplicationContext());
							tableHeaderColumns.setPadding(0, 0, 4, 3);
							tableHeaderColumns.setText("" + c2.getColumnName(k));
							tableHeaderColumns.setTextColor(Color.parseColor("#000000"));
							cell.addView(tableHeaderColumns);
							tableHeader.addView(cell);
						}
						tableLayout.addView(tableHeader);
						c2.moveToFirst();

						// pagination that allows display of the first 10 table items
						paginateTable(c2.getCount());
					} else {
						// if cursor is empty, display empty table
						tvHelp.setVisibility(View.GONE);
						tableLayout.removeAllViews();
						getColumnNames();
						TableRow tableHeader = new TableRow(getApplicationContext());
						tableHeader.setBackgroundColor(Color.BLACK);
						tableHeader.setPadding(0, 2, 0, 2);

						LinearLayout cell = new LinearLayout(AndroidDatabaseManager.this);
						cell.setBackgroundColor(Color.WHITE);
						cell.setLayoutParams(tableRowParams);
						final TextView tableHeaderColumns = new TextView(getApplicationContext());

						tableHeaderColumns.setPadding(0, 0, 4, 3);
						tableHeaderColumns.setText("  Table Is Currently Empty  ");
						tableHeaderColumns.setTextSize(30);
						tableHeaderColumns.setTextColor(Color.RED);
						cell.addView(tableHeaderColumns);
						tableHeader.addView(cell);
						tableLayout.addView(tableHeader);
						tv1.setText("" + 0);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}

	// get column names of the empty tables and save them in an array list
	public void getColumnNames() {
		ArrayList<Cursor> alCur = dbm.getData("PRAGMA table_info(" + indexInfo.tableName + ")");
		Cursor c = alCur.get(0);
		indexInfo.isEmpty = true;
		if (c != null) {
			indexInfo.isEmpty = true;

			ArrayList<String> emptyTableColumnNames = new ArrayList<String>();
			c.moveToFirst();
			do {
				emptyTableColumnNames.add(c.getString(1));
			} while (c.moveToNext());
			indexInfo.alEmptyTableColNames = emptyTableColumnNames;
		}
	}

	// display alert dialog for update and/or delete row
	@SuppressWarnings("unused")
	public void updateDeletePopup(int row) {
		Cursor c = indexInfo.mainCursor;

		// spinner with options to update or delete the selected row
		ArrayList<String> spinnerArray = new ArrayList<String>();
		spinnerArray.add("Click Here to Change this row");
		spinnerArray.add("Update this row");
		spinnerArray.add("Delete this row");

		final ArrayList<String> alValueString = indexInfo.alValue;
		final LinkedList<TextView> alColumnNames = new LinkedList<TextView>();
		final LinkedList<EditText> alColumnValues = new LinkedList<EditText>();

		for (int i = 0; i < c.getColumnCount(); i++) {
			String strName = c.getColumnName(i);
			TextView tv = new TextView(getApplicationContext());
			tv.setText(strName);
			alColumnNames.add(tv);
		}
		for (int i = 0; i < alColumnNames.size(); i++) {
			String cv = alValueString.get(i);
			EditText et = new EditText(getApplicationContext());
			alValueString.add(cv);
			et.setText(cv);
			alColumnValues.add(et);
		}

		int lastRowId = 0;
		final RelativeLayout rl = new RelativeLayout(AndroidDatabaseManager.this);
		rl.setBackgroundColor(Color.WHITE);
		RelativeLayout.LayoutParams rlParams1 = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
			android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		rlParams1.addRule(RelativeLayout.ALIGN_PARENT_TOP);

		final ScrollView svUpdate = new ScrollView(AndroidDatabaseManager.this);
		LinearLayout llUpdate = new LinearLayout(AndroidDatabaseManager.this);
		LinearLayout.LayoutParams llParams1 = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
			android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		llParams1.setMargins(0, 20, 0, 0);

		// spinner which displays update or delete options
		final Spinner spinnerUpdate = new Spinner(getApplicationContext());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(AndroidDatabaseManager.this,
			android.R.layout.simple_spinner_item, spinnerArray) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);

				v.setBackgroundColor(Color.WHITE);
				TextView tv = (TextView) v;
				tv.setTextSize(20);
				return tv;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				v.setBackgroundColor(Color.WHITE);
				return v;
			}
		};

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerUpdate.setAdapter(adapter);
		llUpdate.setId(299);
		llUpdate.addView(spinnerUpdate, llParams1);
		RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
			android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		rlParams.addRule(RelativeLayout.BELOW, lastRowId);
		rl.addView(llUpdate, rlParams);
		for (int i = 0; i < alColumnNames.size(); i++) {
			TextView tv = alColumnNames.get(i);
			EditText et = alColumnValues.get(i);
			int t = i + 100;
			int k = i + 200;
			int lid = i + 300;

			tv.setId(t);
			tv.setTextColor(Color.parseColor("#000000"));
			et.setBackgroundColor(Color.parseColor("#F2F2F2"));

			et.setTextColor(Color.parseColor("#000000"));
			et.setId(k);
			final LinearLayout ll1 = new LinearLayout(AndroidDatabaseManager.this);
			ll1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			ll1.setId(lid);
			LinearLayout.LayoutParams llParams2 = new LinearLayout.LayoutParams(0, 100);
			llParams2.weight = 1;
			tv.setLayoutParams(llParams2);
			et.setLayoutParams(llParams2);
			ll1.addView(tv);
			ll1.addView(et);
			RelativeLayout.LayoutParams rlParams2 = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			rlParams2.addRule(RelativeLayout.BELOW, ll1.getId() - 1);
			rlParams2.setMargins(0, 20, 0, 0);
			lastRowId = ll1.getId();
			rl.addView(ll1, rlParams2);
		}

		svUpdate.addView(rl);
		// display alert dialog after created update layout
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing()) {
					new AlertDialog.Builder(AndroidDatabaseManager.this)
						.setTitle("Values")
						.setView(svUpdate)
						.setCancelable(false)
						.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String strValue = spinnerUpdate.getSelectedItem().toString();

							if (strValue.equalsIgnoreCase("Update this row")) {
								indexInfo.index = 10;
								String strQueryUpdate = "UPDATE " + indexInfo.tableName + " SET ";

								for (int i = 0; i < alColumnNames.size(); i++) {
									TextView tv = alColumnNames.get(i);
									EditText et = alColumnValues.get(i);

									if (!et.getText().toString().equals("null")) {
										strQueryUpdate = strQueryUpdate + tv.getText().toString() + " = ";
										if (i == alColumnNames.size() - 1) {
											strQueryUpdate = strQueryUpdate + "'" + et.getText().toString() + "'";
										} else {
											strQueryUpdate = strQueryUpdate + "'" + et.getText().toString() + "' , ";
										}
									}
								}
								strQueryUpdate = strQueryUpdate + " where ";
								for (int i = 0; i < alColumnNames.size(); i++) {
									TextView tvc = alColumnNames.get(i);
									if (!alValueString.get(i).equals("null")) {
										strQueryUpdate = strQueryUpdate + tvc.getText().toString() + " = ";
										if (i == alColumnNames.size() - 1) {
											strQueryUpdate = strQueryUpdate + "'" + alValueString.get(i) + "' ";
										} else {
											strQueryUpdate = strQueryUpdate + "'" + alValueString.get(i) + "' and ";
										}
									}
								}
								Log.d(TAG, "Update query: " + strQueryUpdate);
								ArrayList<Cursor> alCur1 = dbm.getData(strQueryUpdate);
								Cursor c1 = alCur1.get(1);
								c1.moveToLast();
								Log.i(TAG, "Update mesage: " + c1.getString(0));

								if (c1.getString(0).equalsIgnoreCase("Success")) {
									tvMsg.setBackgroundColor(Color.parseColor("#2ecc71"));
									tvMsg.setText(indexInfo.tableName + " table updated successfully!");
									refreshTable(0);
								} else {
									tvMsg.setBackgroundColor(Color.parseColor("#e74c3c"));
									tvMsg.setText("Error: " + c1.getString(0));
								}
							}

							// if spinner value selected is delete this row
							if (strValue.equalsIgnoreCase("Delete this row")) {
								indexInfo.index = 10;
								String strQuery = "DELETE FROM " + indexInfo.tableName + " WHERE ";

								for (int i = 0; i < alColumnNames.size(); i++) {
									TextView tvc = alColumnNames.get(i);
									if (!alValueString.get(i).equals("null")) {
										strQuery = strQuery + tvc.getText().toString() + " = ";
										if (i == alColumnNames.size() - 1) {
											strQuery = strQuery + "'" + alValueString.get(i) + "' ";
										} else {
											strQuery = strQuery + "'" + alValueString.get(i) + "' and ";
										}
									}
								}
								Log.d(TAG, "Delete query: " + strQuery);
								dbm.getData(strQuery);
								ArrayList<Cursor> alCur2 = dbm.getData(strQuery);
								Cursor c2 = alCur2.get(1);
								c2.moveToLast();
								Log.i(TAG, "Update mesage: " + c2.getString(0));

								if (c2.getString(0).equalsIgnoreCase("Success")) {
									tvMsg.setBackgroundColor(Color.parseColor("#2ecc71"));
									tvMsg.setText("Row deleted from " + indexInfo.tableName + " table");
									refreshTable(0);
								} else {
									tvMsg.setBackgroundColor(Color.parseColor("#e74c3c"));
									tvMsg.setText("Error: " + c2.getString(0));
								}
							}
						}
					})

						.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					})
					.create().show();
				}
			}
		});
	}

	public void refreshActivity() {
		finish();
		startActivity(getIntent());
	}

	public void refreshTable(int d) {
		Cursor c = null;
		tableLayout.removeAllViews();
		if (d == 0) {
			String strQuery = "select * from " + indexInfo.tableName;
			ArrayList<Cursor> alCur1 = dbm.getData(strQuery);
			c = alCur1.get(0);
			indexInfo.mainCursor = c;
		}
		if (d == 1) {
			c = indexInfo.mainCursor;
		}
		if (c != null) {
			int counts = c.getCount();
			tv1.setText("" + counts);
			TableRow tableHeader = new TableRow(getApplicationContext());
			tableHeader.setBackgroundColor(Color.BLACK);
			tableHeader.setPadding(0, 2, 0, 2);
			for (int k = 0; k < c.getColumnCount(); k++) {
				LinearLayout cell = new LinearLayout(AndroidDatabaseManager.this);
				cell.setBackgroundColor(Color.WHITE);
				cell.setLayoutParams(tableRowParams);
				final TextView tableHeaderColumns = new TextView(getApplicationContext());
				tableHeaderColumns.setPadding(0, 0, 4, 3);
				tableHeaderColumns.setText("" + c.getColumnName(k));
				tableHeaderColumns.setTextColor(Color.parseColor("#000000"));
				cell.addView(tableHeaderColumns);
				tableHeader.addView(cell);
			}
			tableLayout.addView(tableHeader);
			c.moveToFirst();

			// pagination that allows display of the first 10 table items
			paginateTable(c.getCount());
		} else {
			TableRow tableHeader = new TableRow(getApplicationContext());
			tableHeader.setBackgroundColor(Color.BLACK);
			tableHeader.setPadding(0, 2, 0, 2);

			LinearLayout cell = new LinearLayout(AndroidDatabaseManager.this);
			cell.setBackgroundColor(Color.WHITE);
			cell.setLayoutParams(tableRowParams);

			final TextView tableHeaderColumns = new TextView(getApplicationContext());
			tableHeaderColumns.setPadding(0, 0, 4, 3);
			tableHeaderColumns.setText("  Table Is Currently Empty  ");
			tableHeaderColumns.setTextSize(30);
			tableHeaderColumns.setTextColor(Color.RED);

			cell.addView(tableHeaderColumns);
			tableHeader.addView(cell);

			tableLayout.addView(tableHeader);
			tv1.setText("" + 0);
		}
	}

	// method displays tables from database in a table layout
	@SuppressWarnings("unused")
	public void paginateTable(final int number) {
		final Cursor c = indexInfo.mainCursor;
		indexInfo.numOfPgs = c.getCount() / 10 + 1;
		indexInfo.currPg = 1;
		c.moveToFirst();
		int currentRow = 0;

		// display the first 10 tables of the table selected by user
		do {
			final TableRow tableRow = new TableRow(getApplicationContext());
			tableRow.setBackgroundColor(Color.BLACK);
			tableRow.setPadding(0, 2, 0, 2);

			for (int j = 0; j < c.getColumnCount(); j++) {
				LinearLayout cell = new LinearLayout(this);
				cell.setBackgroundColor(Color.WHITE);
				cell.setLayoutParams(tableRowParams);
				final TextView tv = new TextView(getApplicationContext());

				tv.setText("" + c.getString(j));
				tv.setTextColor(Color.parseColor("#000000"));
				tv.setPadding(0, 0, 4, 3);
				cell.addView(tv);
				tableRow.addView(cell);
			}

			tableRow.setVisibility(View.VISIBLE);
			currentRow = currentRow + 1;
			tableRow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					final ArrayList<String> alValue = new ArrayList<String>();
					for (int i = 0; i < c.getColumnCount(); i++) {
						LinearLayout llColumn = (LinearLayout) tableRow.getChildAt(i);
						TextView tv = (TextView) llColumn.getChildAt(0);
						String str = tv.getText().toString();
						alValue.add(str);
					}
					indexInfo.alValue = alValue;
					// the below function will display the alert dialog
					updateDeletePopup(0);
				}
			});
			tableLayout.addView(tableRow);

		} while (c.moveToNext() && currentRow < 10);

		indexInfo.index = currentRow;

		// update the table with the previous 10 tables from the database
		btnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int startIndex = (indexInfo.currPg - 2) * 10;

				if (indexInfo.currPg == 1) {
					Toast.makeText(getApplicationContext(), "This is the first page", Toast.LENGTH_LONG).show();
				} else {
					indexInfo.currPg = indexInfo.currPg - 1;
					c.moveToPosition(startIndex);

					boolean decider = true;
					for (int i = 1; i < tableLayout.getChildCount(); i++) {
						TableRow tableRow = (TableRow) tableLayout.getChildAt(i);

						if (decider) {
							tableRow.setVisibility(View.VISIBLE);
							for (int j = 0; j < tableRow.getChildCount(); j++) {
								LinearLayout llColumn = (LinearLayout) tableRow.getChildAt(j);
								TextView columsView = (TextView) llColumn.getChildAt(0);
								columsView.setText("" + c.getString(j));
							}
							decider = !c.isLast();
							if (!c.isLast()) {
								c.moveToNext();
							}
						} else {
							tableRow.setVisibility(View.GONE);
						}

					}
					indexInfo.index = startIndex;
					Log.d(TAG, "index: " + indexInfo.index);
				}
			}
		});

		// update the table with the next 10 tables from the database
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// if there are no tuples to be shown toast that this the last page
				if (indexInfo.currPg >= indexInfo.numOfPgs) {
					Toast.makeText(getApplicationContext(), "This is the last page", Toast.LENGTH_LONG).show();
				} else {
					indexInfo.currPg = indexInfo.currPg + 1;
					boolean decider = true;
					for (int i = 1; i < tableLayout.getChildCount(); i++) {
						TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
						if (decider) {
							tableRow.setVisibility(View.VISIBLE);
							for (int j = 0; j < tableRow.getChildCount(); j++) {
								LinearLayout llColumn = (LinearLayout) tableRow.getChildAt(j);
								TextView tv = (TextView) llColumn.getChildAt(0);
								tv.setText("" + c.getString(j));
							}
							decider = !c.isLast();
							if (!c.isLast()) {
								c.moveToNext();
							}
						} else {
							tableRow.setVisibility(View.GONE);
						}
					}
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		// do nothing
	}

}
