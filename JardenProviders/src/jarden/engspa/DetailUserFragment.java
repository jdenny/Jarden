package jarden.engspa;

import jarden.provider.engspa.EngSpaContract;

import com.jardenconsulting.providerdemoapp.BuildConfig;
import com.jardenconsulting.providerdemoapp.MainActivity;
import com.jardenconsulting.providerdemoapp.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class DetailUserFragment extends Fragment implements OnClickListener {

	private EditText nameEdit;
	private EditText levelEdit;
	private Spinner questionStyleSpinner;
	private Uri userUri;
	private MainActivity mainActivity;
	private ContentResolver contentResolver;
	private View view; // Main Layout View
	private Button updateButton;
	private Button deleteButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "DetailUserFragment.onCreateView()");
		if (savedInstanceState != null) {
			String uriStr = savedInstanceState.getString(EngSpaContract.CONTENT_URI_USER_STR);
			Uri uri = Uri.parse(uriStr);
			String idStr = uri.getLastPathSegment();
			if (idStr != null && !idStr.equals(EngSpaContract.USER_TABLE)) {
				userUri = uri;
			}
		}
		view = inflater.inflate(R.layout.user_edit_layout, container, false);
		Context context = view.getContext();
		nameEdit = (EditText) view.findViewById(R.id.name);
		levelEdit = (EditText) view.findViewById(R.id.level);

		questionStyleSpinner = (Spinner) view.findViewById(R.id.questionStyleSpinner);
		ArrayAdapter<String> questionStyleAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item, EngSpaContract.QUESTION_STYLES);
		questionStyleSpinner.setAdapter(questionStyleAdapter);
		Button newButton = (Button) view.findViewById(R.id.newButton);
		newButton.setOnClickListener(this);
		updateButton = (Button) view.findViewById(R.id.updateButton);
		updateButton.setOnClickListener(this);
		deleteButton = (Button) view.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(this);
		return view;
	}
	@Override
	public void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG) {
			Log.d(MainActivity.TAG, "DetailUserFragment.onResume(" +
					this.userUri + ")");
		}
		/*
		 * This code is put here because of the following sequence of events
		 * triggered when user selects a word from MasterFragment:
		 * 		masterFragment passes selected word back to mainActivity
		 * 		mainActivity calls detailUserFragment.setWordUri()
		 * 		android calls detailUserFragment.onCreateView() and restores UI fields
		 * 			to values set when detailUserFragment was previously shown; these are old!
		 * 		android calls detailUserFragment.onResume(), which sets UI fields to correct values
		 */
		if (this.userUri == null) {
			this.updateButton.setEnabled(false);
			this.deleteButton.setEnabled(false);
		} else {
			showUser();
		}
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		String message;
		if (viewId == R.id.newButton) {
			Uri uri = contentResolver.insert(EngSpaContract.CONTENT_URI_USER, getContentValues());
			message = "row inserted: " + uri.getPath();
		} else if (viewId == R.id.updateButton) {
			int rows = contentResolver.update(userUri, getContentValues(), null, null);
			message = rows + " row updated";
		} else if (viewId == R.id.deleteButton) {
			int rows = contentResolver.delete(userUri, null, null);
			message = rows + " row deleted";
		} else {
			message = "onClick(), unrecognised viewId: " + viewId;
		}
		this.mainActivity.setStatus(message);
	}
	
	public void setUserUri(MainActivity mainActivity, Uri userUri) {
		if (BuildConfig.DEBUG) {
			Log.d(MainActivity.TAG, "DetailUserFragment.setUserUri(" +
					userUri + ")");
		}
		this.mainActivity = mainActivity;
		this.contentResolver = mainActivity.getContentResolver();
		this.userUri = userUri;
	}
	private void showUser() {
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		Cursor cursor = this.contentResolver.query(
				this.userUri, EngSpaContract.PROJECTION_ALL_USER_FIELDS, selection,
				selectionArgs, sortOrder);
		if (cursor.moveToFirst()) {
			String name = cursor.getString(1);
			String level = cursor.getString(2);
			int questionStyleIndex = cursor.getInt(3);
			this.nameEdit.setText(name);
			this.levelEdit.setText(level);
			this.questionStyleSpinner.setSelection(questionStyleIndex);
			if (BuildConfig.DEBUG) {
				Log.d(MainActivity.TAG,
						"DetailUserFragment.showUser(); name=" + name +
						", level=" + level + ", questionStyleIndex=" +
						questionStyleIndex);
			}
			mainActivity.setStatus("");
		} else {
			mainActivity.setStatus("no matching word found!");
		}
	}
	
	private ContentValues getContentValues() {
		String name = nameEdit.getText().toString();
		Integer level = Integer.valueOf(levelEdit.getText().toString());
		Integer questionStyle = questionStyleSpinner.getSelectedItemPosition();

		ContentValues values = new ContentValues();
		values.put(EngSpaContract.NAME, name);
		values.put(EngSpaContract.LEVEL, level);
		values.put(EngSpaContract.QUESTION_STYLE, questionStyle);
		return values;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
        if(BuildConfig.DEBUG) {
        	Log.d(MainActivity.TAG, "PhoneBookActivity.onSaveInstanceState(" +
        			(outState==null?"null":"not null") +")");
        }
		super.onSaveInstanceState(outState);
		outState.putString(EngSpaContract.CONTENT_URI_STR, this.userUri.toString());
	}

}
