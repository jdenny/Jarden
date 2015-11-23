package com.jardenconsulting.spanishapp;

import jarden.engspa.EngSpaUser;
import jarden.provider.engspa.EngSpaContract;
import jarden.provider.engspa.EngSpaContract.QuestionStyle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class UserDialog extends DialogFragment implements DialogInterface.OnClickListener {
	private EditText userNameEditText;
	private EditText userLevelEditText;
	private Spinner questionStyleSpinner;
	private UserSettingsListener userSettingsListener;
	private AlertDialog alertDialog;
	
	public interface UserSettingsListener {
		void onUpdateUser(String userName, int userLevel, QuestionStyle questionStyle);
		EngSpaUser getEngSpaUser();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.userSettingsListener = (UserSettingsListener) activity;
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		LayoutInflater inflater =activity.getLayoutInflater();
		builder.setTitle(R.string.userSettingsStr);
		View view = inflater.inflate(R.layout.dialog_user, null);
		this.userNameEditText = (EditText) view.findViewById(R.id.userNameEditText);
		this.userLevelEditText = (EditText) view.findViewById(R.id.userLevelEditText);
		this.questionStyleSpinner = (Spinner) view.findViewById(R.id.questionStyleSpinner);
		ArrayAdapter<String> questionStyleAdapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item,
				EngSpaContract.questionStyleNames);
		questionStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		questionStyleSpinner.setAdapter(questionStyleAdapter);
		EngSpaUser user = userSettingsListener.getEngSpaUser();
		if (user == null) {
			// if it's a new engSpaUser, the user must supply the values
			setCancelable(false);
		} else {
			userNameEditText.setText(user.getUserName());
			userLevelEditText.setText(String.valueOf(user.getUserLevel()));
			int position = user.getQuestionStyle().ordinal();
			this.questionStyleSpinner.setSelection(position);
			// cancel button provided only for updates
			builder.setNegativeButton(R.string.cancelStr, this);
		}
		builder.setView(view);
		builder.setPositiveButton(R.string.updateStr, this);
		this.alertDialog = builder.create();
		return alertDialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_NEGATIVE) {
			dialog.cancel();
		} else if (which == DialogInterface.BUTTON_POSITIVE) {
			// stop user doing multiple clicks; if load is slow, she
			// may be tempted to click again
			Button positiveButton = this.alertDialog.getButton(which);
			positiveButton.setEnabled(false);
			String userName = userNameEditText.getText().toString().trim();
			String userLevelStr = userLevelEditText.getText().toString();
			int userLevel;
			try {
				userLevel = Integer.parseInt(userLevelStr);
			} catch (NumberFormatException nfe) {
				userLevel = -1;
			}
			String questionStyleStr = (String) questionStyleSpinner.getSelectedItem();
			QuestionStyle questionStyle = QuestionStyle.valueOf(questionStyleStr);
			this.userSettingsListener.onUpdateUser(userName, userLevel, questionStyle);
		}
	}
}
