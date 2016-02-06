package com.jardenconsulting.spanishapp;

import jarden.engspa.EngSpaDAO;
import android.content.SharedPreferences;

public interface EngSpaActivity {
	/**
	 * See if we need to update the database from the server file.
	 */
	void checkDataFileVersion();
	
	EngSpaDAO getEngSpaDAO();

	/**
	 * Each question is given a unique, increment sequence
	 * number, to help determine when to repeat a question.
	 * @return
	 */
	int getQuestionSequence();
	
	SharedPreferences getSharedPreferences();
	
	/**
	 * Get DEBUG tag
	 * @return
	 */
	String getTag();
	
	void setProgressBarVisible(boolean visible);

	/**
	 * Set Spanish word if user later clicks on
	 * speaker button.
	 * @param spanish
	 */
	void setSpanish(String spanish);

	void setStatus(String status);

	void showTopicDialog();

	/**
	 * Set Spanish word (see setSpanish) and speaks it.
	 * @param spanish
	 */
	void speakSpanish(String spanish);
	
	/**
	 * Set engSpa title in the App bar. This is specifically the
	 * title used in EngSpaFragment.
	 * @param title
	 */
	void setEngSpaTitle(String title);
}


