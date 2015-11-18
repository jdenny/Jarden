package com.jardenconsulting.quizrace;

import com.jardenconsulting.bluetooth.BluetoothListener;
import com.jardenconsulting.bluetooth.BluetoothFragment;
import com.jardenconsulting.bluetooth.BluetoothService;
import com.jardenconsulting.bluetooth.BluetoothService.BTState;

import jarden.app.race.GameData;
import jarden.app.race.QuizRaceIF;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 1 or 2-player linear race version of quiz maze.
 * @author john.denny@gmail.com
 *
 */
public class MainActivity extends FragmentActivity implements QuizRaceIF,
		BluetoothListener {
	public static final String TAG = "QuizRace";
	private FragmentManager fragmentManager;
	private BluetoothFragment bluetoothFragment;
	private InitFragment initFragment;
	private QuizRaceFragment quizRaceFragment;
	private TextView statusText;
	/*
	 * When the app is closing down (using pressing back button or
	 * rotating device) we will probably get notified the connection
	 * is lost, and we don't want to restart BluetoothFragment.
	 */
	private boolean closing = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onCreate()");
		setContentView(R.layout.activity_main);
		statusText = (TextView) findViewById(R.id.statusText);
		initFragment = new InitFragment();
		quizRaceFragment = new QuizRaceFragment();
		quizRaceFragment.setActivity(this);
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.fragmentContainer, initFragment);
		ft.commit();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onPause()");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onDestroy()");
		this.closing = true;
	}
	
	@Override // QuizRaceIF
	public void onModeSelected(int mode) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		quizRaceFragment.setMode(mode);
		ft.remove(initFragment);
		ft.add(R.id.fragmentContainer, quizRaceFragment);
		if (mode == QuizRaceIF.BLUETOOTH_MODE) {
			if (bluetoothFragment == null) {
	 			this.bluetoothFragment = new BluetoothFragment();
	 			ft.add(R.id.bluetoothFragmentContainer, bluetoothFragment);
			}
			ft.hide(quizRaceFragment);
		} else if (mode != QuizRaceIF.SINGLE_USER_MODE) {
			throw new IllegalStateException("unrecognised mode: " + mode);
		}
		ft.commit();
	}

	@Override // BluetoothListener
	public void setStatusMessage(String message) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.setStatusMessage(" + message + ")");
		statusText.setText(message);
	}

	@Override // BluetoothListener
	public void setBluetoothService(BluetoothService bluetoothService) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.setBluetoothService(" +
				bluetoothService==null?"":"not " + "null)");
		if (bluetoothService == null) {
			Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
			finish();
		}
		this.quizRaceFragment.setBluetoothService(bluetoothService);
		
	}

	@Override // BluetoothListener
	public String getHelpString() {
		return "2-player maths race";
	}

	public void bluetoothConnected(String statusMessage) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"MainActivity.bluetoothConnected(" +
				statusMessage + ")");
		setStatusMessage(statusMessage);
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.hide(bluetoothFragment);
		ft.show(quizRaceFragment);
		ft.commit();
	}

	@Override // BluetoothListener
	public void onConnectedAsClient(String deviceName) {
		String statusMessage = "Connected; you are the green one";
		bluetoothConnected(statusMessage);
	}

	@Override // BluetoothListener
	public void onConnectedAsServer(String deviceName) {
		String statusMessage = "Connected; you are the blue one";
		bluetoothConnected(statusMessage);
	}

	@Override // BluetoothListener
	public void onConnectionLost() {
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onConnectionLost()");
		if (this.closing) return;
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.show(bluetoothFragment);
		ft.hide(quizRaceFragment);
		ft.commit();
		setStatusMessage("other player has disconnected; waiting for another player");
	}

	@Override // BluetoothListener
	public void onError(String message) {
		setStatusMessage(message);
	}

	@Override // BluetoothListener
	public void onMessageRead(byte[] data) {
		GameData gameData = new GameData(data[0], data[1], data[2]);
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "MainActivity.onMessageRead(); " + gameData);
		}
		this.quizRaceFragment.setGameData(gameData);
	}

	@Override // BluetoothListener
	public void onMessageToast(String string) {
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

	@Override // BluetoothListener
	public void onPlayerNameChange(String playerName, String playerEmail) {
		if (BuildConfig.DEBUG) Log.d(TAG, "MainActivity.onPlayerNameChange()");
	}

	@Override // BluetoothListener
	public void onStateChange(BTState state) {
		if (BuildConfig.DEBUG) Log.d(TAG,
			"MainActivity.onStateChange(" + state + ")");
		setStatusMessage(state.toString());
	}
}
