package com.jardenconsulting.spanishapp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jarden.app.race.RaceFragment;
import jarden.engspa.EngSpaDAO;
import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaUser;
import jarden.engspa.EngSpaUtils;
import jarden.http.MyHttpClient;
import jarden.provider.engspa.EngSpaContract.QuestionStyle;
import jarden.quiz.QuizCache;

import com.jardenconsulting.spanishapp.EngSpaFragment.EngSpaActivity;
import com.jardenconsulting.spanishapp.UserDialog.UserSettingsListener;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/*
 * TODO: add a flow control method:
	new engSpaFragment ->
		new engSpaDAO; get engSpaUser
		if no engSpaUser: create new one with default values
	if can access updated engspa.txt on web site:
		showConfirmUpdateDBDialog
		if confirm:
			call engSpaFragment to update DB
	call engSpaFragment to create engSpaQuiz
 */
public class MainActivity extends AppCompatActivity
		implements EngSpaActivity, UserSettingsListener, OnInitListener,
		NewDBDataDialog.UpdateDBListener, TopicDialog.TopicListener,
		ListView.OnItemClickListener {
    public static final String TAG = "SpanishMain";
	public static final Locale LOCALE_ES = new Locale("es", "ES");

    private static String questionSequenceKey = null;
    private static final String DATA_VERSION_KEY = "DataVersion";
	private static final String VERB_TABLE = "VERB_TABLE";
	private static final String NUMBER_GAME = "NUMBER_GAME";
	private static final String ENGSPA = "ENGSPA";
	private EngSpaFragment engSpaFragment;
	private DialogFragment userDialog;
	private VerbTableFragment verbTableFragment;
	private RaceFragment raceFragment;
	private ProgressBar progressBar;
	private TextView statusTextView;
	private boolean engSpaFileModified;
	private long dateEngSpaFileModified;
	private String updateStatus;
	private TextToSpeech textToSpeech;
	private String questionToSpeak;
	private TopicDialog topicDialog;
	private SharedPreferences sharedPreferences;
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private String[] drawerTitles;
	private ActionBarDrawerToggle actionBarDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.onCreate(savedInstanceState is " +
				(savedInstanceState==null?"":"not ") + "null)");
		sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
		setContentView(R.layout.activity_main);
		this.statusTextView = (TextView) findViewById(R.id.statusTextView);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		this.drawerList = (ListView) findViewById(R.id.left_drawer);
		Resources resources = getResources();
		this.drawerTitles = resources.getStringArray(R.array.navigationDrawerTitles);
		TypedArray iconArray = resources.obtainTypedArray(R.array.navigationDrawIcons);
		int drawerTitlesLength = drawerTitles.length;
		DrawerItem[] drawerItems = new DrawerItem[drawerTitlesLength];
		for (int i = 0; i < drawerTitlesLength; i++) {
			drawerItems[i] = new DrawerItem(iconArray.getDrawable(i), drawerTitles[i]);
		}
		iconArray.recycle();
		DrawerItemAdapter adapter = new DrawerItemAdapter(this,
				R.layout.drawer_list_item, drawerItems);
		this.drawerList.setAdapter(adapter);
        this.drawerList.setOnItemClickListener(this);

		FragmentManager fragmentManager = getSupportFragmentManager();
		if (savedInstanceState != null) {
			this.engSpaFragment = (EngSpaFragment) fragmentManager.findFragmentByTag(ENGSPA);
			String title = savedInstanceState.getString("title");
			if (title != null) setTitle(title);
		}
		showEngSpaFragment();
	}
	@Override // Activity
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override // OnItemClickListener
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0) {
			if (this.verbTableFragment == null) {
				this.verbTableFragment = new VerbTableFragment();
			}
			showFragment(verbTableFragment, VERB_TABLE);
		} else if (position == 1) {
			if (this.raceFragment == null) {
				this.raceFragment = new RaceFragment();
			}
			showFragment(raceFragment, NUMBER_GAME);
		} else if (position == 2) {
			showTopicDialog();
		} else if (position == 3) {
			this.engSpaFragment.setTopic(null);
		} else if (position == 4) {
			// TODO: help
			this.statusTextView.setText("help is on its way!");
		} else {
			this.statusTextView.setText("unrecognised item position: " + position);
		}
		this.drawerList.setItemChecked(position, true);
		this.drawerList.setSelection(position);
		getSupportActionBar().setTitle(this.drawerTitles[position]);
		this.drawerLayout.closeDrawer(this.drawerList);
    }	
	@Override // Activity
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.userSettings) {
			if (this.userDialog == null) this.userDialog = new UserDialog();
			this.userDialog.show(getSupportFragmentManager(), "UserSettingsDialog");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override // EngSpaActivity
	public void showTopicDialog() {
		if (this.topicDialog == null) this.topicDialog = new TopicDialog();
		this.topicDialog.show(getSupportFragmentManager(), "TopicDialog");
	}
	@Override // TopicDialog.TopicListener
	public void onTopicSelected(String topic) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.onTopicSelected(" + topic + ")");
		this.engSpaFragment.setTopic(topic);
	}
	@Override // OnInitListener (called when textToSpeech is initialised)
	public void onInit(int status) {
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onInit()");
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		this.statusTextView.setText("");
		if (status == TextToSpeech.SUCCESS) {
			if (this.textToSpeech == null) {
				// this could happen if activity is paused between creating
				// new textToSpeech and getting the response back here
				this.statusTextView.setText("textToSpeech closed down");
				return;
			}
			int result = textToSpeech.setLanguage(LOCALE_ES);
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "textToSpeech.setLanguage(); result=" + result);
			}
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				this.statusTextView.setText("TextToSpeech for Spanish is not supported");
			}
			if (this.questionToSpeak != null) speakQuestion2(); 
		} else {
			Log.w(TAG, "MainActivity.onInit(" + status + ")");
			this.statusTextView.setText(
				"Initilization of textToSpeech failed! Have you installed text-to-speech?");
		}
	}
	@Override // Activity
	public void onPause() {
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onPause()");
		super.onPause();
		if (this.textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
	}
	@Override // Activity
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("title", getTitle().toString());
		super.onSaveInstanceState(savedInstanceState);
	}
	@Override // EngSpaActivity
	public void speakQuestion(String question) {
		this.questionToSpeak = question;
		if (this.textToSpeech == null) {
			// invokes onInit() on completion
			textToSpeech = new TextToSpeech(this, this);
			this.statusTextView.setText("loading textToSpeech...");
			this.progressBar.setVisibility(ProgressBar.VISIBLE);
		} else {
			speakQuestion2();
		}
	}
	@SuppressWarnings("deprecation")
	private void speakQuestion2() {
		textToSpeech.speak(this.questionToSpeak, TextToSpeech.QUEUE_ADD, null);
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
	@Override // EngSpaActivity
	public void checkDataFileVersion() {
		engSpaFileModified = false;
		this.statusTextView.setText("checking for updates...");
		this.progressBar.setVisibility(ProgressBar.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				long savedVersion = sharedPreferences.getLong(DATA_VERSION_KEY, 0);
				try {
					String urlStr = QuizCache.serverUrlStr + "engspa.txt?attredirects=0&d=1";
					dateEngSpaFileModified = MyHttpClient.getLastModified(urlStr);
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
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.dataFileCheckComplete(" + updated + ")");
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
						EngSpaDAO engSpaDAO = engSpaFragment.getEngSpaDAO();
						engSpaDAO.newDictionary(contentValues);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putLong(DATA_VERSION_KEY, dateEngSpaFileModified);
						editor.commit();
						updateStatus = "";
					} catch (IOException e) {
						Log.e(TAG, "Exception in onUpdateDecision() " + e);
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
		if (tag == VERB_TABLE || tag == NUMBER_GAME) ft.addToBackStack(tag);
		ft.commit();
	}

	/**
	 * Called on completion of UserDialog, which was called either
	 * because there is no EngSpaUser yet defined, or because the
	 * user chose to update it.
	 */
	@Override // UserSettingsListener
	public void onUpdateUser(String userName, int userLevel, QuestionStyle questionStyle) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.onUpdateUser(" + userName + ", " + userLevel +
				", " + questionStyle + ")");
		if (userName.length() < 1) {
			this.statusTextView.setText("no user name supplied");
			return;
		}
		if (userLevel < 1) {
			this.statusTextView.setText("invalid userLevel supplied");
			return;
		}
		this.engSpaFragment.setUser(userName, userLevel, questionStyle);
	}
	@Override // UserSettingsListener
	public EngSpaUser getEngSpaUser() {
		return this.engSpaFragment.getEngSpaUser();
	}
	
	@Override // EngSpaActivity
	public void setStatus(String status) {
		this.statusTextView.setText(status);
	}
	@Override // EngSpaActivity
	public String getTag() {
		return TAG;
	}
	/**
	 * questionSequence is a sequence number that is incremented
	 * each time a question is asked.
	 */
	@Override // EngSpaActivity
	public int getQuestionSequence() {
		if (questionSequenceKey == null) {
			questionSequenceKey = "QSN_" + getEngSpaUser().getUserId();
		}
		int questionSeq = sharedPreferences.getInt(questionSequenceKey, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(questionSequenceKey, ++questionSeq);
		editor.commit();
		return questionSeq;
	}
}
