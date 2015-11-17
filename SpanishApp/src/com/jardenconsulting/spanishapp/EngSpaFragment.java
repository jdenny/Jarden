package com.jardenconsulting.spanishapp;

import jarden.provider.engspa.EngSpaContract;
import jarden.provider.engspa.EngSpaContract.Attribute;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.WordType;
import jarden.engspa.EngSpa;
import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaQuiz.QuestionType;
import jarden.engspa.EngSpaUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class EngSpaFragment extends Fragment implements OnClickListener,
		OnEditorActionListener, OnInitListener, LoaderCallbacks<Cursor> {
	public final static int WORD_LOADER_ID = 1;
	// question styles: TODO: can these somehow be consolidated with
	private final static int QAS_SpokenSpaToSpa = 0;
	// private final static int QAS_SpokenSpaToEng = 1;
	// private final static int QAS_SpokenWrittenSpaToEng = 2;
	// private final static int QAS_WrittenSpaToEng = 3;
	private final static int QAS_WrittenEngToSpa = 4;
	private final static int QAS_Random = 5;
	// end of question styles

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
	private int currentQuestionStyleIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onCreate(" +
				(savedInstanceState==null?"":"not ") + "null)");
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.checkDataFileVersion();
		setRetainInstance(true);
	}
	public void loadDB() {
		getActivity().getSupportLoaderManager().initLoader(
				WORD_LOADER_ID, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onCreateView()");
		// Potentially restore state after configuration change; see knowledgeBase.txt
		String pendingAnswer = null;
		if (this.answerEditText != null) pendingAnswer = answerEditText.getText().toString();

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		this.userNameTextView = (TextView) rootView.findViewById(R.id.userNameTextView);
		this.userLevelTextView = (TextView) rootView.findViewById(R.id.userLevelTextView);
		this.currentCtTextView = (TextView) rootView.findViewById(R.id.currentCtTextView);
		this.failCtTextView = (TextView) rootView.findViewById(R.id.failCtTextView);
		
		Button button = (Button) rootView.findViewById(R.id.goButton);
		button.setOnClickListener(this);
		button = (Button) rootView.findViewById(R.id.repeatButton);
		button.setOnClickListener(this);
		button = (Button) rootView.findViewById(R.id.passButton);
		button.setOnClickListener(this);
		this.questionTextView = (TextView) rootView.findViewById(R.id.questionTextView);
		this.attributeTextView = (TextView) rootView.findViewById(R.id.attributeTextView);
		this.answerEditText = (EditText) rootView.findViewById(R.id.answerEditText);
		if (pendingAnswer != null) this.answerEditText.setText(pendingAnswer);
		this.answerEditText.setOnEditorActionListener(this);
		this.statusTextView = (TextView) rootView.findViewById(R.id.statusTextView);
		return rootView;
	}
	private void nextQuestion() {
		QuestionType questionType = QuestionType.WORD;
		String spanish = engSpaQuiz.getNextQuestion(questionType);
		String english = engSpaQuiz.getEnglish();
		EngSpa engSpa = engSpaQuiz.getCurrentWord();
		
		int qStyleIndex = ((MainActivity) getActivity())
				.getEngSpaUser().getQuestionStyleIndex();
		if (qStyleIndex == QAS_Random) {
			this.currentQuestionStyleIndex = random.nextInt(5);
		} else {
			this.currentQuestionStyleIndex = qStyleIndex;
		}
		this.responseIfCorrect = "Right!";
		if (currentQuestionStyleIndex == QAS_WrittenEngToSpa) {
			this.question = english;
			this.correctAnswer = spanish;
		} else if (currentQuestionStyleIndex == QAS_SpokenSpaToSpa) {
			this.question = spanish;
			this.correctAnswer = this.question;
			this.responseIfCorrect = "Right! " + english;
		} else {
			this.question = spanish;
			this.correctAnswer = english;
		}
		Attribute attribute = engSpa.getAttribute();
		if (attribute != Attribute.n_a) {
			this.attributeTextView.setText(attribute.toString());
		} else {
			this.attributeTextView.setText("");
		}
		this.answerEditText.getText().clear();
		askQuestion();
	}
	private void askQuestion() {
		if (currentQuestionStyleIndex == QAS_WrittenEngToSpa) {
			this.answerEditText.setHint(R.string.spanishStr);
		} else if (currentQuestionStyleIndex == QAS_SpokenSpaToSpa) {
			this.answerEditText.setHint(R.string.spanishStr);
		} else {
			this.answerEditText.setHint(R.string.englishStr);
		}
		if (currentQuestionStyleIndex < 3) {
			speakQuestion();
		}
		if (currentQuestionStyleIndex > 1) {
			this.questionTextView.setText(this.question);
		} else {
			this.questionTextView.setText("");
		}
	}
	public EngSpaQuiz getEngSpaQuiz() {
		return this.engSpaQuiz;
	}
	@SuppressWarnings("deprecation")
	private void speakQuestion() {
		textToSpeech.speak(question, TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.goButton) {
			goPressed();
		} else if (id == R.id.passButton) {
			showAnswer();
		} else if (id == R.id.repeatButton) {
			speakQuestion();
		}
	}
	private void showAnswer() {
		String status = this.question;
		if (this.currentQuestionStyleIndex > 0) {
			status += " = " + this.correctAnswer;
		}
		String suppliedAnswer = getSuppliedAnswer();
		if (suppliedAnswer.length() > 0) {
			status += "; yourAnswer: " + suppliedAnswer;
		}
		this.statusTextView.setText(status);
		nextQuestion();
	}
	private String getSuppliedAnswer() {
		return this.answerEditText.getText().toString().trim();
	}
	private void goPressed() {
		String suppliedAnswer = getSuppliedAnswer().trim();
		if (suppliedAnswer.length() == 0) {
			showAnswer();
		} else {
			boolean correct;
			if ((this.correctAnswer.startsWith("ellos ") ||
					this.correctAnswer.startsWith("ellas ")) &&
					(suppliedAnswer.startsWith("ellos ") ||
					suppliedAnswer.startsWith("ellas ")))
				correct = suppliedAnswer.substring(6).equalsIgnoreCase(this.correctAnswer.substring(6));
			else {
				if (this.correctAnswer.endsWith("!")) {
					// ! at end of imperatives is optional
					if (!suppliedAnswer.endsWith("!")) suppliedAnswer += "!";
				}
				correct = suppliedAnswer.equalsIgnoreCase(this.correctAnswer);
			}
			if (correct) {
				engSpaQuiz.setCorrect(true);
				this.statusTextView.setText(this.responseIfCorrect);
				nextQuestion();
			} else {
				engSpaQuiz.setCorrect(false);
				this.statusTextView.setText("Wrong!");
				if (this.currentQuestionStyleIndex < 3) {
					speakQuestion();
				}
			}
			showStats();
		}
	}
	private int getUserLevel() {
		return ((MainActivity) getActivity()).getEngSpaUser().getUserLevel();
	}
	private void showStats() {
		int owct = engSpaQuiz.getOutstandingWordCount();
		int fwct = engSpaQuiz.getFailedWordCount();
		userLevelTextView.setText(Integer.toString(getUserLevel()));
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
	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onResume()");
		super.onResume();
		// TextToSpeech is recreated here because it's shut down in onPause()
		this.textToSpeech = new TextToSpeech(
				getActivity(), this); // invokes onInit() on completion
		if (this.question != null) {
			askQuestion();
			showStats();
		}
	}
	@Override
	public void onPause() {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG, "EngSpaFragment.onPause()");
		super.onPause();
		if (this.textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
	}
	@Override
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
		int userId = ((MainActivity) getActivity()).getEngSpaUser().getUserId();
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
				this.statusTextView.setText("TextToSpeech for Spanish is not supported");
			}
		} else {
			Log.w(MainActivity.TAG, "EngSpaFragment.onInit(" + status + ")");
			this.statusTextView.setText(
				"Initilization of textToSpeech failed! Have you installed text-to-speech?");
		}
	}

	public void onNewLevel(int userLevel) {
		if (BuildConfig.DEBUG) Log.d(MainActivity.TAG,
				"EngSpaFragment.onNewLevel(" + userLevel + ")");
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
			this.engSpaQuiz = new EngSpaQuiz(engSpaList, getUserLevel());
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
		EngSpaUser engSpaUser = ((MainActivity) getActivity()).getEngSpaUser();
		userNameTextView.setText(engSpaUser.getUserName());
		userLevelTextView.setText(Integer.toString(engSpaUser.getUserLevel()));
		showStats();
	}
}
