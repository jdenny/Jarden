package com.jardenconsulting.spanishapp;

import android.support.v4.app.Fragment;

public abstract class GameFragment extends Fragment {
	public abstract void reset();
	public abstract void stop();
	public void onRightAnswer() {
	}
	public void onWrongAnswer() {
	}
	public void onThreeRightFirstTime() {
	}
}
