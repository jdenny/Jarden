package com.jardenconsulting.spanishapp;

import jarden.provider.engspa.EngSpaContract.QAStyle;
import jarden.provider.engspa.EngSpaContract.VoiceText;
import jarden.engspa.EngSpaDAO;
import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaQuiz.QuizEventListener;
import jarden.engspa.EngSpaUser;
import jarden.engspa.EngSpaUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EngSpaFragment extends Fragment implements OnClickListener,
		OnLongClickListener, OnEditorActionListener, QuizEventListener,
		OnInitListener {
	
	private static final int PHRASE_ACTIVITY_CODE = 1002;
	private static final Locale LOCALE_ES = new Locale("es", "ES");

	private TextView userNameTextView;
	private TextView currentCtTextView;
	private TextView failCtTextView;
	private TextView questionTextView;
	private TextView attributeTextView;
	private EditText answerEditText;
	private TextView statusTextView;
	private ViewGroup selfMarkLayout;
	private ViewGroup buttonLayout;
	private ImageButton micButton;
	private String levelStr;

	private Random random = new Random();
	private QAStyle currentQAStyle;
	private String question;
	private String spanish;
	private String correctAnswer;
	private String responseIfCorrect;
	private EngSpaQuiz engSpaQuiz;
	private EngSpaUser engSpaUser;
	private EngSpaDAO engSpaDAO;
	private EngSpaActivity engSpaActivity;
	private String tipTip = null;
	private TextToSpeech textToSpeech;
	private int orientation;

	@Override // Fragment
	public void onAttach(Activity activity) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onAttach()");
		super.onAttach(activity);
		this.engSpaActivity = (EngSpaActivity) activity;
	}
	@Override // Fragment
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(), "EngSpaFragment.onCreate(" +
				(savedInstanceState==null?"":"not ") + "null)");
		setRetainInstance(true);
		saveOrientation();
		this.levelStr = getResources().getString(R.string.levelStr);
		this.engSpaDAO = engSpaActivity.getEngSpaDAO();
		this.engSpaUser = engSpaDAO.getUser();
		if (this.engSpaUser == null) { // i.e. no user yet on database
			this.engSpaUser = new EngSpaUser("your name",
					1, QAStyle.writtenSpaToEng);
			engSpaDAO.insertUser(engSpaUser);
			this.tipTip = getResources().getString(R.string.tipTip); // tip for new user
		}
		this.engSpaQuiz = new EngSpaQuiz(engSpaDAO, this.engSpaUser);
		this.engSpaQuiz.setQuizEventListener(this);
	}
	@Override // Fragment
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Log.d(engSpaActivity.getTag(),
					"EngSpaFragment.onCreateView(); question=" + question +
					"; textToSpeech is " + (textToSpeech==null?"":"not ") + "null");
		}
		// Potentially restore state after configuration change; before we re-create
		// the views, get relevant information from current values. See knowledgeBase.txt
		String pendingAnswer = null;
		int selfMarkLayoutVisibility = View.GONE;
		if (this.answerEditText != null) pendingAnswer = answerEditText.getText().toString();
		if (this.selfMarkLayout != null) selfMarkLayoutVisibility = selfMarkLayout.getVisibility();
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		this.userNameTextView = (TextView) rootView.findViewById(R.id.userNameTextView);
		this.userNameTextView.setText(this.engSpaUser.getUserName());
		this.currentCtTextView = (TextView) rootView.findViewById(R.id.currentCtTextView);
		this.failCtTextView = (TextView) rootView.findViewById(R.id.failCtTextView);
		failCtTextView.setOnLongClickListener(this);
		rootView.findViewById(R.id.failCtLabel).setOnLongClickListener(this);

		this.selfMarkLayout = (ViewGroup) rootView.findViewById(R.id.selfMarkLayout);
		this.buttonLayout = (ViewGroup) rootView.findViewById(R.id.buttonLayout);
		this.selfMarkLayout.setVisibility(View.GONE);
		Button button = (Button) rootView.findViewById(R.id.goButton);
		button.setOnClickListener(this);
		button.setOnLongClickListener(this);
		this.micButton = (ImageButton) rootView.findViewById(R.id.micButton);
		this.micButton.setOnClickListener(this);
		this.micButton.setOnLongClickListener(this);
		button = (Button) rootView.findViewById(R.id.incorrectButton);
		button.setOnClickListener(this);
		button.setOnLongClickListener(this);
		button = (Button) rootView.findViewById(R.id.correctButton);
		button.setOnClickListener(this);
		button.setOnLongClickListener(this);
		this.questionTextView = (TextView) rootView.findViewById(R.id.questionTextView);
		this.attributeTextView = (TextView) rootView.findViewById(R.id.attributeTextView);
		this.answerEditText = (EditText) rootView.findViewById(R.id.answerEditText);
		if (pendingAnswer != null) this.answerEditText.setText(pendingAnswer);
		if (selfMarkLayoutVisibility == View.VISIBLE) showSelfMarkLayout();
		this.answerEditText.setOnEditorActionListener(this);
		// NOTE: leave statusTextView as the last view to be initialised
		// as this is used to see if the views are all initialised
		this.statusTextView = (TextView) rootView.findViewById(R.id.statusTextView);
		showUserLevel();
		if (tipTip != null) this.statusTextView.setText(tipTip); // tip for new user
		return rootView;
	}
	@Override // Fragment
	public void onResume() {
		if (BuildConfig.DEBUG) {
			Log.d(engSpaActivity.getTag(),
					"EngSpaFragment.onResume(); question=" + question +
					"; textToSpeech is " + (textToSpeech==null?"":"not ") + "null");
		}
		super.onResume();
		askQuestion(false);
	}
	
	/*
	 * part 1 of next question: set up business model, i.e. attributes
	 * which are not part of UI
	 */
	private void nextQuestion() {
		this.spanish = engSpaQuiz.getNextQuestion2(
				engSpaActivity.getQuestionSequence());
		this.engSpaActivity.setSpanish(spanish);
		String english = engSpaQuiz.getEnglish();
		
		QAStyle qaStyle = this.engSpaUser.getQAStyle();
		if (qaStyle == QAStyle.random) {
			// minus 1 as last one is Random itself:
			int randInt = random.nextInt(QAStyle.values().length - 1);
			this.currentQAStyle = QAStyle.values()[randInt];
		} else {
			this.currentQAStyle = qaStyle;
		}
		this.responseIfCorrect = "Right!";
		if (this.currentQAStyle.spaQuestion) {
			this.question = spanish;
			if (this.currentQAStyle.spaAnswer) {
				this.responseIfCorrect = "Right! " + english;
			}
		} else {
			this.question = english;
		}
		if (this.currentQAStyle.spaAnswer) {
			this.correctAnswer = spanish;
		} else {
			this.correctAnswer = english;
		}
		this.answerEditText.getText().clear();
	}
	/*
	 * part 2 of next question: set up UI from business model
	 */
	private void askQuestion() {
		String hint = engSpaQuiz.getCurrentWord().getHint();
		if (hint.length() > 0) hint = "hint: " + hint;
		this.attributeTextView.setText(hint);
		if (this.currentQAStyle.voiceText != VoiceText.text) {
			speakSpanish(this.question);
		}
		if (this.currentQAStyle.spaAnswer) {
			this.answerEditText.setHint(R.string.spanishStr);
			this.micButton.setVisibility(View.VISIBLE);
		} else {
			this.answerEditText.setHint(R.string.englishStr);
			this.micButton.setVisibility(View.INVISIBLE);
		}
		if (this.currentQAStyle.voiceText == VoiceText.voice) {
			this.questionTextView.setText("");
		} else {
			this.questionTextView.setText(this.question);
		}
	}
	public EngSpaQuiz getEngSpaQuiz() {
		return this.engSpaQuiz;
	}

	@Override // onClickListener
	public void onClick(View view) {
		engSpaActivity.setStatus("");
		int id = view.getId();
		if (id == R.id.goButton) {
			goPressed();
		} else if (id == R.id.micButton) {
			startRecogniseSpeechActivity();
		} else if (id == R.id.correctButton) {
			selfMarkButton(true);
		} else if (id == R.id.incorrectButton) {
			selfMarkButton(false);
		}
	}
    @Override // OnLongClickListener
    public boolean onLongClick(View view) {
		int id = view.getId();
		if (id == R.id.goButton) {
			this.statusTextView.setText(R.string.goButtonTip);
			return true;
		} else if (id == R.id.correctButton) {
			this.statusTextView.setText(R.string.correctButtonTip);
			return true;
		} else if (id == R.id.incorrectButton) {
			this.statusTextView.setText(R.string.incorrectButtonTip);
			return true;
		} else if (id == R.id.micButton) {
			this.statusTextView.setText(R.string.micButtonTip);
			return true;
		} else if (id == R.id.failCtLabel || id == R.id.failCtTextView) {
			this.statusTextView.setText(R.string.failCtTip);
			return true;
		}
		return false;
    }
	@Override // OnInitListener (called when textToSpeech is initialised)
	public void onInit(int status) {
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(), "EngSpaFragment.onInit()");
		engSpaActivity.setProgressBarVisible(false);
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
				Log.d(engSpaActivity.getTag(), "textToSpeech.setLanguage(); result=" + result);
			}
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				this.statusTextView.setText("TextToSpeech for Spanish is not supported");
			}
			if (this.spanish != null) speakQuestion2(); 
		} else {
			Log.w(engSpaActivity.getTag(), "EngSpaFragment.onInit(" + status + ")");
			this.statusTextView.setText(
				"Initilization of textToSpeech failed! Have you installed text-to-speech?");
		}
	}
	@Override // Activity
	public void onPause() {
		super.onPause();
		// TODO: why doesn't this work?
		if (// !isOrientationChanged() &&
				this.textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
			if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(),
					"EngSpaFragment.onPause(); textToSpeech closed");
		}
	}
	// return true if orientation changed since previous call
	@SuppressWarnings("unused")
	private boolean isOrientationChanged() {
		int oldOrientation = this.orientation;
		saveOrientation();
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(),
				"EngSpaFragment.getOrientation(); orientation was: " +
				oldOrientation + ", is: " + this.orientation);
		return this.orientation != oldOrientation;
	}
	private void saveOrientation() {
		this.orientation = getResources().getConfiguration().orientation;
	}
	public void speakSpanish(String spanish) {
		this.spanish = spanish;
		if (this.textToSpeech == null) {
			// invokes onInit() on completion
			textToSpeech = new TextToSpeech(getActivity(), this);
			this.statusTextView.setText("loading textToSpeech...");
			engSpaActivity.setProgressBarVisible(true);
		} else {
			speakQuestion2();
		}
	}
	@SuppressWarnings("deprecation")
	private void speakQuestion2() {
		textToSpeech.speak(this.spanish, TextToSpeech.QUEUE_ADD, null);
	}


	private void startRecogniseSpeechActivity() {
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
			Log.d(engSpaActivity.getTag(),
					"EngSpaFragment.onActivityResult(resultCode=" + resultCode);			
		}
		if (resultCode == Activity.RESULT_OK) {
			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			for (String match: matches) {
				Log.d(engSpaActivity.getTag(), "match=" + match);
				if (match.equalsIgnoreCase(this.correctAnswer)) {
					this.responseIfCorrect = "Right! " + match;
					setIsCorrect(true);
					return;
				}
			}
			// show no correct answer yet, but allow user another try
			// without penalty:
			this.statusTextView.setText(matches.get(0) + " is wrong");
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
		askQuestion(true);
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
			if (this.currentQAStyle.voiceText == VoiceText.voice) {
				// if question was spoken only, user may want to see the translated word
				if (this.currentQAStyle.spaAnswer) {
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
			if (!isCorrect && this.currentQAStyle.spaAnswer) {
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
		} else {
			this.statusTextView.setText(responseIfWrong);
		}
		askQuestion(isCorrect);
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
	private void showStats() {
		int cwct = engSpaQuiz.getCurrentWordCount();
		int fwct = engSpaQuiz.getFailedWordCount();
		this.currentCtTextView.setText(Integer.toString(cwct));
		this.failCtTextView.setText(Integer.toString(fwct));
		if (BuildConfig.DEBUG) {
			String debugState = engSpaQuiz.getDebugState();
			Log.d(engSpaActivity.getTag(), debugState);
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
	public void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(), "EngSpaFragment.onDestroy()");
		super.onDestroy();
	}

	/**
	 * Notification from EngSpaQuiz that the user has moved up to
	 * the next level.
	 */
	@Override // QuizEventListener
	public void onNewLevel() {
		/*
		 * userLevel can be incremented by EngSpaQuiz when user answered
		 * enough questions, or set by user invoking options menu item
		 * UserDialog at any time.
		 * EngSpaQuiz ->
		 * 		[updates engSpaUser.level]
		 * 		EngSpaFragment.onNewLevel() [I/F QuizEventListener]
		 * 
		 * UserDialog ->
		 * 		MainActivity.onUserUpdate() [I/F UserSettingsListener] ->
		 * 			EngSpaFragment.setUser() ->
		 * 				EngSpaFragment.onNewLevel() [if level changed]
		 */
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(),
				"EngSpaFragment.onNewLevel(" +
				engSpaUser.getUserLevel() + ")");
		showButtonLayout();
		showUserLevel();
		askQuestion(true);
	}
	public EngSpaUser getEngSpaUser() {
		return this.engSpaUser;
	}
	public EngSpaDAO getEngSpaDAO() {
		return this.engSpaDAO;
	}
	/**
	 * Create or update engSpaUser.
	 * @return false if no changes made
	 */
	public boolean setUser(String userName, int userLevel,
			QAStyle qaStyle) {
		int maxUserLevel = engSpaDAO.getMaxUserLevel();
		if (userLevel > maxUserLevel) {
			userLevel = maxUserLevel;
			this.statusTextView.setText("userLevel set to maximum");
		}
		if (engSpaUser != null &&
				engSpaUser.getUserName().equals(userName) &&
				engSpaUser.getUserLevel() == userLevel &&
				engSpaUser.getQAStyle() == qaStyle) {
			this.statusTextView.setText("no changes made to user");
			return false;
		}
		boolean newLevel = true;
		if (engSpaUser == null) { // i.e. new user
			this.engSpaUser = new EngSpaUser(userName,
					userLevel, qaStyle);
			engSpaDAO.insertUser(engSpaUser);
			this.statusTextView.setText(R.string.tipTip); // tip for new user
		} else { // update to existing user
			newLevel = engSpaUser.getUserLevel() != userLevel;
			engSpaUser.setUserName(userName);
			engSpaUser.setUserLevel(userLevel);
			engSpaUser.setQAStyle(qaStyle);
			engSpaDAO.updateUser(engSpaUser);
		}
		userNameTextView.setText(engSpaUser.getUserName());
		if (newLevel) {
			getEngSpaQuiz().setUserLevel(userLevel);
		}
		onNewLevel(); // strictly only necessary if change level or qaStyle
		return true;
	}
	public void setUserQAStyle(QAStyle qaStyle) {
		engSpaUser.setQAStyle(qaStyle);
		onNewLevel();
	}
	public void setTopic(String topic) {
		if (topic == null) {
			showUserLevel();
		} else {
			this.engSpaActivity.setEngSpaTitle(topic);
		}
		this.engSpaQuiz.setTopic(topic);
		askQuestion(true);
	}
	private void askQuestion(boolean getNext) {
		if (getNext || this.question == null) {
			nextQuestion();
		}
		askQuestion();
		showStats();
	}

	@Override // QuizEventListener
	public void onTopicComplete() {
		showUserLevel();
		engSpaActivity.showTopicDialog();
	}
	private void showUserLevel() {
		this.engSpaActivity.setEngSpaTitle(this.levelStr + " " +
				engSpaUser.getUserLevel());
	}
}
