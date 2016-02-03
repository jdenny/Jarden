package com.jardenconsulting.spanishapp;

public interface EngSpaActivity {
	/**
	 * See if we need to update the database from the server file.
	 */
	void checkDataFileVersion();

	/**
	 * Each question is given a unique, increment sequence
	 * number, to help determine when to repeat a question.
	 * @return
	 */
	int getQuestionSequence();
	
	/**
	 * Get DEBUG tag
	 * @return
	 */
	String getTag();
	
	void setProgressBarVisibility(int visibility);

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


