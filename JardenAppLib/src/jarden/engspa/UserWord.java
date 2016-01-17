package jarden.engspa;

public class UserWord {
	private int userId;
	private int wordId;
	//!! private int wrongCt;
	private int consecutiveRightCt;
	//!! private int levelsWrongCt;
	private int questionSequence = 0;

	public UserWord(int userId, int wordId) {
		this(userId, wordId, 0, 0);
	}
	public UserWord(int userId, int wordId, //!! int wrongCt,
			int questionSequence,
			int consecutiveRightCt /*!!, int levelsWrongCt*/) {
		super();
		this.userId = userId;
		this.wordId = wordId;
		//!! this.wrongCt = wrongCt;
		this.consecutiveRightCt = consecutiveRightCt;
		this.questionSequence = questionSequence;
		//!! this.levelsWrongCt = levelsWrongCt;
	}
	/**
	 * Add result of asking and answering this question.
	 * @param correct: true if answered correctly
	 * @param questionSequence
	 * @return consecutiveRightCt
	 */
	public int addResult(boolean correct, int questionSequence) {
		this.questionSequence = questionSequence;
		if (correct) {
			++consecutiveRightCt;
		} else {
			consecutiveRightCt = 0;
		}
		return consecutiveRightCt;
	}
	public boolean isRecentlyUsed(int questionSequence) {
		int requiredGap = (consecutiveRightCt < 3)?3:10;
		return questionSequence - this.questionSequence <= requiredGap;
	}
	
	/**
	 * if answered wrongly, carried to next level;
	 * if answer wrongly again at next level, carried to next 2 levels (max of two);
	 * i.e. need to answer correctly first time to say okay at this level.
	 * @param userLevel
	 * @return true if needs revision at next level
	 */
	/*!!
	public boolean onIncrementingLevel(int userLevel) {
		if (wrongCt > 0) {
			if (levelsWrongCt < 2) ++levelsWrongCt;
		} else {
			if (levelsWrongCt > 0) --levelsWrongCt;
		}
		wrongCt = 0;
		consecutiveRightCt = 0;
		return levelsWrongCt > 0;
	}
	*/


	@Override
	public String toString() {
		return "UserWord [userId=" + userId + ", wordId=" + wordId +
				/*!!
				+ ", wrongCt=" + wrongCt + ", consecutiveRightCt="
				+ consecutiveRightCt + ", levelsWrongCt=" + levelsWrongCt + "]";
				*/
				", " + toShortString() + "]";
	}
	public String toShortString() {
		return 	"consecRightCt=" + consecutiveRightCt +
				", questionSeq=" + questionSequence;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getWordId() {
		return wordId;
	}

	public void setWordId(int wordId) {
		this.wordId = wordId;
	}

	/*!!
	public int getWrongCt() {
		return wrongCt;
	}

	public void setWrongCt(int wrongCt) {
		this.wrongCt = wrongCt;
	}
	public int getLevelsWrongCt() {
		return levelsWrongCt;
	}

	public void setLevelsWrongCt(int levelsWrongCt) {
		this.levelsWrongCt = levelsWrongCt;
	}
	*/

	public int getConsecutiveRightCt() {
		return consecutiveRightCt;
	}

	public void setConsecutiveRightCt(int consecutiveRightCt) {
		this.consecutiveRightCt = consecutiveRightCt;
	}
	public int getQuestionSequence() {
		return questionSequence;
	}
	public void setQuestionSequence(int questionSequence) {
		this.questionSequence = questionSequence;
	}


}
