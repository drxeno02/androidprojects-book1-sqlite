/** Project and code provided by Leonard Tatum
 * For any questions or comments regarding the use of this code
 * or issues please contact LJTATUM@HOTMAIL.COM
 * ONLINE MOBILE TUTORIALS: ljtatum.blog.com/
 * GITHUB: https://github.com/drxeno02/androidprojects-book1-sqlite */

package com.blog.ljtatum.drxenosqlite.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

import com.blog.ljtatum.drxenosqlite.R;

public class ValidationUtils {

	private static Context mContext;
	private static EditText[] mFieldsArry;

	public ValidationUtils(Context context, EditText[] fieldsArry) {
		mContext = context;
		mFieldsArry = fieldsArry;
	}

	public boolean isValidData(String addEntry, String deleteEntry, boolean isDelete) {
		boolean isAddEntry = !TextUtils.isEmpty(addEntry);
		boolean isDeleteEntry = !TextUtils.isEmpty(deleteEntry);

		if (!isDelete) {
			if (isAddEntry) {
				return true;
			}
			setError(0, mContext.getString(R.string.et_empty));
		} else {
			if (isDeleteEntry) {
				return true;
			}
			setError(1, mContext.getString(R.string.et_empty));
		}
		return false;
	}

	public void setError(int index, String error) {
		mFieldsArry[index].setError(error);
	}

	public void clearError() {
		for (int i = 0; i < mFieldsArry.length; i++) {
			mFieldsArry[i].setError(null);
		}
	}
}
