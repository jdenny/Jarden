package com.jardenconsulting.quizrace;

import jarden.app.race.QuizRaceIF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InitFragment extends Fragment implements OnClickListener {
	private QuizRaceIF quizRaceListener;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		quizRaceListener = (QuizRaceIF) getActivity();
		View view = inflater.inflate(R.layout.init_layout, container, false);
		Button button = (Button) view.findViewById(R.id.bluetoothModeButton);
		button.setOnClickListener(this);
		button = (Button) view.findViewById(R.id.singleUserModeButton);
		button.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		int mode;
		if (viewId == R.id.bluetoothModeButton) {
			mode = QuizRaceIF.BLUETOOTH_MODE;
		} else if (viewId == R.id.singleUserModeButton) {
			mode = QuizRaceIF.SINGLE_USER_MODE;
		} else {
			throw new IllegalStateException("unrecognised viewId: " + viewId);
		}
		quizRaceListener.onModeSelected(mode);
	}

}
