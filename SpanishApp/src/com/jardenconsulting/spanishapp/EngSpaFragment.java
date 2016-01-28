package com.jardenconsulting.spanishapp;

import jarden.provider.engspa.EngSpaContract.QuestionStyle;
import jarden.provider.engspa.EngSpaContract.VoiceText;
import jarden.engspa.EngSpaDAO;
import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaQuiz.QuizEventListener;
import jarden.engspa.EngSpaSQLite2;
import jarden.engspa.EngSpaUser;
import jarden.engspa.EngSpaUtils;
import jarden.engspa.UserWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
		OnLongClickListener, OnEditorActionListener, QuizEventListener {
	
	public interface EngSpaActivity {
		String getTag();
		void checkDataFileVersion();
		void speakQuestion(String question);
		void setStatus(String string);
		void showTopicDialog();
		int getQuestionSequence();
	}

	private static final int PHRASE_ACTIVITY_CODE = 1002; 

	private TextView userNameTextView;
	private TextView currentCtTextView;
	private TextView failCtTextView;
	private TextView questionTextView;
	private TextView attributeTextView;
	private EditText answerEditText;
	private TextView statusTextView;
	private ViewGroup selfMarkLayout;
	private ViewGroup buttonLayout;
	private Button repeatButton;
	private ImageButton micButton;
	private String levelStr;

	private Random random = new Random();
	private QuestionStyle currentQuestionStyle;
	private String question;
	private String correctAnswer;
	private String responseIfCorrect;
	private EngSpaQuiz engSpaQuiz;
	private EngSpaUser engSpaUser;
	private EngSpaDAO engSpaDAO;
	private EngSpaActivity engSpaActivity;
	private String tipTip = null;

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
		if (savedInstanceState == null) { // i.e. app first opened, not restarted
			engSpaActivity.checkDataFileVersion();
		}
		this.levelStr = getResources().getString(R.string.levelStr);
		this.engSpaDAO = new EngSpaSQLite2(getActivity(), engSpaActivity.getTag());
		this.engSpaUser = engSpaDAO.getUser();
		if (this.engSpaUser == null) { // i.e. no user yet on database
			this.engSpaUser = new EngSpaUser("your name",
					1, QuestionStyle.writtenSpaToEng);
			engSpaDAO.insertUser(engSpaUser);
			this.tipTip = getResources().getString(R.string.tipTip); // tip for new user
		}
	}
	@Override // Fragment
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(), "EngSpaFragment.onCreateView()");
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
		this.repeatButton = (Button) rootView.findViewById(R.id.repeatButton);
		this.repeatButton.setOnClickListener(this);
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
		this.statusTextView = (TextView) rootView.findViewById(R.id.statusTextView);
		if (tipTip != null) this.statusTextView.setText(tipTip); // tip for new user

		return rootView;
	}
	public void loadDB() {
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(), "EngSpaFragment.loadDB()");
		this.engSpaQuiz = new EngSpaQuiz(engSpaDAO, this.engSpaUser);
		this.engSpaQuiz.setQuizEventListener(this);
		showUserValues();
		nextQuestion();
	}
	
	/*
	 * part 1 of next question: set up business model, i.e. attributes
	 * which are not part of UI
	 */
	private void nextQuestion() {
		String spanish = engSpaQuiz.getNextQuestion2(
				engSpaActivity.getQuestionSequence());
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
		showStats();
	}
	/*
	 * part 2 of next question: set up UI from business model
	 */
	private void askQuestion() {
		this.attributeTextView.setText("hint: " + engSpaQuiz.getCurrentWord().getHint());
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
	private void speakQuestion() {
		this.engSpaActivity.speakQuestion(question);
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
	public void onResume() {
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(), "EngSpaFragment.onResume()");
		super.onResume();
		if (this.question != null) {
			askQuestion();
			showStats();
		}
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
	public void onNewLevel(int userLevel) {
		onNewLevel();
	}
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
	private void onNewLevel() {
		if (BuildConfig.DEBUG) Log.d(engSpaActivity.getTag(),
				"EngSpaFragment.onNewLevel(" +
				engSpaUser.getUserLevel() + ")");
		showButtonLayout();
		nextQuestion();
		showUserValues();
	}

	private void showUserValues() {
		userNameTextView.setText(engSpaUser.getUserName());
		showUserLevel();
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
			QuestionStyle questionStyle) {
		int maxUserLevel = engSpaDAO.getMaxUserLevel();
		if (userLevel > maxUserLevel) {
			userLevel = maxUserLevel;
			this.statusTextView.setText("userLevel set to maximum");
		}
		if (engSpaUser != null &&
				engSpaUser.getUserName().equals(userName) &&
				engSpaUser.getUserLevel() == userLevel &&
				engSpaUser.getQuestionStyle() == questionStyle) {
			this.statusTextView.setText("no changes made to user");
			return false;
		}
		if (engSpaUser == null) { // i.e. new user
			this.engSpaUser = new EngSpaUser(userName,
					userLevel, questionStyle);
			engSpaDAO.insertUser(engSpaUser);
			this.statusTextView.setText(R.string.tipTip); // tip for new user
		} else { // update to existing user
			boolean newLevel = engSpaUser.getUserLevel() != userLevel;
			engSpaUser.setUserName(userName);
			engSpaUser.setUserLevel(userLevel);
			engSpaUser.setQuestionStyle(questionStyle);
			engSpaDAO.updateUser(engSpaUser);
			if (newLevel) {
				getEngSpaQuiz().setUserLevel(userLevel);
			}
		}
		onNewLevel(); // strictly only necessary if change level or questionType
		return true;
	}
	public void setTopic(String topic) {
		if (topic == null) {
			showUserLevel();
		} else {
			getActivity().setTitle("Spanish - " + topic);
		}
		this.engSpaQuiz.setTopic(topic);
		nextQuestion();
	}

	@Override // QuizEventListener
	public void onTopicComplete() {
		showUserLevel();
		engSpaActivity.showTopicDialog();
	}
	private void showUserLevel() {
		getActivity().setTitle("Spanish - " + this.levelStr + " " +
				engSpaUser.getUserLevel());
	}
	public void listFails() {
		// for now, as debug aid, list all fails, i.e. for all users
		// (yes, I know, there is only one at the moment!)
		List<UserWord> userWordList = this.engSpaDAO.getUserWordList(-1);
		for (UserWord userWord: userWordList) {
			Log.d(engSpaActivity.getTag(), "listFails: " + userWord);
		}
	}
	public void deleteFails() {
		// for now, as debug aid, delete all fails, i.e. for all users
		int rowCt = this.engSpaDAO.deleteAllUserWords(-1);
			Log.d(engSpaActivity.getTag(), "userWords deleted: " + rowCt);
	}
}
