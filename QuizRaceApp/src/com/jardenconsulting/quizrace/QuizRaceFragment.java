package com.jardenconsulting.quizrace;

import java.util.HashMap;
import java.util.Locale;

import com.jardenconsulting.bluetooth.BluetoothService;

import jarden.app.race.GameData;
import jarden.app.race.LaneView;
import jarden.app.race.QuizRaceIF;
import jarden.quiz.AlgebraQuiz;
import jarden.quiz.AreasQuiz;
import jarden.quiz.ArithmeticQuiz;
import jarden.quiz.EndOfQuestionsException;
import jarden.quiz.FractionsQuiz;
import jarden.quiz.NumbersQuiz;
import jarden.quiz.Quiz;
import jarden.quiz.SeriesQuiz;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizRaceFragment extends Fragment implements OnClickListener,
			OnInitListener {
	private int mode; // see QuizRaceListener
	private static final long[] WRONG_VIBRATE = {
		0, 200, 200, 200
	};
	private static final long[] LOST_VIBRATE = {
		0, 400, 400, 400, 400, 400
	};
	private static final int CHASER_DELAY_TENTHS = 100;
	// these variables don't change once setup in onCreateView:
    private Vibrator vibrator;
	private TextView questionView;
	private TextView answerView;
	private LaneView laneAView;
	private LaneView laneBView;
	private LaneView laneCView;
	private LaneView myLaneView;
	private LaneView opponentLaneView;
	private TextView levelAView;
	private TextView levelBView;
	private TextView levelCView;
	private TextView myLevelView;
	private TextView opponentLevelView;
	private HashMap<String, String> textToSpeechParams;
	private MainActivity mainActivity;
	// these variables change their values during the game:
	private String question;
	private Chaser chaser;
	private GameData gameData;
	private boolean clientMode = false;
	private TextToSpeech textToSpeech;
	private boolean speakQuestion = false;
	private Quiz quiz;
    private int level;
	private BluetoothService bluetoothService;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainActivity = (MainActivity) getActivity();
		View view = inflater.inflate(R.layout.quizrace_layout, container, false);
		quiz = new ArithmeticQuiz();
		questionView = (TextView) view.findViewById(R.id.question);
		answerView = (TextView) view.findViewById(R.id.answer);
		laneAView = (LaneView) view.findViewById(R.id.laneA);
		laneAView.setAttributes(Color.BLUE);
		laneAView.setBitmapId(R.drawable.goodie);
		levelAView = (TextView) view.findViewById(R.id.laneALevel);
		laneBView = (LaneView) view.findViewById(R.id.laneB);
		laneBView.setAttributes(Color.RED);
		levelBView = (TextView) view.findViewById(R.id.laneBLevel);
		laneCView = (LaneView) view.findViewById(R.id.laneC);
		laneCView.setAttributes(Color.GREEN);
		levelCView = (TextView) view.findViewById(R.id.laneCLevel);
		Button button = (Button) view.findViewById(R.id.button1);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button2);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button3);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button4);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button5);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button6);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button7);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button8);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button9);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.button0);
		button.setOnClickListener(this);
		ImageButton iButton = (ImageButton) view.findViewById(R.id.buttonBackspace);
		iButton.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.buttonGo);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.resetButton);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.helpButton);
		button.setOnClickListener(this);
		setHasOptionsMenu(true);
		if (clientMode) {
			myLaneView = laneCView;
			opponentLaneView = laneAView;
			myLevelView = levelCView;
			opponentLevelView = levelAView;
		} else {
			myLaneView = laneAView;
			opponentLaneView = laneCView;
			myLevelView = levelAView;
			opponentLevelView = levelCView;
		}
		vibrator = (Vibrator) mainActivity.getSystemService(FragmentActivity.VIBRATOR_SERVICE);
		textToSpeechParams = new HashMap<String, String>(); 
		textToSpeechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "theUtId");
		reset();
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);
		menuInflater.inflate(R.menu.quizrace_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int menuId = item.getItemId();
		if (menuId == R.id.arithmetic) {
			quiz = new ArithmeticQuiz();
		} else if (menuId == R.id.numbers) {
			quiz = new NumbersQuiz();
		} else if (menuId == R.id.algebra) {
			quiz = new AlgebraQuiz();
		} else if (menuId == R.id.areas) {
			quiz = new AreasQuiz();
		} else if (menuId == R.id.fractions) {
			quiz = new FractionsQuiz();
		} else if (menuId == R.id.spanish) {
			boolean wasChecked = item.isChecked(); 
			item.setChecked(!wasChecked); // do what Android should do for us!
			this.speakQuestion = !wasChecked;
			return true; // i.e. no need for reset()
		} else if (menuId == R.id.series) {
			quiz = new SeriesQuiz();
		} else {
			return false;
		}
		reset();
		return true;
	}

	@Override
	public void onClick(View view) {
		String answerStr = answerView.getText().toString();
		int id = view.getId();
		if (id == R.id.buttonBackspace) {
			int asl = answerStr.length();
			if (asl > 0) {
				answerStr = answerStr.substring(0, asl - 1);
			}
			answerView.setText(answerStr);
		} else if (id == R.id.buttonGo) {
			checkAnswer();
		} else if (id == R.id.resetButton) {
			reset();
		} else if (id == R.id.helpButton) {
			help();
		} else {
			Button button = (Button) view;
			answerStr += button.getText();
			answerView.setText(answerStr);
		}
	}

	private void help() {
		this.mainActivity.setStatusMessage("select game type from menu");
	}

	private void reset() {
		gameData = new GameData();
		level = 1;
		myLevelView.setText("1");
		levelBView.setText("1");
		myLaneView.setStatus(GameData.RUNNING);
		laneAView.reset();
		laneBView.reset();
		laneCView.reset();
		startChaser();
		nextQuestion();
	}
	
	private void startChaser() {
		if (chaser != null) {
			chaser.stopChaser();
		}
		chaser = new Chaser();
		Thread thread = new Thread(chaser);
		thread.start();
	}

	class Chaser implements Runnable {
		private boolean chasing = true;
		
		@Override
		public void run() {
			while (chasing) {
				int currentChaserDelay = (int) (CHASER_DELAY_TENTHS /
						(1 + 0.5 * level));
				try { Thread.sleep(currentChaserDelay * 100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
            	// check to see if game already over before we
				// create new runnable object:
            	int himPos = laneBView.getPosition();
            	if (himPos >= QuizRaceIF.LANE_LENGTH) return;
				// run code within UI thread
				laneBView.post(new Runnable() {
		            public void run() {
						int himPos = laneBView.moveOn();
						if (himPos >= QuizRaceIF.LANE_LENGTH) {
							logMessage("you've lost!");
							vibrator.vibrate(LOST_VIBRATE, -1);
							stopChaser();
							myLaneView.setStatus(GameData.CAUGHT);
							gameData.status = GameData.CAUGHT;
							transmitData(gameData);
						}
		            }
		        });
			}
		}
		public void stopChaser() {
			this.chasing = false;
		}
	}
	
	private void transmitData(GameData gameData) {
		if (this.mode == QuizRaceIF.BLUETOOTH_MODE) {
			byte[] data = new byte[3];
			data[0] = (byte) gameData.position;
			data[1] = (byte) gameData.level;
			data[2] = (byte) gameData.status;
	        if (bluetoothService.getState() == BluetoothService.BTState.connected) {
				bluetoothService.write(data);
	        } else {
	            Toast.makeText(mainActivity, "Not connected", Toast.LENGTH_LONG).show();
	        }
		}
	}

	private void logMessage(String message) {
		this.mainActivity.setStatusMessage(message);
		if (BuildConfig.DEBUG) {
			Log.d(MainActivity.TAG, message);
		}
	}

	private void nextQuestion() {
		try {
			question = quiz.getNextQuestion(level);
		} catch (EndOfQuestionsException e) {
			throw new RuntimeException(e);
		}
		poseQuestion();
	}
	@SuppressWarnings("deprecation")
	private void poseQuestion() {
		if (this.speakQuestion) {
			questionView.setText("");
			textToSpeech.speak(question,
					TextToSpeech.QUEUE_ADD, textToSpeechParams);
		} else {
			questionView.setText(question + " ");
		}
		answerView.setText("");
	}
	private void checkAnswer() {
		String answerStr = answerView.getText().toString().trim();
		if (answerStr.length() < 1) {
			logMessage("no answer supplied!");
			return;
		}
		int answer = Integer.parseInt(answerStr);
		int result = quiz.isCorrect(answer);
		if (result == Quiz.CORRECT) {
			logMessage("correct");
			if (this.gameData.status != GameData.CAUGHT) {
				int myPos = myLaneView.moveOn();
				if (myPos >= QuizRaceIF.LANE_LENGTH) {
					myLaneView.reset();
					laneBView.reset();
					logMessage("well done!");
					++level;
					myLevelView.setText(String.valueOf(level));
					levelBView.setText(String.valueOf(level));
					gameData.level = level;
				}
				gameData.position = myLaneView.getPosition();
				transmitData(gameData);
			}
			nextQuestion();
		} else if (result == Quiz.FAIL) {
			logMessage(answer + " is wrong! correct answer is " + quiz.getAnswer());
			nextQuestion();
		} else {
			logMessage(answer + " is wrong!");
			vibrator.vibrate(WRONG_VIBRATE, -1);
			poseQuestion();
		}
	}
	
	@Override // OnInitListener (TextToSpeech)
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			Locale targetLocale = new Locale("spa", "MEX");
			int result = textToSpeech.setLanguage(targetLocale);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				logMessage("Language is not supported");
			} else {
				logMessage("textToSpeech initialised okay; locale=" + targetLocale.toString());
			}
		} else {
			logMessage("Initilization Failed! Have you installed text-to-speech?");
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		this.textToSpeech = new TextToSpeech(getActivity(), this);
	}
	@Override
	public void onPause() {
		super.onDestroy();
		if (this.textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
	}
	
	public void setGameData(GameData gameData) {
		opponentLaneView.setData(gameData);
		opponentLevelView.setText(String.valueOf(gameData.level));
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public void setBluetoothService(BluetoothService bluetoothService) {
		this.bluetoothService = bluetoothService;
	}

}
