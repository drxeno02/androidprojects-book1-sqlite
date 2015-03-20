/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.adapter;

import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blog.ljtatum.drxenosqlite.R;

@SuppressLint("InflateParams")
public class DatabaseAdapter extends BaseAdapter {

	private Context mContext;
	private LinkedHashMap<String, String> mMap;

	public DatabaseAdapter(Context context, LinkedHashMap<String, String> map) {
		mContext = context;
		mMap = map;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMap.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mMap.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.item_main, null);
			holder = new ViewHolder();
			holder.tvId = (TextView) view.findViewById(R.id.tv_id);
			holder.tvEntry = (TextView) view.findViewById(R.id.tv_entry);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Object keyObj = mMap.keySet().toArray()[position];
		Object valObj = mMap.get(keyObj);

		holder.tvId.setText("id= " + keyObj.toString());
		holder.tvEntry.setText("entry= " + valObj.toString());

		return view;
	}

	private static class ViewHolder {
		private TextView tvId, tvEntry;
	}

}
