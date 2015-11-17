package com.jardenconsulting.spanishapp;

import java.io.IOException;
import java.util.List;

import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaQuiz.QuizEventListener;
import jarden.engspa.EngSpaUser;
import jarden.engspa.EngSpaUtils;
import jarden.http.MyHttpClient;
import jarden.provider.engspa.EngSpaContract;
import jarden.quiz.QuizCache;

import com.jardenconsulting.spanishapp.UserDialog.UserSettingsListener;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
		implements UserSettingsListener,
		NewDBDataDialog.UpdateDBListener, QuizEventListener {
    public static final String TAG = "SpanishMain";
    private static final String DATA_VERSION = "DataVersion";
	private static final String VERB_TABLE = "VERB_TABLE";
	private static final String ENGSPA = "ENGSPA";
	private EngSpaFragment engSpaFragment;
	/*
	 * userLevel can be incremented by EngSpaQuiz when user answered
	 * enough questions, or set by user invoking options menu item
	 * UserDialog at any time.
	 * EngSpaQuiz ->
	 * 		MainActivity.onNewLevel() [I/F QuizEventListener] ->
	 * 			EngSpaFragment.onNewLevel()
	 * UserDialog ->
	 * 		MainActivity.onUserUpdate() [I/F UserSettingsListener] ->
	 *			EngSpaQuiz.setUserLevel()
	 * 			EngSpaFragment.onNewLevel()
	 */
	private EngSpaUser engSpaUser;
	private VerbTableFragment verbTableFragment;
	private ProgressBar progressBar;
	private TextView statusTextView;
	private boolean engSpaFileModified;
	private long dateEngSpaFileModified;
	private String updateStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (savedInstanceState != null) {
			this.engSpaFragment = (EngSpaFragment) fragmentManager.findFragmentByTag(ENGSPA);
		}
		setContentView(R.layout.activity_main);
		this.statusTextView = (TextView) findViewById(R.id.statusTextView);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		this.engSpaUser = loadUserFromDB();
		if (this.engSpaUser == null) { // i.e. no user yet on database
			showUserDialog();
		} else {
			showEngSpaFragment();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.userSettings) {
			showUserDialog();
			return true;
		} else if (id == R.id.verbTable) {
			if (this.verbTableFragment == null) {
				this.verbTableFragment = new VerbTableFragment();
			}
			showFragment(verbTableFragment, VERB_TABLE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void showUserDialog() {
		// this seems as if we are re-creating the dialog each time
		// it is shown, but user-groups suggest that Android automatically
		// re-uses the dialog if it's already been created. This doesn't
		// seem to be the case for me!
		// TODO: investigate further.
		DialogFragment dialog = new UserDialog();
		dialog.show(getSupportFragmentManager(), "UserSettingsDialog");
	}
	/* Update EngSpa table on database if engspa.txt on server has been updated.
	 * get dateEngSpaModified from url of engspaversion.txt on server
	 * get savedVersion from SharedPreferences
	 * if there is a new version:
	 * 		ask user for confirmation of update
	 * 		read new data from engspa.txt on server
	 *		delete all rows in EngSpa
	 *		add new data to EngSpa
	 *		set SharedPreferences to latestVersion
	 */
	public void checkDataFileVersion() {
		engSpaFileModified = false;
		this.statusTextView.setText("checking for updates...");
		this.progressBar.setVisibility(ProgressBar.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				SharedPreferences prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);
				long savedVersion = prefs.getLong(DATA_VERSION, 0);
				try {
					dateEngSpaFileModified = MyHttpClient.getLastModified(
							QuizCache.serverUrlStr + "engspa.txt?attredirects=0&d=1");
					engSpaFileModified = dateEngSpaFileModified > savedVersion;
				} catch (IOException e) {
					Log.e(TAG, "Exception in checkDataFileVersion: " + e);
				}
				runOnUiThread(new Runnable() {
					public void run() {
						statusTextView.setText("");
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						dataFileCheckComplete(engSpaFileModified);
					}
				});
			}
		}).start();
	}
	/*
	 * if new version of data file, ask user to confirm update
	 */
	private void dataFileCheckComplete(boolean updated) {
		if (updated) {
			DialogFragment dialog = new NewDBDataDialog();
			dialog.show(getSupportFragmentManager(), "New Dictionary Version");
		} else endOfUpdate();
	}
	private void endOfUpdate() {
		engSpaFragment.loadDB();
	}
	
	/**
	 * Process response from NewDBDataDialog; if confirmed, get new
	 * engspa.txt file from server and use it to update the database.
	 * This needs to be done in background thread, because of access to
	 * network, and because of the delay while updating the database.
	 * Note that bulk load is done via content loader, so automatically
	 * run as background thread.
	 */
	@Override // NewDBDataDialog.UpdateDBListener
	public void onUpdateDecision(boolean doUpdate) {
		if (doUpdate) {
			this.statusTextView.setText("loading new dictionary version...");
			this.progressBar.setVisibility(ProgressBar.VISIBLE);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						List<String> engSpaLines = MyHttpClient.getPageLines(
								QuizCache.serverUrlStr + "engspa.txt?attredirects=0&d=1", "iso-8859-1");
						ContentValues[] contentValues = EngSpaUtils.getContentValuesArray(engSpaLines);
						ContentResolver contentResolver = getContentResolver();
						int rowCt = contentResolver.delete(EngSpaContract.CONTENT_URI_ENGSPA, null, null);
						Log.i(TAG, "rows deleted from database: " + rowCt);
						rowCt = contentResolver.bulkInsert(EngSpaContract.CONTENT_URI_ENGSPA, contentValues);
						Log.i(TAG, "rows inserted to database: " + rowCt);
						SharedPreferences prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putLong(DATA_VERSION, dateEngSpaFileModified);
						editor.commit();
						updateStatus = "";
					} catch (IOException e) {
						Log.e(TAG, "Exception in onUpdateDecision: " + e);
						updateStatus = "dictionary update failed; using existing version";
					}
					runOnUiThread(new Runnable() {
						public void run() {
							statusTextView.setText(updateStatus);
							progressBar.setVisibility(ProgressBar.INVISIBLE);
							endOfUpdate();
						}
					});
				}
			}).start();
		} else endOfUpdate();
	}
	
	public EngSpaQuiz getEngSpaQuiz() {
		return this.engSpaFragment.getEngSpaQuiz();
	}
	public void showEngSpaFragment() {
		if (this.engSpaFragment == null) {
			this.engSpaFragment = new EngSpaFragment();
		}
		showFragment(engSpaFragment, ENGSPA);
	}
	private void showFragment(Fragment fragment, String tag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragmentLayout, fragment, tag);
		if (tag == VERB_TABLE) ft.addToBackStack(tag);
		ft.commit();
	}

	private EngSpaUser loadUserFromDB() {
		ContentResolver contentResolver = getContentResolver();
		String selection = null;
		String sortOrder = null;
		Cursor cursor = contentResolver.query(
				EngSpaContract.CONTENT_URI_USER,
				EngSpaContract.PROJECTION_ALL_USER_FIELDS,
				selection, null, sortOrder);
		if (cursor.moveToNext()) {
			int userId = cursor.getInt(0);
			String userName = cursor.getString(1);
			int userLevel = cursor.getInt(2);
			int questionStyleIndex = cursor.getInt(3);
			EngSpaUser user = new EngSpaUser(userId, userName, userLevel,
					questionStyleIndex);
			Log.i(MainActivity.TAG, "retrieved from database: " + user);
			return user;
		} else return null;
	}

	/**
	 * Called on completion of UserDialog, which was called either
	 * because there is no EngSpaUser yet defined, or because the
	 * user chose to update it.
	 * 		if new user
	 * 			create on database
	 * 			save in this.engSpaUser
	 * 			showEngSpaFragment
	 * 		else
	 * 			update on database
	 * 			update this.engSpaUser
	 */
	@Override // UserSettingsListener
	public void onUpdateUser(String userName, int userLevel, int questionStyle) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.onUpdateUser(" + userName + ", " + userLevel +
				", " + questionStyle + ")");
		if (userName.trim().length() < 1) {
			this.statusTextView.setText("no user name supplied");
			return;
		}
		if (userLevel < 1) {
			this.statusTextView.setText("invalid userLevel supplied");
			return;
		}
		int maxUserLevel = this.engSpaFragment.getEngSpaQuiz().getMaxUserLevel();
		if (userLevel > maxUserLevel) {
			userLevel = maxUserLevel;
			this.statusTextView.setText("userLevel set to maximum");
		}
		boolean newUser = (this.engSpaUser == null);
		boolean newLevel = !newUser && (this.engSpaUser.getUserLevel() != userLevel);
		String uriStr;
		if (newUser) {
			uriStr = EngSpaContract.CONTENT_URI_USER_STR;
			this.engSpaUser = new EngSpaUser(userName, userLevel, questionStyle);
		} else {
			uriStr = EngSpaContract.CONTENT_URI_USER_STR + "/" +
					engSpaUser.getUserId();
			this.engSpaUser.setUserName(userName);
			if (this.engSpaUser.getUserLevel() != userLevel) {
				this.engSpaUser.setUserLevel(userLevel);
				this.engSpaFragment.onNewLevel(userLevel);
			}
			this.engSpaUser.setQuestionStyleIndex(questionStyle);
		}
		Uri userUri = Uri.parse(uriStr);
		ContentValues contentValues = new ContentValues();
		contentValues.put(EngSpaContract.NAME, engSpaUser.getUserName());
		contentValues.put(EngSpaContract.LEVEL, engSpaUser.getUserLevel());
		contentValues.put(EngSpaContract.QUESTION_STYLE, engSpaUser.getQuestionStyleIndex());
		ContentResolver contentResolver = getContentResolver();
		String message;
		if (newUser) {
			userUri = contentResolver.insert(userUri, contentValues);
			message = "row inserted: " + userUri.getPath();
		} else {
			int rows = contentResolver.update(userUri, contentValues, null, null);
			message = rows + " row updated";
		}
		if (BuildConfig.DEBUG) {
			Log.d(TAG, message);
		}
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		if (newUser) {
			showEngSpaFragment();
		} else if (newLevel) {
			getEngSpaQuiz().setUserLevel(userLevel);
			this.engSpaFragment.onNewLevel(userLevel);
		}
	}
	@Override // UserSettingsListener
	public EngSpaUser getEngSpaUser() {
		return this.engSpaUser;
	}
	
	/**
	 * Notification from EngSpaQuiz that the user has moved up to
	 * the next level. Update EngSpaUser on the database, and
	 * inform EngSpaFragment.
	 */
	@Override // QuizEventListener
	public void onNewLevel(int userLevel) {
		String uriStr = EngSpaContract.CONTENT_URI_USER_STR + "/" +
				engSpaUser.getUserId();
		ContentValues contentValues = new ContentValues();
		contentValues.put(EngSpaContract.LEVEL, userLevel);
		int rows = getContentResolver().update(
				Uri.parse(uriStr), contentValues, null, null);
		String message = rows + " row updated";
		if (BuildConfig.DEBUG) {
			Log.d(TAG, message);
		}
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		this.engSpaUser.setUserLevel(userLevel);
		this.engSpaFragment.onNewLevel(userLevel);
	}
}
