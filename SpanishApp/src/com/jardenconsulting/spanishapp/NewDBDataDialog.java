package com.jardenconsulting.spanishapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class NewDBDataDialog extends DialogFragment implements
DialogInterface.OnClickListener {
	private UpdateDBListener updateDBListener;

	public interface UpdateDBListener {
		void onUpdateDecision(boolean doUpdate);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.updateDBListener = (UpdateDBListener) activity;
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Install updated dictionary from server?");
        builder.setPositiveButton("Yes", this)
        		.setNegativeButton("No", this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Log.d("NewDBDataDialog", "onClick(" + which + ")");
		this.updateDBListener.onUpdateDecision(which == DialogInterface.BUTTON_POSITIVE);
	}
}
