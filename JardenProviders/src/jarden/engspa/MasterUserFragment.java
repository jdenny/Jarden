package jarden.engspa;

import jarden.provider.engspa.EngSpaContract;

import com.jardenconsulting.providerdemoapp.MainActivity;
import com.jardenconsulting.providerdemoapp.R;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MasterUserFragment extends Fragment implements OnClickListener,
		OnItemClickListener, LoaderCallbacks<Cursor> {
	private ListView listView;
	private SimpleCursorAdapter adapter;
	private String[] summaryColumns = EngSpaContract.PROJECTION_ALL_USER_FIELDS;
	private MainActivity mainActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.user_list_layout, container, false);
		listView = (ListView) view.findViewById(R.id.usersListView);
		listView.setOnItemClickListener(this);
		Button newButton = (Button) view.findViewById(R.id.newButton);
		newButton.setOnClickListener(this);
		FragmentActivity activity = getActivity();
		int[] to = {R.id.userId, R.id.userName, R.id.userLevel, R.id.questionStyle};
		this.adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.user_layout, null, summaryColumns, to, 0);
		this.listView.setAdapter(adapter);
		activity.getSupportLoaderManager().initLoader(MainActivity.USER_LOADER_ID, null, this);
		return view;
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mainActivity = (MainActivity)activity;
	}

	@Override // LoaderCallbacks<Cursor>
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
		Uri uri = EngSpaContract.CONTENT_URI_USER;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		return new CursorLoader(getActivity(), uri, summaryColumns, selection,
				selectionArgs, sortOrder);
	}

	@Override // LoaderCallbacks<Cursor>
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	@Override // LoaderCallbacks<Cursor>
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View selectedRowView,
			int position, long id) {
		TextView idView = (TextView) selectedRowView.findViewById(R.id.userId);
		String idStr = idView.getText().toString();
		String uriStr = EngSpaContract.CONTENT_URI_USER_STR + "/" + idStr;
		Uri userUri = Uri.parse(uriStr);
		this.mainActivity.setUserUri(userUri);
	}
	@Override
	public void onClick(View view) {
		this.mainActivity.setUserUri(null);
	}
}
