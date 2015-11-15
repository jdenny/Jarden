package jarden.quiz;

public interface QuizListener {
	/**
	 * Called each time user gets question right.
	 */
	public void onRightAnswer();
	/**
	 * Called each time user gets question wrong.
	 */
	public void onWrongAnswer();
	/**
	 * Called each time user gets three questions right first time.
	 */
	public void onThreeRightFirstTime();
	
	public void onReset();
	
	public void onEndOfQuestions();
}
