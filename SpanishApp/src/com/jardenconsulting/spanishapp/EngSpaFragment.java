package com.jardenconsulting.spanishapp;

import jarden.provider.engspa.EngSpaContract;
import jarden.provider.engspa.EngSpaContract.Attribute;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.QuestionStyle;
import jarden.provider.engspa.EngSpaContract.VoiceText;
import jarden.provider.engspa.EngSpaContract.WordType;
import jarden.engspa.EngSpa;
import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaQuiz.QuestionType;
import jarden.engspa.EngSpaUser;
import jarden.engspa.EngSpaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class EngSpaFragment extends Fragment implements OnClickListener,
		OnEditorActionListener, OnInitListener, LoaderCallbacks<Cursor> {
	public final static int WORD_LOADER_ID = 1;
	private final static int PHRASE_ACTIVITY_CODE = 1002; 

	private final static Locale LOCALE_ES = new Locale("es", "ES");
	private TextView userNameTextView;
	private TextView userLevelTextView;
	private TextView currentCtTextView;
	private TextView failCtTextView;
	private TextView questionTextView;
	private TextView attributeTextView;
	private EditText answerEditText;
	private TextView statusTextView;
	private String question;
	private String correctAnswer;
	private String responseIfCorrect;
	private TextToSpeech textToSpeech;
	private Random random = new Random();
	private EngSpaQuiz engSpaQuiz;
	private EngSpaUser engSpaUser;
	private QuestionStyle currentQuestionStyle;
	private ViewGroup selfMarkLayout;
	private ViewGroup buttonLayout;
	private Button repeatButton;
	private ImageButton micButton;
	
	@Override // Fragment
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onCreate(" +
				(savedInstanceState==null?"":"not ") + "null)");
		setRetainInstance(true);
		MainActivity mainActivity = (MainActivity) getActivity();
		this.engSpaUser = loadUserFromDB();
		if (this.engSpaUser == null) { // i.e. no user yet on database
			mainActivity.showUserDialog();
		}
		if (savedInstanceState == null) { // only check for dictionary updates
				// when app is opened, not restarted
			mainActivity.checkDataFileVersion();
		}
	}
	@Override // Fragment
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onCreateView()");
		// Potentially restore state after configuration change; before we re-create
		// the views, get relevant information from current values. See knowledgeBase.txt
		String pendingAnswer = null;
		int selfMarkLayoutVisibility = View.GONE;
		if (this.answerEditText != null) pendingAnswer = answerEditText.getText().toString();
		if (this.selfMarkLayout != null) selfMarkLayoutVisibility = selfMarkLayout.getVisibility();
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		this.userNameTextView = (TextView) rootView.findViewById(R.id.userNameTextView);
		this.userLevelTextView = (TextView) rootView.findViewById(R.id.userLevelTextView);
		this.currentCtTextView = (TextView) rootView.findViewById(R.id.currentCtTextView);
		this.failCtTextView = (TextView) rootView.findViewById(R.id.failCtTextView);
		this.selfMarkLayout = (ViewGroup) rootView.findViewById(R.id.selfMarkLayout);
		this.buttonLayout = (ViewGroup) rootView.findViewById(R.id.buttonLayout);
		this.selfMarkLayout.setVisibility(View.GONE);
		Button button = (Button) rootView.findViewById(R.id.goButton);
		button.setOnClickListener(this);
		this.repeatButton = (Button) rootView.findViewById(R.id.repeatButton);
		this.repeatButton.setOnClickListener(this);
		this.micButton = (ImageButton) rootView.findViewById(R.id.micButton);
		this.micButton.setOnClickListener(this);
		button = (Button) rootView.findViewById(R.id.incorrectButton);
		button.setOnClickListener(this);
		button = (Button) rootView.findViewById(R.id.correctButton);
		button.setOnClickListener(this);
		this.questionTextView = (TextView) rootView.findViewById(R.id.questionTextView);
		this.attributeTextView = (TextView) rootView.findViewById(R.id.attributeTextView);
		this.answerEditText = (EditText) rootView.findViewById(R.id.answerEditText);
		if (pendingAnswer != null) this.answerEditText.setText(pendingAnswer);
		if (selfMarkLayoutVisibility == View.VISIBLE) showSelfMarkLayout();
		this.answerEditText.setOnEditorActionListener(this);
		this.statusTextView = (TextView) rootView.findViewById(R.id.statusTextView);
		return rootView;
	}
	public void loadDB() {
		getActivity().getSupportLoaderManager().initLoader(
				WORD_LOADER_ID, null, this);
	}
	
	private EngSpaUser loadUserFromDB() {
		ContentResolver contentResolver = getActivity().getContentResolver();
		String selection = null;
		String sortOrder = null;
		Cursor cursor = contentResolver.query(
				EngSpaContract.CONTENT_URI_USER,
				EngSpaContract.PROJECTION_ALL_USER_FIELDS,
				selection, null, sortOrder);
		if (cursor.moveToFirst()) {
			int userId = cursor.getInt(0);
			String userName = cursor.getString(1);
			int userLevel = cursor.getInt(2);
			String questionStyleStr = cursor.getString(3);
			QuestionStyle questionStyle = QuestionStyle.valueOf(questionStyleStr);
			EngSpaUser user = new EngSpaUser(userId, userName, userLevel,
					questionStyle);
			Log.i(MainActivity.TAG, "retrieved from database: " + user);
			return user;
		} else return null;
	}

	/*
	 * part 1 of next question: set up business model, i.e. attributes
	 * which are not part of UI
	 */
	private void nextQuestion() {
		QuestionType questionType = QuestionType.WORD;
		String spanish = engSpaQuiz.getNextQuestion(questionType);
		String english = engSpaQuiz.getEnglish();
		
		QuestionStyle questionStyle = this.engSpaUser.getQuestionStyle();
		if (questionStyle == QuestionStyle.random) {
			// minus 1 as last one is Random itself:
			int randInt = random.nextInt(QuestionStyle.values().length - 1);
			this.currentQuestionStyle = QuestionStyle.values()[randInt];
		} else {
			this.currentQuestionStyle = questionStyle;
		}
		this.responseIfCorrect = "Right!";
		if (this.currentQuestionStyle.spaQuestion) {
			this.question = spanish;
			if (this.currentQuestionStyle.spaAnswer) {
				this.responseIfCorrect = "Right! " + english;
			}
		} else {
			this.question = english;
		}
		if (this.currentQuestionStyle.spaAnswer) {
			this.correctAnswer = spanish;
		} else {
			this.correctAnswer = english;
		}
		this.answerEditText.getText().clear();
		askQuestion();
	}
	/*
	 * part 2 of next question: set up UI from business model
	 */
	private void askQuestion() {
		Attribute attribute = engSpaQuiz.getCurrentWord().getAttribute();
		if (attribute != Attribute.n_a) {
			this.attributeTextView.setText("hint: " + attribute.toString());
		} else {
			this.attributeTextView.setText("");
		}
		if (this.currentQuestionStyle.voiceText == VoiceText.text) {
			this.repeatButton.setVisibility(View.INVISIBLE);
		} else {
			this.repeatButton.setVisibility(View.VISIBLE);
			speakQuestion();
		}
		if (this.currentQuestionStyle.spaAnswer) {
			this.answerEditText.setHint(R.string.spanishStr);
			this.micButton.setVisibility(View.VISIBLE);
		} else {
			this.answerEditText.setHint(R.string.englishStr);
			this.micButton.setVisibility(View.INVISIBLE);
		}
		if (this.currentQuestionStyle.voiceText == VoiceText.voice) {
			this.questionTextView.setText("");
		} else {
			this.questionTextView.setText(this.question);
		}
	}
	public EngSpaQuiz getEngSpaQuiz() {
		return this.engSpaQuiz;
	}
	private void speakQuestion() {
		if (this.textToSpeech == null) {
			new Thread(new Runnable() {
				public void run() {
					EngSpaFragment.this.textToSpeech = new TextToSpeech(
						getActivity(), EngSpaFragment.this); // invokes onInit() on completion
				}
			}).start();
		} else {
			speakQuestion2();
		}
	}
	@SuppressWarnings("deprecation")
	private void speakQuestion2() {
		textToSpeech.speak(question, TextToSpeech.QUEUE_ADD, null);
	}

	@Override // onClickListener
	public void onClick(View view) {
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.setStatus("");
		int id = view.getId();
		if (id == R.id.goButton) {
			goPressed();
		} else if (id == R.id.repeatButton) {
			askQuestion();
		} else if (id == R.id.micButton) {
			startSpeechActivity();
		} else if (id == R.id.correctButton) {
			selfMarkButton(true);
		} else if (id == R.id.incorrectButton) {
			selfMarkButton(false);
		}
	}
	private void startSpeechActivity() {
		Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
	    speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
		startActivityForResult(speechIntent, PHRASE_ACTIVITY_CODE);
	}
	/**
	 * Handle the results from the voice recognition activity.
	 * Runs on main (UI) thread.
	 */
	@Override // Fragment
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (BuildConfig.DEBUG) {
			Log.d(MainActivity.TAG,
					"EngSpaFragment.onActivityResult(resultCode=" + resultCode);			
		}
		if (resultCode == Activity.RESULT_OK) {
			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			for (String match: matches) {
				Log.d(MainActivity.TAG, "match=" + match);
				if (match.equalsIgnoreCase(this.correctAnswer)) {
					this.responseIfCorrect = "Right! " + match;
					setIsCorrect(true);
					return;
				}
			}
			setIsCorrect(false, matches.get(0) + " is wrong");
		} else {
			this.statusTextView.setText(
					"resultCode from speech recognition: " + resultCode);
		}
	}
	private String getSuppliedAnswer() {
		return this.answerEditText.getText().toString().trim();
	}
	private void selfMarkButton(boolean isCorrect) {
		this.statusTextView.setText("");
		showButtonLayout();
		engSpaQuiz.setCorrect(isCorrect);
		showStats();
		nextQuestion();
	}
	private void showSelfMarkLayout() {
		this.buttonLayout.setVisibility(View.GONE);
		this.selfMarkLayout.setVisibility(View.VISIBLE);
	}
	private void showButtonLayout() {
		this.selfMarkLayout.setVisibility(View.GONE);
		this.buttonLayout.setVisibility(View.VISIBLE);
	}
	/*
	if answer supplied:
		isCorrect = check if correct
		inform engSpaQuiz of result
		if correct: nextQuestion()
		showStats()
	else:
		switch button layout to selfMark (yes, no)
		display correctAnswer in answer field
		if spokenSpaToSpa also show English
	 */
	private void goPressed() {
		String suppliedAnswer = getSuppliedAnswer().trim();
		if (suppliedAnswer.length() == 0) {
			showSelfMarkLayout();
			this.answerEditText.setText(this.correctAnswer);
			if (this.currentQuestionStyle.voiceText == VoiceText.voice) {
				// if question was spoken only, user may want to see the translated word
				if (this.currentQuestionStyle.spaAnswer) {
					this.statusTextView.setText(engSpaQuiz.getEnglish());
				} else {
					this.statusTextView.setText(engSpaQuiz.getSpanish());
				}
			} else {
				this.statusTextView.setText("");
			}
		} else {
			String normalisedCorrectAnswer = normalise(this.correctAnswer);
			String normalisedSuppliedAnswer = normalise(suppliedAnswer);
			boolean isCorrect = normalisedCorrectAnswer.equals(normalisedSuppliedAnswer);
			if (!isCorrect && this.currentQuestionStyle.spaAnswer) {
				int res = EngSpaUtils.compareSpaWords(normalisedCorrectAnswer,
						normalisedSuppliedAnswer);
				if (res >= 0) {
					isCorrect = true;
					this.responseIfCorrect += " but note accent: " +
							this.correctAnswer + "; your answer: " +
							suppliedAnswer;
				}
			}
			setIsCorrect(isCorrect);
		}
	}
	private void setIsCorrect(boolean isCorrect) {
		setIsCorrect(isCorrect, "Wrong!");
	}
	private void setIsCorrect(boolean isCorrect, String responseIfWrong) {
		engSpaQuiz.setCorrect(isCorrect);
		if (isCorrect) {
			this.statusTextView.setText(this.responseIfCorrect);
			nextQuestion();
		} else {
			this.statusTextView.setText(responseIfWrong);
			if (this.currentQuestionStyle.voiceText != VoiceText.text) {
				speakQuestion();
			}
		}
		showStats();
	}
	/*
	 * Normalise a word or phrase, to make the comparison more likely to succeed.
	 */
	@SuppressLint("DefaultLocale")
	private static String normalise(String text) {
		StringBuilder builder = new StringBuilder(text.toLowerCase());
		// 'they' in English can be 'ellos' or 'ellas' in Spanish; normalise 'ellas' to 'ellos'
		if (builder.length() >= 5 && builder.substring(0, 5).equals("ellas")) {
			builder.setCharAt(3, 'o');
		}
		// ! at end of imperatives is optional
		int builderLastCharIndex = builder.length() - 1;
		if (builder.charAt(builderLastCharIndex) == '!') {
			builder.deleteCharAt(builderLastCharIndex);
		}
		return builder.toString();
	}
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
	private void showStats() {
		int owct = engSpaQuiz.getOutstandingWordCount();
		int fwct = engSpaQuiz.getFailedWordCount();
		userLevelTextView.setText(Integer.toString(this.engSpaUser.getUserLevel()));
		this.currentCtTextView.setText(Integer.toString(owct));
		this.failCtTextView.setText(Integer.toString(fwct));
		if (BuildConfig.DEBUG) {
			String debugState = engSpaQuiz.getDebugState();
			Log.d(MainActivity.TAG, debugState);
		}
	}

	@Override // OnEditorActionListener
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_GO) {
			goPressed();
			handled = true;
		}
		return handled;
	}
	@Override // Fragment
	public void onResume() {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onResume()");
		super.onResume();
		if (this.question != null) {
			askQuestion();
			showStats();
		}
	}
	@Override // Fragment
	public void onPause() {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onPause()");
		super.onPause();
		if (this.textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
	}
	@Override // Fragment
	public void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onDestroy()");
		super.onDestroy();
		/*
		 * At one point I was going to save the state of a session: the 3 fields
		 * in EngSpa that control the level, so that user could interrupt game,
		 * do something else, then resume where he left off. I decided not to
		 * bother, because:
		 * 		I'd have to save all the currents and fails - as below
		 * 		I'd have to restore the same in populateDictionary() - not done
		 * 		if the user gets interrupted, the data is saved anyway by Android
		 * 		he will only lose the data if he exits the game using the back
		 * 		button, and even then the level will be saved.
		 * So it makes sense to have few words per level.
		 */
		boolean saveWordList = false; // in the original design, would be
						// set true whenever the user answered a question
		if (saveWordList) {
			List<EngSpa> currentWordList = engSpaQuiz.getCurrentWordList();
			List<EngSpa> failedWordList = engSpaQuiz.getFailedWordList();
			ContentValues[] valuesArray =
					new ContentValues[currentWordList.size() +
					failedWordList.size()];
			int listIndex = 0;
			for (EngSpa es: currentWordList) {
				valuesArray[listIndex++] = populateContentValues(es);
			}
			for (EngSpa es: failedWordList) {
				valuesArray[listIndex++] = populateContentValues(es);
			}
			int rows;
			ContentResolver contentResolver = getActivity().getContentResolver();
			rows = contentResolver.delete(EngSpaContract.CONTENT_URI_USER_WORD,
					null, null);
			if (BuildConfig.DEBUG) {
				Log.d(MainActivity.TAG, rows + " rows deleted from table userWord");
			}
			rows = contentResolver.bulkInsert(
					EngSpaContract.CONTENT_URI_USER_WORD, valuesArray);
			if (BuildConfig.DEBUG) {
				Log.d(MainActivity.TAG, rows +
						" rows inserted into table userWord");
			}
		}
	}
	private ContentValues populateContentValues(EngSpa es) {
		ContentValues contentValues = new ContentValues();
		int userId = this.engSpaUser.getUserId();
		contentValues.put(EngSpaContract.USER_ID, userId);
		contentValues.put(EngSpaContract.WORD_ID, es.getId());
		contentValues.put(EngSpaContract.CONSEC_RIGHT_CT, es.getConsecutiveRightCt());
		contentValues.put(EngSpaContract.WRONG_CT, es.getWrongCt());
		contentValues.put(EngSpaContract.LEVELS_WRONG_CT, es.getLevelsWrongCt());
		return contentValues;
	}
	@Override // OnInitListener (called when textToSpeech is initialised)
	public void onInit(int status) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onInit()");
		if (status == TextToSpeech.SUCCESS) {
			int result = textToSpeech.setLanguage(LOCALE_ES);
			if (BuildConfig.DEBUG) {
				Log.d(MainActivity.TAG, "textToSpeech.setLanguage(); result=" + result);
			}
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				setStatusOnUiThread("TextToSpeech for Spanish is not supported");
			}
			// do on UI thread? seems okay as it is
			if (EngSpaFragment.this.question != null) speakQuestion2(); 
		} else {
			Log.w(MainActivity.TAG, "EngSpaFragment.onInit(" + status + ")");
			// this.statusTextView.setText(
			setStatusOnUiThread(
				"Initilization of textToSpeech failed! Have you installed text-to-speech?");
		}
	}
	private void setStatusOnUiThread(final String status) {
		this.statusTextView.post(new Runnable() {
			public void run() {
				statusTextView.setText(status);
			}
		});
	}

	public void onNewLevel(int userLevel) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG,
				"EngSpaFragment.onNewLevel(" + userLevel + ")");
		userUpdated();
	}
	public void onUserUpdated() {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG,
				"EngSpaFragment.onUserUpdated()");
		userUpdated();
	}
	private void userUpdated() {
		showButtonLayout();
		nextQuestion();
		showUserValues();
	}

	@Override // LoaderCallbacks<Cursor>
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onCreateLoader()");
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		return new CursorLoader(
				getActivity(),
				EngSpaContract.CONTENT_URI_ENGSPA,
				EngSpaContract.PROJECTION_ALL_FIELDS,
				selection, selectionArgs, sortOrder);
	}

	@Override // LoaderCallbacks<Cursor>
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onLoadFinished()");
		if (cursor == null) {
			Toast.makeText(getActivity(), "no matching entries found!", Toast.LENGTH_LONG).show();
		} else {
			ArrayList<EngSpa> engSpaList = new ArrayList<EngSpa>();
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String english = cursor.getString(1);
				String spanish = cursor.getString(2);
				String wordTypeStr = cursor.getString(3);
				String qualifierStr = cursor.getString(4);
				String attributeStr = cursor.getString(5);
				WordType wordType = WordType.valueOf(wordTypeStr);
				Qualifier qualifier = Qualifier.valueOf(qualifierStr);
				Attribute attribute = Attribute.valueOf(attributeStr);
				int level = cursor.getInt(6);
				// if (wordType == WordType.verb) // hack for testing verbs
				engSpaList.add(new EngSpa(id, english, spanish,
						wordType, qualifier, attribute, level));
			}
			cursor.close();
			this.engSpaQuiz = new EngSpaQuiz(engSpaList, this.engSpaUser.getUserLevel());
			this.engSpaQuiz.setQuizEventListener((MainActivity) getActivity());
			showUserValues();
			nextQuestion();
		}
	}

	@Override // LoaderCallbacks<Cursor>
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.w(MainActivity.TAG, "EngSpaFragment.onLoaderReset()");
	}

	private void showUserValues() {
		userNameTextView.setText(engSpaUser.getUserName());
		userLevelTextView.setText(Integer.toString(engSpaUser.getUserLevel()));
		showStats();
	}
	public EngSpaUser getEngSpaUser() {
		return this.engSpaUser;
	}
}
